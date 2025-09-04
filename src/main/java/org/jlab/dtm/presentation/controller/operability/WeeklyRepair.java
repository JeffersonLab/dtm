package org.jlab.dtm.presentation.controller.operability;

import java.io.IOException;
import java.math.BigInteger;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.jlab.dtm.business.params.IncidentDowntimeReportParams;
import org.jlab.dtm.business.session.*;
import org.jlab.dtm.business.session.AbstractFacade.OrderDirective;
import org.jlab.dtm.business.session.IncidentReportService.IncidentSummary;
import org.jlab.dtm.persistence.entity.Category;
import org.jlab.dtm.persistence.entity.EventType;
import org.jlab.dtm.persistence.entity.IncidentReview;
import org.jlab.dtm.persistence.entity.Workgroup;
import org.jlab.dtm.presentation.util.DtmParamConverter;
import org.jlab.smoothness.business.util.TimeUtil;
import org.jlab.smoothness.persistence.enumeration.Hall;
import org.jlab.smoothness.presentation.util.ParamConverter;
import org.jlab.smoothness.presentation.util.ParamUtil;
import org.jlab.smoothness.presentation.util.ServletUtil;

/**
 * @author ryans
 */
@WebServlet(
    name = "WeeklyRepair",
    urlPatterns = {"/operability/weekly-repair"})
public class WeeklyRepair extends HttpServlet {

  @EJB IncidentReportService incidentReportService;
  @EJB ResponsibleGroupFacade groupFacade;
  @EJB CategoryFacade categoryFacade;
  @EJB IncidentReviewFacade reviewFacade;
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

    Date start = null;

    try {
      start = DtmParamConverter.convertJLabDateTime(request, "start");
    } catch (ParseException e) {
      throw new ServletException("Unable to parse date", e);
    }

    Boolean beamTransport = null;

    try {
      beamTransport = ParamConverter.convertYNBoolean(request, "transport");
    } catch (Exception e) {
      throw new RuntimeException("Unable to parse beam transport boolean", e);
    }

    List<Hall> hallList = DtmParamConverter.convertHallList(request, "hall");

    BigInteger[] typeIdArray = ParamConverter.convertBigIntegerArray(request, "type");

    List<EventType> typeList = new ArrayList<>();

    if (typeIdArray != null) {
      for (BigInteger id : typeIdArray) {
        if (id != null) {
          EventType type = eventTypeFacade.find(id);
          typeList.add(type);
        }
      }
    }

    int offset = ParamUtil.convertAndValidateNonNegativeInt(request, "offset", 0);
    int max = ParamUtil.convertAndValidateNonNegativeInt(request, "max", 5);

    Calendar c = Calendar.getInstance();
    // Date now = c.getTime();
    c.set(Calendar.MILLISECOND, 0);
    c.set(Calendar.SECOND, 0);
    c.set(Calendar.MINUTE, 0);
    c.set(Calendar.HOUR_OF_DAY, 0);
    // Date today = c.getTime();
    c.setFirstDayOfWeek(Calendar.WEDNESDAY);
    c.set(Calendar.DAY_OF_WEEK, Calendar.WEDNESDAY);
    c.add(Calendar.WEEK_OF_YEAR, -1);
    Date currentWeekStart = c.getTime();

    /* Note: We use a 'SECURE' cookie so session changes every request unless over SSL/TLS */
    HttpSession session = request.getSession(true);
    Date sessionStart = (Date) session.getAttribute("startRepair");

    /* Redirect if using defaults to maintain bookmarkability (html-to-image/pdf for example) */
    boolean needRedirect = false;

    if (start == null) {
      needRedirect = true;
      if (sessionStart != null) {
        start = sessionStart;
      } else {
        start = currentWeekStart;
      }
    }

    if (needRedirect) {
      response.sendRedirect(response.encodeRedirectURL(this.getCurrentUrl(request, start, max)));
      return;
    }

    session.setAttribute("startRepair", start);

    Date fourWeeksAgoInclusive = TimeUtil.addDays(start, -21);

    Date end = TimeUtil.calculateWeekEndDate(start);

    double periodDurationHours = 0.0;
    List<IncidentSummary> incidentList = null;
    long totalRecords = 0;
    double totalRepairTime = 0;
    double topDowntime = 0;
    BigInteger eventTypeId = BigInteger.ONE;

    Category categoryRoot = categoryFacade.findBranch(BigInteger.valueOf(0L));
    List<Workgroup> groupList = groupFacade.findAll(new OrderDirective("name"));
    List<EventType> eventTypeList = eventTypeFacade.findActiveWithCategories();

    IncidentDowntimeReportParams params = new IncidentDowntimeReportParams();
    params.setStart(start);
    params.setEnd(end);
    params.setEventTypeIdArray(typeIdArray);
    params.setMax(max);
    params.setBeamTransport(beamTransport);

    if (start != null && end != null) {
      periodDurationHours = (end.getTime() - start.getTime()) / 1000.0 / 60.0 / 60.0;

      params.setSortByDuration(true);
      incidentList = incidentReportService.filterList(params);
      totalRecords = incidentReportService.countFilterList(params);
      totalRepairTime = incidentReportService.sumTotalBoundedDuration(params);

      for (IncidentSummary incident : incidentList) {
        topDowntime = topDowntime + incident.getDowntimeHoursBounded();
        List<Workgroup> repairedByList = groupFacade.findRepairedBy(incident.getIncidentId());
        incident.setRepairedByList(repairedByList);

        List<IncidentReview> reviewList = reviewFacade.findByIncident(incident.getIncidentId());
        incident.setIncidentReviewList(reviewList);
      }
    }

    String selectionMessage = getSelectionMessage(start, end, beamTransport, typeList, hallList);

    Set<Category> rootCacheSet = eventTypeFacade.getRootCacheSet(eventTypeList);

    request.setAttribute("rootCacheSet", rootCacheSet);
    request.setAttribute("start", start);
    request.setAttribute("end", end);
    request.setAttribute("selectionMessage", selectionMessage);
    request.setAttribute("fourWeeksAgoInclusive", fourWeeksAgoInclusive);
    request.setAttribute("max", max);
    request.setAttribute("incidentList", incidentList);
    request.setAttribute("groupList", groupList);
    request.setAttribute("totalRecords", totalRecords);
    request.setAttribute("topDowntime", topDowntime);
    request.setAttribute("totalRepairTime", totalRepairTime);
    request.setAttribute("periodDurationHours", periodDurationHours);
    request.setAttribute("categoryRoot", categoryRoot);
    request.setAttribute("eventTypeList", eventTypeList);
    request.setAttribute("hallArray", Hall.values());

    request
        .getRequestDispatcher("/WEB-INF/views/operability/weekly-repair.jsp")
        .forward(request, response);
  }

  public String getSelectionMessage(
      Date start, Date end, Boolean beamTransport, List<EventType> typeList, List<Hall> hallList) {
    String selectionMessage = "All Incidents ";

    List<String> filters = new ArrayList<>();

    filters.add(TimeUtil.formatSmartRangeSeparateTime(start, end));

    if (typeList != null && !typeList.isEmpty()) {
      filters.add(
          "Type \""
              + typeList.stream().map(EventType::getAbbreviation).collect(Collectors.joining(","))
              + "\"");
    }

    if (hallList != null && !hallList.isEmpty()) {
      filters.add(
          "Hall \"" + hallList.stream().map(Enum::name).collect(Collectors.joining(",")) + "\"");
    }

    if (beamTransport != null) {
      if (beamTransport) {
        filters.add("Beam Transport \"Only\"");
      } else {
        filters.add("Beam Transport \"Excluded\"");
      }
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

  private String getCurrentUrl(HttpServletRequest request, Date start, int max) {
    SimpleDateFormat dateFormat = new SimpleDateFormat(TimeUtil.getFriendlyDateTimePattern());

    Map<String, String> params = new LinkedHashMap<>();

    params.put("start", dateFormat.format(start));
    params.put("max", String.valueOf(max));

    return ServletUtil.getCurrentUrl(request, params);
  }
}
