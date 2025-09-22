package org.jlab.dtm.persistence.entity.aud;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Date;
import java.util.Objects;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import org.hibernate.envers.RevisionType;
import org.jlab.dtm.persistence.entity.ApplicationRevisionInfo;
import org.jlab.dtm.persistence.entity.EternalComponent;
import org.jlab.dtm.persistence.entity.SystemEntity;
import org.jlab.dtm.persistence.enumeration.SystemExpertAcknowledgement;
import org.jlab.smoothness.persistence.util.YnStringToBoolean;

/**
 * @author ryans
 */
@Entity
@Table(name = "INCIDENT_AUD", schema = "DTM_OWNER")
public class IncidentAud implements Serializable {

  private static final long serialVersionUID = 1L;
  @EmbeddedId protected IncidentAudPK incidentAudPK;

  @Basic(optional = false)
  @NotNull
  @Column(name = "TIME_DOWN", nullable = false)
  @Temporal(TemporalType.TIMESTAMP)
  private Date timeDown;

  @Column(name = "TIME_UP")
  @Temporal(TemporalType.TIMESTAMP)
  private Date timeUp;

  @Basic(optional = false)
  @NotNull
  @Size(min = 1, max = 128)
  @Column(nullable = false, length = 128)
  private String title;

  @JoinColumn(name = "SYSTEM_ID", referencedColumnName = "SYSTEM_ID", nullable = false)
  @ManyToOne(optional = false)
  private SystemEntity system;

  @JoinColumn(name = "COMPONENT_ID", referencedColumnName = "COMPONENT_ID")
  @ManyToOne
  private EternalComponent component;

  @Enumerated(EnumType.ORDINAL)
  @NotNull
  @Column(name = "REVTYPE")
  private RevisionType type;

  @JoinColumn(
      name = "REV",
      referencedColumnName = "REV",
      insertable = false,
      updatable = false,
      nullable = false)
  @NotNull
  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  private ApplicationRevisionInfo revision;

  @Basic(optional = false)
  @NotNull
  @Column(name = "EVENT_ID", nullable = false, precision = 22, scale = 0)
  private BigInteger eventId;

  @Size(min = 1, max = 2048)
  @Column(name = "MISSING_EXPLANATION", nullable = true, length = 2048)
  private String explanation;

  @Size(min = 1, max = 2048)
  @Column(name = "SUMMARY", nullable = true, length = 2048)
  private String summary;

  @Size(max = 2048)
  @Column(name = "RESOLUTION", nullable = true, length = 2048)
  private String resolution;

  @Column(name = "REVIEWED_USERNAME", nullable = true, length = 64)
  private String reviewedUsername;

  @Size(max = 512)
  @Column(name = "ROOT_CAUSE", nullable = true, length = 512)
  private String rootCause;

  @Column(name = "RAR_ID")
  private BigInteger rarId;

  @Basic(optional = false)
  @NotNull
  @Column(name = "EXPERT_ACKNOWLEDGED", nullable = false)
  @Enumerated(EnumType.STRING)
  private SystemExpertAcknowledgement expertAcknowledged;

  @Size(max = 64)
  @Column(name = "PERMIT_TO_WORK", nullable = true, length = 64)
  private String permitToWork;

  @Basic
  @Column(name = "RESEARCH_YN", nullable = false, length = 1)
  @Convert(converter = YnStringToBoolean.class)
  private boolean research;

  public IncidentAud() {}

  public String getRootCause() {
    return rootCause;
  }

  public void setRootCause(String rootCause) {
    this.rootCause = rootCause;
  }

  public String getReviewedUsername() {
    return reviewedUsername;
  }

  public void setReviewedBy(String reviewedUsername) {
    this.reviewedUsername = reviewedUsername;
  }

  public Date getTimeDown() {
    return timeDown;
  }

  public void setTimeDown(Date timeDown) {
    this.timeDown = timeDown;
  }

  public Date getTimeUp() {
    return timeUp;
  }

  public void setTimeUp(Date timeUp) {
    this.timeUp = timeUp;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public SystemEntity getSystem() {
    return system;
  }

  public void setSystem(SystemEntity system) {
    this.system = system;
  }

  public EternalComponent getComponent() {
    return component;
  }

  public void setComponent(EternalComponent component) {
    this.component = component;
  }

  public RevisionType getType() {
    return type;
  }

  public void setType(RevisionType type) {
    this.type = type;
  }

  public ApplicationRevisionInfo getRevision() {
    return revision;
  }

  public void setRevision(ApplicationRevisionInfo revision) {
    this.revision = revision;
  }

  public BigInteger getEventId() {
    return eventId;
  }

  public void setEventId(BigInteger eventId) {
    this.eventId = eventId;
  }

  public String getExplanation() {
    return explanation;
  }

  public void setExplanation(String explanation) {
    this.explanation = explanation;
  }

  public String getSummary() {
    return summary;
  }

  public void setSummary(String summary) {
    this.summary = summary;
  }

  public String getResolution() {
    return resolution;
  }

  public void setResolution(String resolution) {
    this.resolution = resolution;
  }

  public BigInteger getRarId() {
    return rarId;
  }

  public void setRarId(BigInteger rarId) {
    this.rarId = rarId;
  }

  public String getPermitToWork() {
    return permitToWork;
  }

  public void setPermitToWork(String permitToWork) {
    this.permitToWork = permitToWork;
  }

  public boolean isResearch() {
    return research;
  }

  public void setResearch(boolean research) {
    this.research = research;
  }

  public SystemExpertAcknowledgement getExpertAcknowledged() {
    return expertAcknowledged;
  }

  public void setExpertAcknowledged(SystemExpertAcknowledgement expertAcknowledged) {
    this.expertAcknowledged = expertAcknowledged;
  }

  public IncidentAudPK getIncidentAudPK() {
    return incidentAudPK;
  }

  public void setIncidentAudPK(IncidentAudPK incidentAudPK) {
    this.incidentAudPK = incidentAudPK;
  }

  @Override
  public int hashCode() {
    int hash = 3;
    hash = 17 * hash + (this.incidentAudPK != null ? this.incidentAudPK.hashCode() : 0);
    return hash;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final IncidentAud other = (IncidentAud) obj;
    return Objects.equals(this.incidentAudPK, other.incidentAudPK);
  }
}
