package org.jlab.dtm.business.params;

import java.math.BigInteger;
import java.util.Date;

public class TuneIncidentsParams {

  private Date start;
  private Date end;
  private BigInteger eventTypeId;
  private Boolean beamTransport;
  private Boolean overnightOpended;
  private BigInteger workgroupId;
  private BigInteger systemId;
  private String component;
  private String chart;
  private String data;
  private Integer maxDuration;
  private Integer minDuration;
  private String maxDurationUnits;
  private String minDurationUnits;

  private boolean sortByDuration = false;
  private int offset = 0;
  private int max = 10;

  public Date getStart() {
    return start;
  }

  public void setStart(Date start) {
    this.start = start;
  }

  public Date getEnd() {
    return end;
  }

  public void setEnd(Date end) {
    this.end = end;
  }

  public BigInteger getEventTypeId() {
    return eventTypeId;
  }

  public void setEventTypeId(BigInteger eventTypeId) {
    this.eventTypeId = eventTypeId;
  }

  public Boolean getBeamTransport() {
    return beamTransport;
  }

  public void setBeamTransport(Boolean beamTransport) {
    this.beamTransport = beamTransport;
  }

  public Boolean getOvernightOpended() {
    return overnightOpended;
  }

  public void setOvernightOpended(Boolean overnightOpended) {
    this.overnightOpended = overnightOpended;
  }

  public Integer getMaxDuration() {
    return maxDuration;
  }

  public void setMaxDuration(Integer maxDuration) {
    this.maxDuration = maxDuration;
  }

  public Integer getMinDuration() {
    return minDuration;
  }

  public void setMinDuration(Integer minDuration) {
    this.minDuration = minDuration;
  }

  public String getMaxDurationUnits() {
    return maxDurationUnits;
  }

  public void setMaxDurationUnits(String maxDurationUnits) {
    this.maxDurationUnits = maxDurationUnits;
  }

  public String getMinDurationUnits() {
    return minDurationUnits;
  }

  public void setMinDurationUnits(String minDurationUnits) {
    this.minDurationUnits = minDurationUnits;
  }

  public BigInteger getSystemId() {
    return systemId;
  }

  public void setSystemId(BigInteger systemId) {
    this.systemId = systemId;
  }

  public BigInteger getWorkgroupId() {
    return workgroupId;
  }

  public void setWorkgroupId(BigInteger workgroupId) {
    this.workgroupId = workgroupId;
  }

  public String getComponent() {
    return component;
  }

  public void setComponent(String component) {
    this.component = component;
  }

  public String getChart() {
    return chart;
  }

  public void setChart(String chart) {
    this.chart = chart;
  }

  public String getData() {
    return data;
  }

  public void setData(String data) {
    this.data = data;
  }

  public boolean isSortByDuration() {
    return sortByDuration;
  }

  public void setSortByDuration(boolean sortByDuration) {
    this.sortByDuration = sortByDuration;
  }

  public int getOffset() {
    return offset;
  }

  public void setOffset(int offset) {
    this.offset = offset;
  }

  public int getMax() {
    return max;
  }

  public void setMax(int max) {
    this.max = max;
  }
}
