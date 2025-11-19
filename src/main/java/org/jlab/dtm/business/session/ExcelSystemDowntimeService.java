package org.jlab.dtm.business.session;

import jakarta.annotation.security.PermitAll;
import jakarta.ejb.Stateless;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.List;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jlab.dtm.persistence.entity.EventType;
import org.jlab.dtm.persistence.model.SystemDowntime;
import org.jlab.smoothness.business.util.TimeUtil;

/**
 * @author ryans
 */
@Stateless
public class ExcelSystemDowntimeService {

  @PermitAll
  public void export(
      OutputStream out,
      List<SystemDowntime> downtimeList,
      String filters,
      double periodDurationHours,
      double grandTotalDuration,
      List<EventType> selectedTypeList,
      double programHours)
      throws IOException {
    Workbook wb = new XSSFWorkbook();
    Sheet sheet1 = wb.createSheet("DTM System Downtime");

    int rownum = 0;
    Row row0 = sheet1.createRow(rownum++);
    // row0.createCell(0).setCellValue("Bounded By: " + filters);

    Row row1 = sheet1.createRow(rownum++);
    row1.createCell(0).setCellValue("SYSTEM");
    row1.createCell(1).setCellValue("DOWNTIME (HOURS)");
    row1.createCell(2).setCellValue("NUMBER OF INCIDENTS");
    row1.createCell(3).setCellValue("MEAN TIME TO RECOVER (HOURS)");

    if (selectedTypeList != null
        && selectedTypeList.size() == 1
        && selectedTypeList.contains(EventType.BLOCKED)) {
      row1.createCell(4).setCellValue("UPTIME (HOURS)");
      row1.createCell(5).setCellValue("MTBF (HOURS)");
      row1.createCell(6).setCellValue("HOURLY FAILURE RATE");
      row1.createCell(7).setCellValue("AVAILABILITY");
      row1.createCell(8).setCellValue("LOSS");
    }

    CreationHelper createHelper = wb.getCreationHelper();
    CellStyle numberStyle = wb.createCellStyle();
    numberStyle.setDataFormat(createHelper.createDataFormat().getFormat("#,##0.0"));
    CellStyle dateStyle = wb.createCellStyle();
    dateStyle.setDataFormat(
        createHelper.createDataFormat().getFormat(TimeUtil.getFriendlyDateTimePattern()));

    Cell c;

    for (SystemDowntime downtime : downtimeList) {
      Row row = sheet1.createRow(rownum++);

      row.createCell(0).setCellValue(downtime.getSystemName());
      c = row.createCell(1);
      c.setCellStyle(numberStyle);
      c.setCellValue(downtime.getDuration() * 24);
      row.createCell(2).setCellValue(downtime.getIncidentCount());
      c = row.createCell(3);
      c.setCellStyle(numberStyle);
      c.setCellValue(downtime.getDuration() / downtime.getIncidentCount() * 24);

      if (selectedTypeList != null
          && selectedTypeList.size() == 1
          && selectedTypeList.contains(EventType.BLOCKED)) {

        double uptime = programHours - (downtime.getDuration() * 24);
        double mtbf = uptime / downtime.getIncidentCount();
        Double failureRate = uptime == 0 ? null : downtime.getIncidentCount() / uptime;
        double availability = programHours == 0 ? 0 : uptime / programHours * 100;
        double loss = 100 - availability;

        c = row.createCell(4);
        c.setCellStyle(numberStyle);
        c.setCellValue(uptime);

        c = row.createCell(5);
        c.setCellStyle(numberStyle);
        c.setCellValue(mtbf);

        c = row.createCell(6);
        c.setCellStyle(numberStyle);
        if (failureRate != null) {
          c.setCellValue(failureRate);
        }

        c = row.createCell(7);
        c.setCellStyle(numberStyle);
        c.setCellValue(availability);

        c = row.createCell(8);
        c.setCellStyle(numberStyle);
        c.setCellValue(loss);
      }
    }

    sheet1.autoSizeColumn(0);

    row0.createCell(0)
        .setCellValue("Bounded By: " + filters + " and program hours: " + programHours);

    sheet1.autoSizeColumn(1);
    sheet1.autoSizeColumn(2);
    sheet1.autoSizeColumn(3);

    if (selectedTypeList != null
        && selectedTypeList.size() == 1
        && selectedTypeList.contains(EventType.BLOCKED)) {
      sheet1.autoSizeColumn(4);
      sheet1.autoSizeColumn(5);
      sheet1.autoSizeColumn(6);
      sheet1.autoSizeColumn(7);
      sheet1.autoSizeColumn(8);
    }

    wb.write(out);
  }

  @PermitAll
  public void exportAsCsv(
      OutputStream out,
      List<SystemDowntime> downtimeList,
      String trim,
      double periodDurationHours,
      double grandTotalDuration) {

    DecimalFormat formatter = new DecimalFormat("#,##0.0");

    try (PrintWriter writer = new PrintWriter(out)) {

      for (SystemDowntime downtime : downtimeList) {
        writer.print(downtime.getSystemName());
        writer.print(",");
        writer.print(formatter.format(downtime.getDuration() * 24));
        writer.print(",");
        writer.print(downtime.getIncidentCount());
        writer.print(",");
        writer.print(formatter.format(downtime.getDuration() / downtime.getIncidentCount() * 24));
        writer.println();
      }
    }
  }
}
