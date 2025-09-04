package org.jlab.dtm.presentation.controller.beamtransport;

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
import org.jlab.dtm.business.params.TuneComparisonReportParams;
import org.jlab.dtm.business.session.AbstractFacade.OrderDirective;
import org.jlab.dtm.business.session.ComponentDowntimeFacade;
import org.jlab.dtm.business.session.EventTypeFacade;
import org.jlab.dtm.business.session.IncidentReportService;
import org.jlab.dtm.business.session.SystemFacade;
import org.jlab.dtm.persistence.entity.EventType;
import org.jlab.dtm.persistence.entity.SystemEntity;
import org.jlab.dtm.persistence.model.ComponentDowntime;
import org.jlab.dtm.presentation.params.TuneComparisonReportUrlParamHandler;
import org.jlab.dtm.presentation.util.FilterSelectionMessage;

/**
 * @author ryans
 */
@WebServlet(
    name = "TuneComparisonReport",
    urlPatterns = {"/beam-transport/tune-comparison"})
public class TuneComparisonReport extends HttpServlet {

  @EJB EventTypeFacade eventTypeFacade;
  @EJB ComponentDowntimeFacade downtimeFacade;
  @EJB SystemFacade systemFacade;
  @EJB IncidentReportService incidentReportService;

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

    TuneComparisonReportUrlParamHandler paramHandler =
        new TuneComparisonReportUrlParamHandler(request, today, sevenDaysAgo);

    TuneComparisonReportParams params;

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
    List<SystemEntity> systemList =
        systemFacade.findAll(new OrderDirective("weight"), new OrderDirective("name"));

    List<ComponentDowntime> downtimeList = null;
    double grandTotalDuration = 0.0;
    double periodDurationHours = 0.0;

    if (params.getStart() != null && params.getEnd() != null) {

      periodDurationHours =
          (params.getEnd().getTime() - params.getStart().getTime()) / 1000.0 / 60.0 / 60.0;

      downtimeList =
          downtimeFacade.findByPeriodAndType(params.getStart(), params.getEnd(), type, true, null);

      for (int i = 0; i < downtimeList.size(); i++) {
        ComponentDowntime downtime = downtimeList.get(i);
        grandTotalDuration = grandTotalDuration + downtime.getDuration();
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
            null,
            "Component",
            params.getData(),
            false);

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
        .getRequestDispatcher("/WEB-INF/views/beam-transport/tune-comparison.jsp")
        .forward(request, response);
  }
}
