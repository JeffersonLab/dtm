package org.jlab.dtm.business.session;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import jakarta.annotation.security.DeclareRoles;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.*;
import jakarta.mail.MessagingException;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jlab.smoothness.business.exception.UserFriendlyException;
import org.jlab.smoothness.business.service.EmailService;
import org.jlab.smoothness.business.service.SettingsService;
import org.jlab.smoothness.business.service.UserAuthorizationService;
import org.jlab.smoothness.business.util.IOUtil;
import org.jlab.smoothness.business.util.TimeUtil;
import org.jlab.smoothness.persistence.view.SettingChangeAction;
import org.jlab.smoothness.persistence.view.User;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/**
 * @author ryans
 */
@Singleton
@DeclareRoles({"dtm-admin", "dtm-reviewer"})
@Startup
@LocalBean // https://www.javacodegeeks.com/2013/03/defining-ejb-3-1-views-local-remote-no-interface.html
public class ScheduledEmailer implements SettingChangeAction {

  private static final Logger LOGGER = Logger.getLogger(ScheduledEmailer.class.getName());
  @Resource private TimerService timerService;
  private Timer timer;
  @EJB IncidentFacade incidentFacade;
  @EJB SettingsService settingsService;

  private final String TIMER_INFO = "ScheduledEmailer";

  @PostConstruct
  private void init() {
    LOGGER.log(Level.FINE, "Canceling Cached Timers");
    listAll();
    clearAll();

    timer = null;

    if (settingsService.getImmutableSettings().is("EMAIL_ENABLED")) {
      LOGGER.log(Level.FINE, "Creating New Timer");
      enable();
    }
  }

  @PermitAll
  public boolean isEnabled() {
    return timer != null;
  }

  private void setEnabled(boolean enabled) {

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

      boolean emailTesting = SettingsService.cachedSettings.is("EMAIL_TESTING_ENABLED");

      List<User> testers = null;

      if (emailTesting) {
        LOGGER.log(Level.INFO, "EMAIL_TESTING_ENABLED=Y (using testlead role)");
        UserAuthorizationService auth = UserAuthorizationService.getInstance();
        testers = auth.getUsersInRole("testlead");
      }

      String toCsv =
          expertRecipientCsv(
              username,
              emailTesting,
              SettingsService.cachedSettings.get("EMAIL_DOMAIN_NAME"),
              testers);

      String sender = SettingsService.cachedSettings.get("EMAIL_SENDER_ADDRESS");

      if (sender == null || sender.isBlank()) {
        throw new UserFriendlyException("No sender configured (EMAIL_SENDER_ADDRESS)");
      }

      EmailService emailService = new EmailService();

      String ccCsv = SettingsService.cachedSettings.get("EMAIL_EXPERT_CC_LIST");

      emailService.sendEmail(sender, sender, toCsv, ccCsv, subject, html, true);
    }
  }

  /**
   * Returns the addresses an expert email goes to: in testing mode, those of the testlead role's
   * members, and otherwise the expert's username followed by the email domain.
   *
   * @param username The expert's username
   * @param emailTesting Whether EMAIL_TESTING_ENABLED is on
   * @param domain The EMAIL_DOMAIN_NAME setting, such as "@jlab.org"
   * @param testers The testlead role's members, or null when not testing
   * @return The recipients as CSV
   * @throws UserFriendlyException If the domain is not set, or if testing and no tester has an
   *     address, so that the email is not sent at all
   */
  static String expertRecipientCsv(
      String username, boolean emailTesting, String domain, List<User> testers)
      throws UserFriendlyException {
    if (emailTesting) {
      Set<String> addressList = new TreeSet<>();

      if (testers != null) {
        for (User user : testers) {
          String email = user.getEmail();

          if (email != null && !email.isBlank()) {
            addressList.add(email.trim());
          }
        }
      }

      if (addressList.isEmpty()) {
        throw new UserFriendlyException(
            "Email testing is on and no member of the testlead role has an email address");
      }

      return IOUtil.toCsv(addressList.toArray());
    }

    if (domain == null || domain.isBlank()) {
      throw new UserFriendlyException("No email domain configured (EMAIL_DOMAIN_NAME)");
    }

    return username + domain.trim();
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

  @Override
  @RolesAllowed("dtm-admin")
  public void handleChange(String key, String value) {
    LOGGER.log(Level.INFO, "SettingChangeAction handleChange: {0}={1}", new Object[] {key, value});
    if ("EMAIL_ENABLED".equals(key)) {
      setEnabled("Y".equals(value));
    }
  }
}
