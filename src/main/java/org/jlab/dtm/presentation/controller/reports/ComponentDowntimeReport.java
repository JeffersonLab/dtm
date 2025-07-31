package org.jlab.dtm.presentation.controller.reports;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jlab.dtm.business.params.ComponentDowntimeReportParams;
import org.jlab.dtm.business.service.FsdTripService;
import org.jlab.dtm.business.session.AbstractFacade;
import org.jlab.dtm.business.session.AbstractFacade.OrderDirective;
import org.jlab.dtm.business.session.CcAccHourService;
import org.jlab.dtm.business.session.ComponentDowntimeFacade;
import org.jlab.dtm.business.session.EventTypeFacade;
import org.jlab.dtm.business.session.IncidentReportService;
import org.jlab.dtm.business.session.SystemFacade;
import org.jlab.dtm.persistence.entity.EventType;
import org.jlab.dtm.persistence.entity.SystemEntity;
import org.jlab.dtm.persistence.model.BeamSummaryTotals;
import org.jlab.dtm.persistence.model.ComponentDowntime;
import org.jlab.dtm.presentation.params.ComponentDowntimeReportUrlParamHandler;
import org.jlab.dtm.presentation.util.FilterSelectionMessage;

/**
 * @author ryans
 */
@WebServlet(
    name = "ComponentDowntimeReport",
    urlPatterns = {"/reports/component-downtime"})
public class ComponentDowntimeReport extends HttpServlet {

  @EJB EventTypeFacade eventTypeFacade;
  @EJB ComponentDowntimeFacade downtimeFacade;
  @EJB SystemFacade systemFacade;
  @EJB IncidentReportService incidentReportService;
  @EJB CcAccHourService accHourService;

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
    Date now = new Date();
    c.set(Calendar.MILLISECOND, 0);
    c.set(Calendar.SECOND, 0);
    c.set(Calendar.MINUTE, 0);
    c.set(Calendar.HOUR_OF_DAY, 7);
    Date today = c.getTime();
    c.add(Calendar.DATE, -7);
    Date sevenDaysAgo = c.getTime();

    ComponentDowntimeReportUrlParamHandler paramHandler =
        new ComponentDowntimeReportUrlParamHandler(request, today, sevenDaysAgo);

    ComponentDowntimeReportParams params;

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

    SystemEntity selectedSystem = null;

    if (params.getSystemId() != null) {
      selectedSystem = systemFacade.find(params.getSystemId());
    }

    List<EventType> eventTypeList = eventTypeFacade.filterList(null);
    List<SystemEntity> systemList =
        systemFacade.findAll(new OrderDirective("weight"), new OrderDirective("name"));

    List<ComponentDowntime> downtimeList = null;
    double grandTotalDuration = 0.0;
    double periodDurationHours = 0.0;
    int incidentCount = 0;
    FsdTripService.FsdSummary fsdSummary = null;
    double programHours = 0.0;
    double tripAwareProgramHours = 0.0;
    BeamSummaryTotals beamSummary = null;

    if (params.getStart() != null && params.getEnd() != null) {

      periodDurationHours =
          (params.getEnd().getTime() - params.getStart().getTime()) / 1000.0 / 60.0 / 60.0;

      downtimeList =
          downtimeFacade.findByPeriodAndType(
              params.getStart(),
              params.getEnd(),
              type,
              params.getBeamTransport(),
              params.getSystemId());

      for (int i = 0; i < downtimeList.size(); i++) {
        ComponentDowntime downtime = downtimeList.get(i);
        grandTotalDuration = grandTotalDuration + downtime.getDuration();
        incidentCount = incidentCount + (int) downtime.getIncidentCount();
      }

      if (params.getEventTypeId() != null && params.getEventTypeId().intValue() == 1) {
        beamSummary = accHourService.reportTotals(params.getStart(), params.getEnd());

        programHours = (beamSummary.calculateProgramSeconds() / 3600.0);
      }

      /*FsdTripService tripService = new FsdTripService();
      TripParams tripParams = new TripParams();
      tripParams.setStart(params.getStart());
      tripParams.setEnd(params.getEnd());
      tripParams.setMaxDuration(BigInteger.valueOf(5l));
      tripParams.setMaxDurationUnits("Minutes");
      FsdTripFilter fsdFilter = new FsdTripFilter(tripParams);
      try {
          fsdSummary = tripService.filterSummary(fsdFilter);
      } catch (SQLException e) {
          throw new ServletException("Unable to query FSD data", e);
      }

      tripAwareProgramHours = programHours - fsdSummary.getHours();

      if (tripAwareProgramHours < 0) {
          tripAwareProgramHours = 0;
      }*/
    }

    String selectionMessage =
        FilterSelectionMessage.getDateRangeReportMessage(
            params.getStart(),
            params.getEnd(),
            type,
            selectedSystem,
            null,
            null,
            null,
            params.getBeamTransport(),
            "Component",
            params.getData(),
            false);

    request.setAttribute("programHours", programHours);
    request.setAttribute("tripAwareProgramHours", tripAwareProgramHours);
    request.setAttribute("fsdSummary", fsdSummary);
    request.setAttribute("incidentCount", incidentCount);
    request.setAttribute("type", type);
    request.setAttribute("start", params.getStart());
    request.setAttribute("end", params.getEnd());
    request.setAttribute("eventTypeList", eventTypeList);
    request.setAttribute("systemList", systemList);
    request.setAttribute("selectionMessage", selectionMessage);
    request.setAttribute("today", today);
    request.setAttribute("sevenDaysAgo", sevenDaysAgo);
    request.setAttribute("downtimeList", downtimeList);
    request.setAttribute("grandTotalDuration", grandTotalDuration);
    request.setAttribute("periodDurationHours", periodDurationHours);

    request
        .getRequestDispatcher("/WEB-INF/views/reports/component-downtime.jsp")
        .forward(request, response);
  }
}
