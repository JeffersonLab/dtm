package org.jlab.dtm.presentation.controller.data;

import org.jlab.dtm.business.params.IncidentParams;
import org.jlab.dtm.business.params.TrendReportParams;
import org.jlab.dtm.business.session.CategoryFacade;
import org.jlab.dtm.business.session.IncidentFacade;
import org.jlab.dtm.business.session.TrendReportFacade;
import org.jlab.dtm.persistence.entity.Category;
import org.jlab.dtm.persistence.entity.Incident;
import org.jlab.dtm.persistence.model.TrendRecord;
import org.jlab.dtm.presentation.params.TrendReportUrlParamHandler;
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
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author ryans
 */
@WebServlet(name = "TrendData", urlPatterns = {"/data/trend"})
public class Trend extends HttpServlet {

    private static final Logger logger = Logger.getLogger(
            Trend.class.getName());
    @EJB
    TrendReportFacade trendReportFacade;
    @EJB
    CategoryFacade categoryFacade;

    /**
     * Handles the HTTP
     * <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String errorReason = null;

        Calendar c = Calendar.getInstance();
        Date now = new Date();
        c.set(Calendar.MILLISECOND, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.HOUR_OF_DAY, 7);
        Date today = c.getTime();
        c.add(Calendar.DATE, -7);
        Date sevenDaysAgo = c.getTime();

        TrendReportUrlParamHandler paramHandler
                = new TrendReportUrlParamHandler(request, today, sevenDaysAgo);

        TrendReportParams params = paramHandler.convert();
        paramHandler.validate(params);

        List<TrendRecord> recordList = null;

        try {
            recordList = trendReportFacade.find(params);
        }  catch (SQLException e) {
            errorReason = e.getClass().getSimpleName() + ": " + e.getMessage();
        }

        //List<Category> alphaCatList = categoryFacade.findAlphaCategoryList();

        response.setContentType("application/json");

        PrintWriter pw = response.getWriter();

        JsonObjectBuilder builder = Json.createObjectBuilder();

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");

        if (errorReason == null) {
            JsonArrayBuilder arrBld = Json.createArrayBuilder();
            if (recordList != null) {
                for (TrendRecord record : recordList) {
                    JsonObjectBuilder objBld = Json.createObjectBuilder();
                    objBld.add("bin", formatter.format(record.getBin()));
                    objBld.add("overall-downtime", record.getAccDownHours());
                    objBld.add("overall-uptime", record.getAccUptimeHours());
                    objBld.add("overall-availability", record.getAccAvailability());
                    objBld.add("trip-downtime", record.getTripHours());
                    objBld.add("trip-count", record.getTripCount());
                    objBld.add("trip-mttr", record.getTripMttrHours());
                    objBld.add("trip-mtbf", record.getMtbtHours());
                    objBld.add("trip-availability", record.getTripAvailability());
                    objBld.add("event-downtime", record.getEventHours());
                    objBld.add("event-count", record.getEventCount());
                    objBld.add("event-mttr", record.getEventMttrHours());
                    objBld.add("event-mtbf", record.getEventMtbfHours());
                    objBld.add("event-availability", record.getEventAvailability());

                    arrBld.add(objBld.build());
                }
            }
            builder.add("stat", "ok");
            builder.add("data", arrBld.build());
        } else {
            builder.add("stat", "fail");
            builder.add("error", errorReason);
        }

        String jsonStr = builder.build().toString();

        pw.write(jsonStr);

        pw.flush();

        boolean error = pw.checkError();

        if (error) {
            logger.log(Level.SEVERE, "PrintWriter Error");
        }
    }
}
