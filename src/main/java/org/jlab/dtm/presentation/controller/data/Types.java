package org.jlab.dtm.presentation.controller.data;

import java.io.IOException;
import java.io.PrintWriter;
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
import org.jlab.dtm.business.session.EventTypeFacade;
import org.jlab.dtm.persistence.entity.EventType;

/**
 * @author ryans
 */
@WebServlet(
    name = "EventTypes",
    urlPatterns = {"/data/types"})
public class Types extends HttpServlet {

  private static final Logger logger = Logger.getLogger(Types.class.getName());
  @EJB EventTypeFacade typeFacade;

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
    List<EventType> typeList = null;

    try {
      typeList = typeFacade.filterList(null);
    } catch (Exception e) {
      logger.log(Level.SEVERE, "Unable to obtain event type list", e);
      errorReason = "Unable to obtain event type list";
    }

    response.setContentType("application/json");

    PrintWriter pw = response.getWriter();

    JsonObjectBuilder builder = Json.createObjectBuilder();

    if (errorReason == null) {
      JsonArrayBuilder arrBld = Json.createArrayBuilder();
      if (typeList != null) {
        for (EventType type : typeList) {
          JsonObjectBuilder typeBld = Json.createObjectBuilder();
          typeBld.add("id", type.getEventTypeId());
          typeBld.add("name", type.getName());
          typeBld.add("description", type.getDescription());
          typeBld.add("abbreviation", type.getAbbreviation());

          arrBld.add(typeBld.build());
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
      logger.log(Level.INFO, "/data/types PrintWriter Error");
    }
  }
}
