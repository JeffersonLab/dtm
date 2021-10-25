package org.jlab.dtm.persistence.enumeration;

public enum SystemExpertAcknowledgement {
    N("No"), Y("Yes"), R("Reassign");
    
    String label;
    
    SystemExpertAcknowledgement(String label) {
        this.label = label;
    }
    
    public String getLabel() {
        return label;
    }    
    
    public static SystemExpertAcknowledgement valueOfAllowNull(String value) {
        if(value == null) {
            return null;
        } else {
            return SystemExpertAcknowledgement.valueOf(value);
        }
    }
}
