package org.jlab.dtm.presentation.params;

import java.io.IOException;
import java.math.BigInteger;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.jlab.dtm.business.params.TuneComparisonReportParams;
import org.jlab.dtm.presentation.util.DtmParamConverter;
import org.jlab.smoothness.business.util.IOUtil;
import org.jlab.smoothness.business.util.TimeUtil;
import org.jlab.smoothness.presentation.util.ParamBuilder;
import org.jlab.smoothness.presentation.util.ParamConverter;
import org.jlab.smoothness.presentation.util.ServletUtil;
import org.jlab.smoothness.presentation.util.UrlParamHandler;

public class TuneComparisonReportUrlParamHandler
    implements UrlParamHandler<TuneComparisonReportParams> {

  private final HttpServletRequest request;
  private final Date today;
  private final Date sevenDaysAgo;

  public TuneComparisonReportUrlParamHandler(
      HttpServletRequest request, Date today, Date sevenDaysAgo) {
    this.request = request;
    this.today = today;
    this.sevenDaysAgo = sevenDaysAgo;
  }

  @Override
  public TuneComparisonReportParams convert() {
    Date start = null;
    Date end = null;

    try {
      start = DtmParamConverter.convertJLabDateTime(request, "start");
      end = DtmParamConverter.convertJLabDateTime(request, "end");
    } catch (ParseException e) {
      throw new RuntimeException("Unable to parse date", e);
    }

    BigInteger eventTypeId = ParamConverter.convertBigInteger(request, "type");

    String chart = request.getParameter("chart");
    String data = request.getParameter("data");

    TuneComparisonReportParams params = new TuneComparisonReportParams();

    params.setStart(start);
    params.setEnd(end);
    params.setEventTypeId(eventTypeId);
    params.setChart(chart);
    params.setData(data);

    return params;
  }

  @Override
  public void validate(TuneComparisonReportParams params) {
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
  public void store(TuneComparisonReportParams params) {
    /* Note: We store each field indivdually as we want to re-use amoung screens*/
    /* Note: We use a 'SECURE' cookie so session changes every request unless over SSL/TLS */
    /* Note: We use an array regardless if the parameter is multi-valued because a null array means no page ever set this param before vs empty array or array with null elements means someone set it, but value is empty*/
    HttpSession session = request.getSession(true);

    session.setAttribute("tuneComparisonStart[]", new Date[] {params.getStart()});
    session.setAttribute("tuneComparisonEnd[]", new Date[] {params.getEnd()});
    session.setAttribute("tuneComparisonEventTypeId[]", new BigInteger[] {params.getEventTypeId()});
    session.setAttribute("tuneComparisonChart[]", new String[] {params.getChart()});
    session.setAttribute("tuneComparisonData[]", new String[] {params.getData()});
  }

  @Override
  public TuneComparisonReportParams defaults() {
    TuneComparisonReportParams defaultParams = new TuneComparisonReportParams();

    defaultParams.setStart(sevenDaysAgo);
    defaultParams.setEnd(today);
    defaultParams.setEventTypeId(BigInteger.ONE);
    defaultParams.setChart("pareto");
    defaultParams.setData("downtime");

    return defaultParams;
  }

  @Override
  public TuneComparisonReportParams materialize() {
    TuneComparisonReportParams defaultValues = defaults();

    /* Note: We store each field indivdually as we want to re-use amoung screens*/
    /* Note: We use a 'SECURE' cookie so session changes every request unless over SSL/TLS */
    /* Note: We use an array regardless if the parameter is multi-valued because a null array means no page ever set this param before vs empty array or array with null elements means someone set it, but value is empty*/
    HttpSession session = request.getSession(true);
    Date[] startArray = (Date[]) session.getAttribute("tuneComparisonStart[]");
    Date[] endArray = (Date[]) session.getAttribute("tuneComparisonEnd[]");
    BigInteger[] eventTypeIdArray =
        (BigInteger[]) session.getAttribute("tuneComparisonEventTypeId[]");
    String[] chartArray = (String[]) session.getAttribute("tuneComparisonChart[]");
    String[] dataArray = (String[]) session.getAttribute("tuneComparisonData[]");

    Date start = defaultValues.getStart();
    Date end = defaultValues.getEnd();
    BigInteger eventTypeId = defaultValues.getEventTypeId();
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

    if (chartArray != null && chartArray.length > 0) {
      chart = chartArray[0];
    }

    if (dataArray != null && dataArray.length > 0) {
      data = dataArray[0];
    }

    TuneComparisonReportParams params = new TuneComparisonReportParams();

    params.setStart(start);
    params.setEnd(end);
    params.setEventTypeId(eventTypeId);
    params.setChart(chart);
    params.setData(data);

    return params;
  }

  @Override
  public boolean qualified() {
    return request.getParameter("qualified") != null;
  }

  @Override
  public String message(TuneComparisonReportParams params) {
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
  public void redirect(HttpServletResponse response, TuneComparisonReportParams params)
      throws IOException {
    ParamBuilder builder = new ParamBuilder();

    SimpleDateFormat dateFormat = new SimpleDateFormat(TimeUtil.getFriendlyDateTimePattern());

    builder.add("start", IOUtil.nullOrFormat(params.getStart(), dateFormat));
    builder.add("end", IOUtil.nullOrFormat(params.getEnd(), dateFormat));
    builder.add("type", IOUtil.nullOrString(params.getEventTypeId()));
    builder.add("chart", IOUtil.nullOrString(params.getChart()));
    builder.add("data", IOUtil.nullOrString(params.getData()));
    builder.add("qualified", "");

    String url = ServletUtil.getCurrentUrlAdvanced(request, builder.getParams());

    response.sendRedirect(response.encodeRedirectURL(url));
  }
}
