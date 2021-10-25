package org.jlab.dtm.business.session;

import java.util.List;
import javax.annotation.security.PermitAll;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import org.jlab.dtm.persistence.entity.Staff;

/**
 *
 * @author ryans
 */
@Stateless
public class StaffFacade extends AbstractFacade<Staff> {
    @PersistenceContext(unitName = "dtmPU")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public StaffFacade() {
        super(Staff.class);
    }

    @PermitAll
    public Staff findByUsername(String username) {
        TypedQuery<Staff> q = em.createQuery("select s from Staff s where username = :username", Staff.class);

        q.setParameter("username", username);

        Staff staff = null;

        List<Staff> resultList = q.getResultList();
        
        if (resultList != null && !resultList.isEmpty()) {
            staff = resultList.get(0);
        }

        return staff;
    }    
}
