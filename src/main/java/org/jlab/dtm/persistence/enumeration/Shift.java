package org.jlab.dtm.persistence.enumeration;

import java.util.Calendar;
import java.util.Date;

public enum Shift {
  OWL("Owl"),
  DAY("Day"),
  SWING("Swing");

  private final String label;

  Shift(String label) {
    this.label = label;
  }

  public String getLabel() {
    return label;
  }

  public Shift getNext() {
    return this.ordinal() < Shift.values().length - 1
        ? Shift.values()[this.ordinal() + 1]
        : Shift.values()[0];
  }

  public Shift getPrevious() {
    return this.ordinal() > 0
        ? Shift.values()[this.ordinal() - 1]
        : Shift.values()[Shift.values().length - 1];
  }

  public static Shift getCcShiftFromDate(Date dateInShift) {
    Shift shift;

    Calendar cal = Calendar.getInstance();

    cal.setTime(dateInShift);

    int hour = cal.get(Calendar.HOUR_OF_DAY);

    if (hour == 23) {
      shift = Shift.OWL;
    } else if (hour <= 6) {
      shift = Shift.OWL;
    } else if (hour <= 14) {
      shift = Shift.DAY;
    } else {
      shift = Shift.SWING;
    }

    return shift;
  }
}
