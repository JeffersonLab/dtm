package org.jlab.dtm.presentation.controller;

import org.jlab.dtm.business.session.IncidentFacade;
import org.jlab.dtm.persistence.entity.Incident;
import org.jlab.smoothness.presentation.util.ParamConverter;

import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigInteger;

/**
 *
 * @author ryans
 */
@WebServlet(name = "IncidentController", urlPatterns = {"/incident"})
public class IncidentController extends HttpServlet {

    @EJB
    IncidentFacade incidentFacade;

    /**
     * Handles the HTTP
     * <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        BigInteger incidentId = ParamConverter.convertBigInteger(request, "incidentId");

        Incident incident = incidentFacade.find(incidentId);

        request.setAttribute("incident", incident);

        request.getRequestDispatcher("/WEB-INF/views/incident.jsp").forward(request, response);
    }
}
