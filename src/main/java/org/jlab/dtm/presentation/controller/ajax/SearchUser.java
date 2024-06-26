package org.jlab.dtm.presentation.controller.ajax;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jlab.smoothness.business.service.UserAuthorizationService;
import org.jlab.smoothness.persistence.view.User;
import org.jlab.smoothness.presentation.util.ParamConverter;

/**
 * @author ryans
 */
@WebServlet(
    name = "SearchUser",
    urlPatterns = {"/ajax/search-user"})
public class SearchUser extends HttpServlet {

  private static final Logger LOGGER = Logger.getLogger(SearchUser.class.getName());

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

    List<User> userList = null;
    Integer totalRecords = null;

    UserAuthorizationService service = UserAuthorizationService.getInstance();

    try {
      String term = request.getParameter("term");
      Integer max = ParamConverter.convertInteger(request, "max");

      userList = service.getUsersLike(term, 0, max);

      if (max != null) {
        totalRecords = service.countUsersLike(term);
      } else {
        totalRecords = userList.size();
      }
    } catch (Exception e) {
      LOGGER.log(Level.SEVERE, "Unable to perform user search", e);
      errorReason = "Unable to perform user search";
    }

    response.setContentType("application/json");

    PrintWriter pw = response.getWriter();

    JsonObjectBuilder json = Json.createObjectBuilder();

    if (errorReason == null) {
      JsonArrayBuilder staffJsonArray = Json.createArrayBuilder();
      if (userList != null) {
        for (User user : userList) {
          JsonObjectBuilder staffJson = Json.createObjectBuilder();
          staffJson.add(
              "label",
              user.getUsername()); // username might be null (though we know it can't be if username
          // term search is required)
          staffJson.add("value", user.getUsername());
          staffJson.add("username", user.getUsername());
          staffJson.add("first", user.getFirstname() == null ? "" : user.getFirstname());
          staffJson.add("last", user.getLastname() == null ? "" : user.getLastname());
          staffJsonArray.add(staffJson);
        }
      }
      json.add("records", staffJsonArray);
      json.add("total_records", totalRecords);
    } else {
      json.add("error", errorReason);
    }

    pw.write(json.build().toString());

    pw.flush();

    boolean error = pw.checkError();

    if (error) {
      LOGGER.log(Level.SEVERE, "PrintWriter Error");
    }
  }
}
