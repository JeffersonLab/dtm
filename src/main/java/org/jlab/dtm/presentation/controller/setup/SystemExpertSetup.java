package org.jlab.dtm.presentation.controller.setup;

import jakarta.ejb.EJB;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigInteger;
import java.util.List;
import org.jlab.dtm.business.session.CategoryFacade;
import org.jlab.dtm.business.session.SystemFacade;
import org.jlab.dtm.persistence.entity.Category;
import org.jlab.dtm.persistence.entity.SystemEntity;
import org.jlab.smoothness.presentation.util.ParamConverter;

/**
 * @author ryans
 */
@WebServlet(
    name = "SystemExpertSetup",
    urlPatterns = {"/setup/subsystem-expert"})
public class SystemExpertSetup extends HttpServlet {

  @EJB CategoryFacade categoryFacade;
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
    BigInteger categoryId = ParamConverter.convertBigInteger(request, "categoryId");
    BigInteger systemId = ParamConverter.convertBigInteger(request, "systemId");

    Category categoryRoot = categoryFacade.findBranch(null);
    List<SystemEntity> systemList = systemFacade.findWithCategory(categoryId);

    String selectionMessage = null;
    Category selectedCategory = null;
    SystemEntity selectedSystem = null;

    if (categoryId != null) {
      selectedCategory = categoryFacade.find(categoryId);
      selectionMessage = selectedCategory.getName();
    }

    if (systemId != null) {
      selectedSystem = systemFacade.findWithExpertList(systemId);

      if (selectionMessage == null) {
        selectionMessage = selectedSystem.getName();
      } else {
        selectionMessage = selectionMessage + " > " + selectedSystem.getName();
      }
    }

    request.setAttribute("selectionMessage", selectionMessage);
    request.setAttribute("categoryRoot", categoryRoot);
    request.setAttribute("systemList", systemList);
    request.setAttribute("selectedSystem", selectedSystem);

    getServletConfig()
        .getServletContext()
        .getRequestDispatcher("/WEB-INF/views/setup/subsystem-expert.jsp")
        .forward(request, response);
  }
}
