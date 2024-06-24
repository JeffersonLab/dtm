package org.jlab.dtm.persistence.enumeration;

/**
 * @author ryans
 */
public enum BulletinBoardPriority {
  HIGH(2),
  MEDIUM(1),
  LOW(0);

  int priorityNumber;

  BulletinBoardPriority(int priorityNumber) {
    this.priorityNumber = priorityNumber;
  }

  public int getPriorityNumber() {
    return priorityNumber;
  }
}
