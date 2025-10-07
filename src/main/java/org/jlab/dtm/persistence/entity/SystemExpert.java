package org.jlab.dtm.persistence.entity;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigInteger;

/**
 * @author ryans
 */
@Entity
@Table(name = "SYSTEM_EXPERT", schema = "DTM_OWNER")
public class SystemExpert implements Serializable {

  private static final long serialVersionUID = 1L;

  @Id
  @Basic(optional = false)
  @NotNull
  @SequenceGenerator(name = "SystemExpertId", sequenceName = "SYSTEM_EXPERT_ID", allocationSize = 1)
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SystemExpertId")
  @Column(name = "SYSTEM_EXPERT_ID", nullable = false, precision = 38, scale = 0)
  private BigInteger systemExpertId;

  @NotNull
  @Column(name = "USERNAME", nullable = false, length = 64)
  private String username;

  @JoinColumn(name = "SYSTEM_ID", referencedColumnName = "SYSTEM_ID", nullable = false)
  @ManyToOne(optional = false)
  private SystemEntity system;

  public SystemExpert() {}

  public BigInteger getSystemExpertId() {
    return systemExpertId;
  }

  public void setSystemExpertId(BigInteger systemExpertId) {
    this.systemExpertId = systemExpertId;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public SystemEntity getSystem() {
    return system;
  }

  public void setSystem(SystemEntity system) {
    this.system = system;
  }

  @Override
  public int hashCode() {
    int hash = 0;
    hash += (systemExpertId != null ? systemExpertId.hashCode() : 0);
    return hash;
  }

  @Override
  public boolean equals(Object object) {
    // TODO: Warning - this method won't work in the case the id fields are not set
    if (!(object instanceof SystemExpert)) {
      return false;
    }
    SystemExpert other = (SystemExpert) object;
    return (this.systemExpertId != null || other.systemExpertId == null)
        && (this.systemExpertId == null || this.systemExpertId.equals(other.systemExpertId));
  }

  @Override
  public String toString() {
    return "org.jlab.dtm.persistence.entity.SystemExpert[ systemExpertId=" + systemExpertId + " ]";
  }
}
