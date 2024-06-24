package org.jlab.dtm.business.session;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.security.PermitAll;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import org.jlab.dtm.persistence.entity.Category;
import org.jlab.smoothness.business.util.IOUtil;

/**
 * @author ryans
 */
@Stateless
public class CategoryFacade extends AbstractFacade<Category> {

  @PersistenceContext(unitName = "dtmPU")
  private EntityManager em;

  @Override
  protected EntityManager getEntityManager() {
    return em;
  }

  public CategoryFacade() {
    super(Category.class);
  }

  public Category findRoot() {
    return find(BigInteger.ZERO);
  }

  @PermitAll
  public List<Category> findAllViaCartesianProduct() {
    TypedQuery<Category> q =
        em.createQuery("select c from Category c left join fetch c.categoryList", Category.class);
    return q.getResultList();
  }

  @PermitAll
  public Category findRootWithAllDescendents() {
    findAllViaCartesianProduct(); // Load all categories into em cache via single query
    Category root = findRoot(); // Should be cache hit
    return root;
  }

  @PermitAll
  public Category findBranch(BigInteger categoryId) {
    Category category;

    if (categoryId == null) {
      categoryId = BigInteger.valueOf(0L);
    }

    findAllViaCartesianProduct(); // load all categories AND their children relationships

    category = find(categoryId);

    category = pruneCategoryTree(category);

    return category;
  }

  @PermitAll
  private Category pruneCategoryTree(Category category) {

    if (category != null) {
      em.detach(category);
      // List<BigInteger> categoryIdList = findCategoryIdListLame(applicationId);
      List<BigInteger> categoryIdList = findCategoryIdList();
      Collections.sort(categoryIdList);
      int index = Collections.binarySearch(categoryIdList, category.getCategoryId());
      if (index < 0) {
        category = null;
      } else {
        pruneCategoryDescendents(category, categoryIdList);
      }
    }

    return category;
  }

  private void pruneCategoryDescendents(Category category, List<BigInteger> categoryIdList) {
    int index;

    List<Category> childrenList = new ArrayList<Category>();

    if (category.getCategoryList() != null) {
      for (Category c : category.getCategoryList()) {
        index = Collections.binarySearch(categoryIdList, c.getCategoryId());

        if (index >= 0) {
          childrenList.add(c);
        }
      }
    }

    em.detach(category);
    category.setCategoryList(childrenList);
    for (Category c : childrenList) {
      pruneCategoryDescendents(c, categoryIdList);
    }
  }

  @SuppressWarnings("unchecked")
  private List<BigInteger> findCategoryIdList() {
    Query q = em.createNativeQuery("select category_id from dtm_owner.category ");

    List<BigInteger> categoryIdList = new ArrayList<>();

    List<Object> resultList = q.getResultList();

    if (resultList != null) {
      for (Object row : resultList) {
        BigInteger categoryId = BigInteger.valueOf(((Number) row).longValue());
        categoryIdList.add(categoryId);
      }
    }

    return categoryIdList;
  }

  @SuppressWarnings("unchecked")
  @PermitAll
  public List<Category> findAlphaCategoryList() {
    String sql =
        "select * from category where category_id in (select distinct category_id from system_alpha_category) order by name asc";

    Query q = em.createNativeQuery(sql, Category.class);

    return q.getResultList();
  }

  @SuppressWarnings("unchecked")
  @PermitAll
  public List<String> findNamesByIds(BigInteger[] categoryIdArray) {
    List<String> list = null;

    if (categoryIdArray != null && categoryIdArray.length > 0) {
      String csv = IOUtil.toCsv(categoryIdArray);

      String sql = "select name from category where category_id in (" + csv + ")";

      Query q = em.createNativeQuery(sql, "CategoryNameOnly");

      list = q.getResultList();
    }

    return list;
  }
}
