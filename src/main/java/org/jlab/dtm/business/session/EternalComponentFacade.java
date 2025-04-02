package org.jlab.dtm.business.session;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.security.PermitAll;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
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
