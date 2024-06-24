package org.jlab.dtm.presentation.controller.reports;

import java.io.IOException;
import java.math.BigInteger;
import java.util.List;
import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jlab.dtm.business.session.EventAudFacade;
import org.jlab.dtm.persistence.entity.aud.EventAud;
import org.jlab.smoothness.presentation.util.Paginator;
import org.jlab.smoothness.presentation.util.ParamConverter;
import org.jlab.smoothness.presentation.util.ParamUtil;

/**
 * @author ryans
 */
@WebServlet(
    name = "EventAudit",
    urlPatterns = {"/reports/activity-audit/event-audit"})
public class EventAudit extends HttpServlet {

  @EJB EventAudFacade eventAudFacade;

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

    BigInteger eventId = ParamConverter.convertBigInteger(request, "eventId");
    BigInteger revisionId = ParamConverter.convertBigInteger(request, "revisionId");

    int offset = ParamUtil.convertAndValidateNonNegativeInt(request, "offset", 0);
    int maxPerPage = 5;

    List<EventAud> eventList = null;
    Long totalRecords = 0L;

    if (eventId != null) {
      eventList = eventAudFacade.filterList(eventId, revisionId, offset, maxPerPage);
      totalRecords = eventAudFacade.countFilterList(eventId, revisionId);

      eventAudFacade.loadStaff(eventList);
    }

    Paginator paginator = new Paginator(totalRecords.intValue(), offset, maxPerPage);

    request.setAttribute("eventList", eventList);
    request.setAttribute("paginator", paginator);

    request
        .getRequestDispatcher("/WEB-INF/views/reports/activity-audit/event-audit.jsp")
        .forward(request, response);
  }
}
