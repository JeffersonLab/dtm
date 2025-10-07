package org.jlab.dtm.business.session;

import jakarta.annotation.security.PermitAll;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import java.math.BigInteger;
import java.util.List;
import org.jlab.dtm.persistence.entity.IncidentReview;

/**
 * @author ryans
 */
@Stateless
public class IncidentReviewFacade extends AbstractFacade<IncidentReview> {

  @PersistenceContext(unitName = "webappPU")
  private EntityManager em;

  @Override
  protected EntityManager getEntityManager() {
    return em;
  }

  public IncidentReviewFacade() {
    super(IncidentReview.class);
  }

  @PermitAll
  public List<IncidentReview> findByIncident(BigInteger incidentId) {
    TypedQuery<IncidentReview> q =
        em.createQuery(
            "select r from IncidentReview r where r.incident.incidentId = :incidentId",
            IncidentReview.class);
    q.setParameter("incidentId", incidentId);
    return q.getResultList();
  }
}
