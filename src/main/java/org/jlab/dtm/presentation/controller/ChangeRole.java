package org.jlab.dtm.presentation.controller;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 *
 * @author ryans
 */
@WebServlet(name = "ChangeRole", urlPatterns = {"/change-role"})
public class ChangeRole extends HttpServlet {

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

        String username = request.getRemoteUser();

        if (username == null || username.trim().isEmpty()) {
            throw new ServletException("You must be authenticated to change roles");
        }

        String role = request.getParameter("role");

        String effectiveRole;

        boolean reviewer = request.isUserInRole("dtm-reviewer");

        if (reviewer && "REVIEWER".equals(role)) {
            effectiveRole = "REVIEWER";
        } else {
            effectiveRole = "OPERATOR";
        }

        HttpSession session = request.getSession();
        session.setAttribute("effectiveRole", effectiveRole);

        String returnUrl = request.getParameter("returnUrl");
        if (returnUrl == null || returnUrl.isEmpty()) {
            returnUrl = request.getContextPath();
        }
        response.sendRedirect(response.encodeRedirectURL(returnUrl));
    }
}
