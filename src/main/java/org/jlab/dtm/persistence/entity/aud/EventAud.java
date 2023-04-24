package org.jlab.dtm.persistence.entity.aud;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;
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
import org.jlab.dtm.persistence.entity.EventType;

/**
 *
 * @author ryans
 */
@Entity
@Table(name = "EVENT_AUD", schema = "DTM_OWNER")
public class EventAud implements Serializable {

    private static final long serialVersionUID = 1L;
    @EmbeddedId
    protected EventAudPK eventAudPK;
    @Column(name = "TIME_UP")
    @Temporal(TemporalType.TIMESTAMP)
    private Date timeUp;
    @NotNull
    @JoinColumn(name = "EVENT_TYPE_ID", referencedColumnName = "EVENT_TYPE_ID", nullable = false)
    @ManyToOne(optional = false)
    private EventType eventType;
    @Enumerated(EnumType.ORDINAL)
    @NotNull
    @Column(name = "REVTYPE")
    private RevisionType type;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 128)
    @Column(nullable = false, length = 128)
    private String title;    
    @JoinColumn(name = "REV", referencedColumnName = "REV", insertable = false, updatable = false, nullable = false)
    @NotNull
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private ApplicationRevisionInfo revision;

    public EventAud() {
    }

    public Date getTimeUp() {
        return timeUp;
    }

    public void setTimeUp(Date timeUp) {
        this.timeUp = timeUp;
    }

    public EventType getEventType() {
        return eventType;
    }

    public void setEventType(EventType eventType) {
        this.eventType = eventType;
    }

    public RevisionType getType() {
        return type;
    }

    public void setType(RevisionType type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public ApplicationRevisionInfo getRevision() {
        return revision;
    }

    public void setRevision(ApplicationRevisionInfo revision) {
        this.revision = revision;
    }

    public EventAudPK getEventAudPK() {
        return eventAudPK;
    }

    public void setEventAudPK(EventAudPK eventAudPK) {
        this.eventAudPK = eventAudPK;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 53 * hash + (this.eventAudPK != null ? this.eventAudPK.hashCode() : 0);
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
        final EventAud other = (EventAud) obj;
        return Objects.equals(this.eventAudPK, other.eventAudPK);
    }
}
