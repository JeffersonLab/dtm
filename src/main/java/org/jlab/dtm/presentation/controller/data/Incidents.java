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
import org.jlab.dtm.business.params.IncidentParams;
import org.jlab.dtm.business.session.IncidentFacade;
import org.jlab.dtm.persistence.entity.Incident;
import org.jlab.smoothness.presentation.util.ParamConverter;

/**
 * @author ryans
 */
@WebServlet(
    name = "IncidentsData",
    urlPatterns = {"/data/incidents"})
public class Incidents extends HttpServlet {

  private static final Logger logger = Logger.getLogger(Incidents.class.getName());
  @EJB IncidentFacade incidentFacade;

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
    List<Incident> incidentList = null;

    try {
      BigInteger incidentId = ParamConverter.convertBigInteger(request, "id");
      Integer max = ParamConverter.convertInteger(request, "max");
      Integer offset = ParamConverter.convertInteger(request, "offset");
      String title = request.getParameter("title");

      if (max == null) {
        max = 10;
      }

      if (offset == null) {
        offset = 0;
      }

      IncidentParams params = new IncidentParams();
      params.setIncidentId(incidentId);
      params.setMax(max);
      params.setOffset(offset);
      params.setTitle(title);

      incidentList = incidentFacade.filterList(params);
    } catch (Exception e) {
      logger.log(Level.SEVERE, "Unable to obtain incident list", e);
      errorReason = e.getClass().getSimpleName() + ": " + e.getMessage();
    }

    response.setContentType("application/json");

    PrintWriter pw = response.getWriter();

    JsonObjectBuilder builder = Json.createObjectBuilder();

    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");

    if (errorReason == null) {
      JsonArrayBuilder arrBld = Json.createArrayBuilder();
      if (incidentList != null) {
        for (Incident incident : incidentList) {
          JsonObjectBuilder incBld = Json.createObjectBuilder();
          incBld.add("id", incident.getIncidentId());
          incBld.add("title", incident.getTitle());
          incBld.add("summary", incident.getSummary());
          incBld.add("time_down", formatter.format(incident.getTimeDown()));

          if (incident.getTimeUp() == null) {
            incBld.addNull("time_up");
          } else {
            incBld.add("time_up", formatter.format(incident.getTimeUp()));
          }

          incBld.add("component", incident.getComponent().getName());
          incBld.add("event_id", incident.getEvent().getEventId());

          arrBld.add(incBld.build());
        }
      }
      builder.add("stat", "ok");
      builder.add("data", arrBld.build());
    } else {
      builder.add("stat", "fail");
      builder.add("error", errorReason);
    }

    String jsonStr = builder.build().toString();

    pw.write(jsonStr);

    pw.flush();

    boolean error = pw.checkError();

    if (error) {
      logger.log(Level.SEVERE, "PrintWriter Error");
    }
  }
}
