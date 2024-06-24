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
import org.jlab.dtm.persistence.model.EventDowntime;
import org.jlab.smoothness.business.util.TimeUtil;

/**
 * @author ryans
 */
@Stateless
public class ExcelEventListService {

  @PermitAll
  public void export(OutputStream out, List<EventDowntime> eventList, String filters)
      throws IOException {
    Workbook wb = new XSSFWorkbook();
    Sheet sheet1 = wb.createSheet("DTM Event List");

    int rownum = 0;
    Row row0 = sheet1.createRow(rownum++);
    row0.createCell(0).setCellValue("Bounded By: " + filters);

    Row row1 = sheet1.createRow(rownum++);
    row1.createCell(0).setCellValue("TITLE");
    row1.createCell(1).setCellValue("EVENT ID");
    row1.createCell(2).setCellValue("TIME DOWN [BOUNDED]");
    row1.createCell(3).setCellValue("TIME UP [BOUNDED]");
    row1.createCell(4).setCellValue("DOWNTIME (HOURS) [BOUNDED]");
    row1.createCell(5).setCellValue("SUSPEND (HOURS) [BOUNDED]");
    row1.createCell(6).setCellValue("RESTORE (HOURS) [BOUNDED]");
    row1.createCell(7).setCellValue("TIME DOWN [NOT BOUNDED]");
    row1.createCell(8).setCellValue("TIME UP [NOT BOUNDED]");
    row1.createCell(9).setCellValue("DOWNTIME (HOURS) [NOT BOUNDED]");
    row1.createCell(10).setCellValue("NUMBER OF INCIDENTS [NOT BOUNDED]");

    CreationHelper createHelper = wb.getCreationHelper();
    CellStyle floatStyle = wb.createCellStyle();
    floatStyle.setDataFormat(createHelper.createDataFormat().getFormat("##0.0##"));
    CellStyle integerStyle = wb.createCellStyle();
    integerStyle.setDataFormat(createHelper.createDataFormat().getFormat("##0"));
    CellStyle dateStyle = wb.createCellStyle();
    dateStyle.setDataFormat(
        createHelper.createDataFormat().getFormat(TimeUtil.getFriendlyDateTimePattern()));

    for (EventDowntime event : eventList) {
      Row row = sheet1.createRow(rownum++);

      Cell c;

      row.createCell(0).setCellValue(event.getTitle());
      row.createCell(1).setCellValue(event.getEventId().longValue());
      c = row.createCell(2);
      c.setCellStyle(dateStyle);
      c.setCellValue(event.getTimeDownBounded());
      c = row.createCell(3);
      c.setCellStyle(dateStyle);
      if (event.getTimeUpBounded() == null) {
        c.setCellValue("");
      } else {
        c.setCellValue(event.getTimeUpBounded());
      }
      c = row.createCell(4);
      c.setCellStyle(floatStyle);
      c.setCellValue(event.getDowntimeHoursBounded());
      c = row.createCell(5);
      c.setCellStyle(floatStyle);
      c.setCellValue(event.getSuspendHoursBounded());
      c = row.createCell(6);
      c.setCellStyle(floatStyle);
      c.setCellValue(event.getRestoreHoursBounded());
      c = row.createCell(7);
      c.setCellStyle(dateStyle);
      c.setCellValue(event.getTimeDown());
      c = row.createCell(8);
      c.setCellStyle(dateStyle);
      if (event.getTimeUp() == null) {
        c.setCellValue("");
      } else {
        c.setCellValue(event.getTimeUp());
      }
      c = row.createCell(9);
      c.setCellStyle(floatStyle);
      c.setCellValue(event.getDowntimeHours());

      c = row.createCell(10);
      c.setCellStyle(integerStyle);
      c.setCellValue(event.getIncidentCount());
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

    wb.write(out);
  }
}
