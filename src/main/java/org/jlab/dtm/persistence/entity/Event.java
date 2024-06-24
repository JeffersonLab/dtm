package org.jlab.dtm.persistence.entity;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Date;
import java.util.List;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderBy;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import org.hibernate.envers.Audited;
import org.jlab.dtm.persistence.entity.view.EventTimeDown;
import org.jlab.smoothness.persistence.view.User;

/**
 * @author ryans
 */
@Entity
@Audited
@Table(schema = "DTM_OWNER")
@NamedQueries({@NamedQuery(name = "Event.findAll", query = "SELECT e FROM Event e")})
public class Event implements Serializable {

  private static final long serialVersionUID = 1L;

  @Id
  @SequenceGenerator(name = "EventId", sequenceName = "EVENT_ID", allocationSize = 1)
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "EventId")
  @Basic(optional = false)
  @NotNull
  @Column(name = "EVENT_ID", nullable = false, precision = 22, scale = 0)
  private BigInteger eventId;

  @Column(name = "TIME_UP")
  @Temporal(TemporalType.TIMESTAMP)
  private Date timeUp;

  @NotNull
  @JoinColumn(name = "EVENT_TYPE_ID", referencedColumnName = "EVENT_TYPE_ID", nullable = false)
  @ManyToOne(optional = false)
  private EventType eventType;

  @Basic(optional = false)
  @NotNull
  @Size(min = 1, max = 128)
  @Column(nullable = false, length = 128)
  private String title;

  @OneToMany(cascade = CascadeType.ALL, mappedBy = "event")
  @OrderBy("timeDown asc")
  private List<Incident> incidentList;

  @JoinColumn(name = "EVENT_ID", referencedColumnName = "EVENT_ID", nullable = true)
  @OneToOne(optional = false, fetch = FetchType.LAZY)
  private EventTimeDown eventTimeDown;

  @Transient private long restoreMillis = 0;
  @Transient private User closedBy = null;

  public Event() {}

  public Event(BigInteger eventId, EventType eventType, Date timeUp) {
    this.eventId = eventId;
    this.eventType = eventType;
    this.timeUp = timeUp;
  }

  public long getRestoreMillis() {
    return restoreMillis;
  }

  public void setRestoreMillis(long restoreMillis) {
    this.restoreMillis = restoreMillis;
  }

  public User getClosedBy() {
    return closedBy;
  }

  public void setClosedBy(User closedBy) {
    this.closedBy = closedBy;
  }

  private Date computeTimeDown() {
    Date td = null;

    if (incidentList != null && !incidentList.isEmpty()) {
      td = incidentList.get(0).getTimeDown();
      for (int i = 1; i < incidentList.size(); i++) {
        Incident incident = incidentList.get(i);
        if (td.after(incident.getTimeDown())) {
          td = incident.getTimeDown();
        }
      }
    }

    return td;
  }

  private boolean computeReviewed() {
    boolean reviewed = true;

    if (incidentList != null && !incidentList.isEmpty()) {
      for (Incident incident : incidentList) {
        if (incident.getReviewedUsername() == null) {
          reviewed = false;
          break;
        }
      }
    }

    return reviewed;
  }

  private boolean computeAtLeastOneReview() {
    boolean atLeastOne = false;

    if (incidentList != null && !incidentList.isEmpty()) {
      for (Incident incident : incidentList) {
        if (incident.getReviewedUsername() != null) {
          atLeastOne = true;
          break;
        }
      }
    }

    return atLeastOne;
  }

  /**
   * This value is computed from the incidentList, so be sure it is already loaded or else you'll
   * get an entity manager proxy error (LazyInitializationException).
   *
   * <p>The incident with the oldest time down is assigned as the event time down.
   *
   * @return The computed time down
   */
  public Date getTimeDown() {
    return computeTimeDown();
  }

  /**
   * Computed value; event is reviewed only if all incidents within are reviewed.
   *
   * @return The computed reviewed state
   */
  public boolean isReviewed() {
    return computeReviewed();
  }

  public boolean isExpertReviewed() {
    boolean reviewed = true;

    if (incidentList != null && !incidentList.isEmpty()) {
      for (Incident incident : incidentList) {
        if (!incident.isExpertReviewed()) {
          reviewed = false;
          break;
        }
      }
    }

    return reviewed;
  }

  public boolean containsAtLeastOneExpertReview() {
    boolean atLeastOne = false;

    if (incidentList != null && !incidentList.isEmpty()) {
      for (Incident incident : incidentList) {
        if (incident.isExpertReviewed()) {
          atLeastOne = true;
          break;
        }
      }
    }

    return atLeastOne;
  }

  public boolean containsAtLeastOneReview() {
    return computeAtLeastOneReview();
  }

  public boolean closedLongAgo() {
    final long MILLIS_PER_10_HOURS = 36000000;
    return (timeUp != null && timeUp.getTime() < (new Date()).getTime() - MILLIS_PER_10_HOURS);
  }

  public Date getMaxIncidentTimeUp() {
    Date maxTimeUp = null;

    if (incidentList != null && !incidentList.isEmpty()) {
      for (Incident incident : incidentList) {
        if (incident.getTimeUp() == null) {
          maxTimeUp = null;
          break;
        } else if (maxTimeUp == null || (incident.getTimeUp().after(maxTimeUp))) {
          maxTimeUp = incident.getTimeUp();
        }
      }
    }

    return maxTimeUp;
  }

  public long getElapsedMillis() {
    Date tu = timeUp;

    if (tu == null) {
      tu = new Date();
    }

    if (getTimeDown() == null) {
      System.out.println("TimeDown null in event: " + eventId);
      return 0;
    }

    return tu.getTime() - getTimeDown().getTime();
  }

  public BigInteger getEventId() {
    return eventId;
  }

  public void setEventId(BigInteger eventId) {
    this.eventId = eventId;
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

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public List<Incident> getIncidentList() {
    return incidentList;
  }

  public void setIncidentList(List<Incident> incidentList) {
    this.incidentList = incidentList;
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
    if (!(object instanceof Event)) {
      return false;
    }
    Event other = (Event) object;
    return (this.eventId != null || other.eventId == null)
        && (this.eventId == null || this.eventId.equals(other.eventId));
  }

  @Override
  public String toString() {
    return "Event{"
        + "eventId="
        + eventId
        + ", timeUp="
        + timeUp
        + ", eventType="
        + eventType
        + '}';
  }

  public boolean isClosed() {
    return (getTimeUp() != null);
  }
}
