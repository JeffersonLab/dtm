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
import org.jlab.dtm.persistence.entity.aud.IncidentAud;

/**
 * @author ryans
 */
@Stateless
public class IncidentAudFacade extends AbstractFacade<IncidentAud> {
  @PersistenceContext(unitName = "dtmPU")
  private EntityManager em;

  @EJB ApplicationRevisionInfoFacade revisionFacade;

  @Override
  protected EntityManager getEntityManager() {
    return em;
  }

  public IncidentAudFacade() {
    super(IncidentAud.class);
  }

  @PermitAll
  public List<IncidentAud> filterList(
      BigInteger incidentId, BigInteger revisionId, int offset, int max) {
    CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
    CriteriaQuery<IncidentAud> cq = cb.createQuery(IncidentAud.class);
    Root<IncidentAud> root = cq.from(IncidentAud.class);
    cq.select(root);

    List<Predicate> filters = new ArrayList<Predicate>();

    if (incidentId != null) {
      filters.add(cb.equal(root.get("incidentAudPK").get("incidentId"), incidentId));
    }

    if (revisionId != null) {
      filters.add(cb.equal(root.get("revision").get("id"), revisionId));
    }

    if (!filters.isEmpty()) {
      cq.where(cb.and(filters.toArray(new Predicate[] {})));
    }
    List<Order> orders = new ArrayList<Order>();
    Path p0 = root.get("revision").get("id");
    Order o0 = cb.asc(p0);
    orders.add(o0);
    cq.orderBy(orders);

    List<IncidentAud> incidentList =
        getEntityManager()
            .createQuery(cq)
            .setFirstResult(offset)
            .setMaxResults(max)
            .getResultList();

    if (incidentList != null) {
      for (IncidentAud incident : incidentList) {
        incident.getRevision().getId(); // Tickle to load
      }
    }

    return incidentList;
  }

  @PermitAll
  public Long countFilterList(BigInteger incidentId, BigInteger revisionId) {
    String selectFrom = "select count(*) from INCIDENT_AUD e ";

    List<String> whereList = new ArrayList<String>();

    String w;

    if (incidentId != null) {
      w = "e.incident_id = " + incidentId;
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

    return ((Number) q.getSingleResult()).longValue();
  }

  @PermitAll
  public void loadStaff(List<IncidentAud> incidentList) {
    if (incidentList != null) {
      for (IncidentAud incident : incidentList) {
        revisionFacade.loadUser(incident.getRevision());
      }
    }
  }
}
