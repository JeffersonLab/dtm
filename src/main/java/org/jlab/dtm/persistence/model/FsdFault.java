package org.jlab.dtm.persistence.model;

import java.math.BigInteger;
import java.util.LinkedHashMap;

/**
 *
 * @author ryans
 */
public class FsdFault {

    private BigInteger fsdFaultId;
    private String node;
    private Integer channel;
    private boolean disjoint;
    private LinkedHashMap<BigInteger, FsdDevice> deviceMap = new LinkedHashMap<>();

    public BigInteger getFsdFaultId() {
        return fsdFaultId;
    }

    public void setFsdFaultId(BigInteger fsdFaultId) {
        this.fsdFaultId = fsdFaultId;
    }

    public String getNode() {
        return node;
    }

    public void setNode(String node) {
        this.node = node;
    }

    public Integer getChannel() {
        return channel;
    }

    public void setChannel(Integer channel) {
        this.channel = channel;
    }

    public boolean isDisjoint() {
        return disjoint;
    }

    public void setDisjoint(boolean disjoint) {
        this.disjoint = disjoint;
    }

    public LinkedHashMap<BigInteger, FsdDevice> getDeviceMap() {
        return deviceMap;
    }

    public void setDeviceMap(LinkedHashMap<BigInteger, FsdDevice> deviceMap) {
        this.deviceMap = deviceMap;
    }
}
