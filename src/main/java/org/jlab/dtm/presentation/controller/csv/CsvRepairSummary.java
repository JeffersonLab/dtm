package org.jlab.dtm.presentation.controller.csv;

import org.jlab.dtm.business.params.RepairSummaryReportParams;
import org.jlab.dtm.business.service.IncidentRepairTrendService;
import org.jlab.dtm.business.session.ExcelRepairsService;
import org.jlab.dtm.persistence.enumeration.BinSize;
import org.jlab.dtm.persistence.model.HistogramBin;
import org.jlab.dtm.presentation.params.RepairSummaryReportUrlParamHandler;
import org.jlab.smoothness.business.util.TimeUtil;

import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;

/**
 *
 * @author ryans
 */
@WebServlet(name = "CsvRepairSummary", urlPatterns = {"/csv/repair-summary.csv"})
public class CsvRepairSummary extends HttpServlet {

    @EJB
    ExcelRepairsService excelService;

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

        RepairSummaryReportUrlParamHandler paramHandler
                = new RepairSummaryReportUrlParamHandler(request, today, sevenDaysAgo);

        RepairSummaryReportParams params = paramHandler.convert();

        List<HistogramBin> trendList = null;

        if (params.getStart() != null && params.getEnd() != null) {

            IncidentRepairTrendService trendService = new IncidentRepairTrendService();

            // ignore hours and minutes if daily graph otherwise ticks won't line up
            if (BinSize.DAY.equals(params.getBinSize())) {
                params.setStart(TimeUtil.startOfDay(params.getStart(), Calendar.getInstance()));
                params.setEnd(TimeUtil.startOfDay(params.getEnd(), Calendar.getInstance()));

            } else if (BinSize.HOUR.equals(params.getBinSize())) { // ignore minutes if hourly graph
                params.setStart(TimeUtil.startOfHour(params.getStart(), Calendar.getInstance()));
                params.setEnd(TimeUtil.startOfHour(params.getEnd(), Calendar.getInstance()));

            } else { // Monthly
                params.setStart(TimeUtil.startOfMonth(params.getStart(), Calendar.getInstance()));
                params.setEnd(TimeUtil.startOfMonth(params.getEnd(), Calendar.getInstance()));

            }

            try {
                trendList = trendService.findTrendListByPeriodInMemory(params);
            } catch (SQLException e) {
                throw new ServletException("Unable to query Downtime Event Repairs", e);
            }
        }

        response.setContentType("text/csv");
        response.setHeader("content-disposition", "attachment;filename=\"repair-summary.csv\"");

        try {
            excelService.exportSummaryAsCsv(response.getOutputStream(), trendList);
        } catch (SQLException e) {
            throw new ServletException("Unable to query database for repairs", e);
        }
    }
}
