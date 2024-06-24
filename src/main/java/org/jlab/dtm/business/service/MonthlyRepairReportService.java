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
import org.jlab.dtm.persistence.model.MonthlyRepairReportRecord;
import org.jlab.dtm.persistence.util.DtmSqlUtil;
import org.jlab.smoothness.business.util.DateIterator;
import org.jlab.smoothness.business.util.IOUtil;
import org.jlab.smoothness.business.util.TimeUtil;

public class MonthlyRepairReportService {

  private static final Logger LOGGER = Logger.getLogger(MonthlyRepairReportService.class.getName());

  public List<MonthlyRepairReportRecord> find(Date start, Date end) throws SQLException {

    LinkedHashMap<Date, HashMap<String, MonthlyRepairReportRecord>> recordMap =
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

    LOGGER.log(Level.FINEST, "Query: {0}", sql);

    try {
      con = DtmSqlUtil.getConnection();

      stmt = con.prepareStatement(sql);

      java.sql.Date starti = new java.sql.Date(start.getTime());
      java.sql.Date endi = new java.sql.Date(end.getTime());

      stmt.setDate(1, endi);
      stmt.setDate(2, starti);

      stmt.setFetchSize(10000); // Fetch a huge amount at a time.

      rs = stmt.executeQuery();

      List<DayChunk> chunkList = new ArrayList<>();

      // Step 1 is to get data from SQL then break apart intervals into chunks (some intervals =
      // more than one chunk if they span multiple days)
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

        List<DayChunk> intermediateChunkList = getChunks(timeDown, timeUp, category, categoryId);
        chunkList.addAll(intermediateChunkList);
      }

      // Step 2 is to break into day bins
      for (DayChunk chunk : chunkList) {

        Date timeDown = chunk.timeDown;
        Date timeUp = chunk.timeUp;
        String category = chunk.category;
        BigInteger categoryId = chunk.categoryId;

        Date binDate = TimeUtil.startOfDay(timeDown, Calendar.getInstance());

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

        HashMap<String, MonthlyRepairReportRecord> groupingMap = recordMap.get(binDate);

        if (groupingMap == null) {
          groupingMap = new HashMap<>();
          recordMap.put(binDate, groupingMap);
        }

        MonthlyRepairReportRecord binBlock = groupingMap.get(category);

        if (binBlock == null) {
          binBlock = new MonthlyRepairReportRecord(category, categoryId, binDate, downtimeHours);
          groupingMap.put(category, binBlock);
        } else {
          binBlock.addDowntimeHours(downtimeHours);
        }
      }

    } finally {
      IOUtil.close(rs, stmt, con);
    }

    // Step 3 is to iterate over bins (days), sort the categories for each day, and create a flat
    // list of records
    List<MonthlyRepairReportRecord> recordList = new ArrayList<>();

    for (Date dateKey : recordMap.keySet()) { // Dates are already ordered...
      HashMap<String, MonthlyRepairReportRecord> groupingMap = recordMap.get(dateKey);
      List<String> keyList = new ArrayList<>(groupingMap.keySet());
      Collections.sort(keyList);
      for (String grouping : keyList) {
        recordList.add(groupingMap.get(grouping));
      }
    }

    return recordList;
  }

  private List<DayChunk> getChunks(
      Date timeDown, Date timeUp, String category, BigInteger categoryId) {
    List<DayChunk> chunkList = new ArrayList<>();

    Date startDayHourZero = TimeUtil.startOfDay(timeDown, Calendar.getInstance());
    Date endDayHourZero = TimeUtil.startOfDay(timeUp, Calendar.getInstance());

    // We have use an open end of interval so don't include final month if day one
    if (endDayHourZero.equals(timeUp)) {
      endDayHourZero = TimeUtil.addDays(timeUp, -1);
    }

    DateIterator iterator = new DateIterator(startDayHourZero, endDayHourZero, Calendar.DATE);

    // SimpleDateFormat formatter = new SimpleDateFormat(TimeUtil.getFriendlyDateTimePattern());

    // System.out.println("+++++++++++++++++++++++++++++");
    // System.out.println("Category: " + category);
    // System.out.println("timeDown: " + formatter.format(timeDown));
    // System.out.println("timeUp: " + formatter.format(timeUp));

    while (iterator.hasNext()) {
      Date day = iterator.next();
      // System.out.println("Month: " + formatter.format(month));

      Date startOfDay = TimeUtil.startOfDay(day, Calendar.getInstance());
      Date startOfNextDay = TimeUtil.startOfNextDay(day, Calendar.getInstance());

      // System.out.println("Start of Month: " + formatter.format(startOfMonth));
      // System.out.println("End (start of next Month): " + formatter.format(startOfNextMonth));

      Date start = (startDayHourZero.getTime() == day.getTime()) ? timeDown : startOfDay;
      Date end = iterator.hasNext() ? startOfNextDay : timeUp;

      // System.out.println("start: " + formatter.format(start));
      // System.out.println("end: " + formatter.format(end));

      DayChunk chunk = new DayChunk(start, end, category, categoryId);
      chunkList.add(chunk);
    }

    return chunkList;
  }

  private class DayChunk {

    Date timeDown;
    Date timeUp;
    String category;
    BigInteger categoryId;

    public DayChunk(Date timeDown, Date timeUp, String category, BigInteger categoryId) {
      this.timeDown = timeDown;
      this.timeUp = timeUp;
      this.category = category;
      this.categoryId = categoryId;
    }
  }
}
