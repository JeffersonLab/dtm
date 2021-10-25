package org.jlab.dtm.business.util;

import org.jlab.dtm.persistence.model.FsdDevice;
import org.jlab.dtm.persistence.model.FsdFault;
import org.jlab.dtm.persistence.model.FsdTrip;

import java.math.BigInteger;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Set;

public class FsdAreaLogic {
    public static String getArea(String region) {
        String area = "Unknown";
        if(region != null) {
            if(region.contains("Hall A")) {
                area = "Hall A";
            } else if(region.contains("Hall B")) {
                area = "Hall B";
            } else if(region.contains("Hall C")) {
                area = "Hall C";
            } else if(region.contains("Hall D")) {
                area = "Hall D";
            } else {
                area = "Accelerator";
            }
        }

        return area;
    }

    public static void setArea(FsdTrip trip) {
        String cause = trip.getRootCause();
        LinkedHashMap<BigInteger, FsdFault> faultMap = trip.getFaultMap();

        String area = "Unknown";

        if(cause != null && (cause.equals("MPS (BCM/BLA)") || cause.equals("Dump (Insert.)"))) {
            area = "Accelerator";
        } else  if(cause != null && (cause.startsWith("RF"))) {
            area = "Accelerator (RF)";
        } else {
            Set<String> areaSet = new LinkedHashSet<>();
            Collection<FsdFault> faultList = faultMap.values();
            if (faultList != null) {
                for (FsdFault fault : faultList) {
                    Collection<FsdDevice> deviceList = fault.getDeviceMap().values();

                    if (deviceList != null) {
                        for (FsdDevice device : deviceList) {
                            areaSet.add(FsdAreaLogic.getArea(device.getRegion()));
                        }
                    }
                }
            }

            if (areaSet.size() == 1) {
                area = areaSet.iterator().next();

            } else if (areaSet.size() > 1) {
                area = "Multiple";
            }
        }

        trip.setArea(area);
    }
}
