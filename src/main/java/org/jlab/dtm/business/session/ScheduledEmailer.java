package org.jlab.dtm.business.session;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.annotation.security.DeclareRoles;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.ScheduleExpression;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.Timeout;
import javax.ejb.Timer;
import javax.ejb.TimerConfig;
import javax.ejb.TimerService;
import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import org.jlab.dtm.business.params.IncidentParams;
import org.jlab.dtm.persistence.entity.Incident;
import org.jlab.smoothness.business.exception.UserFriendlyException;
import org.jlab.smoothness.business.service.EmailService;
import org.jlab.smoothness.business.util.TimeUtil;

/**
 *
 * @author ryans
 */
@Singleton
@DeclareRoles({"dtm-reviewer"})
@Startup
public class ScheduledEmailer {

    private static final Logger LOGGER = Logger.getLogger(
            ScheduledEmailer.class.getName());
    @Resource
    private TimerService timerService;
    private Timer timer;
    @EJB
    IncidentFacade incidentFacade;

    @PostConstruct
    private void init() {
        LOGGER.log(Level.FINE, "Canceling Cached Timers");
        listAll();
        clearAll();

        timer = null;

        String expertEmail = System.getenv("DTM_EXPERT_EMAIL");

        if ("true".equals(expertEmail)) {
            LOGGER.log(Level.FINE, "Creating New Timer");
            setEnabled(true);
        }
    }

    @PermitAll
    public boolean isEnabled() {
        return timer != null;
    }

    @RolesAllowed("dtm-reviewer")
    public void setEnabled(Boolean enabled) {
        if (enabled) {
            enable();
        } else {
            disable();
        }
        LOGGER.log(Level.FINE, "Enabled: {0}; Listing Timers", enabled);
        listAll();
    }

    private void enable() {
        if (!isEnabled()) {
            ScheduleExpression schedExp = new ScheduleExpression();
            schedExp.second("0");
            schedExp.minute("55");
            schedExp.hour("9");
            schedExp.dayOfWeek("Mon-Fri"); // Exclude Sat and Sun
            TimerConfig config = new TimerConfig();
            config.setPersistent(false);
            timer = timerService.createCalendarTimer(schedExp, config);
        }
    }

    private void disable() {
        if (isEnabled()) {
            timer.cancel();
            timer = null;
            clearAll();
        }
    }

    private void listAll() {
        for (Timer t : timerService.getTimers()) {
            LOGGER.log(Level.FINE, "Timer Expression: {0}, Remaining: {1}, Next timeout: {2}", new Object[]{t.getSchedule(), t.getTimeRemaining(), t.getNextTimeout()});
        }
    }

    private void clearAll() {
        /*Timers persist by default and may be hanging around after a redeploy*/
        for (Timer t : timerService.getTimers()) {
            t.cancel();
        }
    }

    @Timeout
    private void handleTimeout(Timer timer) {
        LOGGER.log(Level.WARNING, "Sending Auto Emails");

        try {
            sendExpertActionNeededEmails();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Unable to send email on schedule", e);
        }
    }

    @RolesAllowed("dtm-reviewer")
    public void sendExpertActionNeededEmails() throws IOException, MessagingException {
        int numberOfHours = 24;

        if (TimeUtil.isMonday()) {
            numberOfHours = 72;
        }

        List<String> expertList = incidentFacade.findAllExpertsWithRecentUnreviewedIncidents(numberOfHours);

        for (String s : expertList) {
            try {
                sendExpertMail(s, numberOfHours);
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Unable to send expert action email", e);
            }
        }
    }

    private void sendExpertMail(String s, int numberOfHours) throws UserFriendlyException, MalformedURLException, IOException, AddressException, MessagingException {
        LOGGER.log(Level.FINE, "Sending email for expert: {0}", s);

        IncidentParams params = new IncidentParams();
        params.setReviewed(false);
        params.setSmeUsername(s);
        params.setMax(Integer.MAX_VALUE);
        
        // Just things that have occurred in last "numberOfHours" hours
        params.setStart(TimeUtil.addHours(new Date(), numberOfHours * -1));

        List<Incident> incidentList = incidentFacade.filterList(params);

        String proxyServerName = System.getenv("PROXY_SERVER");
        if (proxyServerName == null || proxyServerName.trim().isEmpty()) {
            throw new RuntimeException("PROXY_SERVER env unset; unable to send expert emails");
        }

        String html = "";

        if (incidentList != null && !incidentList.isEmpty()) {
            html = "<html><head><style>th {font-weight: normal;} td {padding: 0.5em;} td {border-right: 1px solid black;} td:last-child {border-right: 1px solid white;}</style></head><body>";
            html = html +"This email is in reference to a system failure recorded in DTM within the last " + numberOfHours + " hours that requires your review.<br/>";
            html = html + "<ul>";

            for (int i = 0; i < incidentList.size(); i++) {
                html = html + "<li>" + incidentList.get(i).getTitle() + "</li>";
            }

            html = html + "</ul>";
        } else {
            throw new UserFriendlyException("No unacknowledged reviews found");
        }

        html = html + "<br/><b>Please conduct a repair assessment and review the incident(s) here:</b>";
        html = html + "<br/><b><a href=\"https://" + proxyServerName + "/dtm/all-events?acknowledged=N&smeUsername=" + s + "&qualified=\">DTM-RAR Review</a></b>";

        html = html + "<br/><br/><h3>Action Level Reference</h3><table style=\"border-bottom: 1px solid black; border-collapse: collapse;\"><tbody>";
        html = html + "<tr style=\"background-color: rgb(128,0,0); color: white;\"><th style=\"border-right: 1px solid white;\"></th><th style=\"border-right: 1px solid white;\">Triggers</th><th style=\"border-right: 1px solid white;\">Action</th><th style=\"border-right: 1px solid white;\">Time</th><th></th></tr>";
        html = html + "<tr style=\"background-color: rgb(255,255,204);\"><td>Level Ⅰ</td><td>Short repairs (5–30 minutes)</td><td>Group Leader or SME review (check a box)</td><td>2 days</td><td></td></tr>";
        html = html + "<tr><td>Level Ⅱ</td><td>Single system, >30 minute repair</td><td>Group Leader or SME review and root cause statement (a couple of sentences)</td><td>2 days</td><td></td></tr>";
        html = html + "<tr style=\"background-color: rgb(255,255,204);\"><td>Level&nbsp;Ⅲ</td><td>4-hour escalation or Director of Operations discretion</td><td>Group Leader or SME root-cause memo (a more lengthy analysis of an event) and a 3–4 minute report at the next Weekly Summary Meeting (Wednesdays at 1330)</td><td>Next Weekly Summary Meeting</td><td><a href=\"https://ace.jlab.org/cdn/doc/dtm/RARTemplate3.docx\">Template</a></td></tr>";
        html = html + "<tr><td>Level&nbsp;Ⅳ</td><td>Program change, safety issue, compounded event, or Director of Operations discretion</td><td>Formal investigation/report by a Repair Investigation Team and follow-up presentation at the Weekly Summary Meeting</td><td>3 weeks</td><td><a href=\"https://ace.jlab.org/cdn/doc/dtm/RARTemplate4.docx\">Template</a></td></tr>";
        html = html + "</tbody></table><br/>Upon completion of Level Ⅲ+ reports upload the document to the DTM incident.";

        html = html + "<br/><br/>The Repair Assessment Report procedure is available online at the following location:";
        html = html + "<br/><a href=\"http://opsntsrv.acc.jlab.org/ops_docs/online_document_files/ACC_online_files/repair_escalation_reporting.pdf\">RAR Procedure</a></body></html>";

        List<InternetAddress> addresses = new ArrayList<>();

        String subject = "DTM - SME " + s + " Action Needed";
        String toCsv = s + "@jlab.org";

        EmailService emailService = new EmailService();

        // String sender, String from, String toCsv, String subject, String body, boolean html
        emailService.sendEmail("dtm@jlab.org", "dtm@jlab.org", toCsv, subject, html, true);
    }

    @PermitAll
    public void sendExpertActionNeededEmail(String username) throws UserFriendlyException, MalformedURLException, IOException, AddressException, MessagingException {
        int numberOfHours = 24;

        if (TimeUtil.isMonday()) {
            numberOfHours = 72;
        }

        sendExpertMail(username, numberOfHours);
    }
}
