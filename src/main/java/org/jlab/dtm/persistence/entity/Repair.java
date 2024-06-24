package org.jlab.dtm.persistence.entity;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Objects;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

/**
 * @author ryans
 */
@Entity
@Table(name = "INCIDENT_REPAIR", schema = "DTM_OWNER")
public class Repair implements Serializable {
  private static final long serialVersionUID = 1L;

  @Id
  @SequenceGenerator(
      name = "IncidentRepairId",
      sequenceName = "INCIDENT_REPAIR_ID",
      allocationSize = 1)
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "IncidentRepairId")
  @Basic(optional = false)
  @NotNull
  @Column(name = "INCIDENT_REPAIR_ID", nullable = false, precision = 22, scale = 0)
  private BigInteger incidentRepairId;

  @JoinColumn(name = "INCIDENT_ID", referencedColumnName = "INCIDENT_ID", nullable = false)
  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  private Incident incident;

  @JoinColumn(name = "REPAIRED_BY", referencedColumnName = "WORKGROUP_ID", nullable = true)
  @ManyToOne(optional = true, fetch = FetchType.LAZY)
  private Workgroup repairedBy;

  public Repair() {}

  public BigInteger getIncidentRepairId() {
    return incidentRepairId;
  }

  public void setIncidentRepairId(BigInteger incidentRepairId) {
    this.incidentRepairId = incidentRepairId;
  }

  public Incident getIncident() {
    return incident;
  }

  public void setIncident(Incident incident) {
    this.incident = incident;
  }

  public Workgroup getRepairedBy() {
    return repairedBy;
  }

  public void setRepairedBy(Workgroup repairedBy) {
    this.repairedBy = repairedBy;
  }

  @Override
  public int hashCode() {
    int hash = 0;
    hash += (incidentRepairId != null ? incidentRepairId.hashCode() : 0);
    return hash;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final Repair other = (Repair) obj;
    return Objects.equals(this.incidentRepairId, other.incidentRepairId);
  }

  @Override
  public String toString() {
    return "org.jlab.dtm.persistence.entity.IncidentRepair[ incidentRepairId="
        + incidentRepairId
        + " ]";
  }
}
