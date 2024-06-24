package org.jlab.dtm.presentation.controller.data;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigInteger;
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
import org.jlab.dtm.business.session.ComponentFacade;
import org.jlab.dtm.persistence.entity.Component;
import org.jlab.smoothness.presentation.util.ParamConverter;

/**
 * @author ryans
 */
@WebServlet(
    name = "Components",
    urlPatterns = {"/data/components"})
public class Components extends HttpServlet {

  private static final Logger LOGGER = Logger.getLogger(Components.class.getName());
  @EJB ComponentFacade componentFacade;

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
    List<Component> componentList = null;
    Long totalRecords = null;
    String accept = request.getHeader("Accept");
    boolean plaintextFormat = false;
    /*Look at Accept header and if we find text/plain before application/json then we'll do text/plain.  Otherwise we default to application/json*/
    if (accept != null) {
      String[] tokens = accept.split(",");

      for (String token : tokens) {
        if (token != null) {
          if (token.startsWith("text/plain")) {
            plaintextFormat = true;
            break;
          } else if (token.startsWith("application/json")) {
            break;
          }
        }
      }
    }

    /*We can override the HTTP header with a URL parameter*/
    String acceptOverride = request.getParameter("accept");

    if ("plain".equals(acceptOverride)) {
      plaintextFormat = true;
    }

    try {
      BigInteger[] categoryIdArray = ParamConverter.convertBigIntegerArray(request, "category_id");
      BigInteger[] systemIdArray = ParamConverter.convertBigIntegerArray(request, "system_id");
      BigInteger componentId = ParamConverter.convertBigInteger(request, "component_id");
      String q = request.getParameter("q");
      Integer max = ParamConverter.convertInteger(request, "max");
      Integer offset = ParamConverter.convertInteger(request, "offset");

      componentList =
          componentFacade.findMustFilter(
              categoryIdArray, systemIdArray, q, componentId, max, offset);

      if (max != null) {
        totalRecords =
            componentFacade.countMustFilter(categoryIdArray, systemIdArray, q, componentId, max);
      } else {
        totalRecords = Long.valueOf(componentList.size());
      }

    } catch (Exception e) {
      LOGGER.log(Level.SEVERE, "Unable to obtain component list", e);
      errorReason = e.getClass().getSimpleName() + ": " + e.getMessage();
    }

    PrintWriter pw = response.getWriter();

    if (plaintextFormat) {
      response.setContentType("text/plain");

      if (errorReason == null) {
        if (componentList != null) {
          for (Component component : componentList) {
            pw.write(component.getName());
            pw.write(" - ");
            pw.write(component.getComponentId().toString());
            pw.println();
          }
        }
      } else {
        pw.write("Unable to service request");
        pw.println();
        pw.write(errorReason);
      }

    } else {
      response.setContentType("application/json");

      JsonObjectBuilder json = Json.createObjectBuilder();

      if (errorReason == null) {
        JsonArrayBuilder itemJsonArray = Json.createArrayBuilder();
        if (componentList != null) {
          for (Component component : componentList) {
            JsonObjectBuilder itemJson = Json.createObjectBuilder();
            itemJson.add("id", component.getComponentId());
            itemJson.add("name", component.getName());
            itemJson.add("system_id", component.getSystem().getSystemId());

            itemJsonArray.add(itemJson);
          }
        }
        json.add("stat", "ok");
        json.add("data", itemJsonArray);
        json.add("total_records", totalRecords);
      } else {
        json.add("stat", "fail");
        json.add("error", errorReason);
      }

      String jsonStr = json.build().toString();

      pw.write(jsonStr);
    }

    pw.flush();

    boolean error = pw.checkError();

    if (error) {
      LOGGER.log(Level.SEVERE, "PrintWriter Error");
    }
  }
}
