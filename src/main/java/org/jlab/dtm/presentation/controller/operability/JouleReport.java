package org.jlab.dtm.presentation.controller.operability;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jlab.dtm.business.params.JouleReportParams;
import org.jlab.dtm.business.session.JouleReportFacade;
import org.jlab.dtm.business.session.JouleReportFacade.JouleRecord;
import org.jlab.dtm.business.util.DtmTimeUtil;
import org.jlab.dtm.persistence.enumeration.BinSize;
import org.jlab.dtm.presentation.params.JouleReportUrlParamHandler;
import org.jlab.smoothness.business.util.TimeUtil;

/**
 *
 * @author ryans
 */
@WebServlet(name = "JouleReport", urlPatterns = {"/operability/joule"})
public class JouleReport extends HttpServlet {

    @EJB
    JouleReportFacade jouleFacade;
    
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

        JouleReportUrlParamHandler paramHandler
                = new JouleReportUrlParamHandler(request, today, sevenDaysAgo);

        JouleReportParams params;

        if (paramHandler.qualified()) {
            params = paramHandler.convert();
            paramHandler.validate(params);
            paramHandler.store(params);
        } else {
            params = paramHandler.materialize();
            paramHandler.redirect(response, params);
            return;
        }
        
        List<JouleRecord> recordList = null;
        
        if (params.getStart() != null && params.getEnd()
                != null) {

                try {
                    recordList = jouleFacade.find(params);
                }catch (InterruptedException e) {
                    throw new ServletException("Unable to query for PAC Schedule");
                }
        }

        String selectionMessage = TimeUtil.formatSmartRangeSeparateTime(params.getStart(), params.getEnd());

        Date endInclusive = DtmTimeUtil.getEndInclusive(params.getEnd(), params.getSize());

        request.setAttribute("start", params.getStart());
        request.setAttribute("end", params.getEnd());
        request.setAttribute("endInclusive", endInclusive);
        request.setAttribute("selectionMessage", selectionMessage);
        request.setAttribute("today", today);
        request.setAttribute("sevenDaysAgo", sevenDaysAgo);
        request.setAttribute("recordList", recordList);

        request.getRequestDispatcher(
                "/WEB-INF/views/operability/joule.jsp").forward(request, response);
    }
}
