package org.jlab.dtm.persistence.filter;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.jlab.dtm.business.params.TripParams;
import org.jlab.dtm.persistence.enumeration.AccMachineState;
import org.jlab.dtm.persistence.enumeration.FsdExceptionType;
import org.jlab.dtm.persistence.enumeration.HallMachineState;
import org.jlab.smoothness.business.util.TimeUtil;

/**
 *
 * @author ryans
 */
public class FsdTripFilter {

    private final Date start;
    private final Date end;
    private final BigInteger maxDuration;
    private final BigInteger minDuration;
    private final String maxDurationUnits;
    private final String minDurationUnits;
    private final String node;
    private final BigInteger channel;
    private final String area;
    private final String cause;
    private final String system;
    private final String cedType;
    private final String cedName;
    private final Integer maxTypes;
    private final Integer maxDevices;
    private final BigInteger tripId;
    private final BigInteger faultId;
    private final BigInteger exceptionId;
    private final AccMachineState[] accStateArray;
    private final HallMachineState[] hallAStateArray;
    private final HallMachineState[] hallBStateArray;
    private final HallMachineState[] hallCStateArray;
    private final HallMachineState[] hallDStateArray;
    private final FsdExceptionType exceptionType;

    public FsdTripFilter(TripParams params) {
        this.start = params.getStart();
        this.end = params.getEnd();
        this.maxDuration = params.getMaxDuration();
        this.minDuration = params.getMinDuration();
        this.maxDurationUnits = params.getMaxDurationUnits();
        this.minDurationUnits = params.getMinDurationUnits();
        this.node = params.getNode();
        this.channel = params.getChannel();
        this.area = params.getArea();
        this.cause = params.getCause();
        this.system = params.getSystem();
        this.cedType = params.getCedType();
        this.cedName = params.getCedName();
        this.maxTypes = params.getMaxTypes();
        this.maxDevices = params.getMaxDevices();
        this.tripId = params.getTripId();
        this.faultId = params.getFaultId();
        this.exceptionId = params.getExceptionId();
        this.accStateArray = params.getAccStateArray();
        this.hallAStateArray = params.getHallAStateArray();
        this.hallBStateArray = params.getHallBStateArray();
        this.hallCStateArray = params.getHallCStateArray();
        this.hallDStateArray = params.getHallDStateArray();
        this.exceptionType = params.getExceptionType();
    }

    public String getSqlWhereClause() {
        String filter = "";

        List<String> filters = new ArrayList<>();

        if (end != null) {
            filters.add("start_utc < ? ");
        }

        if (start != null) {
            filters.add("start_utc >= ? ");
            //filters.add("coalesce(end_utc, sysdate) >= ? ");
        }

        if (maxDuration != null) {
            filters.add(
                    "(coalesce(end_utc, cast(sys_extract_utc(systimestamp) as date)) - start_utc) * 86400 <= ? ");
        }

        if (minDuration != null) {
            filters.add(
                    "(coalesce(end_utc, cast(sys_extract_utc(systimestamp) as date)) - start_utc) * 86400 >= ? ");
        }

        if (node != null && !node.isEmpty()) {
            filters.add("lower(node) like lower(?) ");
        }

        if (channel != null) {
            filters.add("channel = ? ");
        }

        if (area != null && !area.isEmpty()) {
            filters.add("lower(area) like lower(?) ");
        }

        if (cause != null && !cause.isEmpty()) {
            filters.add("lower(cause) like lower(?) ");
        }

        if (system != null && !system.isEmpty()) {
            filters.add("lower(hco_system_name) like lower(?) ");
        }

        if (cedType != null && !cedType.isEmpty()) {
            filters.add("lower(ced_type) like lower(?) ");
        }

        if (cedName != null && !cedName.isEmpty()) {
            filters.add("lower(ced_name) like lower(?) ");
        }

        if (tripId != null) {
            filters.add("a.fsd_trip_id = ? ");
        }

        if (faultId != null) {
            filters.add("fsd_fault_id = ? ");
        }

        if (exceptionId != null) {
            filters.add("fsd_device_exception_id = ? ");
        }

        if (accStateArray != null && accStateArray.length > 0) {
            String stateFilter = "acc_state in (?";
            for (int i = 1; i < accStateArray.length; i++) {
                stateFilter = stateFilter + ",?";
            }
            stateFilter = stateFilter + ") ";
            filters.add(stateFilter);
        }

        if (hallAStateArray != null && hallAStateArray.length > 0) {
            String stateFilter = "hla_state in (?";
            for (int i = 1; i < hallAStateArray.length; i++) {
                stateFilter = stateFilter + ",?";
            }
            stateFilter = stateFilter + ") ";
            filters.add(stateFilter);
        }

        if (hallBStateArray != null && hallBStateArray.length > 0) {
            String stateFilter = "hlb_state in (?";
            for (int i = 1; i < hallBStateArray.length; i++) {
                stateFilter = stateFilter + ",?";
            }
            stateFilter = stateFilter + ") ";
            filters.add(stateFilter);
        }

        if (hallCStateArray != null && hallCStateArray.length > 0) {
            String stateFilter = "hlc_state in (?";
            for (int i = 1; i < hallCStateArray.length; i++) {
                stateFilter = stateFilter + ",?";
            }
            stateFilter = stateFilter + ") ";
            filters.add(stateFilter);
        }

        if (hallDStateArray != null && hallDStateArray.length > 0) {
            String stateFilter = "hld_state in (?";
            for (int i = 1; i < hallDStateArray.length; i++) {
                stateFilter = stateFilter + ",?";
            }
            stateFilter = stateFilter + ") ";
            filters.add(stateFilter);
        }

        if (exceptionType != null) {
            filters.add(
                    "case (select count(fsd_device_exception_id) from fsd_device_exception where fsd_fault_id = b.fsd_fault_id) when 0 then 'Phantom' when 1 then 'Standard' else 'Ambiguous' end = ? ");
        }

        if (maxTypes != null) {
            filters.add(
                    "a.fsd_trip_id in (select fsd_trip_id from (select fsd_trip_id, ced_type from fsd_trip left join fsd_fault using(fsd_trip_id) left join fsd_device_exception using(fsd_fault_id) group by fsd_trip_id, ced_type) group by fsd_trip_id having count(ced_type) <= ?) ");
        }

        if (maxDevices != null) {
            filters.add(
                    "a.fsd_trip_id in (select fsd_trip_id from fsd_trip left join fsd_fault using(fsd_trip_id) left join fsd_device_exception using(fsd_fault_id) group by fsd_trip_id having count(fsd_trip_id) <= ?) ");
        }

        filters.add("cause is not null ");

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

        if (maxDuration != null) {

            long maxDurationSeconds = maxDuration.longValue();

            if ("Minutes".equals(maxDurationUnits)) {
                maxDurationSeconds = maxDurationSeconds * 60;
            } else if ("Hours".equals(maxDurationUnits)) {
                maxDurationSeconds = maxDurationSeconds * 3600;
            }

            stmt.setBigDecimal(i++, new BigDecimal(maxDurationSeconds));
        }

        if (minDuration != null) {
            long minDurationSeconds = minDuration.longValue();

            if ("Minutes".equals(minDurationUnits)) {
                minDurationSeconds = minDurationSeconds * 60;
            } else if ("Hours".equals(minDurationUnits)) {
                minDurationSeconds = minDurationSeconds * 3600;
            }

            stmt.setBigDecimal(i++, new BigDecimal(minDurationSeconds));
        }

        if (node != null && !node.isEmpty()) {
            stmt.setString(i++, node);
        }

        if (channel != null) {
            stmt.setBigDecimal(i++, new BigDecimal(channel));
        }

        if (area != null && !area.isEmpty()) {
            stmt.setString(i++, area);
        }

        if (cause != null && !cause.isEmpty()) {
            stmt.setString(i++, cause);
        }

        if (system != null && !system.isEmpty()) {
            stmt.setString(i++, system);
        }

        if (cedType != null && !cedType.isEmpty()) {
            stmt.setString(i++, cedType);
        }

        if (cedName != null && !cedName.isEmpty()) {
            stmt.setString(i++, cedName);
        }

        if (tripId != null) {
            stmt.setBigDecimal(i++, new BigDecimal(tripId));
        }

        if (faultId != null) {
            stmt.setBigDecimal(i++, new BigDecimal(faultId));
        }

        if (exceptionId != null) {
            stmt.setBigDecimal(i++, new BigDecimal(exceptionId));
        }

        if (accStateArray != null) {
            for (AccMachineState state : accStateArray) {
                stmt.setString(i++, state.name());
            }
        }

        if (hallAStateArray != null) {
            for (HallMachineState state : hallAStateArray) {
                stmt.setString(i++, state.name());
            }
        }

        if (hallBStateArray != null) {
            for (HallMachineState state : hallBStateArray) {
                stmt.setString(i++, state.name());
            }
        }

        if (hallCStateArray != null) {
            for (HallMachineState state : hallCStateArray) {
                stmt.setString(i++, state.name());
            }
        }

        if (hallDStateArray != null) {
            for (HallMachineState state : hallDStateArray) {
                stmt.setString(i++, state.name());
            }
        }

        if (exceptionType != null) {
            stmt.setString(i++, exceptionType.name());
        }

        if (maxTypes != null) {
            stmt.setInt(i++, maxTypes);
        }

        if (maxDevices != null) {
            stmt.setInt(i++, maxDevices);
        }
    }
}
