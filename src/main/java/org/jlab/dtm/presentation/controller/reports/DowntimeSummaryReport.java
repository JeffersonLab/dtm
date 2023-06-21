package org.jlab.dtm.presentation.controller.reports;

import java.io.IOException;
import java.math.BigInteger;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.jlab.dtm.business.params.IncidentDowntimeReportParams;
import org.jlab.dtm.business.params.TripParams;
import org.jlab.dtm.business.service.FsdTripService;
import org.jlab.dtm.business.session.AbstractFacade;
import org.jlab.dtm.business.session.CategoryDowntimeFacade;
import org.jlab.dtm.business.session.EventDowntimeFacade;
import org.jlab.dtm.business.session.EventTypeFacade;
import org.jlab.dtm.business.session.IncidentReportService;
import org.jlab.dtm.business.session.CcAccHourService;
import org.jlab.dtm.business.session.SystemDowntimeFacade;
import org.jlab.dtm.persistence.entity.EventType;
import org.jlab.dtm.persistence.enumeration.AccMachineState;
import org.jlab.dtm.persistence.filter.FsdTripFilter;
import org.jlab.dtm.persistence.model.BeamSummaryTotals;
import org.jlab.dtm.persistence.model.CategoryDowntime;
import org.jlab.dtm.persistence.model.EventDowntime;
import org.jlab.dtm.persistence.model.SystemDowntime;
import org.jlab.dtm.presentation.util.DtmParamConverter;
import org.jlab.smoothness.business.util.TimeUtil;
import org.jlab.smoothness.presentation.util.ParamConverter;

/**
 *
 * @author ryans
 */
@WebServlet(name = "DowntimeSummaryReport", urlPatterns = {"/reports/downtime-summary"})
public class DowntimeSummaryReport extends HttpServlet {

    @EJB
    EventTypeFacade eventTypeFacade;
    @EJB
    EventDowntimeFacade downtimeFacade;
    @EJB
    IncidentReportService incidentReportService;
    @EJB
    CcAccHourService accHourService;
    @EJB
    CategoryDowntimeFacade categoryDowntimeFacade;
    @EJB
    SystemDowntimeFacade systemDowntimeFacade;

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
        Date end = null;

        try {
            start = DtmParamConverter.convertJLabDateTime(request, "start");
            end = DtmParamConverter.convertJLabDateTime(request, "end");
        } catch (ParseException e) {
            throw new ServletException("Unable to parse date", e);
        }

        BigInteger eventTypeId = ParamConverter.convertBigInteger(request, "type");

        Boolean beamTransport = null;

        Calendar c = Calendar.getInstance();
        Date now = new Date();
        c.set(Calendar.MILLISECOND, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.HOUR_OF_DAY, 7);
        Date today = c.getTime();
        c.add(Calendar.DATE, -7);
        Date sevenDaysAgo = c.getTime();

        /* Note: We use a 'SECURE' cookie so session changes every request unless over SSL/TLS */
        HttpSession session = request.getSession(true);
        Date sessionStart = (Date) session.getAttribute("start");
        Date sessionEnd = (Date) session.getAttribute("end");
        String sessionTypeId = (String) session.getAttribute("eventTypeIdStr");

        /* Redirect if using defaults to maintain bookmarkability (html-to-image/pdf for example) */
        boolean needRedirect = false;

        if (start == null) {
            needRedirect = true;
            if (sessionStart != null) {
                start = sessionStart;
            } else {
                start = sevenDaysAgo;
            }
        }

        if (end == null) {
            needRedirect = true;
            if (sessionEnd != null) {
                end = sessionEnd;
            } else {
                end = today;
            }
        }

        if (request.getParameter("type") == null) { // null is different than empty string
            needRedirect = true;
            if (sessionTypeId != null) {
                if (sessionTypeId.isEmpty()) {
                    eventTypeId = null;
                } else {
                    eventTypeId = new BigInteger(sessionTypeId);
                }
            } else {
                eventTypeId = BigInteger.ONE;
            }
        }

        if (needRedirect) {
            response.sendRedirect(
                    response.encodeRedirectURL(this.getCurrentUrl(request, start, end, eventTypeId)));
            return;
        }

        session.setAttribute("start", start);
        session.setAttribute("end", end);
        session.setAttribute("eventTypeIdStr", eventTypeId == null ? "" : eventTypeId.toString());

        EventType type = null;

        if (eventTypeId != null) {
            type = eventTypeFacade.find(eventTypeId);
        }

        List<EventType> eventTypeList = eventTypeFacade.findAll(new AbstractFacade.OrderDirective(
                "weight"));

        List<EventDowntime> downtimeList = null;
        long eventCount = 0;
        long tripCount = 0;
        long incidentCount = 0;
        double eventDowntime = 0.0;
        double tripDowntime = 0.0;
        double accMttr = 0.0;
        double eventMttr = 0.0;
        double tripMttr = 0.0;
        double periodUptimeHours = 0.0;
        double accDownHours = 0.0;
        Double accMtbf = null;
        Double accFailureRate = null;
        Double eventMtbf = null;
        Double eventFailureRate = null;
        Double tripMtbf = null;
        Double tripFailureRate = null;
        double accAvailability = 0.0;
        double eventAvailability = 0.0;
        double tripAvailability = 0.0;
        double restore = 0.0;
        double nonRestore = 0.0;
        double duration = 0.0;
        double incidentDowntime = 0.0;
        double incidentMttr = 0.0;
        String selectionMessage = null;
        BeamSummaryTotals beamSummary = null;
        double catGrandTotalDuration = 0.0;
        double sysGrandTotalDuration = 0.0;
        FsdTripService.FsdSummary fsdSummary = null;
        double programHours = 0.0;
        double tripUptimeHours = 0.0;
        double eventUptimeHours = 0.0;
        double eventAvailabilityLoss = 0.0;
        double tripAvailabilityLoss = 0.0;

        if (start != null && end != null && type != null) {
            if (start.after(end)) {
                throw new ServletException("start date cannot be after end date");
            }

            duration = (end.getTime() - start.getTime()) / 1000 / 60 / 60;

            downtimeList = downtimeFacade.findByPeriodAndTypeSortByDuration(start, end, type,
                    beamTransport);
            eventCount = downtimeList.size();

            for (int i = 0; i < downtimeList.size(); i++) {
                EventDowntime downtime = downtimeList.get(i);
                eventDowntime = eventDowntime + downtime.getDowntimeHoursBounded();
                restore = restore + downtime.getRestoreHoursBounded();
            }

            nonRestore = eventDowntime - restore;

            FsdTripService tripService = new FsdTripService();
            TripParams tripParams = new TripParams();
            tripParams.setStart(start);
            tripParams.setEnd(end);
            tripParams.setMaxDuration(BigInteger.valueOf(5L));
            tripParams.setMaxDurationUnits("Minutes");
            tripParams.setAccStateArray(new AccMachineState[]{AccMachineState.NULL, AccMachineState.DOWN, AccMachineState.ACC, AccMachineState.MD, AccMachineState.RESTORE});
            FsdTripFilter fsdFilter = new FsdTripFilter(tripParams);
            try {
                fsdSummary = tripService.filterSummary(fsdFilter);
                tripCount = fsdSummary.getCount();
                tripDowntime = fsdSummary.getHours();
            } catch (SQLException e) {
                throw new ServletException("Unable to query FSD data", e);
            }

            if (type.getEventTypeId().intValue() == 1) {
                beamSummary = accHourService.reportTotals(start, end);

                programHours = beamSummary.calculateProgramSeconds() / 3600.0;

                accDownHours = fsdSummary.getHours() + eventDowntime;

                if (accDownHours > duration) {
                    accDownHours = duration;
                }

                tripUptimeHours = programHours - fsdSummary.getHours();

                if (tripUptimeHours < 0) {
                    tripUptimeHours = 0;
                }

                periodUptimeHours = tripUptimeHours - eventDowntime;

                if (periodUptimeHours < 0) {
                    periodUptimeHours = 0;
                }

                eventUptimeHours = programHours - eventDowntime;

                if (eventUptimeHours < 0) {
                    eventUptimeHours = 0;
                }
            }

            if ((eventCount + tripCount) > 0) {
                accMttr = accDownHours / (tripCount + eventCount) * 60; // minutes
                accMtbf = periodUptimeHours / (tripCount + eventCount) * 60; // minutes

                if (periodUptimeHours > 0) {
                    accFailureRate = (tripCount + eventCount) / periodUptimeHours;
                }
            }

            if (eventCount > 0) {
                eventMttr = eventDowntime / eventCount;

                eventMtbf = eventUptimeHours / eventCount;

                if (eventUptimeHours > 0) {
                    eventFailureRate = eventCount / eventUptimeHours;
                }
            }

            if (tripCount > 0) {
                tripMttr = tripDowntime / tripCount * 60; // minutes

                tripMtbf = tripUptimeHours / tripCount * 60; // minutes

                if (tripUptimeHours > 0) {
                    tripFailureRate = tripCount / tripUptimeHours;
                }
            }

            if (programHours > 0) {
                accAvailability = (periodUptimeHours / programHours) * 100;
                eventAvailability = (eventUptimeHours / programHours) * 100;
                tripAvailability = (tripUptimeHours / programHours) * 100;

                eventAvailabilityLoss = eventDowntime / programHours * 100;
                tripAvailabilityLoss = tripDowntime / programHours * 100;
            }

            IncidentDowntimeReportParams params = new IncidentDowntimeReportParams();
            params.setStart(start);
            params.setEnd(end);
            params.setEventTypeId(eventTypeId);
            params.setBeamTransport(beamTransport);

            // Now do incident stuff
            incidentCount = incidentReportService.countFilterList(params);
            if (incidentCount > 0) {
                incidentDowntime = incidentReportService.sumTotalBoundedDuration(params);
                incidentMttr = incidentDowntime / incidentCount;
            }

            /*selectionMessage = FilterSelectionMessage.getSummaryReportCaption(start, end, type,
                    duration);*/
            selectionMessage = TimeUtil.formatSmartRangeSeparateTime(start, end);

            List<CategoryDowntime> catList = categoryDowntimeFacade.findByPeriodAndType(start, end,
                    type,
                    null, true, null);

            for (int i = 0; i < catList.size(); i++) {
                CategoryDowntime dt = catList.get(i);
                catGrandTotalDuration = catGrandTotalDuration + dt.getDuration();
            }

            List<SystemDowntime> sysList = systemDowntimeFacade.findByPeriodAndType(start, end,
                    type,
                    null, null, true);

            for (int i = 0; i < sysList.size(); i++) {
                SystemDowntime dt = sysList.get(i);
                sysGrandTotalDuration = sysGrandTotalDuration + dt.getDuration();
            }
        }

        String typeQualifier = "";

        if (type != null) {
            typeQualifier = type.getName() + " ";
        }

        request.setAttribute("programHours", programHours);
        request.setAttribute("fsdSummary", fsdSummary);
        request.setAttribute("categoryNonOverlappingDowntimeHours", catGrandTotalDuration * 24);
        request.setAttribute("systemNonOverlappingDowntimeHours", sysGrandTotalDuration * 24);
        request.setAttribute("periodDuration", duration);
        request.setAttribute("typeQualifier", typeQualifier);
        request.setAttribute("type", type);
        request.setAttribute("start", start);
        request.setAttribute("end", end);
        request.setAttribute("eventTypeList", eventTypeList);
        request.setAttribute("selectionMessage", selectionMessage);
        request.setAttribute("today", today);
        request.setAttribute("sevenDaysAgo", sevenDaysAgo);
        request.setAttribute("downtimeList", downtimeList);
        request.setAttribute("eventDowntime", eventDowntime);
        request.setAttribute("eventCount", eventCount);
        request.setAttribute("eventMttr", eventMttr);
        request.setAttribute("accMttr", accMttr);
        request.setAttribute("tripMttr", tripMttr);
        request.setAttribute("periodUptimeHours", periodUptimeHours);
        request.setAttribute("eventUptimeHours", eventUptimeHours);
        request.setAttribute("tripUptimeHours", tripUptimeHours);
        request.setAttribute("accDownHours", accDownHours);
        request.setAttribute("accMtbf", accMtbf);
        request.setAttribute("eventMtbf", eventMtbf);
        request.setAttribute("tripMtbf", tripMtbf);
        request.setAttribute("accFailureRate", accFailureRate);
        request.setAttribute("eventFailureRate", eventFailureRate);
        request.setAttribute("tripFailureRate", tripFailureRate);
        request.setAttribute("accAvailability", accAvailability);
        request.setAttribute("eventAvailability", eventAvailability);
        request.setAttribute("tripAvailability", tripAvailability);
        request.setAttribute("restore", restore);
        request.setAttribute("nonRestore", nonRestore);
        request.setAttribute("incidentDowntime", incidentDowntime);
        request.setAttribute("incidentCount", incidentCount);
        request.setAttribute("incidentMttr", incidentMttr);
        request.setAttribute("beamSummary", beamSummary);
        request.setAttribute("eventAvailabilityLoss", eventAvailabilityLoss);
        request.setAttribute("tripAvailabilityLoss", tripAvailabilityLoss);

        request.getRequestDispatcher("/WEB-INF/views/reports/downtime-summary.jsp").forward(request,
                response);
    }

    private String getCurrentUrl(HttpServletRequest request, Date start, Date end,
            BigInteger eventTypeId) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(TimeUtil.getFriendlyDateTimePattern());

        String typeStr = "";

        if (eventTypeId != null) {
            typeStr = eventTypeId.toString();
        }

        return request.getContextPath() + "/reports/downtime-summary?start="
                + URLEncoder.encode(dateFormat.format(
                        start), StandardCharsets.UTF_8) + "&end=" + URLEncoder.encode(
                        dateFormat.format(end), StandardCharsets.UTF_8) + "&type=" + typeStr;
    }
}
