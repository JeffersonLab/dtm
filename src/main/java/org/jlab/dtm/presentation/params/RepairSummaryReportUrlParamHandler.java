package org.jlab.dtm.presentation.params;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.jlab.dtm.business.params.RepairSummaryReportParams;
import org.jlab.dtm.persistence.enumeration.BinSize;
import org.jlab.dtm.presentation.util.DtmParamConverter;
import org.jlab.smoothness.business.util.IOUtil;
import org.jlab.smoothness.business.util.TimeUtil;
import org.jlab.smoothness.presentation.util.*;

public class RepairSummaryReportUrlParamHandler
    implements UrlParamHandler<RepairSummaryReportParams> {

  private final HttpServletRequest request;
  private final Date today;
  private final Date sevenDaysAgo;

  public RepairSummaryReportUrlParamHandler(
      HttpServletRequest request, Date today, Date sevenDaysAgo) {
    this.request = request;
    this.today = today;
    this.sevenDaysAgo = sevenDaysAgo;
  }

  @Override
  public RepairSummaryReportParams convert() {
    Date start = null;
    Date end = null;

    try {
      start = DtmParamConverter.convertJLabDateTime(request, "start");
      end = DtmParamConverter.convertJLabDateTime(request, "end");
    } catch (ParseException e) {
      throw new RuntimeException("Unable to parse date", e);
    }

    String chart = request.getParameter("chart");

    String grouping = request.getParameter("grouping");

    BinSize binSize = DtmParamConverter.convertBinSize(request, "binSize");

    String[] legendDataArray = request.getParameterValues("legendData");

    String[] repairedByArray = request.getParameterValues("repairedBy");

    RepairSummaryReportParams params = new RepairSummaryReportParams();

    params.setStart(start);
    params.setEnd(end);
    params.setChart(chart);
    params.setGrouping(grouping);
    params.setBinSize(binSize);
    params.setLegendDataArray(legendDataArray);
    params.setRepairedByArray(repairedByArray);

    return params;
  }

  @Override
  public void validate(RepairSummaryReportParams params) {
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
  public void store(RepairSummaryReportParams params) {
    /* Note: We store each field indivdually as we want to re-use amoung screens*/
    /* Note: We use a 'SECURE' cookie so session changes every request unless over SSL/TLS */
    /* Note: We use an array regardless if the parameter is multi-valued because a null array means no page ever set this param before vs empty array or array with null elements means someone set it, but value is empty*/
    HttpSession session = request.getSession(true);

    session.setAttribute("start[]", new Date[] {params.getStart()});
    session.setAttribute("end[]", new Date[] {params.getEnd()});
    session.setAttribute("repairChart[]", new String[] {params.getChart()});
    session.setAttribute("repairGrouping[]", new String[] {params.getGrouping()});
    session.setAttribute("binSize[]", new BinSize[] {params.getBinSize()});
    session.setAttribute("repairLegendData[]", params.getLegendDataArray());
    session.setAttribute("repairedBy[]", params.getRepairedByArray());
  }

  @Override
  public RepairSummaryReportParams defaults() {
    RepairSummaryReportParams defaultParams = new RepairSummaryReportParams();

    defaultParams.setStart(sevenDaysAgo);
    defaultParams.setEnd(today);
    defaultParams.setChart("bar");
    defaultParams.setGrouping("cause");
    defaultParams.setLegendDataArray(new String[] {"rate", "lost"});
    defaultParams.setBinSize(BinSize.DAY);

    return defaultParams;
  }

  @Override
  public RepairSummaryReportParams materialize() {
    RepairSummaryReportParams defaultValues = defaults();

    /* Note: We store each field indivdually as we want to re-use amoung screens*/
    /* Note: We use a 'SECURE' cookie so session changes every request unless over SSL/TLS */
    /* Note: We use an array regardless if the parameter is multi-valued because a null array means no page ever set this param before vs empty array or array with null elements means someone set it, but value is empty*/
    HttpSession session = request.getSession(true);
    Date[] startArray = (Date[]) session.getAttribute("start[]");
    Date[] endArray = (Date[]) session.getAttribute("end[]");
    String[] chartArray = (String[]) session.getAttribute("repairChart[]");
    String[] groupingArray = (String[]) session.getAttribute("repairGrouping[]");
    BinSize[] binSizeArray = (BinSize[]) session.getAttribute("binSize[]");
    String[] legendDataArray = (String[]) session.getAttribute("repairLegendData[]");
    String[] repairedByArray = (String[]) session.getAttribute("repairedBy[]");

    Date start = defaultValues.getStart();
    Date end = defaultValues.getEnd();
    String chart = defaultValues.getChart();
    String grouping = defaultValues.getGrouping();
    BinSize binSize = defaultValues.getBinSize();

    if (legendDataArray == null) {
      legendDataArray = defaultValues.getLegendDataArray();
    }

    if (repairedByArray == null) {
      repairedByArray = defaultValues.getRepairedByArray();
    }

    if (startArray != null && startArray.length > 0) {
      start = startArray[0];
    }

    if (endArray != null && endArray.length > 0) {
      end = endArray[0];
    }

    if (chartArray != null && chartArray.length > 0) {
      chart = chartArray[0];
    }

    if (groupingArray != null && groupingArray.length > 0) {
      grouping = groupingArray[0];
    }

    if (binSizeArray != null && binSizeArray.length > 0) {
      binSize = binSizeArray[0];
    }

    RepairSummaryReportParams params = new RepairSummaryReportParams();

    params.setStart(start);
    params.setEnd(end);
    params.setChart(chart);
    params.setGrouping(grouping);
    params.setBinSize(binSize);
    params.setLegendDataArray(legendDataArray);
    params.setRepairedByArray(repairedByArray);

    return params;
  }

  @Override
  public boolean qualified() {
    return request.getParameter("qualified") != null;
  }

  @Override
  public String message(RepairSummaryReportParams params) {
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
  public void redirect(HttpServletResponse response, RepairSummaryReportParams params)
      throws IOException {
    ParamBuilder builder = new ParamBuilder();

    SimpleDateFormat dateFormat = new SimpleDateFormat(TimeUtil.getFriendlyDateTimePattern());

    builder.add("start", IOUtil.nullOrFormat(params.getStart(), dateFormat));
    builder.add("end", IOUtil.nullOrFormat(params.getEnd(), dateFormat));
    builder.add("chart", IOUtil.nullOrString(params.getChart()));
    builder.add("binSize", IOUtil.nullOrString(params.getBinSize()));
    builder.add("grouping", IOUtil.nullOrString(params.getGrouping()));
    builder.add("legendData", params.getLegendDataArray());
    builder.add("repairedBy", params.getRepairedByArray());

    builder.add("qualified", "");

    String url = ServletUtil.getCurrentUrlAdvanced(request, builder.getParams());

    response.sendRedirect(response.encodeRedirectURL(url));
  }
}
