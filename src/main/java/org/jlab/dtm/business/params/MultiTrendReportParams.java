package org.jlab.dtm.business.params;

import java.util.Date;

public class MultiTrendReportParams {
  private Date[] startArray;
  private Date[] endArray;
  private String[] labelArray;
  private String type;
  private String size;

  public Date[] getStartArray() {
    return startArray;
  }

  public void setStartArray(Date[] startArray) {
    this.startArray = startArray;
  }

  public Date[] getEndArray() {
    return endArray;
  }

  public void setEndArray(Date[] endArray) {
    this.endArray = endArray;
  }

  public String[] getLabelArray() {
    return labelArray;
  }

  public void setLabelArray(String[] labelArray) {
    this.labelArray = labelArray;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getSize() {
    return size;
  }

  public void setSize(String size) {
    this.size = size;
  }
}
