package org.jlab.dtm.persistence.enumeration;

public enum AccMachineState {
    NULL("PHYSICS"), OFF("OFF"), DOWN("DOWN"), ACC("ACC"), RESTORE("RESTORE"), MD("STUDIES");
    
    String label;
    
    AccMachineState(String label) {
        this.label = label;
    }
    
    public String getLabel() {
        return label;
    }    
    
    public static AccMachineState valueOfAllowNull(String value) {
        if(value == null) {
            return null;
        } else {
            return AccMachineState.valueOf(value);
        }
    }
}
