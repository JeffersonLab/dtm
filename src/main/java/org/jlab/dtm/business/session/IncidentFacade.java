package org.jlab.dtm.business.session;

import java.io.IOException;
import java.math.BigInteger;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaBuilder.Coalesce;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

/*import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.HttpClientBuilder;*/
import org.jlab.dtm.business.params.IncidentParams;
import org.jlab.dtm.business.service.MigrateOldRarService;
import org.jlab.dtm.persistence.entity.Component;
import org.jlab.dtm.persistence.entity.Event;
import org.jlab.dtm.persistence.entity.EventType;
import org.jlab.dtm.persistence.entity.Incident;
import org.jlab.dtm.persistence.entity.IncidentReview;
import org.jlab.dtm.persistence.entity.Repair;
import org.jlab.dtm.persistence.entity.Workgroup;
import org.jlab.dtm.persistence.entity.SystemEntity;
import org.jlab.dtm.persistence.entity.SystemExpert;
import org.jlab.dtm.persistence.enumeration.IncidentEditType;
import org.jlab.dtm.persistence.enumeration.SystemExpertAcknowledgement;
import org.jlab.dtm.persistence.model.Period;
import org.jlab.smoothness.business.exception.InternalException;
import org.jlab.smoothness.business.exception.UserFriendlyException;
import org.jlab.smoothness.business.exception.WebApplicationException;
import org.jlab.smoothness.business.util.TimeUtil;
import org.jlab.smoothness.persistence.util.JPAUtil;
import org.jlab.smoothness.presentation.filter.AuditContext;

/**
 *
 * @author ryans
 */
@Stateless
public class IncidentFacade extends AbstractFacade<Incident> {

    private static final Logger LOGGER = Logger.getLogger(
            IncidentFacade.class.getName());
    @PersistenceContext(unitName = "dtmPU")
    private EntityManager em;
    @EJB
    EventFacade eventFacade;
    @EJB
    ComponentFacade componentFacade;
    @EJB
    EventTypeFacade eventTypeFacade;
    @EJB
    IncidentFacade incidentFacade;
    @EJB
    LogbookFacade logbookFacade;
    @EJB
    EscalationService escalationService;
    @EJB
    ResponsibleGroupFacade groupFacade;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public IncidentFacade() {
        super(Incident.class);
    }

    @SuppressWarnings("unchecked")
    @PermitAll
    public List<Period> findGapsBetweenIncidents(BigInteger eventId) {
        List<Period> periodList = new ArrayList<>();

        String query
                //= "SELECT cast(start_date as date) as start_date, cast(end_date as date) as end_date FROM (SELECT MAX(time_up) OVER (ORDER BY time_down) start_date, LEAD(time_down) OVER (ORDER BY time_down) end_date FROM incident where event_id = :eventId) WHERE start_date < end_date";                
                = "SELECT cast(start_date as date) as start_date, cast(end_date as date) as end_date FROM (SELECT MAX(nvl(time_up, sysdate)) OVER (ORDER BY time_down) start_date, LEAD(time_down) OVER (ORDER BY time_down) end_date FROM incident where event_id = :eventId) WHERE start_date < end_date";

        Query q = em.createNativeQuery(query);

        q.setParameter("eventId", eventId);

        List<Object[]> resultList = q.getResultList();

        if (resultList != null) {
            for (Object[] row : resultList) {
                Date start = (Date) row[0];
                Date end = (Date) row[1];
                Period period = new Period(start, end);
                periodList.add(period);
            }
        }

        return periodList;
    }

    private void validateAndPopulateIncident(Incident incident, String title, String summary,
            BigInteger componentId, String componentName, Date timeDown, Date timeUp,
            String explanation, String solution, BigInteger[] repairedBy,
            String reviewedBy, String[] expertUsernameArray, BigInteger rarId) throws
            WebApplicationException {
        if (title == null) {
            throw new UserFriendlyException("Incident title must not be empty");
        }

        if (title.length() > 128) {
            throw new UserFriendlyException("Incident title must be no more than 128 characters");
        }

        if (summary == null) {
            throw new UserFriendlyException("Incident summary must not be empty");
        }

        Component component = null;
        if (componentId != null) {
            component = componentFacade.find(componentId);

            if (component == null) {
                throw new UserFriendlyException("Component with ID " + componentId + " not found");
            }

            if (!component.getName().equals(componentName)) {
                throw new UserFriendlyException("Please choose a known component name");
            }
        } else {
            // If user doesn't select component from auto-complete list and 
            // instead pastes value into input or otherwise types full 
            // component name then componentId will be null

            // Attempt to find component by name, this might return multiple results.
            List<Component> componentList = componentFacade.findByName(componentName);

            if (componentList.isEmpty()) {
                throw new UserFriendlyException("Component not found with name: " + componentName);
            }

            if (componentList.size() != 1) {
                throw new UserFriendlyException("Multiple components with that name found, please filter by Category/System and choose from auto-complete");
            }

            component = componentList.get(0);
        }

        if (component.getName().equals("Unknown/Missing")) {
            if (explanation == null
                    || explanation.isEmpty()) {
                throw new UserFriendlyException(
                        "Explanation must be provided for Unknown/Missing component");
            }
        } else {
            explanation = null;
            /*Unset explanation if not Unknown/Missing*/

        }

        SystemEntity system = component.getSystem();

        if (timeDown == null) {
            throw new UserFriendlyException("Incident time down must not be empty");
        }

        // Don't allow timeDown more than 30 seconds into the future
        if (timeDown.after(new Date(new Date().getTime() + 30000))) {
            throw new UserFriendlyException("Incident time down cannot be in the future");
        }

        if (timeUp != null && timeUp.before(timeDown)) {
            throw new UserFriendlyException("Incident time up cannot come before time down");
        }

        // Don't allow timeUp more than 30 seconds into the future
        if (timeUp != null && timeUp.after(new Date(new Date().getTime() + 30000))) {
            throw new UserFriendlyException("Incident time up cannot be in the future");
        }

        AuditContext auditCtx = AuditContext.getCurrentInstance();
        boolean reviewer = "REVIEWER".equals(auditCtx.getExtra("EffectiveRole"));

        if (reviewedBy != null && !reviewedBy.trim().isEmpty()) {

            if (!reviewer) {
                throw new UserFriendlyException(
                        "You must be a reviewer to set the reviewed by field");
            }

            if (!incident.getEvent().isClosed()) {
                throw new UserFriendlyException(
                        "You cannot review an incident belonging to an open event (Reviewed By field must be empty)");
            } // NOTE: if you re-open an event we clear all reviewed by fields of sibling incidents automatically
        }

        if (incident.getExpertAcknowledged() == null) {
            incident.setExpertAcknowledged(SystemExpertAcknowledgement.N);
        }

        incident.setRarId(rarId);

        this.forceAcknowledgeIfCompletedLevelIIPlus(incident);

        // If System is changing then set default expert reviewers
        if (!system.equals(incident.getSystem())) {
            List<SystemExpert> expertList = system.getSystemExpertList();
            List<String> usernameList = new ArrayList<>();
            if (expertList != null) {
                for (SystemExpert se : expertList) {
                    usernameList.add(se.getUsername());
                }
            }
            expertUsernameArray = usernameList.toArray(new String[]{});
        }

        incident.setExplanation(explanation);
        incident.setTitle(title);
        incident.setSummary(summary);
        incident.setSystem(system);
        incident.setComponent(component);
        incident.setTimeDown(timeDown);
        incident.setTimeUp(timeUp);
        incident.setResolution(solution);
        incident.setReviewedUsername(reviewedBy);

        validateAndSetRepairedBy(incident, repairedBy);
        validateAndSetExpertReviewedBy(incident, expertUsernameArray);
    }

    private void validateAndSetRepairedBy(Incident incident, BigInteger[] repairedByGroupIdArray) {
        List<Workgroup> groupList = new ArrayList<>();

        if (repairedByGroupIdArray != null) {
            for (int j = 0; j < repairedByGroupIdArray.length; j++) {
                if (repairedByGroupIdArray[j] != null) {
                    Workgroup group = groupFacade.find(repairedByGroupIdArray[j]);
                    groupList.add(group);
                }
            }
        }

        List<Repair> requestedRepairList = new ArrayList<>();

        for (Workgroup group : groupList) {
            Repair repair = new Repair();
            repair.setRepairedBy(group);
            repair.setIncident(incident);
            requestedRepairList.add(repair);
        }

        if (incident.getIncidentId() != null) {
            clearRepairList(incident.getIncidentId());
        }

        incident.setRepairedByList(requestedRepairList);

        // TODO: Insert a CSV String into a repair history table...        
    }

    private void validateAndSetExpertReviewedBy(Incident incident, String[] expertUsernameArray) throws UserFriendlyException {
        List<IncidentReview> reviewList = new ArrayList<>();
        if (expertUsernameArray != null) {
            for (String s : expertUsernameArray) {
                if(s != null && !s.trim().isEmpty()) {
                    IncidentReview review = new IncidentReview();
                    review.setReviewer(s);
                    review.setIncident(incident);
                    reviewList.add(review);
                }
            }
        }

        if (incident.getIncidentId() != null) {
            clearReviewList(incident.getIncidentId());
        }

        incident.setIncidentReviewList(reviewList);
    }

    @PermitAll
    public void clearRepairList(BigInteger incidentId) {
        Query q
                = em.createNativeQuery("delete from incident_repair where incident_id = :incidentId");

        q.setParameter("incidentId", incidentId);

        q.executeUpdate();
    }

    @PermitAll
    public void clearReviewList(BigInteger incidentId) {
        Query q
                = em.createNativeQuery("delete from incident_review where incident_id = :incidentId");

        q.setParameter("incidentId", incidentId);

        q.executeUpdate();
    }

    @PermitAll
    public void removeIncident(BigInteger incidentId) throws WebApplicationException {
        LOGGER.log(Level.FINEST, "removeIncient - incidentId: {0}", incidentId);
        checkAuthenticated();

        if (incidentId == null) {
            throw new InternalException("Parameter incidentId must not be null");
        }

        Incident incident = find(incidentId);

        if (incident == null) {
            throw new InternalException("Incident with ID " + incidentId + " not found");
        }

        Event event = incident.getEvent();

        checkCanEditIncidentOrEvent(incident.getReviewedUsername() != null, event);

        event.getIncidentList().remove(incident);

        remove(incident);

        if (event.getIncidentList().isEmpty()) {
            escalationService.cancelEscalationTimer(event);
            eventFacade.remove(event);
        } else {
            escalationService.resetEscalation(event);
        }
    }

    @PermitAll
    public Event addEvent(BigInteger eventTypeId, Date eventTimeUp, Date timeDown, Date timeUp,
            String title, String summary, BigInteger componentId, String componentName,
            String eventTitle,
            String explanation, String solution, BigInteger[] repairedBy,
            String reviewedBy,
            String[] expertUsernameArray,
            BigInteger rarId) throws
            WebApplicationException {
        LOGGER.log(Level.FINEST,
                "addEvent - eventTypeId: {0}, eventTimeUp: {1}, timeDown: {2}, timeUp: {3}, title: {4}, componentId: {5}, eventTitle: {6}",
                new Object[]{eventTypeId, eventTimeUp, timeDown, timeUp, title,
                    componentId, eventTitle});
        checkAuthenticated();

        if (eventTypeId == null) {
            throw new InternalException("Parameter eventTypeId must not be null");
        }

        if (eventTitle == null) {
            throw new InternalException("Event Title must not be null");
        }

        if (eventTitle.length() > 128) {
            throw new UserFriendlyException("Event title must be no more than 128 characters");
        }

        EventType type = eventTypeFacade.find(eventTypeId);

        if (type == null) {
            throw new InternalException("Event type with ID " + eventTypeId + " not found");
        }

        Event event = eventFacade.findOpenEvent(eventTypeId);

        if (event != null && eventTimeUp == null) {
            throw new UserFriendlyException("An event of type " + type.getName()
                    + " is already open (cannot add another open event of same type)");
        }

        /* we must ensure that an incident has a timeUp if the event is closed*/
        if (eventTimeUp != null && timeUp == null) {
            throw new UserFriendlyException("Incident time up is required since the event is closed");
        }

        if (eventTimeUp != null && timeUp != null && timeUp.after(eventTimeUp)) {
            throw new UserFriendlyException("Incident time up can not come after event time up");
        }

        // Enforce restrictions on closed events.  Operators must work with open events if they wish to mettle in the past
        AuditContext auditCtx = AuditContext.getCurrentInstance();

        boolean reviewer = "REVIEWER".equals(auditCtx.getExtra("EffectiveRole"));
        //boolean reviewer = context.isCallerInRole("REVIEWER");

        final long MILLIS_PER_10_HOURS = 36000000;

        if (!reviewer && eventTimeUp != null && eventTimeUp.getTime() < ((new Date()).getTime()
                - MILLIS_PER_10_HOURS)) {
            throw new UserFriendlyException("Only a reviewer can add events over 10 hours old");
        }

        event = new Event();
        event.setTitle(eventTitle);
        event.setEventType(type);
        event.setTimeUp(eventTimeUp);

        Incident incident = new Incident();

        // BEGIN EVENT SET BLOCK - Comes before validateIncident due to method using event
        incident.setEvent(event);

        List<Incident> incidentList = event.getIncidentList();
        if (incidentList == null) { // Should always be true since this is "create-new" method
            incidentList = new ArrayList<>();
            event.setIncidentList(incidentList);
        }

        incidentList.add(incident);
        // END EVENT SET BLOCK

        validateAndPopulateIncident(incident, title, summary, componentId, componentName, timeDown,
                timeUp,
                explanation, solution, repairedBy, reviewedBy, expertUsernameArray, rarId);

        eventFacade.create(event);
        create(incident);

        /*
         Must flush here to see if database integrity check trigger throws 
         error.  If we don't flush and there is an error in trigger Hibernate
         will have already passed the new event object onto either the 
         escalationService or logbookFacade and will therefore try to insert
         the event AGAIN, which will result in a unqiue constraint violation
         on the primary key AND will hide the root cause of Oracle Trigger 
         error
         */
        em.flush();

        if (eventTimeUp == null) {
            escalationService.resetEscalation(event);
        }

        if (!reviewer) {
            logbookFacade.silentlyCreateIncidentELog(incident, IncidentEditType.CREATED);
            if (incident.getTimeUp() != null) {
                logbookFacade.silentlyCreateIncidentELog(incident, IncidentEditType.CLOSED);
            }
        }

        return event;
    }

    @PermitAll
    public void editIncident(BigInteger incidentId, Date timeDown, Date timeUp,
            String title, String summary, BigInteger componentId,
            String componentName, String eventTitle, String explanation,
            String solution, BigInteger[] repairedBy, String reviewedBy,
            String[] expertUsernameArray, BigInteger rarId) throws WebApplicationException {
        LOGGER.log(Level.FINEST,
                "editIncident - incidentId: {0}, timeDown: {1}, timeUp: {2}, title: {3}, componentId: {4}, eventTitle: {5}",
                new Object[]{incidentId, timeDown, timeUp, title, componentId, eventTitle});
        checkAuthenticated();

        if (incidentId == null) {
            throw new InternalException("Parameter incidentId must not be null");
        }

        Incident incident = incidentFacade.find(incidentId);

        if (incident == null) {
            throw new InternalException("Incident with ID " + incidentId + " not found");
        }

        Event event = incident.getEvent();

        checkCanEditIncidentOrEvent(incident.getReviewedUsername() != null, event);

        boolean wasPreviouslyClosed = incident.getTimeUp() != null;

        validateAndPopulateIncident(incident, title, summary, componentId,
                componentName, timeDown, timeUp, explanation, solution,
                repairedBy, reviewedBy, expertUsernameArray, rarId);

        if (eventTitle == null) {
            throw new InternalException("Event Title must not be null");
        }

        if (eventTitle.length() > 128) {
            throw new UserFriendlyException("Event title must be no more than 128 characters");
        }

        event.setTitle(eventTitle);

        /*If event is closed (i.e. timeUp has been set), then be sure modifications to incident TimeUp don't make event time up illegal*/
        Date eventTimeUp = event.getTimeUp();

        if (eventTimeUp != null) {
            /* Since we're editing an event that is closed, we must ensure that the modified incident has a timeUp*/
            if (timeUp == null) {
                throw new UserFriendlyException(
                        "Incident time up is required since the event is closed");
            }

            eventFacade.validateEventTimeUp(event, eventTimeUp);
        } else {
            escalationService.resetEscalation(event);
        }

        /*Throw any errors NOW to avoid issues passing a bogus incident to logbook*/
        em.flush();

        AuditContext auditCtx = AuditContext.getCurrentInstance();

        boolean reviewer = "REVIEWER".equals(auditCtx.getExtra("EffectiveRole"));

        if (!reviewer) {
            if (!wasPreviouslyClosed && incident.getTimeUp() != null) {
                logbookFacade.silentlyCreateIncidentELog(incident, IncidentEditType.CLOSED);
            }
        }
    }

    @PermitAll
    public void addIncident(BigInteger eventId, Date timeDown, Date timeUp, String title,
            String summary,
            BigInteger componentId, String componentName, String eventTitle, String explanation,
            String solution, BigInteger[] repairedBy, String reviewedBy,
            String[] expertUsernameArray, BigInteger rarId) throws
            WebApplicationException {
        LOGGER.log(Level.FINEST,
                "addIncident - eventId: {0}, timeDown: {1}, timeUp: {2}, title: {3}, componentId: {4}, eventTitle: {5}",
                new Object[]{eventId, timeDown, timeUp, title, componentId, eventTitle});
        checkAuthenticated();

        if (eventId == null) {
            throw new InternalException("Parameter eventId must not be null");
        }

        if (eventTitle == null) {
            throw new InternalException("Event Title must not be null");
        }

        if (eventTitle.length() > 128) {
            throw new UserFriendlyException("Event title must be no more than 128 characters");
        }

        Event event = eventFacade.find(eventId);

        if (event == null) {
            throw new InternalException("Event with ID " + eventId + " not found");
        }

        checkCanEditIncidentOrEvent((reviewedBy != null && !reviewedBy.trim().isEmpty())
                || event.containsAtLeastOneReview(), event);

        event.setTitle(eventTitle);

        em.flush();

        Incident incident = new Incident();

        // BEGIN EVENT SET BLOCK - Comes before validateIncident due to method using event
        List<Incident> incidentList = event.getIncidentList();
        if (incidentList == null) { // Should never happen since this is "add-to-existing" method
            incidentList = new ArrayList<>();
        }

        em.detach(event); // Weird errors otherwise...
        incident.setEvent(event);
        incidentList.add(incident);
        // END EVENT SET BLOCK        

        validateAndPopulateIncident(incident, title, summary, componentId,
                componentName, timeDown, timeUp, explanation, solution,
                repairedBy, reviewedBy, expertUsernameArray, rarId);

        /*If event is closed (i.e. timeUp has been set), then be sure modifications to incident TimeUp don't make event time up illegal*/
        Date eventTimeUp = event.getTimeUp();

        if (eventTimeUp != null) {
            /* Since we're editing an event that is closed, we must ensure that the modified incident has a timeUp*/
            if (timeUp == null) {
                throw new UserFriendlyException(
                        "Incident time up is required since the event is closed");
            }

            eventFacade.validateEventTimeUp(event, eventTimeUp);
        } else {
            escalationService.resetEscalation(event);
        }

        create(incident);

        /*Throw any errors NOW to avoid issues passing a bogus incident to logbook*/
        em.flush();

        AuditContext auditCtx = AuditContext.getCurrentInstance();

        boolean reviewer = "REVIEWER".equals(auditCtx.getExtra("EffectiveRole"));

        if (!reviewer) {
            logbookFacade.silentlyCreateIncidentELog(incident, IncidentEditType.CREATED);
            if (incident.getTimeUp() != null) {
                logbookFacade.silentlyCreateIncidentELog(incident, IncidentEditType.CLOSED);
            }
        }
    }

    @PermitAll
    public List<Incident> filterList(IncidentParams params) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Incident> cq = cb.createQuery(Incident.class);
        Root<Incident> root = cq.from(Incident.class);

        Join<Incident, Event> event = root.join("event");

        List<Predicate> filters = new ArrayList<>();

        if(params.getClosedOnly() != null && params.getClosedOnly()) {
            filters.add(root.get("timeUp").isNotNull());
        }

        if(params.isDateRangeForUploaded()) {
            if (params.getStart() != null) {
                filters.add(cb.greaterThanOrEqualTo(root.get("rarUploadedDate"), params.getStart()));
            }

            if (params.getEnd() != null) {
                filters.add(cb.lessThan(root.get("rarUploadedDate"), params.getEnd()));
            }
        } else {
            if (params.getStart() != null) {
                Coalesce<Date> c = cb.coalesce();
                c.value(root.get("timeUp"));
                c.value(new Date());
                filters.add(cb.greaterThanOrEqualTo(c, params.getStart()));
            }

            if (params.getEnd() != null) {
                filters.add(cb.lessThan(root.get("timeDown"), params.getEnd()));
            }
        }

        if (params.getEventTypeId() != null) {
            filters.add(cb.equal(event.get("eventType"), params.getEventTypeId()));
        }

        if (params.getEventId() != null) {
            filters.add(cb.equal(event.get("eventId"), params.getEventId()));
        }

        if (params.getIncidentId() != null) {
            filters.add(cb.equal(root.get("incidentId"), params.getIncidentId()));
        }

        if (params.getTitle() != null && !params.getTitle().isEmpty()) {
            filters.add(cb.like(cb.upper(root.get("title")), "%" + params.getTitle().toUpperCase() + "%"));
        }

        if (params.getReviewed() != null) {
            filters.add(cb.equal(root.get("reviewed"), params.getReviewed() ? "Y" : "N"));
        }

        if (params.getLevel() != null) {
            filters.add(cb.equal(root.get("level"), params.getLevel()));
        }

        if (params.getHasAttachment() != null) {
            if(params.getHasAttachment()) {
                filters.add(cb.isNotNull(root.get("rarExt")));
            } else {
                filters.add(cb.isNull(root.get("rarExt")));
            }
        }

        if (params.getSmeUsername() != null && !params.getSmeUsername().isEmpty()) {
            Join<Incident, IncidentReview> reviewList = root.join("incidentReviewList");

            filters.add(cb.in(reviewList.get("reviewer")).value(params.getSmeUsername()));
        }

        if (!filters.isEmpty()) {
            cq.where(cb.and(filters.toArray(new Predicate[]{})));
        }

        cq.select(root);

        List<Order> orderList = new ArrayList<>();
        for (OrderDirective od : params.getOrderDirectives()) {
            if (od != null) {
                Path p = root.get(od.getField());
                Order o = od.isAsc() ? cb.asc(p) : cb.desc(p);
                orderList.add(o);
            }
        }
        
        // We need to ensure unique key is part of sort, especially since we paginate
        orderList.add(cb.desc(root.get("incidentId")));
        
        cq.orderBy(orderList);
        TypedQuery<Incident> q = em.createQuery(cq).setMaxResults(params.getMax()).setFirstResult(params.getOffset());
        return q.getResultList();
    }

    @PermitAll
    public Long countFilterList(IncidentParams params) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<Incident> root = cq.from(Incident.class);

        Join<Incident, Event> event = root.join("event");

        List<Predicate> filters = new ArrayList<>();

        if(params.isDateRangeForUploaded()) {
            if (params.getStart() != null) {
                filters.add(cb.greaterThanOrEqualTo(root.get("rarUploadedDate"), params.getStart()));
            }

            if (params.getEnd() != null) {
                filters.add(cb.lessThan(root.get("rarUploadedDate"), params.getEnd()));
            }
        } else {
            if (params.getStart() != null) {
                Coalesce<Date> c = cb.coalesce();
                c.value(root.get("timeUp"));
                c.value(new Date());
                filters.add(cb.greaterThanOrEqualTo(c, params.getStart()));
            }

            if (params.getEnd() != null) {
                filters.add(cb.lessThan(root.get("timeDown"), params.getEnd()));
            }
        }

        if (params.getEventTypeId() != null) {
            filters.add(cb.equal(event.get("eventType"), params.getEventTypeId()));
        }

        if (params.getEventId() != null) {
            filters.add(cb.equal(event.get("eventId"), params.getEventId()));
        }

        if (params.getIncidentId() != null) {
            filters.add(cb.equal(root.get("incidentId"), params.getIncidentId()));
        }

        if (params.getTitle() != null && !params.getTitle().isEmpty()) {
            filters.add(cb.like(cb.upper(root.get("title")), "%" + params.getTitle().toUpperCase() + "%"));
        }

        if (params.getReviewed() != null) {
            filters.add(cb.equal(root.get("reviewed"), params.getReviewed() ? "Y" : "N"));
        }

        if (params.getLevel() != null) {
            filters.add(cb.equal(root.get("level"), params.getLevel()));
        }

        if (params.getHasAttachment() != null) {
            if(params.getHasAttachment()) {
                filters.add(cb.isNotNull(root.get("rarExt")));
            } else {
                filters.add(cb.isNull(root.get("rarExt")));
            }
        }

        if (params.getSmeUsername() != null && !params.getSmeUsername().isEmpty()) {
            Join<Incident, IncidentReview> reviewList = root.join("incidentReviewList");
            Join<IncidentReview, String> reviewerList = reviewList.join("reviewer");

            filters.add(cb.in(reviewerList.get("username")).value(params.getSmeUsername()));
        }

        if (!filters.isEmpty()) {
            cq.where(cb.and(filters.toArray(new Predicate[]{})));
        }

        cq.select(cb.count(root));
        TypedQuery<Long> q = getEntityManager().createQuery(cq);
        return q.getSingleResult();
    }

    private void forceAcknowledgeIfCompletedLevelIIPlus(Incident incident) {
        // Force acknowledge if RAR ID or Root Cause set
        // We filter list of experts to email by acknolwedge field so this is 
        // important to prevent emailing experts about completed Level II and III+
        if (incident.getExpertAcknowledged() == SystemExpertAcknowledgement.N) {
            String rootCause = incident.getRootCause();

            if (incident.getRarId() != null || (rootCause != null && !rootCause.isEmpty())) {
                incident.setExpertAcknowledged(SystemExpertAcknowledgement.Y);
            }
        }
    }

    @PermitAll
    public void editSystemExpertReview(BigInteger incidentId, SystemExpertAcknowledgement acknowledgement, String rootCause) {
        checkAuthenticated();

        Incident incident = find(incidentId);

        incident.setExpertAcknowledged(acknowledgement);
        incident.setRootCause(rootCause);

        forceAcknowledgeIfCompletedLevelIIPlus(incident);
    }

    @SuppressWarnings("unchecked")
    @PermitAll
    public List<String> findAllExpertsWithRecentUnreviewedIncidents(int numberOfHours) {
        Query q = em.createNativeQuery("select unique reviewer_username from incident_review, incident where incident_review.incident_id = incident.incident_id and expert_acknowledged = 'N' and time_up is not null and time_up > :timeUp");

        Calendar c = Calendar.getInstance();
        //Date now = new Date();
        c.set(Calendar.MILLISECOND, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.HOUR_OF_DAY, 7);
        c.add(Calendar.HOUR_OF_DAY, numberOfHours * -1);

        q.setParameter("timeUp", c);

        return q.getResultList();
    }

    @PermitAll
    public List<Incident> filterListWithLazyRelations(IncidentParams params) {
        List<Incident> incidentList = filterList(params);
        for (Incident incident : incidentList) {
            for (Repair r : incident.getRepairedByList()) {
                r.getRepairedBy().getWorkgroupId();
            }

            JPAUtil.initialize(incident.getIncidentReviewList());
        }
        return incidentList;
    }

    @PermitAll
    public void rarUploaded(BigInteger incidentId, String ext) {
        Incident incident = find(incidentId);

        if(incident != null) {
            incident.setRarExt(ext);
            incident.setRarUploadedDate(new Date());
        } else {
            throw new IllegalArgumentException("Could not locate incident with ID: " + incidentId);
        }
    }

    @RolesAllowed("dtm-admin")
    public List<TransitionRecord> migrateOldRarRecords(int year) throws SQLException, WebApplicationException, IOException, InterruptedException {
        List<TransitionRecord> transitionRecordList = new ArrayList<>();

        /*MigrateOldRarService old = new MigrateOldRarService();
        List<MigrateOldRarService.OldRARRecord> recordList = old.getOldRecords(year);

        Map<Date, Integer> dateMap = new HashMap<>();

        for(MigrateOldRarService.OldRARRecord record: recordList) {
            BigInteger[] repairedByGroupIdArray = null;
            String reviewedBy = null;
            String[] expertUsernameArray = null;

            Integer count = dateMap.get(record.requestDate);

            if(count == null) {
                count = 0;
            } else {
                count++;
            }

            dateMap.put(record.requestDate, count);

            // We simply add 4 hours each time we see the same date
            record.requestDate = TimeUtil.addHours(record.requestDate, count * 4);

            System.err.println("Date: " + record.requestDate);

            Date timeDown = record.requestDate;
            Date eventTimeUp = TimeUtil.addHours(record.requestDate, 4);
            Date timeUp = eventTimeUp;

            switch(record.groupName) {
                case "OPS":
                    repairedByGroupIdArray = new BigInteger[]{BigInteger.valueOf(21)}; // Ops
                    break;
                case "CRYO":
                    repairedByGroupIdArray = new BigInteger[]{BigInteger.valueOf(12)}; // Cryo
                    break;
                case "INJECTOR":
                    repairedByGroupIdArray = new BigInteger[]{BigInteger.valueOf(23)}; // Gun
                    break;
                case "EES":
                    repairedByGroupIdArray = new BigInteger[]{BigInteger.valueOf(8), BigInteger.valueOf(10), BigInteger.valueOf(14)}; // DC Power, I&C Hardware, RF
                    break;
                case "FACILITY":
                    repairedByGroupIdArray = new BigInteger[]{BigInteger.valueOf(19)}; // Facilities
                    break;
                case "ENGINEERING":
                    repairedByGroupIdArray = new BigInteger[]{BigInteger.valueOf(1), BigInteger.valueOf(4)}; // Magnet Measurement, Alignment
                    break;
                case "SRF":
                    repairedByGroupIdArray = new BigInteger[]{BigInteger.valueOf(13)}; // SRF
                    break;
                case "VACUUM":
                    repairedByGroupIdArray = new BigInteger[]{BigInteger.valueOf(3)}; // Vacuum
                    break;
                case "SOFTWARE":
                    repairedByGroupIdArray = new BigInteger[]{BigInteger.valueOf(6)}; // Low Level Apps
                    break;
                case "RADCON":
                    repairedByGroupIdArray = new BigInteger[]{BigInteger.valueOf(20)}; // RADCON
                    break;
                case "AHLA":
                    repairedByGroupIdArray = new BigInteger[]{BigInteger.valueOf(9)}; // High Level Apps
                    break;
                case "INSTALLATION":
                    repairedByGroupIdArray = new BigInteger[]{BigInteger.valueOf(2)}; // Installation
                    break;
                case "SAFETY":
                    repairedByGroupIdArray = new BigInteger[]{BigInteger.valueOf(15)}; // SSG
                    break;
            }


            Staff staff = staffFacade.find(BigInteger.valueOf(record.staffId));

            if(staff != null) {
                String username = staff.getUsername();

                if(username != null) {
                    expertUsernameArray = new String[]{username};
                }
            }


            Event event = this.addEvent(BigInteger.ONE,
                    eventTimeUp,
                    timeDown,
                    timeUp,
                    record.title,
                    "imported from old RAR database from RAR ID " + record.rarId,
                    BigInteger.valueOf(21374),
                    "Unknown/Missing",
                    record.title,
                    "imported",
                    "",
                    repairedByGroupIdArray,
                    reviewedBy,
                    expertUsernameArray,
                    BigInteger.valueOf(record.rarId));

            System.err.println("new event id: " + event.getEventId());

            BigInteger incidentId = event.getIncidentList().get(0).getIncidentId();

            TransitionRecord transitionRecord = new TransitionRecord();
            transitionRecord.rarId = record.rarId;
            transitionRecord.incidentId = incidentId;
            transitionRecordList.add(transitionRecord);
        }*/

        return transitionRecordList;
    }

    @RolesAllowed("dtm-admin")
    public void migrateOldRarAttachments(List<TransitionRecord> recordList) throws SQLException, IOException {
        /*MigrateOldRarService old = new MigrateOldRarService();

        for(TransitionRecord record: recordList) {
            MigrateOldRarService.AttachmentRecord attach = old.getAttachment(record.rarId);

            if (attach != null) {
                System.err.println("attachment found");

                HttpPost post = new HttpPost("http://localhost:8080/dtm/ajax/rar-upload");
                MultipartEntityBuilder builder = MultipartEntityBuilder.create();
                builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
                builder.addBinaryBody("rar", attach.in, ContentType.create(attach.contentType), attach.filename);
                builder.addTextBody("incidentId", record.incidentId.toString(), ContentType.TEXT_PLAIN);

                HttpEntity entity = builder.build();
                post.setEntity(entity);

                HttpClient client = HttpClientBuilder.create().build();
                HttpResponse response = client.execute(post);

                System.err.println("Upload response: " + response.getStatusLine().getStatusCode());
            } else {
                System.err.println("no attachment found");
            }
        }*/
    }

    public class TransitionRecord {
        public int rarId;
        public BigInteger incidentId;
    }
}
