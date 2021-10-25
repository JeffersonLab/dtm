package org.jlab.dtm.persistence.entity;

import java.io.Serializable;
import java.math.BigInteger;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;

/**
 *
 * @author ryans
 */
@Entity
@Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
@Table(name = "EVENT_TYPE", schema = "DTM_OWNER")
@NamedQueries({
    @NamedQuery(name = "EventType.findAll", query = "SELECT e FROM EventType e")})
public class EventType implements Serializable {
    private static final long serialVersionUID = 1L;
    public static EventType ACC = new EventType(BigInteger.ONE);
    // @Max(value=?)  @Min(value=?)//if you know range of your decimal fields consider using these annotations to enforce field validation
    @Id
    @Basic(optional = false)
    @NotNull
    @Column(name = "EVENT_TYPE_ID", nullable = false, precision = 22, scale = 0)
    private BigInteger eventTypeId;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 32)
    @Column(nullable = false, length = 32)
    private String name;
    @Basic(optional = true)
    @Size(min = 0, max = 3)
    @Column(nullable = true, length = 3)
    private String abbreviation;    
    @Basic(optional = false)
    @NotNull
    @Column(nullable = false)
    private BigInteger weight;

    public EventType() {
    }

    public EventType(BigInteger eventTypeId) {
        this.eventTypeId = eventTypeId;
    }

    public BigInteger getEventTypeId() {
        return eventTypeId;
    }

    public void setEventTypeId(BigInteger eventTypeId) {
        this.eventTypeId = eventTypeId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAbbreviation() {
        return abbreviation;
    }

    public void setAbbreviation(String abbreviation) {
        this.abbreviation = abbreviation;
    }

    public BigInteger getWeight() {
        return weight;
    }

    public void setWeight(BigInteger weight) {
        this.weight = weight;
    }

    public String getShortName() {
        String shortName = name;
        
        if("Accelerator".equals(name)) {
            shortName = "Accel";
        }
        
        return shortName;
    }
    
    @Override
    public int hashCode() {
        int hash = 0;
        hash += (eventTypeId != null ? eventTypeId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof EventType)) {
            return false;
        }
        EventType other = (EventType) object;
        if ((this.eventTypeId == null && other.eventTypeId != null) || (this.eventTypeId != null && !this.eventTypeId.equals(other.eventTypeId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "org.jlab.dtm.persistence.entity.EventType[ eventTypeId=" + eventTypeId + " ]";
    }
    
}
