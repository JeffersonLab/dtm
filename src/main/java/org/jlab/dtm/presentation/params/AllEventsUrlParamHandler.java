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
import org.jlab.dtm.business.params.AllEventsParams;
import org.jlab.dtm.business.session.EventTypeFacade;
import org.jlab.dtm.persistence.entity.EventType;
import org.jlab.dtm.persistence.enumeration.SystemExpertAcknowledgement;
import org.jlab.dtm.presentation.util.DtmParamConverter;
import org.jlab.smoothness.business.util.IOUtil;
import org.jlab.smoothness.business.util.TimeUtil;
import org.jlab.smoothness.presentation.util.*;

public class AllEventsUrlParamHandler implements UrlParamHandler<AllEventsParams> {

  private final HttpServletRequest request;
  private final EventTypeFacade eventTypeFacade;

  public AllEventsUrlParamHandler(HttpServletRequest request, EventTypeFacade eventTypeFacade) {
    this.request = request;
    this.eventTypeFacade = eventTypeFacade;
  }

  @Override
  public AllEventsParams convert() {
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

    BigInteger eventId = ParamConverter.convertBigInteger(request, "eventId");
    BigInteger[] incidentIdArray = ParamConverter.convertBigIntegerArray(request, "incidentId");

    SystemExpertAcknowledgement acknowledgement =
        DtmParamConverter.convertSystemExpertAcknowledgement(request, "acknowledged");

    String smeUsername = request.getParameter("smeUsername");

    int offset = ParamUtil.convertAndValidateNonNegativeInt(request, "offset", 0);
    int max = ParamUtil.convertAndValidateNonNegativeInt(request, "max", 10);

    AllEventsParams params = new AllEventsParams();

    params.setStart(start);
    params.setEnd(end);
    params.setEventTypeId(eventTypeId);
    params.setBeamTransport(beamTransport);
    params.setEventId(eventId);
    params.setIncidentIdArray(incidentIdArray);
    params.setAcknowledgement(acknowledgement);
    params.setSmeUsername(smeUsername);
    params.setOffset(offset);
    params.setMax(max);

    return params;
  }

  @Override
  public void validate(AllEventsParams params) {}

  @Override
  public void store(AllEventsParams params) {
    /* Note: We store each field indivdually as we want to re-use amoung screens*/
    /* Note: We use a 'SECURE' cookie so session changes every request unless over SSL/TLS */
    /* Note: We use an array regardless if the parameter is multi-valued because a null array means no page ever set this param before vs empty array or array with null elements means someone set it, but value is empty*/
    HttpSession session = request.getSession(true);

    session.setAttribute("allEventsStart[]", new Date[] {params.getStart()});
    session.setAttribute("allEventsEnd[]", new Date[] {params.getEnd()});
    session.setAttribute("allEventsEventTypeId[]", new BigInteger[] {params.getEventTypeId()});
    session.setAttribute("allEventsTransport[]", new Boolean[] {params.getBeamTransport()});
    session.setAttribute("allEventsEventId[]", new BigInteger[] {params.getEventId()});
    session.setAttribute(
        "allEventsIncidentId[]",
        params.getIncidentIdArray() == null ? new BigInteger[0] : params.getIncidentIdArray());
    session.setAttribute("allEventsSmeUsername[]", new String[] {params.getSmeUsername()});
    session.setAttribute("allEventsOffset[]", new Integer[] {params.getOffset()});
    session.setAttribute("allEventsMax[]", new Integer[] {params.getMax()});
  }

  @Override
  public AllEventsParams defaults() {
    AllEventsParams defaultParams = new AllEventsParams();

    defaultParams.setOffset(0);
    defaultParams.setMax(10);

    return defaultParams;
  }

  @Override
  public AllEventsParams materialize() {
    AllEventsParams defaultValues = defaults();

    /* Note: We store each field indivdually as we want to re-use amoung screens*/
    /* Note: We use a 'SECURE' cookie so session changes every request unless over SSL/TLS */
    /* Note: We use an array regardless if the parameter is multi-valued because a null array means no page ever set this param before vs empty array or array with null elements means someone set it, but value is empty*/
    HttpSession session = request.getSession(true);
    Date[] startArray = (Date[]) session.getAttribute("allEventsStart[]");
    Date[] endArray = (Date[]) session.getAttribute("allEventsEnd[]");
    BigInteger[] eventTypeIdArray = (BigInteger[]) session.getAttribute("allEventsEventTypeId[]");
    Boolean[] transportArray = (Boolean[]) session.getAttribute("allEventsTransport[]");
    BigInteger[] eventIdArray = (BigInteger[]) session.getAttribute("allEventsEventId[]");
    BigInteger[] incidentIdArray = (BigInteger[]) session.getAttribute("allEventsIncidentId[]");
    String[] smeUsernameArray = (String[]) session.getAttribute("allEventsSmeUsername[]");
    Integer[] offsetArray = (Integer[]) session.getAttribute("allEventsOffset[]");
    Integer[] maxArray = (Integer[]) session.getAttribute("allEventsMax[]");

    Date start = defaultValues.getStart();
    Date end = defaultValues.getEnd();
    BigInteger eventTypeId = defaultValues.getEventTypeId();
    Boolean transport = defaultValues.getBeamTransport();
    BigInteger eventId = defaultValues.getEventId();
    String smeUsername = defaultValues.getSmeUsername();
    Integer offset = defaultValues.getOffset();
    Integer max = defaultValues.getMax();

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

    if (eventIdArray != null && eventIdArray.length > 0) {
      eventId = eventIdArray[0];
    }

    if (incidentIdArray == null) {
      incidentIdArray = defaultValues.getIncidentIdArray();
    }

    if (smeUsernameArray != null && smeUsernameArray.length > 0) {
      smeUsername = smeUsernameArray[0];
    }

    if (offsetArray != null && offsetArray.length > 0) {
      offset = offsetArray[0];
    }

    if (maxArray != null && maxArray.length > 0) {
      max = maxArray[0];
    }

    AllEventsParams params = new AllEventsParams();

    params.setStart(start);
    params.setEnd(end);
    params.setEventTypeId(eventTypeId);
    params.setBeamTransport(transport);
    params.setEventId(eventId);
    params.setIncidentIdArray(incidentIdArray);
    params.setSmeUsername(smeUsername);
    params.setOffset(offset);
    params.setMax(max);

    return params;
  }

  @Override
  public boolean qualified() {
    return request.getParameter("qualified") != null;
  }

  @Override
  public String message(AllEventsParams params) {
    List<String> filters = new ArrayList<>();

    EventType type = null;

    if (params.getEventTypeId() != null) {
      type = eventTypeFacade.find(params.getEventTypeId());
    }

    if (params.getStart() != null && params.getEnd() != null) {
      filters.add(TimeUtil.formatSmartRangeSeparateTime(params.getStart(), params.getEnd()));
    } else if (params.getStart() != null) {
      filters.add("Starting " + TimeUtil.formatSmartSingleTime(params.getStart()));
    } else if (params.getEnd() != null) {
      filters.add("Before " + TimeUtil.formatSmartSingleTime(params.getEnd()));
    }

    if (type != null) {
      filters.add("Type \"" + type.getShortName() + "\"");
    }

    if (params.getAcknowledgement() != null) {
      filters.add("Acknowledgement \"" + params.getAcknowledgement().getLabel() + "\"");
    }

    if (params.getEventId() != null) {
      filters.add("Event ID \"" + params.getEventId() + "\"");
    }

    if (params.getSmeUsername() != null && !params.getSmeUsername().isEmpty()) {
      filters.add("SME \"" + params.getSmeUsername() + "\"");
    }

    BigInteger[] incidentIdArray =
        IOUtil.removeNullValues(params.getIncidentIdArray(), BigInteger.class);

    if (incidentIdArray != null && incidentIdArray.length > 0) {
      String sublist = "\"" + incidentIdArray[0] + "\"";

      for (int i = 1; i < incidentIdArray.length; i++) {
        BigInteger incidentId = incidentIdArray[i];
        sublist = sublist + ", \"" + incidentId + "\"";
      }

      filters.add("Incident ID " + sublist);
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
  public void redirect(HttpServletResponse response, AllEventsParams params) throws IOException {
    ParamBuilder builder = new ParamBuilder();

    SimpleDateFormat dateFormat = new SimpleDateFormat(TimeUtil.getFriendlyDateTimePattern());

    builder.add("start", IOUtil.nullOrFormat(params.getStart(), dateFormat));
    builder.add("end", IOUtil.nullOrFormat(params.getEnd(), dateFormat));
    builder.add("type", IOUtil.nullOrString(params.getEventTypeId()));
    builder.add("transport", IOUtil.nullOrBoolean(params.getBeamTransport()));
    builder.add("acknowledged", IOUtil.nullOrString(params.getAcknowledgement()));
    builder.add("eventId", IOUtil.nullOrString(params.getEventId()));
    builder.add("incidentId", params.getIncidentIdArray());
    builder.add("smeUsername", IOUtil.nullOrString(params.getSmeUsername()));
    builder.add("offset", IOUtil.nullOrString(params.getOffset()));
    builder.add("max", IOUtil.nullOrString(params.getMax()));
    builder.add("qualified", "");

    String url = ServletUtil.getCurrentUrlAdvanced(request, builder.getParams());

    response.sendRedirect(response.encodeRedirectURL(url));
  }
}
