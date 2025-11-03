package org.jlab.dtm.presentation.controller.reports;

import jakarta.ejb.EJB;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import org.jlab.dtm.business.params.EventDowntimeReportParams;
import org.jlab.dtm.business.session.EventDowntimeFacade;
import org.jlab.dtm.business.session.EventTypeFacade;
import org.jlab.dtm.persistence.entity.EventType;
import org.jlab.dtm.persistence.model.EventDowntime;
import org.jlab.dtm.presentation.params.EventDowntimeReportUrlParamHandler;
import org.jlab.dtm.presentation.util.FilterSelectionMessage;

/**
 * @author ryans
 */
@WebServlet(
    name = "EventDowntimeReport",
    urlPatterns = {"/reports/event-downtime"})
public class EventDowntimeReport extends HttpServlet {

  @EJB EventTypeFacade eventTypeFacade;
  @EJB EventDowntimeFacade downtimeFacade;

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

    EventDowntimeReportUrlParamHandler paramHandler =
        new EventDowntimeReportUrlParamHandler(request, today, sevenDaysAgo);

    EventDowntimeReportParams params;

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

    List<EventType> eventTypeList = eventTypeFacade.filterList(null);

    List<EventDowntime> downtimeList = null;
    long eventCount = 0;
    double grandTotalDuration = 0.0;
    double meanTimeToRecover = 0.0;
    double restoreTotal = 0.0;
    double periodDurationHours = 0.0;
    int incidentCount = 0;

    if (params.getStart() != null && params.getStart() != null && type != null) {

      periodDurationHours =
          (params.getEnd().getTime() - params.getStart().getTime()) / 1000.0 / 60.0 / 60.0;

      downtimeList =
          downtimeFacade.findByPeriodAndTypeSortByDuration(
              params.getStart(), params.getEnd(), type, params.getBeamTransport());
      eventCount = downtimeList.size();

      for (int i = 0; i < downtimeList.size(); i++) {
        EventDowntime downtime = downtimeList.get(i);
        grandTotalDuration = grandTotalDuration + downtime.getDowntimeHoursBounded();
        restoreTotal = restoreTotal + downtime.getRestoreHoursBounded();
        incidentCount = incidentCount + (int) downtime.getIncidentCount();
      }

      if (eventCount > 0) {
        meanTimeToRecover = grandTotalDuration / downtimeList.size();
      }
    }

    String selectionMessage =
        FilterSelectionMessage.getDateRangeReportMessage(
            params.getStart(),
            params.getEnd(),
            type,
            null,
            null,
            null,
            null,
            params.getBeamTransport(),
            "Event",
            params.getData(),
            false);

    request.setAttribute("incidentCount", incidentCount);
    request.setAttribute("type", type);
    request.setAttribute("start", params.getStart());
    request.setAttribute("end", params.getEnd());
    request.setAttribute("eventTypeList", eventTypeList);
    request.setAttribute("selectionMessage", selectionMessage);
    request.setAttribute("today", today);
    request.setAttribute("sevenDaysAgo", sevenDaysAgo);
    request.setAttribute("downtimeList", downtimeList);
    request.setAttribute("grandTotalDuration", grandTotalDuration);
    request.setAttribute("meanTimeToRecover", meanTimeToRecover);
    request.setAttribute("restoreTotal", restoreTotal);
    request.setAttribute("periodDurationHours", periodDurationHours);

    request
        .getRequestDispatcher("/WEB-INF/views/reports/event-downtime.jsp")
        .forward(request, response);
  }
}
