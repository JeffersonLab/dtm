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
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jlab.dtm.business.session.IncidentFacade;
import org.jlab.dtm.persistence.enumeration.SystemExpertAcknowledgement;
import org.jlab.dtm.presentation.util.DtmParamConverter;
import org.jlab.smoothness.business.util.ExceptionUtil;
import org.jlab.smoothness.presentation.util.ParamConverter;

/**
 * @author ryans
 */
@WebServlet(
    name = "EditSystemExpertReview",
    urlPatterns = {"/ajax/edit-system-expert-review"})
public class EditSystemExpertReview extends HttpServlet {

  private static final Logger logger = Logger.getLogger(EditSystemExpertReview.class.getName());
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
      SystemExpertAcknowledgement acknowledgement =
          DtmParamConverter.convertSystemExpertAcknowledgement(request, "acknowledged");
      String rootCause = request.getParameter("rootCause");

      incidentFacade.editSystemExpertReview(incidentId, acknowledgement, rootCause);
    } catch (EJBAccessException e) {
      logger.log(Level.WARNING, "Unable to edit root cause due to access exception");
      errorReason = "Not Authorized";
    } catch (Exception e) {
      logger.log(Level.SEVERE, "Unable to edit root cause", e);
      Throwable rootCause = ExceptionUtil.getRootCause(e);
      if (rootCause instanceof SQLException) {
        SQLException dbException = (SQLException) rootCause;

        if (dbException.getErrorCode() == 20001) {
          errorReason = "Action results in overlapping events";
        } else if ((dbException.getErrorCode() == 1)
            && (dbException
                .getMessage()
                .contains(
                    "EVENT_AK1"))) { // If attempt to insert with exact same start and end trigger
          // check won't catch so we do this check instead
          errorReason =
              "Action results in overlapping events (Is there already an event during this time?)";
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
