package org.jlab.dtm.persistence.model;

public interface SettingChangeAction {
  public void handleChange(String key, String value);
}
