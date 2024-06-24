package org.jlab.dtm.presentation.controller.ajax;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigInteger;
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
import org.jlab.smoothness.business.exception.UserFriendlyException;
import org.jlab.smoothness.presentation.util.ParamConverter;

/**
 * @author ryans
 */
@WebServlet(
    name = "RemoveIncident",
    urlPatterns = {"/ajax/remove-incident"})
public class RemoveIncident extends HttpServlet {

  private static final Logger logger = Logger.getLogger(RemoveIncident.class.getName());

  @EJB IncidentFacade incidentFacade;

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

    try {
      BigInteger incidentId = ParamConverter.convertBigInteger(request, "incidentId");

      incidentFacade.removeIncident(incidentId);
    } catch (EJBAccessException e) {
      logger.log(Level.WARNING, "Unable to delete incident due to access exception");
      errorReason = e.getMessage();
    } catch (UserFriendlyException e) {
      logger.log(Level.FINE, "Unable to delete incident: {1}", e.getMessage());
      errorReason = e.getMessage();
    } catch (Exception e) {
      logger.log(Level.SEVERE, "Unable to delete incident", e);
      errorReason = "Something unexpected happened";
    }

    response.setContentType("text/xml");

    PrintWriter pw = response.getWriter();

    String xml;

    if (errorReason == null) {
      xml = "<response><span class=\"status\">Success</span></response>";
    } else {
      xml =
          "<response><span class=\"status\">Error</span><span "
              + "class=\"reason\">"
              + errorReason
              + "</span></response>";
    }

    pw.write(xml);

    pw.flush();

    boolean error = pw.checkError();

    if (error) {
      logger.log(Level.SEVERE, "PrintWriter Error");
    }
  }
}
