package org.jlab.dtm.persistence.entity;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.List;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
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
@Table(name= "ALL_COMPONENTS", schema = "HCO_OWNER", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"NAME", "SYSTEM_ID"}),
    @UniqueConstraint(columnNames = {"DATA_SOURCE"}),
    @UniqueConstraint(columnNames = {"SYSTEM_ID", "COMPONENT_ID"})})
@NamedQueries({
    @NamedQuery(name = "Component.findAll", query = "SELECT c FROM Component c")})
public class Component implements Serializable {
    private static final long serialVersionUID = 1L;
    // @Max(value=?)  @Min(value=?)//if you know range of your decimal fields consider using these annotations to enforce field validation
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
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 24)
    @Column(name = "DATA_SOURCE", nullable = false, length = 24)
    private String dataSource;
    @Column(name = "DATA_SOURCE_ID")
    private BigInteger dataSourceId;
    private BigInteger weight;
    @Basic(optional = false)
    @NotNull
    @Column(nullable = false)
    private char masked;
    @OneToMany(mappedBy = "component")
    private List<Incident> incidentList;
    @JoinColumn(name = "SYSTEM_ID", referencedColumnName = "SYSTEM_ID", nullable = false)
    @ManyToOne(optional = false)
    private org.jlab.dtm.persistence.entity.SystemEntity system;
    @JoinColumn(name = "REGION_ID", referencedColumnName = "REGION_ID", nullable = false)
    @ManyToOne(optional = false)
    private Region region;

    public Component() {
    }

    public Component(BigInteger componentId) {
        this.componentId = componentId;
    }

    public Component(BigInteger componentId, String name, String dataSource, char masked) {
        this.componentId = componentId;
        this.name = name;
        this.dataSource = dataSource;
        this.masked = masked;
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

    public String getDataSource() {
        return dataSource;
    }

    public void setDataSource(String dataSource) {
        this.dataSource = dataSource;
    }

    public BigInteger getDataSourceId() {
        return dataSourceId;
    }

    public void setDataSourceId(BigInteger dataSourceId) {
        this.dataSourceId = dataSourceId;
    }

    public BigInteger getWeight() {
        return weight;
    }

    public void setWeight(BigInteger weight) {
        this.weight = weight;
    }

    public char getMasked() {
        return masked;
    }

    public void setMasked(char masked) {
        this.masked = masked;
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

    public Region getRegion() {
        return region;
    }

    public void setRegion(Region region) {
        this.region = region;
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
        if ((this.componentId == null && other.componentId != null) || (this.componentId != null && !this.componentId.equals(other.componentId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "org.jlab.dtm.persistence.entity.Component[ componentId=" + componentId + " ]";
    }
    
}
