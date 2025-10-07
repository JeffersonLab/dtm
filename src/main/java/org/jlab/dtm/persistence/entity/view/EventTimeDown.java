package org.jlab.dtm.persistence.entity.view;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.Date;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;
import org.jlab.dtm.persistence.entity.Event;

/**
 * @author ryans
 */
@Entity
@Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
@Table(name = "EVENT_TIME_DOWN", schema = "DTM_OWNER")
public class EventTimeDown implements Serializable {
  private static final long serialVersionUID = 1L;

  @Id
  @Basic(optional = false)
  @NotNull
  @Column(name = "EVENT_ID", nullable = false, precision = 22, scale = 0)
  private BigInteger eventId;

  @Column(name = "TIME_DOWN")
  @Temporal(TemporalType.TIMESTAMP)
  private Date timeDown;

  @NotNull
  @JoinColumn(name = "EVENT_ID", referencedColumnName = "EVENT_ID", nullable = false)
  @OneToOne(optional = false)
  private Event event;

  public EventTimeDown() {}

  public BigInteger getEventId() {
    return eventId;
  }

  public void setEventId(BigInteger eventId) {
    this.eventId = eventId;
  }

  public Date getTimeDown() {
    return timeDown;
  }

  public void setTimeDown(Date timeDown) {
    this.timeDown = timeDown;
  }

  public Event getEvent() {
    return event;
  }

  public void setEvent(Event event) {
    this.event = event;
  }

  @Override
  public int hashCode() {
    int hash = 0;
    hash += (eventId != null ? eventId.hashCode() : 0);
    return hash;
  }

  @Override
  public boolean equals(Object object) {
    // TODO: Warning - this method won't work in the case the id fields are not set
    if (!(object instanceof EventTimeDown)) {
      return false;
    }
    EventTimeDown other = (EventTimeDown) object;
    return (this.eventId != null || other.eventId == null)
        && (this.eventId == null || this.eventId.equals(other.eventId));
  }

  @Override
  public String toString() {
    return "org.jlab.dtm.persistence.entity.EventTimeDown[ eventId=" + eventId + " ]";
  }
}
