/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.dtm.business.session;

import java.math.BigInteger;
import java.util.*;
import javax.annotation.security.PermitAll;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.*;
import org.jlab.dtm.persistence.entity.Category;
import org.jlab.dtm.persistence.entity.EternalComponent;
import org.jlab.dtm.persistence.entity.SystemEntity;
import org.jlab.smoothness.persistence.util.JPAUtil;

/**
 * @author ryans
 */
@Stateless
public class SystemFacade extends AbstractFacade<SystemEntity> {

  @PersistenceContext(unitName = "dtmPU")
  private EntityManager em;

  @EJB CategoryFacade categoryFacade;

  @Override
  protected EntityManager getEntityManager() {
    return em;
  }

  public SystemFacade() {
    super(SystemEntity.class);
  }

  @PermitAll
  public List<SystemEntity> findAllWithCategory() {
    categoryFacade.findAllViaCartesianProduct(); // Put all categories into em cache

    return findAll(new OrderDirective("name"));
  }

  @PermitAll
  public List<SystemEntity> findWithCategory(BigInteger categoryId) {
    List<SystemEntity> systemList;

    if (categoryId == null) {
      systemList = findAllWithCategory();
    } else {
      // Just load entire hierarchy of categories and cache in em
      categoryFacade.findAllViaCartesianProduct();

      systemList = fetchHierarchy(categoryId);
    }

    return systemList;
  }

  @PermitAll
  public List<SystemEntity> findWithCategory(
      BigInteger[] categoryIdArray, BigInteger componentId, BigInteger systemId) {
    List<SystemEntity> systemList = new ArrayList<>();

    // Just load entire hierarchy of categories and cache in em
    categoryFacade.findAllViaCartesianProduct();

    if (categoryIdArray != null && (categoryIdArray.length == 0 || categoryIdArray[0] == null)) {
      categoryIdArray = null;
    }

    // If searching by component, or by system_id, or for all systems (no filter)
    if (componentId != null
        || systemId != null
        || (componentId == null && systemId == null && categoryIdArray == null)) {

      // Zero or one (first) category are used if searching by componentId or systemId
      BigInteger categoryId = categoryIdArray == null ? null : categoryIdArray[0];

      systemList = findByComponentCategoryAndSystem(componentId, categoryId, systemId);
    } else {
      // componentId == null && systemId == null && categoryId != null && recurse == true
      // Search for systems by category using recursion

      Set<SystemEntity> systemSet = new LinkedHashSet<>(); // Prevent duplicates
      for (BigInteger categoryId : categoryIdArray) {
        if (categoryId != null) {
          systemSet.addAll(fetchHierarchy(categoryId));
        }
      }
      systemList.addAll(systemSet);
    }

    return systemList;
  }

  @PermitAll
  public List<SystemEntity> findByComponentCategoryAndSystem(
      BigInteger componentId, BigInteger categoryId, BigInteger systemId) {
    CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
    CriteriaQuery<SystemEntity> cq = cb.createQuery(SystemEntity.class);
    Root<SystemEntity> root = cq.from(SystemEntity.class);
    cq.select(root);

    List<Predicate> filters = new ArrayList<>();

    if (componentId != null) {
      Join<SystemEntity, EternalComponent> components = root.join("componentList");
      filters.add(components.in(componentId));
    }
    if (categoryId != null) {
      filters.add(cb.equal(root.get("category").get("categoryId"), categoryId));
    }
    if (systemId != null) {
      filters.add(cb.equal(root.get("systemId"), systemId));
    }
    if (!filters.isEmpty()) {
      cq.where(cb.and(filters.toArray(new Predicate[] {})));
    }
    List<Order> orders = new ArrayList<>();
    Path p0 = root.get("name");
    Order o0 = cb.asc(p0);
    orders.add(o0);
    cq.orderBy(orders);

    return getEntityManager().createQuery(cq).getResultList();
  }

  @PermitAll
  public List<SystemEntity> fetchHierarchy(BigInteger categoryId) {
    List<SystemEntity> systemList;

    Category category = categoryFacade.find(categoryId);
    if (category != null) {
      systemList = gatherDescendents(category);
      Collections.sort(systemList);
    } else {
      systemList = new ArrayList<>();
    }

    return systemList;
  }

  @PermitAll
  public List<SystemEntity> gatherDescendents(Category category) {
    List<SystemEntity> systemList;

    if (category.getSystemList() == null || category.getSystemList().isEmpty()) {
      systemList = new ArrayList<>();
    } else {
      systemList = new ArrayList<>();
      for (SystemEntity system : category.getSystemList()) {
        systemList.add(system);
      }
    }

    if (category.getCategoryList() != null && !category.getCategoryList().isEmpty()) {
      for (Category child : category.getCategoryList()) {
        systemList.addAll(gatherDescendents(child));
      }
    }

    return systemList;
  }

  @PermitAll
  public SystemEntity findWithExpertList(BigInteger systemId) {
    SystemEntity system = find(systemId);

    if (system != null) {
      JPAUtil.initialize(system.getSystemExpertList());
    }

    return system;
  }

  @PermitAll
  public List<SystemEntity> findAllWithExpertList() {
    List<SystemEntity> systemList = this.findAll(new OrderDirective("name"));

    for (SystemEntity s : systemList) {
      JPAUtil.initialize(s.getSystemExpertList());
    }

    return systemList;
  }
}
