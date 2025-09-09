package org.jlab.dtm.business.session;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.security.PermitAll;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.criteria.*;
import org.jlab.dtm.persistence.entity.Workgroup;

/**
 * @author ryans
 */
@Stateless
public class ResponsibleGroupFacade extends AbstractFacade<Workgroup> {

  @PersistenceContext(unitName = "webappPU")
  private EntityManager em;

  @Override
  protected EntityManager getEntityManager() {
    return em;
  }

  public ResponsibleGroupFacade() {
    super(Workgroup.class);
  }

  @SuppressWarnings("unchecked")
  @PermitAll
  public List<Workgroup> findRepairedBy(BigInteger incidentId) {
    Query q =
        em.createNativeQuery(
            "select * from dtm_owner.workgroup where workgroup_id in (select repaired_by from incident_repair where incident_id  = :incidentId)",
            Workgroup.class);

    q.setParameter("incidentId", incidentId);

    return q.getResultList();
  }

  @PermitAll
  public List<Workgroup> findActive() {
    CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
    CriteriaQuery<Workgroup> cq = cb.createQuery(Workgroup.class);
    Root<Workgroup> root = cq.from(Workgroup.class);
    cq.select(root);

    List<Predicate> filters = new ArrayList<>();

    filters.add(cb.equal(root.get("archived"), false));

    cq.where(cb.and(filters.toArray(new Predicate[] {})));

    List<Order> orders = new ArrayList<>();
    Path<String> p0 = root.get("name");
    Order o0 = cb.asc(p0);
    orders.add(o0);
    cq.orderBy(orders);
    return getEntityManager().createQuery(cq).getResultList();
  }
}
