package org.jlab.dtm.presentation.controller.operability;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jlab.dtm.business.params.TrendReportParams;
import org.jlab.dtm.business.session.CategoryFacade;
import org.jlab.dtm.business.session.TrendReportFacade;
import org.jlab.dtm.business.util.DtmTimeUtil;
import org.jlab.dtm.persistence.entity.Category;
import org.jlab.dtm.persistence.model.TrendRecord;
import org.jlab.dtm.presentation.params.TrendReportUrlParamHandler;
import org.jlab.smoothness.business.exception.UserFriendlyException;

/**
 * @author ryans
 */
@WebServlet(
    name = "TrendReport",
    urlPatterns = {"/operability/trend"})
public class TrendReport extends HttpServlet {
  @EJB CategoryFacade categoryFacade;
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

    TrendReportUrlParamHandler paramHandler =
        new TrendReportUrlParamHandler(request, today, sevenDaysAgo);

    TrendReportParams params;

    if (paramHandler.qualified()) {
      params = paramHandler.convert();
      paramHandler.validate(params);
      paramHandler.store(params);
    } else {
      params = paramHandler.materialize();
      paramHandler.redirect(response, params);
      return;
    }

    List<TrendRecord> recordList = null;

    params.setIncludeCategories(true);

    String selectionMessage = paramHandler.message(params);
    String errorMessage = null;

    try {
      recordList = trendReportFacade.find(params);
    } catch (SQLException e) {
      throw new ServletException("Unable to load data", e);
    } catch (UserFriendlyException e) {
      errorMessage = e.getMessage();
    }

    List<Category> alphaCatList = categoryFacade.findAlphaCategoryList();

    Date endInclusive = DtmTimeUtil.getEndInclusive(params.getEnd(), params.getSize());

    request.setAttribute("start", params.getStart());
    request.setAttribute("end", params.getEnd());
    request.setAttribute("endInclusive", endInclusive);
    request.setAttribute("alphaCatList", alphaCatList);
    request.setAttribute("recordList", recordList);
    request.setAttribute("selectionMessage", selectionMessage);
    request.setAttribute("errorMessage", errorMessage);

    request.getRequestDispatcher("/WEB-INF/views/operability/trend.jsp").forward(request, response);
  }
}
