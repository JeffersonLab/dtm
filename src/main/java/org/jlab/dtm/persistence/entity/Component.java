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
@Table(name= "ALL_COMPONENTS", schema = "DTM_OWNER")
@NamedQueries({
    @NamedQuery(name = "Component.findAll", query = "SELECT c FROM Component c")})
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

    public Component() {
    }

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
        return (this.componentId != null || other.componentId == null) && (this.componentId == null || this.componentId.equals(other.componentId));
    }

    @Override
    public String toString() {
        return "org.jlab.dtm.persistence.entity.Component[ componentId=" + componentId + " ]";
    }
    
}
