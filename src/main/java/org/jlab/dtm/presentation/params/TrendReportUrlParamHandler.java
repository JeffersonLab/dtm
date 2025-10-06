package org.jlab.dtm.presentation.params;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.jlab.dtm.business.params.TrendReportParams;
import org.jlab.dtm.presentation.util.DtmParamConverter;
import org.jlab.smoothness.business.util.IOUtil;
import org.jlab.smoothness.business.util.TimeUtil;
import org.jlab.smoothness.presentation.util.ParamBuilder;
import org.jlab.smoothness.presentation.util.ServletUtil;
import org.jlab.smoothness.presentation.util.UrlParamHandler;

public class TrendReportUrlParamHandler implements UrlParamHandler<TrendReportParams> {

  private final HttpServletRequest request;
  private final Date today;
  private final Date sevenDaysAgo;

  public TrendReportUrlParamHandler(HttpServletRequest request, Date today, Date sevenDaysAgo) {
    this.request = request;
    this.today = today;
    this.sevenDaysAgo = sevenDaysAgo;
  }

  @Override
  public TrendReportParams convert() {
    Date start = null;
    Date end = null;

    try {
      start = DtmParamConverter.convertJLabDateTime(request, "start");
      end = DtmParamConverter.convertJLabDateTime(request, "end");
    } catch (ParseException e) {
      throw new RuntimeException("Unable to parse date", e);
    }

    String type = request.getParameter("type");
    String size = request.getParameter("size");

    TrendReportParams params = new TrendReportParams();

    params.setStart(start);
    params.setEnd(end);

    params.setType(type);
    params.setSize(size);

    return params;
  }

  @Override
  public void validate(TrendReportParams params) {
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
  public void store(TrendReportParams params) {
    /* Note: We store each field indivdually as we want to re-use amoung screens*/
    /* Note: We use a 'SECURE' cookie so session changes every request unless over SSL/TLS */
    /* Note: We use an array regardless if the parameter is multi-valued because a null array means no page ever set this param before vs empty array or array with null elements means someone set it, but value is empty*/
    HttpSession session = request.getSession(true);

    session.setAttribute("start[]", new Date[] {params.getStart()});
    session.setAttribute("end[]", new Date[] {params.getEnd()});
    session.setAttribute("size[]", new String[] {params.getSize()});
  }

  @Override
  public TrendReportParams defaults() {
    TrendReportParams defaultParams = new TrendReportParams();

    defaultParams.setStart(sevenDaysAgo);
    defaultParams.setEnd(today);

    defaultParams.setType("table");
    defaultParams.setSize("day");

    return defaultParams;
  }

  @Override
  public TrendReportParams materialize() {
    TrendReportParams defaultValues = defaults();

    /* Note: We store each field indivdually as we want to re-use amoung screens*/
    /* Note: We use a 'SECURE' cookie so session changes every request unless over SSL/TLS */
    /* Note: We use an array regardless if the parameter is multi-valued because a null array means no page ever set this param before vs empty array or array with null elements means someone set it, but value is empty*/
    HttpSession session = request.getSession(true);
    Date[] startArray = (Date[]) session.getAttribute("start[]");
    Date[] endArray = (Date[]) session.getAttribute("end[]");
    String[] sizeArray = (String[]) session.getAttribute("size[]");

    Date start = defaultValues.getStart();
    Date end = defaultValues.getEnd();
    String type = defaultValues.getType();
    String size = defaultValues.getSize();

    if (startArray != null && startArray.length > 0) {
      start = startArray[0];
    }

    if (endArray != null && endArray.length > 0) {
      end = endArray[0];
    }

    if (sizeArray != null && sizeArray.length > 0) {
      size = sizeArray[0];
    }

    TrendReportParams params = new TrendReportParams();

    params.setStart(start);
    params.setEnd(end);
    params.setType(type);
    params.setSize(size);

    return params;
  }

  @Override
  public boolean qualified() {
    return request.getParameter("qualified") != null;
  }

  @Override
  public String message(TrendReportParams params) {
    return TimeUtil.formatSmartRangeSeparateTime(params.getStart(), params.getEnd());
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
  public void redirect(HttpServletResponse response, TrendReportParams params) throws IOException {
    ParamBuilder builder = new ParamBuilder();

    SimpleDateFormat dateFormat = new SimpleDateFormat(TimeUtil.getFriendlyDateTimePattern());

    builder.add("start", IOUtil.nullOrFormat(params.getStart(), dateFormat));
    builder.add("end", IOUtil.nullOrFormat(params.getEnd(), dateFormat));
    builder.add("type", IOUtil.nullOrString(params.getType()));
    builder.add("size", IOUtil.nullOrString(params.getSize()));
    builder.add("qualified", "");

    String url = ServletUtil.getCurrentUrlAdvanced(request, builder.getParams());

    response.sendRedirect(response.encodeRedirectURL(url));
  }
}
