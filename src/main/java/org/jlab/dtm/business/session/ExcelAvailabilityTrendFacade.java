package org.jlab.dtm.business.session;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import javax.annotation.security.PermitAll;
import javax.ejb.Stateless;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jlab.dtm.persistence.entity.Category;
import org.jlab.dtm.persistence.model.CategoryDowntime;
import org.jlab.dtm.persistence.model.TrendRecord;

/**
 * @author ryans
 */
@Stateless
public class ExcelAvailabilityTrendFacade {

  @PermitAll
  public void export(
      OutputStream out, List<Category> alphaCatList, List<TrendRecord> records, String filters)
      throws IOException {
    Workbook wb = new XSSFWorkbook();
    Sheet sheet1 = wb.createSheet("Trend");

    int rownum = 0;
    Row row0 = sheet1.createRow(rownum++);
    row0.createCell(0).setCellValue(filters);

    Row row1 = sheet1.createRow(rownum++);
    row1.createCell(0).setCellValue("Bin");
    row1.createCell(1).setCellValue("Overall Downtime (Hours)");
    row1.createCell(2).setCellValue("Overall Uptime (Hours)");
    row1.createCell(3).setCellValue("Overall Availability (%)");
    row1.createCell(4).setCellValue("Trip Downtime (Hours)");
    row1.createCell(5).setCellValue("Trip Count");
    row1.createCell(6).setCellValue("Trip MTTR (Minutes)");
    row1.createCell(7).setCellValue("Trip MTBF (Minutes)");
    row1.createCell(8).setCellValue("Trip Availability (%)");
    row1.createCell(9).setCellValue("Event Downtime (Hours)");
    row1.createCell(10).setCellValue("Event Count");
    row1.createCell(11).setCellValue("Event MTTR (Hours)");
    row1.createCell(12).setCellValue("Event MTBF (Hours)");
    row1.createCell(13).setCellValue("Event Availability (%)");

    int x = 14;
    for (Category cat : alphaCatList) {
      row1.createCell(x++).setCellValue(cat.getName() + " Repair Time (Hours)");
      row1.createCell(x++).setCellValue(cat.getName() + " Count");
      row1.createCell(x++).setCellValue(cat.getName() + " MTTR (Hours)");
      row1.createCell(x++).setCellValue(cat.getName() + " MTBF (Hours)");
      row1.createCell(x++).setCellValue(cat.getName() + " Availability (%)");
    }

    CreationHelper createHelper = wb.getCreationHelper();
    CellStyle floatStyle = wb.createCellStyle();
    floatStyle.setDataFormat(createHelper.createDataFormat().getFormat("#,###,##0.0"));
    CellStyle integerStyle = wb.createCellStyle();
    integerStyle.setDataFormat(createHelper.createDataFormat().getFormat("##0"));
    CellStyle dateStyle = wb.createCellStyle();
    dateStyle.setDataFormat(createHelper.createDataFormat().getFormat("yyyy-MM-dd"));

    for (TrendRecord data : records) {
      Row row = sheet1.createRow(rownum++);

      Cell c;

      c = row.createCell(0);
      c.setCellStyle(dateStyle);
      c.setCellValue(data.getBin());

      c = row.createCell(1);
      c.setCellStyle(floatStyle);
      c.setCellValue(data.accDownHours);

      c = row.createCell(2);
      c.setCellStyle(floatStyle);
      c.setCellValue(data.accUptimeHours);

      c = row.createCell(3);
      c.setCellStyle(floatStyle);
      c.setCellValue(data.accAvailability);

      c = row.createCell(4);
      c.setCellStyle(floatStyle);
      c.setCellValue(data.tripHours);

      c = row.createCell(5);
      c.setCellStyle(integerStyle);
      c.setCellValue(data.tripCount);

      c = row.createCell(6);
      c.setCellStyle(floatStyle);
      c.setCellValue(data.tripMttrHours * 60.0);

      c = row.createCell(7);
      c.setCellStyle(floatStyle);
      c.setCellValue(data.mtbtHours * 60.0);

      c = row.createCell(8);
      c.setCellStyle(floatStyle);
      c.setCellValue(data.tripAvailability);

      c = row.createCell(9);
      c.setCellStyle(floatStyle);
      c.setCellValue(data.eventHours);

      c = row.createCell(10);
      c.setCellStyle(integerStyle);
      c.setCellValue(data.eventCount);

      c = row.createCell(11);
      c.setCellStyle(floatStyle);
      c.setCellValue(data.eventMttrHours);

      c = row.createCell(12);
      c.setCellStyle(floatStyle);
      c.setCellValue(data.eventMtbfHours);

      c = row.createCell(13);
      c.setCellStyle(floatStyle);
      c.setCellValue(data.eventAvailability);

      int i = 14;

      for (Category cat : alphaCatList) {
        CategoryDowntime dt = data.getDowntimeMap().get(cat.getCategoryId().longValue());

        c = row.createCell(i++);
        c.setCellStyle(floatStyle);
        c.setCellValue(dt.getDuration() * 24.0);

        c = row.createCell(i++);
        c.setCellStyle(integerStyle);
        c.setCellValue(dt.getIncidentCount());

        c = row.createCell(i++);
        c.setCellStyle(floatStyle);
        c.setCellValue(
            dt.getIncidentCount() == 0 ? 0 : dt.getDuration() / dt.getIncidentCount() * 24);

        double uptime = data.programHours - (dt.getDuration() * 24);
        uptime = (uptime < 0) ? 0 : uptime;
        Double mtbf = dt.getIncidentCount() == 0 ? null : uptime / dt.getIncidentCount();
        double availability = data.programHours == 0 ? 0 : uptime / data.programHours * 100;

        c = row.createCell(i++);
        c.setCellStyle(floatStyle);
        if (mtbf != null) {
          c.setCellValue(mtbf);
        } else {
          c.setCellValue("");
        }

        c = row.createCell(i++);
        c.setCellStyle(floatStyle);
        c.setCellValue(availability);
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

    int y = 14;
    for (Category cat : alphaCatList) {
      sheet1.autoSizeColumn(y++);
      sheet1.autoSizeColumn(y++);
      sheet1.autoSizeColumn(y++);
      sheet1.autoSizeColumn(y++);
      sheet1.autoSizeColumn(y++);
    }

    wb.write(out);
  }
}
