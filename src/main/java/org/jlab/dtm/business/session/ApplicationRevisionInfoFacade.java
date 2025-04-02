package org.jlab.dtm.business.session;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.annotation.security.PermitAll;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;
import org.hibernate.envers.RevisionType;
import org.jlab.dtm.persistence.entity.ApplicationRevisionInfo;
import org.jlab.dtm.persistence.entity.Event;
import org.jlab.dtm.persistence.entity.Incident;
import org.jlab.dtm.persistence.entity.aud.EventAud;
import org.jlab.dtm.persistence.entity.aud.IncidentAud;
import org.jlab.dtm.persistence.model.AuditedEntityChange;
import org.jlab.smoothness.persistence.view.User;
import org.jlab.smoothness.presentation.util.Functions;

/**
 * @author ryans
 */
@Stateless
public class ApplicationRevisionInfoFacade extends AbstractFacade<ApplicationRevisionInfo> {

  @PersistenceContext(unitName = "webappPU")
  private EntityManager em;

  @Override
  protected EntityManager getEntityManager() {
    return em;
  }

  public ApplicationRevisionInfoFacade() {
    super(ApplicationRevisionInfo.class);
  }

  @PermitAll
  public List<ApplicationRevisionInfo> filterList(
      Date modifiedStart,
      Date modifiedEnd,
      BigInteger eventId,
      BigInteger incidentId,
      int offset,
      int max) {
    CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
    CriteriaQuery<ApplicationRevisionInfo> cq = cb.createQuery(ApplicationRevisionInfo.class);
    Root<ApplicationRevisionInfo> root = cq.from(ApplicationRevisionInfo.class);
    cq.select(root);

    List<Predicate> filters = new ArrayList<Predicate>();

    if (modifiedStart != null) {
      filters.add(cb.greaterThanOrEqualTo(root.get("ts"), modifiedStart.getTime()));
    }

    if (modifiedEnd != null) {
      filters.add(cb.lessThan(root.get("ts"), modifiedEnd.getTime()));
    }

    if (incidentId != null) {
      Subquery<Integer> incidentSubSubquery = cq.subquery(Integer.class);
      Root<IncidentAud> incidentSubRoot = incidentSubSubquery.from(IncidentAud.class);
      incidentSubSubquery.select(incidentSubRoot.get("eventId"));
      incidentSubSubquery.where(
          cb.equal(incidentSubRoot.get("incidentAudPK").get("incidentId"), incidentId));

      Subquery<Integer> incidentSubquery = cq.subquery(Integer.class);
      Root<IncidentAud> incidentRoot = incidentSubquery.from(IncidentAud.class);
      incidentSubquery.select(incidentRoot.get("revision"));
      incidentSubquery.where(cb.in(incidentRoot.get("eventId")).value(incidentSubSubquery));
      Predicate incidentPredicate = cb.in(root.get("id")).value(incidentSubquery);

      Subquery<Integer> eventSubSubquery = cq.subquery(Integer.class);
      Root<IncidentAud> eventSubRoot = eventSubSubquery.from(IncidentAud.class);
      eventSubSubquery.select(eventSubRoot.get("eventId"));
      eventSubSubquery.where(
          cb.equal(eventSubRoot.get("incidentAudPK").get("incidentId"), incidentId));

      Subquery<Integer> eventSubquery = cq.subquery(Integer.class);
      Root<EventAud> eventRoot = eventSubquery.from(EventAud.class);
      eventSubquery.select(eventRoot.get("revision"));
      eventSubquery.where(
          cb.in(eventRoot.get("eventAudPK").get("eventId")).value(eventSubSubquery));
      Predicate eventPredicate = cb.in(root.get("id")).value(eventSubquery);

      filters.add(cb.or(incidentPredicate, eventPredicate));
    }

    if (eventId != null) {
      Subquery<Integer> incidentSubquery = cq.subquery(Integer.class);
      Root<IncidentAud> incidentRoot = incidentSubquery.from(IncidentAud.class);
      incidentSubquery.select(incidentRoot.get("revision"));
      incidentSubquery.where(cb.equal(incidentRoot.get("eventId"), eventId));
      Predicate incidentPredicate = cb.in(root.get("id")).value(incidentSubquery);

      Subquery<Integer> eventSubquery = cq.subquery(Integer.class);
      Root<EventAud> eventRoot = eventSubquery.from(EventAud.class);
      eventSubquery.select(eventRoot.get("revision"));
      eventSubquery.where(cb.in(eventRoot.get("eventAudPK").get("eventId")).value(eventId));
      Predicate eventPredicate = cb.in(root.get("id")).value(eventSubquery);

      filters.add(cb.or(incidentPredicate, eventPredicate));
    }

    if (!filters.isEmpty()) {
      cq.where(cb.and(filters.toArray(new Predicate[] {})));
    }
    List<Order> orders = new ArrayList<Order>();
    Path p0 = root.get("id");
    Order o0 = cb.desc(p0);
    orders.add(o0);
    cq.orderBy(orders);

    List<ApplicationRevisionInfo> revisionList =
        getEntityManager()
            .createQuery(cq)
            .setFirstResult(offset)
            .setMaxResults(max)
            .getResultList();

    if (revisionList != null) {
      for (ApplicationRevisionInfo revision : revisionList) {
        revision.setChangeList(findEntityChangeList(revision.getId()));
      }
    }

    return revisionList;
  }

  @PermitAll
  public Long countFilterList(
      Date modifiedStart, Date modifiedEnd, BigInteger eventId, BigInteger incidentId) {
    CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
    CriteriaQuery<Long> cq = cb.createQuery(Long.class);
    Root<ApplicationRevisionInfo> root = cq.from(ApplicationRevisionInfo.class);

    List<Predicate> filters = new ArrayList<Predicate>();

    if (modifiedStart != null) {
      filters.add(cb.greaterThanOrEqualTo(root.get("ts"), modifiedStart.getTime()));
    }

    if (modifiedEnd != null) {
      filters.add(cb.lessThan(root.get("ts"), modifiedEnd.getTime()));
    }

    if (incidentId != null) {
      Subquery<Integer> incidentSubSubquery = cq.subquery(Integer.class);
      Root<IncidentAud> incidentSubRoot = incidentSubSubquery.from(IncidentAud.class);
      incidentSubSubquery.select(incidentSubRoot.get("eventId"));
      incidentSubSubquery.where(
          cb.equal(incidentSubRoot.get("incidentAudPK").get("incidentId"), incidentId));

      Subquery<Integer> incidentSubquery = cq.subquery(Integer.class);
      Root<IncidentAud> incidentRoot = incidentSubquery.from(IncidentAud.class);
      incidentSubquery.select(incidentRoot.get("revision"));
      incidentSubquery.where(cb.in(incidentRoot.get("eventId")).value(incidentSubSubquery));
      Predicate incidentPredicate = cb.in(root.get("id")).value(incidentSubquery);

      Subquery<Integer> eventSubSubquery = cq.subquery(Integer.class);
      Root<IncidentAud> eventSubRoot = eventSubSubquery.from(IncidentAud.class);
      eventSubSubquery.select(eventSubRoot.get("eventId"));
      eventSubSubquery.where(
          cb.equal(eventSubRoot.get("incidentAudPK").get("incidentId"), incidentId));

      Subquery<Integer> eventSubquery = cq.subquery(Integer.class);
      Root<EventAud> eventRoot = eventSubquery.from(EventAud.class);
      eventSubquery.select(eventRoot.get("revision"));
      eventSubquery.where(
          cb.in(eventRoot.get("eventAudPK").get("eventId")).value(eventSubSubquery));
      Predicate eventPredicate = cb.in(root.get("id")).value(eventSubquery);

      filters.add(cb.or(incidentPredicate, eventPredicate));
    }

    if (eventId != null) {
      Subquery<Integer> incidentSubquery = cq.subquery(Integer.class);
      Root<IncidentAud> incidentRoot = incidentSubquery.from(IncidentAud.class);
      incidentSubquery.select(incidentRoot.get("revision"));
      incidentSubquery.where(cb.equal(incidentRoot.get("eventId"), eventId));
      Predicate incidentPredicate = cb.in(root.get("id")).value(incidentSubquery);

      Subquery<Integer> eventSubquery = cq.subquery(Integer.class);
      Root<EventAud> eventRoot = eventSubquery.from(EventAud.class);
      eventSubquery.select(eventRoot.get("revision"));
      eventSubquery.where(cb.in(eventRoot.get("eventAudPK").get("eventId")).value(eventId));
      Predicate eventPredicate = cb.in(root.get("id")).value(eventSubquery);

      filters.add(cb.or(incidentPredicate, eventPredicate));
    }

    if (!filters.isEmpty()) {
      cq.where(cb.and(filters.toArray(new Predicate[] {})));
    }

    cq.select(cb.count(root));
    TypedQuery<Long> q = getEntityManager().createQuery(cq);
    return q.getSingleResult();
  }

  @SuppressWarnings("unchecked")
  @PermitAll
  public List<AuditedEntityChange> findEntityChangeList(long revision) {
    Query q =
        em.createNativeQuery(
            "select 'I', incident_id, revtype from incident_aud where rev = :revision union select 'E', event_id, revtype from event_aud where rev = :revision");

    q.setParameter("revision", revision);

    List<Object[]> resultList = q.getResultList();

    List<AuditedEntityChange> changeList = new ArrayList<AuditedEntityChange>();

    if (resultList != null) {
      for (Object[] row : resultList) {
        Class entityClass = null;
        entityClass = fromCharacter(((Character) row[0]));
        BigInteger entityId = BigInteger.valueOf(((Number) row[1]).longValue());
        RevisionType type = fromNumber((Number) row[2]);
        changeList.add(new AuditedEntityChange(revision, type, entityId, entityClass));
      }
    }

    return changeList;
  }

  @PermitAll
  public Class fromCharacter(Character c) {
    Class entityClass = null;

    if (c != null) {
      if (c.equals('I')) {
        entityClass = Incident.class;
      } else if (c.equals('E')) {
        entityClass = Event.class;
      }
    }

    return entityClass;
  }

  @PermitAll
  public RevisionType fromNumber(Number n) {
    RevisionType type = null;

    if (n != null) {
      int intValue = (int) n.longValue();

      switch (intValue) {
        case 0:
          type = RevisionType.ADD;
          break;
        case 1:
          type = RevisionType.MOD;
          break;
        case 2:
          type = RevisionType.DEL;
          break;
      }
    }

    return type;
  }

  @PermitAll
  public void loadUser(List<ApplicationRevisionInfo> revisionList) {
    if (revisionList != null) {
      for (ApplicationRevisionInfo revision : revisionList) {
        loadUser(revision);
      }
    }
  }

  @PermitAll
  public void loadUser(ApplicationRevisionInfo revision) {
    if (revision != null) {
      String username = revision.getUsername();

      if (username != null) {
        User user = Functions.lookupUserByUsername(username);

        revision.setUser(user);
      }
    }
  }
}
