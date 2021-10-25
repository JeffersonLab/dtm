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
import org.jlab.dtm.business.params.FsdSummaryReportParams;
import org.jlab.dtm.persistence.enumeration.BinSize;
import org.jlab.dtm.persistence.model.FsdTrip;
import org.jlab.dtm.persistence.util.DtmSqlUtil;
import org.jlab.smoothness.business.util.IOUtil;
import org.jlab.smoothness.business.util.TimeUtil;

public class FsdTripTrendService {

    private static final Logger LOGGER = Logger.getLogger(
            FsdTripTrendService.class.getName());

    public class TripHistogramBin {

        private Date start;
        private int count;
        private long durationMillis;
        private String grouping;

        public Date getStart() {
            return start;
        }

        public void setStart(Date start) {
            this.start = start;
        }

        public int getCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
        }

        public long getDurationMillis() {
            return durationMillis;
        }

        public void setDurationMillis(long durationMillis) {
            this.durationMillis = durationMillis;
        }

        public String getGrouping() {
            return grouping;
        }

        public void setGrouping(String grouping) {
            this.grouping = grouping;
        }

    }

    public List<TripHistogramBin> findTrendListByPeriodInMemory(FsdSummaryReportParams params) throws
            SQLException {

        FsdTripService tripService = new FsdTripService();

        long updateStart = System.currentTimeMillis();
        tripService.updateCause();
        long updateEnd = System.currentTimeMillis();
        LOGGER.log(Level.FINEST, "FSD report update cause seconds: {0}", (updateEnd
                - updateStart) / 1000.0f);

        tripService.updateArea();

        // Linked Hash Map maintains insertion order, which we need;  
        // We have to sort grouping map since insertion order isn't what we want in that case so
        // don't bother with overhead of linked hash map
        LinkedHashMap<Date, HashMap<String, TripHistogramBin>> tripMap = new LinkedHashMap<>();

        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        // Note we only grab trips that are closed for simplicity and to avoid issues with computing duration
        String sql;

        Integer maxDurationMillis = null;

        final int MILLIS_PER_SECOND = 1000;
        final int MILLIS_PER_MINUTE = 60000;
        final int MILLIS_PER_HOUR = 3600000;

        if (params.getMaxDuration() != null) {
            if ("Seconds".equals(params.getMaxDurationUnits())) {
                maxDurationMillis = params.getMaxDuration() * MILLIS_PER_SECOND;
            } else if ("Minutes".equals(params.getMaxDurationUnits())) {
                maxDurationMillis = params.getMaxDuration() * MILLIS_PER_MINUTE;
            } else if ("Hours".equals(params.getMaxDurationUnits())) {
                maxDurationMillis = params.getMaxDuration() * MILLIS_PER_HOUR;
            }
        }

        boolean includeSadTrips = params.getSadTrips() != null ? params.getSadTrips() : false;

        String causeList = null;
        if (params.getCauseArray() != null && params.getCauseArray().length > 0) {
            causeList = "'" + params.getCauseArray()[0].getLabel() + "'";

            for (int i = 1; i < params.getCauseArray().length; i++) {
                causeList = causeList + ",'" + params.getCauseArray()[i].getLabel() + "'";
            }
        }

        // If doing max types query do it a whole different way
        if (params.getMaxTypes() != null) {
            sql = "select fsd_trip_id, start_utc, end_utc, cause, area "
                    + "from ("
                    + "select fsd_trip_id, start_utc, end_utc, cause, area, ced_type from fsd_trip left join fsd_fault using(fsd_trip_id) "
                    + "left join fsd_device_exception using(fsd_fault_id) where "
                    + "start_utc < ? and start_utc >= ? "
                    + "and end_utc is not null and cause is not null ";

            if (maxDurationMillis != null) {
                sql = sql + "and ((end_utc - start_utc) * 86400000) <= " + maxDurationMillis + " ";
            }

            if (!includeSadTrips) {
                sql = sql + "and acc_state <> 'OFF' ";
            }

            if (causeList != null) {
                sql = sql + "and cause in (" + causeList + ") ";
            }

            sql = sql
                    + "group by fsd_trip_id, start_utc, end_utc, cause, area, ced_type) "
                    + "group by fsd_trip_id, start_utc, end_utc, cause, area having count(ced_type) <= "
                    + params.getMaxTypes() + " ";
        } else { // Not filtering by max types
            sql = "select fsd_trip_id, start_utc, end_utc, cause, area "
                    + "from fsd_trip a "
                    + "where start_utc < ? "
                    + "and start_utc >= ? and end_utc is not null and cause is not null ";

            if (maxDurationMillis != null) {
                sql = sql + "and ((end_utc - start_utc) * 86400000) <= " + maxDurationMillis + " ";
            }

            if (!includeSadTrips) {
                sql = sql + "and acc_state <> 'OFF' ";
            }

            if (causeList != null) {
                sql = sql + "and cause in (" + causeList + ") ";
            }
        }

        sql = sql + "order by start_utc asc";

        LOGGER.log(Level.FINEST,
                "Query: {0}", sql);

        try {
            con = DtmSqlUtil.getConnection();

            stmt = con.prepareStatement(sql, ResultSet.TYPE_FORWARD_ONLY,
                    ResultSet.CONCUR_READ_ONLY);

            java.sql.Date startSql = new java.sql.Date(params.getStart().getTime());
            java.sql.Date endSql = new java.sql.Date(params.getEnd().getTime());

            stmt.setDate(1, endSql, TimeUtil.getUtcCalendar());
            stmt.setDate(2, startSql, TimeUtil.getUtcCalendar());

            stmt.setFetchSize(10000); // Fetch a huge amount at a time.

            long queryStart = System.currentTimeMillis();
            rs = stmt.executeQuery();
            long queryEnd = System.currentTimeMillis();
            LOGGER.log(Level.FINEST, "FSD report query seconds: {0}", (queryEnd
                    - queryStart) / 1000.0f);

            long binningStart = System.currentTimeMillis();

            while (rs.next()) {
                Date tripStart = rs.getDate(2, TimeUtil.getUtcCalendar());
                Date tripEnd = rs.getDate(3, TimeUtil.getUtcCalendar());

                /*System.out.println("tripStart: " + tripStart.getTime());*/
                if (tripEnd == null) {
                    tripEnd = TimeUtil.getUtcCalendar().getTime();
                }

                long durationMillis = tripEnd.getTime() - tripStart.getTime();

                String cause = rs.getString(4);

                String area = rs.getString("area");

                Date binDate;
                if (BinSize.HOUR.equals(params.getBinSize())) { // We are doing an hourly histogram so use most recent top of hour (truncate minutes/seconds/millis)
                    binDate = TimeUtil.startOfHour(tripStart, TimeUtil.getUtcCalendar()); // Important to use UTC Calendar so that double 1 AM is possible (truncate to hour in utc not local)
                } else if(BinSize.DAY.equals(params.getBinSize())) { // We are doing a daily histogram so truncate hours/minutes/seconds/millis
                    binDate = TimeUtil.startOfDay(tripStart, Calendar.getInstance());
                    /*truncate to local time midnight*/
                } else { // Monthly
                    binDate = TimeUtil.startOfMonth(tripStart, Calendar.getInstance());
                }

                /*System.out.println("binDate: " + binDate.getTime());*/
                String grouping = null;
                if ("cause".equals(params.getGrouping())) {
                    grouping = cause;
                } else if("area".equals(params.getGrouping())) {
                    grouping = area;
                }

                HashMap<String, TripHistogramBin> groupingMap = tripMap.get(binDate);

                if (groupingMap == null) {
                    groupingMap = new HashMap<>();
                    tripMap.put(binDate, groupingMap);
                }

                TripHistogramBin bin = groupingMap.get(grouping);

                if (bin == null) {
                    bin = new TripHistogramBin();
                    bin.start = binDate;
                    bin.grouping = grouping;
                    groupingMap.put(grouping, bin);
                }

                bin.count++;
                bin.durationMillis = bin.durationMillis + durationMillis;
            }

            long binningEnd = System.currentTimeMillis();
            LOGGER.log(Level.FINEST, "FSD report binning seconds: {0}", (binningEnd
                    - binningStart) / 1000.0f);

        } finally {
            IOUtil.close(rs, stmt, con);
        }

        long sortingStart = System.currentTimeMillis();

        List<TripHistogramBin> trendList = new ArrayList<>();

        for (Date dateKey
                : tripMap.keySet()) { // Dates are already ordered...
            HashMap<String, TripHistogramBin> groupingMap = tripMap.get(dateKey);
            List<String> keyList = new ArrayList<>(groupingMap.keySet());
            Collections.sort(keyList, Collections.reverseOrder());
            for (String grouping : keyList) {
                trendList.add(groupingMap.get(grouping));
            }
        }

        long sortingEnd = System.currentTimeMillis();

        LOGGER.log(Level.FINEST,
                "FSD report sorting seconds: {0}", (sortingEnd
                - sortingStart) / 1000.0f);

        return trendList;
    }
}
