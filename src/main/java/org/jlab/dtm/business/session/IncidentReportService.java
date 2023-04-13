package org.jlab.dtm.business.session;

import java.math.BigInteger;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.security.PermitAll;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import org.jlab.dtm.business.params.IncidentDowntimeReportParams;
import org.jlab.dtm.persistence.entity.IncidentReview;
import org.jlab.dtm.persistence.entity.Workgroup;
import org.jlab.dtm.persistence.enumeration.SystemExpertAcknowledgement;
import org.jlab.dtm.persistence.filter.IncidentFilter;
import org.jlab.smoothness.business.util.IOUtil;
import org.jlab.smoothness.persistence.util.JPAUtil;

/**
 *
 * @author ryans
 */
@Stateless
public class IncidentReportService {

    private static final Logger LOGGER = Logger.getLogger(
            IncidentReportService.class.getName());

    @PersistenceContext(unitName = "dtmPU")
    private EntityManager em;

    public static class IncidentSummary {

        private final BigInteger incidentId;
        private final BigInteger eventId;
        private final BigInteger eventTypeId;
        private final String eventTitle;
        private final String title;
        private final String summary;
        private final String type;
        private final String resolution;
        private final double downtimeHoursBounded;
        private final double downtimeHours;
        private final Date timeDown;
        private final Date timeUp;
        private final Date timeDownBounded;
        private final Date timeUpBounded;
        private final String systemName;
        private final BigInteger systemId;
        private final String alphaCatName;
        private final BigInteger alphaCatId;
        private final String componentName;
        private final BigInteger componentId;
        private final BigInteger frequency;
        private final String explanation;
        private final String reviewedByUsername;
        private final SystemExpertAcknowledgement expertAcknowledged;
        private final String rootCause;
        private final BigInteger rarId;

        private List<Workgroup> repairedByList;
        private List<IncidentReview> incidentReviewList;

        public IncidentSummary(Number incidentId, Number eventId, Number eventTypeId,
                String eventTitle, String title,
                String summary, String type, String resolution, String reviewedByUsername,
                Number downtimeHoursBounded, Number downtimeHours, Date timeDown, Date timeUp,
                String systemName, Number systemId, String alphaCatName, Number alphaCatId,
                String componentName, Number componentId,
                Date timeDownBounded, Date timeUpBounded, Number frequency, String explanation, 
                Character expertAcknowledged, String rootCause, Number rarId) {
            this.incidentId = BigInteger.valueOf(incidentId.longValue());
            this.eventId = BigInteger.valueOf(eventId.longValue());
            this.eventTypeId = BigInteger.valueOf(eventTypeId.longValue());
            this.eventTitle = eventTitle;
            this.title = title;
            this.summary = summary;
            this.type = type;
            this.resolution = resolution;
            this.downtimeHoursBounded = downtimeHoursBounded.doubleValue();
            this.downtimeHours = downtimeHours.doubleValue();
            this.timeDown = timeDown;
            this.timeUp = timeUp;
            this.systemName = systemName;
            this.systemId = BigInteger.valueOf(systemId.longValue());
            this.alphaCatName = alphaCatName;
            this.alphaCatId = BigInteger.valueOf(alphaCatId.longValue());
            this.componentName = componentName;
            this.componentId = componentId == null ? null : BigInteger.valueOf(
                    componentId.longValue());
            this.timeDownBounded = timeDownBounded;
            this.timeUpBounded = timeUpBounded;
            this.frequency = BigInteger.valueOf(frequency.longValue());
            this.explanation = explanation;
            this.reviewedByUsername = reviewedByUsername;
            this.expertAcknowledged = SystemExpertAcknowledgement.valueOf(expertAcknowledged.toString());
            this.rootCause = rootCause;
            this.rarId = rarId == null ? null : BigInteger.valueOf(rarId.longValue());
        }

        public String getAlphaCatName() {
            return alphaCatName;
        }

        public BigInteger getAlphaCatId() {
            return alphaCatId;
        }

        public BigInteger getIncidentId() {
            return incidentId;
        }

        public BigInteger getEventId() {
            return eventId;
        }

        public BigInteger getEventTypeId() {
            return eventTypeId;
        }

        public String getEventTitle() {
            return eventTitle;
        }

        public String getTitle() {
            return title;
        }

        public String getSummary() {
            return summary;
        }

        public String getType() {
            return type;
        }

        public String getResolution() {
            return resolution;
        }

        public double getDowntimeHoursBounded() {
            return downtimeHoursBounded;
        }

        public double getDowntimeHours() {
            return downtimeHours;
        }

        public Date getTimeDown() {
            return timeDown;
        }

        public Date getTimeUp() {
            return timeUp;
        }

        public String getSystemName() {
            return systemName;
        }

        public BigInteger getSystemId() {
            return systemId;
        }

        public String getComponentName() {
            return componentName;
        }

        public BigInteger getComponentId() {
            return componentId;
        }

        public Date getTimeDownBounded() {
            return timeDownBounded;
        }

        public Date getTimeUpBounded() {
            return timeUpBounded;
        }

        public BigInteger getFrequency() {
            return frequency;
        }

        public String getExplanation() {
            return explanation;
        }

        public List<Workgroup> getRepairedByList() {
            return repairedByList;
        }

        public void setRepairedByList(List<Workgroup> repairedByList) {
            this.repairedByList = repairedByList;
        }

        public String getRepairedByIdCsv() {
            String csv;

            if (repairedByList != null) {
                BigInteger[] idArray = new BigInteger[repairedByList.size()];

                for (int i = 0; i < idArray.length; i++) {
                    Workgroup group = repairedByList.get(i);
                    idArray[i] = group.getWorkgroupId();
                }

                csv = IOUtil.toCsv(idArray);
            } else {
                csv = "";
            }

            return csv;
        }

        public String getReviewedByUsername() {
            return reviewedByUsername;
        }

        public SystemExpertAcknowledgement getExpertAcknowledged() {
            return expertAcknowledged;
        }

        public String getRootCause() {
            return rootCause;
        }

        public BigInteger getRarId() {
            return rarId;
        }

        public List<IncidentReview> getIncidentReviewList() {
            return incidentReviewList;
        }

        public void setIncidentReviewList(List<IncidentReview> incidentReviewList) {
            this.incidentReviewList = incidentReviewList;
        }

        public String getReviewedByUsernameSsv() {
            String ssv;

            if (incidentReviewList != null) {
                String[] usernameArray = new String[incidentReviewList.size()];

                for (int i = 0; i < usernameArray.length; i++) {
                    IncidentReview review = incidentReviewList.get(i);
                    String reviewer = review.getReviewer();
                    usernameArray[i] = reviewer;
                }

                ssv = IOUtil.toSsv(usernameArray);
            } else {
                ssv = "";
            }

            return ssv;
        }

        public String getReviewLevelString() {

            final long thirtyMinutesInMillis = 30 * 60000;
            final long fourHoursInMillis = 4 * 3600000;

            String level = "Unknown";

            if (timeUp != null) {
                long durationMillis = timeUp.getTime() - timeDown.getTime();

                if (durationMillis < thirtyMinutesInMillis) {
                    level = "Level Ⅰ";
                } else if (durationMillis < fourHoursInMillis) {
                    level = "Level Ⅱ";
                } else {
                    level = "Level Ⅲ+";
                }
            }

            return level;
        }
    }

    @PermitAll
    public List<IncidentSummary> filterList(IncidentDowntimeReportParams params) {
        Date start = params.getStart();
        Date end = params.getEnd();
        String component = params.getComponent();
        boolean sortByDuration = params.isSortByDuration();
        int offset = params.getOffset();
        int max = params.getMax();

        String sql
                = "select a.incident_id, a.event_id, b.event_type_id, b.title as event_title, a.title, a.summary, e.abbreviation as type, resolution, f.username as reviewed_by_username, ";

        if (end != null) {
            sql = sql + "interval_to_seconds(least(coalesce(a.time_up, sysdate), :end) - ";
        } else {
            sql = sql + "interval_to_seconds(coalesce(a.time_up, sysdate) - ";
        }

        if (start != null) {
            sql = sql + "greatest(a.time_down, :start)) / 60 / 60 as bounded_duration_hours, ";
        } else {
            sql = sql + "a.time_down) / 60 / 60 as bounded_duration_hours, ";
        }

        sql = sql
                + "interval_to_seconds(coalesce(a.time_up, sysdate) - a.time_down) / 60 / 60 as unbounded_duration_hours, "
                + "cast(a.time_down as date) as time_down, cast(a.time_up as date) as time_up, c.name as system_name, c.system_id, w.name as alpha_cat_name, w.category_id as alpha_cat_id, d.name as component_name, d.component_id, ";

        if (start != null) {
            sql = sql
                    + "cast(greatest(a.time_down, :start) as date) as time_down_bounded, ";
        } else {
            sql = sql + "cast(a.time_down as date) as time_down_bounded, ";
        }

        if (end != null) {
            sql = sql
                    + "cast(least(coalesce(a.time_up, sysdate), :end) as date) as time_up_bounded, ";
        } else {
            sql = sql + "cast(coalesce(a.time_up, sysdate) as date) as time_up_bounded, ";
        }

        sql = sql
                + "(select count(incident_id) from incident where component_id = a.component_id ";

        if (end != null) {
            sql = sql + "and time_down < :end ";
        }

        if (start != null) {
            sql = sql + "and coalesce(time_up, sysdate) >= :start - 21 ";
        }

        sql = sql
                + ") as frequency, a.missing_explanation, a.expert_acknowledged, a.root_cause, a.rar_id "
                + "from (incident a left outer join dtm_owner.all_components d on a.component_id = d.component_id) inner join dtm_owner.system_alpha_category v on v.system_id = a.system_id inner join dtm_owner.category w on v.category_id = w.category_id inner join dtm_owner.event b on a.event_id = b.event_id inner join dtm_owner.system c on a.system_id = c.system_id inner join dtm_owner.event_type e on b.event_type_id = e.event_type_id";

        IncidentFilter filter = new IncidentFilter(params);
        sql = sql + filter.getSqlWhereClause();

        if (sortByDuration) {
            sql = sql + " order by bounded_duration_hours desc, incident_id desc";
            //sql = sql + "order by frequency desc, incident_id desc";
        } else { // Sort by Time_Down
            sql = sql + " order by time_down desc, incident_id desc";
        }

        LOGGER.log(Level.FINEST, "query: {0}", sql);

        String limitedQuery
                = "select incident_id, event_id, event_type_id, event_title, title, summary, type, resolution, reviewed_by_username, bounded_duration_hours, unbounded_duration_hours, time_down, time_up, system_name, system_id, alpha_cat_name, alpha_cat_id, component_name, component_id, time_down_bounded, time_up_bounded, frequency, missing_explanation, expert_acknowledged, root_cause, rar_id from (select z.*, ROWNUM rnum from ("
                + sql + ") z where ROWNUM <= " + (offset + max) + ") where rnum > " + offset;

        Query q = em.createNativeQuery(limitedQuery);

        if (start != null) {
            q.setParameter("start", start);
        }

        if (end != null) {
            q.setParameter("end", end);
        }

        if (component != null && !component.trim().isEmpty()) {
            q.setParameter("component", component.toUpperCase());
        }

        List<IncidentSummary> incidentList = JPAUtil.getResultList(q, IncidentSummary.class);

        return incidentList;
    }

    @PermitAll
    public long countFilterList(IncidentDowntimeReportParams params) {
        Date start = params.getStart();
        Date end = params.getEnd();
        String component = params.getComponent();

        String sql
                = "select count(*) "
                + "from (incident a left outer join dtm_owner.all_components d on a.component_id = d.component_id) inner join dtm_owner.system_alpha_category v on v.system_id = a.system_id inner join dtm_owner.category w on v.category_id = w.category_id inner join dtm_owner.event b on a.event_id = b.event_id inner join dtm_owner.system c on a.system_id = c.system_id inner join dtm_owner.event_type e on b.event_type_id = e.event_type_id";

        IncidentFilter filter = new IncidentFilter(params);
        sql = sql + filter.getSqlWhereClause();

        Query q = em.createNativeQuery(sql);

        if (start != null) {
            q.setParameter("start", start);
        }

        if (end != null) {
            q.setParameter("end", end);
        }

        if (component != null && !component.trim().isEmpty()) {
            q.setParameter("component", component.toUpperCase());
        }

        long count = 0;
        List resultList = q.getResultList();
        if (resultList != null && resultList.size() > 0) {
            Number number = (Number) resultList.get(0);
            if (number != null) {
                count = number.longValue();
            }
        }

        return count;
    }

    @PermitAll
    public double sumTotalBoundedDuration(IncidentDowntimeReportParams params) {
        Date start = params.getStart();
        Date end = params.getEnd();
        BigInteger eventTypeId = params.getEventTypeId();
        BigInteger systemId = params.getSystemId();
        BigInteger groupId = params.getWorkgroupId();
        String component = params.getComponent();
        Boolean beamTransport = params.getBeamTransport();
        Boolean overnightOpened = params.getOvernightOpended();

        String sql
                = "select sum(interval_to_seconds(";

        if (end != null) {
            sql = sql + "least(nvl(a.time_up, sysdate), :end) - ";
        } else {
            sql = sql + "nvl(a.time_up, sysdate) - ";
        }

        if (start != null) {
            sql = sql + "greatest(a.time_down, :start)";
        } else {
            sql = sql + "a.time_down";
        }

        sql = sql
                + ")) / 60 / 60 "
                + "from incident a left join dtm_owner.all_components d on a.component_id = d.component_id, event b "
                + "where a.event_id = b.event_id ";

        if (end != null) {
            sql = sql
                    + "and a.time_down < :end ";
        }

        if (start != null) {
            sql = sql
                    + "and nvl(a.time_up, sysdate) >= :start ";
        }

        if (eventTypeId != null) {
            sql = sql + "and b.event_type_id = " + eventTypeId + " ";
        }

        if (systemId != null) {
            sql = sql + "and a.system_id = " + systemId + " ";
        }

        if (groupId != null) {
            sql = sql + "and " + groupId + " in (select repaired_by from incident_repair where incident_id = a.incident_id)" + " ";
        }

        if (component != null && !component.trim().isEmpty()) {
            sql = sql + "and upper(d.name) like :component ";
        }

        // beamTransport Y = only beam transport
        // beamTransport N = everything but beam transport
        // Null means don't filter beam transport specially
        if (beamTransport != null) {
            if (beamTransport) {
                sql = sql
                        + "and d.system_id = (select system_id from dtm_owner.system where name = 'Beam Transport') ";
            } else {
                sql = sql
                        + "and d.system_id != (select system_id from dtm_owner.system where name = 'Beam Transport') ";
            }
        }

        // overnightOpened Y = filter only incidents opened overnight
        // overnightOpened N = filter only incidents closed overnight
        // Null menas don't filter by overnight        
        if (overnightOpened != null) {
            if (overnightOpened) {
                sql = sql
                        + "and (to_char(a.time_down, 'HH24') < 7 or to_char(a.time_down, 'HH24') > 19) ";
            } else { // overnight closed
                sql = sql
                        + "and (to_char(a.time_up, 'HH24') < 7 or to_char(a.time_up, 'HH24') > 19) ";
            }
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

            sql = sql + "and (cast(a.time_up as date) - cast(a.time_down as date)) >= " + value + " ";
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

            sql = sql + "and (cast(a.time_up as date) - cast(a.time_down as date)) <= " + value + " ";
        }

        Query q = em.createNativeQuery(sql);

        if (start != null) {
            q.setParameter("start", start);
        }

        if (end != null) {
            q.setParameter("end", end);
        }

        if (component != null && !component.trim().isEmpty()) {
            q.setParameter("component", component.toUpperCase());
        }

        double total = 0;
        List resultList = q.getResultList();
        if (resultList != null && resultList.size() > 0) {
            Number number = (Number) resultList.get(0);
            if (number != null) {
                total = number.doubleValue();
            }
        }

        return total;
    }
}
