package org.jlab.dtm.business.session;

import jakarta.annotation.security.PermitAll;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import jakarta.servlet.ServletOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import org.jlab.dtm.business.params.JouleReportParams;
import org.jlab.dtm.business.util.DtmDateIterator;
import org.jlab.dtm.business.util.DtmTimeUtil;
import org.jlab.dtm.persistence.entity.EventType;
import org.jlab.dtm.persistence.model.BeamSummaryTotals;
import org.jlab.smoothness.business.service.SettingsService;

@Stateless
public class JouleReportFacade {

  @EJB CcAccHourService accHourService;

  @EJB EventDowntimeFacade eventDowntimeFacade;

  @PermitAll
  public List<JouleRecord> find(JouleReportParams params) throws IOException, InterruptedException {
    List<JouleRecord> recordList = new ArrayList<>();

    Float maintenance = params.getMaintenance();
    Float quality = params.getQuality();
    Float scaler = params.getScaler();
    String type = params.getType();
    String size = params.getSize();

    /*System.err.println("Start: " + params.getStart());
    System.err.println("End: " + params.getEnd());
    System.err.println("Size: " + size);*/

    if ("none".equals(size)) {
      JouleRecord record =
          createRecord(params.getStart(), params.getEnd(), maintenance, quality, scaler);

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

      if (maintenance != null && bins.size() > 0) {
        maintenance = maintenance / bins.size();
      }

      if (quality != null && bins.size() > 0) {
        quality = quality / bins.size();
      }

      for (Date bin : bins) {
        /*System.err.println("Bin: " + bin);*/

        Date start = bin;
        Date end = DtmTimeUtil.add(bin, field, amount);

        if (end.getTime() > params.getEnd().getTime()) {
          /*System.out.println("Bin End over range, setting to range end");*/
          end = params.getEnd();
        }

        JouleRecord record = createRecord(start, end, maintenance, quality, scaler);

        recordList.add(record);
      }
    }

    return recordList;
  }

  private JouleRecord createRecord(
      Date start, Date end, Float maintenance, Float quality, Float scaler)
      throws IOException, InterruptedException {
    /*System.err.println("Bin start: " + start);
    System.err.println("Bin end: " + end);*/

    JouleRecord record = new JouleRecord();

    record.setBin(start);

    double downtimeHours = eventDowntimeFacade.downtimeTotal(start, end, EventType.BLOCKED);

    double tuningHours = eventDowntimeFacade.downtimeTotal(start, end, EventType.TUNING);

    BeamSummaryTotals beamSummary = accHourService.reportTotals(start, end);

    // double programHours = beamSummary.calculateProgramSeconds() / 3600;

    double physicsHours = beamSummary.getUpSeconds() / 3600.0;
    double internalDownHours = beamSummary.getDownSeconds() / 3600.0;

    // System.out.println("EventDownHours: " + downtimeHours);

    // System.out.println("RAW internalDownHours: " + internalDownHours);

    if (downtimeHours < internalDownHours) {
      internalDownHours = downtimeHours;
    }

    // System.out.println("internalDownHours: " + internalDownHours);

    double physicsDownHours = downtimeHours - internalDownHours;

    // System.out.println("RAW physicsDownHours: " + physicsDownHours);

    if (physicsHours < physicsDownHours) {
      physicsDownHours = physicsHours;
    }

    // System.out.println("physicsDownHours: " + physicsDownHours);

    double deliveredResearchHours = physicsHours - physicsDownHours;

    if (quality != null) {
      deliveredResearchHours = deliveredResearchHours + quality;

      if (deliveredResearchHours < 0) {
        deliveredResearchHours = 0;
      }
    }

    double deliveredBeamStudiesHours = beamSummary.getStudiesSeconds() / 3600.0;

    double deliveredTuningAndRestoreHours =
        tuningHours + ((beamSummary.getRestoreSeconds() + beamSummary.getAccSeconds()) / 3600.0);

    double totalDeliveredHours =
        deliveredResearchHours + deliveredBeamStudiesHours + deliveredTuningAndRestoreHours;

    double unscheduledFailures = downtimeHours;

    if (maintenance != null) {
      unscheduledFailures = unscheduledFailures - maintenance;

      if (unscheduledFailures < 0) {
        unscheduledFailures = 0;
      }
    }

    // long begin = System.currentTimeMillis();
    Double budgetedOperationsHours = fetchBudgetedHours(start, end);
    // long stop = System.currentTimeMillis();

    // System.err.println("Fetch Time (Seconds): " + (stop - begin) / 1000.0);

    if (scaler != null) {
      budgetedOperationsHours = budgetedOperationsHours * scaler;
    }

    Double budgetedAvailability = null;

    if (budgetedOperationsHours != null && budgetedOperationsHours > 0) {
      budgetedAvailability = totalDeliveredHours / budgetedOperationsHours * 100;
    }

    double scheduledHours = totalDeliveredHours + unscheduledFailures;
    Double researchAvailability = null;
    Double reliability = null;

    if (scheduledHours > 0) {
      researchAvailability = deliveredResearchHours / scheduledHours * 100;

      reliability = totalDeliveredHours / scheduledHours * 100;
    }

    record.setDeliveredResearchHours(deliveredResearchHours);
    record.setDeliveredBeamStudiesHours(deliveredBeamStudiesHours);
    record.setDeliveredTuningAndRestoreHours(deliveredTuningAndRestoreHours);
    record.setTotalDeliveredHours(totalDeliveredHours);
    record.setBudgetedOperationsHours(budgetedOperationsHours);
    record.setBudgetedAvailability(budgetedAvailability);
    record.setUnscheduledFailuresHours(unscheduledFailures);
    record.setTotalScheduledHours(scheduledHours);
    record.setResearchAvailability(researchAvailability);
    record.setActualAvailability(reliability);

    return record;
  }

  @PermitAll
  public Double fetchBudgetedHours(Date start, Date end) throws IOException, InterruptedException {

    String startStr = DtmTimeUtil.toISO8601Date(start);
    String endStr = DtmTimeUtil.toISO8601Date(end);

    if (startStr == null || endStr == null) {
      throw new IOException("start and end dates are invalid");
    }

    String NPES_SCHEDULE_URL = SettingsService.cachedSettings.get("NPES_SCHEDULE_URL");
    boolean NPES_SCHEDULE_ENABLED = SettingsService.cachedSettings.is("NPES_SCHEDULE_ENABLED");

    int count = 0;

    if (NPES_SCHEDULE_ENABLED) {
      HttpRequest request =
          HttpRequest.newBuilder()
              .GET()
              .uri(
                  URI.create(
                      NPES_SCHEDULE_URL
                          + "/rest/scheduled-count?start="
                          + startStr
                          + "&end="
                          + endStr))
              .timeout(Duration.ofSeconds(7))
              .build();

      HttpClient httpClient = HttpClient.newBuilder().build();

      HttpResponse<String> response =
          httpClient.send(request, HttpResponse.BodyHandlers.ofString());

      if (response.statusCode() != 200) {
        throw new IOException("Unable to obtain NPES schedule (budgeted): " + response.body());
      }

      JsonReader reader = Json.createReader(new StringReader(response.body()));
      JsonObject obj = reader.readObject();

      count = obj.getInt("count");
    }

    return (count * 24.0);
  }

  @PermitAll
  public void exportAsCsv(
      ServletOutputStream out, List<JouleRecord> recordList, String selectionMessage) {
    try (PrintWriter writer = new PrintWriter(out)) {
      writer.println(
          "# Bin, Research, Studies, Tuning & Restore, Delivered, Budgeted, Delivered Ratio, Failures, Scheduled, Research Ratio, Reliability");

      SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
      DecimalFormat decimalFormat =
          new DecimalFormat("#####0.0"); /*No commas in format because we are in CSV!*/

      for (JouleRecord record : recordList) {
        writer.print(dateFormatter.format(record.getBin()));
        writer.print(",");
        writer.print(decimalFormat.format(record.getDeliveredResearchHours()));
        writer.print(",");
        writer.print(decimalFormat.format(record.getDeliveredBeamStudiesHours()));
        writer.print(",");
        writer.print(decimalFormat.format(record.getDeliveredTuningAndRestoreHours()));
        writer.print(",");
        writer.print(decimalFormat.format(record.getTotalDeliveredHours()));
        writer.print(",");
        writer.print(decimalFormat.format(record.getBudgetedOperationsHours()));
        writer.print(",");
        writer.print(formatDouble(record.getBudgetedAvailability(), decimalFormat));
        writer.print("%,");
        writer.print(decimalFormat.format(record.getUnscheduledFailuresHours()));
        writer.print(",");
        writer.print(decimalFormat.format(record.getTotalScheduledHours()));
        writer.print(",");
        writer.print(formatDouble(record.getResearchAvailability(), decimalFormat));
        writer.print("%,");
        writer.print(formatDouble(record.getActualAvailability(), decimalFormat));
        writer.print("%");
        writer.println();
      }
    }
  }

  private String formatDouble(Double value, DecimalFormat formatter) {
    if (value == null) {
      return "";
    } else {
      return formatter.format(value);
    }
  }

  public static class JouleRecord {
    private Date bin;
    private double deliveredResearchHours;
    private double deliveredBeamStudiesHours;
    private double deliveredTuningAndRestoreHours;
    private double totalDeliveredHours;
    private Double budgetedOperationsHours;
    private Double budgetedAvailability;
    private double unscheduledFailuresHours;
    private Double totalScheduledHours;
    private Double researchAvailability;
    private Double actualAvailability;

    public Date getBin() {
      return bin;
    }

    public void setBin(Date bin) {
      this.bin = bin;
    }

    public double getDeliveredResearchHours() {
      return deliveredResearchHours;
    }

    public void setDeliveredResearchHours(double deliveredResearchHours) {
      this.deliveredResearchHours = deliveredResearchHours;
    }

    public double getDeliveredBeamStudiesHours() {
      return deliveredBeamStudiesHours;
    }

    public void setDeliveredBeamStudiesHours(double deliveredBeamStudiesHours) {
      this.deliveredBeamStudiesHours = deliveredBeamStudiesHours;
    }

    public double getDeliveredTuningAndRestoreHours() {
      return deliveredTuningAndRestoreHours;
    }

    public void setDeliveredTuningAndRestoreHours(double deliveredTuningAndRestoreHours) {
      this.deliveredTuningAndRestoreHours = deliveredTuningAndRestoreHours;
    }

    public double getTotalDeliveredHours() {
      return totalDeliveredHours;
    }

    public void setTotalDeliveredHours(double totalDeliveredHours) {
      this.totalDeliveredHours = totalDeliveredHours;
    }

    public Double getBudgetedOperationsHours() {
      return budgetedOperationsHours;
    }

    public void setBudgetedOperationsHours(Double budgetedOperationsHours) {
      this.budgetedOperationsHours = budgetedOperationsHours;
    }

    public Double getBudgetedAvailability() {
      return budgetedAvailability;
    }

    public void setBudgetedAvailability(Double budgetedAvailability) {
      this.budgetedAvailability = budgetedAvailability;
    }

    public double getUnscheduledFailuresHours() {
      return unscheduledFailuresHours;
    }

    public void setUnscheduledFailuresHours(double unscheduledFailuresHours) {
      this.unscheduledFailuresHours = unscheduledFailuresHours;
    }

    public Double getTotalScheduledHours() {
      return totalScheduledHours;
    }

    public void setTotalScheduledHours(Double totalScheduledHours) {
      this.totalScheduledHours = totalScheduledHours;
    }

    public Double getResearchAvailability() {
      return researchAvailability;
    }

    public void setResearchAvailability(Double researchAvailability) {
      this.researchAvailability = researchAvailability;
    }

    public Double getActualAvailability() {
      return actualAvailability;
    }

    public void setActualAvailability(Double actualAvailability) {
      this.actualAvailability = actualAvailability;
    }
  }
}
