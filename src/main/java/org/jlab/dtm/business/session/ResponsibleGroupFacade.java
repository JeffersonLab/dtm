package org.jlab.dtm.business.session;

import java.math.BigInteger;
import java.util.List;
import javax.annotation.security.PermitAll;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import org.jlab.dtm.persistence.entity.ResponsibleGroup;
/**
 *
 * @author ryans
 */
@Stateless
public class ResponsibleGroupFacade extends AbstractFacade<ResponsibleGroup> {

    @PersistenceContext(unitName = "dtmPU")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public ResponsibleGroupFacade() {
        super(ResponsibleGroup.class);
    }

    @SuppressWarnings("unchecked")
    @PermitAll
    public List<ResponsibleGroup> findBySystem(BigInteger systemId) {
        if (systemId == null) {
            return findAll(new OrderDirective("name"));
        }

        Query q = em.createNativeQuery("select a.* from responsible_group a, group_responsibility b where a.group_id = b.group_id and b.system_id = :systemId order by b.weight, a.name asc", ResponsibleGroup.class);

        q.setParameter("systemId", systemId);

        return q.getResultList();
    }

    @SuppressWarnings("unchecked")
    @PermitAll
    public List<ResponsibleGroup> findRepairedBy(BigInteger incidentId) {
        Query q = em.createNativeQuery("select * from responsible_group where group_id in (select repaired_by from incident_repair where incident_id  = :incidentId)", ResponsibleGroup.class);

        q.setParameter("incidentId", incidentId);

        return q.getResultList();
    }
}
