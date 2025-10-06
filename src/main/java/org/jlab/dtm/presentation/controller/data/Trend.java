package org.jlab.dtm.presentation.controller.data;

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
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jlab.dtm.business.params.TrendReportParams;
import org.jlab.dtm.business.session.TrendReportFacade;
import org.jlab.dtm.persistence.model.TrendRecord;
import org.jlab.dtm.presentation.params.TrendReportUrlParamHandler;
import org.jlab.smoothness.business.exception.UserFriendlyException;

/**
 * @author ryans
 */
@WebServlet(
    name = "TrendData",
    urlPatterns = {"/data/trend"})
public class Trend extends HttpServlet {

  private static final Logger logger = Logger.getLogger(Trend.class.getName());
  @EJB TrendReportFacade trendReportFacade;

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

    TrendReportUrlParamHandler paramHandler =
        new TrendReportUrlParamHandler(request, today, sevenDaysAgo);

    TrendReportParams params = paramHandler.convert();
    paramHandler.validate(params);

    List<TrendRecord> recordList = null;

    try {
      recordList = trendReportFacade.find(params);
    } catch (UserFriendlyException e) {
      errorReason = e.getUserMessage();
    } catch (SQLException e) {
      errorReason = "Internal Error";
    }

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
