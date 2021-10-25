package org.jlab.dtm.business.params;

import java.math.BigInteger;
import java.util.Date;
import org.jlab.dtm.persistence.enumeration.AccMachineState;
import org.jlab.dtm.persistence.enumeration.FsdExceptionType;
import org.jlab.dtm.persistence.enumeration.HallMachineState;

public class TripParams {

    private Date start;
    private Date end;
    private BigInteger maxDuration;
    private BigInteger minDuration;
    private String maxDurationUnits;
    private String minDurationUnits;
    private String node;
    private BigInteger channel;
    private String area;
    private String cause;
    private String system;
    private String cedType;
    private String cedName;
    private Integer maxTypes;
    private Integer maxDevices;
    private BigInteger tripId;
    private BigInteger faultId;
    private BigInteger exceptionId;
    private AccMachineState[] accStateArray;
    private HallMachineState[] hallAStateArray;
    private HallMachineState[] hallBStateArray;
    private HallMachineState[] hallCStateArray;
    private HallMachineState[] hallDStateArray;
    private FsdExceptionType exceptionType;
    

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

    public BigInteger getMaxDuration() {
        return maxDuration;
    }

    public void setMaxDuration(BigInteger maxDuration) {
        this.maxDuration = maxDuration;
    }

    public BigInteger getMinDuration() {
        return minDuration;
    }

    public void setMinDuration(BigInteger minDuration) {
        this.minDuration = minDuration;
    }

    public String getMaxDurationUnits() {
        return maxDurationUnits;
    }

    public void setMaxDurationUnits(String maxDurationUnits) {
        this.maxDurationUnits = maxDurationUnits;
    }

    public String getMinDurationUnits() {
        return minDurationUnits;
    }

    public void setMinDurationUnits(String minDurationUnits) {
        this.minDurationUnits = minDurationUnits;
    }

    public String getNode() {
        return node;
    }

    public void setNode(String node) {
        this.node = node;
    }

    public BigInteger getChannel() {
        return channel;
    }

    public void setChannel(BigInteger channel) {
        this.channel = channel;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public String getCause() {
        return cause;
    }

    public void setCause(String cause) {
        this.cause = cause;
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

    public Integer getMaxTypes() {
        return maxTypes;
    }

    public void setMaxTypes(Integer maxTypes) {
        this.maxTypes = maxTypes;
    }

    public Integer getMaxDevices() {
        return maxDevices;
    }

    public void setMaxDevices(Integer maxDevices) {
        this.maxDevices = maxDevices;
    }

    public BigInteger getTripId() {
        return tripId;
    }

    public void setTripId(BigInteger tripId) {
        this.tripId = tripId;
    }

    public BigInteger getFaultId() {
        return faultId;
    }

    public void setFaultId(BigInteger faultId) {
        this.faultId = faultId;
    }

    public BigInteger getExceptionId() {
        return exceptionId;
    }

    public void setExceptionId(BigInteger exceptionId) {
        this.exceptionId = exceptionId;
    }

    public AccMachineState[] getAccStateArray() {
        return accStateArray;
    }

    public void setAccStateArray(AccMachineState[] accStateArray) {
        this.accStateArray = accStateArray;
    }

    public HallMachineState[] getHallAStateArray() {
        return hallAStateArray;
    }

    public void setHallAStateArray(HallMachineState[] hallAStateArray) {
        this.hallAStateArray = hallAStateArray;
    }

    public HallMachineState[] getHallBStateArray() {
        return hallBStateArray;
    }

    public void setHallBStateArray(HallMachineState[] hallBStateArray) {
        this.hallBStateArray = hallBStateArray;
    }

    public HallMachineState[] getHallCStateArray() {
        return hallCStateArray;
    }

    public void setHallCStateArray(HallMachineState[] hallCStateArray) {
        this.hallCStateArray = hallCStateArray;
    }

    public HallMachineState[] getHallDStateArray() {
        return hallDStateArray;
    }

    public void setHallDStateArray(HallMachineState[] hallDStateArray) {
        this.hallDStateArray = hallDStateArray;
    }

    public FsdExceptionType getExceptionType() {
        return exceptionType;
    }

    public void setExceptionType(FsdExceptionType exceptionType) {
        this.exceptionType = exceptionType;
    }
}
