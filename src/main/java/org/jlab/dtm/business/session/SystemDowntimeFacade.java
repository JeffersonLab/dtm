package org.jlab.dtm.business.session;

import java.math.BigInteger;
import java.util.Date;
import java.util.List;
import javax.annotation.security.PermitAll;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import org.jlab.dtm.persistence.entity.EventType;
import org.jlab.dtm.persistence.entity.SystemEntity;
import org.jlab.dtm.persistence.model.SystemDowntime;
import org.jlab.smoothness.persistence.util.JPAUtil;

/**
 * @author ryans
 */
@Stateless
public class SystemDowntimeFacade extends AbstractFacade<SystemEntity> {

  @PersistenceContext(unitName = "webappPU")
  private EntityManager em;

  @Override
  protected EntityManager getEntityManager() {
    return em;
  }

  public SystemDowntimeFacade() {
    super(SystemEntity.class);
  }

  @PermitAll
  public List<SystemDowntime> findByPeriodAndType(
      Date start,
      Date end,
      EventType type,
      Boolean beamTransport,
      BigInteger categoryId,
      boolean packed) {

    // Note: If you don't choose an event_type_id (ACC, HALL A, etc.) then the packed interval still
    // won't pack incidents from different types

    String sql =
        "select a.name, a.system_id, count(a.name) as incident_count, sum(interval_to_seconds(least(nvl(b.time_up, sysdate), :end) - greatest(b.time_down, :start))) / 60 / 60 / 24 as duration "
            + "from dtm_owner.system a, ";

    if (packed) {
      sql = sql + "system_packed_incidents b, ";
    } else {
      sql = sql + "incident b inner join event c using(event_id), ";
    }

    sql =
        sql
            + "system_alpha_category d "
            + "where a.system_id = b.system_id "
            + "and b.time_down < :end "
            + "and nvl(b.time_up, sysdate) >= :start "
            + "and a.system_id = d.system_id ";

    if (type != null) {
      sql = sql + "and event_type_id = " + type.getEventTypeId() + " ";
    }

    // beamTransport Y = only beam transport
    // beamTransport N = everything but beam transport
    // Null means don't filter beam transport specially
    if (beamTransport != null) {
      if (beamTransport) {
        sql =
            sql
                + "and b.system_id = (select system_id from dtm_owner.system where name = 'Beam Transport') ";
      }
      if (!beamTransport) {
        sql =
            sql
                + "and b.system_id != (select system_id from dtm_owner.system where name = 'Beam Transport') ";
      }
    }

    if (categoryId != null) {
      sql = sql + "and d.category_id = :categoryId ";
    }

    sql = sql + "group by a.name, a.system_id order by duration desc";

    Query q = em.createNativeQuery(sql);

    q.setParameter("start", start);
    q.setParameter("end", end);

    if (categoryId != null) {
      q.setParameter("categoryId", categoryId);
    }

    List<SystemDowntime> downtimeList = JPAUtil.getResultList(q, SystemDowntime.class);

    return downtimeList;
  }
}
