package org.jlab.dtm.persistence.filter;

import java.math.BigInteger;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.jlab.smoothness.business.util.TimeUtil;

/**
 * @author ryans
 */
public class FsdExceptionTrendFilter {

  private final Date start;
  private final Date end;
  private final boolean groupByCategory;
  private final BigInteger[] categoryIdArray;

  public FsdExceptionTrendFilter(
      Date start, Date end, boolean groupByCategory, BigInteger[] categoryIdArray) {
    this.start = start;
    this.end = end;
    this.groupByCategory = groupByCategory;
    this.categoryIdArray = categoryIdArray;
  }

  public String getSqlWhereClause() {
    String filter = "";

    List<String> filters = new ArrayList<String>();

    if (start != null) {
      filters.add("start_utc < ? ");
    }

    if (end != null) {
      filters.add("coalesce(end_utc, sysdate) >= ? ");
    }

    if (!filters.isEmpty()) {
      filter = "where " + filters.get(0);

      if (filters.size() > 1) {
        for (int i = 1; i < filters.size(); i++) {
          filter = filter + "and " + filters.get(i);
        }
      }
    }

    return filter;
  }

  public void assignParameterValues(PreparedStatement stmt) throws SQLException {
    int i = 1;

    if (end != null) {
      stmt.setDate(i++, new java.sql.Date(end.getTime()), TimeUtil.getUtcCalendar());
    }

    if (start != null) {
      stmt.setDate(i++, new java.sql.Date(start.getTime()), TimeUtil.getUtcCalendar());
    }
  }

  public String getSelectionMessage() {
    List<String> filters = new ArrayList<String>();

    String dateFormat = TimeUtil.getFriendlyDateTimePattern();

    SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);

    String message = "";

    message = formatter.format(start) + " to " + formatter.format(end);

    if (!filters.isEmpty()) {
      message = message + " where";
      for (String filter : filters) {
        message += " " + filter + " and";
      }

      // Remove trailing " and"
      message = message.substring(0, message.length() - 4);
    }

    return message;
  }
}
