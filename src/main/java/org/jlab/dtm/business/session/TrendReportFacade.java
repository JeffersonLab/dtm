package org.jlab.dtm.business.session;

import java.math.BigInteger;
import java.sql.SQLException;
import java.util.*;
import javax.annotation.security.PermitAll;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import org.jlab.dtm.business.params.MultiTrendReportParams;
import org.jlab.dtm.business.params.TrendReportParams;
import org.jlab.dtm.business.params.TripParams;
import org.jlab.dtm.business.service.FsdTripService;
import org.jlab.dtm.business.util.DtmDateIterator;
import org.jlab.dtm.business.util.DtmTimeUtil;
import org.jlab.dtm.persistence.entity.Category;
import org.jlab.dtm.persistence.entity.EventType;
import org.jlab.dtm.persistence.enumeration.AccMachineState;
import org.jlab.dtm.persistence.filter.FsdTripFilter;
import org.jlab.dtm.persistence.model.BeamSummaryTotals;
import org.jlab.dtm.persistence.model.CategoryDowntime;
import org.jlab.dtm.persistence.model.EventDowntime;
import org.jlab.dtm.persistence.model.TrendRecord;
import org.jlab.smoothness.business.exception.UserFriendlyException;

@Stateless
public class TrendReportFacade {

  @EJB CategoryDowntimeFacade categoryDowntimeFacade;
  @EJB CategoryFacade categoryFacade;
  @EJB EventDowntimeFacade eventDowntimeFacade;
  @EJB CcAccHourService accHourService;

  @PermitAll
  public List<TrendRecord> find(TrendReportParams params)
      throws SQLException, UserFriendlyException {
    List<TrendRecord> recordList = new ArrayList<>();

    String size = params.getSize();

    EventType eventType = EventType.ACC;

    boolean includeCategories =
        params.getIncludeCategories() != null && params.getIncludeCategories();

    if ("none".equals(size)) {
      TrendRecord record = load(params.getStart(), params.getEnd(), eventType, includeCategories);

      recordList.add(record);
    } else {

      int field = Calendar.DATE;
      int amount = 1;

      if (size != null) {
        switch (size) {
          case "week":
            field = Calendar.WEEK_OF_YEAR;
            break;
          case "month":
            field = Calendar.MONTH;
            break;
          case "quarter":
            field = Calendar.MONTH;
            amount = 3;
            break;
          case "year":
            field = Calendar.YEAR;
            break;
        }
      }

      DtmDateIterator iterator =
          new DtmDateIterator(params.getStart(), params.getEnd(), field, amount);
      List<Date> bins = new ArrayList<>();

      /*We create collection just so we can get number of bins*/
      while (iterator.hasNext()) {
        Date bin = iterator.next();

        bins.add(bin);
      }

      if (bins.size() > 12) {
        throw new UserFriendlyException(
            bins.size()
                + " bins requested, but number must be no more than 12, select larger bin size or smaller date range");
      }

      for (Date bin : bins) {
        /*System.err.println("Bin: " + bin);*/

        Date start = bin;
        Date end = DtmTimeUtil.add(bin, field, amount);

        if (end.getTime() > params.getEnd().getTime()) {
          /*System.out.println("Bin End over range, setting to range end");*/
          end = params.getEnd();
        }

        TrendRecord record = load(start, end, eventType, includeCategories);

        recordList.add(record);
      }
    }

    return recordList;
  }

  private void fillInTrendCategories(TrendRecord record, Date start, Date end, EventType type) {
    record.categoryDowntimeList =
        categoryDowntimeFacade.findByPeriodAndType(start, end, type, null, true, null);

    if (record.categoryDowntimeList != null) {
      for (CategoryDowntime cd : record.categoryDowntimeList) {
        record.downtimeMap.put(cd.getId(), cd);
      }
    }

    List<Category> alphaCatList = categoryFacade.findAlphaCategoryList();
    record.missingList = new ArrayList<>();
    for (Category alpha : alphaCatList) {
      if (record.downtimeMap.get(alpha.getCategoryId().longValue()) == null) {
        CategoryDowntime dt = new CategoryDowntime(alpha.getName(), alpha.getCategoryId(), 0, 0);
        record.missingList.add(dt);
        record.downtimeMap.put(alpha.getCategoryId().longValue(), dt);
      }
    }

    record.categoryDowntimeList.addAll(record.missingList);

    /*double grandTotalDuration = 0.0;
    for (int i = 0; i < record.categoryDowntimeList.size(); i++) {
        CategoryDowntime downtime = record.categoryDowntimeList.get(i);
        grandTotalDuration = grandTotalDuration + downtime.getDuration();
    }*/
  }

  @PermitAll
  public TrendRecord load(Date start, Date end, EventType type, boolean includeCategories)
      throws SQLException {
    TrendRecord record = new TrendRecord();

    record.bin = start;

    record.periodHours = (end.getTime() - start.getTime()) / 1000 / 60 / 60;

    BeamSummaryTotals beamSummary = accHourService.reportTotals(start, end);

    record.programHours = (beamSummary.calculateProgramSeconds() / 3600.0);

    // Machine Overall Event Downtime
    List<EventDowntime> eventList =
        eventDowntimeFacade.findByPeriodAndTypeSortByDuration(start, end, type, null);
    record.eventCount = eventList.size();

    record.eventHours = 0;
    for (int i = 0; i < eventList.size(); i++) {
      EventDowntime downtime = eventList.get(i);
      record.eventHours = record.eventHours + downtime.getDowntimeHoursBounded();
      // restore = restore + downtime.getRestoreHoursBounded();
    }

    record.downtimeMap = new HashMap<>();

    if (includeCategories) {
      fillInTrendCategories(record, start, end, type);
    }

    FsdTripService tripService = new FsdTripService();
    TripParams tripParams = new TripParams();
    tripParams.setStart(start);
    tripParams.setEnd(end);
    tripParams.setMaxDuration(BigInteger.valueOf(5L));
    tripParams.setMaxDurationUnits("Minutes");
    tripParams.setAccStateArray(
        new AccMachineState[] {
          AccMachineState.NULL,
          AccMachineState.DOWN,
          AccMachineState.ACC,
          AccMachineState.MD,
          AccMachineState.RESTORE
        });
    FsdTripFilter fsdFilter = new FsdTripFilter(tripParams);
    FsdTripService.FsdSummary fsdSummary = null;
    record.tripCount = 0;
    record.tripHours = 0;

    fsdSummary = tripService.filterSummary(fsdFilter);
    record.tripCount = fsdSummary.getCount();
    record.tripHours = fsdSummary.getHours();

    record.accDownHours = record.tripHours + record.eventHours;

    if (record.accDownHours > record.periodHours) {
      record.accDownHours = record.periodHours;
    }

    record.tripUptimeHours = record.programHours - fsdSummary.getHours();

    if (record.tripUptimeHours < 0) {
      record.tripUptimeHours = 0;
    }

    record.eventUptimeHours = record.programHours - record.eventHours;

    if (record.eventUptimeHours < 0) {
      record.eventUptimeHours = 0;
    }

    record.accUptimeHours = record.tripUptimeHours - record.eventHours;

    if (record.accUptimeHours < 0) {
      record.accUptimeHours = 0;
    }

    record.tripMttrHours = 0;
    record.mtbtHours = 0;
    if (record.tripCount > 0) {
      record.tripMttrHours = record.tripHours / record.tripCount;
      if (record.tripUptimeHours > 0) {
        record.mtbtHours = record.tripUptimeHours / record.tripCount;
      }
    }

    record.eventMttrHours = 0;
    record.eventMtbfHours = 0;
    if (record.eventCount > 0) {
      record.eventMttrHours = record.eventHours / record.eventCount;

      record.eventMtbfHours = record.eventUptimeHours / record.eventCount;
    }

    record.accAvailability = 0;
    if (record.programHours > 0) {
      record.accAvailability = (record.accUptimeHours / record.programHours) * 100;
    }

    record.eventAvailability = 0;
    if (record.programHours > 0) {
      record.eventAvailability = (record.eventUptimeHours / record.programHours) * 100;
    }

    record.tripAvailability = 0;
    if (record.programHours > 0) {
      record.tripAvailability = (record.tripUptimeHours / record.programHours) * 100;
    }

    return record;
  }

  @PermitAll
  public List<List<TrendRecord>> findMultiple(MultiTrendReportParams params)
      throws SQLException, UserFriendlyException {
    List<List<TrendRecord>> recordListList = new ArrayList<>();

    String[] labelArray = params.getLabelArray();
    Date[] startArray = params.getStartArray();
    Date[] endArray = params.getEndArray();

    if (labelArray != null && labelArray.length > 0) {

      if (startArray == null
          || endArray == null
          || startArray.length != labelArray.length
          || endArray.length != labelArray.length) {
        throw new RuntimeException("label, start, and end parameters must have same cardinality");
      }

      for (int i = 0; i < labelArray.length; i++) {

        TrendReportParams trendParams = new TrendReportParams();
        trendParams.setSize(params.getSize());

        trendParams.setStart(startArray[i]);
        trendParams.setEnd(endArray[i]);

        List<TrendRecord> recordList = find(trendParams);

        recordListList.add(recordList);
      }
    }

    return recordListList;
  }
}
