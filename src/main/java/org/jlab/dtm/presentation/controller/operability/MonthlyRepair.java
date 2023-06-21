package org.jlab.dtm.presentation.controller.operability;

import java.io.IOException;
import java.math.BigInteger;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.jlab.dtm.business.params.IncidentDowntimeReportParams;
import org.jlab.dtm.business.service.MonthlyRepairReportService;
import org.jlab.dtm.business.session.AbstractFacade.OrderDirective;
import org.jlab.dtm.business.session.CategoryFacade;
import org.jlab.dtm.business.session.CategoryMonthlyGoalFacade;
import org.jlab.dtm.business.session.EventTypeFacade;
import org.jlab.dtm.business.session.IncidentReportService;
import org.jlab.dtm.business.session.IncidentReportService.IncidentSummary;
import org.jlab.dtm.business.session.MonthlyNoteFacade;
import org.jlab.dtm.business.session.TrendReportFacade;
import org.jlab.dtm.business.session.ResponsibleGroupFacade;
import org.jlab.dtm.persistence.entity.Category;
import org.jlab.dtm.persistence.entity.CategoryMonthlyGoal;
import org.jlab.dtm.persistence.entity.EventType;
import org.jlab.dtm.persistence.entity.MonthlyNote;
import org.jlab.dtm.persistence.entity.Workgroup;
import org.jlab.dtm.persistence.model.CategoryDowntime;
import org.jlab.dtm.persistence.model.MonthlyRepairReportRecord;
import org.jlab.dtm.persistence.model.TrendRecord;
import org.jlab.dtm.presentation.util.DtmParamConverter;
import org.jlab.smoothness.business.util.TimeUtil;
import org.jlab.smoothness.presentation.util.ParamUtil;
import org.jlab.smoothness.presentation.util.ServletUtil;

/**
 *
 * @author ryans
 */
@WebServlet(name = "MonthlyRepair", urlPatterns = {"/operability/monthly-repair"})
public class MonthlyRepair extends HttpServlet {

    @EJB
    EventTypeFacade eventTypeFacade;
    @EJB
    IncidentReportService incidentReportService;
    @EJB
    ResponsibleGroupFacade groupFacade;
    @EJB
    CategoryFacade categoryFacade;
    @EJB
    MonthlyNoteFacade noteFacade;
    @EJB
    CategoryMonthlyGoalFacade goalFacade;
    @EJB
    TrendReportFacade trendReportFacade;

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

        Date start = null;

        try {
            start = DtmParamConverter.convertMonthAndYear(request, "date");
        } catch (ParseException e) {
            throw new ServletException("Unable to parse date", e);
        }

        int max = ParamUtil.convertAndValidateNonNegativeInt(request, "max", 5);

        Calendar c = Calendar.getInstance();
        //Date now = c.getTime();
        c.set(Calendar.MILLISECOND, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.DATE, 1); // Just-in-case force first of month
        //Date today = c.getTime();
        c.add(Calendar.MONTH, -1);
        Date currentMonthStart = c.getTime();

        /* Note: We use a 'SECURE' cookie so session changes every request unless over SSL/TLS */
        HttpSession session = request.getSession(true);
        Date sessionStart = (Date) session.getAttribute("startMonthlyAvail");

        /* Redirect if using defaults to maintain bookmarkability (html-to-image/pdf for example) */
        boolean needRedirect = false;

        if (start == null) {
            needRedirect = true;
            if (sessionStart != null) {
                start = sessionStart;
            } else {
                start = currentMonthStart;
            }
        }

        start = TimeUtil.startOfMonth(start, Calendar.getInstance());

        if (needRedirect) {
            response.sendRedirect(
                    response.encodeRedirectURL(this.getCurrentUrl(request, start, max)));
            return;
        }

        c.setTime(start);
        c.add(Calendar.MONTH, -1);
        Date previousMonth = c.getTime();
        c.add(Calendar.MONTH, 2);
        Date nextMonth = c.getTime();

        session.setAttribute("startMonthlyAvail", start);

        Date fourWeeksAgoInclusive = TimeUtil.addDays(start, -21);

        Date end = TimeUtil.startOfNextMonth(start, Calendar.getInstance());

        Date lastMonthStart = previousMonth;
        Date lastMonthEnd = start;

        double periodDurationHours = 0.0;
        List<IncidentSummary> incidentList = null;
        long totalRecords = 0;
        double totalRepairTime = 0;
        double topDowntime = 0;
        BigInteger eventTypeId = BigInteger.ONE;

        TrendRecord currentData = null;
        TrendRecord lastMonthData = null;
        Map<Long, CategoryDowntime> lastMonthDowntimeMap = null;

        List<MonthlyRepairReportRecord> chartRecordList = null;

        EventType type = eventTypeFacade.find(eventTypeId);

        Category categoryRoot = categoryFacade.findBranch(BigInteger.valueOf(0L));
        List<Workgroup> groupList = groupFacade.findAll(new OrderDirective("name"));

        IncidentDowntimeReportParams params = new IncidentDowntimeReportParams();
        params.setStart(start);
        params.setEnd(end);
        params.setEventTypeId(eventTypeId);
        params.setBeamTransport(false);
        
        if (start != null && end != null) {
            periodDurationHours = (end.getTime() - start.getTime()) / 1000.0 / 60.0 / 60.0;

            params.setSortByDuration(true);
            incidentList = incidentReportService.filterList(params);
            totalRecords = incidentReportService.countFilterList(params);
            totalRepairTime = incidentReportService.sumTotalBoundedDuration(params);

            for (IncidentSummary incident : incidentList) {
                topDowntime = topDowntime + incident.getDowntimeHoursBounded();
                List<Workgroup> repairedByList = groupFacade.findRepairedBy(
                        incident.getIncidentId());
                incident.setRepairedByList(repairedByList);
            }

            try {
                currentData = trendReportFacade.load(start, end, type, true);
                lastMonthData = trendReportFacade.load(lastMonthStart, lastMonthEnd, type, true);
            } catch (SQLException e) {
                throw new ServletException("Unable to query month data", e);
            }
            lastMonthDowntimeMap = new HashMap<>();
            if (lastMonthData.categoryDowntimeList != null) {
                for (CategoryDowntime cd : lastMonthData.categoryDowntimeList) {
                    lastMonthDowntimeMap.put(cd.getId(), cd);
                }
            }

            // Get Chart data
            try {
                MonthlyRepairReportService reportService = new MonthlyRepairReportService();
                chartRecordList = reportService.find(start, end);

                if (currentData.missingList != null) {
                    for (CategoryDowntime cd : currentData.missingList) {
                        MonthlyRepairReportRecord mrrr = new MonthlyRepairReportRecord(cd.getName(),
                                BigInteger.valueOf(cd.getId()), start, 0);
                        chartRecordList.add(mrrr);
                    }
                }
            } catch (SQLException e) {
                throw new ServletException("Unable to query report database", e);
            }
        }

        SimpleDateFormat formatter = new SimpleDateFormat("MMMM, yyyy");

        String selectionMessage = formatter.format(start);
        //String selectionMessage = TimeUtil.formatSmartRangeSeparateTime(start, end);

        List<String> footnoteList = new ArrayList<>();

        Map<Long, CategoryMonthlyGoal> goalMap = goalFacade.findMap(start);

        MonthlyNote monthInfo = noteFacade.find(start);

        if (monthInfo == null) {
            monthInfo = new MonthlyNote();
        }

        findDefaultGoals(monthInfo, goalMap, currentData.categoryDowntimeList);

        request.setAttribute("monthInfo", monthInfo);
        request.setAttribute("goalMap", goalMap);
        request.setAttribute("footnoteList", footnoteList);
        request.setAttribute("chartRecordList", chartRecordList);
        request.setAttribute("data", currentData);
        request.setAttribute("lastMonthData", lastMonthData);
        request.setAttribute("lastMonthDowntimeMap", lastMonthDowntimeMap);
        request.setAttribute("previousMonth", previousMonth);
        request.setAttribute("nextMonth", nextMonth);
        request.setAttribute("start", start);
        request.setAttribute("end", end);
        request.setAttribute("selectionMessage", selectionMessage);
        request.setAttribute("fourWeeksAgoInclusive", fourWeeksAgoInclusive);
        request.setAttribute("max", max);
        request.setAttribute("incidentList", incidentList);
        request.setAttribute("groupList", groupList);
        request.setAttribute("totalRecords", totalRecords);
        request.setAttribute("topDowntime", topDowntime);
        request.setAttribute("totalRepairTime", totalRepairTime);
        request.setAttribute("periodDurationHours", periodDurationHours);
        request.setAttribute("categoryRoot", categoryRoot);

        request.getRequestDispatcher("/WEB-INF/views/operability/monthly-repair.jsp").forward(
                request,
                response);
    }

    private String getCurrentUrl(HttpServletRequest request, Date start, int max) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM yyyy");

        Map<String, String> params = new LinkedHashMap<>();

        params.put("date", dateFormat.format(start));
        params.put("max", String.valueOf(max));

        return ServletUtil.getCurrentUrl(request, params);
    }

    private void findDefaultGoals(MonthlyNote monthInfo, Map<Long, CategoryMonthlyGoal> goalMap,
            List<CategoryDowntime> downtimeList) {
        if (monthInfo.getMachineGoal() == null) {
            Float machineGoal = noteFacade.findMostRecentMachineGoal();
            monthInfo.setMachineGoal(machineGoal);
        }

        if (monthInfo.getEventGoal() == null) {
            Float eventGoal = noteFacade.findMostRecentEventGoal();
            monthInfo.setEventGoal(eventGoal);
        }

        if (monthInfo.getTripGoal() == null) {
            Float tripGoal = noteFacade.findMostRecentTripGoal();
            monthInfo.setTripGoal(tripGoal);
        }

        if (downtimeList != null) {
            for (CategoryDowntime d : downtimeList) {
                Long id = d.getId();
                CategoryMonthlyGoal goal = goalMap.get(id);
                if (goal == null) {
                    goal = goalFacade.findMostRecent(id);
                    if (goal != null) {
                        CategoryMonthlyGoal nGoal = new CategoryMonthlyGoal();
                        nGoal.setCategoryId(id);
                        nGoal.setGoal(goal.getGoal());
                        goalMap.put(id, nGoal);
                    }
                }
            }
        }
    }
}
