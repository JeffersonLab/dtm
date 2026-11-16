package org.jlab.dtm.presentation.controller.ajax;

import jakarta.ejb.EJB;
import jakarta.ejb.EJBAccessException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jlab.dtm.business.session.IncidentFacade;
import org.jlab.smoothness.business.exception.UserFriendlyException;
import org.jlab.smoothness.presentation.util.ParamConverter;

/** Saves an incident's Operability (OPR) review: its solution, repairers and OPR reviewer. */
@WebServlet(
    name = "EditOperabilityReview",
    urlPatterns = {"/ajax/edit-operability-review"})
public class EditOperabilityReview extends HttpServlet {

  private static final Logger LOGGER = Logger.getLogger(EditOperabilityReview.class.getName());
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
      String solution = request.getParameter("solution");
      BigInteger[] repairedBy = ParamConverter.convertBigIntegerArray(request, "repairedBy[]");
      String reviewedBy = request.getParameter("reviewedBy");

      incidentFacade.editOperabilityReview(incidentId, solution, repairedBy, reviewedBy);
    } catch (EJBAccessException e) {
      LOGGER.log(Level.WARNING, "Unable to edit operability review due to access exception");
      errorReason = "Not Authorized";
    } catch (NumberFormatException e) {
      LOGGER.log(Level.FINE, "Unable to edit operability review: {0}", e.getMessage());
      errorReason = "Number Format Unacceptable";
    } catch (UserFriendlyException e) {
      LOGGER.log(Level.FINE, "Unable to edit operability review: {0}", e.getMessage());
      errorReason = e.getUserMessage();
    } catch (Exception e) {
      LOGGER.log(Level.SEVERE, "Unable to edit operability review", e);
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
      LOGGER.log(Level.SEVERE, "PrintWriter Error");
    }
  }
}
