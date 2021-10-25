package org.jlab.dtm.presentation.controller.ajax;

import java.io.IOException;
import java.io.PrintWriter;
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
import org.jlab.dtm.business.session.CategoryMonthlyGoalFacade;
import org.jlab.dtm.business.session.MonthlyNoteFacade;
import org.jlab.dtm.presentation.util.DtmParamConverter;
import org.jlab.smoothness.business.exception.UserFriendlyException;
import org.jlab.smoothness.business.util.ExceptionUtil;
import org.jlab.smoothness.presentation.util.ParamConverter;

/**
 *
 * @author ryans
 */
@WebServlet(name = "SaveMonthlyInfo", urlPatterns = {"/ajax/save-monthly-info"})
public class SaveMonthlyInfo extends HttpServlet {

    private static final Logger logger = Logger.getLogger(SaveMonthlyInfo.class.getName());
    @EJB
    MonthlyNoteFacade infoFacade;
    @EJB
    CategoryMonthlyGoalFacade catGoalFacade;

    /**
     * Handles the HTTP
     * <code>POST</code> method.
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
            Date month = DtmParamConverter.convertJLabDateTime(request, "month");
            String note = request.getParameter("note");
            Float machineGoal = ParamConverter.convertFloat(request, "machineGoal");
            Float tripGoal = ParamConverter.convertFloat(request, "tripGoal");
            Float eventGoal = ParamConverter.convertFloat(request, "eventGoal");
            Long[] catIdArray = ParamConverter.convertLongArray(request, "catId[]");
            Float[] catGoalArray = ParamConverter.convertFloatArray(request, "catGoal[]");
            
            infoFacade.save(month, note, machineGoal, tripGoal, eventGoal);
            
            catGoalFacade.save(month, catIdArray, catGoalArray);
        } catch (EJBAccessException e) {
            logger.log(Level.WARNING, "Unable to perform save due to access exception");
            errorReason = e.getMessage();
        } catch (UserFriendlyException e) {
            logger.log(Level.FINE, "Unable to save {0}", e.getMessage());
            errorReason = e.getMessage();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Unable to save", e);
            Throwable rootCause = ExceptionUtil.getRootCause(e);
            /*if (rootCause instanceof SQLException) {
                SQLException dbException = (SQLException) rootCause;

                if (dbException.getErrorCode() == 20001) {
                    errorReason = "Action results in overlapping events";
                } else if((dbException.getErrorCode() == 1) && (dbException.getMessage().contains("EVENT_AK1"))) { // If attempt to insert with exact same start and end trigger check won't catch so we do this check instead
                    errorReason = "Action results in overlapping events (Is there already an event during this time?)";
                } else {
                    errorReason = "Database exception";
                }
            } else {
                errorReason = "Something unexpected happened";
            }*/
            errorReason = rootCause.getMessage();
        }

        response.setContentType("text/xml");

        PrintWriter pw = response.getWriter();

        String xml;

        if (errorReason == null) {
            xml = "<response><span class=\"status\">Success</span></response>";
        } else {
            xml = "<response><span class=\"status\">Error</span><span "
                    + "class=\"reason\">" + errorReason + "</span></response>";
        }

        pw.write(xml);

        pw.flush();

        boolean error = pw.checkError();

        if (error) {
            logger.log(Level.SEVERE, "PrintWriter Error");
        }
    }
}
