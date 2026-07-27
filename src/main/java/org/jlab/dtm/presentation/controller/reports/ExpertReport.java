package org.jlab.dtm.presentation.controller.reports;

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
    name = "ExpertReport",
    urlPatterns = {"/reports/expert"})
public class ExpertReport extends HttpServlet {
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

    Category selectedCategory = null;

    BigInteger categoryId = ParamConverter.convertBigInteger(request, "category");

    if (categoryId != null) {
      selectedCategory = categoryFacade.find(categoryId);
    }

    List<Category> categoryList = categoryFacade.findAlphaCategoryList();
    List<SystemEntity> systemList = systemFacade.findAllWithExpertList(categoryId);

    String selectionMessage = "All Experts";

    if (selectedCategory != null) {
      selectionMessage = "Category \"" + selectedCategory.getName() + "\"";
    }

    request.setAttribute("selectionMessage", selectionMessage);
    request.setAttribute("categoryList", categoryList);
    request.setAttribute("systemList", systemList);

    getServletConfig()
        .getServletContext()
        .getRequestDispatcher("/WEB-INF/views/reports/expert.jsp")
        .forward(request, response);
  }
}
