package org.jlab.dtm.presentation.controller.setup;

import java.io.IOException;
import java.util.List;
import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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
