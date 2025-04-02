package org.jlab.dtm.business.session;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.security.PermitAll;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import org.jlab.dtm.persistence.entity.CategoryMonthlyGoal;

/**
 * @author ryans
 */
@Stateless
public class CategoryMonthlyGoalFacade extends AbstractFacade<CategoryMonthlyGoal> {

  @PersistenceContext(unitName = "webappPU")
  private EntityManager em;

  @Override
  protected EntityManager getEntityManager() {
    return em;
  }

  public CategoryMonthlyGoalFacade() {
    super(CategoryMonthlyGoal.class);
  }

  @PermitAll
  public List<CategoryMonthlyGoal> find(Date month) {
    TypedQuery<CategoryMonthlyGoal> query =
        em.createQuery(
            "select a from CategoryMonthlyGoal a where a.month = :month",
            CategoryMonthlyGoal.class);

    query.setParameter("month", month);

    return query.getResultList();
  }

  @PermitAll
  public Map<Long, CategoryMonthlyGoal> findMap(Date month) {
    List<CategoryMonthlyGoal> goalList = find(month);

    Map<Long, CategoryMonthlyGoal> map = new HashMap<>();

    if (goalList != null) {
      for (CategoryMonthlyGoal goal : goalList) {
        map.put(goal.getCategoryId(), goal);
      }
    }

    return map;
  }

  @PermitAll
  public void save(Date month, Long[] catIdArray, Float[] catGoalArray) {
    Map<Long, CategoryMonthlyGoal> goalMap = findMap(month);

    if (catIdArray != null && catGoalArray != null) {
      if (catIdArray.length != catGoalArray.length) {
        throw new RuntimeException("category ID array is not the same size as category goal array");
      }

      for (int i = 0; i < catIdArray.length; i++) {
        Long catId = catIdArray[i];
        Float goal = catGoalArray[i];

        CategoryMonthlyGoal cmg = goalMap.get(catId);

        if (cmg == null) {
          cmg = new CategoryMonthlyGoal();
          cmg.setMonth(month);
          cmg.setCategoryId(catId);
        }

        cmg.setGoal(goal);

        edit(cmg);
      }
    }
  }

  @PermitAll
  public CategoryMonthlyGoal findMostRecent(Long id) {
    TypedQuery<CategoryMonthlyGoal> query =
        em.createQuery(
            "select a from CategoryMonthlyGoal a where a.categoryId = :id and a.goal is not null order by a.month desc",
            CategoryMonthlyGoal.class);

    query.setParameter("id", id);

    List<CategoryMonthlyGoal> resultList = query.setMaxResults(1).getResultList();

    CategoryMonthlyGoal goal = null;

    if (resultList != null && !resultList.isEmpty()) {
      goal = resultList.get(0);
    }

    return goal;
  }
}
