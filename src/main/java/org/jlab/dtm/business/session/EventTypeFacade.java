package org.jlab.dtm.business.session;

import jakarta.annotation.security.PermitAll;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import java.util.*;
import org.jlab.dtm.persistence.entity.Category;
import org.jlab.dtm.persistence.entity.EventType;
import org.jlab.dtm.persistence.enumeration.Include;

/**
 * @author ryans
 */
@Stateless
public class EventTypeFacade extends AbstractFacade<EventType> {
  @PersistenceContext(unitName = "webappPU")
  private EntityManager em;

  @EJB CategoryFacade categoryFacade;

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

  @PermitAll
  public List<EventType> findActiveWithCategories() {
    String str =
        "SELECT distinct e FROM EventType e JOIN FETCH e.categoryList where e.archived = false order by e.weight asc";
    TypedQuery<EventType> q = em.createQuery(str, EventType.class);
    List<EventType> typeList = q.getResultList();

    for (EventType type : typeList) {
      type.getCategoryList()
          .sort(
              new Comparator<Category>() {
                @Override
                public int compare(Category o1, Category o2) {
                  return o1.getName().compareTo(o2.getName());
                }
              });
    }

    return typeList;
  }

  @PermitAll
  public Set<Category> getRootCacheSet(List<EventType> eventTypeList) {
    Set<Category> rootCacheSet = new HashSet<>();
    for (EventType type : eventTypeList) {
      for (Category category : type.getCategoryList()) {
        Category withDescendentsLoaded = categoryFacade.findBranch(category.getCategoryId());
        rootCacheSet.add(withDescendentsLoaded);
      }
    }

    return rootCacheSet;
  }
}
