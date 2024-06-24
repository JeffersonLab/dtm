package org.jlab.dtm.business.session;

import java.io.PrintWriter;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import javax.annotation.security.PermitAll;
import javax.ejb.Stateless;
import javax.servlet.ServletOutputStream;
import org.jlab.dtm.persistence.model.*;

/**
 * @author ryans
 */
@Stateless
public class ExcelRepairsService {

  @PermitAll
  public void exportSummaryAsCsv(ServletOutputStream out, List<HistogramBin> recordList)
      throws SQLException {

    SimpleDateFormat dateFormatter = new SimpleDateFormat("dd-MMM-yyyy HH");
    DecimalFormat numberFormatter = new DecimalFormat("###,##0.0000");

    try (PrintWriter writer = new PrintWriter(out)) {

      writer.print("Date");
      writer.print(",");
      writer.print("Incidents");
      writer.print(",");
      writer.print("New Incidents");
      writer.print(",");
      writer.print("Duration (Hours)");
      writer.print(",");
      writer.print("Grouping");
      writer.println();

      for (HistogramBin bin : recordList) {
        writer.print(dateFormatter.format(bin.getStart()));
        writer.print(",");
        writer.print(bin.getCount());
        writer.print(",");
        writer.print(bin.getNewCount());
        writer.print(",");
        writer.print(numberFormatter.format(bin.getDurationMillis() / 3.6e+6));
        writer.print(",");
        writer.print(bin.getGrouping() == null ? "" : bin.getGrouping());
        writer.println();
      }
    }
  }
}
