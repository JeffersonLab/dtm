package org.jlab.dtm.presentation.controller.csv;

import java.io.IOException;
import java.sql.SQLException;
import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jlab.dtm.business.params.TripParams;
import org.jlab.dtm.business.session.ExcelTripsService;
import org.jlab.dtm.persistence.filter.FsdTripFilter;
import org.jlab.dtm.presentation.params.TripUrlParamHandler;

/**
 * @author ryans
 */
@WebServlet(
    name = "CsvTrips",
    urlPatterns = {"/csv/trips.csv"})
public class CsvTrips extends HttpServlet {

  @EJB ExcelTripsService excelService;

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

    TripUrlParamHandler paramHandler = new TripUrlParamHandler(request);

    TripParams params = paramHandler.convert();

    FsdTripFilter filter = new FsdTripFilter(params);

    response.setContentType("text/csv");
    response.setHeader("content-disposition", "attachment;filename=\"trips.csv\"");

    String selectionMessage = paramHandler.message(params).trim();

    try {
      excelService.exportAsCsv(
          response.getOutputStream(),
          filter,
          selectionMessage,
          "agg".equals(request.getParameter("format")));
    } catch (SQLException e) {
      throw new ServletException("Unable to query database for trips", e);
    }
  }
}
