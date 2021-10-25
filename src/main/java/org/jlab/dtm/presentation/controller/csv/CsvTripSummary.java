package org.jlab.dtm.presentation.controller.csv;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jlab.dtm.business.params.FsdSummaryReportParams;
import org.jlab.dtm.business.session.ExcelTripsService;
import org.jlab.dtm.persistence.enumeration.BinSize;
import org.jlab.dtm.presentation.params.FsdSummaryReportUrlParamHandler;
import org.jlab.smoothness.business.util.TimeUtil;

/**
 *
 * @author ryans
 */
@WebServlet(name = "CsvTripSummary", urlPatterns = {"/csv/trip-summary.csv"})
public class CsvTripSummary extends HttpServlet {

    @EJB
    ExcelTripsService excelService;

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
        c.set(Calendar.MILLISECOND, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.HOUR_OF_DAY, 7);
        Date today = c.getTime();
        c.add(Calendar.DATE, -7);
        Date sevenDaysAgo = c.getTime();

        FsdSummaryReportUrlParamHandler paramHandler
                = new FsdSummaryReportUrlParamHandler(request, today, sevenDaysAgo);

        FsdSummaryReportParams params = paramHandler.convert();

        Date endInclusive = null;

        // ignore hours and minutes if daily graph otherwise ticks won't line up
        if (BinSize.DAY.equals(params.getBinSize())) {
            params.setStart(TimeUtil.startOfDay(params.getStart(), Calendar.getInstance()));
            params.setEnd(TimeUtil.startOfDay(params.getEnd(), Calendar.getInstance()));

            endInclusive = TimeUtil.addDays(params.getEnd(), -1);
        } else if (BinSize.HOUR.equals(params.getBinSize())) { // ignore minutes if hourly graph
            params.setStart(TimeUtil.startOfHour(params.getStart(), Calendar.getInstance()));
            params.setEnd(TimeUtil.startOfHour(params.getEnd(), Calendar.getInstance()));

            endInclusive = TimeUtil.addHours(params.getEnd(), -1);
        } else { // Monthly
            params.setStart(TimeUtil.startOfMonth(params.getStart(), Calendar.getInstance()));
            params.setEnd(TimeUtil.startOfMonth(params.getEnd(), Calendar.getInstance()));

            endInclusive = TimeUtil.addMonths(params.getEnd(), -1);
        }

        response.setContentType("text/csv");
        response.setHeader("content-disposition", "attachment;filename=\"trip-summary.csv\"");

        try {
            excelService.exportSummaryAsCsv(response.getOutputStream(), params);
        } catch (SQLException e) {
            throw new ServletException("Unable to query database for trips", e);
        }
    }
}
