package org.jlab.dtm.business.session;

import jakarta.annotation.security.PermitAll;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jlab.dtm.persistence.entity.Event;
import org.jlab.dtm.persistence.entity.EventType;
import org.jlab.dtm.persistence.model.EventDowntime;
import org.jlab.smoothness.persistence.util.JPAUtil;

/**
 * @author ryans
 */
@Stateless
public class EventDowntimeFacade extends AbstractFacade<Event> {

  private static final Logger LOGGER = Logger.getLogger(EventDowntimeFacade.class.getName());

  @PersistenceContext(unitName = "webappPU")
  private EntityManager em;

  @Override
  protected EntityManager getEntityManager() {
    return em;
  }

  public EventDowntimeFacade() {
    super(Event.class);
  }

  @PermitAll
  public List<EventDowntime> findByPeriodAndTypeSortByDuration(
      Date start, Date end, EventType type, Boolean beamTransport) {
    String sql =
        "select x.event_id, (x.duration / 60 / 60) as downtime_hours, (nvl(y.restore_bounded, 0) / 60 / 60) as restore_hours_bounded, x.number_of_incidents, x.event_title as title, cast(x.time_down as date), cast(x.time_up as date), cast(x.time_down_bounded as date), cast(x.time_up_bounded as date), (x.duration_bounded / 60 / 60) as downtime_hours_bounded from "
            + "("
            + "select a.event_id, interval_to_seconds(nvl(a.time_up, sysdate) - a.time_down) as duration, a.number_of_incidents, a.event_title, a.time_down, a.time_up, greatest(a.time_down, :start) as time_down_bounded, least(nvl(a.time_up, sysdate), :end) as time_up_bounded, interval_to_seconds(least(nvl(a.time_up, sysdate), :end) - greatest(a.time_down, :start)) as duration_bounded "
            + "from event_first_incident a "
            + "where a.event_type_id = :type "
            + "and a.time_down < :end "
            + "and nvl(a.time_up, sysdate) >= :start "
            + ") x left outer join "
            + "("
            + "select z.event_id, sum(interval_to_seconds(least(nvl(z.time_up, sysdate), :end) - greatest(z.time_down, :start))) as restore_bounded "
            + "from restore_time z "
            + "where "
            + "z.time_down < :end "
            + "and nvl(z.time_up, sysdate) >= :start "
            + "group by z.event_id "
            + ") y "
            + "on x.event_id = y.event_id ";

    // beamTransport Y = only beam transport
    // beamTransport N = everything but beam transport
    // Null means don't filter beam transport specially
    if (beamTransport != null) {
      if (beamTransport) {
        sql =
            sql
                + "where x.event_id in (select event_id from incident where system_id in (select system_id from dtm_owner.system_alpha_category join dtm_owner.category using (category_id) where category.name = 'Beam Transport')) ";
      } else {
        sql =
            sql
                + "where x.event_id not in (select event_id from incident where system_id in (select system_id from dtm_owner.system_alpha_category join dtm_owner.category using (category_id) where category.name = 'Beam Transport')) ";
      }
    }

    sql = sql + "order by x.duration_bounded desc";

    LOGGER.log(Level.FINEST, "Query: {0}", sql);

    Query q = em.createNativeQuery(sql);

    q.setParameter("start", start);
    q.setParameter("end", end);
    q.setParameter("type", type.getEventTypeId());

    List<EventDowntime> downtimeList = JPAUtil.getResultList(q, EventDowntime.class);

    return downtimeList;
  }

  @PermitAll
  public double downtimeTotal(Date start, Date end, EventType type) {
    List<EventDowntime> downtimeList = findByPeriodAndTypeSortByDuration(start, end, type, null);

    double eventDowntime = 0;

    for (int i = 0; i < downtimeList.size(); i++) {
      EventDowntime downtime = downtimeList.get(i);
      eventDowntime = eventDowntime + downtime.getDowntimeHoursBounded();
    }

    return eventDowntime;
  }
}
