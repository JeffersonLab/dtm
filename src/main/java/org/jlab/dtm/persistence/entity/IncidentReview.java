package org.jlab.dtm.persistence.entity;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigInteger;

/**
 * @author ryans
 */
@Entity
@Table(name = "INCIDENT_REVIEW", schema = "DTM_OWNER")
@NamedQueries({
  @NamedQuery(name = "IncidentReview.findAll", query = "SELECT i FROM IncidentReview i")
})
public class IncidentReview implements Serializable {

  private static final long serialVersionUID = 1L;

  @Id
  @SequenceGenerator(
      name = "IncidentReviewId",
      sequenceName = "INCIDENT_REVIEW_ID",
      allocationSize = 1)
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "IncidentReviewId")
  @Basic(optional = false)
  @NotNull
  @Column(name = "INCIDENT_REVIEW_ID", nullable = false, precision = 38, scale = 0)
  private BigInteger incidentReviewId;

  @NotNull
  @Column(name = "REVIEWER_USERNAME", nullable = false)
  private String reviewer;

  @JoinColumn(name = "INCIDENT_ID", referencedColumnName = "INCIDENT_ID", nullable = false)
  @ManyToOne(optional = false)
  private Incident incident;

  public IncidentReview() {}

  public IncidentReview(BigInteger incidentReviewId) {
    this.incidentReviewId = incidentReviewId;
  }

  public BigInteger getIncidentReviewId() {
    return incidentReviewId;
  }

  public void setIncidentReviewId(BigInteger incidentReviewId) {
    this.incidentReviewId = incidentReviewId;
  }

  public String getReviewer() {
    return reviewer;
  }

  public void setReviewer(String reviewer) {
    this.reviewer = reviewer;
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
    hash += (incidentReviewId != null ? incidentReviewId.hashCode() : 0);
    return hash;
  }

  @Override
  public boolean equals(Object object) {
    // TODO: Warning - this method won't work in the case the id fields are not set
    if (!(object instanceof IncidentReview)) {
      return false;
    }
    IncidentReview other = (IncidentReview) object;
    return (this.incidentReviewId != null || other.incidentReviewId == null)
        && (this.incidentReviewId == null || this.incidentReviewId.equals(other.incidentReviewId));
  }

  @Override
  public String toString() {
    return "org.jlab.dtm.persistence.entity.IncidentReview[ incidentReviewId="
        + incidentReviewId
        + " ]";
  }
}
