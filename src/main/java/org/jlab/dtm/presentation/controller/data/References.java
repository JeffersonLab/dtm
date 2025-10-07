package org.jlab.dtm.presentation.controller.data;

import jakarta.ejb.EJB;
import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObjectBuilder;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jlab.dtm.business.session.LogbookFacade;
import org.jlab.dtm.persistence.model.LogReference;
import org.jlab.smoothness.business.exception.UserFriendlyException;
import org.jlab.smoothness.presentation.util.ParamConverter;

/**
 * @author ryans
 */
@WebServlet(
    name = "ReferencesData",
    urlPatterns = {"/data/references"})
public class References extends HttpServlet {

  private static final Logger logger = Logger.getLogger(References.class.getName());
  @EJB LogbookFacade logbookFacade;

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
    List<LogReference> referenceList = null;

    try {
      BigInteger incidentId = ParamConverter.convertBigInteger(request, "id");

      if (incidentId == null) {
        throw new UserFriendlyException("Incident ID is required");
      }

      referenceList = logbookFacade.getLogReferences(incidentId);
    } catch (UserFriendlyException e) {
      errorReason = e.getUserMessage();
    } catch (Exception e) {
      logger.log(Level.SEVERE, "Unable to obtain references list", e);
      errorReason = "Unable to obtain references list";
    }

    response.setContentType("application/json");

    PrintWriter pw = response.getWriter();

    JsonObjectBuilder builder = Json.createObjectBuilder();

    if (errorReason == null) {
      JsonArrayBuilder arrBld = Json.createArrayBuilder();
      if (referenceList != null) {
        for (LogReference reference : referenceList) {
          JsonObjectBuilder incBld = Json.createObjectBuilder();
          incBld.add("lognumber", reference.getLognumber());
          incBld.add("title", reference.getTitle());
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
