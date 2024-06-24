package org.jlab.dtm.persistence.model;

import java.util.Date;

/**
 * @author ryans
 */
public class Period {
  private Date startDate;
  private Date endDate;

  public Period(Date startDate, Date endDate) {
    this.startDate = startDate;
    this.endDate = endDate;
  }

  public Date getStartDate() {
    return startDate;
  }

  public void setStartDate(Date startDate) {
    this.startDate = startDate;
  }

  public Date getEndDate() {
    return endDate;
  }

  public void setEndDate(Date endDate) {
    this.endDate = endDate;
  }
}
