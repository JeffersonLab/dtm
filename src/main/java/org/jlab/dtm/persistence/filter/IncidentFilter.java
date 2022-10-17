package org.jlab.dtm.persistence.filter;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.jlab.dtm.business.params.IncidentDowntimeReportParams;
import org.jlab.smoothness.persistence.filter.RequestFilter;

/**
 *
 * @author ryans
 */
public class IncidentFilter extends RequestFilter<IncidentDowntimeReportParams> {

    public IncidentFilter(IncidentDowntimeReportParams params) {
        super(params);
    }

    @Override
    public String getSqlWhereClause() {
        String filter = "";

        List<String> filters = new ArrayList<>();

        if (params.getEnd() != null) {
            filters.add("a.time_down < :end");
        }

        if (params.getStart() != null) {
            filters.add("coalesce(a.time_up, sysdate) >= :start");
        }

        if (params.getEventTypeId() != null) {
            filters.add("b.event_type_id = " + params.getEventTypeId());
        }

        if (params.getSystemId() != null) {
            filters.add("a.system_id = " + params.getSystemId());
        }

        if (params.getWorkgroupId() != null) {
            filters.add(params.getWorkgroupId() + " in (select repaired_by from incident_repair where incident_id = a.incident_id)");
        }

        if (params.getComponent() != null && !params.getComponent().trim().isEmpty()) {
            filters.add("upper(d.name) like :component");
        }

        final double HOURS_TO_DAYS = 24;
        final double MINUTES_TO_DAYS = 1440;
        final double SECONDS_TO_DAYS = 86400;

        if (params.getMinDuration() != null) {

            double value = params.getMinDuration().doubleValue();

            if ("Hours".equals(params.getMinDurationUnits())) {
                value = value / HOURS_TO_DAYS;
            } else if (("Minutes").equals(params.getMinDurationUnits())) {
                value = value / MINUTES_TO_DAYS;
            } else { // Hope it's seconds
                value = value / SECONDS_TO_DAYS;
            }

            filters.add("(cast(a.time_up as date) - cast(a.time_down as date)) >= " + value);
        }

        if (params.getMaxDuration() != null) {

            double value = params.getMaxDuration().doubleValue();

            if ("Hours".equals(params.getMaxDurationUnits())) {
                value = value / HOURS_TO_DAYS;
            } else if (("Minutes").equals(params.getMaxDurationUnits())) {
                value = value / MINUTES_TO_DAYS;
            } else { // Hope it's seconds
                value = value / SECONDS_TO_DAYS;
            }

            filters.add("(cast(a.time_up as date) - cast(a.time_down as date)) <= " + value);
        }

        // beamTransport Y = only beam transport
        // beamTransport N = everything but beam transport
        // Null means don't filter beam transport specially
        if (params.getBeamTransport() != null) {
            if (params.getBeamTransport()) {
                filters.add("d.system_id = (select system_id from dtm_owner.system where name = 'Beam Transport')");
            } else {
                filters.add("d.system_id != (select system_id from dtm_owner.system where name = 'Beam Transport')");
            }
        }

        // overnightOpened Y = filter only incidents opened overnight
        // overnightOpened N = filter only incidents closed overnight
        // Null menas don't filter by overnight        
        if (params.getOvernightOpended() != null) {
            if (params.getOvernightOpended()) {
                filters.add("(to_char(a.time_down, 'HH24') < 7 or to_char(a.time_down, 'HH24') > 19)");
            } else { // overnight closed
                filters.add("(to_char(a.time_up, 'HH24') < 7 or to_char(a.time_up, 'HH24') > 19)");
            }
        }

        if (!filters.isEmpty()) {
            filter = "where " + filters.get(0);

            if (filters.size() > 1) {
                for (int i = 1; i < filters.size(); i++) {
                    filter = filter + " and " + filters.get(i);
                }
            }
        }

        return filter;
    }

    @Override
    public void assignParameterValues(PreparedStatement stmt) throws SQLException {

    }
}
