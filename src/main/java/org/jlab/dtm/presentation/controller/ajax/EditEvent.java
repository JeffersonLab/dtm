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
import org.jlab.dtm.business.session.EventFacade;
import org.jlab.dtm.persistence.util.DtmSqlUtil;
import org.jlab.dtm.presentation.util.DtmParamConverter;
import org.jlab.smoothness.business.exception.UserFriendlyException;
import org.jlab.smoothness.presentation.util.ParamConverter;

/**
 * @author ryans
 */
@WebServlet(
    name = "EditEvent",
    urlPatterns = {"/ajax/edit-event"})
public class EditEvent extends HttpServlet {

  private static final Logger logger = Logger.getLogger(EditEvent.class.getName());

  @EJB EventFacade eventFacade;

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
      BigInteger eventId = ParamConverter.convertBigInteger(request, "eventId");
      BigInteger eventTypeId = ParamConverter.convertBigInteger(request, "eventTypeId");
      String title = request.getParameter("eventTitle");
      Date timeUp = DtmParamConverter.convertJLabDateTime(request, "timeUp");

      eventFacade.editEvent(eventId, timeUp, title, eventTypeId);
    } catch (EJBAccessException e) {
      logger.log(Level.WARNING, "Unable to edit event time up due to access exception");
      errorReason = e.getMessage();
    } catch (UserFriendlyException e) {
      logger.log(Level.FINE, "Unable to edit event time up: {1}", e.getMessage());
      errorReason = e.getMessage();
    } catch (Exception e) {
      logger.log(Level.SEVERE, "Unable to perform event action", e);
      Throwable rootCause = DtmSqlUtil.getFirstNestedSqlException(e);

      if (rootCause != null) {
        logger.log(Level.WARNING, "Root Cause: {0}", rootCause.getClass());

        SQLException dbException = (SQLException) rootCause;

        if (dbException.getErrorCode() == 20020) {
          errorReason = "Action results in overlapping events";
        } else if (dbException.getErrorCode() == 1 && "23000".equals(dbException.getSQLState())) {
          errorReason = "Action results in overlapping events (Time Up match found)";
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
