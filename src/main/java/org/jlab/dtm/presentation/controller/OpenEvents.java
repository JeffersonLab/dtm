package org.jlab.dtm.presentation.controller;

import java.io.IOException;
import java.math.BigInteger;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jlab.dtm.business.session.AbstractFacade.OrderDirective;
import org.jlab.dtm.business.session.CategoryFacade;
import org.jlab.dtm.business.session.EventFacade;
import org.jlab.dtm.business.session.EventTypeFacade;
import org.jlab.dtm.business.session.ResponsibleGroupFacade;
import org.jlab.dtm.persistence.entity.Category;
import org.jlab.dtm.persistence.entity.Event;
import org.jlab.dtm.persistence.entity.EventType;
import org.jlab.dtm.persistence.entity.ResponsibleGroup;

/**
 *
 * @author ryans
 */
@WebServlet(name = "OpenEvents", urlPatterns = {"/open-events"})
public class OpenEvents extends HttpServlet {

    private static final Logger logger = Logger.getLogger(
            OpenEvents.class.getName());
    @EJB
    EventFacade eventFacade;
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
        logger.log(Level.FINEST, "Querying category tree");
        Category cebafRoot = categoryFacade.findBranch(BigInteger.valueOf(1L), BigInteger.valueOf(2L));
        Category lerfRoot = categoryFacade.findBranch(BigInteger.valueOf(2L), BigInteger.valueOf(2L));
        Category otherRoot = categoryFacade.findBranch(BigInteger.valueOf(3L), BigInteger.valueOf(2L));
        Category cryoRoot = categoryFacade.findBranch(BigInteger.valueOf(4L), BigInteger.valueOf(2L));
        Category facilitiesRoot = categoryFacade.findBranch(BigInteger.valueOf(5L), BigInteger.valueOf(2L));
        Category hallRoot = categoryFacade.findBranch(BigInteger.valueOf(465L), BigInteger.valueOf(2L));
        logger.log(Level.FINEST, "Querying event type list");
        List<EventType> eventTypeList = eventTypeFacade.findAll(new OrderDirective("weight"));
        logger.log(Level.FINEST, "Querying event list");
        List<Event> openEventList = eventFacade.findOpenEventListWithIncidents();

        logger.log(Level.FINEST, "Querying restore time");
        eventFacade.computeRestoreTime(openEventList);
        
        logger.log(Level.FINEST, "Querying event history");
        eventFacade.loadClosedBy(openEventList);
        
        logger.log(Level.FINEST, "Querying incident repaired by list");
        eventFacade.loadRepairedBy(openEventList);

        // Expert Reviews
        eventFacade.loadReviewedBy(openEventList);

        logger.log(Level.FINEST, "Querying group list");
        List<ResponsibleGroup> groupList = groupFacade.findAll(new OrderDirective("name"));

        request.setAttribute("cebafRoot", cebafRoot);
        request.setAttribute("lerfRoot", lerfRoot);
        request.setAttribute("otherRoot", otherRoot);
        request.setAttribute("cryoRoot", cryoRoot);
        request.setAttribute("facilitiesRoot", facilitiesRoot);
        request.setAttribute("hallRoot", hallRoot);
        request.setAttribute("openEventList", openEventList);
        request.setAttribute("eventTypeList", eventTypeList);
        request.setAttribute("groupList", groupList);

        request.getRequestDispatcher("/WEB-INF/views/open-events.jsp").forward(request, response);
    }
}
