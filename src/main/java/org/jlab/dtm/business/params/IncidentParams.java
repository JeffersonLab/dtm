package org.jlab.dtm.business.params;

import java.math.BigInteger;
import java.util.Date;
import org.jlab.dtm.business.session.AbstractFacade.OrderDirective;
import org.jlab.dtm.persistence.enumeration.IncidentSortKey;
import org.jlab.dtm.persistence.enumeration.ReviewLevel;
import org.jlab.dtm.persistence.enumeration.RootCauseIncidentMask;
import org.jlab.dtm.persistence.enumeration.SystemExpertAcknowledgement;

public class IncidentParams extends PaginationParams {

  private Date start;
  private Date end;
  private BigInteger eventTypeId;
  private Boolean beamTransport;
  private BigInteger eventId;
  private BigInteger incidentId;
  private String title;
  private SystemExpertAcknowledgement acknowledgement;
  private Boolean reviewed;
  private ReviewLevel level;
  private String smeUsername;
  private IncidentSortKey sort;
  private RootCauseIncidentMask mask;
  private boolean dateRangeForUploaded = false;
  private Boolean hasAttachment;

  private Boolean closedOnly = false;

  public Boolean getClosedOnly() {
    return closedOnly;
  }

  public void setClosedOnly(Boolean closedOnly) {
    this.closedOnly = closedOnly;
  }

  public Date getStart() {
    return start;
  }

  public void setStart(Date start) {
    this.start = start;
  }

  public Date getEnd() {
    return end;
  }

  public void setEnd(Date end) {
    this.end = end;
  }

  public BigInteger getEventTypeId() {
    return eventTypeId;
  }

  public void setEventTypeId(BigInteger eventTypeId) {
    this.eventTypeId = eventTypeId;
  }

  public Boolean getBeamTransport() {
    return beamTransport;
  }

  public void setBeamTransport(Boolean beamTransport) {
    this.beamTransport = beamTransport;
  }

  public Boolean getReviewed() {
    return reviewed;
  }

  public void setReviewed(Boolean reviewed) {
    this.reviewed = reviewed;
  }

  public BigInteger getEventId() {
    return eventId;
  }

  public void setEventId(BigInteger eventId) {
    this.eventId = eventId;
  }

  public BigInteger getIncidentId() {
    return incidentId;
  }

  public void setIncidentId(BigInteger incidentId) {
    this.incidentId = incidentId;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public SystemExpertAcknowledgement getAcknowledgement() {
    return acknowledgement;
  }

  public void setAcknowledgement(SystemExpertAcknowledgement acknowledgement) {
    this.acknowledgement = acknowledgement;
  }

  public String getSmeUsername() {
    return smeUsername;
  }

  public void setSmeUsername(String smeUsername) {
    this.smeUsername = smeUsername;
  }

  public ReviewLevel getLevel() {
    return level;
  }

  public void setLevel(ReviewLevel level) {
    this.level = level;
  }

  public RootCauseIncidentMask getMask() {
    return mask;
  }

  public void setMask(RootCauseIncidentMask mask) {
    this.mask = mask;
  }

  public IncidentSortKey getSort() {
    return sort;
  }

  public void setSort(IncidentSortKey sort) {
    this.sort = sort;
  }

  public boolean isDateRangeForUploaded() {
    return dateRangeForUploaded;
  }

  public void setDateRangeForUploaded(boolean dateRangeForUploaded) {
    this.dateRangeForUploaded = dateRangeForUploaded;
  }

  public Boolean getHasAttachment() {
    return hasAttachment;
  }

  public void setHasAttachment(Boolean hasAttachment) {
    this.hasAttachment = hasAttachment;
  }

  public OrderDirective[] getOrderDirectives() {
    OrderDirective order;

    if (sort == null) {
      order = new OrderDirective("durationSeconds", false);
    } else {
      switch (sort) {
        case TIME_DOWN:
          order = new OrderDirective("timeDown", false);
          break;
        case RAR_UPLOADED:
          order = new OrderDirective("rarUploadedDate", false);
          break;
        default:
          order = new OrderDirective("durationSeconds", false);
      }
    }

    return new OrderDirective[] {order};
  }
}
