package org.jlab.dtm.business.params;

import java.util.Date;
import org.jlab.dtm.persistence.enumeration.BinSize;

public class RepairSummaryReportParams {
  private Date start;
  private Date end;
  private String grouping;
  private BinSize binSize;
  private String[] legendDataArray;
  private String chart;
  private String[] repairedByArray;

  public String getGrouping() {
    return grouping;
  }

  public void setGrouping(String grouping) {
    this.grouping = grouping;
  }

  public BinSize getBinSize() {
    return binSize;
  }

  public void setBinSize(BinSize binSize) {
    this.binSize = binSize;
  }

  public String[] getLegendDataArray() {
    return legendDataArray;
  }

  public void setLegendDataArray(String[] legendDataArray) {
    this.legendDataArray = legendDataArray;
  }

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

  public String getChart() {
    return chart;
  }

  public void setChart(String chart) {
    this.chart = chart;
  }

  public String[] getRepairedByArray() {
    return repairedByArray;
  }

  public void setRepairedByArray(String[] repairedByArray) {
    this.repairedByArray = repairedByArray;
  }
}
