package org.jlab.dtm.presentation.controller.operability;

import java.io.IOException;
import java.math.BigInteger;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.jlab.dtm.business.params.IncidentDowntimeReportParams;
import org.jlab.dtm.business.session.AbstractFacade.OrderDirective;
import org.jlab.dtm.business.session.CategoryFacade;
import org.jlab.dtm.business.session.IncidentReportService;
import org.jlab.dtm.business.session.IncidentReportService.IncidentSummary;
import org.jlab.dtm.business.session.IncidentReviewFacade;
import org.jlab.dtm.business.session.ResponsibleGroupFacade;
import org.jlab.dtm.persistence.entity.Category;
import org.jlab.dtm.persistence.entity.IncidentReview;
import org.jlab.dtm.persistence.entity.Workgroup;
import org.jlab.dtm.presentation.util.DtmParamConverter;
import org.jlab.smoothness.business.util.TimeUtil;
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

    // Category categoryRoot = categoryFacade.findBranch(BigInteger.valueOf(0L),
    // BigInteger.valueOf(2L));
    Category cebafRoot = categoryFacade.findBranch(BigInteger.valueOf(1L));
    Category lerfRoot = categoryFacade.findBranch(BigInteger.valueOf(2L));
    Category otherRoot = categoryFacade.findBranch(BigInteger.valueOf(3L));
    Category cryoRoot = categoryFacade.findBranch(BigInteger.valueOf(4L));
    Category facilitiesRoot = categoryFacade.findBranch(BigInteger.valueOf(5L));
    Category hallRoot = categoryFacade.findBranch(BigInteger.valueOf(465L));
    List<Workgroup> groupList = groupFacade.findAll(new OrderDirective("name"));

    IncidentDowntimeReportParams params = new IncidentDowntimeReportParams();
    params.setStart(start);
    params.setEnd(end);
    params.setEventTypeId(eventTypeId);
    params.setMax(max);
    params.setBeamTransport(false);

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

    String selectionMessage = TimeUtil.formatSmartRangeSeparateTime(start, end);

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
    // request.setAttribute("categoryRoot", categoryRoot);
    request.setAttribute("cebafRoot", cebafRoot);
    request.setAttribute("lerfRoot", lerfRoot);
    request.setAttribute("otherRoot", otherRoot);
    request.setAttribute("cryoRoot", cryoRoot);
    request.setAttribute("facilitiesRoot", facilitiesRoot);
    request.setAttribute("hallRoot", hallRoot);

    request
        .getRequestDispatcher("/WEB-INF/views/operability/weekly-repair.jsp")
        .forward(request, response);
  }

  private String getCurrentUrl(HttpServletRequest request, Date start, int max) {
    SimpleDateFormat dateFormat = new SimpleDateFormat(TimeUtil.getFriendlyDateTimePattern());

    Map<String, String> params = new LinkedHashMap<>();

    params.put("start", dateFormat.format(start));
    params.put("max", String.valueOf(max));

    return ServletUtil.getCurrentUrl(request, params);
  }
}
