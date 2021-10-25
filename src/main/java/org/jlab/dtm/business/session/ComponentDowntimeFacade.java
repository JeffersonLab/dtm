package org.jlab.dtm.business.session;

import java.math.BigInteger;
import java.util.Date;
import java.util.List;
import javax.annotation.security.PermitAll;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import org.jlab.dtm.persistence.entity.Component;
import org.jlab.dtm.persistence.entity.EventType;
import org.jlab.dtm.persistence.model.ComponentDowntime;
import org.jlab.smoothness.persistence.util.JPAUtil;

/**
 *
 * @author ryans
 */
@Stateless
public class ComponentDowntimeFacade extends AbstractFacade<Component> {

    @PersistenceContext(unitName = "dtmPU")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public ComponentDowntimeFacade() {
        super(Component.class);
    }

    @PermitAll
    public List<ComponentDowntime> findByPeriodAndType(Date start, Date end, EventType type, Boolean beamTransport, BigInteger systemId) {
        String sql =
                "select a.component_id, a.name, d.name as systemName, count(a.name) as incident_count, sum(interval_to_seconds(least(nvl(b.time_up, sysdate), :end) - greatest(b.time_down, :start))) / 60 / 60 / 24 as duration "
                + "from hco_owner.all_components a, incident b, event c, hco_owner.all_systems d "
                + "where a.component_id = b.component_id "
                + "and a.system_id = d.system_id "
                + "and b.event_id = c.event_id "
                + "and b.time_down < :end "
                + "and nvl(b.time_up, sysdate) >= :start ";

        if(type != null) {
            sql = sql + "and c.event_type_id = " + type.getEventTypeId() + " ";
        }

        // beamTransport Y = only beam transport
        // beamTransport N = everything but beam transport
        // Null means don't filter beam transport specially
        if (beamTransport != null) {
            if (beamTransport) {
                sql = sql
                        + "and b.system_id = (select system_id from hco_owner.all_systems where name = 'Beam Transport') ";
            }
            if (!beamTransport) {
                sql = sql
                        + "and b.system_id != (select system_id from hco_owner.all_systems where name = 'Beam Transport') ";
            }
        }        
        
        if (systemId != null) {
            sql = sql + "and a.system_id = :systemId ";
        }

        sql = sql + "group by a.component_id, d.name, a.name order by duration desc";


        Query q = em.createNativeQuery(sql);

        q.setParameter("start", start);
        q.setParameter("end", end);

        if (systemId != null) {
            q.setParameter("systemId", systemId);
        }

        List<ComponentDowntime> downtimeList = JPAUtil.getResultList(q, ComponentDowntime.class);

        return downtimeList;
    }
}
