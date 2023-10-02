package org.jlab.dtm.persistence.model;

import java.util.Date;

public class HistogramBin {
    private Date start;
    private int count = 0;
    private long durationMillis = 0;
    private String grouping;

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
}
