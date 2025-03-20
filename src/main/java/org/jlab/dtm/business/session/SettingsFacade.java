package org.jlab.dtm.business.session;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.security.DeclareRoles;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateless;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import org.jlab.dtm.persistence.entity.Setting;
import org.jlab.dtm.persistence.enumeration.SettingsType;
import org.jlab.dtm.persistence.model.ImmutableSettings;
import org.jlab.dtm.persistence.model.SettingChangeAction;
import org.jlab.smoothness.business.exception.UserFriendlyException;

/**
 * @author ryans
 */
@Stateless
@DeclareRoles("dtm-admin")
public class SettingsFacade extends AbstractFacade<Setting> {

  public static volatile ImmutableSettings cachedSettings;

  @PersistenceContext(unitName = "dtmPU")
  private EntityManager em;

  public SettingsFacade() {
    super(Setting.class);
  }

  @Override
  protected EntityManager getEntityManager() {
    return em;
  }

  @PermitAll
  public ImmutableSettings getImmutableSettings() {
    List<Setting> settingList = findAll();

    return new ImmutableSettings(settingList);
  }

  // Caller is responsible for updating both ServletContext and SettingsFacade cache
  // since EJB can't easily update ServletContext.
  //
  // In order to perform any cleanup action needed by updating a setting such as disabling features
  // (such as stopping scheduled timers)
  // The registered SettingChangeAction is invoked, if any.
  @RolesAllowed("dtm-admin")
  public void editSetting(String key, String value) throws UserFriendlyException {
    Setting setting = find(key);

    if (setting == null) {
      throw new UserFriendlyException("Setting not found with key: " + key);
    }

    if (SettingsType.BOOLEAN.equals(setting.getType())) {
      if (!"Y".equals(value) && !"N".equals(value)) {
        throw new UserFriendlyException("Boolean Setting value must be 'Y' or 'N'");
      }
    }

    setting.setValue(value);

    if (setting.getChangeActionJNDI() != null) {
      SettingChangeAction action = lookupChangeActionViaJNDI(setting.getChangeActionJNDI());
      if (action != null) {
        action.handleChange(key, value);
      }
    }
  }

  private SettingChangeAction lookupChangeActionViaJNDI(String name) {
    try {
      InitialContext ic = new InitialContext();
      return (SettingChangeAction) ic.lookup(name); // example: java:global/dtm/ScheduledEmailer
    } catch (NamingException e) {
      throw new RuntimeException("Unable to obtain EJB", e);
    }
  }

  private List<Predicate> getFilters(
      CriteriaBuilder cb, Root<Setting> root, String key, String tag) {
    List<Predicate> filters = new ArrayList<>();

    if (key != null && !key.isEmpty()) {
      filters.add(cb.like(cb.lower(root.get("key")), key.toLowerCase()));
    }

    if (tag != null && !tag.isEmpty()) {
      filters.add(cb.like(cb.lower(root.get("tag")), tag.toLowerCase()));
    }

    return filters;
  }

  @PermitAll
  public List<Setting> filterList(String key, String tag, int offset, int max) {
    CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
    CriteriaQuery<Setting> cq = cb.createQuery(Setting.class);
    Root<Setting> root = cq.from(Setting.class);
    cq.select(root);

    List<Predicate> filters = getFilters(cb, root, key, tag);

    if (!filters.isEmpty()) {
      cq.where(cb.and(filters.toArray(new Predicate[] {})));
    }

    List<Order> orders = new ArrayList<>();
    Path p0 = root.get("tag");
    Order o0 = cb.asc(p0);
    orders.add(o0);
    Path p1 = root.get("weight");
    Order o1 = cb.asc(p1);
    orders.add(o1);
    Path p2 = root.get("key");
    Order o2 = cb.asc(p2);
    orders.add(o2);
    cq.orderBy(orders);
    return getEntityManager()
        .createQuery(cq)
        .setFirstResult(offset)
        .setMaxResults(max)
        .getResultList();
  }

  @PermitAll
  public long countList(String key, String tag) {
    CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
    CriteriaQuery<Long> cq = cb.createQuery(Long.class);
    Root<Setting> root = cq.from(Setting.class);

    List<Predicate> filters = getFilters(cb, root, key, tag);

    if (!filters.isEmpty()) {
      cq.where(cb.and(filters.toArray(new Predicate[] {})));
    }

    cq.select(cb.count(root));
    TypedQuery<Long> q = getEntityManager().createQuery(cq);
    return q.getSingleResult();
  }

  @PermitAll
  public List<String> findTags() {
    TypedQuery<String> q =
        getEntityManager()
            .createQuery("SELECT DISTINCT tag FROM Setting order by tag asc", String.class);
    return q.getResultList();
  }
}
