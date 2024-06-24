package org.jlab.dtm.business.service;

import java.math.BigInteger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jlab.dtm.persistence.model.AnnualRepairReportRecord;
import org.jlab.dtm.persistence.util.DtmSqlUtil;
import org.jlab.smoothness.business.util.DateIterator;
import org.jlab.smoothness.business.util.IOUtil;
import org.jlab.smoothness.business.util.TimeUtil;

public class AnnualRepairReportService {

  private static final Logger logger = Logger.getLogger(AnnualRepairReportService.class.getName());

  public List<AnnualRepairReportRecord> find(Date start, Date end) throws SQLException {

    LinkedHashMap<Date, HashMap<String, AnnualRepairReportRecord>> recordMap =
        new LinkedHashMap<>();

    Connection con = null;
    PreparedStatement stmt = null;
    ResultSet rs = null;

    // Note 1: We query all incidents in the period and then do the group by in memory on the
    // application server.  This makes it easier to group by a date interval.
    String sql =
        "select a.time_down, a.time_up, c.name as category, category_id "
            + "from incident a inner join system_alpha_category b using(system_id) inner join category c using(category_id) inner join event d using(event_id) "
            + "where d.event_type_id = 1 "
            + "and a.time_down < ? "
            + "and nvl(a.time_up, sysdate) >= ? "
            + "order by a.time_down asc";

    logger.log(Level.FINEST, "Query: {0}", sql);

    try {
      con = DtmSqlUtil.getConnection();

      stmt = con.prepareStatement(sql);

      java.sql.Date starti = new java.sql.Date(start.getTime());
      java.sql.Date endi = new java.sql.Date(end.getTime());

      stmt.setDate(1, endi);
      stmt.setDate(2, starti);

      stmt.setFetchSize(10000); // Fetch a huge amount at a time.

      rs = stmt.executeQuery();

      List<MonthChunk> chunkList = new ArrayList<>();

      // Step 1 is to get data from SQL then break apart intervals into chunks (some intervals =
      // more than one chunk if they span multiple months)
      while (rs.next()) {
        Date timeDown = rs.getDate(1);
        Date timeUp = rs.getDate(2);
        String category = rs.getString(3);
        BigInteger categoryId = rs.getBigDecimal(4).toBigInteger();

        if (timeDown.before(start)) {
          timeDown = start;
        }

        if (timeUp == null) {
          timeUp = new Date();
        }

        if (timeUp.after(end)) {
          timeUp = end;
        }

        List<MonthChunk> intermediateChunkList = getChunks(timeDown, timeUp, category, categoryId);
        chunkList.addAll(intermediateChunkList);
      }

      // Step 2 is to break into month bins
      for (MonthChunk chunk : chunkList) {

        Date timeDown = chunk.timeDown;
        Date timeUp = chunk.timeUp;
        String category = chunk.category;
        BigInteger categoryId = chunk.categoryId;

        Date binDate = TimeUtil.startOfMonth(timeDown, Calendar.getInstance());

        final double MILLISECONDS_PER_HOUR = 3600000;

        double downtimeHours = (timeUp.getTime() - timeDown.getTime()) / MILLISECONDS_PER_HOUR;

        // SimpleDateFormat formatter = new SimpleDateFormat("MMM yyyy");
        // SimpleDateFormat formatter2 = new
        // SimpleDateFormat(TimeUtil.getFriendlyDateTimePattern());
        // System.out.println("---------------------");
        // System.out.println("TimeDown: " + formatter2.format(timeDown));
        // System.out.println("TimeUp: " + formatter2.format(timeUp));
        // System.out.println("Month: " + formatter.format(binDate));
        // System.out.println("Category: " + category);
        // System.out.println("Downtime: " + downtimeHours);

        HashMap<String, AnnualRepairReportRecord> groupingMap = recordMap.get(binDate);

        if (groupingMap == null) {
          groupingMap = new HashMap<>();
          recordMap.put(binDate, groupingMap);
        }

        AnnualRepairReportRecord binBlock = groupingMap.get(category);

        if (binBlock == null) {
          binBlock = new AnnualRepairReportRecord(category, categoryId, binDate, downtimeHours);
          groupingMap.put(category, binBlock);
        } else {
          binBlock.addDowntimeHours(downtimeHours);
        }
      }

    } finally {
      IOUtil.close(rs, stmt, con);
    }

    // Step 3 is to iterate over bins (months), sort the categories for each month, and create a
    // flat list of records
    List<AnnualRepairReportRecord> recordList = new ArrayList<>();

    for (Date dateKey : recordMap.keySet()) { // Dates are already ordered...
      HashMap<String, AnnualRepairReportRecord> groupingMap = recordMap.get(dateKey);
      List<String> keyList = new ArrayList<>(groupingMap.keySet());
      Collections.sort(keyList);
      for (String grouping : keyList) {
        recordList.add(groupingMap.get(grouping));
      }
    }

    return recordList;
  }

  private List<MonthChunk> getChunks(
      Date timeDown, Date timeUp, String category, BigInteger categoryId) {
    List<MonthChunk> chunkList = new ArrayList<>();

    Date startMonthDayOne = TimeUtil.startOfMonth(timeDown, Calendar.getInstance());
    Date endMonthDayOne = TimeUtil.startOfMonth(timeUp, Calendar.getInstance());

    // We have use an open end of interval so don't include final month if day one
    if (endMonthDayOne.equals(timeUp)) {
      endMonthDayOne = TimeUtil.addMonths(timeUp, -1);
    }

    DateIterator iterator = new DateIterator(startMonthDayOne, endMonthDayOne, Calendar.MONTH);

    // SimpleDateFormat formatter = new SimpleDateFormat(TimeUtil.getFriendlyDateTimePattern());

    // System.out.println("+++++++++++++++++++++++++++++");
    // System.out.println("Category: " + category);
    // System.out.println("timeDown: " + formatter.format(timeDown));
    // System.out.println("timeUp: " + formatter.format(timeUp));

    while (iterator.hasNext()) {
      Date month = iterator.next();
      // System.out.println("Month: " + formatter.format(month));

      Date startOfMonth = TimeUtil.startOfMonth(month, Calendar.getInstance());
      Date startOfNextMonth = TimeUtil.startOfNextMonth(month, Calendar.getInstance());

      // System.out.println("Start of Month: " + formatter.format(startOfMonth));
      // System.out.println("End (start of next Month): " + formatter.format(startOfNextMonth));

      Date start = (startMonthDayOne.getTime() == month.getTime()) ? timeDown : startOfMonth;
      Date end = iterator.hasNext() ? startOfNextMonth : timeUp;

      // System.out.println("start: " + formatter.format(start));
      // System.out.println("end: " + formatter.format(end));

      MonthChunk chunk = new MonthChunk(start, end, category, categoryId);
      chunkList.add(chunk);
    }

    return chunkList;
  }

  private class MonthChunk {

    Date timeDown;
    Date timeUp;
    String category;
    BigInteger categoryId;

    public MonthChunk(Date timeDown, Date timeUp, String category, BigInteger categoryId) {
      this.timeDown = timeDown;
      this.timeUp = timeUp;
      this.category = category;
      this.categoryId = categoryId;
    }
  }
}
