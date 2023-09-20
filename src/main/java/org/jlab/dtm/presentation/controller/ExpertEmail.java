package org.jlab.dtm.presentation.controller;

import org.jlab.dtm.business.params.IncidentParams;
import org.jlab.dtm.business.session.IncidentFacade;
import org.jlab.dtm.persistence.entity.Incident;
import org.jlab.smoothness.business.util.TimeUtil;

import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;
import java.util.List;

/**
 * @author ryans
 */
@WebServlet(name = "ExpertEmail", urlPatterns = {"/expert-email"})
public class ExpertEmail extends HttpServlet {

    @EJB
    IncidentFacade incidentService;

    /**
     * Handles the HTTP
     * <code>GET</code> method.
     *
     * @param request  servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException      if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String username = request.getParameter("username");

        if (username == null) {
            throw new ServletException("Username is required");
        }

        int numberOfHours = 24;

        if (TimeUtil.isMonday()) {
            numberOfHours = 72;
        }

        IncidentParams params = new IncidentParams();
        params.setReviewed(false);
        params.setSmeUsername(username);
        params.setMax(Integer.MAX_VALUE);
        params.setClosedOnly(true);

        // Just things that have occurred in last "numberOfHours" hours
        params.setStart(TimeUtil.addHours(new Date(), numberOfHours * -1));

        /*
        // This is what ScheduledEmailer does, test code with:
        List<String> usernames = incidentService.findAllExpertsWithRecentUnreviewedIncidents(numberOfHours);
        System.err.println(usernames.size());
        for(String u: usernames) {
            System.err.println(u);
        }*/

        List<Incident> incidentList = incidentService.filterList(params);

        String willNotBeSentMessage = null;

        if (incidentList.isEmpty()) {
            willNotBeSentMessage = "There are no new action items";
        }

        request.setAttribute("username", username);
        request.setAttribute("numberOfHours", numberOfHours);
        request.setAttribute("incidentList", incidentList);
        request.setAttribute("willNotBeSentMessage", willNotBeSentMessage);

        getServletConfig().getServletContext().getRequestDispatcher(
                "/WEB-INF/views/expert-email.jsp").forward(request, response);
    }
}
