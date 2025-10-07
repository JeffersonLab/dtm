package org.jlab.dtm.persistence.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;
import org.jlab.dtm.persistence.enumeration.ReviewLevel;
import org.jlab.dtm.persistence.enumeration.SystemExpertAcknowledgement;
import org.jlab.smoothness.business.util.IOUtil;
import org.jlab.smoothness.presentation.util.Functions;

/**
 * @author ryans
 */
@Entity
@Audited
@Table(schema = "DTM_OWNER")
@NamedQueries({@NamedQuery(name = "Incident.findAll", query = "SELECT i FROM Incident i")})
public class Incident implements Serializable {

  private static final double MILLIS_PER_HOUR = 3600000;

  private static final long serialVersionUID = 1L;

  @Id
  @SequenceGenerator(name = "IncidentId", sequenceName = "INCIDENT_ID", allocationSize = 1)
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "IncidentId")
  @Basic(optional = false)
  @NotNull
  @Column(name = "INCIDENT_ID", nullable = false, precision = 22, scale = 0)
  private BigInteger incidentId;

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

  @JoinColumn(name = "EVENT_ID", referencedColumnName = "EVENT_ID", nullable = false)
  @ManyToOne(optional = false)
  private Event event;

  @JoinColumn(name = "COMPONENT_ID", referencedColumnName = "COMPONENT_ID")
  @ManyToOne
  private Component component;

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

  @OneToMany(cascade = CascadeType.ALL, mappedBy = "incident", fetch = FetchType.LAZY)
  @NotAudited
  private List<Repair> repairedByList;

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

  @OneToMany(cascade = CascadeType.ALL, mappedBy = "incident")
  @NotAudited
  private List<IncidentReview> incidentReviewList;

  @Column(name = "DURATION_SECONDS", updatable = false, insertable = false)
  private Long durationSeconds;

  @Column(name = "REVIEWED", updatable = false, insertable = false)
  private String reviewed;

  @Column(name = "REVIEW_LEVEL", updatable = false, insertable = false)
  @Enumerated(EnumType.STRING)
  private ReviewLevel level;

  @Size(max = 12)
  @Column(name = "RAR_EXT", nullable = true, length = 12)
  private String rarExt;

  @Column(name = "RAR_UPLOADED_DATE")
  @Temporal(TemporalType.TIMESTAMP)
  private Date rarUploadedDate;

  @Size(max = 64)
  @Column(name = "PERMIT_TO_WORK", nullable = true, length = 64)
  private String permitToWork;

  public Incident() {}

  public String getRootCause() {
    return rootCause;
  }

  public void setRootCause(String rootCause) {
    this.rootCause = rootCause;
  }

  public String getReviewedUsername() {
    return reviewedUsername;
  }

  public void setReviewedUsername(String reviewedUsername) {
    this.reviewedUsername = reviewedUsername;
  }

  public BigInteger getIncidentId() {
    return incidentId;
  }

  public void setIncidentId(BigInteger incidentId) {
    this.incidentId = incidentId;
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

  public Event getEvent() {
    return event;
  }

  public void setEvent(Event event) {
    this.event = event;
  }

  public Component getComponent() {
    return component;
  }

  public void setComponent(Component component) {
    this.component = component;
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

  public List<Repair> getRepairedByList() {
    return repairedByList;
  }

  public void setRepairedByList(List<Repair> repairedByList) {
    this.repairedByList = repairedByList;
  }

  public Long getDurationSeconds() {
    return durationSeconds;
  }

  public void setRarExt(String rarExt) {
    this.rarExt = rarExt;
  }

  public String getRarExt() {
    return rarExt;
  }

  public String getPermitToWork() {
    return permitToWork;
  }

  public void setPermitToWork(String permitToWork) {
    this.permitToWork = permitToWork;
  }

  public long getElapsedMillis() {
    Date tu = timeUp;

    if (tu == null) {
      tu = new Date();
    }

    return tu.getTime() - getTimeDown().getTime();
  }

  public ReviewLevel getReviewLevel() {
    return level;
  }

  public String getReviewLevelString() {

    final long thirtyMinutesInMillis = 30 * 60000;
    final long fourHoursInMillis = 4 * 3600000;

    String level = "Unknown";

    if (timeUp != null) {
      long durationMillis = timeUp.getTime() - timeDown.getTime();

      if (durationMillis < thirtyMinutesInMillis) {
        level = "Level Ⅰ";
      } else if (durationMillis < fourHoursInMillis) {
        level = "Level Ⅱ";
      } else {
        level = "Level Ⅲ+";
      }
    }

    return level;
  }

  public String getRepairedByIdCsv() {
    String csv;

    if (repairedByList != null) {
      BigInteger[] idArray = new BigInteger[repairedByList.size()];

      for (int i = 0; i < idArray.length; i++) {
        Repair repair = repairedByList.get(i);
        Workgroup group = repair.getRepairedBy();
        idArray[i] = group.getWorkgroupId();
      }

      csv = IOUtil.toCsv(idArray);
    } else {
      csv = "";
    }

    return csv;
  }

  public String getReviewedByUsernameSsv() {
    String ssv;

    if (incidentReviewList != null) {
      String[] usernameArray = new String[incidentReviewList.size()];

      for (int i = 0; i < usernameArray.length; i++) {
        IncidentReview review = incidentReviewList.get(i);
        String reviewer = review.getReviewer();
        usernameArray[i] = reviewer;
      }

      ssv = IOUtil.toSsv(usernameArray);
    } else {
      ssv = "";
    }

    return ssv;
  }

  public String getReviewedByExpertsFormattedTsv() {
    return getExpertsFormattedTsv(incidentReviewList);
  }

  public static String getExpertsFormattedTsv(List<IncidentReview> reviewList) {
    String value = "";

    if (reviewList != null && !reviewList.isEmpty()) {

      reviewList.sort(
          new Comparator<IncidentReview>() {
            @Override
            public int compare(IncidentReview o1, IncidentReview o2) {
              return o1.getReviewer().compareTo(o2.getReviewer());
            }
          });

      value = Functions.formatUsername(reviewList.get(0).getReviewer());

      for (int i = 1; i < reviewList.size(); i++) {
        value = value + "\t" + Functions.formatUsername(reviewList.get(i).getReviewer());
      }
    }

    return value;
  }

  @Override
  public int hashCode() {
    int hash = 0;
    hash += (incidentId != null ? incidentId.hashCode() : 0);
    return hash;
  }

  @Override
  public boolean equals(Object object) {
    // TODO: Warning - this method won't work in the case the id fields are not set
    if (!(object instanceof Incident)) {
      return false;
    }
    Incident other = (Incident) object;
    return (this.incidentId != null || other.incidentId == null)
        && (this.incidentId == null || this.incidentId.equals(other.incidentId));
  }

  @Override
  public String toString() {
    return "Incident{"
        + "incidentId="
        + incidentId
        + ", timeDown="
        + timeDown
        + ", timeUp="
        + timeUp
        + ", title="
        + title
        + ", system="
        + system
        + ", component="
        + component
        + '}';
  }

  public BigInteger getRarId() {
    return rarId;
  }

  public void setRarId(BigInteger rarId) {
    this.rarId = rarId;
  }

  public SystemExpertAcknowledgement getExpertAcknowledged() {
    return expertAcknowledged;
  }

  public void setExpertAcknowledged(SystemExpertAcknowledgement expertAcknowledged) {
    this.expertAcknowledged = expertAcknowledged;
  }

  public List<IncidentReview> getIncidentReviewList() {
    return incidentReviewList;
  }

  public void setIncidentReviewList(List<IncidentReview> incidentReviewList) {
    this.incidentReviewList = incidentReviewList;
  }

  public Date getRarUploadedDate() {
    return rarUploadedDate;
  }

  public void setRarUploadedDate(Date rarUploadedDate) {
    this.rarUploadedDate = rarUploadedDate;
  }

  public String getReviewed() {
    return reviewed;
  }

  /* See "getReviewed()" instead, which uses a virtual (computed) column in Oracle */
  public boolean isExpertReviewed() {
    boolean reviewed = false;

    String level = this.getReviewLevelString();

    switch (level) {
      case "Level Ⅲ+":
        if (rarExt != null) {
          reviewed = true;
        }
        break;
      case "Level Ⅱ":
        if (rootCause != null && !rootCause.isEmpty()) {
          reviewed = true;
        }
        break;
      case "Level Ⅰ":
        if (SystemExpertAcknowledgement.Y == expertAcknowledged) {
          reviewed = true;
        }
    }

    return reviewed;
  }

  public double getDowntimeHours() {
    // interval_to_seconds(coalesce(a.time_up, sysdate) - a.time_down) / 60 / 60 as
    // unbounded_duration_hours

    double millis = this.getElapsedMillis();

    return millis / MILLIS_PER_HOUR;
  }

  public double getDowntimeHoursBounded(Date start, Date end) {
    // cast(greatest(a.time_down, :start) as date) as time_down_bounded, ";
    // cast(least(coalesce(a.time_up, sysdate), :end) as date) as time_up_bounded, ";

    double hours;

    if (start != null && end != null) {
      long startMillis = Math.max(timeDown.getTime(), start.getTime());

      Date tu = timeUp;
      if (tu == null) {
        tu = new Date();
      }

      long endMillis = Math.min(tu.getTime(), end.getTime());

      double millis = endMillis - startMillis;
      hours = millis / MILLIS_PER_HOUR;
    } else {
      hours = getDowntimeHours();
    }
    return hours;
  }
}
