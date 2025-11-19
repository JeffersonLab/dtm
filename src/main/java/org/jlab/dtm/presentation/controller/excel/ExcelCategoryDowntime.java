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
import org.jlab.dtm.business.session.CategoryDowntimeFacade;
import org.jlab.dtm.business.session.CcAccHourService;
import org.jlab.dtm.business.session.EventTypeFacade;
import org.jlab.dtm.business.session.ExcelCategoryDowntimeService;
import org.jlab.dtm.persistence.entity.EventType;
import org.jlab.dtm.persistence.model.BeamSummaryTotals;
import org.jlab.dtm.persistence.model.CategoryDowntime;
import org.jlab.dtm.presentation.util.DtmParamConverter;
import org.jlab.dtm.presentation.util.FilterSelectionMessage;
import org.jlab.smoothness.business.exception.UserFriendlyException;
import org.jlab.smoothness.presentation.util.ParamConverter;
import org.jlab.smoothness.presentation.util.ParamUtil;

/**
 * @author ryans
 */
@WebServlet(
    name = "ExcelCategoryDowntime",
    urlPatterns = {"/excel/category-downtime.xlsx"})
public class ExcelCategoryDowntime extends HttpServlet {

  @EJB ExcelCategoryDowntimeService excelService;
  @EJB EventTypeFacade eventTypeFacade;
  @EJB CategoryDowntimeFacade downtimeFacade;
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

    String filters =
        FilterSelectionMessage.getReportMessage(
            start, end, selectedTypeList, null, null, null, null, beamTransport, packed);

    List<CategoryDowntime> downtimeList = null;
    double grandTotalDuration = 0.0;
    double periodDurationHours = 0.0;

    if (start != null && end != null) {
      if (start.after(end)) {
        throw new ServletException("start date cannot be after end date");
      }

      periodDurationHours = (end.getTime() - start.getTime()) / 1000.0 / 60.0 / 60.0;

      downtimeList =
          downtimeFacade.findByPeriodAndType(
              start, end, selectedTypeList, beamTransport, packed, null);

      for (int i = 0; i < downtimeList.size(); i++) {
        CategoryDowntime downtime = downtimeList.get(i);
        grandTotalDuration = grandTotalDuration + downtime.getDuration();
      }
    }

    BeamSummaryTotals beamSummary = null;
    double programHours = 0.0;

    if (selectedTypeList != null
        && selectedTypeList.size() == 1
        && selectedTypeList.contains(EventType.BLOCKED)) {
      beamSummary = accHourService.reportTotals(start, end);

      programHours = (beamSummary.calculateProgramSeconds() / 3600.0);
    }

    response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
    response.setHeader("content-disposition", "attachment;filename=\"category-downtime.xlsx\"");

    excelService.export(
        response.getOutputStream(),
        downtimeList,
        filters.trim(),
        periodDurationHours,
        grandTotalDuration,
        selectedTypeList,
        programHours);
  }
}
