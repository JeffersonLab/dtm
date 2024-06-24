package org.jlab.dtm.persistence.model;

import java.math.BigInteger;
import java.util.Date;

/**
 * @author ryans
 */
public class MonthlyRepairReportRecord {
  private final String category;
  private final BigInteger categoryId;
  private final Date day; // Year is important too so we use Date object to encapsulate both
  private double downtimeHours;

  public MonthlyRepairReportRecord(
      String category, BigInteger categoryId, Date day, double downtimeHours) {
    this.category = category;
    this.categoryId = categoryId;
    this.day = day;
    this.downtimeHours = downtimeHours;
  }

  public String getCategory() {
    return category;
  }

  public BigInteger getCategoryId() {
    return categoryId;
  }

  public Date getDay() {
    return day;
  }

  public double getDowntimeHours() {
    return downtimeHours;
  }

  public void addDowntimeHours(double hours) {
    downtimeHours = downtimeHours + hours;
  }
}
