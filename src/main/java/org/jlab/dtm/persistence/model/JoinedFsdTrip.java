package org.jlab.dtm.persistence.model;

import java.math.BigInteger;
import java.util.Date;
import org.jlab.dtm.persistence.enumeration.AccMachineState;
import org.jlab.dtm.persistence.enumeration.HallMachineState;

/**
 * @author ryans
 */
public class JoinedFsdTrip {
  private BigInteger fsdTripId;
  private BigInteger fsdFaultId;
  private BigInteger fsdDeviceExceptionid;
  private Date start;
  private Date end;
  private AccMachineState accState;
  private HallMachineState hallAState;
  private HallMachineState hallBState;
  private HallMachineState hallCState;
  private HallMachineState hallDState;
  private String rootCause;
  private String node;
  private Integer channel;
  private String category;
  private String system;
  private String cedType;
  private String cedName;
  private Boolean confirmed;

  public BigInteger getFsdTripId() {
    return fsdTripId;
  }

  public void setFsdTripId(BigInteger fsdTripId) {
    this.fsdTripId = fsdTripId;
  }

  public BigInteger getFsdFaultId() {
    return fsdFaultId;
  }

  public void setFsdFaultId(BigInteger fsdFaultId) {
    this.fsdFaultId = fsdFaultId;
  }

  public BigInteger getFsdDeviceExceptionid() {
    return fsdDeviceExceptionid;
  }

  public void setFsdDeviceExceptionid(BigInteger fsdDeviceExceptionid) {
    this.fsdDeviceExceptionid = fsdDeviceExceptionid;
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

  public AccMachineState getAccState() {
    return accState;
  }

  public void setAccState(AccMachineState accState) {
    this.accState = accState;
  }

  public HallMachineState getHallAState() {
    return hallAState;
  }

  public void setHallAState(HallMachineState hallAState) {
    this.hallAState = hallAState;
  }

  public HallMachineState getHallBState() {
    return hallBState;
  }

  public void setHallBState(HallMachineState hallBState) {
    this.hallBState = hallBState;
  }

  public HallMachineState getHallCState() {
    return hallCState;
  }

  public void setHallCState(HallMachineState hallCState) {
    this.hallCState = hallCState;
  }

  public HallMachineState getHallDState() {
    return hallDState;
  }

  public void setHallDState(HallMachineState hallDState) {
    this.hallDState = hallDState;
  }

  public long getElapsedMillis() {
    Date down = start;
    Date up = end;

    if (up == null) {
      up = new Date();
    }

    if (down == null) {
      down = up;
    }

    return up.getTime() - down.getTime();
  }

  public String getRootCause() {
    return rootCause;
  }

  public void setRootCause(String rootCause) {
    this.rootCause = rootCause;
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

  public Boolean isConfirmed() {
    return confirmed;
  }

  public void setConfirmed(Boolean confirmed) {
    this.confirmed = confirmed;
  }

  @Override
  public String toString() {
    return "Trip ID: " + fsdTripId + ", Cause: " + rootCause;
  }
}
