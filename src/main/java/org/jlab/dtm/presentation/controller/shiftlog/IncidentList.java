package org.jlab.dtm.presentation.controller.shiftlog;

import java.io.IOException;
import java.math.BigInteger;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jlab.dtm.business.params.IncidentDowntimeReportParams;
import org.jlab.dtm.business.session.EventTypeFacade;
import org.jlab.dtm.business.session.IncidentReportService;
import org.jlab.dtm.business.session.IncidentReportService.IncidentSummary;
import org.jlab.dtm.business.session.SystemFacade;
import org.jlab.dtm.persistence.entity.EventType;
import org.jlab.dtm.persistence.entity.SystemEntity;
import org.jlab.dtm.presentation.util.DtmParamConverter;
import org.jlab.dtm.presentation.util.FilterSelectionMessage;
import org.jlab.smoothness.business.exception.UserFriendlyException;
import org.jlab.smoothness.presentation.util.ParamConverter;

/**
 * @author ryans
 */
@WebServlet(
    name = "IncidentList",
    urlPatterns = {"/shiftlog/incident-list.html"})
public class IncidentList extends HttpServlet {

  @EJB EventTypeFacade eventTypeFacade;
  @EJB IncidentReportService incidentReportService;
  @EJB SystemFacade systemFacade;

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

    Date start = null;
    Date end = null;

    try {
      start = DtmParamConverter.convertJLabDateTime(request, "start");
      end = DtmParamConverter.convertJLabDateTime(request, "end");
    } catch (ParseException e) {
      throw new ServletException("Unable to parse date", e);
    }

    BigInteger eventTypeId = ParamConverter.convertBigInteger(request, "type");

    if (eventTypeId == null
        && request.getParameter("type") == null) { // null is different than empty string
      eventTypeId = BigInteger.ONE;
    }

    EventType type = null;

    if (eventTypeId != null) {
      type = eventTypeFacade.find(eventTypeId);
    }

    BigInteger systemId = ParamConverter.convertBigInteger(request, "system");

    SystemEntity selectedSystem = null;

    if (systemId != null) {
      selectedSystem = systemFacade.find(systemId);
    }

    String component = request.getParameter("component");

    Boolean beamTransport = null;
    try {
      beamTransport = ParamConverter.convertYNBoolean(request, "transport");
    } catch (UserFriendlyException e) {
      throw new ServletException("transport must be Y or N", e);
    }

    if (start == null || end == null) {
      throw new ServletException("start and end dates must not be empty");
    }

    IncidentDowntimeReportParams params = new IncidentDowntimeReportParams();
    params.setStart(start);
    params.setEnd(end);
    params.setEventTypeId(eventTypeId);
    params.setSystemId(systemId);
    params.setComponent(component);
    params.setBeamTransport(beamTransport);

    long totalRecords = incidentReportService.countFilterList(params);

    params.setSortByDuration(true);
    params.setMax((int) totalRecords);
    List<IncidentSummary> incidentList = incidentReportService.filterList(params);

    DecimalFormat formatter = new DecimalFormat("###,###");

    String selectionMessage = "Found " + formatter.format(totalRecords) + " incidents ";

    String filters =
        FilterSelectionMessage.getReportMessage(
            start, end, type, selectedSystem, null, null, component, beamTransport, false);

    if (filters.length() > 0) {
      selectionMessage = selectionMessage + " in " + filters;
    }

    request.setAttribute("type", type);
    request.setAttribute("start", start);
    request.setAttribute("end", end);
    request.setAttribute("selectionMessage", selectionMessage);
    request.setAttribute("incidentList", incidentList);

    request
        .getRequestDispatcher("/WEB-INF/views/shiftlog/incident-list.jsp")
        .forward(request, response);
  }
}
