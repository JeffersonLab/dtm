package org.jlab.dtm.presentation.controller.reports;

import jakarta.ejb.EJB;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.jlab.dtm.business.session.SystemFacade;
import org.jlab.dtm.persistence.entity.SystemEntity;

/**
 * @author ryans
 */
@WebServlet(
    name = "ExpertReport",
    urlPatterns = {"/reports/expert"})
public class ExpertReport extends HttpServlet {
  @EJB SystemFacade systemFacade;

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

    List<SystemEntity> systemList = systemFacade.findAllWithExpertList();

    request.setAttribute("systemList", systemList);

    getServletConfig()
        .getServletContext()
        .getRequestDispatcher("/WEB-INF/views/reports/expert.jsp")
        .forward(request, response);
  }
}
