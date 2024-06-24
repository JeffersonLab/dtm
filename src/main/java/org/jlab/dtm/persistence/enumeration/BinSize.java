package org.jlab.dtm.persistence.enumeration;

/**
 * @author ryans
 */
public enum BinSize {
  HOUR("Hour"),
  DAY("Day"),
  MONTH("Month");

  String label;

  BinSize(String label) {
    this.label = label;
  }

  public String getLabel() {
    return label;
  }
}
