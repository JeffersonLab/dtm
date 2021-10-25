package org.jlab.dtm.business.service;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jlab.dtm.business.params.IncidentParams;
import org.jlab.dtm.persistence.enumeration.ReviewLevel;
import org.jlab.dtm.persistence.util.DtmSqlUtil;
import org.jlab.smoothness.business.util.IOUtil;

/**
 *
 * @author ryans
 */
public class RootCauseOverallMetricService {

    private static final Logger LOGGER = Logger.getLogger(RootCauseOverallMetricService.class.getName());

    public double countDurationSeconds(IncidentParams params) throws
            SQLException {
        BigDecimal count = BigDecimal.ZERO;

        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        String sql
                = "select sum(duration_seconds) from incident inner join event using(event_id) ";

        sql = sql
                + getSqlWhereClause(params);

        LOGGER.log(Level.FINEST, "Query: {0}", sql);

        try {
            con = DtmSqlUtil.getConnection();

            stmt = con.prepareStatement(sql);

            assignParameterValues(stmt, params);

            rs = stmt.executeQuery();

            if (rs.next()) {
                count = rs.getBigDecimal(1);
            }

        } finally {
            IOUtil.close(rs, stmt, con);
        }

        return count == null ? 0 : count.doubleValue();
    }

    public BigInteger countRecords(IncidentParams params) throws
            SQLException {
        BigInteger count = BigInteger.ZERO;

        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        String sql
                = "select count(*) from incident inner join event using(event_id) ";

        sql = sql
                + getSqlWhereClause(params);

        LOGGER.log(Level.FINEST, "Query: {0}", sql);

        try {
            con = DtmSqlUtil.getConnection();

            stmt = con.prepareStatement(sql);

            assignParameterValues(stmt, params);

            rs = stmt.executeQuery();

            if (rs.next()) {
                count = rs.getBigDecimal(1).toBigIntegerExact();
            }

        } finally {
            IOUtil.close(rs, stmt, con);
        }

        return count;
    }

    public RootCauseOverallMetrics find(IncidentParams params) throws SQLException {
        params.setReviewed(null);
        params.setLevel(null);
        double repairHours = countDurationSeconds(params) / 3600;
        long repairs = countRecords(params).longValue();
        
        params.setReviewed(false);
        double deadbeatHours = countDurationSeconds(params) / 3600;
        long deadbeats = countRecords(params).longValue();        
        
        params.setReviewed(null);
        params.setLevel(ReviewLevel.ONE);
        double levelOneHours = countDurationSeconds(params) / 3600;
        long levelOne = countRecords(params).longValue();        
        
        params.setLevel(ReviewLevel.TWO);
        double levelTwoHours = countDurationSeconds(params) / 3600;
        long levelTwo = countRecords(params).longValue();        
        
        params.setLevel(ReviewLevel.THREE_PLUS);
        double levelThreePlusHours = countDurationSeconds(params) / 3600;
        long levelThreePlus = countRecords(params).longValue();

        RootCauseOverallMetrics metrics = new RootCauseOverallMetrics(repairHours, deadbeatHours, levelOneHours, levelTwoHours, levelThreePlusHours, repairs, deadbeats, levelOne, levelTwo, levelThreePlus);

        return metrics;
    }

    private String getSqlWhereClause(IncidentParams params) {

        List<String> filters = new ArrayList<>();

        if (params.getEnd() != null) {
            filters.add("time_down < ? ");
        }

        if (params.getStart() != null) {
            filters.add("coalesce(incident.time_up, sysdate) >= ? ");
        }

        if (params.getEventTypeId() != null) {
            filters.add("event_type_id = ? ");
        }

        if (params.getReviewed() != null) {
            filters.add("reviewed = ? ");
        }

        if (params.getLevel() != null) {
            filters.add("review_level = ? ");
        }
        
        if(params.getIncidentId() != null) {
            filters.add("incident_id = ? ");
        }
        
        if(params.getEventId() != null) {
            filters.add("event_id = ? ");
        }
        
        if(params.getSmeUsername() != null && !params.getSmeUsername().isEmpty()) {
            filters.add("incident_id in (select incident_id from incident inner join incident_review using(incident_id), staff where reviewer_id = staff_id and username = ?) ");
        }

        String filter = "";

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

    private void assignParameterValues(PreparedStatement stmt, IncidentParams params) throws SQLException {
        int i = 1;

        if (params.getEnd() != null) {
            stmt.setDate(i++, new java.sql.Date(params.getEnd().getTime()));
        }

        if (params.getStart() != null) {
            stmt.setDate(i++, new java.sql.Date(params.getStart().getTime()));
        }

        if (params.getEventTypeId() != null) {
            stmt.setBigDecimal(i++, new BigDecimal(params.getEventTypeId()));
        }

        if (params.getReviewed() != null) {
            stmt.setString(i++, params.getReviewed() ? "Y" : "N");
        }
        
        if (params.getLevel() != null) {
            stmt.setString(i++, params.getLevel().name());
        }       
        
        if(params.getIncidentId() != null) {
            stmt.setBigDecimal(i++, new BigDecimal(params.getIncidentId()));
        }
        
        if(params.getEventId() != null) {
            stmt.setBigDecimal(i++, new BigDecimal(params.getEventId()));
        }        
        
        if(params.getSmeUsername() != null && !params.getSmeUsername().isEmpty()) {
            stmt.setString(i++, params.getSmeUsername());
        }        
    }

    public static class RootCauseOverallMetrics {

        private final double repairHours;
        private final double deadbeatHours;
        private final double levelOneHours;
        private final double levelTwoHours;
        private final double levelThreePlusHours;
        private final long repairs;
        private final long deadbeats;
        private final long levelOne;
        private final long levelTwo;
        private final long levelThreePlus;

        public RootCauseOverallMetrics(double repairHours, double deadbeatHours, double levelOneHours, double levelTwoHours, double levelThreePlusHours, long repairs, long deadbeats, long levelOne, long levelTwo, long levelThreePlus) {
            this.repairHours = repairHours;
            this.deadbeatHours = deadbeatHours;
            this.levelOneHours = levelOneHours;
            this.levelTwoHours = levelTwoHours;
            this.levelThreePlusHours = levelThreePlusHours;
            this.repairs = repairs;
            this.deadbeats = deadbeats;
            this.levelOne = levelOne;
            this.levelTwo = levelTwo;
            this.levelThreePlus = levelThreePlus;
        }

        public double getRepairHours() {
            return repairHours;
        }

        public double getDeadbeatHours() {
            return deadbeatHours;
        }

        public double getLevelOneHours() {
            return levelOneHours;
        }

        public double getLevelTwoHours() {
            return levelTwoHours;
        }

        public double getLevelThreePlusHours() {
            return levelThreePlusHours;
        }

        public long getRepairs() {
            return repairs;
        }

        public long getDeadbeats() {
            return deadbeats;
        }

        public long getLevelOne() {
            return levelOne;
        }

        public long getLevelTwo() {
            return levelTwo;
        }

        public long getLevelThreePlus() {
            return levelThreePlus;
        }
    }
}
