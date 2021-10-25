package org.jlab.dtm.persistence.model;

import java.math.BigInteger;

/**
 *
 * @author ryans
 */
public class FsdDevice {

    private BigInteger fsdDeviceId;
    private String category;
    private String system;
    private String cedType;
    private String cedName;    
    private boolean confirmed;
    private String region;

    public BigInteger getFsdDeviceId() {
        return fsdDeviceId;
    }

    public void setFsdDeviceId(BigInteger fsdDeviceId) {
        this.fsdDeviceId = fsdDeviceId;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getSystem() {
        return system;
    }

    public void setSystem(String system) {
        this.system = system;
    }

    public String getCedType() {
        return cedType;
    }

    public void setCedType(String cedType) {
        this.cedType = cedType;
    }

    public String getCedName() {
        return cedName;
    }

    public void setCedName(String cedName) {
        this.cedName = cedName;
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public void setConfirmed(boolean confirmed) {
        this.confirmed = confirmed;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }
}
