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
import org.jlab.dtm.business.session.*;
import org.jlab.dtm.persistence.entity.Category;
import org.jlab.dtm.persistence.entity.EventType;
import org.jlab.dtm.persistence.model.BeamSummaryTotals;
import org.jlab.dtm.persistence.model.SystemDowntime;
import org.jlab.dtm.presentation.util.DtmParamConverter;
import org.jlab.dtm.presentation.util.FilterSelectionMessage;
import org.jlab.smoothness.business.exception.UserFriendlyException;
import org.jlab.smoothness.presentation.util.ParamConverter;
import org.jlab.smoothness.presentation.util.ParamUtil;

/**
 * @author ryans
 */
@WebServlet(
    name = "ExcelSystemDowntime",
    urlPatterns = {"/excel/system-downtime.xlsx"})
public class ExcelSystemDowntime extends HttpServlet {

  @EJB ExcelSystemDowntimeService excelService;
  @EJB EventTypeFacade eventTypeFacade;
  @EJB SystemDowntimeFacade downtimeFacade;
  @EJB CategoryFacade categoryFacade;
  @EJB CcAccHourService accHourService;

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

    EventType type = null;

    if (eventTypeId != null) {
      type = eventTypeFacade.find(eventTypeId);
    }

    BigInteger categoryId = ParamConverter.convertBigInteger(request, "category");

    Boolean beamTransport = null;
    try {
      beamTransport = ParamConverter.convertYNBoolean(request, "transport");
    } catch (UserFriendlyException e) {
      throw new ServletException("transport must be Y or N", e);
    }

    boolean packed = false;
    try {
      packed = ParamUtil.convertAndValidateYNBoolean(request, "packed", true);
    } catch (UserFriendlyException e) {
      throw new ServletException("packed must be Y or N", e);
    }

    Category selectedCategory = null;

    if (categoryId != null) {
      selectedCategory = categoryFacade.find(categoryId);
    }

    String filters =
        FilterSelectionMessage.getReportMessage(
            start, end, type, null, selectedCategory, null, null, beamTransport, packed);

    List<SystemDowntime> downtimeList = null;
    double grandTotalDuration = 0.0;
    double periodDurationHours = 0.0;

    if (start != null && end != null) {
      if (start.after(end)) {
        throw new ServletException("start date cannot be after end date");
      }

      periodDurationHours = (end.getTime() - start.getTime()) / 1000.0 / 60.0 / 60.0;

      downtimeList =
          downtimeFacade.findByPeriodAndType(start, end, type, beamTransport, categoryId, packed);

      for (int i = 0; i < downtimeList.size(); i++) {
        SystemDowntime downtime = downtimeList.get(i);
        grandTotalDuration = grandTotalDuration + downtime.getDuration();
      }
    }

    BeamSummaryTotals beamSummary = null;
    double programHours = 0.0;

    if (EventType.BLOCKED.equals(type)) {
      beamSummary = accHourService.reportTotals(start, end);

      programHours = (beamSummary.calculateProgramSeconds() / 3600.0);
    }

    response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
    response.setHeader("content-disposition", "attachment;filename=\"system-downtime.xlsx\"");

    excelService.export(
        response.getOutputStream(),
        downtimeList,
        filters.trim(),
        periodDurationHours,
        grandTotalDuration,
        type,
        programHours);
  }
}
