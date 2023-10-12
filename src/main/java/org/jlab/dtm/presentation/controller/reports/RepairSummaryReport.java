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
import org.jlab.smoothness.business.util.DateRange;
import org.jlab.smoothness.business.util.TimeUtil;

import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigInteger;
import java.sql.SQLException;
import java.util.*;
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

            DateRange range = HistogramBin.adjust(params.getStart(), params.getEnd(), params.getBinSize());

            params.setStart(range.getStart());
            params.setEnd(range.getEnd());

            endInclusive = HistogramBin.getInclusiveEnd(params.getEnd(), params.getBinSize());

            periodHours = (params.getEnd().getTime() - params.getStart().getTime()) / 1000 / 60 / 60;

            IncidentRepairTrendService trendService = new IncidentRepairTrendService();

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

        String repairedBySubtitle = "";

        Map<BigInteger, Workgroup> groupMap = new HashMap<>();

        for(Workgroup group: groupList) {
            groupMap.put(group.getWorkgroupId(), group);
        }

        if(params.getRepairedByArray() != null && params.getRepairedByArray().length > 0) {
            repairedBySubtitle = " Repaired By ";
            for(int i = 0; i < params.getRepairedByArray().length; i++) {
                String idStr = params.getRepairedByArray()[i];
                BigInteger idNumber = new BigInteger(idStr);
                Workgroup group = groupMap.get(idNumber);
                String name = group == null ? "" : group.getName();
                repairedBySubtitle = repairedBySubtitle + "\"" + name + "\" ";
            }
        }

        subtitle = subtitle + repairedBySubtitle;

        List<String> footnoteList = getFootnotes();

        request.setAttribute("footnoteList", footnoteList);
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

    private List<String> getFootnotes() {
        List<String> notes = new ArrayList<>();

        notes.add("Repairs can be concurrent (can exceed bin size)");
        notes.add("A single incident Repaired By can be multi-valued");
        notes.add("A single incident may span multiple bins");
        notes.add("Includes all event types");

        return notes;
    }
}
