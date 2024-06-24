package org.jlab.dtm.presentation.params;

import java.io.IOException;
import java.math.BigInteger;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.jlab.dtm.business.params.ComponentDowntimeReportParams;
import org.jlab.dtm.presentation.util.DtmParamConverter;
import org.jlab.smoothness.business.util.IOUtil;
import org.jlab.smoothness.business.util.TimeUtil;
import org.jlab.smoothness.presentation.util.*;

public class ComponentDowntimeReportUrlParamHandler
    implements UrlParamHandler<ComponentDowntimeReportParams> {

  private final HttpServletRequest request;
  private final Date today;
  private final Date sevenDaysAgo;

  public ComponentDowntimeReportUrlParamHandler(
      HttpServletRequest request, Date today, Date sevenDaysAgo) {
    this.request = request;
    this.today = today;
    this.sevenDaysAgo = sevenDaysAgo;
  }

  @Override
  public ComponentDowntimeReportParams convert() {
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

    String chart = request.getParameter("chart");
    String data = request.getParameter("data");

    ComponentDowntimeReportParams params = new ComponentDowntimeReportParams();

    params.setStart(start);
    params.setEnd(end);
    params.setEventTypeId(eventTypeId);
    params.setBeamTransport(beamTransport);
    params.setSystemId(systemId);
    params.setChart(chart);
    params.setData(data);

    return params;
  }

  @Override
  public void validate(ComponentDowntimeReportParams params) {
    if (params.getStart() == null) {
      throw new RuntimeException("start date must not be empty");
    }

    if (params.getEnd() == null) {
      throw new RuntimeException("end date must not be empty");
    }

    if (params.getStart().after(params.getEnd())) {
      throw new RuntimeException("start date must not come before end date");
    }
  }

  @Override
  public void store(ComponentDowntimeReportParams params) {
    /* Note: We store each field indivdually as we want to re-use amoung screens*/
    /* Note: We use a 'SECURE' cookie so session changes every request unless over SSL/TLS */
    /* Note: We use an array regardless if the parameter is multi-valued because a null array means no page ever set this param before vs empty array or array with null elements means someone set it, but value is empty*/
    HttpSession session = request.getSession(true);

    session.setAttribute("start[]", new Date[] {params.getStart()});
    session.setAttribute("end[]", new Date[] {params.getEnd()});
    session.setAttribute("eventTypeId[]", new BigInteger[] {params.getEventTypeId()});
    session.setAttribute("transport[]", new Boolean[] {params.getBeamTransport()});
    session.setAttribute("systemId[]", new BigInteger[] {params.getSystemId()});
    session.setAttribute("chart[]", new String[] {params.getChart()});
    session.setAttribute("data[]", new String[] {params.getData()});
  }

  @Override
  public ComponentDowntimeReportParams defaults() {
    ComponentDowntimeReportParams defaultParams = new ComponentDowntimeReportParams();

    defaultParams.setStart(sevenDaysAgo);
    defaultParams.setEnd(today);
    defaultParams.setEventTypeId(BigInteger.ONE);
    defaultParams.setBeamTransport(false);
    defaultParams.setChart("bar");
    defaultParams.setData("downtime");

    return defaultParams;
  }

  @Override
  public ComponentDowntimeReportParams materialize() {
    ComponentDowntimeReportParams defaultValues = defaults();

    /* Note: We store each field indivdually as we want to re-use amoung screens*/
    /* Note: We use a 'SECURE' cookie so session changes every request unless over SSL/TLS */
    /* Note: We use an array regardless if the parameter is multi-valued because a null array means no page ever set this param before vs empty array or array with null elements means someone set it, but value is empty*/
    HttpSession session = request.getSession(true);
    Date[] startArray = (Date[]) session.getAttribute("start[]");
    Date[] endArray = (Date[]) session.getAttribute("end[]");
    BigInteger[] eventTypeIdArray = (BigInteger[]) session.getAttribute("eventTypeId[]");
    Boolean[] transportArray = (Boolean[]) session.getAttribute("transport[]");
    BigInteger[] systemIdArray = (BigInteger[]) session.getAttribute("systemId[]");
    String[] chartArray = (String[]) session.getAttribute("chart[]");
    String[] dataArray = (String[]) session.getAttribute("data[]");

    Date start = defaultValues.getStart();
    Date end = defaultValues.getEnd();
    BigInteger eventTypeId = defaultValues.getEventTypeId();
    Boolean transport = defaultValues.getBeamTransport();
    BigInteger systemId = defaultValues.getSystemId();
    String chart = defaultValues.getChart();
    String data = defaultValues.getData();

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

    if (systemIdArray != null && systemIdArray.length > 0) {
      systemId = systemIdArray[0];
    }

    if (chartArray != null && chartArray.length > 0) {
      chart = chartArray[0];
    }

    if (dataArray != null && dataArray.length > 0) {
      data = dataArray[0];
    }

    ComponentDowntimeReportParams params = new ComponentDowntimeReportParams();

    params.setStart(start);
    params.setEnd(end);
    params.setEventTypeId(eventTypeId);
    params.setBeamTransport(transport);
    params.setSystemId(systemId);
    params.setChart(chart);
    params.setData(data);

    return params;
  }

  @Override
  public boolean qualified() {
    return request.getParameter("qualified") != null;
  }

  @Override
  public String message(ComponentDowntimeReportParams params) {
    return null;
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
  public void redirect(HttpServletResponse response, ComponentDowntimeReportParams params)
      throws IOException {
    ParamBuilder builder = new ParamBuilder();

    SimpleDateFormat dateFormat = new SimpleDateFormat(TimeUtil.getFriendlyDateTimePattern());

    builder.add("start", IOUtil.nullOrFormat(params.getStart(), dateFormat));
    builder.add("end", IOUtil.nullOrFormat(params.getEnd(), dateFormat));
    builder.add("type", IOUtil.nullOrString(params.getEventTypeId()));
    builder.add("transport", IOUtil.nullOrBoolean(params.getBeamTransport()));
    builder.add("system", IOUtil.nullOrString(params.getSystemId()));
    builder.add("chart", IOUtil.nullOrString(params.getChart()));
    builder.add("data", IOUtil.nullOrString(params.getData()));
    builder.add("qualified", "");

    String url = ServletUtil.getCurrentUrlAdvanced(request, builder.getParams());

    response.sendRedirect(response.encodeRedirectURL(url));
  }
}
