package org.jlab.dtm.business.session;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.security.PermitAll;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import org.jlab.dtm.persistence.entity.aud.EventAud;

/**
 *
 * @author ryans
 */
@Stateless
public class EventAudFacade extends AbstractFacade<EventAud> {
    @PersistenceContext(unitName = "dtmPU")
    private EntityManager em;

    @EJB
    ApplicationRevisionInfoFacade revisionFacade;
    
    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public EventAudFacade() {
        super(EventAud.class);
    }

    @PermitAll
    public List<EventAud> filterList(BigInteger eventId, BigInteger revisionId, int offset, int max) {
        CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<EventAud> cq = cb.createQuery(EventAud.class);
        Root<EventAud> root = cq.from(EventAud.class);
        cq.select(root);

        List<Predicate> filters = new ArrayList<Predicate>();

        if (eventId != null) {            
            filters.add(cb.equal(root.get("eventAudPK").get("eventId"), eventId));
        }

        if (revisionId != null) {
            filters.add(cb.equal(root.get("revision").get("id"), revisionId));            
        }

        if (!filters.isEmpty()) {
            cq.where(cb.and(filters.toArray(new Predicate[]{})));
        }
        List<Order> orders = new ArrayList<Order>();
        Path p0 = root.get("revision").get("id");
        Order o0 = cb.asc(p0);
        orders.add(o0);
        cq.orderBy(orders);

        List<EventAud> eventList = getEntityManager().createQuery(cq).setFirstResult(offset).setMaxResults(max).getResultList();
        
        if(eventList != null) {
            for(EventAud event: eventList) {
                event.getRevision().getId(); // Tickle to load
            }
        }
        
        return eventList;
    }

    // there is a bug in hibernate which causes generated sql to be invalid for composite select count
    /*public Long countFilterList(BigInteger eventId, BigInteger revisionId) {
        CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<EventAud> root = cq.from(EventAud.class);

        List<Predicate> filters = new ArrayList<Predicate>();

        if (eventId != null) {            
            filters.add(cb.equal(root.get("eventAudPK").get("eventId"), eventId));
        }

        if (revisionId != null) {
            filters.add(cb.equal(root.get("revision").get("id"), revisionId));            
        }
        
        if (!filters.isEmpty()) {
            cq.where(cb.and(filters.toArray(new Predicate[]{})));
        }

        cq.select(cb.count(root));
        TypedQuery<Long> q = getEntityManager().createQuery(cq);
        return q.getSingleResult();
    }    */

    @PermitAll
    public Long countFilterList(BigInteger eventId, BigInteger revisionId) {
        String selectFrom = "select count(*) from EVENT_AUD e ";

        List<String> whereList = new ArrayList<String>();

        String w;

        if (eventId != null) {
            w = "e.event_id = " + eventId;
            whereList.add(w);
        }

        if (revisionId != null) {
            w = "e.rev = " + revisionId;
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
        Query q = em.createNativeQuery(sql); 
        
        return ((Number)q.getSingleResult()).longValue();
    }

    @PermitAll
    public void loadStaff(List<EventAud> eventList) {
        if(eventList != null) {
            for(EventAud event: eventList) {
                revisionFacade.loadStaff(event.getRevision());
            }
        }
    }

    @PermitAll
    public EventAud findLatestCloseRevision(BigInteger eventId) {
        CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<EventAud> cq = cb.createQuery(EventAud.class);
        Root<EventAud> root = cq.from(EventAud.class);
        cq.select(root);

        List<Predicate> filters = new ArrayList<Predicate>();

        // Find EventAud where timeUp is not null
        filters.add(cb.isNotNull(root.get("timeUp")));
        
        // Find EventAud where change was a ADD or MOD (not DEL)
        filters.add(cb.notEqual(root.get("type"), 3));
        
        
        if (eventId != null) {            
            filters.add(cb.equal(root.get("eventAudPK").get("eventId"), eventId));
        }

        if (!filters.isEmpty()) {
            cq.where(cb.and(filters.toArray(new Predicate[]{})));
        }
        List<Order> orders = new ArrayList<Order>();
        Path p0 = root.get("revision").get("id");
        Order o0 = cb.asc(p0);
        orders.add(o0);
        cq.orderBy(orders);

        List<EventAud> eventList = getEntityManager().createQuery(cq).setFirstResult(0).setMaxResults(1).getResultList();
        
        EventAud aud = null;
        
        if(eventList != null && !eventList.isEmpty()) {
            aud = eventList.get(0);
            aud.getRevision().getId(); // tickle
        }
        
        return aud;
    }
}
