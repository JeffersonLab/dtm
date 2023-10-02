package org.jlab.dtm.business.service;

import org.jlab.dtm.business.params.FsdSummaryReportParams;
import org.jlab.dtm.persistence.enumeration.BinSize;
import org.jlab.dtm.persistence.model.HistogramBin;
import org.jlab.dtm.persistence.util.DtmSqlUtil;
import org.jlab.smoothness.business.util.IOUtil;
import org.jlab.smoothness.business.util.TimeUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class IncidentRepairTrendService {

    private static final Logger LOGGER = Logger.getLogger(
            IncidentRepairTrendService.class.getName());

    public List<HistogramBin> findTrendListByPeriodInMemory(FsdSummaryReportParams params) throws
            SQLException {

        // Linked Hash Map maintains insertion order, which we need;  
        // We have to sort grouping map since insertion order isn't what we want in that case so
        // don't bother with overhead of linked hash map
        LinkedHashMap<Date, HashMap<String, HistogramBin>> incidentMap = new LinkedHashMap<>();

        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        // Note we only grab incidents that are closed for simplicity and to avoid issues with computing duration
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

        String causeList = null;
        if (params.getCauseArray() != null && params.getCauseArray().length > 0) {
            causeList = "'" + params.getCauseArray()[0].getLabel() + "'";

            for (int i = 1; i < params.getCauseArray().length; i++) {
                causeList = causeList + ",'" + params.getCauseArray()[i].getLabel() + "'";
            }
        }



            sql = "select incident_id, time_down, time_up, cast(greatest(a.time_down, ?) as date) as time_down_bounded, cast(least(coalesce(a.time_up, sysdate), ?) as date) as time_up_bounded "
                    + "from incident a "
                    + "where time_up is not null and time_down < ? "
                    + "and time_up > ? ";


            if (causeList != null) {
                sql = sql + "and cause in (" + causeList + ") ";
            }

        sql = sql + "order by time_down asc";

        LOGGER.log(Level.FINEST,
                "Query: {0}", sql);

        try {
            con = DtmSqlUtil.getConnection();

            stmt = con.prepareStatement(sql, ResultSet.TYPE_FORWARD_ONLY,
                    ResultSet.CONCUR_READ_ONLY);

            java.sql.Date startSql = new java.sql.Date(params.getStart().getTime());
            java.sql.Date endSql = new java.sql.Date(params.getEnd().getTime());

            stmt.setDate(1, startSql);
            stmt.setDate(2, endSql);
            stmt.setDate(3, endSql);
            stmt.setDate(4, startSql);

            stmt.setFetchSize(10000); // Fetch a huge amount at a time.

            long queryStart = System.currentTimeMillis();
            rs = stmt.executeQuery();
            long queryEnd = System.currentTimeMillis();
            LOGGER.log(Level.FINEST, "Repair report query seconds: {0}", (queryEnd
                    - queryStart) / 1000.0f);

            long binningStart = System.currentTimeMillis();

            while (rs.next()) {
                Date timeDown = rs.getDate(4);
                Date timeUp = rs.getDate(5);

                /*System.out.println("tripStart: " + tripStart.getTime());*/
                if (timeUp == null) {
                    timeUp = new Date();
                }



                //String cause = rs.getString(4);
                //String area = rs.getString("area");

                Date binStart;
                Date nextBinStart;
                boolean overflow;
                int binUnit;

                if (BinSize.HOUR.equals(params.getBinSize())) { // We are doing an hourly histogram so use most recent top of hour (truncate minutes/seconds/millis)
                    binStart = TimeUtil.startOfHour(timeDown, Calendar.getInstance());
                    binUnit = Calendar.HOUR_OF_DAY;
                } else if(BinSize.DAY.equals(params.getBinSize())) { // We are doing a daily histogram so truncate hours/minutes/seconds/millis
                    binStart = TimeUtil.startOfDay(timeDown, Calendar.getInstance());
                    binUnit = Calendar.DATE;
                } else { // Monthly
                    binStart = TimeUtil.startOfMonth(timeDown, Calendar.getInstance());
                    binUnit = Calendar.MONTH;
                }

                do {
                    nextBinStart = nextBinStart(binStart, binUnit);

                    overflow = timeUp.getTime() > nextBinStart.getTime();

                    long durationMillis = Math.min(timeUp.getTime(), nextBinStart.getTime()) - timeDown.getTime();

                    //long binSizeMillis = nextBinStart.getTime() - binStart.getTime();

                    addIncident(incidentMap, binStart, durationMillis);

                    binStart = nextBinStart;
                    timeDown = nextBinStart;
                } while(overflow);
            }

            long binningEnd = System.currentTimeMillis();
            LOGGER.log(Level.FINEST, "Repair report binning seconds: {0}", (binningEnd
                    - binningStart) / 1000.0f);

        } finally {
            IOUtil.close(rs, stmt, con);
        }

        long sortingStart = System.currentTimeMillis();

        List<HistogramBin> trendList = new ArrayList<>();

        for (Date dateKey
                : incidentMap.keySet()) { // Dates are already ordered...
            HashMap<String, HistogramBin> groupingMap = incidentMap.get(dateKey);
            List<String> keyList = new ArrayList<>(groupingMap.keySet());
            Collections.sort(keyList, Collections.reverseOrder());
            for (String grouping : keyList) {
                trendList.add(groupingMap.get(grouping));
            }
        }

        long sortingEnd = System.currentTimeMillis();

        LOGGER.log(Level.FINEST,
                "Repair report sorting seconds: {0}", (sortingEnd
                - sortingStart) / 1000.0f);

        return trendList;
    }

    private Date nextBinStart(Date binDate, int binUnit) {
        Calendar cal = Calendar.getInstance();

        cal.setTime(binDate);
        cal.add(binUnit, 1);

        return cal.getTime();
    }

    private void addIncident(LinkedHashMap<Date, HashMap<String, HistogramBin>> incidentMap, Date binDate, long durationMillis) {
        /*System.out.println("binDate: " + binDate.getTime());*/
        String grouping = null;
                /*if ("cause".equals(params.getGrouping())) {
                    grouping = cause;
                } else if("area".equals(params.getGrouping())) {
                    grouping = area;
                }*/

        HashMap<String, HistogramBin> groupingMap = incidentMap.get(binDate);

        if (groupingMap == null) {
            groupingMap = new HashMap<>();
            incidentMap.put(binDate, groupingMap);
        }

        HistogramBin bin = groupingMap.get(grouping);

        if (bin == null) {
            bin = new HistogramBin();
            bin.setStart(binDate);
            bin.setGrouping(grouping);
            groupingMap.put(grouping, bin);
        }

        bin.incrementCount();
        bin.setDurationMillis(bin.getDurationMillis() + durationMillis);
    }
}
