package org.jlab.dtm.presentation.params;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.jlab.dtm.business.params.FsdSummaryReportParams;
import org.jlab.dtm.persistence.enumeration.BinSize;
import org.jlab.dtm.persistence.enumeration.RootCause;
import org.jlab.dtm.presentation.util.DtmParamConverter;
import org.jlab.smoothness.business.util.IOUtil;
import org.jlab.smoothness.business.util.TimeUtil;
import org.jlab.smoothness.presentation.util.*;

public class FsdSummaryReportUrlParamHandler implements UrlParamHandler<FsdSummaryReportParams> {

  private final HttpServletRequest request;
  private final Date today;
  private final Date sevenDaysAgo;

  public FsdSummaryReportUrlParamHandler(
      HttpServletRequest request, Date today, Date sevenDaysAgo) {
    this.request = request;
    this.today = today;
    this.sevenDaysAgo = sevenDaysAgo;
  }

  @Override
  public FsdSummaryReportParams convert() {
    Date start = null;
    Date end = null;

    try {
      start = DtmParamConverter.convertJLabDateTime(request, "start");
      end = DtmParamConverter.convertJLabDateTime(request, "end");
    } catch (ParseException e) {
      throw new RuntimeException("Unable to parse date", e);
    }

    Integer maxDuration = ParamConverter.convertInteger(request, "maxDuration");

    String maxDurationUnits = request.getParameter("maxDurationUnits");

    Integer maxTypes = ParamConverter.convertInteger(request, "maxTypes");

    String chart = request.getParameter("chart");

    String grouping = request.getParameter("grouping");

    BinSize binSize = DtmParamConverter.convertBinSize(request, "binSize");

    Integer maxY = ParamConverter.convertInteger(request, "maxY");

    String[] legendDataArray = request.getParameterValues("legendData");

    String tripRateBasis = request.getParameter("rateBasis");

    boolean sadTrips = false;

    try {
      sadTrips = ParamUtil.convertAndValidateYNBoolean(request, "sadTrips", false);
    } catch (Exception e) {
      throw new RuntimeException("Unable to parse sadTrips parameter", e);
    }

    RootCause[] causeArray = DtmParamConverter.convertRootCauseArray(request, "cause");

    FsdSummaryReportParams params = new FsdSummaryReportParams();

    params.setStart(start);
    params.setEnd(end);
    params.setChart(chart);
    params.setMaxDuration(maxDuration);
    params.setMaxDurationUnits(maxDurationUnits);
    params.setMaxTypes(maxTypes);
    params.setGrouping(grouping);
    params.setBinSize(binSize);
    params.setMaxY(maxY);
    params.setLegendDataArray(legendDataArray);
    params.setTripRateBasis(tripRateBasis);
    params.setSadTrips(sadTrips);
    params.setCauseArray(causeArray);

    return params;
  }

  @Override
  public void validate(FsdSummaryReportParams params) {
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
  public void store(FsdSummaryReportParams params) {
    /* Note: We store each field indivdually as we want to re-use amoung screens*/
    /* Note: We use a 'SECURE' cookie so session changes every request unless over SSL/TLS */
    /* Note: We use an array regardless if the parameter is multi-valued because a null array means no page ever set this param before vs empty array or array with null elements means someone set it, but value is empty*/
    HttpSession session = request.getSession(true);

    session.setAttribute("start[]", new Date[] {params.getStart()});
    session.setAttribute("end[]", new Date[] {params.getEnd()});
    session.setAttribute("chart[]", new String[] {params.getChart()});
    session.setAttribute("maxDuration[]", new Integer[] {params.getMaxDuration()});
    session.setAttribute("maxDurationUnits[]", new String[] {params.getMaxDurationUnits()});
    session.setAttribute("maxTypes[]", new Integer[] {params.getMaxTypes()});
    session.setAttribute("fsdgrouping[]", new String[] {params.getGrouping()});
    session.setAttribute("binSize[]", new BinSize[] {params.getBinSize()});
    session.setAttribute("maxY[]", new Integer[] {params.getMaxY()});
    session.setAttribute("legendData[]", params.getLegendDataArray());
    session.setAttribute("tripRateBasis[]", new String[] {params.getTripRateBasis()});
    session.setAttribute("sadTrips[]", new Boolean[] {params.getSadTrips()});
    session.setAttribute("cause[]", params.getCauseArray());
  }

  @Override
  public FsdSummaryReportParams defaults() {
    FsdSummaryReportParams defaultParams = new FsdSummaryReportParams();

    defaultParams.setStart(sevenDaysAgo);
    defaultParams.setEnd(today);
    defaultParams.setChart("bar");
    defaultParams.setMaxY(17);
    defaultParams.setMaxDuration(5);
    defaultParams.setMaxDurationUnits("Minutes");
    defaultParams.setGrouping("cause");
    defaultParams.setLegendDataArray(new String[] {"rate", "lost"});
    defaultParams.setBinSize(BinSize.DAY);
    defaultParams.setTripRateBasis("program");
    defaultParams.setSadTrips(false);

    return defaultParams;
  }

  @Override
  public FsdSummaryReportParams materialize() {
    FsdSummaryReportParams defaultValues = defaults();

    /* Note: We store each field indivdually as we want to re-use amoung screens*/
    /* Note: We use a 'SECURE' cookie so session changes every request unless over SSL/TLS */
    /* Note: We use an array regardless if the parameter is multi-valued because a null array means no page ever set this param before vs empty array or array with null elements means someone set it, but value is empty*/
    HttpSession session = request.getSession(true);
    Date[] startArray = (Date[]) session.getAttribute("start[]");
    Date[] endArray = (Date[]) session.getAttribute("end[]");
    String[] chartArray = (String[]) session.getAttribute("chart[]");
    Integer[] maxDurationArray = (Integer[]) session.getAttribute("maxDuration[]");
    String[] maxDurationUnitsArray = (String[]) session.getAttribute("maxDurationUnits[]");
    Integer[] maxTypesArray = (Integer[]) session.getAttribute("maxTypes[]");
    String[] groupingArray = (String[]) session.getAttribute("fsdgrouping[]");
    BinSize[] binSizeArray = (BinSize[]) session.getAttribute("binSize[]");
    Integer[] maxYArray = (Integer[]) session.getAttribute("maxY[]");
    String[] legendDataArray = (String[]) session.getAttribute("legendData[]");
    String[] tripRateBasisArray = (String[]) session.getAttribute("tripRateBasis[]");
    Boolean[] sadTripsArray = (Boolean[]) session.getAttribute("sadTrips[]");
    RootCause[] causeArray = (RootCause[]) session.getAttribute("cause[]");

    Date start = defaultValues.getStart();
    Date end = defaultValues.getEnd();
    String chart = defaultValues.getChart();
    Integer maxDuration = defaultValues.getMaxDuration();
    String maxDurationUnits = defaultValues.getMaxDurationUnits();
    Integer maxTypes = defaultValues.getMaxTypes();
    String grouping = defaultValues.getGrouping();
    BinSize binSize = defaultValues.getBinSize();
    Integer maxY = defaultValues.getMaxY();
    String tripRateBasis = defaultValues.getTripRateBasis();
    Boolean sadTrips = defaultValues.getSadTrips();

    if (legendDataArray == null) {
      legendDataArray = defaultValues.getLegendDataArray();
    }

    if (causeArray == null) {
      causeArray = defaultValues.getCauseArray();
    }

    if (startArray != null && startArray.length > 0) {
      start = startArray[0];
    }

    if (endArray != null && endArray.length > 0) {
      end = endArray[0];
    }

    if (maxDurationArray != null && maxDurationArray.length > 0) {
      maxDuration = maxDurationArray[0];
    }

    if (maxDurationUnitsArray != null && maxDurationUnitsArray.length > 0) {
      maxDurationUnits = maxDurationUnitsArray[0];
    }

    if (chartArray != null && chartArray.length > 0) {
      chart = chartArray[0];
    }

    if (maxTypesArray != null && maxTypesArray.length > 0) {
      maxTypes = maxTypesArray[0];
    }

    if (groupingArray != null && groupingArray.length > 0) {
      grouping = groupingArray[0];
    }

    if (binSizeArray != null && binSizeArray.length > 0) {
      binSize = binSizeArray[0];
    }

    if (maxYArray != null && maxYArray.length > 0) {
      maxY = maxYArray[0];
    }

    if (tripRateBasisArray != null && tripRateBasisArray.length > 0) {
      tripRateBasis = tripRateBasisArray[0];
    }

    if (sadTripsArray != null && sadTripsArray.length > 0) {
      sadTrips = sadTripsArray[0];
    }

    FsdSummaryReportParams params = new FsdSummaryReportParams();

    params.setStart(start);
    params.setEnd(end);
    params.setChart(chart);
    params.setMaxDuration(maxDuration);
    params.setMaxDurationUnits(maxDurationUnits);
    params.setMaxTypes(maxTypes);
    params.setGrouping(grouping);
    params.setBinSize(binSize);
    params.setMaxY(maxY);
    params.setLegendDataArray(legendDataArray);
    params.setTripRateBasis(tripRateBasis);
    params.setSadTrips(sadTrips);
    params.setCauseArray(causeArray);

    return params;
  }

  @Override
  public boolean qualified() {
    return request.getParameter("qualified") != null;
  }

  @Override
  public String message(FsdSummaryReportParams params) {
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
  public void redirect(HttpServletResponse response, FsdSummaryReportParams params)
      throws IOException {
    ParamBuilder builder = new ParamBuilder();

    SimpleDateFormat dateFormat = new SimpleDateFormat(TimeUtil.getFriendlyDateTimePattern());

    builder.add("start", IOUtil.nullOrFormat(params.getStart(), dateFormat));
    builder.add("end", IOUtil.nullOrFormat(params.getEnd(), dateFormat));
    builder.add("chart", IOUtil.nullOrString(params.getChart()));
    builder.add("maxDuration", IOUtil.nullOrString(params.getMaxDuration()));
    builder.add("maxDurationUnits", IOUtil.nullOrString(params.getMaxDurationUnits()));
    builder.add("maxTypes", IOUtil.nullOrString(params.getMaxTypes()));
    builder.add("binSize", IOUtil.nullOrString(params.getBinSize()));
    builder.add("grouping", IOUtil.nullOrString(params.getGrouping()));
    builder.add("maxY", IOUtil.nullOrString(params.getMaxY()));
    builder.add("legendData", params.getLegendDataArray());
    builder.add("rateBasis", IOUtil.nullOrString(params.getTripRateBasis()));
    builder.add("sadTrips", IOUtil.nullOrBoolean(params.getSadTrips()));
    builder.add("cause", params.getCauseArray());

    builder.add("qualified", "");

    String url = ServletUtil.getCurrentUrlAdvanced(request, builder.getParams());

    response.sendRedirect(response.encodeRedirectURL(url));
  }
}
