package org.jlab.dtm.persistence.model;

/**
 * @author ryans
 */
public class ComponentDowntime {
  private long
      id; /*Don't actually use ID, but need it in group by because component name is NOT guarenteed unique!*/
  private String name;
  private String systemName;
  private long incidentCount;
  private double duration;

  public ComponentDowntime(
      Number id, String name, String systemName, Number incidentCount, Number duration) {
    this.id = id.longValue();
    this.name = name;
    this.systemName = systemName;
    this.incidentCount = incidentCount.longValue();
    this.duration = duration.doubleValue();
  }

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getSystemName() {
    return systemName;
  }

  public void setSystemName(String systemName) {
    this.systemName = systemName;
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
