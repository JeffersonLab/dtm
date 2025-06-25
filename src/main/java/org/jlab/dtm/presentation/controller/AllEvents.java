package org.jlab.dtm.presentation.controller;

import java.io.IOException;
import java.math.BigInteger;
import java.text.DecimalFormat;
import java.util.List;
import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jlab.dtm.business.params.AllEventsParams;
import org.jlab.dtm.business.session.AbstractFacade.OrderDirective;
import org.jlab.dtm.business.session.CategoryFacade;
import org.jlab.dtm.business.session.EventFacade;
import org.jlab.dtm.business.session.EventTypeFacade;
import org.jlab.dtm.business.session.ResponsibleGroupFacade;
import org.jlab.dtm.business.session.SystemFacade;
import org.jlab.dtm.persistence.entity.Category;
import org.jlab.dtm.persistence.entity.Event;
import org.jlab.dtm.persistence.entity.EventType;
import org.jlab.dtm.persistence.entity.Workgroup;
import org.jlab.dtm.presentation.params.AllEventsUrlParamHandler;
import org.jlab.smoothness.presentation.util.Paginator;

/**
 * @author ryans
 */
@WebServlet(
    name = "AllEvents",
    urlPatterns = {"/events", "/all-events"})
public class AllEvents extends HttpServlet {

  @EJB EventFacade eventFacade;
  @EJB EventTypeFacade eventTypeFacade;
  @EJB SystemFacade systemFacade;
  @EJB CategoryFacade categoryFacade;
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

    AllEventsUrlParamHandler paramHandler = new AllEventsUrlParamHandler(request, eventTypeFacade);

    AllEventsParams params;

    if (paramHandler.qualified()) {
      params = paramHandler.convert();
      paramHandler.validate(params);
      paramHandler.store(params);
    } else {
      params = paramHandler.materialize();
      paramHandler.redirect(response, params);
      return;
    }

    Category cebafRoot = categoryFacade.findBranch(BigInteger.valueOf(1L));
    Category lerfRoot = categoryFacade.findBranch(BigInteger.valueOf(2L));
    Category otherRoot = categoryFacade.findBranch(BigInteger.valueOf(3L));
    Category cryoRoot = categoryFacade.findBranch(BigInteger.valueOf(4L));
    Category facilitiesRoot = categoryFacade.findBranch(BigInteger.valueOf(5L));
    Category hallRoot = categoryFacade.findBranch(BigInteger.valueOf(465L));
    List<EventType> eventTypeList = eventTypeFacade.filterList(null);
    List<Event> eventList = eventFacade.filterList(params);
    Long totalRecords = eventFacade.countFilterList(params);

    eventFacade.computeRestoreTime(eventList);
    eventFacade.loadClosedBy(eventList);
    eventFacade.loadRepairedBy(eventList);
    eventFacade.loadReviewedBy(eventList);

    Paginator paginator =
        new Paginator(totalRecords.intValue(), params.getOffset(), params.getMax());

    DecimalFormat formatter = new DecimalFormat("###,###");

    String selectionMessage = paramHandler.message(params);

    String paginationMessage =
        " {"
            + paginator.getStartNumber()
            + " - "
            + paginator.getEndNumber()
            + " of "
            + formatter.format(totalRecords)
            + "}";

    selectionMessage = selectionMessage + paginationMessage;

    List<Workgroup> groupList = groupFacade.findAll(new OrderDirective("name"));

    request.setAttribute("start", params.getStart());
    request.setAttribute("end", params.getEnd());
    request.setAttribute("cebafRoot", cebafRoot);
    request.setAttribute("lerfRoot", lerfRoot);
    request.setAttribute("otherRoot", otherRoot);
    request.setAttribute("cryoRoot", cryoRoot);
    request.setAttribute("facilitiesRoot", facilitiesRoot);
    request.setAttribute("hallRoot", hallRoot);
    request.setAttribute("eventList", eventList);
    request.setAttribute("eventTypeList", eventTypeList);
    request.setAttribute("paginator", paginator);
    request.setAttribute("selectionMessage", selectionMessage);
    request.setAttribute("groupList", groupList);

    request.getRequestDispatcher("/WEB-INF/views/all-events.jsp").forward(request, response);
  }
}
