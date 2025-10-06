package org.jlab.dtm.business.session;

import jakarta.annotation.security.PermitAll;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import java.math.BigInteger;
import java.util.List;
import org.jlab.dtm.persistence.entity.Repair;

/**
 * @author ryans
 */
@Stateless
public class RepairFacade extends AbstractFacade<Repair> {

  @PersistenceContext(unitName = "webappPU")
  private EntityManager em;

  @Override
  protected EntityManager getEntityManager() {
    return em;
  }

  public RepairFacade() {
    super(Repair.class);
  }

  @PermitAll
  public List<Repair> findByIncident(BigInteger incidentId) {
    TypedQuery<Repair> q =
        em.createQuery(
            "select r from Repair r left join fetch r.repairedBy where r.incident.incidentId = :incidentId",
            Repair.class);
    q.setParameter("incidentId", incidentId);
    return q.getResultList();
  }
}
