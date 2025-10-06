/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.dtm.persistence.entity;

import jakarta.persistence.Basic;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

/**
 * @author ryans
 */
@Entity
@Table(schema = "DTM_OWNER")
@NamedQueries({@NamedQuery(name = "Region.findAll", query = "SELECT r FROM Region r")})
public class Region implements Serializable {
  private static final long serialVersionUID = 1L;

  // @Max(value=?)  @Min(value=?)//if you know range of your decimal fields consider using these
  // annotations to enforce field validation
  @Id
  @Basic(optional = false)
  @NotNull
  @Column(name = "REGION_ID", nullable = false, precision = 22, scale = 0)
  private BigDecimal regionId;

  @Basic(optional = false)
  @NotNull
  @Size(min = 1, max = 128)
  @Column(nullable = false, length = 128)
  private String name;

  @Size(max = 128)
  @Column(length = 128)
  private String alias;

  private BigInteger weight;

  @OneToMany(cascade = CascadeType.ALL, mappedBy = "region")
  private List<EternalComponent> componentList;

  public Region() {}

  public Region(BigDecimal regionId) {
    this.regionId = regionId;
  }

  public Region(BigDecimal regionId, String name) {
    this.regionId = regionId;
    this.name = name;
  }

  public BigDecimal getRegionId() {
    return regionId;
  }

  public void setRegionId(BigDecimal regionId) {
    this.regionId = regionId;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getAlias() {
    return alias;
  }

  public void setAlias(String alias) {
    this.alias = alias;
  }

  public BigInteger getWeight() {
    return weight;
  }

  public void setWeight(BigInteger weight) {
    this.weight = weight;
  }

  public List<EternalComponent> getComponentList() {
    return componentList;
  }

  public void setComponentList(List<EternalComponent> componentList) {
    this.componentList = componentList;
  }

  @Override
  public int hashCode() {
    int hash = 0;
    hash += (regionId != null ? regionId.hashCode() : 0);
    return hash;
  }

  @Override
  public boolean equals(Object object) {
    // TODO: Warning - this method won't work in the case the id fields are not set
    if (!(object instanceof Region)) {
      return false;
    }
    Region other = (Region) object;
    return (this.regionId != null || other.regionId == null)
        && (this.regionId == null || this.regionId.equals(other.regionId));
  }

  @Override
  public String toString() {
    return "org.jlab.dtm.persistence.entity.Region[ regionId=" + regionId + " ]";
  }
}
