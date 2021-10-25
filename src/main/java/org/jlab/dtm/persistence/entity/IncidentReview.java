package org.jlab.dtm.persistence.entity;

import java.io.Serializable;
import java.math.BigInteger;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.ConstraintMode;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

/**
 *
 * @author ryans
 */
@Entity
@Table(name = "INCIDENT_REVIEW", schema = "DTM_OWNER")
@NamedQueries({
    @NamedQuery(name = "IncidentReview.findAll", query = "SELECT i FROM IncidentReview i")})
public class IncidentReview implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @SequenceGenerator(name = "IncidentReviewId", sequenceName = "INCIDENT_REVIEW_ID", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "IncidentReviewId")    
    @Basic(optional = false)
    @NotNull
    @Column(name = "INCIDENT_REVIEW_ID", nullable = false, precision = 38, scale = 0)
    private BigInteger incidentReviewId;
    /*@Basic(optional = false)
    @NotNull
    @Column(name = "REVIEWER_ID", nullable = false)
    private BigInteger reviewerId;*/  
    @JoinColumn(name = "REVIEWER_ID", referencedColumnName = "STAFF_ID", foreignKey = @javax.persistence.ForeignKey(name = "none", value = ConstraintMode.NO_CONSTRAINT))
    @ManyToOne(optional = true, fetch = FetchType.EAGER)    
    private Staff reviewer;    
    @JoinColumn(name = "INCIDENT_ID", referencedColumnName = "INCIDENT_ID", nullable = false)
    @ManyToOne(optional = false)
    private Incident incident;

    public IncidentReview() {
    }

    public IncidentReview(BigInteger incidentReviewId) {
        this.incidentReviewId = incidentReviewId;
    }

    public BigInteger getIncidentReviewId() {
        return incidentReviewId;
    }

    public void setIncidentReviewId(BigInteger incidentReviewId) {
        this.incidentReviewId = incidentReviewId;
    }

    public Staff getReviewer() {
        return reviewer;
    }

    public void setReviewer(Staff reviewer) {
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
        if ((this.incidentReviewId == null && other.incidentReviewId != null) || (this.incidentReviewId != null && !this.incidentReviewId.equals(other.incidentReviewId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "org.jlab.dtm.persistence.entity.IncidentReview[ incidentReviewId=" + incidentReviewId + " ]";
    }
    
}
