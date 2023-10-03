package org.jlab.dtm.presentation.controller.reports;

import org.jlab.dtm.business.params.RepairSummaryReportParams;
import org.jlab.dtm.business.service.IncidentRepairTrendService;
import org.jlab.dtm.business.session.AbstractFacade;
import org.jlab.dtm.business.session.CcAccHourService;
import org.jlab.dtm.business.session.ResponsibleGroupFacade;
import org.jlab.dtm.persistence.entity.Workgroup;
import org.jlab.dtm.persistence.enumeration.BinSize;
import org.jlab.dtm.persistence.model.BeamSummaryTotals;
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
import java.util.logging.Logger;

/**
 *
 * @author ryans
 */
@WebServlet(name = "RepairSummaryReport", urlPatterns = {"/reports/repair-summary"})
public class RepairSummaryReport extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(
            RepairSummaryReport.class.getName());

    @EJB
    CcAccHourService accHourService;

    @EJB
    ResponsibleGroupFacade groupFacade;

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
        Date now = c.getTime();
        c.set(Calendar.MILLISECOND, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.HOUR_OF_DAY, 7);
        Date today = c.getTime();
        c.add(Calendar.DATE, -7);
        Date sevenDaysAgo = c.getTime();
        
        RepairSummaryReportUrlParamHandler paramHandler
                = new RepairSummaryReportUrlParamHandler(request, today, sevenDaysAgo);

        RepairSummaryReportParams params;

        if (paramHandler.qualified()) {
            params = paramHandler.convert();
            paramHandler.validate(params);
            paramHandler.store(params);
        } else {
            params = paramHandler.materialize();
            paramHandler.redirect(response, params);
            return;
        }

        long getStartMillis = System.currentTimeMillis();

        Date endInclusive = null;

        double programHours = 0.0;
        double periodHours = 0.0;

        //List<String> categoryNameList = categoryFacade.findNamesByIds(categoryIdArray);
        List<HistogramBin> trendList = null;

        if (params.getStart() != null && params.getEnd() != null) {

            periodHours = (params.getEnd().getTime() - params.getStart().getTime()) / 1000 / 60 / 60;

            IncidentRepairTrendService trendService = new IncidentRepairTrendService();

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

            try {
                long serviceCallStart = System.currentTimeMillis();
                trendList = trendService.findTrendListByPeriodInMemory(params);
                long serviceCallEnd = System.currentTimeMillis();
                LOGGER.log(Level.FINEST, "Repair report service call seconds: {0}", (serviceCallEnd
                        - serviceCallStart) / 1000.0f);
            } catch (SQLException e) {
                throw new ServletException("Unable to query Downtime Event Repairs", e);
            }

            BeamSummaryTotals beamSummary = accHourService.reportTotals(params.getStart(),
                    params.getEnd());

            programHours = (beamSummary.calculateProgramSeconds() / 3600.0);

        }

        List<Workgroup> groupList = groupFacade.findAll(new AbstractFacade.OrderDirective("name"));

        String subtitle = TimeUtil.formatSmartRangeSeparateTime(params.getStart(), params.getEnd());

        request.setAttribute("binSizeArray", BinSize.values());
        request.setAttribute("groupList", groupList);
        request.setAttribute("chart", params.getChart());
        request.setAttribute("start", params.getStart());
        request.setAttribute("end", params.getEnd());
        request.setAttribute("endInclusive", endInclusive);
        request.setAttribute("today", today);
        request.setAttribute("sevenDaysAgo", sevenDaysAgo);
        request.setAttribute("subtitle", subtitle);
        request.setAttribute("trendList", trendList);
        request.setAttribute("programHours", programHours);
        request.setAttribute("periodHours", periodHours);

        request.getRequestDispatcher("/WEB-INF/views/reports/repair-summary.jsp").forward(request,
                response);

        long getEndMillis = System.currentTimeMillis();

        LOGGER.log(Level.FINEST, "Repair report Get method seconds: {0}", (getEndMillis
                - getStartMillis) / 1000.0f);
    }
}
