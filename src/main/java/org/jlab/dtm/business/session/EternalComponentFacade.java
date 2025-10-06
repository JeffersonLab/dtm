package org.jlab.dtm.business.session;

import jakarta.annotation.security.PermitAll;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;
import org.jlab.dtm.persistence.entity.EternalComponent;

/**
 * @author ryans
 */
@Stateless
public class EternalComponentFacade extends AbstractFacade<EternalComponent> {
  @PersistenceContext(unitName = "webappPU")
  private EntityManager em;

  @Override
  protected EntityManager getEntityManager() {
    return em;
  }

  public EternalComponentFacade() {
    super(EternalComponent.class);
  }

  @PermitAll
  public List<EternalComponent> findByName(String componentName) {
    CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
    CriteriaQuery<EternalComponent> cq = cb.createQuery(getEntityClass());
    Root<EternalComponent> root = cq.from(getEntityClass());

    List<Predicate> filters = new ArrayList<>();

    filters.add(cb.equal(root.get("name"), componentName));

    if (!filters.isEmpty()) {
      cq.where(cb.and(filters.toArray(new Predicate[] {})));
    }

    cq.select(root);

    return getEntityManager().createQuery(cq).getResultList();
  }
}
