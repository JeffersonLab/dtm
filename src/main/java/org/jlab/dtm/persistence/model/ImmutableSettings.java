package org.jlab.dtm.persistence.model;

/**
 * Read-only (immutable) application Settings.
 *
 * <p>Settings are looked up frequently and change infrequently. They're cached in two places: -
 * ServletContext.getAttribute("settings") - For global Servlets/JSP access -
 * SettingsFacade.cachedSettings - For global EJB access (static volatile)
 *
 * <p>The editable JPA Settings entity should be used only by Setup page that edits Settings.
 * Everywhere else should use this ImmutableSettings POJO class via one of the caches above.
 *
 * <p>Settings are initialized at org.jlab.dtm.presentation.util.SettingsCacheInit during app
 * startup. Changes to settings should be done via Setup tab and at that time the caches should be
 * refreshed.
 */
public final class ImmutableSettings {
  private boolean logbookEnabled;
  private boolean emailEnabled;
  private String booksCsv;
  private String expertEmailCcCsv;

  public ImmutableSettings(
      boolean emailEnabled, boolean logbookEnabled, String booksCsv, String expertEmailCcCsv) {
    this.emailEnabled = emailEnabled;
    this.logbookEnabled = logbookEnabled;
    this.booksCsv = booksCsv;
    this.expertEmailCcCsv = expertEmailCcCsv;
  }

  public String getExpertEmailCcCsv() {
    return expertEmailCcCsv;
  }

  public boolean isEmailEnabled() {
    return emailEnabled;
  }

  public boolean isLogbookEnabled() {
    return logbookEnabled;
  }

  public String getBooksCsv() {
    return booksCsv;
  }
}
