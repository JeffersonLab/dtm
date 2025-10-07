package org.jlab.dtm.presentation.controller.setup.ajax;

import jakarta.ejb.EJB;
import jakarta.ejb.EJBAccessException;
import jakarta.json.Json;
import jakarta.json.stream.JsonGenerator;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jlab.dtm.business.session.SystemExpertFacade;
import org.jlab.smoothness.business.exception.UserFriendlyException;
import org.jlab.smoothness.business.util.ExceptionUtil;
import org.jlab.smoothness.presentation.util.ParamConverter;

/**
 * @author ryans
 */
@WebServlet(
    name = "AddExpert",
    urlPatterns = {"/setup/ajax/add-expert"})
public class AddExpert extends HttpServlet {

  private static final Logger logger = Logger.getLogger(AddExpert.class.getName());

  @EJB SystemExpertFacade expertFacade;

  /**
   * Handles the HTTP <code>POST</code> method.
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

    BigInteger expertId = null;

    try {
      BigInteger systemId = ParamConverter.convertBigInteger(request, "systemId");
      String username = request.getParameter("username");

      logger.log(Level.FINE, "SystemId: {0}; Username: {1}", new Object[] {systemId, username});

      expertId = expertFacade.add(systemId, username);

    } catch (EJBAccessException e) {
      logger.log(Level.WARNING, "Not authorized", e);
      errorReason = "Not authorized";
    } catch (UserFriendlyException e) {
      logger.log(Level.WARNING, "Application Exception", e);
      errorReason = e.getMessage();
    } catch (Exception e) {
      logger.log(Level.SEVERE, "Unable to add expert", e);

      Throwable rootCause = ExceptionUtil.getRootCause(e);

      if (rootCause instanceof SQLException) {
        SQLException dbException = (SQLException) rootCause;

        if (dbException.getErrorCode() == 1
            && "23000".equals(dbException.getSQLState())
            && dbException.getMessage().contains("SYSTEM_EXPERT_AK1")) {
          errorReason = "Expert already associated with Subsystem";
        } else {
          errorReason = "Database exception: " + e.getMessage();
        }
      } else {
        errorReason = e.getClass().getSimpleName() + ": " + e.getMessage();
      }
    }

    String stat = "ok";

    if (errorReason != null) {
      stat = "fail";
    }

    response.setContentType("application/json");

    OutputStream out = response.getOutputStream();

    try (JsonGenerator gen = Json.createGenerator(out)) {
      gen.writeStartObject().write("stat", stat); // This is unnecessary - if 200 OK then it worked
      if (errorReason != null) {
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        gen.write("error", errorReason);
      } else {
        gen.write("id", expertId);
      }
      gen.writeEnd();
    }
  }
}
