package org.jlab.dtm.presentation.controller.data;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jlab.dtm.business.session.EscalationService;
import org.jlab.dtm.business.session.EventFacade;
import org.jlab.dtm.persistence.entity.Event;
import org.jlab.dtm.persistence.entity.Incident;
import org.jlab.dtm.presentation.util.DtmFunctions;
import org.jlab.smoothness.presentation.util.ParamConverter;

/**
 * @author ryans
 */
@WebServlet(
    name = "EventsData",
    urlPatterns = {"/data/events"})
public class Events extends HttpServlet {

  private static final Logger logger = Logger.getLogger(Events.class.getName());
  @EJB EventFacade eventFacade;
  @EJB EscalationService escalationService;

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
    String errorReason = null;
    List<Event> eventList = null;
    String jsonp = null;

    try {
      BigInteger eventTypeId = ParamConverter.convertBigInteger(request, "event_type_id");
      BigInteger eventId = ParamConverter.convertBigInteger(request, "event_id");
      BigInteger incidentId = ParamConverter.convertBigInteger(request, "incident_id");
      jsonp = request.getParameter("jsonp");

      eventList = eventFacade.filterListWithIncidentsDefaultOpen(eventTypeId, eventId, incidentId);
    } catch (Exception e) {
      logger.log(Level.SEVERE, "Unable to obtain event list", e);
      errorReason = e.getClass().getSimpleName() + ": " + e.getMessage();
    }

    response.setContentType("application/json");

    PrintWriter pw = response.getWriter();

    JsonObjectBuilder builder = Json.createObjectBuilder();

    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");

    if (errorReason == null) {
      JsonArrayBuilder arrBld = Json.createArrayBuilder();
      if (eventList != null) {
        for (Event event : eventList) {
          JsonObjectBuilder evtBld = Json.createObjectBuilder();
          evtBld.add("id", event.getEventId());
          evtBld.add("title", event.getTitle());
          evtBld.add(
              "duration", DtmFunctions.millisToHumanReadable(event.getElapsedMillis(), false));
          evtBld.add("escalation", escalationService.getEscalationLevel(event));
          evtBld.add("event_type_id", event.getEventType().getEventTypeId());
          evtBld.add("time_down", formatter.format(event.getTimeDown()));
          evtBld.add(
              "time_up", event.getTimeUp() == null ? "" : formatter.format(event.getTimeUp()));

          JsonArrayBuilder incidentArrBld = Json.createArrayBuilder();
          for (Incident incident : event.getIncidentList()) {
            JsonObjectBuilder incBld = Json.createObjectBuilder();
            incBld.add("id", incident.getIncidentId());
            incBld.add("title", incident.getTitle());
            incBld.add("time_down", formatter.format(incident.getTimeDown()));
            incBld.add(
                "time_up",
                incident.getTimeUp() == null ? "" : formatter.format(incident.getTimeUp()));
            incBld.add("system_id", incident.getSystem().getSystemId());
            incBld.add(
                "component_id",
                incident.getComponent() == null
                    ? BigInteger.ZERO
                    : incident.getComponent().getComponentId());

            incidentArrBld.add(incBld.build());
          }

          evtBld.add("incidents", incidentArrBld.build());

          arrBld.add(evtBld.build());
        }
      }
      builder.add("stat", "ok");
      builder.add("data", arrBld.build());
    } else {
      builder.add("stat", "fail");
      builder.add("error", errorReason);
    }

    String jsonStr = builder.build().toString();

    if (jsonp != null) {
      jsonStr = jsonp + "(" + jsonStr + ");";
    }

    pw.write(jsonStr);

    pw.flush();

    boolean error = pw.checkError();

    if (error) {
      logger.log(Level.SEVERE, "PrintWriter Error");
    }
  }
}
