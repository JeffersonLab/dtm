package org.jlab.dtm.business.session;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.text.DecimalFormat;
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
import org.jlab.dtm.persistence.model.CategoryDowntime;
import org.jlab.smoothness.business.util.TimeUtil;

/**
 *
 * @author ryans
 */
@Stateless
public class ExcelCategoryDowntimeService {

    @PermitAll
    public void export(OutputStream out, List<CategoryDowntime> categoryDowntimeList,
            String filters, double periodDurationHours, double grandTotalDuration) throws
            IOException {
        Workbook wb = new XSSFWorkbook();
        Sheet sheet1 = wb.createSheet("DTM Category Downtime");

        int rownum = 0;
        Row row0 = sheet1.createRow(rownum++);
        //row0.createCell(0).setCellValue("Bounded By: " + filters);

        Row row1 = sheet1.createRow(rownum++);
        row1.createCell(0).setCellValue("CATEGORY");
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

        for (CategoryDowntime incident : categoryDowntimeList) {
            Row row = sheet1.createRow(rownum++);

            row.createCell(0).setCellValue(incident.getName());
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
    public void exportAsCsv(OutputStream out, List<CategoryDowntime> downtimeList,
            String trim, double periodDurationHours, double grandTotalDuration) {

        DecimalFormat formatter = new DecimalFormat("#,##0.0");
        
        try (PrintWriter writer = new PrintWriter(out)) {

            for (CategoryDowntime downtime : downtimeList) {
                writer.print(downtime.getName());
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
