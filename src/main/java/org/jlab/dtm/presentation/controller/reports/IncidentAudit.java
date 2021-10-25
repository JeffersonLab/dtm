package org.jlab.dtm.presentation.controller.reports;

import java.io.IOException;
import java.math.BigInteger;
import java.util.List;
import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jlab.dtm.business.session.IncidentAudFacade;
import org.jlab.dtm.persistence.entity.aud.IncidentAud;
import org.jlab.smoothness.presentation.util.Paginator;
import org.jlab.smoothness.presentation.util.ParamConverter;
import org.jlab.smoothness.presentation.util.ParamUtil;

/**
 *
 * @author ryans
 */
@WebServlet(name = "IncidentAudit", urlPatterns = {"/reports/activity-audit/incident-audit"})
public class IncidentAudit extends HttpServlet {

    @EJB
    IncidentAudFacade incidentAudFacade;
    
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

        BigInteger incidentId = ParamConverter.convertBigInteger(request, "incidentId");
        BigInteger revisionId = ParamConverter.convertBigInteger(request, "revisionId");
        
        int offset = ParamUtil.convertAndValidateNonNegativeInt(request, "offset", 0);
        int maxPerPage = 5;

        List<IncidentAud> incidentList = null;
        Long totalRecords = 0L;
        
        if(incidentId != null) {
            incidentList = incidentAudFacade.filterList(incidentId, revisionId, offset, maxPerPage);
            totalRecords = incidentAudFacade.countFilterList(incidentId, revisionId);
            
            incidentAudFacade.loadStaff(incidentList);
        }
        
        Paginator paginator = new Paginator(totalRecords.intValue(), offset, maxPerPage);

        
        request.setAttribute("incidentList", incidentList);
        request.setAttribute("paginator", paginator);        
        
        request.getRequestDispatcher("/WEB-INF/views/reports/activity-audit/incident-audit.jsp").forward(request, response);
    }
}
