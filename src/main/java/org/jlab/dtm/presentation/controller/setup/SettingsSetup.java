package org.jlab.dtm.presentation.controller.setup;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jlab.dtm.business.session.SettingsFacade;
import org.jlab.dtm.persistence.entity.Setting;
import org.jlab.smoothness.presentation.util.Paginator;
import org.jlab.smoothness.presentation.util.ParamUtil;

/**
 * @author ryans
 */
@WebServlet(
    name = "SettingsSetup",
    urlPatterns = {"/setup/settings"})
public class SettingsSetup extends HttpServlet {

  @EJB SettingsFacade settingsFacade;

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

    String key = request.getParameter("key");
    String tag = request.getParameter("tag");
    int offset = ParamUtil.convertAndValidateNonNegativeInt(request, "offset", 0);
    int max = 100;

    List<Setting> settingList = settingsFacade.filterList(key, tag, offset, max);

    long totalRecords = settingsFacade.countList(key, tag);

    List<String> tagList = settingsFacade.findTags();

    Paginator paginator = new Paginator(totalRecords, offset, max);

    String selectionMessage = createSelectionMessage(paginator, key, tag);

    request.setAttribute("tagList", tagList);
    request.setAttribute("settingList", settingList);
    request.setAttribute("selectionMessage", selectionMessage);
    request.setAttribute("paginator", paginator);

    getServletConfig()
        .getServletContext()
        .getRequestDispatcher("/WEB-INF/views/setup/settings.jsp")
        .forward(request, response);
  }

  private String createSelectionMessage(Paginator paginator, String key, String tag) {
    DecimalFormat formatter = new DecimalFormat("###,###");

    String selectionMessage = "All Settings";

    List<String> filters = new ArrayList<>();

    if (key != null && !key.isBlank()) {
      filters.add("Key \"" + key + "\"");
    }

    if (tag != null && !tag.isBlank()) {
      filters.add("Tag \"" + tag + "\"");
    }

    if (!filters.isEmpty()) {
      selectionMessage = filters.get(0);

      for (int i = 1; i < filters.size(); i++) {
        String filter = filters.get(i);
        selectionMessage += " and " + filter;
      }
    }

    if (paginator.getTotalRecords() < paginator.getMaxPerPage() && paginator.getOffset() == 0) {
      selectionMessage =
          selectionMessage + " {" + formatter.format(paginator.getTotalRecords()) + "}";
    } else {
      selectionMessage =
          selectionMessage
              + " {"
              + formatter.format(paginator.getStartNumber())
              + " - "
              + formatter.format(paginator.getEndNumber())
              + " of "
              + formatter.format(paginator.getTotalRecords())
              + "}";
    }

    return selectionMessage;
  }
}
