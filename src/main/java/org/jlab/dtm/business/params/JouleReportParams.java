package org.jlab.dtm.business.params;

import java.util.Date;

public class JouleReportParams {
        private Date start;
        private Date end;
        private Float quality;
        private Float maintenance;
        private Float scaler;
        private String type;
        private String size;

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

    public Float getMaintenance() {
        return maintenance;
    }

    public void setMaintenance(Float maintenance) {
        this.maintenance = maintenance;
    }

    public Float getScaler() {
        return scaler;
    }

    public void setScaler(Float scaler) {
        this.scaler = scaler;
    }

    public Float getQuality() {
        return quality;
    }

    public void setQuality(Float quality) {
        this.quality = quality;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }
}
