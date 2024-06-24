package org.jlab.dtm.business.session;

import java.util.Date;
import java.util.List;
import javax.annotation.security.PermitAll;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import org.jlab.dtm.persistence.entity.MonthlyNote;
import org.jlab.smoothness.business.exception.UserFriendlyException;

/**
 * @author ryans
 */
@Stateless
public class MonthlyNoteFacade extends AbstractFacade<MonthlyNote> {

  @PersistenceContext(unitName = "dtmPU")
  private EntityManager em;

  @Override
  protected EntityManager getEntityManager() {
    return em;
  }

  public MonthlyNoteFacade() {
    super(MonthlyNote.class);
  }

  @PermitAll
  public MonthlyNote find(Date month) {
    TypedQuery<MonthlyNote> query =
        em.createQuery("select a from MonthlyNote a where a.month = :month", MonthlyNote.class);

    query.setParameter("month", month);

    List<MonthlyNote> resultList = query.getResultList();

    MonthlyNote note = null;

    if (resultList != null && !resultList.isEmpty()) {
      note = resultList.get(0);
    }

    return note;
  }

  @PermitAll
  public void save(Date month, String note, Float machineGoal, Float tripGoal, Float eventGoal)
      throws UserFriendlyException {
    MonthlyNote mn = find(month);

    if (mn == null) {
      mn = new MonthlyNote();
      mn.setMonth(month);
    }

    mn.setNote(note);
    mn.setMachineGoal(machineGoal);
    mn.setTripGoal(tripGoal);
    mn.setEventGoal(eventGoal);

    edit(mn);
  }

  @PermitAll
  public Float findMostRecentMachineGoal() {
    TypedQuery<Float> query =
        em.createQuery(
            "select a.machineGoal from MonthlyNote a where a.machineGoal is not null order by a.month desc",
            Float.class);

    List<Float> resultList = query.setMaxResults(1).getResultList();

    Float goal = null;

    if (resultList != null && !resultList.isEmpty()) {
      goal = resultList.get(0);
    }

    return goal;
  }

  @PermitAll
  public Float findMostRecentEventGoal() {
    TypedQuery<Float> query =
        em.createQuery(
            "select a.eventGoal from MonthlyNote a where a.eventGoal is not null order by a.month desc",
            Float.class);

    List<Float> resultList = query.setMaxResults(1).getResultList();

    Float goal = null;

    if (resultList != null && !resultList.isEmpty()) {
      goal = resultList.get(0);
    }

    return goal;
  }

  @PermitAll
  public Float findMostRecentTripGoal() {
    TypedQuery<Float> query =
        em.createQuery(
            "select a.tripGoal from MonthlyNote a where a.tripGoal is not null order by a.month desc",
            Float.class);

    List<Float> resultList = query.setMaxResults(1).getResultList();

    Float goal = null;

    if (resultList != null && !resultList.isEmpty()) {
      goal = resultList.get(0);
    }

    return goal;
  }
}
