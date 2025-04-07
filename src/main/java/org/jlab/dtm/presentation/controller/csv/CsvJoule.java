package org.jlab.dtm.presentation.controller.csv;

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
import org.jlab.dtm.business.params.JouleReportParams;
import org.jlab.dtm.business.session.JouleReportFacade;
import org.jlab.dtm.presentation.params.JouleReportUrlParamHandler;
import org.jlab.smoothness.business.util.TimeUtil;

/**
 * @author ryans
 */
@WebServlet(
    name = "CsvJoule",
    urlPatterns = {"/csv/joule.csv"})
public class CsvJoule extends HttpServlet {

  @EJB JouleReportFacade jouleFacade;

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

    JouleReportUrlParamHandler paramHandler =
        new JouleReportUrlParamHandler(request, today, sevenDaysAgo);

    JouleReportParams params;

    if (paramHandler.qualified()) {
      params = paramHandler.convert();
      paramHandler.validate(params);
      paramHandler.store(params);
    } else {
      params = paramHandler.materialize();
      paramHandler.redirect(response, params);
      return;
    }

    List<JouleReportFacade.JouleRecord> recordList = null;

    if (params.getStart() != null && params.getEnd() != null) {

      try {
        recordList = jouleFacade.find(params);
      } catch (InterruptedException e) {
        throw new ServletException("Unable to query for NPES Schedule");
      }
    }

    String selectionMessage =
        TimeUtil.formatSmartRangeSeparateTime(params.getStart(), params.getEnd());

    response.setContentType("text/csv");
    response.setHeader("content-disposition", "attachment;filename=\"joule.csv\"");

    jouleFacade.exportAsCsv(response.getOutputStream(), recordList, selectionMessage);
  }
}
