package org.jlab.dtm.persistence.model;

/**
 * @author ryans
 */
public class CategoryDowntime {

  private String name;
  private long id;
  private long incidentCount;
  private double duration;

  public CategoryDowntime(String name, Number id, Number incidentCount, Number duration) {
    this.name = name;
    this.id = id.longValue();
    this.incidentCount = incidentCount.longValue();
    this.duration = duration.doubleValue();
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public long getIncidentCount() {
    return incidentCount;
  }

  public void setIncidentCount(long incidentCount) {
    this.incidentCount = incidentCount;
  }

  public double getDuration() {
    return duration;
  }

  public void setDuration(double duration) {
    this.duration = duration;
  }
}
