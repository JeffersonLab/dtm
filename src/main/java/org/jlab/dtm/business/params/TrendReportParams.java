package org.jlab.dtm.business.params;

import java.util.Date;

public class TrendReportParams {
  private Date start;
  private Date end;
  private String type;
  private String size;
  private Boolean includeCategories;

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

  public Boolean getIncludeCategories() {
    return includeCategories;
  }

  public void setIncludeCategories(Boolean includeCategories) {
    this.includeCategories = includeCategories;
  }
}
