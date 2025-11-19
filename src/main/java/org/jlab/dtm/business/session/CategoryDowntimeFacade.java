package org.jlab.dtm.business.session;

import jakarta.annotation.security.PermitAll;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import java.math.BigInteger;
import java.util.Date;
import java.util.List;
import org.jlab.dtm.persistence.entity.Category;
import org.jlab.dtm.persistence.entity.EventType;
import org.jlab.dtm.persistence.model.CategoryDowntime;
import org.jlab.smoothness.persistence.util.JPAUtil;

/**
 * @author ryans
 */
@Stateless
public class CategoryDowntimeFacade extends AbstractFacade<Category> {

  @PersistenceContext(unitName = "webappPU")
  private EntityManager em;

  @Override
  protected EntityManager getEntityManager() {
    return em;
  }

  public CategoryDowntimeFacade() {
    super(Category.class);
  }

  @PermitAll
  public List<CategoryDowntime> findByPeriodAndType(
      Date start,
      Date end,
      List<EventType> typeList,
      Boolean beamTransport,
      boolean packed,
      BigInteger categoryId) {

    // Note: The Beam Transport filter only works if not using packed intervals

    String sql =
        "select e.name, e.category_id, count(e.category_id) as incident_count, sum(interval_to_seconds(least(nvl(b.time_up, sysdate), :end) - greatest(b.time_down, :start))) / 60 / 60 / 24 as duration "
            + "from ";

    if (packed) {
      sql =
          sql
              + "alpha_cat_packed_incidents b inner join dtm_owner.category e on e.category_id = b.category_id ";
    } else {
      sql =
          sql
              + "dtm_owner.system a inner join system_alpha_category d using(system_id) inner join dtm_owner.category e on e.category_id = d.category_id inner join incident b using(system_id) inner join event c using(event_id) ";
    }

    sql = sql + "where b.time_down < :end " + "and nvl(b.time_up, sysdate) >= :start ";

    if (typeList != null && !typeList.isEmpty()) {
      String typeListString = typeList.get(0).getEventTypeId().toString();
      for (int i = 1; i < typeList.size(); i++) {
        typeListString = typeListString + "," + typeList.get(i).getEventTypeId().toString();
      }

      sql = sql + "and event_type_id in (" + typeListString + ") ";
    }

    if (categoryId != null) {
      sql = sql + "and e.category_id = " + categoryId + " ";
    }

    // beamTransport Y = only beam transport
    // beamTransport N = everything but beam transport
    // Null means don't filter beam transport specially
    if (beamTransport != null && !packed) { // Only if not packed can you filter by beam transport
      if (beamTransport) {
        sql =
            sql
                + "and system_id in (select system_id from dtm_owner.system_alpha_category join dtm_owner.category using (category_id) where category.name = 'Beam Transport') ";
      } else {
        sql =
            sql
                + "and system_id not in (select system_id from dtm_owner.system_alpha_category join dtm_owner.category using (category_id) where category.name = 'Beam Transport') ";
      }
    }

    sql = sql + "group by e.name, e.category_id order by duration desc";

    Query q = em.createNativeQuery(sql);

    q.setParameter("start", start);
    q.setParameter("end", end);

    List<CategoryDowntime> downtimeList = JPAUtil.getResultList(q, CategoryDowntime.class);

    return downtimeList;
  }
}
