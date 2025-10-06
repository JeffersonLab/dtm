package org.jlab.dtm.presentation.controller.operability;

import jakarta.ejb.EJB;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.jlab.dtm.business.service.AnnualRepairReportService;
import org.jlab.dtm.business.session.CcAccHourService;
import org.jlab.dtm.persistence.model.AnnualRepairReportRecord;
import org.jlab.dtm.presentation.util.DtmParamConverter;
import org.jlab.smoothness.business.util.TimeUtil;
import org.jlab.smoothness.presentation.util.ServletUtil;

/**
 * @author ryans
 */
@WebServlet(
    name = "AnnualRepair",
    urlPatterns = {"/operability/annual-repair"})
public class AnnualRepair extends HttpServlet {

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

    Date start = null;

    try {
      start = DtmParamConverter.convertJLabDateTime(request, "start");
    } catch (ParseException e) {
      throw new ServletException("Unable to parse date", e);
    }

    Calendar c = Calendar.getInstance();
    // Date now = c.getTime();
    c.set(Calendar.MILLISECOND, 0);
    c.set(Calendar.SECOND, 0);
    c.set(Calendar.MINUTE, 0);
    c.set(Calendar.HOUR_OF_DAY, 0);
    // Date today = c.getTime();
    // c.setFirstDayOfWeek(Calendar.TUESDAY);
    // c.set(Calendar.DAY_OF_WEEK, Calendar.TUESDAY);
    // Date currentWeekStart = c.getTime();
    c.add(Calendar.YEAR, -1);
    Date oneYearAgo = c.getTime();

    /* Note: We use a 'SECURE' cookie so session changes every request unless over SSL/TLS */
    HttpSession session = request.getSession(true);
    Date sessionStart = (Date) session.getAttribute("startAnnualRepair");

    /* Redirect if using defaults to maintain bookmarkability (html-to-image/pdf for example) */
    boolean needRedirect = false;

    if (start == null) {
      needRedirect = true;
      if (sessionStart != null) {
        start = sessionStart;
      } else {
        start = oneYearAgo;
      }
    }

    if (needRedirect) {
      response.sendRedirect(response.encodeRedirectURL(this.getCurrentUrl(request, start)));
      return;
    }

    session.setAttribute("startAnnualRepair", start);

    Date end = TimeUtil.calculateYearEndDate(start);

    List<AnnualRepairReportRecord> recordList = null;
    List<CcAccHourService.MonthTotals> monthTotals = null;

    if (start != null && end != null) {
      try {
        AnnualRepairReportService reportService = new AnnualRepairReportService();
        recordList = reportService.find(start, end);
      } catch (SQLException e) {
        throw new ServletException("Unable to query report database", e);
      }

      monthTotals = accHourService.monthTotals(start, end);
    }

    String selectionMessage = TimeUtil.formatSmartRangeSeparateTime(start, end);

    List<String> footnoteList = new ArrayList<>();

    footnoteList.add("Incident Downtime");

    request.setAttribute("footnoteList", footnoteList);
    request.setAttribute("monthTotals", monthTotals);
    request.setAttribute("start", start);
    request.setAttribute("end", end);
    request.setAttribute("selectionMessage", selectionMessage);
    request.setAttribute("recordList", recordList);

    request
        .getRequestDispatcher("/WEB-INF/views/operability/annual-repair.jsp")
        .forward(request, response);
  }

  private String getCurrentUrl(HttpServletRequest request, Date start) {
    SimpleDateFormat dateFormat = new SimpleDateFormat(TimeUtil.getFriendlyDateTimePattern());

    Map<String, String> params = new LinkedHashMap<>();

    params.put("start", dateFormat.format(start));

    return ServletUtil.getCurrentUrl(request, params);
  }
}
