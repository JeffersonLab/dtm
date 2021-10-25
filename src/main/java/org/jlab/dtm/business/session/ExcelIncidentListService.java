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
import org.jlab.dtm.business.session.IncidentReportService.IncidentSummary;
import org.jlab.smoothness.business.util.TimeUtil;

/**
 *
 * @author ryans
 */
@Stateless
public class ExcelIncidentListService {

    @PermitAll
    public void export(OutputStream out, List<IncidentSummary> incidentList, String filters) throws
            IOException {
        Workbook wb = new XSSFWorkbook();
        Sheet sheet1 = wb.createSheet("DTM Incident List");

        int rownum = 0;
        Row row0 = sheet1.createRow(rownum++);
        row0.createCell(0).setCellValue("Bounded By: " + filters);

        Row row1 = sheet1.createRow(rownum++);
        row1.createCell(0).setCellValue("TYPE");
        row1.createCell(1).setCellValue("TITLE");
        row1.createCell(2).setCellValue("SUMMARY");
        row1.createCell(3).setCellValue("INCIDENT ID");
        row1.createCell(4).setCellValue("EVENT ID");
        row1.createCell(5).setCellValue("TIME DOWN [BOUNDED]");
        row1.createCell(6).setCellValue("TIME UP [BOUNDED]");
        row1.createCell(7).setCellValue("DOWNTIME (HOURS) [BOUNDED]");
        row1.createCell(8).setCellValue("TIME DOWN [NOT BOUNDED]");
        row1.createCell(9).setCellValue("TIME UP [NOT BOUNDED]");
        row1.createCell(10).setCellValue("DOWNTIME (HOURS) [NOT BOUNDED]");
        row1.createCell(11).setCellValue("SYSTEM NAME");
        row1.createCell(12).setCellValue("SYSTEM ID");
        row1.createCell(13).setCellValue("COMPONENT NAME");
        row1.createCell(14).setCellValue("COMPONENT ID");
        row1.createCell(15).setCellValue("REVIEWED BY USERNAME");
        row1.createCell(16).setCellValue("RESOLUTION");        

        CreationHelper createHelper = wb.getCreationHelper();
        CellStyle numberStyle = wb.createCellStyle();
        numberStyle.setDataFormat(createHelper.createDataFormat().getFormat("##0.###"));
        CellStyle dateStyle = wb.createCellStyle();
        dateStyle.setDataFormat(
                createHelper.createDataFormat().getFormat(TimeUtil.getFriendlyDateTimePattern()));

        Cell c;

        for (IncidentSummary incident : incidentList) {
            Row row = sheet1.createRow(rownum++);

            row.createCell(0).setCellValue(incident.getType());
            row.createCell(1).setCellValue(incident.getTitle());
            row.createCell(2).setCellValue(incident.getSummary());
            row.createCell(3).setCellValue(incident.getIncidentId().longValue());
            row.createCell(4).setCellValue(incident.getEventId().longValue());
            c = row.createCell(5);
            c.setCellStyle(dateStyle);
            c.setCellValue(incident.getTimeDownBounded());
            c = row.createCell(6);
            c.setCellStyle(dateStyle);
            if (incident.getTimeUpBounded() == null) {
                c.setCellValue("");
            } else {
                c.setCellValue(incident.getTimeUpBounded());
            }
            c = row.createCell(7);
            c.setCellStyle(numberStyle);
            c.setCellValue(incident.getDowntimeHoursBounded());
            c = row.createCell(8);
            c.setCellValue(incident.getTimeDown());
            c.setCellStyle(dateStyle);
            c = row.createCell(9);
            c.setCellStyle(dateStyle);
            if (incident.getTimeUp() == null) {
                c.setCellValue("");
            } else {
                c.setCellValue(incident.getTimeUp());
            }
            c = row.createCell(10);
            c.setCellStyle(numberStyle);
            c.setCellValue(incident.getDowntimeHours());
            row.createCell(11).setCellValue(incident.getSystemName());
            row.createCell(12).setCellValue(incident.getSystemId().longValue());
            row.createCell(13).setCellValue(incident.getComponentName());
            if (incident.getComponentId() == null) {
                row.createCell(14).setCellValue("");
            } else {
                row.createCell(14).setCellValue(incident.getComponentId().longValue());
            }
            if (incident.getReviewedByUsername() == null) {
                row.createCell(15).setCellValue("");
            } else {
                row.createCell(15).setCellValue(incident.getReviewedByUsername());
            }            
            if (incident.getResolution() == null) {
                row.createCell(16).setCellValue("");
            } else {
                row.createCell(16).setCellValue(incident.getResolution());
            }
        }

        /*sheet1.autoSizeColumn(0);*/
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

        wb.write(out);
    }

}
