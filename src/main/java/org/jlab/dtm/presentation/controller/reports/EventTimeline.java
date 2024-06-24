package org.jlab.dtm.presentation.controller.reports;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.jlab.dtm.business.session.AbstractFacade.OrderDirective;
import org.jlab.dtm.business.session.EventFacade;
import org.jlab.dtm.business.session.EventTypeFacade;
import org.jlab.dtm.persistence.entity.Event;
import org.jlab.dtm.persistence.entity.EventType;
import org.jlab.dtm.presentation.util.DtmParamConverter;
import org.jlab.smoothness.business.util.TimeUtil;

/**
 * @author ryans
 */
@WebServlet(
    name = "EventTimeline",
    urlPatterns = {"/reports/event-timeline"})
public class EventTimeline extends HttpServlet {

  @EJB EventFacade eventFacade;
  @EJB EventTypeFacade eventTypeFacade;

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

    Calendar c = Calendar.getInstance();
    Date now = c.getTime();
    c.set(Calendar.MILLISECOND, 0);
    c.set(Calendar.SECOND, 0);
    c.set(Calendar.MINUTE, 0);
    c.set(Calendar.HOUR_OF_DAY, 7);
    Date today = c.getTime();
    c.add(Calendar.DATE, -7);
    Date sevenDaysAgo = c.getTime();

    /* Note: We use a 'SECURE' cookie so session changes every request unless over SSL/TLS */
    HttpSession session = request.getSession(true);
    Date sessionStart = (Date) session.getAttribute("start");
    Date sessionEnd = (Date) session.getAttribute("end");

    /* Redirect if using defaults to maintain bookmarkability (html-to-image/pdf for example) */
    boolean needRedirect = false;

    if (start == null) {
      needRedirect = true;
      if (sessionStart != null) {
        start = sessionStart;
      } else {
        start = sevenDaysAgo;
      }
    }

    if (end == null) {
      needRedirect = true;
      if (sessionEnd != null) {
        end = sessionEnd;
      } else {
        end = today;
      }
    }

    if (needRedirect) {
      response.sendRedirect(response.encodeRedirectURL(this.getCurrentUrl(request, start, end)));
      return;
    }

    session.setAttribute("start", start);
    session.setAttribute("end", end);

    List<EventType> eventTypeList = eventTypeFacade.findAll(new OrderDirective("weight"));

    Long duration = null;
    List<Event> eventList = null;
    long eventCount = 0;

    if (start != null && end != null) {
      if (start.after(end)) {
        throw new ServletException("start date cannot be after end date");
      }

      duration = end.getTime() - start.getTime();

      eventList = eventFacade.findEventListWithIncidents(start, end, null);
      eventCount = eventList.size();
    }

    DecimalFormat formatter = new DecimalFormat("###,###");

    String selectionMessage = TimeUtil.formatSmartRangeSeparateTime(start, end);

    request.setAttribute("selectionMessage", selectionMessage);
    request.setAttribute("today", today);
    request.setAttribute("sevenDaysAgo", sevenDaysAgo);
    request.setAttribute("eventList", eventList);
    request.setAttribute("eventTypeList", eventTypeList);
    request.setAttribute("start", start);
    request.setAttribute("end", end);
    request.setAttribute("timelineDuration", duration);

    request
        .getRequestDispatcher("/WEB-INF/views/reports/event-timeline.jsp")
        .forward(request, response);
  }

  private String getCurrentUrl(HttpServletRequest request, Date start, Date end) {
    SimpleDateFormat dateFormat = new SimpleDateFormat(TimeUtil.getFriendlyDateTimePattern());

    return request.getContextPath()
        + "/reports/event-timeline?start="
        + URLEncoder.encode(dateFormat.format(start), StandardCharsets.UTF_8)
        + "&end="
        + URLEncoder.encode(dateFormat.format(end), StandardCharsets.UTF_8);
  }
}
