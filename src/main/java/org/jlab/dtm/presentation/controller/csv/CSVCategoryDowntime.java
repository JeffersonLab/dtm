package org.jlab.dtm.presentation.controller.csv;

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
import org.jlab.dtm.business.session.CategoryDowntimeFacade;
import org.jlab.dtm.business.session.EventTypeFacade;
import org.jlab.dtm.business.session.ExcelCategoryDowntimeService;
import org.jlab.dtm.persistence.entity.EventType;
import org.jlab.dtm.persistence.model.CategoryDowntime;
import org.jlab.dtm.presentation.util.DtmParamConverter;
import org.jlab.dtm.presentation.util.FilterSelectionMessage;
import org.jlab.smoothness.presentation.util.ParamConverter;
import org.jlab.smoothness.presentation.util.ParamUtil;

/**
 *
 * @author ryans
 */
@WebServlet(name = "CSVCategoryDowntime", urlPatterns = {"/csv/category-downtime.csv"})
public class CSVCategoryDowntime extends HttpServlet {

    @EJB
    ExcelCategoryDowntimeService excelService;
    @EJB
    EventTypeFacade eventTypeFacade;
    @EJB
    CategoryDowntimeFacade downtimeFacade;
    
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

        Boolean beamTransport = ParamUtil.convertAndValidateYNBoolean(request, "transport");

        boolean packed = ParamUtil.convertAndValidateYNBoolean(request, "transport", true);
        
        String filters = FilterSelectionMessage.getReportMessage(start, end, type, null, null, null, null, beamTransport, packed);

        List<CategoryDowntime> downtimeList = null;
        double grandTotalDuration = 0.0;
        double periodDurationHours = 0.0;

        if (start != null && end
                != null) {
            if (start.after(end)) {
                throw new ServletException("start date cannot be after end date");
            }

            periodDurationHours = (end.getTime() - start.getTime()) / 1000.0 / 60.0 / 60.0;

            downtimeList = downtimeFacade.findByPeriodAndType(start, end, type, beamTransport, packed, null);

            for (int i = 0; i < downtimeList.size(); i++) {
                CategoryDowntime downtime = downtimeList.get(i);
                grandTotalDuration = grandTotalDuration + downtime.getDuration();
            }
        }

        response.setContentType("text/csv");
        response.setHeader("content-disposition", "attachment;filename=\"category-downtime.csv\"");

        excelService.exportAsCsv(response.getOutputStream(), downtimeList, filters.trim(), periodDurationHours, grandTotalDuration);
    }
}
