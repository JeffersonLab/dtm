package org.jlab.dtm.business.util;

import org.jlab.smoothness.business.util.TimeUtil;
import org.jlab.smoothness.presentation.util.ParamConverter;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Logger;

/**
 *
 * @author ryans
 */
public final class DtmTimeUtil {

    private static final Logger logger = Logger.getLogger(
            DtmTimeUtil.class.getName());

    private DtmTimeUtil() {
        // not public
    }

    public static String toISO8601Date(Date date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String value = null;
        if (date != null) {
            LocalDate ld = date.toInstant().atZone(ZoneId.of("America/New_York")).toLocalDate();
            value = ld.format(formatter);
        }
        return value;
    }

    public static Date add(Date date, int field, int amount) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(field, amount);
        return cal.getTime();
    }

    public static Date getEndInclusive(Date end, String size) {
        Date endInclusive = end;

        if(size != null && end != null) {
            switch (size) {
                case "day":
                    endInclusive = TimeUtil.addDays(end, -1);
                    break;
                case "week":
                    endInclusive = TimeUtil.addDays(end, -7);
                    break;
                case "month":
                    endInclusive = TimeUtil.addMonths(end, -1);
                    break;
                case "quarter":
                    endInclusive = TimeUtil.addMonths(end, -3);
                    break;
                case "year":
                    endInclusive = TimeUtil.addYears(end, -1);
                    break;
            }
        }

        return endInclusive;
    }

    public static Date startOfNextHour(Date date, Calendar tz) {
        return TimeUtil.addHours(TimeUtil.startOfHour(date, tz), 1);
    }
}
