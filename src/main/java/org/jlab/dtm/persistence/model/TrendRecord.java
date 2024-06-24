package org.jlab.dtm.persistence.model;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author ryans
 */
public class TrendRecord {

  public Date bin;
  public double periodHours;
  public double programHours;
  public double accDownHours;
  public double eventHours;
  public long eventCount;
  public double eventMttrHours;
  public double tripUptimeHours;
  public double eventUptimeHours;
  public double accUptimeHours;
  public double eventMtbfHours;
  public double accAvailability;
  public double tripAvailability;
  public double eventAvailability;
  public double tripHours;
  public long tripCount;
  public double tripMttrHours;
  public double mtbtHours;
  public List<CategoryDowntime> categoryDowntimeList;
  public List<CategoryDowntime> missingList;
  public Map<Long, CategoryDowntime> downtimeMap;

  public Map<Long, CategoryDowntime> getDowntimeMap() {
    return downtimeMap;
  }

  public void setDowntimeMap(Map<Long, CategoryDowntime> downtimeMap) {
    this.downtimeMap = downtimeMap;
  }

  public Date getBin() {
    return bin;
  }

  public void setBin(Date bin) {
    this.bin = bin;
  }

  public double getPeriodHours() {
    return periodHours;
  }

  public void setPeriodHours(double periodHours) {
    this.periodHours = periodHours;
  }

  public double getProgramHours() {
    return programHours;
  }

  public void setProgramHours(double programHours) {
    this.programHours = programHours;
  }

  public double getAccDownHours() {
    return accDownHours;
  }

  public void setAccDownHours(double accDownHours) {
    this.accDownHours = accDownHours;
  }

  public double getEventUptimeHours() {
    return eventUptimeHours;
  }

  public void setEventUptimeHours(double eventUptimeHours) {
    this.eventUptimeHours = eventUptimeHours;
  }

  public double getTripUptimeHours() {
    return tripUptimeHours;
  }

  public void setTripUptimeHours(double tripUptimeHours) {
    this.tripUptimeHours = tripUptimeHours;
  }

  public double getEventHours() {
    return eventHours;
  }

  public void setEventHours(double eventHours) {
    this.eventHours = eventHours;
  }

  public long getEventCount() {
    return eventCount;
  }

  public void setEventCount(long eventCount) {
    this.eventCount = eventCount;
  }

  public double getEventMttrHours() {
    return eventMttrHours;
  }

  public void setEventMttrHours(double eventMttrHours) {
    this.eventMttrHours = eventMttrHours;
  }

  public double getAccUptimeHours() {
    return accUptimeHours;
  }

  public void setAccUptimeHours(double accUptimeHours) {
    this.accUptimeHours = accUptimeHours;
  }

  public double getEventMtbfHours() {
    return eventMtbfHours;
  }

  public void setEventMtbfHours(double eventMtbfHours) {
    this.eventMtbfHours = eventMtbfHours;
  }

  public double getAccAvailability() {
    return accAvailability;
  }

  public void setAccAvailability(double accAvailability) {
    this.accAvailability = accAvailability;
  }

  public double getEventAvailability() {
    return eventAvailability;
  }

  public void setEventAvailability(double eventAvailability) {
    this.eventAvailability = eventAvailability;
  }

  public double getTripAvailability() {
    return tripAvailability;
  }

  public void setTripAvailability(double tripAvailability) {
    this.tripAvailability = tripAvailability;
  }

  public double getTripHours() {
    return tripHours;
  }

  public void setTripHours(double tripHours) {
    this.tripHours = tripHours;
  }

  public long getTripCount() {
    return tripCount;
  }

  public void setTripCount(long tripCount) {
    this.tripCount = tripCount;
  }

  public double getTripMttrHours() {
    return tripMttrHours;
  }

  public void setTripMttrHours(double tripMttrHours) {
    this.tripMttrHours = tripMttrHours;
  }

  public double getMtbtHours() {
    return mtbtHours;
  }

  public void setMtbtHours(double mtbtHours) {
    this.mtbtHours = mtbtHours;
  }

  public List<CategoryDowntime> getCategoryDowntimeList() {
    return categoryDowntimeList;
  }

  public void setCategoryDowntimeList(List<CategoryDowntime> categoryDowntimeList) {
    this.categoryDowntimeList = categoryDowntimeList;
  }

  public List<CategoryDowntime> getMissingList() {
    return missingList;
  }

  public void setMissingList(List<CategoryDowntime> missingList) {
    this.missingList = missingList;
  }
}
