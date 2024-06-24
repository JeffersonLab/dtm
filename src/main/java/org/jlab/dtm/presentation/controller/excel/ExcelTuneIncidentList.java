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
import org.jlab.dtm.business.params.IncidentDowntimeReportParams;
import org.jlab.dtm.business.session.EventTypeFacade;
import org.jlab.dtm.business.session.ExcelIncidentListService;
import org.jlab.dtm.business.session.IncidentReportService;
import org.jlab.dtm.business.session.IncidentReportService.IncidentSummary;
import org.jlab.dtm.business.session.SystemFacade;
import org.jlab.dtm.persistence.entity.EventType;
import org.jlab.dtm.presentation.util.DtmParamConverter;
import org.jlab.dtm.presentation.util.FilterSelectionMessage;
import org.jlab.smoothness.presentation.util.ParamConverter;

/**
 * @author ryans
 */
@WebServlet(
    name = "ExcelTuneIncidentList",
    urlPatterns = {"/excel/tune-incident-list.xlsx"})
public class ExcelTuneIncidentList extends HttpServlet {

  @EJB IncidentReportService incidentReportService;
  @EJB ExcelIncidentListService excelService;
  @EJB EventTypeFacade eventTypeFacade;
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

    Date start = null;
    Date end = null;

    try {
      start = DtmParamConverter.convertJLabDateTime(request, "start");
      end = DtmParamConverter.convertJLabDateTime(request, "end");
    } catch (ParseException e) {
      throw new ServletException("Unable to parse date", e);
    }

    BigInteger eventTypeId = ParamConverter.convertBigInteger(request, "type");

    EventType type = null;

    if (eventTypeId != null) {
      type = eventTypeFacade.find(eventTypeId);
    }

    String component = request.getParameter("component");

    String filters =
        FilterSelectionMessage.getReportMessage(
            start, end, type, null, null, null, component, true, false);

    IncidentDowntimeReportParams params = new IncidentDowntimeReportParams();
    params.setStart(start);
    params.setEnd(end);
    params.setEventTypeId(eventTypeId);
    params.setComponent(component);

    long totalRecords = incidentReportService.countFilterList(params);
    params.setSortByDuration(true);
    params.setMax((int) totalRecords);
    List<IncidentSummary> incidentList = incidentReportService.filterList(params);

    response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
    response.setHeader("content-disposition", "attachment;filename=\"tune-incident-list.xlsx\"");

    excelService.export(response.getOutputStream(), incidentList, filters.trim());
  }
}
