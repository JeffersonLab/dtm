package org.jlab.dtm.presentation.controller.beamteam;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jlab.dtm.business.params.IncidentDowntimeReportParams;
import org.jlab.dtm.business.session.AbstractFacade;
import org.jlab.dtm.business.session.EventTypeFacade;
import org.jlab.dtm.business.session.IncidentReportService;
import org.jlab.dtm.business.session.IncidentReportService.IncidentSummary;
import org.jlab.dtm.business.session.ResponsibleGroupFacade;
import org.jlab.dtm.business.session.SystemFacade;
import org.jlab.dtm.persistence.entity.EventType;
import org.jlab.dtm.persistence.entity.SystemEntity;
import org.jlab.dtm.presentation.params.IncidentDowntimeReportUrlParamHandler;
import org.jlab.dtm.presentation.util.FilterSelectionMessage;
import org.jlab.smoothness.presentation.util.Paginator;
import org.jlab.smoothness.presentation.util.ParamUtil;

/**
 * @author ryans
 */
@WebServlet(
    name = "TuneIncidents",
    urlPatterns = {"/beam-team/tune-incidents"})
public class TuneIncidents extends HttpServlet {

  @EJB EventTypeFacade eventTypeFacade;
  @EJB IncidentReportService incidentReportService;
  @EJB SystemFacade systemFacade;
  @EJB ResponsibleGroupFacade groupFacade;

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

    Calendar c = Calendar.getInstance();
    Date now = c.getTime();
    c.set(Calendar.MILLISECOND, 0);
    c.set(Calendar.SECOND, 0);
    c.set(Calendar.MINUTE, 0);
    c.set(Calendar.HOUR_OF_DAY, 7);
    Date today = c.getTime();
    c.add(Calendar.DATE, -7);
    Date sevenDaysAgo = c.getTime();

    IncidentDowntimeReportUrlParamHandler paramHandler =
        new IncidentDowntimeReportUrlParamHandler(
            request, today, sevenDaysAgo, eventTypeFacade, systemFacade, groupFacade);

    IncidentDowntimeReportParams params;

    if (paramHandler.qualified()) {
      params = paramHandler.convert();
      paramHandler.validate(params);
      paramHandler.store(params);
    } else {
      params = paramHandler.materialize();
      paramHandler.redirect(response, params);
      return;
    }

    EventType type = null;

    if (params.getEventTypeId() != null) {
      type = eventTypeFacade.find(params.getEventTypeId());
    }

    List<SystemEntity> systemList =
        systemFacade.findAll(
            new AbstractFacade.OrderDirective("weight"), new AbstractFacade.OrderDirective("name"));
    List<EventType> eventTypeList =
        eventTypeFacade.findAll(new AbstractFacade.OrderDirective("weight"));

    int offset = ParamUtil.convertAndValidateNonNegativeInt(request, "offset", 0);
    int maxPerPage = ParamUtil.convertAndValidateNonNegativeInt(request, "max", 10);

    Double periodDurationHours = null;
    List<IncidentSummary> incidentList;
    long totalRecords;
    double totalRepairTime;

    if (params.getStart() != null && params.getEnd() != null) {
      periodDurationHours =
          (params.getEnd().getTime() - params.getStart().getTime()) / 1000.0 / 60.0 / 60.0;
    }

    params.setBeamTransport(Boolean.TRUE);

    incidentList = incidentReportService.filterList(params);
    totalRecords = incidentReportService.countFilterList(params);
    totalRepairTime = incidentReportService.sumTotalBoundedDuration(params);

    Paginator paginator = new Paginator((int) totalRecords, offset, maxPerPage);

    DecimalFormat formatter = new DecimalFormat("###,###");

    // We use null for BeamTransport parameter so it doesn't show up in message
    String selectionMessage =
        FilterSelectionMessage.getDateRangeReportMessage(
            params.getStart(),
            params.getEnd(),
            type,
            null,
            null,
            null,
            params.getComponent(),
            null,
            null,
            "incident",
            false);

    selectionMessage =
        selectionMessage
            + "{"
            + paginator.getStartNumber()
            + " - "
            + paginator.getEndNumber()
            + " of "
            + formatter.format(totalRecords)
            + "}";

    request.setAttribute("type", type);
    request.setAttribute("start", params.getStart());
    request.setAttribute("end", params.getEnd());
    request.setAttribute("eventTypeList", eventTypeList);
    request.setAttribute("systemList", systemList);
    request.setAttribute("selectionMessage", selectionMessage);
    request.setAttribute("today", today);
    request.setAttribute("sevenDaysAgo", sevenDaysAgo);
    request.setAttribute("incidentList", incidentList);
    request.setAttribute("totalRepairTime", totalRepairTime);
    request.setAttribute("paginator", paginator);
    request.setAttribute("periodDurationHours", periodDurationHours);

    request
        .getRequestDispatcher("/WEB-INF/views/beam-team/tune-incidents.jsp")
        .forward(request, response);
  }
}
