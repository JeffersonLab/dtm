package org.jlab.dtm.presentation.controller.excel;

import jakarta.ejb.EJB;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigInteger;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.jlab.dtm.business.params.IncidentDowntimeReportParams;
import org.jlab.dtm.business.session.*;
import org.jlab.dtm.business.session.IncidentReportService.IncidentSummary;
import org.jlab.dtm.persistence.entity.EventType;
import org.jlab.dtm.persistence.entity.SystemEntity;
import org.jlab.dtm.persistence.entity.Workgroup;
import org.jlab.dtm.presentation.util.DtmParamConverter;
import org.jlab.dtm.presentation.util.FilterSelectionMessage;
import org.jlab.smoothness.business.exception.UserFriendlyException;
import org.jlab.smoothness.presentation.util.ParamConverter;

/**
 * @author ryans
 */
@WebServlet(
    name = "ExcelIncidentList",
    urlPatterns = {"/excel/incident-list.xlsx"})
public class ExcelIncidentList extends HttpServlet {

  @EJB IncidentReportService incidentReportService;
  @EJB ExcelIncidentListService excelService;
  @EJB EventTypeFacade eventTypeFacade;
  @EJB SystemFacade systemFacade;
  @EJB ResponsibleGroupFacade groupFacade;

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

    BigInteger[] typeIdArray = ParamConverter.convertBigIntegerArray(request, "type");

    List<EventType> selectedTypeList = new ArrayList<>();

    if (typeIdArray != null) {
      for (BigInteger id : typeIdArray) {
        if (id != null) {
          EventType type = eventTypeFacade.find(id);
          selectedTypeList.add(type);
        }
      }
    }

    BigInteger systemId = ParamConverter.convertBigInteger(request, "system");

    SystemEntity selectedSystem = null;

    if (systemId != null) {
      selectedSystem = systemFacade.find(systemId);
    }

    String component = request.getParameter("component");

    Boolean beamTransport = null;
    try {
      beamTransport = ParamConverter.convertYNBoolean(request, "transport");
    } catch (UserFriendlyException e) {
      throw new ServletException("transport must be Y or N", e);
    }

    String filters =
        FilterSelectionMessage.getReportMessage(
            start,
            end,
            selectedTypeList,
            selectedSystem,
            null,
            null,
            component,
            beamTransport,
            false);

    IncidentDowntimeReportParams params = new IncidentDowntimeReportParams();
    params.setStart(start);
    params.setEnd(end);
    params.setEventTypeIdArray(typeIdArray);
    params.setSystemId(systemId);
    params.setComponent(component);
    params.setBeamTransport(beamTransport);

    long totalRecords = incidentReportService.countFilterList(params);
    params.setSortByDuration(true);
    params.setMax((int) totalRecords);
    List<IncidentSummary> incidentList = incidentReportService.filterList(params);

    for (IncidentSummary incident : incidentList) {
      List<Workgroup> repairedByList = groupFacade.findRepairedBy(incident.getIncidentId());
      incident.setRepairedByList(repairedByList);
    }

    response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
    response.setHeader("content-disposition", "attachment;filename=\"incident-list.xlsx\"");

    excelService.export(response.getOutputStream(), incidentList, filters.trim());
  }
}
