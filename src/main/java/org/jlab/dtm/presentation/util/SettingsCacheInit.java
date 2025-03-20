package org.jlab.dtm.presentation.util;

import javax.ejb.EJB;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import org.jlab.dtm.business.session.SettingsFacade;
import org.jlab.dtm.persistence.entity.Settings;
import org.jlab.dtm.persistence.model.ImmutableSettings;

/**
 * A ServletContextListener that provide the settings to the ServletContext to allow easy
 * configuration. The settings are application-scoped (cached) so either require an app restart, or
 * manual refresh from Setup tab in order to requery from database.
 */
@WebListener
public class SettingsCacheInit implements ServletContextListener {

  @EJB SettingsFacade settingsFacade;

  @Override
  public void contextInitialized(ServletContextEvent sce) {
    ServletContext context = sce.getServletContext();

    Settings settings = settingsFacade.findSettings();

    ImmutableSettings immutable = settings.immutable();

    SettingsFacade.cachedSettings = immutable;

    context.setAttribute("settings", immutable);
  }

  @Override
  public void contextDestroyed(ServletContextEvent sce) {
    // Nothing to do
  }
}
