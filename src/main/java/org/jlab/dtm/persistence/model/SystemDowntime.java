package org.jlab.dtm.persistence.model;

/**
 *
 * @author ryans
 */
public class SystemDowntime {
    private String systemName;
    private double duration;
    private long systemId;
    private long incidentCount;

    public SystemDowntime(String systemName, Number systemId, Number incidentCount, Number duration) {
        this.systemName = systemName;
        this.systemId = systemId.longValue();
        this.incidentCount = incidentCount.longValue();
        this.duration = duration.doubleValue();
    }

    public String getSystemName() {
        return systemName;
    }

    public void setSystemName(String systemName) {
        this.systemName = systemName;
    }

    public long getSystemId() {
        return systemId;
    }

    public void setSystemId(long systemId) {
        this.systemId = systemId;
    }

    public double getDuration() {
        return duration;
    }

    public void setDuration(double duration) {
        this.duration = duration;
    }

    public long getIncidentCount() {
        return incidentCount;
    }

    public void setIncidentCount(long incidentCount) {
        this.incidentCount = incidentCount;
    }
}
