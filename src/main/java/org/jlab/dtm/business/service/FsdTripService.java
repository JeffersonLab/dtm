package org.jlab.dtm.business.service;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jlab.dtm.business.util.FsdAreaLogic;
import org.jlab.dtm.business.util.FsdRootCauseLogic;
import org.jlab.dtm.persistence.enumeration.AccMachineState;
import org.jlab.dtm.persistence.enumeration.HallMachineState;
import org.jlab.dtm.persistence.filter.FsdTripFilter;
import org.jlab.dtm.persistence.model.FsdDevice;
import org.jlab.dtm.persistence.model.FsdFault;
import org.jlab.dtm.persistence.model.FsdTrip;
import org.jlab.dtm.persistence.util.DtmSqlUtil;
import org.jlab.smoothness.business.util.IOUtil;
import org.jlab.smoothness.business.util.TimeUtil;

/**
 *
 * @author ryans
 */
public class FsdTripService {

    private static final Logger logger = Logger.getLogger(
            FsdTripService.class.getName());

    public BigInteger countList(FsdTripFilter filter) throws
            SQLException {
        BigInteger count = BigInteger.ZERO;

        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        String sql
                = "select count(distinct(a.fsd_trip_id)) from fsd_trip a left join fsd_fault b on a.fsd_trip_id = b.fsd_trip_id left join fsd_device_exception c on b.fsd_fault_id = c.fsd_fault_id ";

        sql = sql
                + filter.getSqlWhereClause();

        logger.log(Level.FINEST, "Query: {0}", sql);

        try {
            con = DtmSqlUtil.getConnection();

            stmt = con.prepareStatement(sql);

            filter.assignParameterValues(stmt);

            rs = stmt.executeQuery();

            if (rs.next()) {
                count = rs.getBigDecimal(1).toBigIntegerExact();
            }

        } finally {
            IOUtil.close(rs, stmt, con);
        }

        return count;
    }

    public FsdSummary filterSummary(FsdTripFilter filter) throws SQLException {
        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        // Note: We grab count which START in user selected time period.  This is consistent with other FSD reports, but differs from most other downtime and time accounting reports.
        String sql
                = "select count(a.fsd_trip_id) as count, sum(coalesce(end_utc, cast(sys_extract_utc(systimestamp) as date)) - start_utc) * 24 as hours from fsd_trip a ";

        // Filter result set
        sql = sql + filter.getSqlWhereClause();

        FsdSummary result = null;

        logger.log(Level.FINEST, "Query: {0}", sql);

        try {
            con = DtmSqlUtil.getConnection();

            stmt = con.prepareStatement(sql);

            filter.assignParameterValues(stmt);

            rs = stmt.executeQuery();

            if (rs.next()) {
                long count = rs.getBigDecimal(1).longValueExact();
                BigDecimal hoursObj = rs.getBigDecimal(2);

                double hours = 0.0d;

                if (hoursObj != null) {
                    hours = hoursObj.doubleValue();
                }

                result = new FsdSummary(hours, count);
            }
        } finally {
            IOUtil.close(rs, stmt, con);
        }

        return result;
    }

    public List<FsdTrip> filterListWithDependencies(FsdTripFilter filter, int offset, int max) throws
            SQLException {
        List<FsdTrip> tripList = null;

        updateCause();

        updateArea();

        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        /**
         * There are at least two ways to apply a user specified time interval filter to a set of
         * items which have a duration themselves. (1) Include the item if it overlaps the time
         * interval at any point; could begin before the interval or extend after it. (2) Only
         * include items which begin within the interval. If doing the first then it might be
         * helpful to take the greatest of item start and boundary start as the psudo item start and
         * to take the least of item end and boundary end as the psudo item end. If doing the second
         * it is easy to create a histogram because you don't have to break up an item into multiple
         * pieces depending on how many bins the item spans.
         *
         * This query behaves like the second as it is correlated with a histogram (FSD Summary
         * report)
         *
         * Note: the operability annual repair report does the more difficult binning thing. Maybe
         * someday we can update this to do the same. However, this gets more complicated when you
         * want to know the number/count of trips too - does splitting a bin boundary cause the
         * count to increase?
         */
        String sql
                = "select distinct a.fsd_trip_id, start_utc, end_utc, acc_state, hla_state, hlb_state, hlc_state, hld_state, cause, area from fsd_trip a left join fsd_fault b on a.fsd_trip_id = b.fsd_trip_id left join fsd_device_exception c on b.fsd_fault_id = c.fsd_fault_id ";

        // Filter result set
        sql = sql + filter.getSqlWhereClause();

        // Order pagination
        sql = sql + "order by start_utc desc, fsd_trip_id desc";

        // Limit number of count (pagination)
        sql = "select * from (select z.*, ROWNUM rnum from ("
                + sql + ") z where ROWNUM <= " + (offset + max) + ") where rnum > " + offset;

        // Join dependencies
        sql
                = "select fsd_trip_id, fsd_fault_id, fsd_device_exception_id, start_utc, end_utc, acc_state, hla_state, hlb_state, hlc_state, hld_state, node, channel, disjoint_yn, category.name as category, hco_system_name, ced_type, ced_name, fault_confirmation_yn, cause, area, dtm_owner.region.name as region from ("
                + sql
                + ") left join fsd_fault using(fsd_trip_id) left join fsd_device_exception using(fsd_fault_id) left join dtm_owner.system s on s.name = hco_system_name left join system_alpha_category_plus on s.system_id = system_alpha_category_plus.system_id left join dtm_owner.category on system_alpha_category_plus.category_id = category.category_id "
                + "left join dtm_owner.component on fsd_device_exception.ced_name = component.name left join dtm_owner.region on region.region_id = component.region_id ";

        // Order result set
        sql = sql + "order by start_utc desc, fsd_trip_id desc, hco_system_name desc, ced_name desc";

        logger.log(Level.FINEST, "Query: {0}", sql);

        try {
            con = DtmSqlUtil.getConnection();

            stmt = con.prepareStatement(sql);

            filter.assignParameterValues(stmt);

            rs = stmt.executeQuery();

            tripList = populateTripsFromRs(rs);
        } finally {
            IOUtil.close(rs, stmt, con);
        }

        /*for (FsdTrip trip : tripList) {
            logic.setRootCause(trip);
        }*/
        return tripList;
    }

    private List<FsdTrip> populateTripsFromRs(ResultSet rs) throws SQLException {
        LinkedHashMap<BigInteger, FsdTrip> tripMap = new LinkedHashMap<>();

        while (rs.next()) {
            // Trip fields
            BigInteger fsdTripId = rs.getBigDecimal("fsd_trip_id").toBigIntegerExact();
            Date start = rs.getTimestamp("start_utc", TimeUtil.getUtcCalendar());
            Date end = rs.getTimestamp("end_utc", TimeUtil.getUtcCalendar());
            AccMachineState accState = AccMachineState.valueOfAllowNull(
                    rs.getString("acc_state"));
            HallMachineState hallAState = HallMachineState.valueOfAllowNull(rs.getString(
                    "hla_state"));
            HallMachineState hallBState = HallMachineState.valueOfAllowNull(rs.getString(
                    "hlb_state"));
            HallMachineState hallCState = HallMachineState.valueOfAllowNull(rs.getString(
                    "hlc_state"));
            HallMachineState hallDState = HallMachineState.valueOfAllowNull(rs.getString(
                    "hld_state"));
            String cause = rs.getString("cause");
            String area = rs.getString("area");

            // Fault fields
            BigInteger fsdFaultId
                    = rs.getBigDecimal("fsd_fault_id") == null ? null : rs.getBigDecimal(
                    "fsd_fault_id").toBigIntegerExact();
            String node = rs.getString("node");
            Integer channel = rs.getInt("channel"); // will be 0 if SQL NULL
            if (rs.wasNull()) {
                channel = null;
            }
            String disjointString = rs.getString("disjoint_yn");

            // Device fields
            BigInteger fsdDeviceId
                    = rs.getBigDecimal("fsd_device_exception_id") == null ? null : rs.getBigDecimal(
                    "fsd_device_exception_id").toBigIntegerExact();
            String category = rs.getString("category");
            String systemResult = rs.getString("hco_system_name");
            String cedType = rs.getString("ced_type");
            String cedName = rs.getString("ced_name");
            String confirmedString = rs.getString("fault_confirmation_yn");
            String region = rs.getString("region");

            FsdTrip trip = tripMap.get(fsdTripId);

            if (trip == null) {
                trip = new FsdTrip();
                trip.setFsdTripId(fsdTripId);
                trip.setStart(start);
                trip.setEnd(end);
                trip.setAccState(accState);
                trip.setHallAState(hallAState);
                trip.setHallBState(hallBState);
                trip.setHallCState(hallCState);
                trip.setHallDState(hallDState);
                trip.setRootCause(cause);
                trip.setArea(area);

                tripMap.put(fsdTripId, trip);
            }

            if (fsdFaultId != null) {
                LinkedHashMap<BigInteger, FsdFault> faultMap = trip.getFaultMap();
                FsdFault fault = faultMap.get(fsdFaultId);

                if (fault == null) {
                    fault = new FsdFault();
                    fault.setFsdFaultId(fsdFaultId);
                    fault.setNode(node);
                    fault.setChannel(channel);
                    fault.setDisjoint("Y".equals(disjointString));

                    faultMap.put(fsdFaultId, fault);
                }

                if (fsdDeviceId != null) {
                    LinkedHashMap<BigInteger, FsdDevice> deviceMap = fault.getDeviceMap();
                    FsdDevice device = new FsdDevice();
                    device.setFsdDeviceId(fsdDeviceId);
                    device.setCategory(category);
                    device.setSystem(systemResult);
                    device.setCedType(cedType);
                    device.setCedName(cedName);
                    device.setConfirmed("Y".equals(confirmedString));
                    device.setRegion(region);
                    deviceMap.put(fsdDeviceId, device);
                }
            }
        }

        return new ArrayList<>(tripMap.values());
    }

    private List<FsdTrip> selectTripsWithNullCause() throws SQLException {
        List<FsdTrip> tripList = null;

        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        String sql
                = "select fsd_trip_id, fsd_fault_id, fsd_device_exception_id, start_utc, end_utc, acc_state, hla_state, hlb_state, hlc_state, hld_state, node, channel, disjoint_yn, dtm_owner.category.name as category, hco_system_name, ced_type, ced_name, fault_confirmation_yn, cause, area, dtm_owner.region.name as region from fsd_trip "
                + "left join fsd_fault using(fsd_trip_id) left join fsd_device_exception using(fsd_fault_id) left join dtm_owner.system s on s.name = hco_system_name left join dtm_owner.system_alpha_category_plus on s.system_id = dtm_owner.system_alpha_category_plus.system_id left join dtm_owner.category on dtm_owner.system_alpha_category_plus.category_id = dtm_owner.category.category_id "
                + "left join dtm_owner.component on fsd_device_exception.ced_name = component.name left join dtm_owner.region on region.region_id = component.region_id "
                + "where cause is null";

        logger.log(Level.FINEST, "Query: {0}", sql);

        try {
            con = DtmSqlUtil.getConnection();

            stmt = con.prepareStatement(sql);

            rs = stmt.executeQuery();

            tripList = populateTripsFromRs(rs);
        } finally {
            IOUtil.close(rs, stmt, con);
        }

        return tripList;
    }

    private List<FsdTrip> selectTripsWithNullArea() throws SQLException {
        List<FsdTrip> tripList = null;

        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        String sql
                = "select fsd_trip_id, fsd_fault_id, fsd_device_exception_id, start_utc, end_utc, acc_state, hla_state, hlb_state, hlc_state, hld_state, node, channel, disjoint_yn, dtm_owner.category.name as category, hco_system_name, ced_type, ced_name, fault_confirmation_yn, cause, area, dtm_owner.region.name as region from fsd_trip "
                + "left join fsd_fault using(fsd_trip_id) left join fsd_device_exception using(fsd_fault_id) left join dtm_owner.system s on s.name = hco_system_name left join dtm_owner.system_alpha_category_plus on s.system_id = dtm_owner.system_alpha_category_plus.system_id left join dtm_owner.category on dtm_owner.system_alpha_category_plus.category_id = dtm_owner.category.category_id "
                + "left join dtm_owner.component on fsd_device_exception.ced_name = component.name left join dtm_owner.region on region.region_id = component.region_id "
                + "where area is null";

        logger.log(Level.FINEST, "Query: {0}", sql);

        try {
            con = DtmSqlUtil.getConnection();

            stmt = con.prepareStatement(sql);

            rs = stmt.executeQuery();

            tripList = populateTripsFromRs(rs);
        } finally {
            IOUtil.close(rs, stmt, con);
        }

        return tripList;
    }

    private void updateTripListCause(List<FsdTrip> tripList) throws SQLException {
        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        String sql
                = "update fsd_trip set cause = ? where fsd_trip_id = ?";

        logger.log(Level.FINEST, "Query: {0}", sql);

        try {
            con = DtmSqlUtil.getConnection();

            stmt = con.prepareStatement(sql);

            for (FsdTrip trip : tripList) {
                stmt.setString(1, trip.getRootCause());
                stmt.setBigDecimal(2, new BigDecimal(trip.getFsdTripId()));

                stmt.executeUpdate();
            }
        } finally {
            IOUtil.close(rs, stmt, con);
        }
    }

    private void updateTripListArea(List<FsdTrip> tripList) throws SQLException {
        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        String sql
                = "update fsd_trip set area = ? where fsd_trip_id = ?";

        logger.log(Level.FINEST, "Query: {0}", sql);

        try {
            con = DtmSqlUtil.getConnection();

            stmt = con.prepareStatement(sql);

            for (FsdTrip trip : tripList) {
                stmt.setString(1, trip.getArea());
                stmt.setBigDecimal(2, new BigDecimal(trip.getFsdTripId()));

                stmt.executeUpdate();
            }
        } finally {
            IOUtil.close(rs, stmt, con);
        }
    }

    public void updateCause() throws SQLException {
        List<FsdTrip> tripList = selectTripsWithNullCause();

        logger.log(Level.FINEST, "Updating cause for {0} trips", tripList.size());

        if (!tripList.isEmpty()) { // Don't query CED if no count to update

            FsdRootCauseLogic logic = null;

            try {
                logic = new FsdRootCauseLogic();
            } catch (IOException e) {
                throw new SQLException("Unable to lookup C100 names from CED", e);
            }

            for (FsdTrip trip : tripList) {
                logic.setRootCauseIncludeSecondary(trip);
            }

            updateTripListCause(tripList);
        }
    }

    public void updateArea() throws SQLException {
        List<FsdTrip> tripList = selectTripsWithNullArea();

        for (FsdTrip trip : tripList) {
            FsdAreaLogic.setArea(trip);
        }

        updateTripListArea(tripList);
    }

    public void streamTripCsv(OutputStream out, FsdTripFilter filter, String message, boolean aggregate) throws SQLException {

        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        /**
         * There are at least two ways to apply a user specified time interval filter to a set of
         * items which have a duration themselves. (1) Include the item if it overlaps the time
         * interval at any point; could begin before the interval or extend after it. (2) Only
         * include items which begin within the interval. If doing the first then it might be
         * helpful to take the greatest of item start and boundary start as the psudo item start and
         * to take the least of item end and boundary end as the psudo item end. If doing the second
         * it is easy to create a histogram because you don't have to break up an item into multiple
         * pieces depending on how many bins the item spans.
         *
         * This query behaves like the second as it is correlated with a histogram (FSD Summary
         * report)
         */
        String sql
                //   = "select * from fsd_trip a left join fsd_fault b on a.fsd_trip_id = b.fsd_trip_id left join fsd_device_exception c on b.fsd_fault_id = c.fsd_fault_id ";
                = "select a.fsd_trip_id, b.fsd_fault_id, fsd_device_exception_id, start_utc, end_utc, acc_state, hla_state, hlb_state, hlc_state, hld_state, cause, area, node, channel, disjoint_yn, dtm_owner.category.name as category, hco_system_name, ced_type, ced_name, fault_confirmation_yn from "
                + "fsd_trip a left join fsd_fault b on a.fsd_trip_id = b.fsd_trip_id left join fsd_device_exception c on b.fsd_fault_id = c.fsd_fault_id left join dtm_owner.system s on s.name = hco_system_name left join dtm_owner.system_alpha_category_plus on s.system_id = dtm_owner.system_alpha_category_plus.system_id left join dtm_owner.category on dtm_owner.system_alpha_category_plus.category_id = dtm_owner.category.category_id ";

        // Filter result set
        sql = sql + filter.getSqlWhereClause();

        logger.log(Level.FINEST, "Aggregate (listagg): {0}", aggregate);
        
        if(aggregate) {
            // Limit number of rows concatenated as otherwise we'll overflow 4000 bytes per column
            sql = sql + "and a.fsd_trip_id in (select fsd_trip_id from fsd_trip left join fsd_fault using(fsd_trip_id) left join fsd_device_exception using(fsd_fault_id) group by fsd_trip_id having count(fsd_trip_id) <= 100) ";
            
            sql = "select fsd_trip_id, start_utc, end_utc, acc_state, hla_state, hlb_state, hlc_state, hld_state, cause, area, "
                    + "listagg(fsd_fault_id, '; ') within group (order by fsd_fault_id) as fsd_fault_id, "
                    + "listagg(node, '; ') within group (order by node) as node, "
                    + "listagg(channel, '; ') within group (order by channel) as channel, "
                    + "listagg(disjoint_yn, '; ') within group (order by disjoint_yn) as disjoint_yn, "
                    + "listagg(category, '; ') within group (order by category) as category, "
                    + "listagg(hco_system_name, '; ') within group (order by hco_system_name) as hco_system_name, "
                    //+ "rtrim(xmlagg(xmlelement(e,hco_system_name,'; ').extract('//text()') order by hco_system_name).GetClobVal(),'; ') as hco_system_name, " // This is too slow, but does solve the 4000 bytes problem
                    + "listagg(ced_type, '; ') within group (order by ced_type) as ced_type, "
                    + "listagg(ced_name, '; ') within group (order by ced_name) as ced_name, "
                    + "listagg(fault_confirmation_yn, '; ') within group (order by fault_confirmation_yn) as fault_confirmation_yn, "
                    + "listagg(fsd_device_exception_id, '; ') within group (order by fsd_device_exception_id) as fsd_device_exception_id from (" + sql + ") group by fsd_trip_id, start_utc, end_utc, acc_state, hla_state, hlb_state, hlc_state, hld_state, cause, area ";
        }
        
        // Order result set
        sql = sql + "order by start_utc desc, fsd_trip_id desc, hco_system_name desc, ced_name desc";

        logger.log(Level.FINEST, "Stream Query: {0}", sql);

        try {
            con = DtmSqlUtil.getConnection();

            stmt = con.prepareStatement(sql);

            filter.assignParameterValues(stmt);

            rs = stmt.executeQuery();

            //new PrintWriter(new OutputStreamWriter(out, "UTF-8"))
            try (PrintWriter writer = new PrintWriter(out)) {

                writer.print("\"# ");
                if(aggregate) {
                    writer.print("[Trips with over 100 faulted devices not shown] ");
                }
                writer.println(message.replaceAll("\"", "\"\"") + "\"");

                writer.print("TRIP_ID");
                writer.print(",");
                writer.print("START_DATE");
                writer.print(",");
                writer.print("END_DATE");
                writer.print(",");
                writer.print("ACC_STATE");
                writer.print(",");
                writer.print("HALL_A_STATE");
                writer.print(",");
                writer.print("HALL_B_STATE");
                writer.print(",");
                writer.print("HALL_C_STATE");
                writer.print(",");
                writer.print("HALL_D_STATE");
                writer.print(",");
                writer.print("AREA");
                writer.print(",");
                writer.print("CAUSE");
                writer.print(",");
                writer.print("FAULT_ID");
                writer.print(",");
                writer.print("NODE");
                writer.print(",");
                writer.print("CHANNEL");
                writer.print(",");
                writer.print("SECONDARY PATH");
                writer.print(",");
                writer.print("DEVICE EXCEPTION ID");
                writer.print(",");
                writer.print("CATEGORY");
                writer.print(",");
                writer.print("SYSTEM");
                writer.print(",");
                writer.print("CED TYPE");
                writer.print(",");
                writer.print("CED NAME");
                writer.print(",");
                writer.print("CONFIRMED");
                writer.println();

                SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

                while (rs.next()) {
                    // Trip fields
                    BigInteger fsdTripId = rs.getBigDecimal("fsd_trip_id").toBigIntegerExact();
                    Date start = rs.getTimestamp("start_utc", TimeUtil.getUtcCalendar());
                    Date end = rs.getTimestamp("end_utc", TimeUtil.getUtcCalendar());
                    AccMachineState accState = AccMachineState.valueOfAllowNull(
                            rs.getString("acc_state"));
                    HallMachineState hallAState = HallMachineState.valueOfAllowNull(rs.getString(
                            "hla_state"));
                    HallMachineState hallBState = HallMachineState.valueOfAllowNull(rs.getString(
                            "hlb_state"));
                    HallMachineState hallCState = HallMachineState.valueOfAllowNull(rs.getString(
                            "hlc_state"));
                    HallMachineState hallDState = HallMachineState.valueOfAllowNull(rs.getString(
                            "hld_state"));
                    String area = rs.getString("area");
                    String cause = rs.getString("cause");

                    // Fault fields
                    String fsdFaultId = rs.getString("fsd_fault_id");
                    String node = rs.getString("node");
                    String channel = rs.getString("channel");
                    String disjointString = rs.getString("disjoint_yn");
                    
                    /*BigInteger fsdFaultId
                            = rs.getBigDecimal("fsd_fault_id") == null ? null : rs.getBigDecimal(
                            "fsd_fault_id").toBigIntegerExact();
                    String node = rs.getString("node");
                    Integer channel = rs.getInt("channel"); // will be 0 if SQL NULL
                    if (rs.wasNull()) {
                        channel = null;
                    }*/

                    // Device fields
                    /*BigInteger fsdDeviceId
                            = rs.getBigDecimal("fsd_device_exception_id") == null ? null : rs.getBigDecimal(
                            "fsd_device_exception_id").toBigIntegerExact();*/
                    String fsdDeviceId = rs.getString("fsd_device_exception_id");
                    String category = rs.getString("category");
                    String systemResult = rs.getString("hco_system_name");
                    String cedType = rs.getString("ced_type");
                    String cedName = rs.getString("ced_name");
                    String confirmedString = rs.getString("fault_confirmation_yn");

                    //JoinedFsdTrip trip = new JoinedFsdTrip();
                    // Trip Attributes
                    /*trip.setFsdTripId(fsdTripId);
                    trip.setStart(start);
                    trip.setEnd(end);
                    trip.setAccState(accState);
                    trip.setHallAState(hallAState);
                    trip.setHallBState(hallBState);
                    trip.setHallCState(hallCState);
                    trip.setHallDState(hallDState);
                    trip.setRootCause(cause);*/
                    // Fault Attributes
                    /*trip.setFsdFaultId(fsdFaultId);
                    trip.setNode(node);
                    trip.setChannel(channel);*/
                    // Device Attributes
                    /*trip.setFsdDeviceExceptionid(fsdDeviceId);
                    trip.setCategory(category);
                    trip.setSystem(systemResult);
                    trip.setCedType(cedType);
                    trip.setCedName(cedName);
                    trip.setConfirmed(confirmedString == null ? null : "Y".equals(confirmedString));*/
                    writer.print(fsdTripId);
                    writer.print(",");
                    writer.print(dateFormatter.format(start));
                    writer.print(",");
                    writer.print(end == null ? "" : dateFormatter.format(end));
                    writer.print(",");
                    writer.print(accState == null ? "" : accState.getLabel());
                    writer.print(",");
                    writer.print(hallAState == null ? "" : hallAState);
                    writer.print(",");
                    writer.print(hallBState == null ? "" : hallBState);
                    writer.print(",");
                    writer.print(hallCState == null ? "" : hallCState);
                    writer.print(",");
                    writer.print(hallDState == null ? "" : hallDState);
                    writer.print(",");
                    writer.print(area == null ? "" : area);
                    writer.print(",");
                    writer.print(cause == null ? "" : cause);
                    writer.print(",");
                    writer.print(fsdFaultId == null ? "" : fsdFaultId);
                    writer.print(",");
                    writer.print(node == null ? "" : node);
                    writer.print(",");
                    writer.print(channel == null ? "" : channel);
                    writer.print(",");
                    writer.print(disjointString == null ? "" : disjointString);
                    writer.print(",");
                    writer.print(fsdDeviceId == null ? "" : fsdDeviceId);
                    writer.print(",");
                    writer.print(category == null ? "" : category);
                    writer.print(",");
                    writer.print(systemResult == null ? "" : systemResult);
                    writer.print(",");
                    writer.print(cedType == null ? "" : cedType);
                    writer.print(",");
                    writer.print(cedName == null ? "" : cedName);
                    writer.print(",");
                    writer.print(confirmedString == null ? "N" : confirmedString);
                    writer.println();
                }
            } /*catch (UnsupportedEncodingException ex) {
                throw new RuntimeException("Unable to encode stream", ex);
            }*/
        } finally {
            IOUtil.close(rs, stmt, con);
        }
    }

    public static class FsdSummary {

        private final double hours;
        private final long count;

        public FsdSummary(double hours, long count) {
            this.hours = hours;
            this.count = count;
        }

        public double getHours() {
            return hours;
        }

        public long getCount() {
            return count;
        }
    }
}
