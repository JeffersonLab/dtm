package org.jlab.dtm.persistence.model;

import java.io.Serializable;
import java.math.BigInteger;
import org.jlab.dtm.persistence.enumeration.EscalationLevel;

/**
 * @author ryans
 */
public class EscalationInfo implements Serializable {
  private static final long serialVersionUID = 2L;
  private final BigInteger eventId;
  private final String eventTitle;
  private final EscalationLevel level;

  public EscalationInfo(BigInteger eventId, String eventTitle, EscalationLevel level) {
    this.eventId = eventId;
    this.eventTitle = eventTitle;
    this.level = level;
  }

  public BigInteger getEventId() {
    return eventId;
  }

  public String getEventTitle() {
    return eventTitle;
  }

  public EscalationLevel getEscalationLevel() {
    return level;
  }

  @Override
  public String toString() {
    return "EscalationInfo{"
        + "eventId="
        + eventId
        + ", eventTitle="
        + eventTitle
        + ", level="
        + level
        + '}';
  }
}
