/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.dtm.business.session;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.security.PermitAll;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.jlab.dtm.persistence.entity.Category;
import org.jlab.dtm.persistence.entity.SystemEntity;
import org.jlab.smoothness.persistence.util.JPAUtil;

/**
 *
 * @author ryans
 */
@Stateless
public class SystemFacade extends AbstractFacade<SystemEntity> {

    @PersistenceContext(unitName = "dtmPU")
    private EntityManager em;
    @EJB
    CategoryFacade categoryFacade;

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
