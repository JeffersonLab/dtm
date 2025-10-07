package org.jlab.dtm.presentation.controller.ajax;

import jakarta.ejb.EJB;
import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObjectBuilder;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jlab.dtm.business.session.SystemFacade;
import org.jlab.dtm.persistence.entity.SystemEntity;
import org.jlab.dtm.persistence.enumeration.Include;
import org.jlab.dtm.presentation.util.DtmParamConverter;
import org.jlab.smoothness.presentation.util.ParamConverter;

/**
 * @author ryans
 */
@WebServlet(
    name = "FilterSystemListByCategory",
    urlPatterns = {"/ajax/filter-system-list-by-category"})
public class FilterSystemListByCategory extends HttpServlet {

  private static final Logger LOGGER = Logger.getLogger(FilterSystemListByCategory.class.getName());
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
  @SuppressWarnings("unchecked")
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    String errorReason = null;

    List<SystemEntity> systemList = null;

    try {
      BigInteger categoryId = ParamConverter.convertBigInteger(request, "categoryId");
      Include archived = DtmParamConverter.convertInclude(request, "archived");

      systemList = systemFacade.findWithCategory(categoryId, archived);

    } catch (Exception e) {
      LOGGER.log(Level.SEVERE, "Unable to filter system list", e);
      errorReason = "Unable to filter system list";
    }

    response.setContentType("application/json");

    PrintWriter pw = response.getWriter();

    JsonObjectBuilder json = Json.createObjectBuilder();

    if (errorReason == null) {
      JsonArrayBuilder optionJsonArray = Json.createArrayBuilder();
      if (systemList != null) {
        for (SystemEntity system : systemList) {
          JsonObjectBuilder systemJson = Json.createObjectBuilder();
          systemJson.add("name", system.getName());
          systemJson.add("value", system.getSystemId());
          optionJsonArray.add(systemJson);
        }
      }
      json.add("status", "success");
      json.add("optionList", optionJsonArray);
    } else {
      json.add("status", "error");
      json.add("errorReason", errorReason);
    }

    pw.write(json.build().toString());

    pw.flush();

    boolean error = pw.checkError();

    if (error) {
      LOGGER.log(Level.SEVERE, "PrintWriter Error");
    }
  }
}
