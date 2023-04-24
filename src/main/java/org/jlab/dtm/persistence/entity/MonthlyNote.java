package org.jlab.dtm.persistence.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 *
 * @author ryans
 */
@Entity
@Table(name = "MONTHLY_NOTE", schema = "DTM_OWNER", uniqueConstraints
        = {
            @UniqueConstraint(columnNames = {"MONTH"})})
@NamedQueries({
    @NamedQuery(name = "MonthlyNote.findAll", query = "SELECT m FROM MonthlyNote m")})
public class MonthlyNote implements Serializable {

    private static final long serialVersionUID = 1L;
    // @Max(value=?)  @Min(value=?)//if you know range of your decimal fields consider using these annotations to enforce field validation
    @Id
    @SequenceGenerator(name = "NoteId", sequenceName = "NOTE_ID", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "NoteId")
    @Basic(optional = false)
    @NotNull
    @Column(name = "NOTE_ID", nullable = false, precision = 22, scale = 0)
    private BigDecimal noteId;
    @Basic(optional = false)
    @NotNull
    @Column(nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date month;
    @Size(max = 3500)
    @Column(length = 3500)
    private String note;
    @Column(name = "MACHINE_GOAL")
    private Float machineGoal;
    @Column(name = "TRIP_GOAL")
    private Float tripGoal;
    @Column(name = "EVENT_GOAL")
    private Float eventGoal;
    
    public MonthlyNote() {
    }

    public MonthlyNote(BigDecimal noteId) {
        this.noteId = noteId;
    }

    public MonthlyNote(BigDecimal noteId, Date month) {
        this.noteId = noteId;
        this.month = month;
    }

    public BigDecimal getNoteId() {
        return noteId;
    }

    public void setNoteId(BigDecimal noteId) {
        this.noteId = noteId;
    }

    public Date getMonth() {
        return month;
    }

    public void setMonth(Date month) {
        this.month = month;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public Float getMachineGoal() {
        return machineGoal;
    }

    public void setMachineGoal(Float machineGoal) {
        this.machineGoal = machineGoal;
    }

    public Float getTripGoal() {
        return tripGoal;
    }

    public void setTripGoal(Float tripGoal) {
        this.tripGoal = tripGoal;
    }

    public Float getEventGoal() {
        return eventGoal;
    }

    public void setEventGoal(Float eventGoal) {
        this.eventGoal = eventGoal;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (noteId != null ? noteId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof MonthlyNote)) {
            return false;
        }
        MonthlyNote other = (MonthlyNote) object;
        return (this.noteId != null || other.noteId == null) &&
                (this.noteId == null || this.noteId.equals(other.noteId));
    }

    @Override
    public String toString() {
        return "org.jlab.dtm.persistence.entity.MonthlyNote[ noteId=" + noteId + " ]";
    }
    
}
