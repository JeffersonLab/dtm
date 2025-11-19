package org.jlab.dtm.business.params;

import java.math.BigInteger;
import java.util.Date;

public class ComponentDowntimeReportParams {
  private Date start;
  private Date end;
  BigInteger[] eventTypeIdArray;
  Boolean beamTransport;
  BigInteger systemId;
  String chart;
  String data;

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

  public BigInteger[] getEventTypeIdArray() {
    return eventTypeIdArray;
  }

  public void setEventTypeIdArray(BigInteger[] eventTypeIdArray) {
    this.eventTypeIdArray = eventTypeIdArray;
  }

  public Boolean getBeamTransport() {
    return beamTransport;
  }

  public void setBeamTransport(Boolean beamTransport) {
    this.beamTransport = beamTransport;
  }

  public BigInteger getSystemId() {
    return systemId;
  }

  public void setSystemId(BigInteger systemId) {
    this.systemId = systemId;
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
}
