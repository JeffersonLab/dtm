package org.jlab.dtm.presentation.controller.reports;

import jakarta.ejb.EJB;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigInteger;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import org.jlab.dtm.business.session.AbstractFacade;
import org.jlab.dtm.business.session.ApplicationRevisionInfoFacade;
import org.jlab.dtm.business.session.EventTypeFacade;
import org.jlab.dtm.persistence.entity.ApplicationRevisionInfo;
import org.jlab.dtm.persistence.entity.EventType;
import org.jlab.dtm.presentation.util.DtmParamConverter;
import org.jlab.smoothness.presentation.util.Paginator;
import org.jlab.smoothness.presentation.util.ParamConverter;
import org.jlab.smoothness.presentation.util.ParamUtil;

/**
 * @author ryans
 */
@WebServlet(
    name = "ActivityAudit",
    urlPatterns = {"/reports/activity-audit"})
public class ActivityAudit extends HttpServlet {

  @EJB ApplicationRevisionInfoFacade revisionFacade;
  @EJB EventTypeFacade eventTypeFacade;

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

    Date modifiedStart, modifiedEnd;

    try {
      modifiedStart = DtmParamConverter.convertJLabDateTime(request, "modifiedStart");
      modifiedEnd = DtmParamConverter.convertJLabDateTime(request, "modifiedEnd");
    } catch (ParseException e) {
      throw new ServletException("Date format error", e);
    }

    BigInteger eventId = ParamConverter.convertBigInteger(request, "eventId");
    BigInteger incidentId = ParamConverter.convertBigInteger(request, "incidentId");

    int offset = ParamUtil.convertAndValidateNonNegativeInt(request, "offset", 0);
    int maxPerPage = 10;

    List<EventType> eventTypeList =
        eventTypeFacade.findAll(new AbstractFacade.OrderDirective("weight"));

    List<ApplicationRevisionInfo> revisionList =
        revisionFacade.filterList(
            modifiedStart, modifiedEnd, eventId, incidentId, offset, maxPerPage);
    Long totalRecords =
        revisionFacade.countFilterList(modifiedStart, modifiedEnd, eventId, incidentId);

    revisionFacade.loadUser(revisionList);

    Paginator paginator = new Paginator(totalRecords.intValue(), offset, maxPerPage);

    request.setAttribute("eventTypeList", eventTypeList);
    request.setAttribute("revisionList", revisionList);
    request.setAttribute("paginator", paginator);

    request
        .getRequestDispatcher("/WEB-INF/views/reports/activity-audit.jsp")
        .forward(request, response);
  }
}
