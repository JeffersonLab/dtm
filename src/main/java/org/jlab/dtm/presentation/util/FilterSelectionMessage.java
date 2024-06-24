package org.jlab.dtm.presentation.util;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import org.jlab.dtm.persistence.entity.Category;
import org.jlab.dtm.persistence.entity.EventType;
import org.jlab.dtm.persistence.entity.SystemEntity;
import org.jlab.dtm.persistence.enumeration.RootCause;
import org.jlab.smoothness.business.util.TimeUtil;

/**
 * @author ryans
 */
public final class FilterSelectionMessage {

  private FilterSelectionMessage() {
    // Private constructor
  }

  /*public static String getSummaryReportCaption(Date start, Date end, EventType type, double period) {

      DecimalFormat numberFormatter = new DecimalFormat("###,###");
      SimpleDateFormat dateFormatter = new SimpleDateFormat(TimeUtil.getFriendlyDateTimePattern());

      String message = type.getName() + " Downtime Summary in Hours for "
              + numberFormatter.format(period) + " Hour Period from "
              + dateFormatter.format(start) + " to " + dateFormatter.format(end);

      return message;
  }*/
  public static String getReportMessage(
      Date start,
      Date end,
      EventType type,
      SystemEntity system,
      Category category,
      String dateFormat,
      String component,
      Boolean beamTransport,
      boolean packed) {

    List<String> filters = new ArrayList<>();

    if (packed) {
      filters.add("Non-Overlapping \"Yes\"");
    }

    if (dateFormat == null) {
      dateFormat = TimeUtil.getFriendlyDateTimePattern();
    }

    SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);

    if (start != null) {
      filters.add("Start Date \"" + formatter.format(start) + "\"");
    }

    if (end != null) {
      filters.add("End Date \"" + formatter.format(end) + "\"");
    }

    if (type != null) {
      filters.add("Type \"" + type.getName() + "\"");
    }

    if (category != null) {
      filters.add("Category \"" + category.getName() + "\"");
    }

    if (system != null) {
      filters.add("System \"" + system.getName() + "\"");
    }

    if (component != null && !component.trim().isEmpty()) {
      filters.add("Component \"" + component + "\"");
    }

    if (beamTransport != null) {
      if (beamTransport) {
        filters.add("Beam Transport only");
      } else {
        filters.add("Beam Transport excluded");
      }
    }

    String message = "";

    if (!filters.isEmpty()) {
      for (String filter : filters) {
        message += " " + filter + " and";
      }

      // Remove trailing " and"
      message = message.substring(0, message.length() - 4);
    }

    return message;
  }

  public static String getDateRangeReportMessage(
      Date start,
      Date end,
      EventType type,
      SystemEntity system,
      Category category,
      String dateFormat,
      String component,
      Boolean beamTransport,
      String grouping,
      String data,
      boolean packed) {

    List<String> filters = new ArrayList<>();

    // if (dateFormat == null) {
    //    dateFormat = TimeUtil.getFriendlyDateTimePattern();
    // }
    // SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);
    String typeQualifier = "";

    if (type != null) {
      typeQualifier = type.getName() + " ";
    }

    String packedQualifier = "";

    if (packed) {
      packedQualifier = "Non-Overlapping ";
    }

    String dataQualifier = "";

    if ("downtime".equals(data)) {
      dataQualifier = "Incident Downtime (Hours) ";
    } else if ("count".equals(data)) {
      dataQualifier = "Incident Count ";
    } else if ("mttr".equals(data)) {
      dataQualifier = "Incident Mean Time to Recover (Hours) ";
    } else if ("uptime".equals(data)) {
      dataQualifier = "Incident Uptime (Hours) ";
    } else if ("mtbf".equals(data)) {
      dataQualifier = "Incident Mean Time between Failures (Hours) ";
    } else if ("failure".equals(data)) {
      dataQualifier = "Hourly Failure Rate ";
    } else if ("availability".equals(data)) {
      dataQualifier = "Availability ";
    } else if ("restore".equals(data)) {
      dataQualifier = "Restore (Hours) ";
    } else if ("incident".equals(data)) {
      dataQualifier = "Incidents ";
    }

    String categoryQualifier = "";

    if (category != null) {
      categoryQualifier = "for " + category.getName();

      categoryQualifier = categoryQualifier + " ";
    }

    String systemQualifier = "";

    if (system != null) {
      systemQualifier = "for " + system.getName();

      systemQualifier = systemQualifier + " ";
    }

    if (component != null && !component.trim().isEmpty()) {
      filters.add("component matches \"" + component + "\"");
    }

    String transportQualifier = "";

    if (beamTransport != null) {
      if (beamTransport) {
        transportQualifier = "Beam Transport only ";
      } else {
        transportQualifier = "Beam Transport excluded ";
      }
    }

    if (packed
        && "Category"
            .equals(grouping)) { // In Category reports - Can't filter by transport if packed
      transportQualifier = "";
    }

    String groupingQualifier = "";

    // if(grouping != null) {
    //    groupingQualifier = " by " + grouping + " ";
    // }
    String timeQualifier = "";

    if (start != null && end != null) {
      timeQualifier = "from " + TimeUtil.formatSmartRangeSeparateTime(start, end) + " ";
    } else if (start != null) {
      timeQualifier = "starting " + TimeUtil.formatSmartSingleTime(start) + " ";
    } else if (end != null) {
      timeQualifier = "before " + TimeUtil.formatSmartSingleTime(end) + " ";
    }

    String message =
        typeQualifier
            + packedQualifier
            + dataQualifier
            + groupingQualifier
            + timeQualifier
            + categoryQualifier
            + systemQualifier
            + transportQualifier;

    if (!filters.isEmpty()) {
      message = message + " where";
      for (String filter : filters) {
        message += " " + filter + " and";
      }

      // Remove trailing " and"
      message = message.substring(0, message.length() - 4);

      message = message + " "; // We have trailing space in case of append
    }

    return message;
  }

  public static String getIncidentTrendReportMessage(
      Date start,
      Date end,
      EventType type,
      boolean includeBeamTransport,
      String data,
      String grouping,
      int interval,
      List<String> categoryNameList) {

    // List<String> filters = new ArrayList<String>();
    SimpleDateFormat formatter;

    if (start.equals(TimeUtil.startOfDay(start, Calendar.getInstance()))
        && end.equals(TimeUtil.startOfDay(end, Calendar.getInstance(Locale.ENGLISH)))) {
      formatter = new SimpleDateFormat("dd-MMM-yyyy");
    } else {
      formatter = new SimpleDateFormat(TimeUtil.getFriendlyDateTimePattern());
    }

    String typeQualifier = "";

    if (type != null) {
      typeQualifier = type.getName() + " ";
    }

    String transportQualifier = "";

    if (!includeBeamTransport) {
      transportQualifier = "(Beam Transport excluded) ";
    }

    String dataQualifier = "Downtime ";

    if ("count".equals(data)) {
      dataQualifier = "Count ";
    } else if ("mttr".equals(data)) {
      dataQualifier = "Mean Time to Recover ";
    }

    String groupingQualifier = "";

    if ("category".equals(grouping)) {
      groupingQualifier = " by Category ";
    }

    String intervalQualifier = interval + "-Hour ";

    if (interval == 1) {
      intervalQualifier = "Hourly ";
    } else if (interval == 24) {
      intervalQualifier = "Daily ";
    } else if (interval == 168) {
      intervalQualifier = "Weekly ";
    }

    // String selectionMessage = dataStr + " by System ";
    String categoryQualifier = "";

    if (categoryNameList != null && !categoryNameList.isEmpty()) {
      categoryQualifier = "for " + categoryNameList.get(0);

      for (int i = 1; i < categoryNameList.size(); i++) {
        categoryQualifier = categoryQualifier + ", " + categoryNameList.get(i);
      }

      categoryQualifier = categoryQualifier + " ";
    }

    String message =
        intervalQualifier
            + typeQualifier
            + "Incident "
            + dataQualifier
            + groupingQualifier
            + "from "
            + formatter.format(start)
            + " to "
            + formatter.format(end)
            + " "
            + categoryQualifier
            + transportQualifier;

    /*if (!filters.isEmpty()) {
        message = message + " where";
        for (String filter : filters) {
            message += " " + filter + " and";
        }

        // Remove trailing " and"
        message = message.substring(0, message.length() - 4);
    }*/
    return message;
  }

  public static String getFsdTrendReportMessage(
      Date start,
      Date end,
      String data,
      String grouping,
      int interval,
      List<String> categoryNameList) {

    SimpleDateFormat formatter;

    if (start.equals(TimeUtil.startOfDay(start, Calendar.getInstance()))
        && end.equals(TimeUtil.startOfDay(end, Calendar.getInstance()))) {
      formatter = new SimpleDateFormat("dd-MMM-yyyy");
    } else {
      formatter = new SimpleDateFormat(TimeUtil.getFriendlyDateTimePattern());
    }

    String dataQualifier = "Downtime ";

    if ("count".equals(data)) {
      dataQualifier = "Count ";
    } else if ("mttr".equals(data)) {
      dataQualifier = "Mean Time to Recover ";
    }

    String groupingQualifier = "";

    String intervalQualifier = interval + "-Hour ";

    if (interval == 1) {
      intervalQualifier = "Hourly ";
    } else if (interval == 24) {
      intervalQualifier = "Daily ";
    } else if (interval == 168) {
      intervalQualifier = "Weekly ";
    }

    if ("category".equals(grouping)) {
      groupingQualifier = " by Category ";
    }

    String categoryQualifier = "";

    if (categoryNameList != null && !categoryNameList.isEmpty()) {
      categoryQualifier = "for " + categoryNameList.get(0);

      for (int i = 1; i < categoryNameList.size(); i++) {
        categoryQualifier = categoryQualifier + ", " + categoryNameList.get(i);
      }

      categoryQualifier = categoryQualifier + " ";
    }

    String message =
        intervalQualifier
            + "Faulted FSD Device "
            + dataQualifier
            + groupingQualifier
            + "from "
            + formatter.format(start)
            + " to "
            + formatter.format(end)
            + " "
            + categoryQualifier;

    return message;
  }

  public static List<String> getFsdTripReportFootnotes(
      Integer maxDuration,
      String maxDurationUnits,
      Integer maxTypes,
      boolean programBasis,
      double programHours,
      double periodHours,
      Boolean sadTrips,
      RootCause[] causeArray) {
    ArrayList<String> footnoteList = new ArrayList<>();

    if (maxDuration != null && maxDurationUnits != null) {
      String maxDurationQualifier = "Max Trip Duration: " + maxDuration + " " + maxDurationUnits;
      footnoteList.add(maxDurationQualifier);
    }

    if (maxTypes != null) {
      String maxTypesQualifier = "Max CED Types Per Trip: " + maxTypes;
      footnoteList.add(maxTypesQualifier);
    }

    if (programBasis) {
      String rateBasis = "Rate from Program (" + programHours + " hrs)";
      footnoteList.add(rateBasis);
    } else {
      String rateBasis = "Rate from Period (" + periodHours + " hrs)";
      footnoteList.add(rateBasis);
    }

    if (sadTrips != null && !sadTrips) {
      String excludeSadTrips = "SAD Trips excluded";
      footnoteList.add(excludeSadTrips);
    }

    if (causeArray != null && causeArray.length > 0) {
      String sublist = "\"" + causeArray[0].getLabel() + "\"";

      for (int i = 1; i < causeArray.length; i++) {
        String cause = causeArray[i].getLabel();
        sublist = sublist + ", \"" + cause + "\"";
      }

      footnoteList.add("Cause " + sublist);
    }

    return footnoteList;
  }

  public static String getFsdTripTrendReportMessage(
      Date start,
      Date end,
      Integer maxDuration,
      String maxDurationUnits,
      Integer maxTypes,
      String grouping,
      int interval,
      List<String> categoryNameList) {

    /*SimpleDateFormat formatter;

    if(start.equals(TimeUtil.startOfDay(start)) && end.equals(TimeUtil.startOfDay(end))) {
        formatter = new SimpleDateFormat("dd-MMM-yyyy");
    } else {
        formatter = new SimpleDateFormat(TimeUtil.getFriendlyDateTimePattern());
    }    */
    String dateRangeQualifier = TimeUtil.formatSmartRangeSeparateTime(start, end);

    String dataQualifier = "Count ";

    String groupingQualifier = "";

    String intervalQualifier = interval + "-Hour ";

    if (interval == 1) {
      intervalQualifier = "Hourly ";
    } else if (interval == 24) {
      intervalQualifier = "Daily ";
    } else if (interval == 168) {
      intervalQualifier = "Weekly ";
    }

    if ("category".equals(grouping)) {
      groupingQualifier = " by Category ";
    }

    String categoryQualifier = "";

    if (categoryNameList != null && !categoryNameList.isEmpty()) {
      categoryQualifier = "for " + categoryNameList.get(0);

      for (int i = 1; i < categoryNameList.size(); i++) {
        categoryQualifier = categoryQualifier + ", " + categoryNameList.get(i);
      }

      categoryQualifier = categoryQualifier + " ";
    }

    String maxDurationQualifier = "";

    if (maxDuration != null && maxDurationUnits != null) {
      maxDurationQualifier = "Max Trip Duration " + maxDuration + " " + maxDurationUnits;

      if (maxTypes != null) {
        maxDurationQualifier = maxDurationQualifier + ", ";
      }
    }

    String maxTypesQualifier = "";

    if (maxTypes != null) {
      maxTypesQualifier = "Max Types Per Trip " + maxTypes + " ";
    }

    String message =
        dateRangeQualifier + " " + categoryQualifier + maxDurationQualifier + maxTypesQualifier;

    return message;
  }

  public static String getTimelineMessage(Date start, Date end) {

    SimpleDateFormat formatter = new SimpleDateFormat(TimeUtil.getFriendlyDateTimePattern());

    return "from " + formatter.format(start) + " to " + formatter.format(end);
  }
}
