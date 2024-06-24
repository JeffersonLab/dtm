package org.jlab.dtm.persistence.model;

import java.math.BigInteger;
import java.util.Date;

/**
 * @author ryans
 */
public class EventDowntime {

  private final BigInteger eventId;
  private final double downtimeHours;
  private final double restoreHoursBounded;
  private final long incidentCount;
  private final String title;
  private final Date timeDown;
  private final Date timeUp;
  private final Date timeDownBounded;
  private final Date timeUpBounded;
  private final double downtimeHoursBounded;

  public EventDowntime(
      Number eventId,
      Number downtimeHours,
      Number restoreHoursBounded,
      Number incidentCount,
      String title,
      Date timeDown,
      Date timeUp,
      Date timeDownBounded,
      Date timeUpBounded,
      Number downtimeHoursBounded) {
    this.eventId = new BigInteger(eventId.toString());
    this.downtimeHours = downtimeHours.doubleValue();
    this.restoreHoursBounded = restoreHoursBounded.doubleValue();
    this.incidentCount = incidentCount.longValue();
    this.title = title;
    this.timeDown = timeDown;
    this.timeUp = timeUp;
    this.timeDownBounded = timeDownBounded;
    this.timeUpBounded = timeUpBounded;
    this.downtimeHoursBounded = downtimeHoursBounded.doubleValue();
  }

  public BigInteger getEventId() {
    return eventId;
  }

  public double getDowntimeHours() {
    return downtimeHours;
  }

  public double getSuspendHoursBounded() {
    return downtimeHours - restoreHoursBounded;
  }

  public double getRestoreHoursBounded() {
    return restoreHoursBounded;
  }

  public long getIncidentCount() {
    return incidentCount;
  }

  public String getTitle() {
    return title;
  }

  public Date getTimeDown() {
    return timeDown;
  }

  public Date getTimeUp() {
    return timeUp;
  }

  public Date getTimeDownBounded() {
    return timeDownBounded;
  }

  public Date getTimeUpBounded() {
    return timeUpBounded;
  }

  public double getDowntimeHoursBounded() {
    return downtimeHoursBounded;
  }
}
