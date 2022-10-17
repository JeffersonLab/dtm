package org.jlab.dtm.business.service;

import java.math.BigInteger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jlab.dtm.persistence.model.CategoryTrendInfo;
import org.jlab.dtm.persistence.util.DtmSqlUtil;
import org.jlab.smoothness.business.util.DateIterator;
import org.jlab.smoothness.business.util.IOUtil;
import org.jlab.smoothness.business.util.TimeUtil;

public class FsdExceptionTrendService {

    private static final Logger logger = Logger.getLogger(
            FsdExceptionTrendService.class.getName());

    /**
     * This query is a doozy so I'm going to explain it.
     *
     * Objective: Obtain a histogram of FSD device exceptions (count or duration sum) per unit time
     * (day or hour).
     *
     * Note 1: The query must be dynamic based on user input so using a stored procedure or database
     * function becomes difficult (not to mention harder to maintain). Stored procedures / database
     * functions / anonymous PL/SQL blocks are also tricky when the goal is for it to return a
     * cursor as then the client must use an Oracle database driver specific API instead of JDBC.
     * Another route I examined was simply having the client issue multiple queries. This is ugly
     * too, and has terrible performance if doing one query per histogram 'bin'.
     *
     *
     * Note 2: There are two major confusing spots in this query: (1) we need to generate a
     * collection of histogram 'bins' - this is done by using 'select from dual... connect by'. (2)
     * we need to execute a complex correlated query for each bin that returns multiple columns, but
     * a correlated subquery can only return a single column - to get around this we create a single
     * string that is a concatenation of the three columns we need then parse the parts back later.
     * An alternative might be to issue multiple queries, one per column needed. Of course we could
     * issue one query per bin as well. Another alternative is to create a stored procedure that
     * issues PL/SQL loop and returns a cursor. None of the alternatives are great.
     *
     * Note 3: Another complication with this query is that the database is storing the dates in
     * UTC, but we work in local time (America/New_York) in Java so you'll see 'from_tz ... at time
     * zone... as date'
     *
     * Note 4: The 'least .... greatest' part is because the user is selecting a date range of
     * interest, but some of the FSD device exceptions may span a boundary.
     *
     * Note 5: If the user indicates they want to group by category then the query gets even
     * hairier. Plus, the user can indicate to limit the results to only FSDs of a particular
     * category, and this is independent of whether the group by is in effect. The group by is
     * further complicated by the fact that we want a full list of categories for each bin, even if
     * most are zero; this is because the histogram needs to be continuous, especially if the user
     * select line mode instead of bar mode they would notice and it would be misleading. This
     * generates a lot of rows as it is logically a cross-join. This is done by using a 'union all
     * ... group by date, category'
     *
     * Note 6: Parameter index to name map: 1 = start 2 = end 3 = start 4 = end 5 = start
     */
    private String getSql(Date day, boolean groupByCategory, BigInteger[] categoryIdArray) {
        String sql
                = "select n.day_date, "
                + "(select (count(c.fsd_device_exception_id) || '|' || "
                + "nvl(sum(least("
                + "nvl("
                + "cast((from_tz(cast(a.end_utc as timestamp), '+00:00') at time zone 'America/New_York') as date), "
                + " sysdate),"
                + " n.day_date + 1) - greatest("
                + "cast((from_tz(cast(a.start_utc as timestamp), '+00:00') at time zone 'America/New_York') as date), "
                + "n.day_date)) * 24, 0) || '|' || ";

        if (groupByCategory) {
            sql = sql + " f.name) as count_p_duration_p_category ";
        } else {
            sql = sql + " '') as count_p_duration_p_category ";
        }

        sql = sql
                + "from "
                + "fsd_trip a join fsd_fault b using (fsd_trip_id) join fsd_device_exception c using (fsd_fault_id) join dtm_owner.system d on (nvl(c.hco_system_name, 'Unknown/Missing') = d.name) join system_alpha_category e using(system_id) join category f on (e.category_id = f.category_id) "
                + "where "
                + "cast((from_tz(cast(a.start_utc as timestamp), '+00:00') at time zone 'America/New_York') as date) "
                + "< n.day_date + 1 "
                + "and nvl("
                + "cast((from_tz(cast(a.end_utc as timestamp), '+00:00') at time zone 'America/New_York') as date), "
                + "sysdate) >= n.day_date ";

        String categoryCsv = IOUtil.toCsv(categoryIdArray);

        if (categoryCsv != null && !categoryCsv.isEmpty()) {
            sql = sql + "and e.category_id in (" + categoryCsv + ") ";
        }

        if (groupByCategory) {
            sql = sql + "group by f.name ";

            String categoryQualifier = "";

            if (categoryCsv != null && !categoryCsv.isEmpty()) {
                categoryQualifier = "and x.category_id in (" + categoryCsv + ")";
            }

            sql
                    = "select day_date, sum(exception_count), sum(duration_hours), category_name from (("
                    + sql
                    + ") union all (select n.day_date as day_date, 0 as exception_count, 0 as duration_hours, category_name from (select distinct(y.name) as category_name from system_alpha_category x, category y where x.category_id = y.category_id "
                    + categoryQualifier + "))) group by day_date, category_name ";

        }

        sql = sql
                + ") as count_p_duration_p_category from (select ? + (level - 1) as day_date from dual connect by level <= (? - ?) + 1) n ";

        /*Now split varchar */
        sql
                = "select day_date, to_number(substr(count_p_duration_p_category, 1, instr(count_p_duration_p_category, '|') - 1)) as count, "
                + "to_number(substr(count_p_duration_p_category, instr(count_p_duration_p_category, '|') + 1, (instr(count_p_duration_p_category, '|', 1, 2) - 1) - instr(count_p_duration_p_category, '|'))) as duration, "
                + "substr(count_p_duration_p_category, instr(count_p_duration_p_category, '|', 1, 2) + 1) as category from ("
                + sql + ")";

        return sql;
    }

    public List<CategoryTrendInfo> findTrendListByPeriod(Date start, Date end,
            boolean groupByCategory, BigInteger[] categoryIdArray) throws SQLException {

        List<CategoryTrendInfo> trendList = new ArrayList<CategoryTrendInfo>();

        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        String sql = getSql(start, groupByCategory, categoryIdArray);

        logger.log(Level.FINEST, "Query: {0}", sql);

        try {
            con = DtmSqlUtil.getConnection();

            stmt = con.prepareStatement(sql);

            /**
             * 1 = end 2 = start 3 = start 4 = end 5 = start
             */
            //java.sql.Date starti = new java.sql.Date(start.getTime());
            //java.sql.Date endi = new java.sql.Date(TimeUtil.addDays(start, 1).getTime());
            java.sql.Date starti = new java.sql.Date(start.getTime());
            java.sql.Date endi = new java.sql.Date(end.getTime());

            stmt.setDate(1, starti);
            stmt.setDate(2, endi);
            stmt.setDate(3, starti);

            //assignParameterValues(stmt);
            rs = stmt.executeQuery();

            while (rs.next()) {
                Date date = rs.getDate(1);
                BigInteger count = rs.getBigDecimal(2).toBigIntegerExact();
                double duration = rs.getBigDecimal(3).doubleValue();
                String category = rs.getString(4);

                trendList.add(new CategoryTrendInfo(date, count, duration, category));
            }

        } finally {
            IOUtil.close(rs, stmt, con);
        }

        return trendList;
    }

    private String getSqlSingle(Date day, boolean groupByCategory, BigInteger[] categoryIdArray) {
        String sql
                = "select ? as day_date, "
                + "count(c.fsd_device_exception_id) as exception_count, "
                + "nvl(sum(least("
                + "nvl("
                + "cast((from_tz(cast(a.end_utc as timestamp), '+00:00') at time zone 'America/New_York') as date), "
                + " sysdate),"
                + " ?) - greatest("
                + "cast((from_tz(cast(a.start_utc as timestamp), '+00:00') at time zone 'America/New_York') as date), "
                + "?)) * 24, 0) as duration_hours ";

        if (groupByCategory) {
            sql = sql + ", f.name as category_name ";
        } else {
            sql = sql + ", '' as category_name ";
        }

        sql = sql
                + "from "
                + "fsd_trip a join fsd_fault b using (fsd_trip_id) join fsd_device_exception c using (fsd_fault_id) join dtm_owner.system d on (nvl(c.hco_system_name, 'Unknown/Missing') = d.name) join system_alpha_category e using(system_id) join category f on (e.category_id = f.category_id) "
                + "where "
                + "cast((from_tz(cast(a.start_utc as timestamp), '+00:00') at time zone 'America/New_York') as date) "
                + "< ? "
                + "and nvl("
                + "cast((from_tz(cast(a.end_utc as timestamp), '+00:00') at time zone 'America/New_York') as date), "
                + "sysdate) >= ? ";

        String categoryCsv = IOUtil.toCsv(categoryIdArray);

        if (categoryCsv != null && !categoryCsv.isEmpty()) {
            sql = sql + "and e.category_id in (" + categoryCsv + ") ";
        }

        if (groupByCategory) {
            sql = sql + "group by f.name ";

            String categoryQualifier = "";

            if (categoryCsv != null && !categoryCsv.isEmpty()) {
                categoryQualifier = "and x.category_id in (" + categoryCsv + ")";
            }

            sql
                    = "select day_date, sum(exception_count), sum(duration_hours), category_name from (("
                    + sql
                    + ") union all (select ? as day_date, 0 as exception_count, 0 as duration_hours, category_name from (select distinct(y.name) as category_name from system_alpha_category x, category y where x.category_id = y.category_id "
                    + categoryQualifier + "))) group by day_date, category_name ";

        }
        return sql;
    }

    public List<CategoryTrendInfo> findTrendByDateSingle(Date day,
            boolean groupByCategory, BigInteger[] categoryIdArray) throws SQLException {
        List<CategoryTrendInfo> trendList = new ArrayList<CategoryTrendInfo>();

        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        String sql = getSqlSingle(day, groupByCategory, categoryIdArray);

        logger.log(Level.FINEST, "Query: {0}", sql);

        try {
            con = DtmSqlUtil.getConnection();

            stmt = con.prepareStatement(sql);

            /**
             * 1 = start 2 = end 3 = start 4 = end 5 = start
             */
            java.sql.Date start = new java.sql.Date(day.getTime());
            java.sql.Date end = new java.sql.Date(TimeUtil.addDays(day, 1).getTime());

            stmt.setDate(1, start);
            stmt.setDate(2, end);
            stmt.setDate(3, start);
            stmt.setDate(4, end);
            stmt.setDate(5, start);
            
            if(groupByCategory) {
                stmt.setDate(6, start);
            }
            
            //assignParameterValues(stmt);

            rs = stmt.executeQuery();

            while (rs.next()) {
                Date date = rs.getDate(1);
                BigInteger count = rs.getBigDecimal(2).toBigIntegerExact();
                double duration = rs.getBigDecimal(3).doubleValue();
                String category = rs.getString(4);

                trendList.add(new CategoryTrendInfo(date, count, duration, category));
            }

        } finally {
            IOUtil.close(rs, stmt, con);
        }

        return trendList;
    }

    public List<CategoryTrendInfo> findTrendListByPeriodClientLoop(Date start, Date end,
            boolean groupByCategory, BigInteger[] categoryIdArray) throws SQLException {

        List<CategoryTrendInfo> trendList = new ArrayList<CategoryTrendInfo>();

        DateIterator iterator = new DateIterator(start, end, Calendar.DATE);

        for (Date date : iterator) {
            trendList.addAll(findTrendByDateSingle(date, groupByCategory, categoryIdArray));
        }

        return trendList;
    }
    
    public List<CategoryTrendInfo> findTrendListByPeriodTableFunc(Date start, Date end,
            boolean groupByCategory, int interval, BigInteger[] categoryIdArray) throws SQLException {

        List<CategoryTrendInfo> trendList = new ArrayList<CategoryTrendInfo>();

        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        String sql = "select * from table(fsd_histo_report(?, ?, ?, ?))";

        if(groupByCategory) {
            sql = "select * from table(fsd_histo_group_report(?, ?, ?, ?))";
        }
        
        logger.log(Level.FINEST, "Query: {0}", sql);

        try {
            con = DtmSqlUtil.getConnection();

            stmt = con.prepareStatement(sql);

            java.sql.Date starti = new java.sql.Date(start.getTime());
            java.sql.Date endi = new java.sql.Date(end.getTime());

            stmt.setDate(1, starti);
            stmt.setDate(2, endi);
            stmt.setInt(3, interval);
            stmt.setString(4, IOUtil.toCsv(categoryIdArray));

            rs = stmt.executeQuery();

            while (rs.next()) {
                Date date = rs.getDate(1);
                BigInteger count = rs.getBigDecimal(2).toBigIntegerExact();
                double duration = rs.getBigDecimal(3).doubleValue();
                String category = rs.getString(4);

                trendList.add(new CategoryTrendInfo(date, count, duration, category));
            }

        } finally {
            IOUtil.close(rs, stmt, con);
        }

        return trendList;
    }
}
