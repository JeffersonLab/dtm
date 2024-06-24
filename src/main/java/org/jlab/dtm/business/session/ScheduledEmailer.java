package org.jlab.dtm.business.session;

import java.io.IOException;
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
import org.jlab.smoothness.business.exception.UserFriendlyException;
import org.jlab.smoothness.business.service.EmailService;
import org.jlab.smoothness.business.util.TimeUtil;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/**
 * @author ryans
 */
@Singleton
@DeclareRoles({"dtm-admin", "dtm-reviewer"})
@Startup
public class ScheduledEmailer {

  private static final Logger LOGGER = Logger.getLogger(ScheduledEmailer.class.getName());
  @Resource private TimerService timerService;
  private Timer timer;
  @EJB IncidentFacade incidentFacade;
  @EJB SettingsFacade settingsFacade;

  private final String TIMER_INFO = "ScheduledEmailer";

  @PostConstruct
  private void init() {
    LOGGER.log(Level.FINE, "Canceling Cached Timers");
    listAll();
    clearAll();

    timer = null;

    if (settingsFacade.findSettings().isAutoEmail()) {
      LOGGER.log(Level.FINE, "Creating New Timer");
      enable();
    }
  }

  @PermitAll
  public boolean isEnabled() {
    return timer != null;
  }

  @RolesAllowed("dtm-reviewer")
  public void setEnabled(Boolean enabled) {

    settingsFacade.setAutoEmail(enabled);

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
      config.setInfo(TIMER_INFO);
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
      LOGGER.log(
          Level.FINE,
          "Timer Expression: {0}, Remaining: {1}, Next timeout: {2}",
          new Object[] {t.getSchedule(), t.getTimeRemaining(), t.getNextTimeout()});
    }
  }

  private void clearAll() {
    /*Timers persist by default and may be hanging around after a redeploy*/
    for (Timer t : timerService.getTimers()) {
      // Only cancel Email timers
      if (TIMER_INFO.equals(t.getInfo())) {
        t.cancel();
      }
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

  @RolesAllowed({"dtm-admin", "dtm-reviewer"})
  public void sendExpertActionNeededEmails() throws IOException, MessagingException {
    int numberOfHours = 24;

    if (TimeUtil.isMonday()) {
      numberOfHours = 72;
    }

    List<String> expertList =
        incidentFacade.findAllExpertsWithRecentUnreviewedIncidents(numberOfHours);

    for (String s : expertList) {
      try {
        sendExpertMail(s, numberOfHours);
      } catch (Exception e) {
        LOGGER.log(Level.WARNING, "Unable to send expert action email", e);
      }
    }
  }

  private void sendExpertMail(String username, int numberOfHours)
      throws UserFriendlyException, IOException, MessagingException {
    LOGGER.log(Level.FINE, "Sending email for expert: {0}", username);

    String url = "http://localhost:8080/dtm/expert-email?email=Y&username=" + username;

    Document doc = Jsoup.connect(url).get();

    if (!doc.select("#doNotSend").text().trim().isEmpty()) {
      LOGGER.log(Level.FINE, "Skipping expert email: " + username);
    } else {
      LOGGER.log(Level.FINE, "Sending expert email: " + username);

      String html = doc.outerHtml();

      String subject = "DTM - SME " + username + " Action Needed";
      String toCsv = username + "@jlab.org";

      EmailService emailService = new EmailService();

      String ccCsv = settingsFacade.findSettings().getExpertEmailCcCsv();

      emailService.sendEmail("dtm@jlab.org", "dtm@jlab.org", toCsv, ccCsv, subject, html, true);
    }
  }

  @PermitAll
  public void sendExpertActionNeededEmail(String username)
      throws UserFriendlyException, IOException, MessagingException {
    int numberOfHours = 24;

    if (TimeUtil.isMonday()) {
      numberOfHours = 72;
    }

    sendExpertMail(username, numberOfHours);
  }
}
