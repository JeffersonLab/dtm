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
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;

import org.jlab.dtm.persistence.entity.Component;
import org.jlab.dtm.persistence.entity.SystemEntity;
import org.jlab.smoothness.business.util.IOUtil;

/**
 *
 * @author ryans
 */
@Stateless
public class ComponentFacade extends AbstractFacade<Component> {
    @PersistenceContext(unitName = "dtmPU")
    private EntityManager em;

    @EJB
    SystemFacade systemFacade;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public ComponentFacade() {
        super(Component.class);
    }

    @PermitAll
    public List<Component> findByName(String componentName) {
        CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<Component> cq = cb.createQuery(getEntityClass());
        Root<Component> root = cq.from(getEntityClass());        
        
        List<Predicate> filters = new ArrayList<>();        
        
        filters.add(cb.equal(root.get("name"), componentName));
        
        if (!filters.isEmpty()) {
            cq.where(cb.and(filters.toArray(new Predicate[]{})));
        }        
        
        cq.select(root);        
        
        return getEntityManager().createQuery(cq).getResultList();
    }

    @PermitAll
    public Long countMustFilter(BigInteger[] categoryIdArray, BigInteger[] systemIdArray, String q,
                                BigInteger componentId, Integer max) {
        Long count;

        categoryIdArray = IOUtil.removeNullValues(categoryIdArray, BigInteger.class);
        systemIdArray = IOUtil.removeNullValues(systemIdArray, BigInteger.class);

        if (max == null && categoryIdArray == null && systemIdArray == null && (q == null || q.isEmpty())
                && componentId == null) {
            count = 0L; // Return zero if no filter provided
        } else {

            CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
            CriteriaQuery<Long> cq = cb.createQuery(Long.class);
            Root<Component> root = cq.from(Component.class);

            List<Predicate> filters = new ArrayList<>();

            if(categoryIdArray != null && categoryIdArray.length > 0) {
                Set<BigInteger> systemSet = new LinkedHashSet<>();
                for(BigInteger categoryId: categoryIdArray) {
                    List<SystemEntity> systemList = systemFacade.fetchHierarchy(categoryId);
                    for(SystemEntity entity: systemList) {
                        systemSet.add(entity.getSystemId());
                    }
                }

                if(!systemSet.isEmpty()) {
                    if(systemIdArray != null) {
                        systemSet.addAll(Arrays.asList(systemIdArray));
                    }

                    systemIdArray = systemSet.toArray(new BigInteger[]{});
                }
            }

            if (systemIdArray != null && systemIdArray.length > 0) {
                filters.add(root.get("system").in((Object[])systemIdArray));
            }
            if (q != null && !q.isEmpty()) {
                String searchString = q.toUpperCase();

                // auto wild
                searchString = "%" + searchString + "%";

                filters.add(cb.like(cb.upper(root.get("name")), searchString));
            }
            if (componentId != null) {
                filters.add(cb.equal(root.get("componentId"), componentId));
            }
            if (!filters.isEmpty()) {
                cq.where(cb.and(filters.toArray(new Predicate[]{})));
            }

            cq.select(cb.count(root));
            TypedQuery<Long> query = getEntityManager().createQuery(cq);
            count = query.getSingleResult();
        }
        return count;
    }

    @PermitAll
    public List<Component> findMustFilter(BigInteger[] categoryIdArray, BigInteger[] systemIdArray, String q,
                                          BigInteger componentId, Integer max,
                                          Integer offset) {
        List<Component> componentList;

        categoryIdArray = IOUtil.removeNullValues(categoryIdArray, BigInteger.class);
        systemIdArray = IOUtil.removeNullValues(systemIdArray, BigInteger.class);

        if (max == null && categoryIdArray == null && systemIdArray == null && (q == null || q.isEmpty())
                && componentId == null) {
            componentList = new ArrayList<>(); // Return empty list if no filter provided
        } else {
            CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
            CriteriaQuery<Component> cq = cb.createQuery(Component.class);
            Root<Component> root = cq.from(Component.class);
            cq.select(root);

            List<Predicate> filters = new ArrayList<>();

            if(categoryIdArray != null && categoryIdArray.length > 0) {
                Set<BigInteger> systemSet = new LinkedHashSet<>();
                for(BigInteger categoryId: categoryIdArray) {
                    List<SystemEntity> systemList = systemFacade.fetchHierarchy(categoryId);
                    for(SystemEntity entity: systemList) {
                        systemSet.add(entity.getSystemId());
                    }
                }

                if(!systemSet.isEmpty()) {
                    if(systemIdArray != null) {
                        systemSet.addAll(Arrays.asList(systemIdArray));
                    }

                    systemIdArray = systemSet.toArray(new BigInteger[]{});
                }
            }

            if (systemIdArray != null && systemIdArray.length > 0) {
                filters.add(root.get("system").in((Object[])systemIdArray));
            }
            if (q != null && !q.isEmpty()) {
                String searchString = q.toUpperCase();

                // auto wild
                searchString = "%" + searchString + "%";


                Predicate p1 = cb.like(cb.upper(root.get("name")), searchString);
                Predicate p2 = cb.like(cb.upper(root.get("nameAlias")), searchString);

                filters.add(cb.or(p1, p2));
            }
            if (componentId != null) {
                filters.add(cb.equal(root.get("componentId"), componentId));
            }
            if (!filters.isEmpty()) {
                cq.where(cb.and(filters.toArray(new Predicate[]{})));
            }
            List<Order> orders = new ArrayList<>();
            Path p1 = root.get("name");
            Order o1 = cb.asc(p1);
            orders.add(o1);
            cq.orderBy(orders);

            TypedQuery<Component> query = getEntityManager().createQuery(cq);

            if (max != null) {
                query.setMaxResults(max);
            }

            if (offset != null) {
                query.setFirstResult(offset);
            }

            componentList = query.getResultList();
        }

        return componentList;
    }
}
