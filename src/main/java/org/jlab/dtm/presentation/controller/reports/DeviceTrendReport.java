package org.jlab.dtm.presentation.controller.reports;

import jakarta.ejb.EJB;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.math.BigInteger;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import org.jlab.dtm.business.service.FsdExceptionTrendService;
import org.jlab.dtm.business.session.CategoryFacade;
import org.jlab.dtm.business.session.EventTypeFacade;
import org.jlab.dtm.persistence.entity.Category;
import org.jlab.dtm.persistence.enumeration.Shift;
import org.jlab.dtm.persistence.model.CategoryTrendInfo;
import org.jlab.dtm.presentation.util.DtmParamConverter;
import org.jlab.dtm.presentation.util.FilterSelectionMessage;
import org.jlab.smoothness.business.util.TimeUtil;
import org.jlab.smoothness.presentation.util.ParamBuilder;
import org.jlab.smoothness.presentation.util.ParamConverter;
import org.jlab.smoothness.presentation.util.ParamUtil;
import org.jlab.smoothness.presentation.util.ServletUtil;

/**
 * @author ryans
 */
@WebServlet(
    name = "DeviceTrendReport",
    urlPatterns = {"/reports/device-trend"})
public class DeviceTrendReport extends HttpServlet {

  @EJB EventTypeFacade eventTypeFacade;
  @EJB CategoryFacade categoryFacade;

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

    Date start = null; // Inclusive
    Date end = null; // Exclusive
    Date endInclusive = null;

    try {
      start = DtmParamConverter.convertJLabDateTime(request, "start");
      end = DtmParamConverter.convertJLabDateTime(request, "end");
    } catch (ParseException e) {
      throw new ServletException("Unable to parse date", e);
    }

    String chart = request.getParameter("chart");

    String data = request.getParameter("data");

    String grouping = request.getParameter("grouping");

    int interval = ParamUtil.convertAndValidateNonNegativeInt(request, "interval", 24);

    BigInteger[] categoryIdArray = ParamConverter.convertBigIntegerArray(request, "category");

    Calendar c = Calendar.getInstance();
    Date now = c.getTime();
    c.set(Calendar.MILLISECOND, 0);
    c.set(Calendar.SECOND, 0);
    c.set(Calendar.MINUTE, 0);
    c.set(Calendar.HOUR_OF_DAY, 7);
    Date today = c.getTime();
    c.add(Calendar.DATE, -7);
    Date sevenDaysAgo = c.getTime();
    c.add(Calendar.DATE, 4);
    Date threeDaysAgo = c.getTime();
    c.add(Calendar.DATE, 2);
    Date oneDayAgo = c.getTime();
    Date currentShiftStart = TimeUtil.getCcShiftStart(now);
    Date currentShiftEnd = TimeUtil.getCcShiftEnd(now);
    Date dateInPreviousShift = TimeUtil.addHours(currentShiftStart, -1);
    Date previousShiftStart = TimeUtil.getCcShiftStart(dateInPreviousShift);
    Date previousShiftEnd = TimeUtil.getCcShiftEnd(dateInPreviousShift);
    Shift currentShift = Shift.getCcShiftFromDate(currentShiftStart);
    Shift previousShift = Shift.getCcShiftFromDate(previousShiftStart);

    /* Note: We use a 'SECURE' cookie so session changes every request unless over SSL/TLS */
    HttpSession session = request.getSession(true);
    Date sessionStart = (Date) session.getAttribute("start");
    Date sessionEnd = (Date) session.getAttribute("end");
    String sessionChart = (String) session.getAttribute("chartTrend");
    String sessionData = (String) session.getAttribute("dataTrend");
    String sessionGrouping = (String) session.getAttribute("groupingTrend");
    Integer sessionInterval = (Integer) session.getAttribute("intervalTrend");
    BigInteger[] sessionCategoryIdArray =
        (BigInteger[]) session.getAttribute("categoryIdArrayTrend");

    /* Redirect if using defaults to maintain bookmarkability (html-to-image/pdf for example) */
    boolean needRedirect = false;

    if (start == null) {
      needRedirect = true;
      if (sessionStart != null) {
        start = sessionStart;
      } else {
        start = sevenDaysAgo;
      }
    }

    if (end == null) {
      needRedirect = true;
      if (sessionEnd != null) {
        end = sessionEnd;
      } else {
        end = today;
      }
    }

    if (request.getParameter("chart") == null) { // null is different than empty string
      needRedirect = true;
      if (sessionChart != null) {
        if (sessionChart.isEmpty()) {
          chart = "line"; // empty is not an option; use default
        } else {
          chart = sessionChart;
        }
      } else {
        chart = "line";
      }
    }

    if (request.getParameter("data") == null) { // null is different than empty string
      needRedirect = true;
      if (sessionData != null) {
        if (sessionData.isEmpty()) {
          data = "downtime"; // empty is not an option; use default
        } else {
          data = sessionData;
        }
      } else {
        data = "downtime";
      }
    }

    if (request.getParameter("grouping") == null) { // null is different than empty string
      needRedirect = true;
      if (sessionGrouping != null) {
        if (sessionGrouping.isEmpty()) {
          grouping = "";
        } else {
          grouping = sessionGrouping;
        }
      } else {
        grouping = "";
      }
    }

    if (request.getParameter("interval") == null) { // null is different than empty string
      needRedirect = true;
      if (sessionInterval != null) {
        interval = sessionInterval;
      } else {
        interval = 24;
      }
    }

    // multi-selects and checkboxes don't send empty fields so use hidden field to see if was form
    // submission / redirect
    if (request.getParameter("referrer") == null) {
      if (request.getParameter("category") == null) {
        needRedirect = true;
        categoryIdArray = sessionCategoryIdArray;
      }
    }

    if (needRedirect) {
      response.sendRedirect(
          response.encodeRedirectURL(
              this.getCurrentUrl(
                  request, start, end, chart, data, grouping, interval, categoryIdArray)));
      return;
    }

    session.setAttribute("start", start);
    session.setAttribute("end", end);
    session.setAttribute("chartTrend", chart == null ? "" : chart);
    session.setAttribute("dataTrend", data == null ? "" : data);
    session.setAttribute("groupingTrend", grouping == null ? "" : grouping);
    session.setAttribute("intervalTrend", interval);
    session.setAttribute("categoryIdArrayTrend", categoryIdArray);

    String range = "custom";

    if (end.getTime() == currentShiftEnd.getTime()
        && start.getTime() == currentShiftStart.getTime()) {
      range = "0shift";
    } else if (end.getTime() == previousShiftEnd.getTime()
        && start.getTime() == previousShiftStart.getTime()) {
      range = "1shift";
    } else if (end.getTime() == today.getTime()) {
      if (start.getTime() == sevenDaysAgo.getTime()) {
        range = "7days";
      } else if (start.getTime() == threeDaysAgo.getTime()) {
        range = "3days";
      } else if (start.getTime() == oneDayAgo.getTime()) {
        range = "1day";
      }
    }

    List<Category> categoryList = categoryFacade.findAlphaCategoryList();

    List<String> categoryNameList = categoryFacade.findNamesByIds(categoryIdArray);

    List<CategoryTrendInfo> trendList = null;

    if (start != null && end != null) {
      if (start.after(end)) {
        throw new ServletException("start date cannot be after end date");
      }

      FsdExceptionTrendService trendService = new FsdExceptionTrendService();

      // ignore hours and minutes if daily graph otherwise ticks won't line up
      if (interval == 24) {
        start = TimeUtil.startOfDay(start, Calendar.getInstance());
        end = TimeUtil.startOfDay(end, Calendar.getInstance());

        endInclusive = TimeUtil.addDays(end, -1);
      } else if (interval == 1) { // ignore minutes if hourly graph
        start = TimeUtil.startOfHour(start, Calendar.getInstance());
        end = TimeUtil.startOfHour(end, Calendar.getInstance());

        endInclusive = TimeUtil.addHours(end, -1);
      }

      try {
        trendList =
            trendService.findTrendListByPeriodTableFunc(
                start, end, "category".equals(grouping), interval, categoryIdArray);
      } catch (SQLException e) {
        throw new ServletException("Unable to query Faulted FSD Devices", e);
      }
    }

    String selectionMessage =
        FilterSelectionMessage.getFsdTrendReportMessage(
            start, end, data, grouping, interval, categoryNameList);

    request.setAttribute("chart", chart);
    request.setAttribute("data", data);
    request.setAttribute("range", range);
    request.setAttribute("start", start);
    request.setAttribute("end", end);
    request.setAttribute("endInclusive", endInclusive);
    request.setAttribute("currentShift", currentShift);
    request.setAttribute("previousShift", previousShift);
    request.setAttribute("today", today);
    request.setAttribute("sevenDaysAgo", sevenDaysAgo);
    request.setAttribute("categoryList", categoryList);
    request.setAttribute("selectionMessage", selectionMessage);
    request.setAttribute("trendList", trendList);

    request
        .getRequestDispatcher("/WEB-INF/views/reports/device-trend.jsp")
        .forward(request, response);
  }

  private String getCurrentUrl(
      HttpServletRequest request,
      Date start,
      Date end,
      String chart,
      String data,
      String grouping,
      int interval,
      BigInteger[] categoryIdArray) {
    SimpleDateFormat dateFormat = new SimpleDateFormat(TimeUtil.getFriendlyDateTimePattern());

    ParamBuilder builder = new ParamBuilder();

    builder.add("start", dateFormat.format(start));
    builder.add("end", dateFormat.format(end));
    builder.add("chart", chart);
    builder.add("data", data);
    builder.add("grouping", grouping);
    builder.add("interval", String.valueOf(interval));

    builder.add("referrer", "redirect");
    builder.add("category", categoryIdArray);

    return ServletUtil.getCurrentUrlAdvanced(request, builder.getParams());
  }
}
