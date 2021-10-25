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
import org.jlab.dtm.business.params.TuneIncidentsParams;
import org.jlab.dtm.business.session.EventTypeFacade;
import org.jlab.dtm.business.session.ResponsibleGroupFacade;
import org.jlab.dtm.business.session.SystemFacade;
import org.jlab.dtm.persistence.entity.EventType;
import org.jlab.dtm.persistence.entity.ResponsibleGroup;
import org.jlab.dtm.persistence.entity.SystemEntity;
import org.jlab.dtm.presentation.util.DtmParamConverter;
import org.jlab.smoothness.business.util.IOUtil;
import org.jlab.smoothness.business.util.TimeUtil;
import org.jlab.smoothness.presentation.util.*;

public class TuneIncidentstUrlParamHandler implements
        UrlParamHandler<TuneIncidentsParams> {

    private final HttpServletRequest request;
    private final Date today;
    private final Date sevenDaysAgo;
    private final EventTypeFacade typeFacade;
    private final SystemFacade systemFacade;
    private final ResponsibleGroupFacade groupFacade;

    public TuneIncidentstUrlParamHandler(HttpServletRequest request, Date today,
            Date sevenDaysAgo, EventTypeFacade typeFacade, SystemFacade systemFacade, ResponsibleGroupFacade groupFacade) {
        this.request = request;
        this.today = today;
        this.sevenDaysAgo = sevenDaysAgo;
        this.typeFacade = typeFacade;
        this.systemFacade = systemFacade;
        this.groupFacade = groupFacade;
    }

    @Override
    public TuneIncidentsParams convert() {
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
            beamTransport = ParamUtil.convertAndValidateYNBoolean(request, "transport");
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
        
        TuneIncidentsParams params = new TuneIncidentsParams();

        params.setStart(start);
        params.setEnd(end);
        params.setEventTypeId(eventTypeId);
        params.setBeamTransport(beamTransport);
        params.setGroupId(groupId);
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
    public void validate(TuneIncidentsParams params) {
    }

    @Override
    public void store(TuneIncidentsParams params) {
        /* Note: We store each field indivdually as we want to re-use amoung screens*/
 /* Note: We use a 'SECURE' cookie so session changes every request unless over SSL/TLS */
 /* Note: We use an array regardless if the parameter is multi-valued because a null array means no page ever set this param before vs empty array or array with null elements means someone set it, but value is empty*/
        HttpSession session = request.getSession(true);

        session.setAttribute("tuneIncidentsStart[]", new Date[]{params.getStart()});
        session.setAttribute("tuneIncidentsEnd[]", new Date[]{params.getEnd()});
        session.setAttribute("tuneIncidentsEventTypeId[]", new BigInteger[]{params.getEventTypeId()});
        session.setAttribute("tuneIncidentsTransport[]", new Boolean[]{params.getBeamTransport()});
        session.setAttribute("tuneIncidentsSystemId[]", new BigInteger[]{params.getSystemId()});
        session.setAttribute("tuneIncidentsGroup[]", new BigInteger[]{params.getGroupId()});
        session.setAttribute("tuneIncidentsComponent[]", new String[]{params.getComponent()});        
        session.setAttribute("tuneIncidentsChart[]", new String[]{params.getChart()});
        session.setAttribute("tuneIncidentsData[]", new String[]{params.getData()});
        session.setAttribute("tuneIncidentsMaxDuration[]", new Integer[]{params.getMaxDuration()});
        session.setAttribute("tuneIncidentsMinDuration[]", new Integer[]{params.getMinDuration()});
        session.setAttribute("tuneIncidentsMaxDurationUnits[]", new String[]{params.getMaxDurationUnits()});
        session.setAttribute("tuneIncidentsMinDurationUnits[]", new String[]{params.getMinDurationUnits()});        
    }

    @Override
    public TuneIncidentsParams defaults() {
        TuneIncidentsParams defaultParams = new TuneIncidentsParams();

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
    public TuneIncidentsParams materialize() {
        TuneIncidentsParams defaultValues = defaults();

        /* Note: We store each field indivdually as we want to re-use amoung screens*/
 /* Note: We use a 'SECURE' cookie so session changes every request unless over SSL/TLS */
 /* Note: We use an array regardless if the parameter is multi-valued because a null array means no page ever set this param before vs empty array or array with null elements means someone set it, but value is empty*/
        HttpSession session = request.getSession(true);
        Date[] startArray = (Date[]) session.getAttribute("tuneIncidentsStart[]");
        Date[] endArray = (Date[]) session.getAttribute("tuneIncidentsEnd[]");
        BigInteger[] eventTypeIdArray = (BigInteger[]) session.getAttribute("tuneIncidentsEventTypeId[]");
        Boolean[] transportArray = (Boolean[]) session.getAttribute("tuneIncidentsTransport[]");
        BigInteger[] systemIdArray = (BigInteger[]) session.getAttribute("tuneIncidentsSystemId[]");
        BigInteger[] groupIdArray = (BigInteger[])session.getAttribute("tuneIncidentsGroupId[]");
        String[] componentArray = (String[]) session.getAttribute("tuneIncidentsComponent[]");
        String[] chartArray = (String[]) session.getAttribute("tuneIncidentsChart[]");
        String[] dataArray = (String[]) session.getAttribute("tuneIncidentsData[]");
        Integer[] maxDurationArray = (Integer[]) session.getAttribute("tuneIncidentsMaxDuration[]");
        Integer[] minDurationArray = (Integer[]) session.getAttribute("tuneIncidentsMinDuration[]");
        String[] maxDurationUnitsArray = (String[]) session.getAttribute("tuneIncidentsMaxDurationUnits[]");
        String[] minDurationUnitsArray = (String[]) session.getAttribute("tuneIncidentsMinDurationUnits[]");        

        Date start = defaultValues.getStart();
        Date end = defaultValues.getEnd();
        BigInteger eventTypeId = defaultValues.getEventTypeId();
        Boolean transport = defaultValues.getBeamTransport();
        BigInteger systemId = defaultValues.getSystemId();
        BigInteger groupId = defaultValues.getGroupId();
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
        
        TuneIncidentsParams params = new TuneIncidentsParams();

        params.setStart(start);
        params.setEnd(end);
        params.setEventTypeId(eventTypeId);
        params.setBeamTransport(transport);
        params.setGroupId(groupId);
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
    public String message(TuneIncidentsParams params) {

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
        
        ResponsibleGroup selectedGroup = null;
        
        if(params.getGroupId() != null) {
           selectedGroup = groupFacade.find(params.getGroupId());
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
    public void redirect(HttpServletResponse response, TuneIncidentsParams params) throws
            IOException {
        ParamBuilder builder = new ParamBuilder();

        SimpleDateFormat dateFormat
                = new SimpleDateFormat(TimeUtil.getFriendlyDateTimePattern());

        builder.add("start", IOUtil.nullOrFormat(params.getStart(), dateFormat));
        builder.add("end", IOUtil.nullOrFormat(params.getEnd(), dateFormat));
        builder.add("type", IOUtil.nullOrString(params.getEventTypeId()));
        builder.add("transport", IOUtil.nullOrBoolean(params.getBeamTransport()));
        builder.add("system", IOUtil.nullOrString(params.getSystemId()));
        builder.add("group", IOUtil.nullOrString(params.getGroupId()));
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
