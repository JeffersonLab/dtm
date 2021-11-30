package org.jlab.dtm.business.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import org.jlab.dtm.persistence.model.FsdDevice;
import org.jlab.dtm.persistence.model.FsdFault;
import org.jlab.dtm.persistence.model.FsdTrip;

/**
 *
 * @author ryans
 */
public class FsdRootCauseLogic {

    private static final String CED_MODULE_NAMES_JSON_URL
            = "http://ced/inventory/?z=&t=CryoModule&p=ModuleType&out=json";

    private final Collection<String> c100Names = new HashSet<>(); // ArrayList.contains may have better performance for small size collection but whatever...

    private final Collection<String> otherNames = new HashSet<>();

    public FsdRootCauseLogic() throws IOException {
        lookupModuleNames();
    }

    private void lookupModuleNames() throws MalformedURLException, IOException {
        URL url = new URL(CED_MODULE_NAMES_JSON_URL);
        InputStream in = url.openStream();
        try (JsonReader reader = Json.createReader(in)) {
            JsonObject json = reader.readObject();
            String status = json.getString("stat");
            if (!"ok".equals(status)) {
                throw new IOException("Unable to lookup C100 Names from CED; response stat not 'ok'");
            }
            JsonObject inventory = json.getJsonObject("Inventory");
            JsonArray elements = inventory.getJsonArray("elements");
            for (JsonObject element : elements.getValuesAs(JsonObject.class)) {
                String name = element.getString("name");
                JsonObject properties = element.getJsonObject("properties");
                String type = properties.getString("ModuleType");
                if ("C100".equals(type)) {
                    c100Names.add(name);
                } else {
                    otherNames.add(name);
                }
            }
        }
    }

    public String getRootCause(Set<String> cedNameSet, Set<String> cedTypeSet,
            Set<String> categorySet) {
        String cause;

        /*If no system specified*/
        if (categorySet.isEmpty()) {
            cause = "Unknown/Missing"; // Might be Phantom; or HCO_SYSTEM lookup failed
            // If cedNameSet.isEmpty() then Phantom; otherwise SYSTEM lookup failed
        } else if (categorySet.size() == 1) {
            /*If only one category cause*/

            cause = categorySet.iterator().next();
        } else /*Multiple values*/ if (categorySet.contains("RF")) {
            /*It's always RF's fault!*/

            cause = "RF";
        } else {
            cause = "Multiple/Other";
        }

        /*Let's get more specific on Dumps*/
        if ("Beam Dumps".equals(cause)) {
            cause = "Dump (Multi/Other)";

            if (cedTypeSet.size() == 1) { // If only one type cause...
                String type = cedTypeSet.iterator().next();
                if ("2kWDump".equalsIgnoreCase(type)) { // Renamed 2KWDump Sep. 2015 so IgnoreCase
                    cause = "Dump (Insert.)";
                } else if ("HighPowerDump".equals(type)) {
                    cause = "Dump (Station.)";
                }
            }
        }

        /*Let's get more specific on RF*/
        if ("RF".equals(cause)) {
            cause = "RF (Multi/Other)";
            if (cedTypeSet.size() == 1) { // If only one type cause...
                String type = cedTypeSet.iterator().next();
                if ("RFSeparator".equals(type)) {
                    cause = "RF (Separator)";
                } else if ("CryoModule".equals(type)) {
                    /*It is definately a CryoModule, but may be more than one...*/

                    //cause = "RF (Cryomodule)";
                    if (cedNameSet.size() == 1) { // If only one name cause...
                        cause = "RF (C25/C50)";
                        String name = cedNameSet.iterator().next();
                        if (c100Names.contains(name)) {
                            cause = "RF (C100)";
                        }
                    } else if (!cedNameSet.isEmpty()) { // If multiple
                        int c100Count = 0;
                        int c25_50Count = 0;

                        /*Let's count how many of each and if all of one kind then use it; otherwise stay RF (Multi/Other)*/
                        for (String name : cedNameSet) {
                            if (c100Names.contains(name)) {
                                c100Count++;
                            } else {
                                c25_50Count++;
                            }
                        }

                        if (c100Count == 0 && c25_50Count > 0) {
                            cause = "RF (C25/C50)";
                        } else if (c100Count > 0 && c25_50Count == 0) {
                            cause = "RF (C100)";
                        }
                    }
                }
            } else if (cedTypeSet.contains("CryoModule")) { // Jay wants to minimize "RF (Multi/Other)" so if any cryomodule use that, and if any c100 in particular use that.
                int c100Count = 0;
                int c25_50Count = 0;

                for (String name : cedNameSet) {
                    if (c100Names.contains(name)) {
                        c100Count++;
                    } else if (otherNames.contains(name)) {
                        c25_50Count++;
                    }
                }

                if (c100Count == 0 && c25_50Count > 0) {
                    cause = "RF (C25/C50)";
                } else if (c100Count > 0 && c25_50Count == 0) {
                    cause = "RF (C100)";
                }
            }
        }

        /*Let's rename Safety Systems and get more detailed */
        if ("Safety Systems".equals(cause)) {
            cause = "MPS (Multi/Other)"; // Simple rename (HCO PSS system likely won't cause trip so MPS is more accurate)

            // Ignore DiffuserCard DFHLC3A echo trip
            if(cedTypeSet.size() > 1 && cedNameSet.remove("DFHLC3A")) {
                // We removed DFHLC3A so remove DiffuserCard Type
                cedTypeSet.remove("DiffuserCard");
            }

            if (cedTypeSet.size() == 1) { // If only one type cause...
                String type = cedTypeSet.iterator().next();
                if ("BeamLossMonitor".equals(type)) {
                    cause = "MPS (BLM)";
                } else if ("BCM".equals(type)) {
                    cause = "MPS (BCM/BLA)";
                } else if("IonChamber".equals(type)) {
                    cause = "MPS (IC)";
                }
            }
        }

        if ("Hall A".equals(cause) || "Hall B".equals(cause) || "Hall C".equals(cause)
                || "Hall D".equals(cause)) {
            cause = "Hall";
        }

        // Last chance to try to make Unknown/Missing more meaningful / check for Hall stuff / Rasters
        if ("Unknown/Missing".equals(cause) || "Other".equals(cause)) {
            if (cedTypeSet.size() == 1) {
                String type = cedTypeSet.iterator().next();
                if ("Target".equals(type) || "Hall".equals(type)) {
                    cause = "Hall";
                }
            }

            if (cedNameSet.size() == 1) { // This has CED type Radiator, but not sure if there are others outside of hall...
                String name = cedNameSet.iterator().next();
                if ("IARAD00".equals(name)) {
                    cause = "Hall";
                }
            }

            // We should look for Rasters here
            if (cedTypeSet.size() == 2) {
                if (cedTypeSet.contains("VDiagKicker") && cedTypeSet.contains("HDiagKicker")) {
                    cause = "Magnets";
                }
            }
        }

        // Something has gone wrong (probably due to HCO_SYSTEM_NAME = None)...
        if (cause == null) {
            cause = "Unknown/Missing";
        }

        return cause;
    }

    public void setRootCauseExcludeSecondary(FsdTrip trip) {
        List<FsdFault> faultList = new ArrayList<>();

        if (trip.getFaultMap() != null) {
            for (FsdFault fault : trip.getFaultMap().values()) {
                if (!fault.isDisjoint()) {
                    faultList.add(fault);
                }
            }
        }

        setRootCause(trip, faultList);
    }

    public void setRootCauseIncludeSecondary(FsdTrip trip) {
        List<FsdFault> faultList = new ArrayList<>();
        if (trip.getFaultMap() != null) {
            faultList.addAll(trip.getFaultMap().values());
        }
        setRootCause(trip, faultList);
    }

    private void setRootCause(FsdTrip trip, List<FsdFault> fsdFaults) {
        Set<String> cedNameSet = new LinkedHashSet<>();
        Set<String> cedTypeSet = new LinkedHashSet<>();
        Set<String> categorySet = new LinkedHashSet<>();

        if (fsdFaults != null) {
            for (FsdFault fault : fsdFaults) {
                if (fault.getDeviceMap() != null) {
                    for (FsdDevice device : fault.getDeviceMap().values()) {
                        cedNameSet.add(device.getCedName());
                        cedTypeSet.add(device.getCedType());
                        categorySet.add(device.getCategory());
                    }
                }
            }
        }

        String rootCause = getRootCause(cedNameSet, cedTypeSet, categorySet);

        trip.setRootCause(rootCause);
    }
}
