package org.jlab.dtm.business.session;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.security.PermitAll;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.*;
import org.jlab.dtm.persistence.entity.EventType;
import org.jlab.dtm.persistence.enumeration.Include;

/**
 * @author ryans
 */
@Stateless
public class EventTypeFacade extends AbstractFacade<EventType> {
  @PersistenceContext(unitName = "webappPU")
  private EntityManager em;

  @Override
  protected EntityManager getEntityManager() {
    return em;
  }

  public EventTypeFacade() {
    super(EventType.class);
  }

  private List<Predicate> getFilters(
      CriteriaBuilder cb,
      CriteriaQuery<? extends Object> cq,
      Root<EventType> root,
      Include archived) {
    List<Predicate> filters = new ArrayList<>();

    if (archived == null) {
      filters.add(cb.equal(root.get("archived"), false));
    } else if (Include.EXCLUSIVELY == archived) {
      filters.add(cb.equal(root.get("archived"), true));
    } // else Include.YES, which means don't filter at all

    return filters;
  }

  @PermitAll
  public List<EventType> filterList(Include archived) {
    CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
    CriteriaQuery<EventType> cq = cb.createQuery(EventType.class);
    Root<EventType> root = cq.from(EventType.class);
    cq.select(root);

    List<Predicate> filters = getFilters(cb, cq, root, archived);

    if (!filters.isEmpty()) {
      cq.where(cb.and(filters.toArray(new Predicate[] {})));
    }

    List<Order> orders = new ArrayList<>();
    Path p0 = root.get("weight");
    Order o0 = cb.asc(p0);
    orders.add(o0);
    cq.orderBy(orders);
    return getEntityManager().createQuery(cq).getResultList();
  }
}
