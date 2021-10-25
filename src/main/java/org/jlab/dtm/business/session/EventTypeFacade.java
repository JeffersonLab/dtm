package org.jlab.dtm.business.session;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.jlab.dtm.persistence.entity.EventType;

/**
 *
 * @author ryans
 */
@Stateless
public class EventTypeFacade extends AbstractFacade<EventType> {
    @PersistenceContext(unitName = "dtmPU")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public EventTypeFacade() {
        super(EventType.class);
    }
    
}
