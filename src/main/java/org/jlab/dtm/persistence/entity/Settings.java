package org.jlab.dtm.persistence.entity;

import java.io.Serializable;
import java.math.BigInteger;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import org.jlab.dtm.persistence.model.ImmutableSettings;
import org.jlab.smoothness.persistence.util.YnStringToBoolean;

/**
 * Editable settings JPA entity. See Also: org.jlab.dtm.persistence.model.ImmutableSettings.
 *
 * @author ryans
 */
@Entity
@Table(name = "SETTINGS", schema = "DTM_OWNER")
public class Settings implements Serializable {

  private static final long serialVersionUID = 1L;

  @Id
  @Basic(optional = false)
  @NotNull
  @Column(name = "SETTINGS_ID", nullable = false, precision = 22, scale = 0)
  private BigInteger settingsId;

  @Column(name = "AUTO_EMAIL_YN")
  @Size(min = 1, max = 1)
  @NotNull
  @Convert(converter = YnStringToBoolean.class)
  private boolean emailEnabled;

  @Column(name = "LOGBOOK_YN")
  @Size(min = 1, max = 1)
  @NotNull
  @Convert(converter = YnStringToBoolean.class)
  private boolean logbookEnabled;

  @Basic(optional = true)
  @Column(name = "EXPERT_EMAIL_CC_CSV", nullable = true)
  private String expertEmailCcCsv;

  public BigInteger getSettingsId() {
    return settingsId;
  }

  public void setSettingsId(BigInteger settingsId) {
    this.settingsId = settingsId;
  }

  public boolean isEmailEnabled() {
    return emailEnabled;
  }

  public void setEmailEnabled(boolean emailEnabled) {
    this.emailEnabled = emailEnabled;
  }

  public boolean isLogbookEnabled() {
    return logbookEnabled;
  }

  public void setLogbookEnabled(boolean logbookEnabled) {
    this.logbookEnabled = logbookEnabled;
  }

  public String getExpertEmailCcCsv() {
    return expertEmailCcCsv;
  }

  public ImmutableSettings immutable() {
    return new ImmutableSettings(emailEnabled, logbookEnabled, expertEmailCcCsv);
  }
}
