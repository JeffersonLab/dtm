package org.jlab.dtm.business.params;

import java.util.Date;
import org.jlab.dtm.persistence.enumeration.BinSize;
import org.jlab.dtm.persistence.enumeration.RootCause;

public class FsdSummaryReportParams {
        private Date start;
        private Date end;
        private Integer maxDuration;
        private String maxDurationUnits;
        private Integer maxTypes;
        private String grouping;
        private BinSize binSize;
        private Integer maxY;
        private String[] legendDataArray;
        private String chart;     
        private String tripRateBasis;
        private Boolean sadTrips;
        private RootCause[] causeArray;

    public Integer getMaxDuration() {
        return maxDuration;
    }

    public void setMaxDuration(Integer maxDuration) {
        this.maxDuration = maxDuration;
    }

    public String getMaxDurationUnits() {
        return maxDurationUnits;
    }

    public void setMaxDurationUnits(String maxDurationUnits) {
        this.maxDurationUnits = maxDurationUnits;
    }

    public Integer getMaxTypes() {
        return maxTypes;
    }

    public void setMaxTypes(Integer maxTypes) {
        this.maxTypes = maxTypes;
    }

    public String getGrouping() {
        return grouping;
    }

    public void setGrouping(String grouping) {
        this.grouping = grouping;
    }

    public BinSize getBinSize() {
        return binSize;
    }

    public void setBinSize(BinSize binSize) {
        this.binSize = binSize;
    }

    public Integer getMaxY() {
        return maxY;
    }

    public void setMaxY(Integer maxY) {
        this.maxY = maxY;
    }

    public String[] getLegendDataArray() {
        return legendDataArray;
    }

    public void setLegendDataArray(String[] legendDataArray) {
        this.legendDataArray = legendDataArray;
    }

    public Date getStart() {
        return start;
    }

    public void setStart(Date start) {
        this.start = start;
    }

    public Date getEnd() {
        return end;
    }

    public void setEnd(Date end) {
        this.end = end;
    }

    public String getChart() {
        return chart;
    }

    public void setChart(String chart) {
        this.chart = chart;
    }

    public String getTripRateBasis() {
        return tripRateBasis;
    }

    public void setTripRateBasis(String tripRateBasis) {
        this.tripRateBasis = tripRateBasis;
    }

    public Boolean getSadTrips() {
        return sadTrips;
    }

    public void setSadTrips(Boolean sadTrips) {
        this.sadTrips = sadTrips;
    }

    public RootCause[] getCauseArray() {
        return causeArray;
    }

    public void setCauseArray(RootCause[] causeArray) {
        this.causeArray = causeArray;
    }
}
