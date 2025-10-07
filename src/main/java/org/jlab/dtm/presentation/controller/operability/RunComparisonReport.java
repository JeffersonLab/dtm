package org.jlab.dtm.presentation.controller.operability;

import jakarta.ejb.EJB;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import org.jlab.dtm.business.params.MultiTrendReportParams;
import org.jlab.dtm.business.session.*;
import org.jlab.dtm.persistence.entity.Category;
import org.jlab.dtm.persistence.model.TrendRecord;
import org.jlab.dtm.presentation.params.MultiTrendReportUrlParamHandler;
import org.jlab.smoothness.business.exception.UserFriendlyException;

/**
 * @author ryans
 */
@WebServlet(
    name = "RunComparisonReport",
    urlPatterns = {"/operability/run-compare"})
public class RunComparisonReport extends HttpServlet {

  @EJB EventTypeFacade eventTypeFacade;
  @EJB ResponsibleGroupFacade groupFacade;
  @EJB CategoryFacade categoryFacade;
  @EJB CategoryDowntimeFacade categoryDowntimeFacade;
  @EJB CcAccHourService accHourService;
  @EJB EventDowntimeFacade eventDowntimeFacade;
  @EJB MonthlyNoteFacade noteFacade;
  @EJB CategoryMonthlyGoalFacade goalFacade;
  @EJB TrendReportFacade trendReportFacade;

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

    MultiTrendReportUrlParamHandler paramHandler =
        new MultiTrendReportUrlParamHandler(request, today, sevenDaysAgo);

    MultiTrendReportParams params;

    if (paramHandler.qualified()) {
      params = paramHandler.convert();
      paramHandler.validate(params);
      paramHandler.store(params);
    } else {
      params = paramHandler.materialize();
      paramHandler.redirect(response, params);
      return;
    }

    List<List<TrendRecord>> recordListList = null;

    String errorMessage = null;

    try {
      recordListList = trendReportFacade.findMultiple(params);
    } catch (SQLException e) {
      throw new ServletException("Unable to load data", e);
    } catch (UserFriendlyException e) {
      errorMessage = e.getMessage();
    }

    List<Category> alphaCatList = null;
    // alphaCatList = categoryFacade.findAlphaCategoryList();

    String selectionMessage = paramHandler.message(params);

    request.setAttribute("alphaCatList", alphaCatList);
    request.setAttribute("recordListList", recordListList);
    request.setAttribute("selectionMessage", selectionMessage);
    request.setAttribute("errorMessage", errorMessage);

    request
        .getRequestDispatcher("/WEB-INF/views/operability/run-compare.jsp")
        .forward(request, response);
  }
}
