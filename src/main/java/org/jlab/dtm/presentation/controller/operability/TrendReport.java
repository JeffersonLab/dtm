package org.jlab.dtm.presentation.controller.operability;

import java.io.IOException;
import java.math.BigInteger;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.jlab.dtm.business.params.JouleReportParams;
import org.jlab.dtm.business.params.TrendReportParams;
import org.jlab.dtm.business.session.CategoryDowntimeFacade;
import org.jlab.dtm.business.session.CategoryFacade;
import org.jlab.dtm.business.session.CategoryMonthlyGoalFacade;
import org.jlab.dtm.business.session.EventDowntimeFacade;
import org.jlab.dtm.business.session.EventTypeFacade;
import org.jlab.dtm.business.session.MonthlyNoteFacade;
import org.jlab.dtm.business.session.TrendReportFacade;
import org.jlab.dtm.business.session.OpAccHourService;
import org.jlab.dtm.business.session.ResponsibleGroupFacade;
import org.jlab.dtm.business.util.DtmTimeUtil;
import org.jlab.dtm.persistence.entity.Category;
import org.jlab.dtm.persistence.entity.EventType;
import org.jlab.dtm.persistence.model.TrendRecord;
import org.jlab.dtm.presentation.params.JouleReportUrlParamHandler;
import org.jlab.dtm.presentation.params.TrendReportUrlParamHandler;
import org.jlab.dtm.presentation.util.DtmParamConverter;
import org.jlab.smoothness.business.util.DateIterator;
import org.jlab.smoothness.business.util.TimeUtil;
import org.jlab.smoothness.presentation.util.ServletUtil;

/**
 *
 * @author ryans
 */
@WebServlet(name = "TrendReport", urlPatterns = {"/operability/trend"})
public class TrendReport extends HttpServlet {

    @EJB
    EventTypeFacade eventTypeFacade;
    @EJB
    ResponsibleGroupFacade groupFacade;
    @EJB
    CategoryFacade categoryFacade;
    @EJB
    CategoryDowntimeFacade categoryDowntimeFacade;
    @EJB
    OpAccHourService accHourService;
    @EJB
    EventDowntimeFacade eventDowntimeFacade;
    @EJB
    MonthlyNoteFacade noteFacade;
    @EJB
    CategoryMonthlyGoalFacade goalFacade;
    @EJB
    TrendReportFacade trendReportFacade;

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

        TrendReportParams params;

        if (paramHandler.qualified()) {
            params = paramHandler.convert();
            paramHandler.validate(params);
            paramHandler.store(params);
        } else {
            params = paramHandler.materialize();
            paramHandler.redirect(response, params);
            return;
        }

        List<TrendRecord> recordList = null;

        params.setIncludeCategories(true);

        try {
            recordList = trendReportFacade.find(params);
        }  catch (SQLException e) {
            throw new ServletException("Unable to load data", e);
        }

        List<Category> alphaCatList = categoryFacade.findAlphaCategoryList();

        String selectionMessage = paramHandler.message(params);

        Date endInclusive = DtmTimeUtil.getEndInclusive(params.getEnd(), params.getSize());

        request.setAttribute("start", params.getStart());
        request.setAttribute("end", params.getEnd());
        request.setAttribute("endInclusive", endInclusive);
        request.setAttribute("alphaCatList", alphaCatList);
        request.setAttribute("recordList", recordList);
        request.setAttribute("selectionMessage", selectionMessage);

        request.getRequestDispatcher("/WEB-INF/views/operability/trend.jsp").forward(
                request,
                response);
    }
}
