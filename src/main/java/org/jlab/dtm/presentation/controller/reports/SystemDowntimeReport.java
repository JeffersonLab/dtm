package org.jlab.dtm.presentation.controller.reports;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jlab.dtm.business.params.SystemDowntimeReportParams;
import org.jlab.dtm.business.service.FsdTripService;
import org.jlab.dtm.business.session.AbstractFacade;
import org.jlab.dtm.business.session.CategoryDowntimeFacade;
import org.jlab.dtm.business.session.CategoryFacade;
import org.jlab.dtm.business.session.CcAccHourService;
import org.jlab.dtm.business.session.EventTypeFacade;
import org.jlab.dtm.business.session.SystemDowntimeFacade;
import org.jlab.dtm.persistence.entity.Category;
import org.jlab.dtm.persistence.entity.EventType;
import org.jlab.dtm.persistence.model.BeamSummaryTotals;
import org.jlab.dtm.persistence.model.CategoryDowntime;
import org.jlab.dtm.persistence.model.SystemDowntime;
import org.jlab.dtm.presentation.params.SystemDowntimeReportUrlParamHandler;
import org.jlab.dtm.presentation.util.FilterSelectionMessage;

/**
 * @author ryans
 */
@WebServlet(
    name = "SystemDowntimeReport",
    urlPatterns = {"/reports/system-downtime"})
public class SystemDowntimeReport extends HttpServlet {

  @EJB EventTypeFacade eventTypeFacade;
  @EJB SystemDowntimeFacade downtimeFacade;
  @EJB CategoryFacade categoryFacade;
  @EJB CategoryDowntimeFacade categoryDowntimeFacade;
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

    SystemDowntimeReportUrlParamHandler paramHandler =
        new SystemDowntimeReportUrlParamHandler(request, today, sevenDaysAgo);

    SystemDowntimeReportParams params;

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

    Category selectedCategory = null;

    if (params.getCategoryId() != null) {
      selectedCategory = categoryFacade.find(params.getCategoryId());
    }

    List<EventType> eventTypeList =
        eventTypeFacade.findAll(new AbstractFacade.OrderDirective("weight"));
    List<Category> categoryList = categoryFacade.findAlphaCategoryList();

    List<SystemDowntime> downtimeList = null;
    List<SystemDowntime> nonOverlappingSystemDowntimeList = null;
    Map<Long, SystemDowntime> nonOverlappingSystemDowntimeMap = new HashMap<>();
    List<CategoryDowntime> nonOverlappingCategoryDowntimeList = null;
    double downtimeHours = 0.0;
    double nonOverlappingCategoryDowntimeHours = 0.0;
    double nonOverlappingSystemDowntimeHours = 0.0;
    double periodDurationHours = 0.0;
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
              params.getCategoryId(),
              false);

      double grandTotalDuration = 0.0;
      for (int i = 0; i < downtimeList.size(); i++) {
        SystemDowntime downtime = downtimeList.get(i);
        grandTotalDuration = grandTotalDuration + downtime.getDuration();
      }

      downtimeHours = grandTotalDuration * 24;

      nonOverlappingSystemDowntimeList =
          downtimeFacade.findByPeriodAndType(
              params.getStart(),
              params.getEnd(),
              type,
              params.getBeamTransport(),
              params.getCategoryId(),
              true);

      grandTotalDuration = 0.0;
      for (int i = 0; i < nonOverlappingSystemDowntimeList.size(); i++) {
        SystemDowntime downtime = nonOverlappingSystemDowntimeList.get(i);
        grandTotalDuration = grandTotalDuration + downtime.getDuration();
        nonOverlappingSystemDowntimeMap.put(downtime.getSystemId(), downtime);
      }

      nonOverlappingSystemDowntimeHours = grandTotalDuration * 24;

      nonOverlappingCategoryDowntimeList =
          categoryDowntimeFacade.findByPeriodAndType(
              params.getStart(),
              params.getEnd(),
              type,
              params.getBeamTransport(),
              true,
              params.getCategoryId());

      grandTotalDuration = 0.0;
      for (int i = 0; i < nonOverlappingCategoryDowntimeList.size(); i++) {
        CategoryDowntime downtime = nonOverlappingCategoryDowntimeList.get(i);
        grandTotalDuration = grandTotalDuration + downtime.getDuration();
      }

      nonOverlappingCategoryDowntimeHours = grandTotalDuration * 24;

      if (params.getEventTypeId() != null && params.getEventTypeId().intValue() == 1) {
        beamSummary = accHourService.reportTotals(params.getStart(), params.getEnd());

        programHours = (beamSummary.calculateProgramSeconds() / 3600.0);
      }
    }

    String selectionMessage =
        FilterSelectionMessage.getDateRangeReportMessage(
            params.getStart(),
            params.getEnd(),
            type,
            null,
            selectedCategory,
            null,
            null,
            params.getBeamTransport(),
            "System",
            params.getData(),
            params.getPacked());

    request.setAttribute("programHours", programHours);
    request.setAttribute("tripAwareProgramHours", tripAwareProgramHours);
    request.setAttribute("fsdSummary", fsdSummary);
    request.setAttribute("type", type);
    request.setAttribute("start", params.getStart());
    request.setAttribute("end", params.getEnd());
    request.setAttribute("eventTypeList", eventTypeList);
    request.setAttribute("categoryList", categoryList);
    request.setAttribute("selectionMessage", selectionMessage);
    request.setAttribute("today", today);
    request.setAttribute("sevenDaysAgo", sevenDaysAgo);
    request.setAttribute("downtimeList", downtimeList);
    request.setAttribute("nonOverlappingSystemDowntimeMap", nonOverlappingSystemDowntimeMap);
    request.setAttribute("downtimeHours", downtimeHours);
    request.setAttribute(
        "nonOverlappingCategoryDowntimeHours", nonOverlappingCategoryDowntimeHours);
    request.setAttribute("nonOverlappingSystemDowntimeHours", nonOverlappingSystemDowntimeHours);
    request.setAttribute("periodDurationHours", periodDurationHours);

    request
        .getRequestDispatcher("/WEB-INF/views/reports/system-downtime.jsp")
        .forward(request, response);
  }
}
