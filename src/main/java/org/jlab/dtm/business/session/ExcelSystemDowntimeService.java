package org.jlab.dtm.business.session;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jlab.dtm.persistence.model.CategoryDowntime;
import org.jlab.dtm.persistence.model.SystemDowntime;
import org.jlab.smoothness.business.util.TimeUtil;

import javax.annotation.security.PermitAll;
import javax.ejb.Stateless;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.List;

/**
 *
 * @author ryans
 */
@Stateless
public class ExcelSystemDowntimeService {

    @PermitAll
    public void export(OutputStream out, List<SystemDowntime> downtimeList,
            String filters, double periodDurationHours, double grandTotalDuration) throws
            IOException {
        Workbook wb = new XSSFWorkbook();
        Sheet sheet1 = wb.createSheet("DTM System Downtime");

        int rownum = 0;
        Row row0 = sheet1.createRow(rownum++);
        //row0.createCell(0).setCellValue("Bounded By: " + filters);

        Row row1 = sheet1.createRow(rownum++);
        row1.createCell(0).setCellValue("SYSTEM");
        row1.createCell(1).setCellValue("DOWNTIME (HOURS)");
        row1.createCell(2).setCellValue("NUMBER OF INCIDENTS");
        row1.createCell(3).setCellValue("MEAN TIME TO RECOVER (HOURS)");

        CreationHelper createHelper = wb.getCreationHelper();
        CellStyle numberStyle = wb.createCellStyle();
        numberStyle.setDataFormat(createHelper.createDataFormat().getFormat("#,##0.0"));
        CellStyle dateStyle = wb.createCellStyle();
        dateStyle.setDataFormat(
                createHelper.createDataFormat().getFormat(TimeUtil.getFriendlyDateTimePattern()));

        Cell c;

        for (SystemDowntime incident : downtimeList) {
            Row row = sheet1.createRow(rownum++);

            row.createCell(0).setCellValue(incident.getSystemName());
            c = row.createCell(1);
            c.setCellStyle(numberStyle);
            c.setCellValue(incident.getDuration() * 24);
            row.createCell(2).setCellValue(incident.getIncidentCount());
            c = row.createCell(3);
            c.setCellStyle(numberStyle);
            c.setCellValue(incident.getDuration() / incident.getIncidentCount() * 24);
        }

        sheet1.autoSizeColumn(0);

        row0.createCell(0).setCellValue("Bounded By: " + filters);

        sheet1.autoSizeColumn(1);
        sheet1.autoSizeColumn(2);
        sheet1.autoSizeColumn(3);

        wb.write(out);
    }

    @PermitAll
    public void exportAsCsv(OutputStream out, List<SystemDowntime> downtimeList,
            String trim, double periodDurationHours, double grandTotalDuration) {

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
