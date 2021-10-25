package org.jlab.dtm.persistence.model;

import java.math.BigInteger;
import java.util.*;

import org.jlab.dtm.persistence.enumeration.AccMachineState;
import org.jlab.dtm.persistence.enumeration.HallMachineState;

/**
 *
 * @author ryans
 */
public class FsdTrip {
    private BigInteger fsdTripId;
    private Date start;
    private Date end;
    private AccMachineState accState;
    private HallMachineState hallAState;
    private HallMachineState hallBState;
    private HallMachineState hallCState;
    private HallMachineState hallDState;
    private LinkedHashMap<BigInteger, FsdFault> faultMap = new LinkedHashMap<>();
    private String rootCause;
    private String area;
    
    public BigInteger getFsdTripId() {
        return fsdTripId;
    }

    public void setFsdTripId(BigInteger fsdTripId) {
        this.fsdTripId = fsdTripId;
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

    public LinkedHashMap<BigInteger, FsdFault> getFaultMap() {
        return faultMap;
    }

    public void setFaultMap(LinkedHashMap<BigInteger, FsdFault> faultMap) {
        this.faultMap = faultMap;
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
    
    public int getDeviceCount() {
        int count = 0;
        for(FsdFault fault: faultMap.values()) {
            for(FsdDevice device: fault.getDeviceMap().values()) {
                count++;
            }
        }        
        
        return count;
    }
    
    public String getTitle() {
        LinkedHashSet<String> titleSet = new LinkedHashSet<String>();
        
        for(FsdFault fault: faultMap.values()) {
            for(FsdDevice device: fault.getDeviceMap().values()) {
                if(device.getSystem() != null) {
                    titleSet.add(device.getSystem());
                }
            }
        }
        
        String title = "";
        Iterator<String> iterator = titleSet.iterator();
        if(iterator.hasNext()) {
            title = iterator.next();
            
            while(iterator.hasNext()) {
                title = title + " + " + iterator.next();
            }
        }
        
        // Truncate long titles
        if(title.length() > 128) {
            title = title.substring(0, 128) + "...";
        } else if(title.isEmpty()) {
            title = "<< PHANTOM >>";
        }
        
        return title;
    }

    public String getRootCause() {
        return rootCause;
    }

    public void setRootCause(String rootCause) {
        this.rootCause = rootCause;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    @Override
    public String toString() {
        return "Trip ID: " + fsdTripId + ", Cause: " + rootCause;
    }
}
