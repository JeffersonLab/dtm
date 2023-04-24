package org.jlab.dtm.presentation.controller.operability;

import org.jlab.dtm.business.params.IncidentParams;
import org.jlab.dtm.business.session.AbstractFacade.OrderDirective;
import org.jlab.dtm.business.session.*;
import org.jlab.dtm.persistence.entity.*;
import org.jlab.dtm.persistence.enumeration.IncidentSortKey;
import org.jlab.dtm.persistence.enumeration.ReviewLevel;
import org.jlab.dtm.presentation.util.DtmParamConverter;
import org.jlab.smoothness.business.exception.WebApplicationException;
import org.jlab.smoothness.business.util.TimeUtil;
import org.jlab.smoothness.presentation.util.Paginator;
import org.jlab.smoothness.presentation.util.ParamConverter;
import org.jlab.smoothness.presentation.util.ServletUtil;

import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.math.BigInteger;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 *
 * @author ryans
 */
@WebServlet(name = "WeeklyRootCauseReport", urlPatterns = {"/operability/weekly-root-cause"})
public class WeeklyRootCauseReport extends HttpServlet {

    @EJB
    IncidentFacade incidentFacade;
    @EJB
    EventTypeFacade eventTypeFacade;
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
        Calendar c = Calendar.getInstance();
        c.set(Calendar.MILLISECOND, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.HOUR_OF_DAY, 7);
        Date today = c.getTime();
        Date currentWeekStart = TimeUtil.startOfWeek(today, Calendar.WEDNESDAY);
        Date currentWeekEnd = TimeUtil.addDays(currentWeekStart, 7);

        Date start = null;

        try {
            start = DtmParamConverter.convertJLabDateTime(request, "start");
        } catch (ParseException e) {
            throw new ServletException("Unable to parse date", e);
        }


        /* Note: We use a 'SECURE' cookie so session changes every request unless over SSL/TLS */
        HttpSession session = request.getSession(true);
        Date sessionStart = (Date) session.getAttribute("startRepair");

        /* Redirect if using defaults to maintain bookmarkability (html-to-image/pdf for example) */
        boolean needRedirect = false;

        if (start == null) {
            needRedirect = true;
            if (sessionStart != null) {
                start = sessionStart;
            } else {
                start = currentWeekStart;
            }
        }

        if (needRedirect) {
            response.sendRedirect(
                    response.encodeRedirectURL(this.getCurrentUrl(request, start)));
            return;
        }

        session.setAttribute("startRepair", start);


        Date end = TimeUtil.calculateWeekEndDate(start);


        IncidentParams params = new IncidentParams();
        params.setStart(start);
        params.setEnd(end);



        Category categoryRoot = categoryFacade.findBranch(BigInteger.valueOf(0L));
        List<EventType> eventTypeList = eventTypeFacade.findAll(new OrderDirective("weight"));

        double periodDurationHours = 0.0;
        List<Incident> incidentList = null;
        Long totalRecords = 0L;
        double topDowntime = 0;

        if (params.getStart() != null && params.getEnd() != null) {
            periodDurationHours = (params.getEnd().getTime() - params.getStart().getTime()) / 1000.0 / 60.0 / 60.0;
        }

        Integer offset = ParamConverter.convertInteger(request, "offset");
        Integer max = ParamConverter.convertInteger(request, "max");

        if(offset == null) {
            offset = 0;
        }

        if(max == null) {
            max = 100;
        }

        params.setSort(IncidentSortKey.RAR_UPLOADED);
        params.setReviewed(null);
        params.setLevel(ReviewLevel.THREE_PLUS);
        params.setDateRangeForUploaded(true);
        params.setOffset(offset);
        params.setMax(max);

        incidentList = incidentFacade.filterListWithLazyRelations(params);
        totalRecords = incidentFacade.countFilterList(params);

        request.setAttribute("incidentList", incidentList);
        request.setAttribute("topDowntime", topDowntime);
        request.setAttribute("periodDurationHours", periodDurationHours);

        Paginator paginator = new Paginator(totalRecords.intValue(), params.getOffset(), params.getMax());

        DecimalFormat formatter = new DecimalFormat("###,###");

        String selectionMessage = TimeUtil.formatSmartRangeSeparateTime(start, end);

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

        request.getRequestDispatcher("/WEB-INF/views/operability/weekly-root-cause.jsp").forward(request, response);
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

    private String getCurrentUrl(HttpServletRequest request, Date start) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(TimeUtil.getFriendlyDateTimePattern());

        Map<String, String> params = new LinkedHashMap<>();

        params.put("start", dateFormat.format(start));

        return ServletUtil.getCurrentUrl(request, params);
    }
}
