package org.jlab.dtm.business.session;

import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.security.PermitAll;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;
import org.jlab.dtm.business.params.AllEventsParams;
import org.jlab.dtm.persistence.entity.Event;
import org.jlab.dtm.persistence.entity.EventType;
import org.jlab.dtm.persistence.entity.Incident;
import org.jlab.dtm.persistence.entity.IncidentReview;
import org.jlab.dtm.persistence.entity.Repair;
import org.jlab.dtm.persistence.entity.aud.EventAud;
import org.jlab.dtm.persistence.entity.view.EventTimeDown;
import org.jlab.dtm.persistence.enumeration.IncidentEditType;
import org.jlab.dtm.persistence.model.Period;
import org.jlab.smoothness.business.exception.InternalException;
import org.jlab.smoothness.business.exception.UserFriendlyException;
import org.jlab.smoothness.business.exception.WebApplicationException;
import org.jlab.smoothness.business.util.IOUtil;
import org.jlab.smoothness.persistence.util.JPAUtil;
import org.jlab.smoothness.presentation.filter.AuditContext;

/**
 * @author ryans
 */
@Stateless
public class EventFacade extends AbstractFacade<Event> {

  private static final Logger logger = Logger.getLogger(EventFacade.class.getName());

  @PersistenceContext(unitName = "webappPU")
  private EntityManager em;

  @EJB EventTypeFacade eventTypeFacade;
  @EJB IncidentFacade incidentFacade;
  @EJB EventAudFacade eventAudFacade;
  @EJB ApplicationRevisionInfoFacade revisionFacade;
  @EJB LogbookFacade logbookFacade;
  @EJB EscalationService escalationService;
  @EJB RepairFacade repairFacade;
  @EJB IncidentReviewFacade reviewFacade;

  @Override
  protected EntityManager getEntityManager() {
    return em;
  }

  public EventFacade() {
    super(Event.class);
  }

  @PermitAll
  public List<Event> findOpenEventList() {
    CriteriaBuilder cb = em.getCriteriaBuilder();
    CriteriaQuery<Event> cq = cb.createQuery(Event.class);
    Root<Event> root = cq.from(Event.class);
    cq.select(root);
    cq.where(cb.isNull(root.<Date>get("timeUp")));
    return em.createQuery(cq).getResultList();
  }

  @PermitAll
  public List<Event> findOpenEventListWithIncidents() {
    List<Event> eventList = findOpenEventList();

    for (Event e : eventList) {
      JPAUtil.initialize(e.getIncidentList());
      loadCategories(e);
    }

    return eventList;
  }

  @PermitAll
  public List<Event> findEventListWithIncidents(Date start, Date end, BigInteger eventTypeId) {
    String selectFrom = "select e from Event e ";

    List<String> whereList = new ArrayList<String>();

    String w;

    w = "e.eventTimeDown.timeDown < :end and nvl(e.timeUp, sysdate) >= :start";
    whereList.add(w);

    if (eventTypeId != null) {
      w = "e.eventType.eventTypeId = " + eventTypeId;
      whereList.add(w);
    }

    String where = "";

    if (!whereList.isEmpty()) {
      where = "where ";
      for (String wh : whereList) {
        where = where + wh + " and ";
      }

      where = where.substring(0, where.length() - 5);
    }

    String sql = selectFrom + " " + where;
    TypedQuery<Event> q = em.createQuery(sql, Event.class);

    q.setParameter("start", start);
    q.setParameter("end", end);

    List<Event> eventList = q.getResultList();

    if (eventList != null) {
      for (Event e : eventList) {
        JPAUtil.initialize(e.getIncidentList());
      }
    }

    return eventList;
  }

  @PermitAll
  public List<Event> findEventListWithIncidents() {
    List<Event> eventList = findAll();

    for (Event e : eventList) {
      JPAUtil.initialize(e.getIncidentList());
    }

    return eventList;
  }

  @PermitAll
  public Event findOpenEvent(BigInteger eventTypeId) {
    CriteriaBuilder cb = em.getCriteriaBuilder();
    CriteriaQuery<Event> cq = cb.createQuery(Event.class);
    Root<Event> root = cq.from(Event.class);
    cq.select(root);
    cq.where(
        cb.and(cb.isNull(root.<Date>get("timeUp")), cb.equal(root.get("eventType"), eventTypeId)));
    List<Event> eventList = em.createQuery(cq).getResultList();
    Event event = null;
    if (eventList != null && !eventList.isEmpty()) {
      event = eventList.get(0);
    }
    return event;
  }

  @PermitAll
  public void editEvent(BigInteger eventId, Date timeUp, String title, BigInteger eventTypeId)
      throws WebApplicationException {
    logger.log(
        Level.FINEST,
        "editEvent - eventId: {0}, timeUp: {1}, title: {2}, eventTypeId: {3}",
        new Object[] {eventId, timeUp, title, eventTypeId});
    checkAuthenticated();

    if (eventId == null) {
      throw new InternalException("Parameter eventId must not be null");
    }

    if (eventTypeId == null) {
      throw new InternalException("Parameter eventTypeId must not be null");
    }

    if (title == null || title.isEmpty()) {
      throw new InternalException("Parameter title must not be null");
    }

    Event event = find(eventId);

    if (event == null) {
      throw new InternalException("Event with ID " + eventId + " not found");
    }

    checkCanEditIncidentOrEvent(event.containsAtLeastOneReview(), event);

    EventType type = eventTypeFacade.find(eventTypeId);

    if (type == null) {
      throw new InternalException("EventType with ID " + eventTypeId + " not found");
    }

    event.setTitle(title);
    event.setEventType(type);

    List<Incident> closedIncidents = new ArrayList<Incident>();

    if (timeUp == null) { // Reopen or keep open
      Event openEvent = findOpenEvent(event.getEventType().getEventTypeId());

      if (openEvent != null && !event.equals(openEvent)) {
        throw new UserFriendlyException(
            "An event of type " + event.getEventType().getName() + " is already open");
      }

      // if(event.getTimeUp() != null) { // The event is being re-opened
      // }
      validateEventTimeUp(event, new Date());

      // Clear reviewed by field of all incidents as not supposed to be able to review incidents in
      // an open event
      for (Incident incident : event.getIncidentList()) {
        if (incident.getReviewedUsername() != null) {
          incident.setReviewedUsername(null);
        }
      }

      event.setTimeUp(timeUp);

      escalationService.resetEscalation(event);
    } else { // move event along timeline or close
      validateEventTimeUp(event, timeUp);

      // Close any incidents BEFORE attempting to close event
      for (Incident incident : event.getIncidentList()) {
        if (incident.getTimeUp() == null) {
          incident.setTimeUp(timeUp);
          closedIncidents.add(incident);
        }
      }

      em.flush(); // Incidents MUST be closed before you can close an event

      boolean isClose = event.getTimeUp() == null;
      /*we're moving it along timeline if already a timeUp*/

      event.setTimeUp(timeUp);

      if (isClose) {
        escalationService.cancelEscalationTimer(event);
      }

      AuditContext auditCtx = AuditContext.getCurrentInstance();

      boolean reviewer = "REVIEWER".equals(auditCtx.getExtra("EffectiveRole"));

      if (!reviewer) {
        for (Incident incident : closedIncidents) {
          logbookFacade.silentlyCreateIncidentELog(incident, IncidentEditType.CLOSED);
        }
      }
    }
  }

  @PermitAll
  public void validateRange(Event event, Date timeDown, Date timeUp) {
    // incidentFacade.findByNotTypeAndDateRange(event.getEventType(), timeUp);
  }

  @PermitAll
  public void validateEventTimeUp(Event event, Date timeUp) throws WebApplicationException {
    Date latestIncidentTimeUp = computeLatestIncidentTimeUp(event.getIncidentList());

    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    // Don't allow timeUp more than 30 seconds into the future
    if (timeUp != null && timeUp.after(new Date(new Date().getTime() + 30000))) {
      throw new UserFriendlyException("Event time up cannot be in the future");
    }

    if (latestIncidentTimeUp != null && timeUp.before(latestIncidentTimeUp)) {
      throw new UserFriendlyException(
          "Event time up ("
              + formatter.format(timeUp)
              + ") must not be before latest incident time up ("
              + formatter.format(latestIncidentTimeUp)
              + ")");
    }

    Date latestIncidentTimeDown = computeLatestIncidentTimeDown(event.getIncidentList());

    if (latestIncidentTimeDown != null && timeUp.before(latestIncidentTimeDown)) {
      throw new UserFriendlyException("Event time up must not be before latest incident time down");
    }
  }

  private Date computeLatestIncidentTimeUp(List<Incident> incidentList) {
    Date latestIncidentTimeUp = null;

    if (incidentList != null && !incidentList.isEmpty()) {

      for (Incident incident : incidentList) {
        if (latestIncidentTimeUp == null) {
          latestIncidentTimeUp = incident.getTimeUp();
        } else if (incident.getTimeUp() != null
            && latestIncidentTimeUp.before(incident.getTimeUp())) {
          latestIncidentTimeUp = incident.getTimeUp();
        }
      }
    }

    return latestIncidentTimeUp;
  }

  private Date computeLatestIncidentTimeDown(List<Incident> incidentList) {
    Date latestIncidentTimeDown = null;

    if (incidentList != null && !incidentList.isEmpty()) {

      for (Incident incident : incidentList) {
        if (latestIncidentTimeDown == null) {
          latestIncidentTimeDown = incident.getTimeDown();
        } else if (incident.getTimeDown() != null
            && latestIncidentTimeDown.before(incident.getTimeDown())) {
          latestIncidentTimeDown = incident.getTimeDown();
        }
      }
    }

    return latestIncidentTimeDown;
  }

  @PermitAll
  public List<Event> filterList(AllEventsParams params) {
    CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
    CriteriaQuery<EventTimeDown> cq = cb.createQuery(EventTimeDown.class);
    Root<EventTimeDown> eventTimeDown = cq.from(EventTimeDown.class);
    cq.select(eventTimeDown).distinct(true);
    List<Predicate> filters = new ArrayList<>();

    Join<EventTimeDown, Event> event = eventTimeDown.join("event");
    Join<Event, Incident> incidentList = event.join("incidentList");

    if (params.getEnd() != null) { // "time_down < ? "
      filters.add(cb.lessThan((eventTimeDown.get("timeDown")), params.getEnd()));
    }

    if (params.getStart() != null) { // coalesce(time_up, sysdate) >= ?
      filters.add(
          cb.greaterThanOrEqualTo(
              (cb.coalesce(event.get("timeUp"), new Date())), params.getStart()));
    }
    if (params.getEventTypeId() != null) {
      filters.add(cb.equal(event.get("eventType"), params.getEventTypeId()));
    }
    if (params.getEventId() != null) {
      filters.add(cb.equal(event.get("eventId"), params.getEventId()));
    }

    if (params.getSmeUsername() != null && !params.getSmeUsername().isEmpty()) {
      Join<Incident, IncidentReview> reviewList = incidentList.join("incidentReviewList");

      filters.add(cb.in(reviewList.get("reviewer")).value(params.getSmeUsername()));
    }

    BigInteger[] incidentIdArray =
        IOUtil.removeNullValues(params.getIncidentIdArray(), BigInteger.class);

    if (incidentIdArray != null && incidentIdArray.length > 0) {

      List<BigInteger> a = Arrays.asList(incidentIdArray);

      filters.add(cb.in(incidentList.get("incidentId")).value(a));
    }

    if (params.getBeamTransport() != null) {
      Subquery<BigInteger> incidentSubquery = cq.subquery(BigInteger.class);
      Root<Incident> incidentSubRoot = incidentSubquery.from(Incident.class);
      incidentSubquery.select(incidentSubRoot.get("event"));
      incidentSubquery.where(cb.equal(incidentSubRoot.get("system"), 616));

      if (params.getBeamTransport()) {
        filters.add(event.get("eventId").in(incidentSubquery)); // 616 = 'Beam Transport'
      } else {
        filters.add(cb.not(event.get("eventId").in(incidentSubquery)));
      }
    }
    if (params.getAcknowledgement() != null) {
      filters.add(cb.equal(incidentList.get("expertAcknowledged"), params.getAcknowledgement()));
    }
    if (!filters.isEmpty()) {
      cq.where(cb.and(filters.toArray(new Predicate[] {})));
    }
    List<Order> orders = new ArrayList<>();
    Path p1 = eventTimeDown.get("timeDown");
    Order o1 = cb.desc(p1);
    orders.add(o1);
    cq.orderBy(orders);
    List<EventTimeDown> eventTimeDownList =
        getEntityManager()
            .createQuery(cq)
            .setFirstResult(params.getOffset())
            .setMaxResults(params.getMax())
            .getResultList();

    List<Event> eventList = new ArrayList<>();

    for (EventTimeDown etd : eventTimeDownList) {
      Event e = etd.getEvent();
      JPAUtil.initialize(e.getIncidentList());
      loadCategories(e);
      eventList.add(e);
    }

    return eventList;
  }

  @PermitAll
  public Long countFilterList(AllEventsParams params) {
    CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
    CriteriaQuery<Long> cq = cb.createQuery(Long.class);
    Root<EventTimeDown> eventTimeDown = cq.from(EventTimeDown.class);

    List<Predicate> filters = new ArrayList<>();

    Join<Event, EventTimeDown> event = eventTimeDown.join("event");
    Join<Event, Incident> incidentList = event.join("incidentList");

    if (params.getEnd() != null) { // "time_down < ? "
      filters.add(cb.lessThan((eventTimeDown.get("timeDown")), params.getEnd()));
    }

    if (params.getStart() != null) { // coalesce(time_up, sysdate) >= ?
      filters.add(
          cb.greaterThanOrEqualTo(
              (cb.coalesce(event.get("timeUp"), new Date())), params.getStart()));
    }
    if (params.getEventTypeId() != null) {
      filters.add(cb.equal(event.get("eventType"), params.getEventTypeId()));
    }
    if (params.getEventId() != null) {
      filters.add(cb.equal(event.get("eventId"), params.getEventId()));
    }

    if (params.getSmeUsername() != null && !params.getSmeUsername().isEmpty()) {
      Join<Incident, IncidentReview> reviewList = incidentList.join("incidentReviewList");

      filters.add(cb.in(reviewList.get("reviewer")).value(params.getSmeUsername()));
    }

    BigInteger[] incidentIdArray =
        IOUtil.removeNullValues(params.getIncidentIdArray(), BigInteger.class);

    if (incidentIdArray != null && incidentIdArray.length > 0) {
      List<BigInteger> a = Arrays.asList(incidentIdArray);

      filters.add(cb.in(incidentList.get("incidentId")).value(a));
    }
    if (params.getBeamTransport() != null) {
      Subquery<BigInteger> incidentSubquery = cq.subquery(BigInteger.class);
      Root<Incident> incidentSubRoot = incidentSubquery.from(Incident.class);
      incidentSubquery.select(incidentSubRoot.get("event"));
      incidentSubquery.where(cb.equal(incidentSubRoot.get("system"), 616));

      if (params.getBeamTransport()) {
        filters.add(event.get("eventId").in(incidentSubquery)); // 616 = 'Beam Transport'
      } else {
        filters.add(cb.not(event.get("eventId").in(incidentSubquery)));
      }
    }
    if (params.getAcknowledgement() != null) {
      filters.add(cb.equal(incidentList.get("expertAcknowledged"), params.getAcknowledgement()));
    }
    if (!filters.isEmpty()) {
      cq.where(cb.and(filters.toArray(new Predicate[] {})));
    }

    cq.select(cb.countDistinct(eventTimeDown)); // Use countDistinct() vs distinct(true);
    TypedQuery<Long> q = getEntityManager().createQuery(cq);
    return q.getSingleResult();
  }

  @PermitAll
  public void removeEvent(BigInteger eventId) throws WebApplicationException {
    logger.log(Level.FINEST, "removeEvent - eventId: {0}", eventId);
    checkAuthenticated();

    if (eventId == null) {
      throw new InternalException("Parameter eventId must not be null");
    }

    Event event = find(eventId);

    if (event == null) {
      throw new InternalException("Event with ID " + eventId + " not found");
    }

    checkCanEditIncidentOrEvent(event.containsAtLeastOneReview(), event);

    if (event.getTimeUp() == null) { // Cancel event if still open
      escalationService.cancelEscalationTimer(event);
    }

    remove(event);
  }

  @PermitAll
  public void loadClosedBy(List<Event> eventList) {
    if (eventList != null) {
      for (Event event : eventList) {
        loadClosedBy(event);
      }
    }
  }

  @PermitAll
  public void loadRepairedBy(List<Event> eventList) {
    if (eventList != null) {
      for (Event event : eventList) {
        loadRepairedBy(event);
      }
    }
  }

  @PermitAll
  public void loadReviewedBy(List<Event> eventList) {
    if (eventList != null) {
      for (Event event : eventList) {
        loadReviewedBy(event);
      }
    }
  }

  @PermitAll
  public void loadClosedBy(Event event) {
    EventAud aud = eventAudFacade.findLatestCloseRevision(event.getEventId());

    if (aud != null) {
      revisionFacade.loadUser(aud.getRevision());
      event.setClosedBy(aud.getRevision().getUser());
    }
  }

  @PermitAll
  public void loadRepairedBy(Event event) {
    List<Incident> incidentList = event.getIncidentList();

    for (Incident incident : incidentList) {
      if (em.contains(incident)) { // We don't want any auto loading / persisting
        em.detach(incident);
      }

      List<Repair> repairList = repairFacade.findByIncident(incident.getIncidentId());

      incident.setRepairedByList(repairList);
    }
  }

  @PermitAll
  public void loadReviewedBy(Event event) {
    List<Incident> incidentList = event.getIncidentList();

    for (Incident incident : incidentList) {
      if (em.contains(incident)) { // We don't want any auto loading / persisting
        em.detach(incident);
      }

      List<IncidentReview> reviewList = reviewFacade.findByIncident(incident.getIncidentId());

      incident.setIncidentReviewList(reviewList);
    }
  }

  @PermitAll
  public void loadCategories(List<Event> eventList) {
    if (eventList != null) {
      for (Event event : eventList) {
        loadCategories(event);
      }
    }
  }

  @PermitAll
  public void loadCategories(Event event) {
    if (event != null) {
      List<Incident> incidentList = event.getIncidentList();
      if (incidentList != null) {
        for (Incident incident : incidentList) {
          incident.getSystem().getCategory().getName();
        }
      }
    }
  }

  @PermitAll
  public void computeRestoreTime(List<Event> eventList) {
    if (eventList != null) {
      for (Event event : eventList) {
        computeRestoreTime(event);
      }
    }
  }

  @PermitAll
  public void computeRestoreTime(Event event) {
    if (event != null) {
      long restoreMillis = 0;
      List<Period> periodList = incidentFacade.findGapsBetweenIncidents(event.getEventId());
      Date maxIncidentTimeUp = event.getMaxIncidentTimeUp();
      Date eventTimeUp = event.getTimeUp();

      if (eventTimeUp == null) {
        eventTimeUp = new Date();
      }

      if (maxIncidentTimeUp == null) {
        maxIncidentTimeUp = eventTimeUp;
      }

      if (periodList != null) {
        for (Period p : periodList) {
          restoreMillis = restoreMillis + (p.getEndDate().getTime() - p.getStartDate().getTime());
        }
      }

      long trailingRestore = eventTimeUp.getTime() - maxIncidentTimeUp.getTime();

      restoreMillis = restoreMillis + trailingRestore;

      event.setRestoreMillis(restoreMillis);
    }
  }

  @PermitAll
  public List<Event> filterListWithIncidentsDefaultOpen(
      BigInteger eventTypeId, BigInteger eventId, BigInteger incidentId) {
    CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
    CriteriaQuery<Event> cq = cb.createQuery(Event.class);
    Root<Event> root = cq.from(Event.class);
    cq.select(root);
    List<Predicate> filters = new ArrayList<Predicate>();

    if (eventId == null && incidentId == null) {
      filters.add(cb.isNull(root.get("timeUp")));
    }

    if (eventTypeId != null) {
      filters.add(cb.equal(root.get("eventType").get("eventTypeId"), eventTypeId));
    }

    if (eventId != null) {
      filters.add(cb.equal(root.get("eventId"), eventId));
    }

    if (incidentId != null) {
      Join<Event, Incident> incidentList = root.join("incidentList");
      filters.add(cb.equal(incidentList.get("incidentId"), incidentId));
    }

    if (!filters.isEmpty()) {
      cq.where(cb.and(filters.toArray(new Predicate[] {})));
    }

    List<Order> orders = new ArrayList<Order>();
    Path p1 = root.get("eventType").get("eventTypeId");
    Order o1 = cb.desc(p1);
    orders.add(o1);
    cq.orderBy(orders);

    List<Event> eventList = getEntityManager().createQuery(cq).getResultList();

    for (Event event : eventList) {
      JPAUtil.initialize(event.getIncidentList());
    }

    return eventList;
  }
}
