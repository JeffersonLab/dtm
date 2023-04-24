package org.jlab.dtm.presentation.controller.ajax;

import org.jlab.dtm.business.session.SystemFacade;
import org.jlab.dtm.persistence.entity.SystemEntity;
import org.jlab.smoothness.presentation.util.ParamConverter;

import javax.ejb.EJB;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author ryans
 */
@WebServlet(name = "FilterSystemListByCategory", urlPatterns = {"/ajax/filter-system-list-by-category"})
public class FilterSystemListByCategory extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(
            FilterSystemListByCategory.class.getName());
    @EJB
    SystemFacade systemFacade;

    /**
     * Handles the HTTP
     * <code>GET</code> method.
     *
     * @param request  servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException      if an I/O error occurs
     */
    @Override
    @SuppressWarnings("unchecked")
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String errorReason = null;

        List<SystemEntity> systemList = null;

        try {
            BigInteger categoryId = ParamConverter.convertBigInteger(request, "categoryId");
            BigInteger applicationId = ParamConverter.convertBigInteger(request, "applicationId");

            systemList = systemFacade.findWithCategory(categoryId);

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Unable to filter system list", e);
            errorReason = e.getClass().getSimpleName() + ": " + e.getMessage();
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
