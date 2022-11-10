package org.jlab.dtm.presentation.params;

import java.io.IOException;
import java.math.BigInteger;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.jlab.dtm.business.params.IncidentDowntimeReportParams;
import org.jlab.dtm.business.session.EventTypeFacade;
import org.jlab.dtm.business.session.ResponsibleGroupFacade;
import org.jlab.dtm.business.session.SystemFacade;
import org.jlab.dtm.persistence.entity.EventType;
import org.jlab.dtm.persistence.entity.Workgroup;
import org.jlab.dtm.persistence.entity.SystemEntity;
import org.jlab.dtm.presentation.util.DtmParamConverter;
import org.jlab.smoothness.business.util.IOUtil;
import org.jlab.smoothness.business.util.TimeUtil;
import org.jlab.smoothness.presentation.util.*;

public class IncidentDowntimeReportUrlParamHandler implements
        UrlParamHandler<IncidentDowntimeReportParams> {

    private final HttpServletRequest request;
    private final Date today;
    private final Date sevenDaysAgo;
    private final EventTypeFacade typeFacade;
    private final SystemFacade systemFacade;
    private final ResponsibleGroupFacade groupFacade;

    public IncidentDowntimeReportUrlParamHandler(HttpServletRequest request, Date today,
            Date sevenDaysAgo, EventTypeFacade typeFacade, SystemFacade systemFacade, ResponsibleGroupFacade groupFacade) {
        this.request = request;
        this.today = today;
        this.sevenDaysAgo = sevenDaysAgo;
        this.typeFacade = typeFacade;
        this.systemFacade = systemFacade;
        this.groupFacade = groupFacade;
    }

    @Override
    public IncidentDowntimeReportParams convert() {
        Date start = null;
        Date end = null;

        try {
            start = DtmParamConverter.convertJLabDateTime(request, "start");
            end = DtmParamConverter.convertJLabDateTime(request, "end");
        } catch (ParseException e) {
            throw new RuntimeException("Unable to parse date", e);
        }

        BigInteger eventTypeId = ParamConverter.convertBigInteger(request, "type");
        Boolean beamTransport = null;

        try {
            beamTransport = ParamConverter.convertYNBoolean(request, "transport");
        } catch (Exception e) {
            throw new RuntimeException("Unable to parse beam transport boolean", e);
        }

        BigInteger systemId = ParamConverter.convertBigInteger(request, "system");
        
        BigInteger groupId = ParamConverter.convertBigInteger(request, "group");
        
        String component = request.getParameter("component");
        String chart = request.getParameter("chart");
        String data = request.getParameter("data");

        int offset = ParamUtil.convertAndValidateNonNegativeInt(request, "offset", 0);
        int max = ParamUtil.convertAndValidateNonNegativeInt(request, "max", 10);
        
        Integer maxDuration = ParamConverter.convertInteger(request, "maxDuration");
        Integer minDuration = ParamConverter.convertInteger(request, "minDuration");

        String maxDurationUnits = request.getParameter("maxDurationUnits");
        String minDurationUnits = request.getParameter("minDurationUnits");        
        
        IncidentDowntimeReportParams params = new IncidentDowntimeReportParams();

        params.setStart(start);
        params.setEnd(end);
        params.setEventTypeId(eventTypeId);
        params.setBeamTransport(beamTransport);
        params.setWorkgroupId(groupId);
        params.setSystemId(systemId);
        params.setComponent(component);
        params.setChart(chart);
        params.setData(data);
        params.setOffset(offset);
        params.setMax(max);
        params.setMaxDuration(maxDuration);
        params.setMaxDurationUnits(maxDurationUnits);
        params.setMinDuration(minDuration);
        params.setMinDurationUnits(minDurationUnits);

        return params;
    }

    @Override
    public void validate(IncidentDowntimeReportParams params) {
        if (params.getStart() == null) {
            throw new RuntimeException("start date must not be empty");
        }

        if (params.getEnd() == null) {
            throw new RuntimeException("end date must not be empty");
        }
        
        if(params.getStart().after(params.getEnd())) {
            throw new RuntimeException("start date must not come before end date");
        }
    }

    @Override
    public void store(IncidentDowntimeReportParams params) {
        /* Note: We store each field indivdually as we want to re-use amoung screens*/
 /* Note: We use a 'SECURE' cookie so session changes every request unless over SSL/TLS */
 /* Note: We use an array regardless if the parameter is multi-valued because a null array means no page ever set this param before vs empty array or array with null elements means someone set it, but value is empty*/
        HttpSession session = request.getSession(true);

        session.setAttribute("start[]", new Date[]{params.getStart()});
        session.setAttribute("end[]", new Date[]{params.getEnd()});
        session.setAttribute("eventTypeId[]", new BigInteger[]{params.getEventTypeId()});
        session.setAttribute("transport[]", new Boolean[]{params.getBeamTransport()});
        session.setAttribute("systemId[]", new BigInteger[]{params.getSystemId()});
        session.setAttribute("group[]", new BigInteger[]{params.getWorkgroupId()});
        session.setAttribute("component[]", new String[]{params.getComponent()});        
        session.setAttribute("chart[]", new String[]{params.getChart()});
        session.setAttribute("data[]", new String[]{params.getData()});
        session.setAttribute("maxDuration[]", new Integer[]{params.getMaxDuration()});
        session.setAttribute("minDuration[]", new Integer[]{params.getMinDuration()});
        session.setAttribute("maxDurationUnits[]", new String[]{params.getMaxDurationUnits()});
        session.setAttribute("minDurationUnits[]", new String[]{params.getMinDurationUnits()});        
    }

    @Override
    public IncidentDowntimeReportParams defaults() {
        IncidentDowntimeReportParams defaultParams = new IncidentDowntimeReportParams();

        defaultParams.setStart(sevenDaysAgo);
        defaultParams.setEnd(today);
        defaultParams.setEventTypeId(BigInteger.ONE);
        defaultParams.setBeamTransport(false);
        defaultParams.setChart("bar");
        defaultParams.setData("downtime");
        defaultParams.setMaxDurationUnits("Minutes");
        defaultParams.setMinDurationUnits("Minutes");

        return defaultParams;
    }

    @Override
    public IncidentDowntimeReportParams materialize() {
        IncidentDowntimeReportParams defaultValues = defaults();

        /* Note: We store each field indivdually as we want to re-use amoung screens*/
 /* Note: We use a 'SECURE' cookie so session changes every request unless over SSL/TLS */
 /* Note: We use an array regardless if the parameter is multi-valued because a null array means no page ever set this param before vs empty array or array with null elements means someone set it, but value is empty*/
        HttpSession session = request.getSession(true);
        Date[] startArray = (Date[]) session.getAttribute("start[]");
        Date[] endArray = (Date[]) session.getAttribute("end[]");
        BigInteger[] eventTypeIdArray = (BigInteger[]) session.getAttribute("eventTypeId[]");
        Boolean[] transportArray = (Boolean[]) session.getAttribute("transport[]");
        BigInteger[] systemIdArray = (BigInteger[]) session.getAttribute("systemId[]");
        BigInteger[] groupIdArray = (BigInteger[])session.getAttribute("groupId[]");
        String[] componentArray = (String[]) session.getAttribute("component[]");
        String[] chartArray = (String[]) session.getAttribute("chart[]");
        String[] dataArray = (String[]) session.getAttribute("data[]");
        Integer[] maxDurationArray = (Integer[]) session.getAttribute("maxDuration[]");
        Integer[] minDurationArray = (Integer[]) session.getAttribute("minDuration[]");
        String[] maxDurationUnitsArray = (String[]) session.getAttribute("maxDurationUnits[]");
        String[] minDurationUnitsArray = (String[]) session.getAttribute("minDurationUnits[]");        

        Date start = defaultValues.getStart();
        Date end = defaultValues.getEnd();
        BigInteger eventTypeId = defaultValues.getEventTypeId();
        Boolean transport = defaultValues.getBeamTransport();
        BigInteger systemId = defaultValues.getSystemId();
        BigInteger groupId = defaultValues.getWorkgroupId();
        String component = defaultValues.getComponent();
        String chart = defaultValues.getChart();
        String data = defaultValues.getData();
        Integer maxDuration = defaultValues.getMaxDuration();
        Integer minDuration = defaultValues.getMinDuration();
        String maxDurationUnits = defaultValues.getMaxDurationUnits();
        String minDurationUnits = defaultValues.getMinDurationUnits();        

        if (startArray != null && startArray.length > 0) {
            start = startArray[0];
        }

        if (endArray != null && endArray.length > 0) {
            end = endArray[0];
        }

        if (eventTypeIdArray != null && eventTypeIdArray.length > 0) {
            eventTypeId = eventTypeIdArray[0];
        }

        if (transportArray != null && transportArray.length > 0) {
            transport = transportArray[0];
        }

        if (groupIdArray != null && groupIdArray.length > 0) {
            groupId = groupIdArray[0];
        }          
        
        if (systemIdArray != null && systemIdArray.length > 0) {
            systemId = systemIdArray[0];
        }        
        
        if (componentArray != null && componentArray.length > 0) {
            component = componentArray[0];
        }
        
        if (chartArray != null && chartArray.length > 0) {
            chart = chartArray[0];
        }

        if (dataArray != null && dataArray.length > 0) {
            data = dataArray[0];
        }

        if (maxDurationArray != null && maxDurationArray.length > 0) {
            maxDuration = maxDurationArray[0];
        }

        if (minDurationArray != null && minDurationArray.length > 0) {
            minDuration = minDurationArray[0];
        }

        if (maxDurationUnitsArray != null && maxDurationUnitsArray.length > 0) {
            maxDurationUnits = maxDurationUnitsArray[0];
        }

        if (minDurationUnitsArray != null && minDurationUnitsArray.length > 0) {
            minDurationUnits = minDurationUnitsArray[0];
        }        
        
        IncidentDowntimeReportParams params = new IncidentDowntimeReportParams();

        params.setStart(start);
        params.setEnd(end);
        params.setEventTypeId(eventTypeId);
        params.setBeamTransport(transport);
        params.setWorkgroupId(groupId);
        params.setSystemId(systemId);
        params.setComponent(component);
        params.setChart(chart);
        params.setData(data);
        params.setMaxDuration(maxDuration);
        params.setMinDuration(minDuration);
        params.setMaxDurationUnits(maxDurationUnits);
        params.setMinDurationUnits(minDurationUnits);        

        return params;
    }

    @Override
    public boolean qualified() {
        return request.getParameter("qualified") != null;
    }

    @Override
    public String message(IncidentDowntimeReportParams params) {

        List<String> filters = new ArrayList<>();

        String typeQualifier = "";

        EventType selectedType = null;

        if (params.getEventTypeId() != null) {
            selectedType = typeFacade.find(params.getEventTypeId());
        }
        
        SystemEntity selectedSystem = null;

        if (params.getSystemId() != null) {
            selectedSystem = systemFacade.find(params.getSystemId());
        }
        
        Workgroup selectedGroup = null;
        
        if(params.getWorkgroupId() != null) {
           selectedGroup = groupFacade.find(params.getWorkgroupId());
        }        
        
        if (params.getStart() != null && params.getEnd() != null) {
            filters.add(TimeUtil.formatSmartRangeSeparateTime(params.getStart(), params.getEnd()));
        } else if (params.getStart() != null) {
            filters.add("Starting " + TimeUtil.formatSmartSingleTime(params.getStart()));
        } else if (params.getEnd() != null) {
            filters.add("Before " + TimeUtil.formatSmartSingleTime(params.getEnd()));
        }        

        
        if (params.getMaxDuration() != null) {
            filters.add("Max Duration \"" + params.getMaxDuration() + " (" + params.getMaxDurationUnits() + ")\"");
        }

        if (params.getMinDuration() != null) {
            filters.add("Min Duration \"" + params.getMinDuration() + " (" + params.getMinDurationUnits() + ")\"");
        }         
        
        if (selectedType != null) {
            filters.add("Type \"" + selectedType.getName() + "\"");
        }

        if (selectedSystem != null) {
            filters.add("System " + selectedSystem.getName());
        }

        if (params.getComponent() != null && !params.getComponent().trim().isEmpty()) {
            filters.add("Component matches \"" + params.getComponent() + "\"");
        }

        if (params.getBeamTransport() != null) {
            if (params.getBeamTransport()) {
                filters.add("Beam Transport only");
            } else {
                filters.add("Beam Transport excluded");
            }
        }
        
        if(selectedGroup != null) {
            filters.add("Repaired by " + selectedGroup.getName());
        }               

        String message = "";

        if (!filters.isEmpty()) {
            message = filters.get(0);

            for (int i = 1; i < filters.size(); i++) {
                String filter = filters.get(i);
                message += " and " + filter;
            }
        }      
        
        return message;
    }

    /**
     * Sends a redirect response indicating the qualified URL. If calling this method from a Servlet
     * doGet method generally a return statement should immediately follow. This method is useful to
     * maintain a restful / bookmarkable URL for the user.
     *
     * @param response The Servlet response
     * @param params The parameter object
     * @throws IOException If unable to redirect
     */
    @Override
    public void redirect(HttpServletResponse response, IncidentDowntimeReportParams params) throws
            IOException {
        ParamBuilder builder = new ParamBuilder();

        SimpleDateFormat dateFormat
                = new SimpleDateFormat(TimeUtil.getFriendlyDateTimePattern());

        builder.add("start", IOUtil.nullOrFormat(params.getStart(), dateFormat));
        builder.add("end", IOUtil.nullOrFormat(params.getEnd(), dateFormat));
        builder.add("type", IOUtil.nullOrString(params.getEventTypeId()));
        builder.add("transport", IOUtil.nullOrBoolean(params.getBeamTransport()));
        builder.add("system", IOUtil.nullOrString(params.getSystemId()));
        builder.add("group", IOUtil.nullOrString(params.getWorkgroupId()));
        builder.add("component", IOUtil.nullOrString(params.getComponent()));
        builder.add("chart", IOUtil.nullOrString(params.getChart()));
        builder.add("data", IOUtil.nullOrString(params.getData()));
        builder.add("maxDuration", IOUtil.nullOrString(params.getMaxDuration()));
        builder.add("minDuration", IOUtil.nullOrString(params.getMinDuration()));
        builder.add("maxDurationUnits", IOUtil.nullOrString(params.getMaxDurationUnits()));
        builder.add("minDurationUnits", IOUtil.nullOrString(params.getMinDurationUnits()));        
        builder.add("qualified", "");

        String url = ServletUtil.getCurrentUrlAdvanced(request, builder.getParams());

        response.sendRedirect(
                response.encodeRedirectURL(url));
    }
}
