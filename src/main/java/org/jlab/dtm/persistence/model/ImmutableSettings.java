package org.jlab.dtm.persistence.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jlab.dtm.persistence.entity.Setting;

/**
 * Read-only (immutable) application Settings.
 *
 * <p>Settings are looked up frequently and change infrequently. They're cached in two places: -
 * ServletContext.getAttribute("settings") - For global Servlets/JSP access -
 * SettingsFacade.cachedSettings - For global EJB access (static volatile)
 *
 * <p>The editable JPA Settings entity should be used only by Setup page that edits Settings.
 * Everywhere else should use this ImmutableSettings POJO class via one of the caches above.
 *
 * <p>Settings are initialized at org.jlab.dtm.presentation.util.SettingsCacheInit during app
 * startup. Changes to settings should be done via Setup tab and at that time the caches should be
 * refreshed.
 */
public final class ImmutableSettings {
  private Map<String, Setting> map;

  public ImmutableSettings(List<Setting> settingList) {
    map = new HashMap<String, Setting>();
    for (Setting setting : settingList) {
      map.put(setting.getKey(), setting);
    }
  }

  public String get(String key) {
    return map.get(key).getValue();
  }

  public boolean is(String key) {
    Setting s = map.get(key);

    // assert s.getType() == SettingsType.BOOLEAN;

    return "Y".equals(s.getValue());
  }

  public List<String> csv(String key) {
    return null;
  }
}
