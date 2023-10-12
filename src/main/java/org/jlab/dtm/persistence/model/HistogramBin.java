package org.jlab.dtm.persistence.model;

import org.jlab.dtm.business.util.DtmTimeUtil;
import org.jlab.dtm.persistence.enumeration.BinSize;
import org.jlab.smoothness.business.util.DateRange;
import org.jlab.smoothness.business.util.TimeUtil;

import java.util.Calendar;
import java.util.Date;

public class HistogramBin {
    private Date start;
    private int count = 0;

    /**
     * Count of NEW incidents as a single incident may overflow into multiple bins
     */
    private int newCount = 0;
    private long durationMillis = 0;
    private String grouping;

    public static DateRange adjust(Date start, Date end, BinSize size) {
        switch(size) {
            case HOUR:
                start = TimeUtil.startOfHour(start, Calendar.getInstance());
                if(!TimeUtil.startOfHour(end, Calendar.getInstance()).equals(end)) {
                    end = DtmTimeUtil.startOfNextHour(end, Calendar.getInstance());
                }
                break;
            case DAY:
                start = TimeUtil.startOfDay(start, Calendar.getInstance());
                if(!TimeUtil.startOfDay(end, Calendar.getInstance()).equals(end)) {
                    end = TimeUtil.startOfNextDay(end, Calendar.getInstance());
                }
                break;
            case MONTH:
                start = TimeUtil.startOfMonth(start, Calendar.getInstance());
                if(!TimeUtil.startOfMonth(end, Calendar.getInstance()).equals(end)) {
                    end = TimeUtil.startOfNextMonth(end, Calendar.getInstance());
                }
                break;
            default:
                throw new RuntimeException("Unknown bin size: " + size);
        }

        return new DateRange(start, end);
    }

    public static Date getInclusiveEnd(Date end, BinSize size) {
        switch(size) {
            case HOUR:
                end = TimeUtil.addHours(end, -1);
                break;
            case DAY:
                end = TimeUtil.addDays(end, -1);
                break;
            case MONTH:
                end = TimeUtil.addMonths(end, -1);
                break;
            default:
                throw new RuntimeException("Unknown bin size: " + size);
        }

        return end;
    }

    public Date getStart() {
        return start;
    }

    public void setStart(Date start) {
        this.start = start;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public long getDurationMillis() {
        return durationMillis;
    }

    public void setDurationMillis(long durationMillis) {
        this.durationMillis = durationMillis;
    }

    public String getGrouping() {
        return grouping;
    }

    public void setGrouping(String grouping) {
        this.grouping = grouping;
    }

    public void incrementCount() {
        this.count++;
    }

    public void incrementNewCount() {
        this.newCount++;
    }

    public int getNewCount() {
        return newCount;
    }
}
