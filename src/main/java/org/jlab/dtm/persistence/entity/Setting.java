package org.jlab.dtm.persistence.entity;

import java.io.Serializable;
import java.util.Objects;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import org.jlab.dtm.persistence.enumeration.SettingsType;

/**
 * Editable setting JPA entity. See Also: org.jlab.dtm.persistence.model.ImmutableSettings.
 *
 * @author ryans
 */
@Entity
@Table(name = "SETTING", schema = "DTM_OWNER")
public class Setting implements Serializable {

  private static final long serialVersionUID = 1L;

  @Id
  @Basic(optional = false)
  @NotNull
  @Column(name = "KEY", nullable = false)
  private String key;

  @NotNull
  @Column(name = "VALUE", nullable = false)
  private String value;

  @NotNull
  @Column(name = "TYPE", nullable = false)
  @Enumerated(EnumType.STRING)
  private SettingsType type;

  @NotNull
  @Column(name = "DESCRIPTION", nullable = false)
  private String description;

  @NotNull
  @Column(name = "TAG", nullable = false)
  private String tag;

  @NotNull
  @Column(name = "WEIGHT", nullable = false)
  private Integer weight;

  @Column(name = "CHANGE_ACTION_JNDI")
  private String changeActionJNDI;

  public String getKey() {
    return key;
  }

  public void setKey(String key) {
    this.key = key;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  public SettingsType getType() {
    return type;
  }

  public void setType(SettingsType type) {
    this.type = type;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getTag() {
    return tag;
  }

  public void setTag(String tag) {
    this.tag = tag;
  }

  public Integer getWeight() {
    return weight;
  }

  public void setWeight(Integer weight) {
    this.weight = weight;
  }

  public String getChangeActionJNDI() {
    return changeActionJNDI;
  }

  public void setChangeActionJNDI(String changeActionJNDI) {
    this.changeActionJNDI = changeActionJNDI;
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof Setting)) return false;
    Setting setting = (Setting) o;
    return Objects.equals(key, setting.key)
        && Objects.equals(value, setting.value)
        && type == setting.type
        && Objects.equals(description, setting.description);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(key);
  }
}
