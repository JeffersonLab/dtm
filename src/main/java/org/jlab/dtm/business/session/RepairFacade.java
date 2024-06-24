package org.jlab.dtm.business.session;

import java.math.BigInteger;
import java.util.List;
import javax.annotation.security.PermitAll;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import org.jlab.dtm.persistence.entity.Repair;

/**
 * @author ryans
 */
@Stateless
public class RepairFacade extends AbstractFacade<Repair> {

  @PersistenceContext(unitName = "dtmPU")
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
