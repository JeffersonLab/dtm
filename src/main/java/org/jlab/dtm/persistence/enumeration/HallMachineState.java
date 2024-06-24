package org.jlab.dtm.persistence.enumeration;

public enum HallMachineState {
  OFF("OFF"),
  DOWN("DOWN"),
  TUNE("TUNE"),
  BANU("BANU"),
  UP("UP");

  String label;

  HallMachineState(String label) {
    this.label = label;
  }

  public String getLabel() {
    return label;
  }

  public static HallMachineState valueOfAllowNull(String value) {
    if (value == null) {
      return null;
    } else {
      return HallMachineState.valueOf(value);
    }
  }
}
