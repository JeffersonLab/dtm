package org.jlab.dtm.business.session;

import java.math.BigInteger;
import java.util.List;
import javax.annotation.security.PermitAll;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import org.jlab.dtm.persistence.entity.Workgroup;

/**
 * @author ryans
 */
@Stateless
public class ResponsibleGroupFacade extends AbstractFacade<Workgroup> {

  @PersistenceContext(unitName = "dtmPU")
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
}
