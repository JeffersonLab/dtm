package org.jlab.dtm.business.service;

import java.math.BigInteger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jlab.dtm.persistence.entity.EventType;
import org.jlab.dtm.persistence.model.CategoryTrendInfo;
import org.jlab.dtm.persistence.util.DtmSqlUtil;
import org.jlab.smoothness.business.util.IOUtil;

public class IncidentTrendService {

    private static final Logger logger = Logger.getLogger(
            IncidentTrendService.class.getName());
    
    public List<CategoryTrendInfo> findTrendListByPeriod(Date start, Date end,
            EventType type, boolean includeBeamTransport, boolean groupByCategory, int interval, BigInteger[] categoryIdArray) throws SQLException {

        List<CategoryTrendInfo> trendList = new ArrayList<>();

        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        String sql = "select * from table(incident_histo_report(?, ?, ?, ?, ?, ?))";

        if(groupByCategory) {
            sql = "select * from table(incident_histo_group_report(?, ?, ?, ?, ?, ?))";
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
            if(type == null) {
                stmt.setNull(5, Types.NUMERIC);
            } else {
                stmt.setInt(5, type.getEventTypeId().intValue());
            }
            stmt.setString(6, includeBeamTransport ? "Y" : "N");

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
