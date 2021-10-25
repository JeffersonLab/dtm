package org.jlab.dtm.persistence.enumeration;

/**
 *
 * @author ryans
 */
public enum RootCauseIncidentMask {
    NONE("All"),
    DEADBEATS("Not Reviewed"),
    LEVEL_ONE("Level Ⅰ"),
    LEVEL_TWO("Level Ⅱ"),
    LEVEL_THREE_PLUS("Level Ⅲ+");
    
    private final String label;
    
    RootCauseIncidentMask(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
