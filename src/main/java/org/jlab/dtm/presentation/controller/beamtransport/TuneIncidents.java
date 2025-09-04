package org.jlab.dtm.presentation.controller.beamtransport;

import java.io.IOException;
import java.math.BigInteger;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
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
import org.jlab.smoothness.business.util.TimeUtil;
import org.jlab.smoothness.presentation.util.Paginator;
import org.jlab.smoothness.presentation.util.ParamUtil;

/**
 * @author ryans
 */
@WebServlet(
    name = "TuneIncidents",
    urlPatterns = {"/beam-transport/tune-incidents"})
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

    List<SystemEntity> systemList =
        systemFacade.findAll(
            new AbstractFacade.OrderDirective("weight"), new AbstractFacade.OrderDirective("name"));
    List<EventType> eventTypeList = eventTypeFacade.filterList(null);

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

    BigInteger[] typeIdArray = params.getEventTypeIdArray();
    List<EventType> typeList = new ArrayList<>();
    if (typeIdArray != null) {
      for (BigInteger id : typeIdArray) {
        if (id != null) {
          EventType type = eventTypeFacade.find(id);
          typeList.add(type);
        }
      }
    }

    // We use null for BeamTransport parameter so it doesn't show up in message
    String selectionMessage =
        getSelectionMessage(params.getStart(), params.getEnd(), typeList, params.getComponent());

    selectionMessage =
        selectionMessage
            + " {"
            + paginator.getStartNumber()
            + " - "
            + paginator.getEndNumber()
            + " of "
            + formatter.format(totalRecords)
            + "}";

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
        .getRequestDispatcher("/WEB-INF/views/beam-transport/tune-incidents.jsp")
        .forward(request, response);
  }

  public String getSelectionMessage(
      Date start, Date end, List<EventType> typeList, String component) {
    String selectionMessage = "All Tune Incidents ";

    List<String> filters = new ArrayList<>();

    filters.add(TimeUtil.formatSmartRangeSeparateTime(start, end));

    if (typeList != null && !typeList.isEmpty()) {
      filters.add(
          "Type \""
              + typeList.stream().map(EventType::getAbbreviation).collect(Collectors.joining(","))
              + "\"");
    }

    if (component != null && !component.trim().isEmpty()) {
      filters.add("component matches \"" + component + "\"");
    }

    if (!filters.isEmpty()) {
      selectionMessage = filters.get(0);

      for (int i = 1; i < filters.size(); i++) {
        String filter = filters.get(i);
        selectionMessage += " and " + filter;
      }
    }

    return selectionMessage;
  }
}
