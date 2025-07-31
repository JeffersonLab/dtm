package org.jlab.dtm.business.session;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;
import javax.annotation.security.PermitAll;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import org.jlab.dtm.persistence.entity.EventType;
import org.jlab.dtm.persistence.entity.Incident;
import org.jlab.dtm.persistence.model.CategoryTrendInfo;
import org.jlab.smoothness.business.util.DateIterator;
import org.jlab.smoothness.business.util.IOUtil;
import org.jlab.smoothness.business.util.TimeUtil;
import org.jlab.smoothness.persistence.util.JPAUtil;

/**
 * @author ryans
 */
@Stateless
public class IncidentTrendFacade extends AbstractFacade<Incident> {

  private static final Logger logger = Logger.getLogger(IncidentTrendFacade.class.getName());

  @PersistenceContext(unitName = "webappPU")
  private EntityManager em;

  @Override
  protected EntityManager getEntityManager() {
    return em;
  }

  public IncidentTrendFacade() {
    super(Incident.class);
  }

  @PermitAll
  public List<CategoryTrendInfo> findTrendByDate(
      Date day,
      EventType type,
      boolean includeBeamTransport,
      boolean groupByCategory,
      BigInteger[] categoryIdArray) {
    String sql =
        "select :start as day_date, "
            + "count(b.incident_id) as incident_count, "
            + "nvl(sum(interval_to_seconds(least(nvl(b.time_up, sysdate), :end) - greatest(b.time_down, :start))) / 60 / 60 / 24, 0) as duration ";

    if (groupByCategory) {
      sql = sql + ", e.name as category_name ";
    } else {
      sql = sql + ", '' as category_name ";
    }

    sql =
        sql
            + "from "
            + "incident b, event c, system_alpha_category d, category e "
            + "where "
            + "b.system_id = d.system_id "
            + "and b.event_id = c.event_id "
            + "and d.category_id = e.category_id "
            + "and b.time_down < :end "
            + "and nvl(b.time_up, sysdate) >= :start ";

    if (type != null) {
      sql = sql + "and c.event_type_id = " + type.getEventTypeId() + " ";
    }

    if (!includeBeamTransport) {
      sql =
          sql
              + "and d.system_id not in (select system_id from dtm_owner.system_alpha_category join dtm_owner.category using (category_id) where category.name = 'Beam Transport') ";
    }

    String categoryCsv = IOUtil.toCsv(categoryIdArray);

    if (categoryCsv != null && !categoryCsv.isEmpty()) {
      sql = sql + "and d.category_id in (" + categoryCsv + ") ";
    }

    if (groupByCategory) {
      sql = sql + "group by e.name ";

      String categoryQualifier = "";

      if (categoryCsv != null && !categoryCsv.isEmpty()) {
        categoryQualifier = "and x.category_id in (" + categoryCsv + ")";
      }

      sql =
          "select day_date, sum(incident_count), sum(duration), category_name from (("
              + sql
              + ") union all (select :start as day_date, 0 as incident_count, 0 as duration, category_name from (select distinct(y.name) as category_name from system_alpha_category x, category y where x.category_id = y.category_id "
              + categoryQualifier
              + "))) group by day_date, category_name ";
    }

    Query q = em.createNativeQuery(sql);

    Date start = TimeUtil.startOfDay(day, Calendar.getInstance());
    Date end = TimeUtil.addDays(start, 1);

    q.setParameter("start", start);
    q.setParameter("end", end);

    return JPAUtil.getResultList(q, CategoryTrendInfo.class);
  }

  @PermitAll
  public List<CategoryTrendInfo> findTrendListByPeriod(
      Date start,
      Date end,
      EventType type,
      boolean includeBeamTransport,
      boolean groupByCategory,
      int interval,
      BigInteger[] categoryIdArray) {

    List<CategoryTrendInfo> trendList = new ArrayList<CategoryTrendInfo>();

    DateIterator iterator = new DateIterator(start, end, Calendar.DATE);

    for (Date date : iterator) {
      trendList.addAll(
          findTrendByDate(date, type, includeBeamTransport, groupByCategory, categoryIdArray));
    }

    return trendList;
  }
}
