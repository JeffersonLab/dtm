package org.jlab.dtm.presentation.controller.excel;

import jakarta.ejb.EJB;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigInteger;
import java.sql.SQLException;
import java.util.List;
import org.jlab.dtm.business.params.TripParams;
import org.jlab.dtm.business.service.FsdTripService;
import org.jlab.dtm.business.session.ExcelTripsService;
import org.jlab.dtm.persistence.filter.FsdTripFilter;
import org.jlab.dtm.persistence.model.FsdTrip;
import org.jlab.dtm.presentation.params.TripUrlParamHandler;
import org.jlab.smoothness.presentation.util.ParamUtil;

/**
 * @author ryans
 */
@WebServlet(
    name = "ExcelTrips",
    urlPatterns = {"/excel/trips.xlsx"})
public class ExcelTrips extends HttpServlet {

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

    FsdTripService tripService = new FsdTripService();

    BigInteger totalRecords;

    int offset = ParamUtil.convertAndValidateNonNegativeInt(request, "offset", 0);
    int max = ParamUtil.convertAndValidateNonNegativeInt(request, "max", Integer.MAX_VALUE);

    FsdTripFilter filter = new FsdTripFilter(params);

    String selectionMessage = paramHandler.message(params).trim();

    response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
    response.setHeader("content-disposition", "attachment;filename=\"trips.xlsx\"");

    try {
      totalRecords = tripService.countList(filter);

      if (totalRecords.longValue() > 65500L) {
        throw new ServletException(
            "Excel 2003 Supports no more than 65,500 rows; use the filter to restrict your results");
      }

      List<FsdTrip> tripList = tripService.filterListWithDependencies(filter, offset, max);
      excelService.exportMixed(response.getOutputStream(), tripList, selectionMessage);

    } catch (SQLException e) {
      throw new ServletException("Unable to query database for trips", e);
    }
  }
}
