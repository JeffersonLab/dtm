package org.jlab.dtm.presentation.controller;

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
import org.jlab.dtm.business.params.TripParams;
import org.jlab.dtm.business.service.FsdTripService;
import org.jlab.dtm.business.session.SystemFacade;
import org.jlab.dtm.persistence.enumeration.AccMachineState;
import org.jlab.dtm.persistence.enumeration.HallMachineState;
import org.jlab.dtm.persistence.enumeration.Shift;
import org.jlab.dtm.persistence.filter.FsdTripFilter;
import org.jlab.dtm.persistence.model.FsdTrip;
import org.jlab.dtm.presentation.params.TripUrlParamHandler;
import org.jlab.smoothness.business.util.TimeUtil;
import org.jlab.smoothness.presentation.util.Paginator;
import org.jlab.smoothness.presentation.util.ParamUtil;

/**
 *
 * @author ryans
 */
@WebServlet(name = "Trips", urlPatterns = {"/trips"})
public class Trips extends HttpServlet {

    @EJB
    SystemFacade systemFacade;

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

       TripUrlParamHandler paramHandler
                = new TripUrlParamHandler(request);

        TripParams params;

        if (paramHandler.qualified()) {
            params = paramHandler.convert();
            paramHandler.validate(params);
            paramHandler.store(params);
        } else {
            params = paramHandler.materialize();
            paramHandler.redirect(response, params);
            return;
        }

        FsdTripService tripService = new FsdTripService();

        List<FsdTrip> tripList = null;
        BigInteger totalRecords = null;

        int offset = ParamUtil.convertAndValidateNonNegativeInt(request, "offset", 0);
        int max = ParamUtil.convertAndValidateNonNegativeInt(request, "max", 25);
        
        FsdTripFilter filter
                = new FsdTripFilter(params);

        try {
            tripList = tripService.filterListWithDependencies(filter, offset, max);
            totalRecords = tripService.countList(filter);

        } catch (SQLException e) {
            throw new ServletException("Unable to query database for trips", e);
        }

        Paginator paginator = new Paginator(totalRecords.intValue(), offset, max);

        DecimalFormat formatter = new DecimalFormat("###,###");

        String selectionMessage = paramHandler.message(params).trim();
        //String selectionMessage = filter.getSelectionMessage().trim();

        String paginationMessage = " {" + paginator.getStartNumber() + " - "
                + paginator.getEndNumber() + " of " + formatter.format(totalRecords) + "}";

        selectionMessage = selectionMessage + paginationMessage;

        request.setAttribute("start", params.getStart());
        request.setAttribute("end", params.getEnd());
        request.setAttribute("max", max);
        request.setAttribute("selectionMessage", selectionMessage);
        request.setAttribute("tripList", tripList);
        request.setAttribute("paginator", paginator);
        request.setAttribute("accMachineStateArray", AccMachineState.values());
        request.setAttribute("hallMachineStateArray", HallMachineState.values());

        request.getRequestDispatcher("/WEB-INF/views/trips.jsp").forward(request, response);
    }
}
