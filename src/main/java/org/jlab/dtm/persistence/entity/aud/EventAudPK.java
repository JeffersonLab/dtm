package org.jlab.dtm.persistence.entity.aud;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.Objects;

/**
 * @author ryans
 */
@Embeddable
public class EventAudPK implements Serializable {
  @Basic(optional = false)
  @NotNull
  @Column(name = "EVENT_ID", nullable = false)
  private BigInteger eventId;

  @Basic(optional = false)
  @NotNull
  @Column(name = "REV", nullable = false)
  private BigInteger rev;

  public EventAudPK() {}

  public EventAudPK(BigInteger eventId, BigInteger rev) {
    this.eventId = eventId;
    this.rev = rev;
  }

  public BigInteger getEventId() {
    return eventId;
  }

  public void setEventId(BigInteger eventId) {
    this.eventId = eventId;
  }

  public BigInteger getRev() {
    return rev;
  }

  public void setRev(BigInteger rev) {
    this.rev = rev;
  }

  @Override
  public int hashCode() {
    int hash = 3;
    hash = 23 * hash + (this.eventId != null ? this.eventId.hashCode() : 0);
    hash = 23 * hash + (this.rev != null ? this.rev.hashCode() : 0);
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
    final EventAudPK other = (EventAudPK) obj;
    if (!Objects.equals(this.eventId, other.eventId)) {
      return false;
    }
    return Objects.equals(this.rev, other.rev);
  }
}
