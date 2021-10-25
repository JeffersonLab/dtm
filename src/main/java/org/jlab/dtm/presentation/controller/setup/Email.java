package org.jlab.dtm.presentation.controller.setup;

import java.io.IOException;
import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jlab.dtm.business.session.DtmSettingsFacade;
import org.jlab.dtm.business.session.ScheduledEmailer;
import org.jlab.dtm.persistence.entity.DtmSettings;
import org.jlab.smoothness.presentation.util.ParamUtil;

/**
 *
 * @author ryans
 */
@WebServlet(name = "Email", urlPatterns = {"/setup/email"})
public class Email extends HttpServlet {

    @EJB
    ScheduledEmailer emailer;
    @EJB
    DtmSettingsFacade settingsFacade;

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

        DtmSettings settings = settingsFacade.findSettings();
    
        request.setAttribute("schedulerEnabled", emailer.isEnabled());

        getServletConfig().getServletContext().getRequestDispatcher(
                "/WEB-INF/views/setup/email.jsp").forward(request, response);
    }

    /**
     * Handles the HTTP <code>Post</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Boolean enabled;

        try {
            enabled = ParamUtil.convertAndValidateYNBoolean(request, "schedulerEnabled");
        } catch (Exception e) {
            throw new ServletException("Unable to convert parameter", e);
        }

        if (enabled == null) {
            throw new ServletException("schedulerEnabled must not be empty");
        }

        emailer.setEnabled(enabled);

        response.sendRedirect(response.encodeRedirectURL("email"));
    }
}
