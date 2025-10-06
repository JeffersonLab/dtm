package org.jlab.dtm.presentation.params;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.jlab.dtm.business.params.JouleReportParams;
import org.jlab.dtm.presentation.util.DtmParamConverter;
import org.jlab.smoothness.business.util.IOUtil;
import org.jlab.smoothness.business.util.TimeUtil;
import org.jlab.smoothness.presentation.util.ParamBuilder;
import org.jlab.smoothness.presentation.util.ParamConverter;
import org.jlab.smoothness.presentation.util.ServletUtil;
import org.jlab.smoothness.presentation.util.UrlParamHandler;

public class JouleReportUrlParamHandler implements UrlParamHandler<JouleReportParams> {

  private final HttpServletRequest request;
  private final Date today;
  private final Date sevenDaysAgo;

  public JouleReportUrlParamHandler(HttpServletRequest request, Date today, Date sevenDaysAgo) {
    this.request = request;
    this.today = today;
    this.sevenDaysAgo = sevenDaysAgo;
  }

  @Override
  public JouleReportParams convert() {
    Date start = null;
    Date end = null;

    try {
      start = DtmParamConverter.convertJLabDateTime(request, "start");
      end = DtmParamConverter.convertJLabDateTime(request, "end");
    } catch (ParseException e) {
      throw new RuntimeException("Unable to parse date", e);
    }

    Float quality = ParamConverter.convertFloat(request, "quality");
    Float maintenance = ParamConverter.convertFloat(request, "maintenance");
    Float scaler = ParamConverter.convertFloat(request, "scaler");

    String type = request.getParameter("type");
    String size = request.getParameter("size");

    JouleReportParams params = new JouleReportParams();

    params.setStart(start);
    params.setEnd(end);

    params.setQuality(quality);
    params.setMaintenance(maintenance);
    params.setScaler(scaler);

    params.setType(type);
    params.setSize(size);

    return params;
  }

  @Override
  public void validate(JouleReportParams params) {
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
  public void store(JouleReportParams params) {
    /* Note: We store each field indivdually as we want to re-use amoung screens*/
    /* Note: We use a 'SECURE' cookie so session changes every request unless over SSL/TLS */
    /* Note: We use an array regardless if the parameter is multi-valued because a null array means no page ever set this param before vs empty array or array with null elements means someone set it, but value is empty*/
    HttpSession session = request.getSession(true);

    session.setAttribute("start[]", new Date[] {params.getStart()});
    session.setAttribute("end[]", new Date[] {params.getEnd()});
  }

  @Override
  public JouleReportParams defaults() {
    JouleReportParams defaultParams = new JouleReportParams();

    defaultParams.setStart(sevenDaysAgo);
    defaultParams.setEnd(today);

    defaultParams.setQuality(0f);
    defaultParams.setMaintenance(0f);
    defaultParams.setScaler(1f);

    defaultParams.setType("table");
    defaultParams.setSize("none");

    return defaultParams;
  }

  @Override
  public JouleReportParams materialize() {
    JouleReportParams defaultValues = defaults();

    /* Note: We store each field indivdually as we want to re-use amoung screens*/
    /* Note: We use a 'SECURE' cookie so session changes every request unless over SSL/TLS */
    /* Note: We use an array regardless if the parameter is multi-valued because a null array means no page ever set this param before vs empty array or array with null elements means someone set it, but value is empty*/
    HttpSession session = request.getSession(true);
    Date[] startArray = (Date[]) session.getAttribute("start[]");
    Date[] endArray = (Date[]) session.getAttribute("end[]");
    Float[] qualityArray = (Float[]) session.getAttribute("quality[]");
    Float[] maintenanceArray = (Float[]) session.getAttribute("maintenance[]");

    Date start = defaultValues.getStart();
    Date end = defaultValues.getEnd();
    Float quality = defaultValues.getQuality();
    Float maintenance = defaultValues.getMaintenance();
    Float scaler = defaultValues.getScaler();
    String type = defaultValues.getType();
    String size = defaultValues.getSize();

    if (startArray != null && startArray.length > 0) {
      start = startArray[0];
    }

    if (endArray != null && endArray.length > 0) {
      end = endArray[0];
    }

    if (qualityArray != null && qualityArray.length > 0) {
      quality = qualityArray[0];
    }

    if (maintenanceArray != null && maintenanceArray.length > 0) {
      maintenance = maintenanceArray[0];
    }

    JouleReportParams params = new JouleReportParams();

    params.setStart(start);
    params.setEnd(end);
    params.setQuality(quality);
    params.setMaintenance(maintenance);
    params.setScaler(scaler);
    params.setType(type);
    params.setSize(size);

    return params;
  }

  @Override
  public boolean qualified() {
    return request.getParameter("qualified") != null;
  }

  @Override
  public String message(JouleReportParams params) {
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
  public void redirect(HttpServletResponse response, JouleReportParams params) throws IOException {
    ParamBuilder builder = new ParamBuilder();

    SimpleDateFormat dateFormat = new SimpleDateFormat(TimeUtil.getFriendlyDateTimePattern());

    builder.add("start", IOUtil.nullOrFormat(params.getStart(), dateFormat));
    builder.add("end", IOUtil.nullOrFormat(params.getEnd(), dateFormat));
    builder.add("quality", IOUtil.nullOrString(params.getQuality()));
    builder.add("maintenance", IOUtil.nullOrString(params.getMaintenance()));
    builder.add("scaler", IOUtil.nullOrString(params.getScaler()));
    builder.add("type", IOUtil.nullOrString(params.getType()));
    builder.add("size", IOUtil.nullOrString(params.getSize()));
    builder.add("qualified", "");

    String url = ServletUtil.getCurrentUrlAdvanced(request, builder.getParams());

    response.sendRedirect(response.encodeRedirectURL(url));
  }
}
