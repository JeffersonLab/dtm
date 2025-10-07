package org.jlab.dtm.presentation.controller.csv;

import jakarta.ejb.EJB;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
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
