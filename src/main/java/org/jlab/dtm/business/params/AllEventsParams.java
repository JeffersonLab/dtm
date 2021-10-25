package org.jlab.dtm.business.params;

import java.math.BigInteger;
import java.util.Date;
import org.jlab.dtm.persistence.enumeration.SystemExpertAcknowledgement;

public class AllEventsParams extends PaginationParams {

    private Date start;
    private Date end;
    private BigInteger eventTypeId;
    private Boolean beamTransport;
    private BigInteger eventId;
    private BigInteger[] incidentIdArray;
    private SystemExpertAcknowledgement acknowledgement;
    private String smeUsername;

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

    public BigInteger getEventId() {
        return eventId;
    }

    public void setEventId(BigInteger eventId) {
        this.eventId = eventId;
    }

    public BigInteger[] getIncidentIdArray() {
        return incidentIdArray;
    }

    public void setIncidentIdArray(BigInteger[] incidentIdArray) {
        this.incidentIdArray = incidentIdArray;
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
}
