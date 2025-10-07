package org.jlab.dtm.presentation.controller.setup;

import jakarta.ejb.EJB;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.jlab.dtm.business.session.CategoryFacade;
import org.jlab.dtm.business.session.EventTypeFacade;
import org.jlab.dtm.persistence.entity.Category;
import org.jlab.dtm.persistence.entity.EventType;

/**
 * @author ryans
 */
@WebServlet(
    name = "CategorySetup",
    urlPatterns = {"/setup/categories"})
public class CategorySetup extends HttpServlet {

  @EJB CategoryFacade categoryFacade;
  @EJB EventTypeFacade typeFacade;

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

    List<Category> categoryList = categoryFacade.findAlphaCategoryList();
    List<EventType> typeList = typeFacade.findActiveWithCategories();

    request.setAttribute("categoryList", categoryList);
    request.setAttribute("typeList", typeList);

    Category root = categoryFacade.findRootWithChildren();
    request.setAttribute("root", root);

    getServletConfig()
        .getServletContext()
        .getRequestDispatcher("/WEB-INF/views/setup/categories.jsp")
        .forward(request, response);
  }
}
