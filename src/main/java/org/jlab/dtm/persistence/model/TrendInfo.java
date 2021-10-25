package org.jlab.dtm.persistence.model;

import java.util.Date;

/**
 *
 * @author ryans
 */
public class TrendInfo  {
    private final Date date;
    private final long count;
    private final double duration;

    public TrendInfo(Date date, Number count, Number duration) {
        this.date = date;
        this.count = count.longValue();
        this.duration = duration.doubleValue();
    }

    public Date getDate() {
        return date;
    }

    public long getCount() {
        return count;
    }

    public double getDuration() {
        return duration;
    }
}
