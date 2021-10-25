package org.jlab.dtm.presentation.controller.reports;

import java.io.IOException;
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
import org.jlab.dtm.business.params.IncidentDowntimeReportParams;
import org.jlab.dtm.business.session.AbstractFacade;
import org.jlab.dtm.business.session.AbstractFacade.OrderDirective;
import org.jlab.dtm.business.session.EventTypeFacade;
import org.jlab.dtm.business.session.IncidentReportService;
import org.jlab.dtm.business.session.IncidentReportService.IncidentSummary;
import org.jlab.dtm.business.session.ResponsibleGroupFacade;
import org.jlab.dtm.business.session.SystemFacade;
import org.jlab.dtm.persistence.entity.EventType;
import org.jlab.dtm.persistence.entity.ResponsibleGroup;
import org.jlab.dtm.persistence.entity.SystemEntity;
import org.jlab.dtm.persistence.enumeration.Shift;
import org.jlab.dtm.presentation.params.IncidentDowntimeReportUrlParamHandler;
import org.jlab.smoothness.business.util.DateIterator;
import org.jlab.smoothness.business.util.TimeUtil;
import org.jlab.smoothness.presentation.util.Paginator;

/**
 *
 * @author ryans
 */
@WebServlet(name = "IncidentDowntime", urlPatterns = {"/reports/incident-downtime"})
public class IncidentDowntime extends HttpServlet {

    @EJB
    EventTypeFacade eventTypeFacade;
    @EJB
    IncidentReportService incidentReportService;
    @EJB
    SystemFacade systemFacade;
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

        IncidentDowntimeReportUrlParamHandler paramHandler
                = new IncidentDowntimeReportUrlParamHandler(request, today, sevenDaysAgo, eventTypeFacade, systemFacade, groupFacade);

        IncidentDowntimeReportParams params;

        if (paramHandler.qualified()) {
            params = paramHandler.convert();
            paramHandler.validate(params);
            paramHandler.store(params);
        } else {
            params = paramHandler.materialize();
            paramHandler.redirect(response, params);
            return;
        }

        List<ResponsibleGroup> groupList = groupFacade.findAll(new OrderDirective("name"));
        List<SystemEntity> systemList = systemFacade.findAll(new AbstractFacade.OrderDirective("weight"), new AbstractFacade.OrderDirective("name"));
        List<EventType> eventTypeList = eventTypeFacade.findAll(new AbstractFacade.OrderDirective("weight"));

        double periodDurationHours = 0.0;
        List<IncidentSummary> incidentList = null;
        long totalRecords = 0;
        double totalRepairTime = 0;
        double overnightRepairTime = 0;
        long overnightOpenedCount = 0;
        long overnightClosedCount = 0;
        double overnightOpenedHours = 0;
        double overnightClosedHours = 0;

        if (params.getStart() != null && params.getEnd() != null) {
            periodDurationHours = (params.getEnd().getTime() - params.getStart().getTime()) / 1000.0 / 60.0 / 60.0;

            params.setSortByDuration(true);
            incidentList = incidentReportService.filterList(params);
            totalRecords = incidentReportService.countFilterList(params);

            totalRepairTime = incidentReportService.sumTotalBoundedDuration(params);

            DateIterator it = new DateIterator(TimeUtil.startOfDay(params.getStart(), Calendar.getInstance()), TimeUtil.startOfDay(params.getEnd(), Calendar.getInstance()));

            IncidentDowntimeReportParams itParams = new IncidentDowntimeReportParams();

            itParams.setEventTypeId(params.getEventTypeId());
            itParams.setSystemId(params.getSystemId());
            itParams.setGroupId(params.getGroupId());
            itParams.setComponent(params.getComponent());
            itParams.setBeamTransport(params.getBeamTransport());
            itParams.setMaxDuration(params.getMaxDuration());
            itParams.setMaxDurationUnits(params.getMinDurationUnits());
            itParams.setMinDuration(params.getMinDuration());
            itParams.setMinDurationUnits(params.getMinDurationUnits());

            while (it.hasNext()) {
                Date next = it.next();

                Calendar cal = Calendar.getInstance();
                cal.setTime(next);
                cal.set(Calendar.HOUR_OF_DAY, 7);
                Date nEnd = cal.getTime();
                cal.add(Calendar.DATE, -1);
                cal.set(Calendar.HOUR_OF_DAY, 19);
                Date nStart = cal.getTime();

                if (nStart.before(params.getStart())) {
                    nStart = params.getStart();
                }

                if (nEnd.after(params.getEnd())) {
                    nEnd = params.getEnd();
                }

                itParams.setStart(nStart);
                itParams.setEnd(nEnd);

                double hrs = incidentReportService.sumTotalBoundedDuration(itParams);

                //System.out.println("next: " + next);
                //System.out.println("nStart: " + nStart);
                //System.out.println("nEnd: " + nEnd);
                //System.out.println("hours: " + hrs);
                overnightRepairTime = overnightRepairTime + hrs;
            }

            //System.out.println("offHoursRepairTime: " + offHoursRepairTime);
            params.setOvernightOpended(Boolean.TRUE);
            overnightOpenedCount = incidentReportService.countFilterList(params);
            overnightOpenedHours = incidentReportService.sumTotalBoundedDuration(params);

            params.setOvernightOpended(Boolean.FALSE);
            overnightClosedCount = incidentReportService.countFilterList(params);
            overnightClosedHours = incidentReportService.sumTotalBoundedDuration(params);
        }

        Paginator paginator = new Paginator((int) totalRecords, params.getOffset(), params.getMax());

        DecimalFormat formatter = new DecimalFormat("###,###");

        String selectionMessage = paramHandler.message(params);

        selectionMessage = selectionMessage + " {" + paginator.getStartNumber() + " - " + paginator.getEndNumber() + " of " + formatter.format(totalRecords) + "}";

        request.setAttribute("start", params.getStart());
        request.setAttribute("end", params.getEnd());
        request.setAttribute("eventTypeList", eventTypeList);
        request.setAttribute("groupList", groupList);
        request.setAttribute("systemList", systemList);
        request.setAttribute("selectionMessage", selectionMessage);
        request.setAttribute("today", today);
        request.setAttribute("sevenDaysAgo", sevenDaysAgo);
        request.setAttribute("incidentList", incidentList);
        request.setAttribute("totalRepairTime", totalRepairTime);
        request.setAttribute("overnightRepairTime", overnightRepairTime);
        request.setAttribute("overnightOpenedCount", overnightOpenedCount);
        request.setAttribute("overnightClosedCount", overnightClosedCount);
        request.setAttribute("overnightOpenedHours", overnightOpenedHours);
        request.setAttribute("overnightClosedHours", overnightClosedHours);
        request.setAttribute("paginator", paginator);
        request.setAttribute("periodDurationHours", periodDurationHours);

        request.getRequestDispatcher("/WEB-INF/views/reports/incident-downtime.jsp").forward(request, response);
    }
}
