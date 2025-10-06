package org.jlab.dtm.business.session;

import jakarta.annotation.security.PermitAll;
import jakarta.ejb.Stateless;
import jakarta.servlet.ServletOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jlab.dtm.business.params.FsdSummaryReportParams;
import org.jlab.dtm.business.service.FsdTripService;
import org.jlab.dtm.business.service.FsdTripTrendService;
import org.jlab.dtm.business.service.FsdTripTrendService.TripHistogramBin;
import org.jlab.dtm.persistence.filter.FsdTripFilter;
import org.jlab.dtm.persistence.model.FsdDevice;
import org.jlab.dtm.persistence.model.FsdFault;
import org.jlab.dtm.persistence.model.FsdTrip;
import org.jlab.dtm.persistence.model.JoinedFsdTrip;
import org.jlab.smoothness.business.util.TimeUtil;

/**
 * @author ryans
 */
@Stateless
public class ExcelTripsService {

  @PermitAll
  public void exportJoined(OutputStream out, List<JoinedFsdTrip> tripList, String selectionMessage)
      throws IOException {
    Workbook wb = new XSSFWorkbook();
    Sheet sheet1 = wb.createSheet("FSD Trips");

    int rownum = 0;
    Row row0 = sheet1.createRow(rownum++);

    sheet1.createRow(rownum++); // Spacer row

    Row row = sheet1.createRow(rownum++);
    row.createCell(0).setCellValue("FSD_TRIP_ID");
    row.createCell(1).setCellValue("TIME_DOWN");
    row.createCell(2).setCellValue("TIME_UP");
    row.createCell(3).setCellValue("DURATION_SECONDS");
    row.createCell(4).setCellValue("ACC_STATE");
    row.createCell(5).setCellValue("HLA_STATE");
    row.createCell(6).setCellValue("HLB_STATE");
    row.createCell(7).setCellValue("HLC_STATE");
    row.createCell(8).setCellValue("HLD_STATE");
    row.createCell(9).setCellValue("CAUSE");
    row.createCell(10).setCellValue("NODE");
    row.createCell(11).setCellValue("CHANNEL");
    row.createCell(12).setCellValue("HCO_CATEGORY_NAME");
    row.createCell(13).setCellValue("HCO_SYSTEM_NAME");
    row.createCell(14).setCellValue("CED_TYPE");
    row.createCell(15).setCellValue("CED_NAME");
    row.createCell(16).setCellValue("FAULT_CONFIRMATION_YN");

    CreationHelper createHelper = wb.getCreationHelper();
    CellStyle integerStyle = wb.createCellStyle();
    integerStyle.setDataFormat(createHelper.createDataFormat().getFormat("###,##0"));
    CellStyle dateStyle = wb.createCellStyle();
    dateStyle.setDataFormat(createHelper.createDataFormat().getFormat("dd-MMM-yyyy HH:mm:ss"));

    Cell c;

    for (JoinedFsdTrip trip : tripList) {
      row = sheet1.createRow(rownum++);

      row.createCell(0).setCellValue(trip.getFsdTripId().longValue());
      c = row.createCell(1);
      c.setCellStyle(dateStyle);
      c.setCellValue(trip.getStart());
      c = row.createCell(2);
      c.setCellStyle(dateStyle);
      if (trip.getEnd() != null) {
        c.setCellValue(trip.getEnd());
      }
      c = row.createCell(3);
      c.setCellStyle(integerStyle);
      c.setCellValue(trip.getElapsedMillis() / 1000);
      c = row.createCell(4);
      if (trip.getAccState() != null) {
        c.setCellValue(trip.getAccState().getLabel());
      }
      c = row.createCell(5);
      if (trip.getHallAState() != null) {
        c.setCellValue(trip.getHallAState().getLabel());
      }
      c = row.createCell(6);
      if (trip.getHallBState() != null) {
        c.setCellValue(trip.getHallBState().getLabel());
      }
      c = row.createCell(7);
      if (trip.getHallCState() != null) {
        c.setCellValue(trip.getHallCState().getLabel());
      }
      c = row.createCell(8);
      if (trip.getHallDState() != null) {
        c.setCellValue(trip.getHallDState().getLabel());
      }
      c = row.createCell(9);
      if (trip.getRootCause() != null) {
        c.setCellValue(trip.getRootCause());
      }
      c = row.createCell(10);
      if (trip.getNode() != null) {
        c.setCellValue(trip.getNode());
      }
      c = row.createCell(11);
      c.setCellStyle(integerStyle);
      if (trip.getChannel() != null) {
        c.setCellValue(trip.getChannel());
      }
      c = row.createCell(12);
      if (trip.getCategory() != null) {
        c.setCellValue(trip.getCategory());
      }
      c = row.createCell(13);
      if (trip.getSystem() != null) {
        c.setCellValue(trip.getSystem());
      }
      c = row.createCell(14);
      if (trip.getCedType() != null) {
        c.setCellValue(trip.getCedType());
      }
      c = row.createCell(15);
      if (trip.getCedName() != null) {
        c.setCellValue(trip.getCedName());
      }
      c = row.createCell(16);
      if (trip.isConfirmed() != null) {
        c.setCellValue(trip.isConfirmed() ? "Y" : "N");
      }
    }

    sheet1.autoSizeColumn(0);
    sheet1.autoSizeColumn(1);
    sheet1.autoSizeColumn(2);
    sheet1.autoSizeColumn(3);
    sheet1.autoSizeColumn(4);
    sheet1.autoSizeColumn(5);
    sheet1.autoSizeColumn(6);
    sheet1.autoSizeColumn(7);
    sheet1.autoSizeColumn(8);
    sheet1.autoSizeColumn(9);
    sheet1.autoSizeColumn(10);
    sheet1.autoSizeColumn(11);
    sheet1.autoSizeColumn(12);
    sheet1.autoSizeColumn(13);
    sheet1.autoSizeColumn(14);
    sheet1.autoSizeColumn(15);
    sheet1.autoSizeColumn(16);

    SimpleDateFormat formatter = new SimpleDateFormat(TimeUtil.getFriendlyDateTimePattern());
    row0.createCell(0)
        .setCellValue(
            "JOINED EXPORT (Duplicate FSD_TRIP_ID possible - one for each device): [generated "
                + formatter.format(new Date())
                + "]: "
                + selectionMessage);

    wb.write(out);
  }

  @PermitAll
  public void exportMixed(OutputStream out, List<FsdTrip> tripList, String selectionMessage)
      throws IOException {
    Workbook wb = new XSSFWorkbook();
    Sheet sheet1 = wb.createSheet("FSD Trips");

    int rownum = 0;
    Row row0 = sheet1.createRow(rownum++);

    sheet1.createRow(rownum++); // Spacer row

    Row row = sheet1.createRow(rownum++);
    row.createCell(0).setCellValue("FSD_TRIP_ID");
    row.createCell(1).setCellValue("TIME_DOWN");
    row.createCell(2).setCellValue("TIME_UP");
    row.createCell(3).setCellValue("DURATION_SECONDS");
    row.createCell(4).setCellValue("ACC_STATE");
    row.createCell(5).setCellValue("HLA_STATE");
    row.createCell(6).setCellValue("HLB_STATE");
    row.createCell(7).setCellValue("HLC_STATE");
    row.createCell(8).setCellValue("HLD_STATE");
    row.createCell(9).setCellValue("CAUSE");
    row.createCell(10).setCellValue("NODE CSV");
    row.createCell(11).setCellValue("CHANNEL CSV");
    row.createCell(12).setCellValue("HCO_CATEGORY_NAME CSV");
    row.createCell(13).setCellValue("HCO_SYSTEM_NAME CSV");
    row.createCell(14).setCellValue("CED_TYPE CSV");
    row.createCell(15).setCellValue("CED_NAME CSV");
    // row.createCell(16).setCellValue("FAULT_CONFIRMATION_YN CSV");

    sheet1.autoSizeColumn(10);
    sheet1.autoSizeColumn(11);
    sheet1.autoSizeColumn(12);
    sheet1.autoSizeColumn(13);
    sheet1.autoSizeColumn(14);
    sheet1.autoSizeColumn(15);
    // sheet1.autoSizeColumn(16);

    CreationHelper createHelper = wb.getCreationHelper();
    CellStyle integerStyle = wb.createCellStyle();
    integerStyle.setDataFormat(createHelper.createDataFormat().getFormat("###,##0"));
    CellStyle dateStyle = wb.createCellStyle();
    dateStyle.setDataFormat(createHelper.createDataFormat().getFormat("dd-MMM-yyyy HH:mm:ss"));

    Cell c;

    for (FsdTrip trip : tripList) {
      row = sheet1.createRow(rownum++);

      row.createCell(0).setCellValue(trip.getFsdTripId().longValue());
      c = row.createCell(1);
      c.setCellStyle(dateStyle);
      c.setCellValue(trip.getStart());
      c = row.createCell(2);
      c.setCellStyle(dateStyle);
      if (trip.getEnd() != null) {
        c.setCellValue(trip.getEnd());
      }
      c = row.createCell(3);
      c.setCellStyle(integerStyle);
      c.setCellValue(trip.getElapsedMillis() / 1000);
      c = row.createCell(4);
      if (trip.getAccState() != null) {
        c.setCellValue(trip.getAccState().getLabel());
      }
      c = row.createCell(5);
      if (trip.getHallAState() != null) {
        c.setCellValue(trip.getHallAState().getLabel());
      }
      c = row.createCell(6);
      if (trip.getHallBState() != null) {
        c.setCellValue(trip.getHallBState().getLabel());
      }
      c = row.createCell(7);
      if (trip.getHallCState() != null) {
        c.setCellValue(trip.getHallCState().getLabel());
      }
      c = row.createCell(8);
      if (trip.getHallDState() != null) {
        c.setCellValue(trip.getHallDState().getLabel());
      }
      c = row.createCell(9);
      if (trip.getRootCause() != null) {
        c.setCellValue(trip.getRootCause());
      }

      List<FsdFault> faultList = new ArrayList<>(trip.getFaultMap().values());
      List<FsdDevice> deviceList = new ArrayList<>();
      String nodeCsv = "";
      String channelCsv = "";
      if (!faultList.isEmpty()) {
        FsdFault fault = faultList.get(0);
        deviceList.addAll(fault.getDeviceMap().values());

        if (fault.getNode() != null) {
          nodeCsv = fault.getNode();
        } else {
          nodeCsv = "<none>";
        }
        if (fault.getChannel() != null) {
          channelCsv = fault.getChannel().toString();
        } else {
          channelCsv = "<none>";
        }
      }
      for (int i = 1; i < faultList.size(); i++) {
        FsdFault fault = faultList.get(i);
        deviceList.addAll(fault.getDeviceMap().values());

        if (fault.getNode() != null) {
          nodeCsv = nodeCsv + "," + fault.getNode();
        } else {
          nodeCsv = nodeCsv + "," + "<none>";
        }
        if (fault.getChannel() != null) {
          channelCsv = channelCsv + "," + fault.getChannel().toString();
        } else {
          channelCsv = channelCsv + "," + "<none>";
        }
      }

      c = row.createCell(10);
      c.setCellValue(nodeCsv);
      c = row.createCell(11);
      c.setCellStyle(integerStyle);
      c.setCellValue(channelCsv);

      String categoryCsv = "";
      String systemCsv = "";
      String cedTypeCsv = "";
      String cedNameCsv = "";
      if (!deviceList.isEmpty()) {
        FsdDevice device = deviceList.get(0);

        if (device.getCategory() != null) {
          categoryCsv = device.getCategory();
        } else {
          categoryCsv = "<none>";
        }
        if (device.getSystem() != null) {
          systemCsv = device.getSystem();
        } else {
          systemCsv = "<none>";
        }
        if (device.getCedType() != null) {
          cedTypeCsv = device.getCedType();
        } else {
          cedTypeCsv = "<none>";
        }
        if (device.getCedName() != null) {
          cedNameCsv = device.getCedName();
        } else {
          cedNameCsv = "<none>";
        }
      }
      for (int i = 1; i < deviceList.size(); i++) {
        FsdDevice device = deviceList.get(i);

        if (device.getCategory() != null) {
          categoryCsv = categoryCsv + "," + device.getCategory();
        } else {
          categoryCsv = categoryCsv + "," + "<none>";
        }
        if (device.getSystem() != null) {
          systemCsv = systemCsv + "," + device.getSystem();
        } else {
          systemCsv = systemCsv + "," + "<none>";
        }
        if (device.getCedType() != null) {
          cedTypeCsv = cedTypeCsv + "," + device.getCedType();
        } else {
          cedTypeCsv = cedTypeCsv + "," + "<none>";
        }
        if (device.getCedName() != null) {
          cedNameCsv = cedNameCsv + "," + device.getCedName();
        } else {
          cedNameCsv = cedNameCsv + "," + "<none>";
        }
      }

      c = row.createCell(12);
      c.setCellValue(categoryCsv);
      c = row.createCell(13);
      c.setCellValue(systemCsv);
      c = row.createCell(14);
      c.setCellValue(cedTypeCsv);
      c = row.createCell(15);
      c.setCellValue(cedNameCsv);
    }

    sheet1.autoSizeColumn(0);
    sheet1.autoSizeColumn(1);
    sheet1.autoSizeColumn(2);
    sheet1.autoSizeColumn(3);
    sheet1.autoSizeColumn(4);
    sheet1.autoSizeColumn(5);
    sheet1.autoSizeColumn(6);
    sheet1.autoSizeColumn(7);
    sheet1.autoSizeColumn(8);
    sheet1.autoSizeColumn(9);

    SimpleDateFormat formatter = new SimpleDateFormat(TimeUtil.getFriendlyDateTimePattern());
    row0.createCell(0)
        .setCellValue(
            "MIXED EXPORT (unique FSD_TRIP_ID - CSV used to aggregate related lists) [generated "
                + formatter.format(new Date())
                + "]: "
                + selectionMessage);

    wb.write(out);
  }

  @PermitAll
  public void exportAsCsv(OutputStream out, FsdTripFilter filter, String message, boolean aggregate)
      throws SQLException {

    FsdTripService tripService = new FsdTripService();

    tripService.streamTripCsv(out, filter, message, aggregate);
  }

  @PermitAll
  public void exportSummaryAsCsv(ServletOutputStream out, FsdSummaryReportParams params)
      throws SQLException {
    FsdTripTrendService tripService = new FsdTripTrendService();

    List<TripHistogramBin> recordList = tripService.findTrendListByPeriodInMemory(params);

    SimpleDateFormat dateFormatter = new SimpleDateFormat("dd-MMM-yyyy HH");
    DecimalFormat numberFormatter = new DecimalFormat("###,##0.0000");

    try (PrintWriter writer = new PrintWriter(out)) {

      writer.print("Date");
      writer.print(",");
      writer.print("Number of Trips");
      writer.print(",");
      writer.print("Duration (Hours)");
      writer.print(",");
      writer.print("Grouping");
      writer.println();

      for (TripHistogramBin bin : recordList) {
        writer.print(dateFormatter.format(bin.getStart()));
        writer.print(",");
        writer.print(bin.getCount());
        writer.print(",");
        writer.print(numberFormatter.format(bin.getDurationMillis() / 3.6e+6));
        writer.print(",");
        writer.print(bin.getGrouping() == null ? "" : bin.getGrouping());
        writer.println();
      }
    }
  }
}
