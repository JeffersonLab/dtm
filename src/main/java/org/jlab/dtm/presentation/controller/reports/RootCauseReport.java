package org.jlab.dtm.presentation.controller.reports;

import java.io.IOException;
import java.math.BigInteger;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jlab.dtm.business.params.IncidentParams;
import org.jlab.dtm.business.service.RootCauseOverallMetricService;
import org.jlab.dtm.business.service.RootCauseOverallMetricService.RootCauseOverallMetrics;
import org.jlab.dtm.business.session.AbstractFacade.OrderDirective;
import org.jlab.dtm.business.session.CategoryFacade;
import org.jlab.dtm.business.session.EventTypeFacade;
import org.jlab.dtm.business.session.IncidentFacade;
import org.jlab.dtm.business.session.ResponsibleGroupFacade;
import org.jlab.dtm.business.session.SystemFacade;
import org.jlab.dtm.persistence.entity.Category;
import org.jlab.dtm.persistence.entity.EventType;
import org.jlab.dtm.persistence.entity.Incident;
import org.jlab.dtm.persistence.entity.Workgroup;
import org.jlab.dtm.persistence.enumeration.IncidentSortKey;
import org.jlab.dtm.persistence.enumeration.ReviewLevel;
import org.jlab.dtm.persistence.enumeration.Shift;
import org.jlab.dtm.presentation.params.IncidentUrlParamHandler;
import org.jlab.smoothness.business.exception.WebApplicationException;
import org.jlab.smoothness.business.util.TimeUtil;
import org.jlab.smoothness.presentation.util.Paginator;
import org.jlab.smoothness.presentation.util.ParamConverter;

/**
 *
 * @author ryans
 */
@WebServlet(name = "RootCauseReport", urlPatterns = {"/reports/root-cause"})
public class RootCauseReport extends HttpServlet {

    @EJB
    IncidentFacade incidentFacade;
    @EJB
    EventTypeFacade eventTypeFacade;
    @EJB
    SystemFacade systemFacade;
    @EJB
    CategoryFacade categoryFacade;
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

        // TODO: All of this range calculation logic should be client-side?
        Calendar c = Calendar.getInstance();
        Date now = c.getTime();
        c.set(Calendar.MILLISECOND, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.HOUR_OF_DAY, 7);
        Date today = c.getTime();
        c.add(Calendar.DATE, -7);
        Date sevenDaysAgo = c.getTime();
        c.add(Calendar.DATE, 4);
        Date threeDaysAgo = c.getTime();
        c.add(Calendar.DATE, 2);
        Date oneDayAgo = c.getTime();
        Date currentShiftStart = TimeUtil.getCcShiftStart(now);
        Date currentShiftEnd = TimeUtil.getCcShiftEnd(now);
        Date dateInPreviousShift = TimeUtil.addHours(currentShiftStart, -1);
        Date previousShiftStart = TimeUtil.getCcShiftStart(dateInPreviousShift);
        Date previousShiftEnd = TimeUtil.getCcShiftEnd(dateInPreviousShift);
        Shift currentShift = Shift.getCcShiftFromDate(currentShiftStart);
        Shift previousShift = Shift.getCcShiftFromDate(previousShiftStart);
        Date currentMonthStart = TimeUtil.startOfMonth(today, c);
        Date currentMonthEnd = TimeUtil.startOfNextMonth(today, c);
        Date previousMonthStart = TimeUtil.addMonths(currentMonthStart, -1);
        Date previousMonthEnd = currentMonthStart;

        IncidentUrlParamHandler paramHandler
                = new IncidentUrlParamHandler(request, eventTypeFacade, previousMonthStart, previousMonthEnd, IncidentSortKey.DURATION);

        IncidentParams params;

        if (paramHandler.qualified()) {
            params = paramHandler.convert();
            paramHandler.validate(params);
            paramHandler.store(params);
        } else {
            params = paramHandler.materialize();
            paramHandler.redirect(response, params);
            return;
        }

        Category categoryRoot = categoryFacade.findBranch(BigInteger.valueOf(0L), BigInteger.valueOf(2L));
        List<EventType> eventTypeList = eventTypeFacade.findAll(new OrderDirective("weight"));

        double periodDurationHours = 0.0;
        List<Incident> incidentList = null;
        Long totalRecords = 0l;
        double topDowntime = 0;

        if (params.getStart() != null && params.getEnd() != null) {
            periodDurationHours = (params.getEnd().getTime() - params.getStart().getTime()) / 1000.0 / 60.0 / 60.0;
        }

        switch (params.getMask()) {
            case NONE:
                params.setReviewed(null);
                params.setLevel(null);
                break;
            case DEADBEATS:
                params.setReviewed(false);
                params.setLevel(null);
                break;
            case LEVEL_ONE:
                params.setReviewed(null);
                params.setLevel(ReviewLevel.ONE);
                break;
            case LEVEL_TWO:
                params.setReviewed(null);
                params.setLevel(ReviewLevel.TWO);
                break;
            default: // LEVEL_THREE_PLUS
                params.setReviewed(null);
                params.setLevel(ReviewLevel.THREE_PLUS);
        }

        incidentList = incidentFacade.filterListWithLazyRelations(params);
        totalRecords = incidentFacade.countFilterList(params);

        RootCauseOverallMetricService metricService = new RootCauseOverallMetricService();

        try {
            RootCauseOverallMetrics overallMetric = metricService.find(params);
            request.setAttribute("overallMetric", overallMetric);
        } catch (SQLException e) {
            throw new ServletException("Unable to query metrics", e);
        }

        request.setAttribute("incidentList", incidentList);
        request.setAttribute("topDowntime", topDowntime);
        request.setAttribute("periodDurationHours", periodDurationHours);

        Paginator paginator = new Paginator(totalRecords.intValue(), params.getOffset(), params.getMax());

        DecimalFormat formatter = new DecimalFormat("###,###");

        String selectionMessage = paramHandler.message(params);

        String paginationMessage = " {" + paginator.getStartNumber() + " - " + paginator.getEndNumber() + " of " + formatter.format(totalRecords) + "}";

        List<Workgroup> groupList = groupFacade.findAll(new OrderDirective("name"));

        EventType eventType = null;

        if (params.getEventTypeId() != null) {
            eventType = eventTypeFacade.find(params.getEventTypeId());
        }

        request.setAttribute("incidentMask", params.getMask());
        request.setAttribute("start", params.getStart());
        request.setAttribute("end", params.getEnd());
        request.setAttribute("categoryRoot", categoryRoot);
        request.setAttribute("eventType", eventType);
        request.setAttribute("eventTypeList", eventTypeList);
        request.setAttribute("paginator", paginator);
        request.setAttribute("selectionMessage", selectionMessage);
        request.setAttribute("paginationMessage", paginationMessage);
        request.setAttribute("groupList", groupList);

        request.getRequestDispatcher("/WEB-INF/views/reports/root-cause.jsp").forward(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        Integer year = ParamConverter.convertInteger(request, "year");

        if(year == null) {
            throw new ServletException("year must not be empty");
        }

        try {
            List<IncidentFacade.TransitionRecord> recordList = incidentFacade.migrateOldRarRecords(year);
            // Separate transactions so when we go to add attachments by posting to the http endpoint the incidents exist!
            incidentFacade.migrateOldRarAttachments(recordList);
        } catch(SQLException | WebApplicationException | InterruptedException e) {
            throw new ServletException(e);
        }
    }
}
