package org.jlab.dtm.presentation.util;

import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.servlet.http.HttpServletRequest;

import org.jlab.dtm.persistence.enumeration.AccMachineState;
import org.jlab.dtm.persistence.enumeration.BinSize;
import org.jlab.dtm.persistence.enumeration.FsdExceptionType;
import org.jlab.dtm.persistence.enumeration.HallMachineState;
import org.jlab.dtm.persistence.enumeration.IncidentSortKey;
import org.jlab.dtm.persistence.enumeration.RootCause;
import org.jlab.dtm.persistence.enumeration.RootCauseIncidentMask;
import org.jlab.dtm.persistence.enumeration.SystemExpertAcknowledgement;
import org.jlab.smoothness.business.util.TimeUtil;

/**
 *
 * @author ryans
 */
public final class DtmParamConverter {

    private DtmParamConverter() {
        // No one can instantiate due to private visibility
    }

    public static Date convertMonthAndYear(HttpServletRequest request, String name) throws
            ParseException {
        final DateTimeFormatter formatter = new DateTimeFormatterBuilder().parseCaseInsensitive().appendPattern("MMMM yyyy").toFormatter();

        Date value = null;

        String valueStr = request.getParameter(name);

        if (valueStr != null && !valueStr.isEmpty()) {
            final YearMonth ym = formatter.parse(valueStr, YearMonth::from);
            final LocalDate ld = ym.atDay(1);
            final ZonedDateTime zdt = ld.atStartOfDay(ZoneId.of("America/New_York"));
            value = Date.from(zdt.toInstant());
        }

        return value;
    }

    public static Date convertISO8601Date(HttpServletRequest request, String name) throws
            ParseException {
        final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        Date value = null;

        String valueStr = request.getParameter(name);

        if (valueStr != null && !valueStr.isEmpty()) {
            final LocalDate ld = formatter.parse(valueStr, LocalDate::from);
            final ZonedDateTime zdt = ld.atStartOfDay(ZoneId.of("America/New_York"));
            value = Date.from(zdt.toInstant());
        }

        return value;
    }

    public static Date convertJLabDate(HttpServletRequest request, String name) throws
            ParseException {
        final DateTimeFormatter formatter = new DateTimeFormatterBuilder().parseCaseInsensitive().appendPattern("dd-MMM-yyyy").toFormatter();

        Date value = null;

        String valueStr = request.getParameter(name);

        if (valueStr != null && !valueStr.isEmpty()) {
            final LocalDate ld = formatter.parse(valueStr, LocalDate::from);
            final ZonedDateTime zdt = ld.atStartOfDay(ZoneId.of("America/New_York"));
            value = Date.from(zdt.toInstant());
        }

        return value;
    }

    public static Date convertJLabDateTime(HttpServletRequest request, String name) throws
            ParseException {
        final DateTimeFormatter formatter = new DateTimeFormatterBuilder().parseCaseInsensitive().appendPattern(TimeUtil.getFriendlyDateTimePattern()).toFormatter();

        Date value = null;

        String valueStr = request.getParameter(name);

        if (valueStr != null && !valueStr.isEmpty()) {
            final LocalDateTime ldt = formatter.parse(valueStr, LocalDateTime::from);
            final ZonedDateTime zdt = ldt.atZone(ZoneId.of("America/New_York"));
            value = Date.from(zdt.toInstant());
        }

        return value;
    }

    public static Date convertJLabDateTime(String valueStr) throws
            ParseException {
        final DateTimeFormatter formatter = new DateTimeFormatterBuilder().parseCaseInsensitive().appendPattern(TimeUtil.getFriendlyDateTimePattern()).toFormatter();

        Date value = null;

        if (valueStr != null && !valueStr.isEmpty()) {
            final LocalDateTime ldt = formatter.parse(valueStr, LocalDateTime::from);
            final ZonedDateTime zdt = ldt.atZone(ZoneId.of("America/New_York"));
            value = Date.from(zdt.toInstant());
        }

        return value;
    }

    public static Date convertJLabMonth(HttpServletRequest request, String name) throws
            ParseException {
        final DateTimeFormatter formatter = new DateTimeFormatterBuilder().parseCaseInsensitive().appendPattern("MMM yyyy").toFormatter();

        Date value = null;

        String valueStr = request.getParameter(name);

        if (valueStr != null && !valueStr.isEmpty()) {
            final YearMonth ym = formatter.parse(valueStr, YearMonth::from);
            final LocalDate ld = ym.atDay(1);
            final ZonedDateTime zdt = ld.atStartOfDay(ZoneId.of("America/New_York"));
            value = Date.from(zdt.toInstant());
        }

        return value;
    }

    public static Date convertISO8601DateTime(HttpServletRequest request, String name) throws
            ParseException {
        final DateTimeFormatter formatter = new DateTimeFormatterBuilder().parseCaseInsensitive().appendPattern("yyyy-MM-dd HH:mm").toFormatter();

        Date value = null;

        String valueStr = request.getParameter(name);

        if (valueStr != null && !valueStr.isEmpty()) {
            final LocalDateTime ldt = formatter.parse(valueStr, LocalDateTime::from);
            final ZonedDateTime zdt = ldt.atZone(ZoneId.of("America/New_York"));
            value = Date.from(zdt.toInstant());
        }

        return value;
    }

    public static RootCause[] convertRootCauseArray(HttpServletRequest request, String name) {
        String[] valueStrArray = request.getParameterValues(name);
        List<RootCause> valueList = new ArrayList<>();

        if (valueStrArray != null && valueStrArray.length > 0) {
            for (String valueStr : valueStrArray) {
                if (valueStr != null && !valueStr.isEmpty()) {
                    RootCause value = RootCause.valueOf(valueStr);
                    valueList.add(value);
                }
            }
        }

        return valueList.toArray(new RootCause[]{});
    }

    public static AccMachineState[] convertAccStateArray(HttpServletRequest request, String name) {
        String[] valueStrArray = request.getParameterValues(name);
        List<AccMachineState> valueList = new ArrayList<AccMachineState>();

        if (valueStrArray != null && valueStrArray.length > 0) {
            for (String valueStr : valueStrArray) {
                if (valueStr != null && !valueStr.isEmpty()) {
                    AccMachineState value = AccMachineState.valueOf(valueStr);
                    valueList.add(value);
                }
            }
        }

        return valueList.toArray(new AccMachineState[]{});
    }

    public static AccMachineState convertAccState(HttpServletRequest request, String name) {
        AccMachineState value = null;

        String valueStr = request.getParameter(name);

        if (valueStr != null && !valueStr.isEmpty()) {
            value = AccMachineState.valueOf(valueStr);
        }

        return value;
    }
    
    public static IncidentSortKey convertIncidentSortKey(HttpServletRequest request, String name) {
        IncidentSortKey value = null;

        String valueStr = request.getParameter(name);

        if (valueStr != null && !valueStr.isEmpty()) {
            value = IncidentSortKey.valueOf(valueStr);
        }

        return value;        
    }    

    public static SystemExpertAcknowledgement convertSystemExpertAcknowledgement(HttpServletRequest request, String name) {
        SystemExpertAcknowledgement value = null;

        String valueStr = request.getParameter(name);

        if (valueStr != null && !valueStr.isEmpty()) {
            value = SystemExpertAcknowledgement.valueOf(valueStr);
        }

        return value;
    }

    public static RootCauseIncidentMask converRootCauseIncidentMask(HttpServletRequest request, String name) {
        RootCauseIncidentMask value = null;

        String valueStr = request.getParameter(name);

        if (valueStr != null && !valueStr.isEmpty()) {
            value = RootCauseIncidentMask.valueOf(valueStr);
        }

        return value;
    }

    public static HallMachineState[] convertHallStateArray(HttpServletRequest request, String name) {
        String[] valueStrArray = request.getParameterValues(name);
        List<HallMachineState> valueList = new ArrayList<HallMachineState>();

        if (valueStrArray != null && valueStrArray.length > 0) {
            for (String valueStr : valueStrArray) {
                if (valueStr != null && !valueStr.isEmpty()) {
                    HallMachineState value = HallMachineState.valueOf(valueStr);
                    valueList.add(value);
                }
            }
        }

        return valueList.toArray(new HallMachineState[]{});
    }

    public static HallMachineState convertHallState(HttpServletRequest request, String name) {
        HallMachineState value = null;

        String valueStr = request.getParameter(name);

        if (valueStr != null && !valueStr.isEmpty()) {
            value = HallMachineState.valueOf(valueStr);
        }

        return value;
    }

    public static FsdExceptionType convertFsdExceptionType(HttpServletRequest request,
            String name) {
        FsdExceptionType value = null;

        String valueStr = request.getParameter(name);

        if (valueStr != null && !valueStr.isEmpty()) {
            value = FsdExceptionType.valueOf(valueStr);
        }

        return value;
    }

    public static BinSize convertBinSize(HttpServletRequest request, String name) {
        BinSize value = null;

        String valueStr = request.getParameter(name);

        if (valueStr != null && !valueStr.isEmpty()) {
            value = BinSize.valueOf(valueStr);
        }

        return value;
    }
}
