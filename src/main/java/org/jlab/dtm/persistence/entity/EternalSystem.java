package org.jlab.dtm.persistence.entity;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.List;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;
import org.hibernate.envers.RelationTargetAuditMode;
import org.jlab.smoothness.persistence.util.YnStringToBoolean;

/**
 * @author ryans
 */
@Entity
@Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
@Table(
    name = "ALL_SYSTEMS",
    schema = "DTM_OWNER",
    uniqueConstraints = {@UniqueConstraint(columnNames = {"NAME"})})
public class EternalSystem implements Serializable, Comparable<EternalSystem> {
  private static final long serialVersionUID = 1L;

  @Id
  @Basic(optional = false)
  @NotNull
  @Column(name = "SYSTEM_ID", nullable = false, precision = 22, scale = 0)
  private BigInteger systemId;

  @Basic(optional = false)
  @NotNull
  @Size(min = 1, max = 128)
  @Column(nullable = false, length = 128)
  private String name;

  private BigInteger weight;

  @OneToMany(cascade = CascadeType.ALL, mappedBy = "system")
  private List<Incident> incidentList;

  @JoinColumn(name = "CATEGORY_ID", referencedColumnName = "CATEGORY_ID")
  @ManyToOne(fetch = FetchType.LAZY)
  private Category category;

  @OneToMany(cascade = CascadeType.ALL, mappedBy = "system")
  private List<EternalComponent> componentList;

  @NotAudited
  @OneToMany(cascade = CascadeType.ALL, mappedBy = "system")
  private List<SystemExpert> systemExpertList;

  @Basic
  @Column(name = "SRM_YN", nullable = false, length = 1)
  @Convert(converter = YnStringToBoolean.class)
  private boolean inSrm;

  public EternalSystem() {}

  public EternalSystem(BigInteger systemId) {
    this.systemId = systemId;
  }

  public EternalSystem(BigInteger systemId, String name) {
    this.systemId = systemId;
    this.name = name;
  }

  public BigInteger getSystemId() {
    return systemId;
  }

  public void setSystemId(BigInteger systemId) {
    this.systemId = systemId;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public BigInteger getWeight() {
    return weight;
  }

  public void setWeight(BigInteger weight) {
    this.weight = weight;
  }

  public List<Incident> getIncidentList() {
    return incidentList;
  }

  public void setIncidentList(List<Incident> incidentList) {
    this.incidentList = incidentList;
  }

  public Category getCategory() {
    return category;
  }

  public void setCategory(Category category) {
    this.category = category;
  }

  public List<EternalComponent> getComponentList() {
    return componentList;
  }

  public void setComponentList(List<EternalComponent> componentList) {
    this.componentList = componentList;
  }

  public List<SystemExpert> getSystemExpertList() {
    return systemExpertList;
  }

  public void setSystemExpertList(List<SystemExpert> systemExpertList) {
    this.systemExpertList = systemExpertList;
  }

  public boolean isSrmSystem() {
    return inSrm;
  }

  @Override
  public int compareTo(EternalSystem c) {
    return getName().compareTo(c.getName()); // TODO: look at weight as well
  }

  @Override
  public int hashCode() {
    int hash = 0;
    hash += (systemId != null ? systemId.hashCode() : 0);
    return hash;
  }

  @Override
  public boolean equals(Object object) {
    // TODO: Warning - this method won't work in the case the id fields are not set
    if (!(object instanceof EternalSystem)) {
      return false;
    }
    EternalSystem other = (EternalSystem) object;
    return (this.systemId != null || other.systemId == null)
        && (this.systemId == null || this.systemId.equals(other.systemId));
  }

  @Override
  public String toString() {
    return "org.jlab.dtm.persistence.entity.EternalSystem[ systemId=" + systemId + " ]";
  }
}
