package org.jlab.dtm.business.params;

import java.math.BigInteger;
import java.util.Date;

public class CategoryDowntimeReportParams {
        private Date start;
        private Date end;
        BigInteger eventTypeId;
        Boolean beamTransport;
        String chart;        
        String data;
        private boolean packed;

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

    public BigInteger getEventTypeId() {
        return eventTypeId;
    }

    public void setEventTypeId(BigInteger eventTypeId) {
        this.eventTypeId = eventTypeId;
    }

    public Boolean getBeamTransport() {
        return beamTransport;
    }

    public void setBeamTransport(Boolean beamTransport) {
        this.beamTransport = beamTransport;
    }

    public String getChart() {
        return chart;
    }

    public void setChart(String chart) {
        this.chart = chart;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
    
    public boolean getPacked() {
        return packed;
    }
    
    public void setPacked(boolean packed) {
        this.packed = packed;
    }
}
