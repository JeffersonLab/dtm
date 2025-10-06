package org.jlab.dtm.persistence.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.List;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;
import org.jlab.smoothness.persistence.util.YnStringToBoolean;

/**
 * @author ryans
 */
@Entity
@Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
@Table(name = "COMPONENT", schema = "DTM_OWNER")
@NamedQueries({@NamedQuery(name = "Component.findAll", query = "SELECT c FROM Component c")})
public class Component implements Serializable {
  private static final long serialVersionUID = 1L;

  @Id
  @Basic(optional = false)
  @NotNull
  @Column(name = "COMPONENT_ID", nullable = false, precision = 22, scale = 0)
  private BigInteger componentId;

  @Basic(optional = false)
  @NotNull
  @Size(min = 1, max = 128)
  @Column(nullable = false, length = 128)
  private String name;

  @OneToMany(mappedBy = "component")
  private List<Incident> incidentList;

  @JoinColumn(name = "SYSTEM_ID", referencedColumnName = "SYSTEM_ID", nullable = false)
  @ManyToOne(optional = false)
  private org.jlab.dtm.persistence.entity.SystemEntity system;

  @JoinColumn(name = "REGION_ID", referencedColumnName = "REGION_ID", nullable = false)
  @ManyToOne(optional = false)
  private Region region;

  @Basic
  @Column(name = "ARCHIVED_YN", nullable = false, length = 1)
  @Convert(converter = YnStringToBoolean.class)
  private boolean archived;

  public Component() {}

  public Component(BigInteger componentId) {
    this.componentId = componentId;
  }

  public Component(BigInteger componentId, String name) {
    this.componentId = componentId;
    this.name = name;
  }

  public BigInteger getComponentId() {
    return componentId;
  }

  public void setComponentId(BigInteger componentId) {
    this.componentId = componentId;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public List<Incident> getIncidentList() {
    return incidentList;
  }

  public void setIncidentList(List<Incident> incidentList) {
    this.incidentList = incidentList;
  }

  public org.jlab.dtm.persistence.entity.SystemEntity getSystem() {
    return system;
  }

  public void setSystem(org.jlab.dtm.persistence.entity.SystemEntity system) {
    this.system = system;
  }

  public boolean isArchived() {
    return archived;
  }

  public void setArchived(boolean archived) {
    this.archived = archived;
  }

  @Override
  public int hashCode() {
    int hash = 0;
    hash += (componentId != null ? componentId.hashCode() : 0);
    return hash;
  }

  @Override
  public boolean equals(Object object) {
    // TODO: Warning - this method won't work in the case the id fields are not set
    if (!(object instanceof Component)) {
      return false;
    }
    Component other = (Component) object;
    return (this.componentId != null || other.componentId == null)
        && (this.componentId == null || this.componentId.equals(other.componentId));
  }

  @Override
  public String toString() {
    return "org.jlab.dtm.persistence.entity.Component[ componentId=" + componentId + " ]";
  }
}
