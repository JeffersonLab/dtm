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
import org.jlab.dtm.business.params.TripParams;
import org.jlab.dtm.persistence.enumeration.AccMachineState;
import org.jlab.dtm.persistence.enumeration.FsdExceptionType;
import org.jlab.dtm.persistence.enumeration.HallMachineState;
import org.jlab.dtm.presentation.util.DtmParamConverter;
import org.jlab.smoothness.business.util.IOUtil;
import org.jlab.smoothness.business.util.TimeUtil;
import org.jlab.smoothness.presentation.util.ParamBuilder;
import org.jlab.smoothness.presentation.util.ParamConverter;
import org.jlab.smoothness.presentation.util.ServletUtil;
import org.jlab.smoothness.presentation.util.UrlParamHandler;

public class TripUrlParamHandler implements UrlParamHandler<TripParams> {

  private final HttpServletRequest request;

  public TripUrlParamHandler(HttpServletRequest request) {
    this.request = request;
  }

  @Override
  public TripParams convert() {
    Date start = null;
    Date end = null;

    try {
      start = DtmParamConverter.convertJLabDateTime(request, "start");
      end = DtmParamConverter.convertJLabDateTime(request, "end");
    } catch (ParseException e) {
      throw new RuntimeException("Unable to parse date", e);
    }

    BigInteger maxDuration = ParamConverter.convertBigInteger(request, "maxDuration");
    BigInteger minDuration = ParamConverter.convertBigInteger(request, "minDuration");

    String maxDurationUnits = request.getParameter("maxDurationUnits");
    String minDurationUnits = request.getParameter("minDurationUnits");

    AccMachineState[] accStateArray = DtmParamConverter.convertAccStateArray(request, "accState");
    HallMachineState[] hallAStateArray =
        DtmParamConverter.convertHallStateArray(request, "hallAState");
    HallMachineState[] hallBStateArray =
        DtmParamConverter.convertHallStateArray(request, "hallBState");
    HallMachineState[] hallCStateArray =
        DtmParamConverter.convertHallStateArray(request, "hallCState");
    HallMachineState[] hallDStateArray =
        DtmParamConverter.convertHallStateArray(request, "hallDState");

    String node = request.getParameter("node");
    BigInteger channel = ParamConverter.convertBigInteger(request, "channel");

    String area = request.getParameter("area");

    String cause = request.getParameter("cause");

    String system = request.getParameter("system");

    String cedType = request.getParameter("cedType");
    String cedName = request.getParameter("cedName");

    FsdExceptionType exceptionType =
        DtmParamConverter.convertFsdExceptionType(request, "exceptionType");

    Integer maxTypes = ParamConverter.convertInteger(request, "maxTypes");
    Integer maxDevices = ParamConverter.convertInteger(request, "maxDevices");

    BigInteger tripId = ParamConverter.convertBigInteger(request, "tripId");
    // BigInteger faultId = DtmParamConverter.convertBigInteger(request, "faultId");
    // BigInteger exceptionId = DtmParamConverter.convertBigInteger(request, "exceptionId");

    TripParams params = new TripParams();

    params.setStart(start);
    params.setEnd(end);
    params.setMaxDuration(maxDuration);
    params.setMinDuration(minDuration);
    params.setMaxDurationUnits(maxDurationUnits);
    params.setMinDurationUnits(minDurationUnits);
    params.setAccStateArray(accStateArray);
    params.setHallAStateArray(hallAStateArray);
    params.setHallBStateArray(hallBStateArray);
    params.setHallCStateArray(hallCStateArray);
    params.setHallDStateArray(hallDStateArray);
    params.setNode(node);
    params.setChannel(channel);
    params.setArea(area);
    params.setCause(cause);
    params.setSystem(system);
    params.setCedType(cedType);
    params.setCedName(cedName);
    params.setExceptionType(exceptionType);
    params.setMaxTypes(maxTypes);
    params.setMaxDevices(maxDevices);
    params.setTripId(tripId);
    // params.setFaultId(faultId);
    // params.setExceptionId(exceptionId);

    return params;
  }

  @Override
  public void validate(TripParams params) {
    if (params.getStart() != null
        && params.getEnd() != null
        && (params.getStart().after(params.getEnd()))) {
      throw new RuntimeException("start date must not come before end date");
    }
  }

  @Override
  public void store(TripParams params) {
    /* Note: We store each field indivdually as we want to re-use amoung screens*/
    /* Note: We use a 'SECURE' cookie so session changes every request unless over SSL/TLS */
    /* Note: We use an array regardless if the parameter is multi-valued because a null array means no page ever set this param before vs empty array or array with null elements means someone set it, but value is empty*/
    HttpSession session = request.getSession(true);

    session.setAttribute("tripStart[]", new Date[] {params.getStart()});
    session.setAttribute("tripEnd[]", new Date[] {params.getEnd()});
    session.setAttribute("tripMaxDuration[]", new BigInteger[] {params.getMaxDuration()});
    session.setAttribute("tripMinDuration[]", new BigInteger[] {params.getMinDuration()});
    session.setAttribute("tripMaxDurationUnits[]", new String[] {params.getMaxDurationUnits()});
    session.setAttribute("tripMinDurationUnits[]", new String[] {params.getMinDurationUnits()});
    session.setAttribute(
        "tripAccState[]",
        params.getAccStateArray() == null ? new AccMachineState[0] : params.getAccStateArray());
    session.setAttribute(
        "tripHallAState[]",
        params.getHallAStateArray() == null
            ? new HallMachineState[0]
            : params.getHallAStateArray());
    session.setAttribute(
        "tripHallBState[]",
        params.getHallBStateArray() == null
            ? new HallMachineState[0]
            : params.getHallBStateArray());
    session.setAttribute(
        "tripHallCState[]",
        params.getHallCStateArray() == null
            ? new HallMachineState[0]
            : params.getHallCStateArray());
    session.setAttribute(
        "tripHallDState[]",
        params.getHallDStateArray() == null
            ? new HallMachineState[0]
            : params.getHallDStateArray());
    session.setAttribute("tripArea[]", new String[] {params.getArea()});
    session.setAttribute("tripCause[]", new String[] {params.getCause()});
    session.setAttribute("tripSystem[]", new String[] {params.getSystem()});
    session.setAttribute("tripCedType[]", new String[] {params.getCedType()});
    session.setAttribute("tripCedName[]", new String[] {params.getCedName()});
    session.setAttribute("tripExceptionType[]", new FsdExceptionType[] {params.getExceptionType()});
    session.setAttribute("tripMaxTypes[]", new Integer[] {params.getMaxTypes()});
    session.setAttribute("tripMaxDevices[]", new Integer[] {params.getMaxDevices()});
    session.setAttribute("tripNode[]", new String[] {params.getNode()});
    session.setAttribute("tripChannel[]", new BigInteger[] {params.getChannel()});
    session.setAttribute("tripTripId[]", new BigInteger[] {params.getTripId()});
  }

  @Override
  public TripParams defaults() {
    TripParams defaultParams = new TripParams();

    return defaultParams;
  }

  @Override
  public TripParams materialize() {
    TripParams defaultValues = defaults();

    /* Note: We store each field indivdually as we want to re-use amoung screens*/
    /* Note: We use a 'SECURE' cookie so session changes every request unless over SSL/TLS */
    /* Note: We use an array regardless if the parameter is multi-valued because a null array means no page ever set this param before vs empty array or array with null elements means someone set it, but value is empty*/
    HttpSession session = request.getSession(true);
    Date[] startArray = (Date[]) session.getAttribute("tripStart[]");
    Date[] endArray = (Date[]) session.getAttribute("tripEnd[]");
    BigInteger[] maxDurationArray = (BigInteger[]) session.getAttribute("tripMaxDuration[]");
    BigInteger[] minDurationArray = (BigInteger[]) session.getAttribute("tripMinDuration[]");
    String[] maxDurationUnitsArray = (String[]) session.getAttribute("tripMaxDurationUnits[]");
    String[] minDurationUnitsArray = (String[]) session.getAttribute("tripMinDurationUnits[]");
    AccMachineState[] accStateArray = (AccMachineState[]) session.getAttribute("tripAccState[]");
    HallMachineState[] hallAStateArray =
        (HallMachineState[]) session.getAttribute("tripHallAState[]");
    HallMachineState[] hallBStateArray =
        (HallMachineState[]) session.getAttribute("tripHallBState[]");
    HallMachineState[] hallCStateArray =
        (HallMachineState[]) session.getAttribute("tripHallCState[]");
    HallMachineState[] hallDStateArray =
        (HallMachineState[]) session.getAttribute("tripHallDState[]");
    String[] areaArray = (String[]) session.getAttribute("tripArea[]");
    String[] causeArray = (String[]) session.getAttribute("tripCause[]");
    String[] systemArray = (String[]) session.getAttribute("tripSystem[]");
    String[] cedTypeArray = (String[]) session.getAttribute("tripCedType[]");
    String[] cedNameArray = (String[]) session.getAttribute("tripCedName[]");
    FsdExceptionType[] exceptionTypeArray =
        (FsdExceptionType[]) session.getAttribute("tripExceptionType[]");
    Integer[] maxTypesArray = (Integer[]) session.getAttribute("tripMaxTypes[]");
    Integer[] maxDevicesArray = (Integer[]) session.getAttribute("tripMaxDevices[]");
    String[] nodeArray = (String[]) session.getAttribute("tripNode[]");
    BigInteger[] channelArray = (BigInteger[]) session.getAttribute("tripChannel[]");
    BigInteger[] tripIdArray = (BigInteger[]) session.getAttribute("tripTripId[]");

    Date start = defaultValues.getStart();
    Date end = defaultValues.getEnd();
    BigInteger maxDuration = defaultValues.getMaxDuration();
    BigInteger minDuration = defaultValues.getMinDuration();
    String maxDurationUnits = defaultValues.getMaxDurationUnits();
    String minDurationUnits = defaultValues.getMinDurationUnits();
    String area = defaultValues.getArea();
    String cause = defaultValues.getCause();
    String system = defaultValues.getSystem();
    String cedType = defaultValues.getCedType();
    String cedName = defaultValues.getCedName();
    FsdExceptionType exceptionType = defaultValues.getExceptionType();
    Integer maxTypes = defaultValues.getMaxTypes();
    Integer maxDevices = defaultValues.getMaxDevices();
    String node = defaultValues.getNode();
    BigInteger channel = defaultValues.getChannel();
    BigInteger tripId = defaultValues.getTripId();

    if (startArray != null && startArray.length > 0) {
      start = startArray[0];
    }

    if (endArray != null && endArray.length > 0) {
      end = endArray[0];
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

    if (accStateArray == null) {
      accStateArray = defaultValues.getAccStateArray();
    }

    if (hallAStateArray == null) {
      hallAStateArray = defaultValues.getHallAStateArray();
    }

    if (hallBStateArray == null) {
      hallBStateArray = defaultValues.getHallBStateArray();
    }

    if (hallCStateArray == null) {
      hallCStateArray = defaultValues.getHallCStateArray();
    }

    if (hallDStateArray == null) {
      hallDStateArray = defaultValues.getHallDStateArray();
    }

    if (areaArray != null && areaArray.length > 0) {
      area = areaArray[0];
    }

    if (causeArray != null && causeArray.length > 0) {
      cause = causeArray[0];
    }

    if (systemArray != null && systemArray.length > 0) {
      system = systemArray[0];
    }

    if (cedTypeArray != null && cedTypeArray.length > 0) {
      cedType = cedTypeArray[0];
    }

    if (cedNameArray != null && cedNameArray.length > 0) {
      cedName = cedNameArray[0];
    }

    if (exceptionTypeArray != null && exceptionTypeArray.length > 0) {
      exceptionType = exceptionTypeArray[0];
    }

    if (maxTypesArray != null && maxTypesArray.length > 0) {
      maxTypes = maxTypesArray[0];
    }

    if (maxDevicesArray != null && maxDevicesArray.length > 0) {
      maxDevices = maxDevicesArray[0];
    }

    if (nodeArray != null && nodeArray.length > 0) {
      node = nodeArray[0];
    }

    if (channelArray != null && channelArray.length > 0) {
      channel = channelArray[0];
    }

    if (tripIdArray != null && tripIdArray.length > 0) {
      tripId = tripIdArray[0];
    }

    TripParams params = new TripParams();

    params.setStart(start);
    params.setEnd(end);
    params.setMaxDuration(maxDuration);
    params.setMinDuration(minDuration);
    params.setMaxDurationUnits(maxDurationUnits);
    params.setMinDurationUnits(minDurationUnits);
    params.setAccStateArray(accStateArray);
    params.setHallAStateArray(hallAStateArray);
    params.setHallBStateArray(hallBStateArray);
    params.setHallCStateArray(hallCStateArray);
    params.setHallDStateArray(hallDStateArray);
    params.setArea(area);
    params.setCause(cause);
    params.setSystem(system);
    params.setCedType(cedType);
    params.setCedName(cedName);
    params.setExceptionType(exceptionType);
    params.setMaxTypes(maxTypes);
    params.setMaxDevices(maxDevices);
    params.setNode(node);
    params.setChannel(channel);
    params.setTripId(tripId);

    return params;
  }

  @Override
  public boolean qualified() {
    return request.getParameter("qualified") != null;
  }

  @Override
  public String message(TripParams params) {
    String dateFormat = TimeUtil.getFriendlyDateTimePattern();

    SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);

    List<String> filters = new ArrayList<>();

    if (params.getStart() != null && params.getEnd() != null) {
      filters.add(TimeUtil.formatSmartRangeSeparateTime(params.getStart(), params.getEnd()));
    } else if (params.getStart() != null) {
      filters.add("Starting " + TimeUtil.formatSmartSingleTime(params.getStart()));
    } else if (params.getEnd() != null) {
      filters.add("Before " + TimeUtil.formatSmartSingleTime(params.getEnd()));
    }

    if (params.getExceptionType() != null) {
      filters.add("Exception Type \"" + params.getExceptionType().name() + "\"");
    }

    if (params.getCedType() != null && !params.getCedType().isEmpty()) {
      filters.add("CED Type \"" + params.getCedType() + "\"");
    }

    if (params.getMaxDuration() != null) {
      filters.add(
          "Max Duration \""
              + params.getMaxDuration()
              + " ("
              + params.getMaxDurationUnits()
              + ")\"");
    }

    if (params.getMinDuration() != null) {
      filters.add(
          "Min Duration \""
              + params.getMinDuration()
              + " ("
              + params.getMinDurationUnits()
              + ")\"");
    }

    if (params.getArea() != null && !params.getArea().isEmpty()) {
      filters.add("Area \"" + params.getArea() + "\"");
    }

    if (params.getCause() != null && !params.getCause().isEmpty()) {
      filters.add("Cause \"" + params.getCause() + "\"");
    }

    if (params.getSystem() != null && !params.getSystem().isEmpty()) {
      filters.add("System \"" + params.getSystem() + "\"");
    }

    if (params.getCedName() != null && !params.getCedName().isEmpty()) {
      filters.add("CED Name \"" + params.getCedName() + "\"");
    }

    if (params.getMaxTypes() != null) {
      filters.add("Max Types /Trip \"" + params.getMaxTypes() + "\"");
    }

    if (params.getMaxDevices() != null) {
      filters.add("Max Devices /Trip \"" + params.getMaxDevices() + "\"");
    }

    if (params.getNode() != null && !params.getNode().isEmpty()) {
      filters.add("Node \"" + params.getNode() + "\"");
    }

    if (params.getChannel() != null) {
      filters.add("Channel \"" + params.getChannel() + "\"");
    }

    if (params.getAccStateArray() != null && params.getAccStateArray().length > 0) {
      String sublist = "\"" + params.getAccStateArray()[0].getLabel() + "\"";

      for (int i = 1; i < params.getAccStateArray().length; i++) {
        AccMachineState state = params.getAccStateArray()[i];
        sublist = sublist + ", \"" + state.getLabel() + "\"";
      }

      filters.add("ACC State " + sublist);
    }

    if (params.getHallAStateArray() != null && params.getHallAStateArray().length > 0) {
      String sublist = "\"" + params.getHallAStateArray()[0].getLabel() + "\"";

      for (int i = 1; i < params.getHallAStateArray().length; i++) {
        HallMachineState state = params.getHallAStateArray()[i];
        sublist = sublist + ", \"" + state.getLabel() + "\"";
      }

      filters.add("Hall A State " + sublist);
    }

    if (params.getHallBStateArray() != null && params.getHallBStateArray().length > 0) {
      String sublist = "\"" + params.getHallBStateArray()[0].getLabel() + "\"";

      for (int i = 1; i < params.getHallBStateArray().length; i++) {
        HallMachineState state = params.getHallBStateArray()[i];
        sublist = sublist + ", \"" + state.getLabel() + "\"";
      }

      filters.add("Hall B State " + sublist);
    }

    if (params.getHallCStateArray() != null && params.getHallCStateArray().length > 0) {
      String sublist = "\"" + params.getHallCStateArray()[0].getLabel() + "\"";

      for (int i = 1; i < params.getHallCStateArray().length; i++) {
        HallMachineState state = params.getHallCStateArray()[i];
        sublist = sublist + ", \"" + state.getLabel() + "\"";
      }

      filters.add("Hall C State " + sublist);
    }

    if (params.getHallDStateArray() != null && params.getHallDStateArray().length > 0) {
      String sublist = "\"" + params.getHallDStateArray()[0].getLabel() + "\"";

      for (int i = 1; i < params.getHallDStateArray().length; i++) {
        HallMachineState state = params.getHallDStateArray()[i];
        sublist = sublist + ", \"" + state.getLabel() + "\"";
      }

      filters.add("Hall D State " + sublist);
    }

    if (params.getTripId() != null) {
      filters.add("Trip ID \"" + params.getTripId() + "\"");
    }

    if (params.getFaultId() != null) {
      filters.add("Fault ID \"" + params.getFaultId() + "\"");
    }

    if (params.getExceptionId() != null) {
      filters.add("Device Exception ID \"" + params.getExceptionId() + "\"");
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
  public void redirect(HttpServletResponse response, TripParams params) throws IOException {
    ParamBuilder builder = new ParamBuilder();

    SimpleDateFormat dateFormat = new SimpleDateFormat(TimeUtil.getFriendlyDateTimePattern());

    builder.add("start", IOUtil.nullOrFormat(params.getStart(), dateFormat));
    builder.add("end", IOUtil.nullOrFormat(params.getEnd(), dateFormat));
    builder.add("maxDuration", IOUtil.nullOrString(params.getMaxDuration()));
    builder.add("minDuration", IOUtil.nullOrString(params.getMinDuration()));
    builder.add("maxDurationUnits", IOUtil.nullOrString(params.getMaxDurationUnits()));
    builder.add("minDurationUnits", IOUtil.nullOrString(params.getMinDurationUnits()));
    builder.add("accState", params.getAccStateArray());
    builder.add("hallAState", params.getHallAStateArray());
    builder.add("hallBState", params.getHallBStateArray());
    builder.add("hallCState", params.getHallCStateArray());
    builder.add("hallDState", params.getHallDStateArray());
    builder.add("area", IOUtil.nullOrString(params.getArea()));
    builder.add("cause", IOUtil.nullOrString(params.getCause()));
    builder.add("system", IOUtil.nullOrString(params.getSystem()));
    builder.add("cedType", IOUtil.nullOrString(params.getCedType()));
    builder.add("cedName", IOUtil.nullOrString(params.getCedName()));
    builder.add("exceptionType", IOUtil.nullOrString(params.getExceptionType()));
    builder.add("maxTypes", IOUtil.nullOrString(params.getMaxTypes()));
    builder.add("maxDevices", IOUtil.nullOrString(params.getMaxDevices()));
    builder.add("node", IOUtil.nullOrString(params.getNode()));
    builder.add("channel", IOUtil.nullOrString(params.getChannel()));
    builder.add("qualified", "");

    String url = ServletUtil.getCurrentUrlAdvanced(request, builder.getParams());

    response.sendRedirect(response.encodeRedirectURL(url));
  }
}
