package org.jlab.dtm.persistence.enumeration;

/**
 * @author ryans
 */
public enum RootCause {
  InsertableDump("Dump (Insert.)"),
  StationaryDump("Dump (Station.)"),
  BCM("MPS (BCM/BLA)"),
  BLM("MPS (BLM)"),
  IC("MPS (IC)"),
  OtherMPS("MPS (Multi/Other)"),
  Magnets("Magents"),
  Unknown("Unknown/Missing"),
  Vacuum("Vacuum"),
  OldRF("RF (C25/50)"),
  C100("RF (C75/100)"),
  OtherRF("RF (Multi/Other)"),
  Separator("RF (Separator)");

  String label;

  RootCause(String label) {
    this.label = label;
  }

  public String getLabel() {
    return label;
  }
}
