package org.jlab.dtm.presentation.controller.excel;

import java.io.IOException;
import java.math.BigInteger;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jlab.dtm.business.session.EventDowntimeFacade;
import org.jlab.dtm.business.session.EventTypeFacade;
import org.jlab.dtm.business.session.ExcelEventListService;
import org.jlab.dtm.persistence.entity.EventType;
import org.jlab.dtm.persistence.model.EventDowntime;
import org.jlab.dtm.presentation.util.DtmParamConverter;
import org.jlab.dtm.presentation.util.FilterSelectionMessage;
import org.jlab.smoothness.business.exception.UserFriendlyException;
import org.jlab.smoothness.presentation.util.ParamConverter;
import org.jlab.smoothness.presentation.util.ParamUtil;

/**
 * @author ryans
 */
@WebServlet(
    name = "ExcelEventList",
    urlPatterns = {"/excel/event-list.xlsx"})
public class ExcelEventList extends HttpServlet {

  @EJB ExcelEventListService excelService;
  @EJB EventTypeFacade eventTypeFacade;
  @EJB EventDowntimeFacade downtimeFacade;

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

    Date start = null;
    Date end = null;

    try {
      start = DtmParamConverter.convertJLabDateTime(request, "start");
      end = DtmParamConverter.convertJLabDateTime(request, "end");
    } catch (ParseException e) {
      throw new ServletException("Unable to parse date", e);
    }

    if (start == null) {
      throw new ServletException("Start date must not be null");
    }

    if (end == null) {
      throw new ServletException("End date must not be null");
    }

    BigInteger eventTypeId = ParamConverter.convertBigInteger(request, "type");

    if (eventTypeId == null) {
      throw new ServletException("eventTypeId must not be null");
    }

    EventType type = eventTypeFacade.find(eventTypeId);

    if (type == null) {
      throw new ServletException("Could not find event type with ID: " + eventTypeId);
    }

    Boolean beamTransport = null;
    try {
      beamTransport = ParamUtil.convertAndValidateYNBoolean(request, "transport");
    } catch (UserFriendlyException e) {
      throw new ServletException("transport must be Y or N", e);
    }

    String filters =
        FilterSelectionMessage.getReportMessage(
            start, end, type, null, null, null, null, beamTransport, false);

    // Note: Bounded means the selected time period boundaries are used as min and max time down and
    // time up for events which span the period boundary

    List<EventDowntime> eventList =
        downtimeFacade.findByPeriodAndTypeSortByDuration(start, end, type, beamTransport, null);

    response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
    response.setHeader("content-disposition", "attachment;filename=\"incident-list.xlsx\"");

    excelService.export(response.getOutputStream(), eventList, filters.trim());
  }
}
