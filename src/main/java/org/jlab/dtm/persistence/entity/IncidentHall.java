package org.jlab.dtm.persistence.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigInteger;
import org.jlab.smoothness.persistence.enumeration.Hall;

/**
 * @author ryans
 */
@Entity
@Table(name = "INCIDENT_HALL", schema = "DTM_OWNER")
public class IncidentHall implements Serializable {

  private static final long serialVersionUID = 1L;

  @Id
  @SequenceGenerator(name = "IncidentHallId", sequenceName = "INCIDENT_HALL_ID", allocationSize = 1)
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "IncidentHallId")
  @Basic(optional = false)
  @NotNull
  @Column(name = "INCIDENT_HALL_ID", nullable = false, precision = 38, scale = 0)
  private BigInteger incidentHallId;

  @NotNull
  @Column(name = "HALL", nullable = false)
  @Enumerated(EnumType.STRING)
  private Hall hall;

  @JoinColumn(name = "INCIDENT_ID", referencedColumnName = "INCIDENT_ID", nullable = false)
  @ManyToOne(optional = false)
  private Incident incident;

  public IncidentHall() {}

  public IncidentHall(BigInteger incidentHallId) {
    this.incidentHallId = incidentHallId;
  }

  public BigInteger getIncidentHallId() {
    return incidentHallId;
  }

  public void setIncidentReviewId(BigInteger incidentReviewId) {
    this.incidentHallId = incidentHallId;
  }

  public Hall getHall() {
    return hall;
  }

  public void setHall(Hall hall) {
    this.hall = hall;
  }

  public Incident getIncident() {
    return incident;
  }

  public void setIncident(Incident incident) {
    this.incident = incident;
  }

  @Override
  public int hashCode() {
    int hash = 0;
    hash += (incidentHallId != null ? incidentHallId.hashCode() : 0);
    return hash;
  }

  @Override
  public boolean equals(Object object) {
    if (!(object instanceof IncidentHall)) {
      return false;
    }
    IncidentHall other = (IncidentHall) object;
    return (this.incidentHallId != null || other.incidentHallId == null)
        && (this.incidentHallId == null || this.incidentHallId.equals(other.incidentHallId));
  }

  @Override
  public String toString() {
    return "org.jlab.dtm.persistence.entity.IncidentHall[ incidentHallId=" + incidentHallId + " ]";
  }
}
