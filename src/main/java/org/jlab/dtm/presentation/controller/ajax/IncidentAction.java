package org.jlab.dtm.presentation.controller.ajax;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.sql.SQLException;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.ejb.EJBAccessException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jlab.dtm.business.session.IncidentFacade;
import org.jlab.dtm.persistence.entity.Event;
import org.jlab.dtm.persistence.entity.Incident;
import org.jlab.dtm.persistence.util.DtmSqlUtil;
import org.jlab.dtm.presentation.util.DtmParamConverter;
import org.jlab.smoothness.business.exception.UserFriendlyException;
import org.jlab.smoothness.presentation.util.ParamConverter;

/**
 *
 * @author ryans
 */
@WebServlet(name = "IncidentAction", urlPatterns = {"/ajax/incident-action"})
public class IncidentAction extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(
            IncidentAction.class.getName());
    @EJB
    IncidentFacade incidentFacade;

    /**
     * Handles the HTTP
     * <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String errorReason = null;
        String action = null;
        Event event = null;

        try {
            BigInteger incidentId = ParamConverter.convertBigInteger(request, "incidentId");
            BigInteger eventId = ParamConverter.convertBigInteger(request, "eventId");
            BigInteger eventTypeId = ParamConverter.convertBigInteger(request, "eventTypeId");
            Date eventTimeUp = DtmParamConverter.convertJLabDateTime(request, "eventTimeUp");
            String title = request.getParameter("title");
            String summary = request.getParameter("summary");
            String eventTitle = request.getParameter("eventTitle");
            BigInteger componentId = ParamConverter.convertBigInteger(request, "componentId");
            String componentName = request.getParameter("componentName");
            Date timeDown = DtmParamConverter.convertJLabDateTime(request, "timeDown");
            Date timeUp = DtmParamConverter.convertJLabDateTime(request, "timeUp");
            action = request.getParameter("action");
            String explanation = request.getParameter("explanation");
            String solution = request.getParameter("solution");
            BigInteger[] repairedBy = ParamConverter.convertBigIntegerArray(request, "repairedBy[]");
            String reviewedBy = request.getParameter("reviewedBy");
            String[] expertUsernameArray = request.getParameterValues("expertUsername[]");
            BigInteger rarId = ParamConverter.convertBigInteger(request, "rarId");
            
            if ("add-event".equals(action)) {
                event = incidentFacade.addEvent(eventTypeId, eventTimeUp, timeDown, timeUp, title, summary, componentId, componentName, eventTitle, explanation, solution, repairedBy, reviewedBy, expertUsernameArray, rarId);
            } else if ("add-incident".equals(action)) {
                incidentFacade.addIncident(eventId, timeDown, timeUp, title, summary, componentId, componentName, eventTitle, explanation, solution, repairedBy, reviewedBy, expertUsernameArray, rarId);
            } else if ("edit-incident".equals(action)) {
                incidentFacade.editIncident(incidentId, timeDown, timeUp, title, summary, componentId, componentName, eventTitle, explanation, solution, repairedBy, reviewedBy, expertUsernameArray, rarId);
            } else {
                errorReason = "unrecognized action";
            }
        } catch (EJBAccessException e) {
            LOGGER.log(Level.WARNING, "Unable to perform incident action ({0}) due to access exception", action);
            errorReason = e.getMessage();
        } catch(NumberFormatException e) {
            LOGGER.log(Level.FINE, "Unable to perform incident action ({0}): {1}", new Object[]{action, e.getMessage()});
            errorReason = "Number Format Unacceptable: " + e.getMessage();            
        } catch (UserFriendlyException e) {
            LOGGER.log(Level.FINE, "Unable to perform incident action ({0}): {1}", new Object[]{action, e.getMessage()});
            errorReason = e.getMessage();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Unable to perform incident action", e);
            Throwable rootCause = DtmSqlUtil.getFirstNestedSqlException(e);

            LOGGER.log(Level.WARNING, "Root Cause: {0}", rootCause.getClass());

            if (rootCause instanceof SQLException) {
                SQLException dbException = (SQLException) rootCause;

                if (dbException.getErrorCode() == 20001) {
                    errorReason = "Action results in overlapping events";
                } else if((dbException.getErrorCode() == 1) && (dbException.getMessage().contains("EVENT_AK1"))) { // If attempt to insert with exact same start and end trigger check won't catch so we do this check instead
                    errorReason = "Action results in overlapping events (Is there already an event during this time?)";
                } else {
                    errorReason = "Database exception";
                }
            } else {
                errorReason = "Something unexpected happened";
            }
        }

        response.setContentType("text/xml");

        PrintWriter pw = response.getWriter();

        String xml;

        if (errorReason == null) {
            xml = "<response><span class=\"status\">Success</span>";

            if(event != null) {
                xml = xml + "<span class=\"event\">" + event.getEventId() + "</span>";

                if(event.getIncidentList() != null && !event.getIncidentList().isEmpty()) {
                    for(Incident i: event.getIncidentList()) {
                        xml = xml + "<span class=\"incident\">" + i.getIncidentId() + "</span>";
                    }
                }
            }

            xml = xml + "</response>";
        } else {
            xml = "<response><span class=\"status\">Error</span><span "
                    + "class=\"reason\">" + errorReason + "</span></response>";
        }

        pw.write(xml);

        pw.flush();

        boolean error = pw.checkError();

        if (error) {
            LOGGER.log(Level.SEVERE, "PrintWriter Error");
        }
    }
}
