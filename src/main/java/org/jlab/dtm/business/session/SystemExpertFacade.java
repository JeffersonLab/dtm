package org.jlab.dtm.business.session;

import java.math.BigInteger;
import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.jlab.dtm.persistence.entity.SystemEntity;
import org.jlab.dtm.persistence.entity.SystemExpert;
import org.jlab.smoothness.business.exception.UserFriendlyException;

/**
 *
 * @author ryans
 */
@Stateless
public class SystemExpertFacade extends AbstractFacade<SystemExpert> {

    @PersistenceContext(unitName = "dtmPU")
    private EntityManager em;

    @EJB
    SystemFacade systemFacade;
    
    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public SystemExpertFacade() {
        super(SystemExpert.class);
    }
    
    @RolesAllowed("dtreview")
    public BigInteger add(BigInteger systemId, String username) throws UserFriendlyException {
        if(systemId == null) {
            throw new UserFriendlyException("Subsystem must be specified");
        }
        
        if(username == null || username.isEmpty()) {
            throw new UserFriendlyException("Username must be specified");
        }
        
        SystemEntity system = systemFacade.find(systemId);
        
        if(system == null) {
            throw new UserFriendlyException("Subsytem with ID: " + systemId + " not found");
        }
        
        SystemExpert se = new SystemExpert();
        
        se.setSystem(system);        
        se.setUsername(username);
        
        se = em.merge(se);
        
        return se.getSystemExpertId();
    }

    @RolesAllowed("dtreview")
    public void delete(BigInteger expertId) throws UserFriendlyException {
        if(expertId == null) {
            throw new UserFriendlyException("Expert ID must be specified");
        }
        
        SystemExpert se = find(expertId);
        
        if(se == null) {
            throw new UserFriendlyException("Expert record with ID: " + expertId + " not found");
        }
        
        em.remove(se);
    }
}
