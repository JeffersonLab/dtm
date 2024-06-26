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
import org.jlab.dtm.business.session.EventFacade;
import org.jlab.smoothness.business.exception.UserFriendlyException;
import org.jlab.smoothness.presentation.util.ParamConverter;

/**
 * @author ryans
 */
@WebServlet(
    name = "RemoveEvent",
    urlPatterns = {"/ajax/remove-event"})
public class RemoveEvent extends HttpServlet {

  private static final Logger logger = Logger.getLogger(RemoveEvent.class.getName());

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

      eventFacade.removeEvent(eventId);
    } catch (EJBAccessException e) {
      logger.log(Level.WARNING, "Unable to delete event due to access exception");
      errorReason = "Not Authorized";
    } catch (UserFriendlyException e) {
      logger.log(Level.FINE, "Unable to delete event: {1}", e.getMessage());
      errorReason = "Invalid User Input";
    } catch (Exception e) {
      logger.log(Level.SEVERE, "Unable to delete event", e);
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
