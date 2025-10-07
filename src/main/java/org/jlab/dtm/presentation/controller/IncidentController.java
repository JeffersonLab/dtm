package org.jlab.dtm.presentation.controller;

import jakarta.ejb.EJB;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigInteger;
import java.util.List;
import org.jlab.dtm.business.session.*;
import org.jlab.dtm.persistence.entity.Incident;
import org.jlab.dtm.persistence.entity.IncidentReview;
import org.jlab.dtm.persistence.entity.Repair;
import org.jlab.dtm.persistence.entity.Workgroup;
import org.jlab.smoothness.presentation.util.ParamConverter;

/**
 * @author ryans
 */
@WebServlet(
    name = "IncidentController",
    urlPatterns = {"/incident"})
public class IncidentController extends HttpServlet {

  @EJB IncidentFacade incidentFacade;
  @EJB RepairFacade repairFacade;
  @EJB IncidentReviewFacade reviewFacade;
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
    BigInteger incidentId = ParamConverter.convertBigInteger(request, "incidentId");

    List<Workgroup> groupList = groupFacade.findAll(new AbstractFacade.OrderDirective("name"));

    Incident incident = incidentFacade.findWithExtras(incidentId);

    List<Repair> repairList = repairFacade.findByIncident(incident.getIncidentId());
    incident.setRepairedByList(repairList);

    List<IncidentReview> reviewList = reviewFacade.findByIncident(incident.getIncidentId());
    incident.setIncidentReviewList(reviewList);

    request.setAttribute("groupList", groupList);
    request.setAttribute("incident", incident);

    request.getRequestDispatcher("/WEB-INF/views/incident.jsp").forward(request, response);
  }
}
