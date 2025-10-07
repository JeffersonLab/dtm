package org.jlab.dtm.presentation.controller.setup;

import jakarta.ejb.EJB;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;
import org.jlab.dtm.business.session.ScheduledEmailer;
import org.jlab.smoothness.business.service.SettingsService;
import org.jlab.smoothness.business.util.TimeUtil;

/**
 * @author ryans
 */
@WebServlet(
    name = "Email",
    urlPatterns = {"/setup/email"})
public class Email extends HttpServlet {

  @EJB ScheduledEmailer emailer;
  @EJB SettingsService settingsService;

  /**
   * Handles the HTTP <code>GET</code> method.
   *
   * @param request servlet request
   * @param response servlet response
   * @throws ServletException if a servlet-specific error occurs
   * @throws IOException if an I/O error occurs
   */
  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {

    int numberOfHours = 24;

    if (TimeUtil.isMonday()) {
      numberOfHours = 72;
    }

    // Just things that have occurred in last "numberOfHours" hours
    Date end = new Date();
    Date start = (TimeUtil.addHours(end, numberOfHours * -1));

    String ccCsv = SettingsService.cachedSettings.get("EMAIL_EXPERT_CC_LIST");

    request.setAttribute("schedulerEnabled", emailer.isEnabled());
    request.setAttribute("numberOfHours", numberOfHours);
    request.setAttribute("start", start);
    request.setAttribute("end", end);
    request.setAttribute("ccCsv", ccCsv);

    getServletConfig()
        .getServletContext()
        .getRequestDispatcher("/WEB-INF/views/setup/email.jsp")
        .forward(request, response);
  }
}
