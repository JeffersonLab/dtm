package org.jlab.dtm.persistence.model;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * @author ryans
 */
public class MonthYear {
  private final Calendar cal = Calendar.getInstance();

  public MonthYear(Date date) {
    this(date, TimeZone.getDefault());
  }

  public MonthYear(int monthStartAtOne, int year, TimeZone timezone) {
    cal.setTimeZone(timezone);
    cal.set(Calendar.MONTH, monthStartAtOne - 1);
    cal.set(Calendar.YEAR, year);
    myOwnVersionOfCalendarClear();
  }

  public MonthYear(Date date, TimeZone timezone) {
    cal.setTimeZone(timezone);
    cal.setTime(date);
    myOwnVersionOfCalendarClear();
  }

  private void myOwnVersionOfCalendarClear() {
    cal.set(Calendar.DATE, 1);
    cal.set(Calendar.HOUR, 0);
    cal.set(Calendar.MINUTE, 0);
    cal.set(Calendar.MILLISECOND, 0);
  }

  public int getMonthOfYearStartAtZero() {
    return cal.get(Calendar.MONTH);
  }

  public int getMonthOfYearStartAtOne() {
    return cal.get(Calendar.MONTH) + 1;
  }

  public int getYear() {
    return cal.get(Calendar.YEAR);
  }

  public void addMonths(int a) {
    cal.add(Calendar.MONTH, a);
  }

  public Date getTime() {
    return cal.getTime();
  }

  public TimeZone getTimeZone() {
    return cal.getTimeZone();
  }
}
