package org.jlab.dtm.persistence.entity.aud;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Date;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import org.hibernate.envers.RevisionType;
import org.jlab.dtm.persistence.entity.ApplicationRevisionInfo;
import org.jlab.dtm.persistence.entity.Component;
import org.jlab.dtm.persistence.entity.Staff;
import org.jlab.dtm.persistence.entity.SystemEntity;
import org.jlab.dtm.persistence.enumeration.SystemExpertAcknowledgement;

/**
 *
 * @author ryans
 */
@Entity
@Table(name = "INCIDENT_AUD", schema = "DTM_OWNER")
public class IncidentAud implements Serializable {

    private static final long serialVersionUID = 1L;
    @EmbeddedId
    protected IncidentAudPK incidentAudPK;
    @Basic(optional = false)
    @NotNull
    @Column(name = "TIME_DOWN", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date timeDown;
    @Column(name = "TIME_UP")
    @Temporal(TemporalType.TIMESTAMP)
    private Date timeUp;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 128)
    @Column(nullable = false, length = 128)
    private String title;
    @JoinColumn(name = "SYSTEM_ID", referencedColumnName = "SYSTEM_ID", nullable = false)
    @ManyToOne(optional = false)
    private SystemEntity system;
    @JoinColumn(name = "COMPONENT_ID", referencedColumnName = "COMPONENT_ID")
    @ManyToOne
    private Component component;
    @Enumerated(EnumType.ORDINAL)
    @NotNull
    @Column(name = "REVTYPE")
    private RevisionType type;
    @JoinColumn(name = "REV", referencedColumnName = "REV", insertable = false, updatable = false, nullable = false)
    @NotNull
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private ApplicationRevisionInfo revision;
    @Basic(optional = false)
    @NotNull
    @Column(name = "EVENT_ID", nullable = false, precision = 22, scale = 0)
    private BigInteger eventId;
    @Size(min = 1, max = 2048)
    @Column(name= "MISSING_EXPLANATION", nullable = true, length = 2048)    
    private String explanation;
    @Size(min = 1, max = 2048)
    @Column(name= "SUMMARY", nullable = true, length = 2048)    
    private String summary;
    @Size(max = 2048)
    @Column(name= "RESOLUTION", nullable = true, length = 2048)    
    private String resolution;    
    @JoinColumn(name = "REVIEWED_BY", referencedColumnName = "STAFF_ID")
    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    private Staff reviewedBy;  
    @Size(max = 512)
    @Column(name= "ROOT_CAUSE", nullable = true, length = 512)    
    private String rootCause;      
    @Column(name = "RAR_ID")
    private BigInteger rarId;
    @Basic(optional = false)
    @NotNull
    @Column(name = "EXPERT_ACKNOWLEDGED", nullable = false)
    @Enumerated(EnumType.STRING)
    private SystemExpertAcknowledgement expertAcknowledged;    
    
    public IncidentAud() {
    }

    public String getRootCause() {
        return rootCause;
    }

    public void setRootCause(String rootCause) {
        this.rootCause = rootCause;
    }

    public Staff getReviewedBy() {
        return reviewedBy;
    }

    public void setReviewedBy(Staff reviewedBy) {
        this.reviewedBy = reviewedBy;
    }

    public Date getTimeDown() {
        return timeDown;
    }

    public void setTimeDown(Date timeDown) {
        this.timeDown = timeDown;
    }

    public Date getTimeUp() {
        return timeUp;
    }

    public void setTimeUp(Date timeUp) {
        this.timeUp = timeUp;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public SystemEntity getSystem() {
        return system;
    }

    public void setSystem(SystemEntity system) {
        this.system = system;
    }

    public Component getComponent() {
        return component;
    }

    public void setComponent(Component component) {
        this.component = component;
    }

    public RevisionType getType() {
        return type;
    }

    public void setType(RevisionType type) {
        this.type = type;
    }

    public ApplicationRevisionInfo getRevision() {
        return revision;
    }

    public void setRevision(ApplicationRevisionInfo revision) {
        this.revision = revision;
    }

    public BigInteger getEventId() {
        return eventId;
    }

    public void setEventId(BigInteger eventId) {
        this.eventId = eventId;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getResolution() {
        return resolution;
    }

    public void setResolution(String resolution) {
        this.resolution = resolution;
    }

    public BigInteger getRarId() {
        return rarId;
    }

    public void setRarId(BigInteger rarId) {
        this.rarId = rarId;
    }

    public SystemExpertAcknowledgement getExpertAcknowledged() {
        return expertAcknowledged;
    }

    public void setExpertAcknowledged(SystemExpertAcknowledgement expertAcknowledged) {
        this.expertAcknowledged = expertAcknowledged;
    }
    
    public IncidentAudPK getIncidentAudPK() {
        return incidentAudPK;
    }

    public void setIncidentAudPK(IncidentAudPK incidentAudPK) {
        this.incidentAudPK = incidentAudPK;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 17 * hash + (this.incidentAudPK != null ? this.incidentAudPK.hashCode() : 0);
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
        final IncidentAud other = (IncidentAud) obj;
        if (this.incidentAudPK != other.incidentAudPK && (this.incidentAudPK == null || !this.incidentAudPK.equals(other.incidentAudPK))) {
            return false;
        }
        return true;
    }
}
