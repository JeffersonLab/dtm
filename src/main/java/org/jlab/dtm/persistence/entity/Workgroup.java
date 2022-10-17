package org.jlab.dtm.persistence.entity;

import java.io.Serializable;
import java.math.BigInteger;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import org.hibernate.envers.Audited;
import static org.hibernate.envers.RelationTargetAuditMode.NOT_AUDITED;

@Entity
@Audited(targetAuditMode = NOT_AUDITED)
@Table(name = "WORKGROUP", schema = "DTM_OWNER")
public class Workgroup implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @NotNull
    @Column(name = "WORKGROUP_ID", nullable = false, precision = 22, scale = 0)
    private BigInteger workgroupId;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 128)
    @Column(nullable = false, length = 128)
    private String name;

    public Workgroup() {
    }

    public Workgroup(BigInteger workgroupId, String name) {
        this.workgroupId = workgroupId;
        this.name = name;
    }

    public BigInteger getWorkgroupId() {
        return workgroupId;
    }

    public void setWorkgroupId(BigInteger workgroupId) {
        this.workgroupId = workgroupId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (workgroupId != null ? workgroupId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Workgroup)) {
            return false;
        }
        Workgroup other = (Workgroup) object;
        if ((this.workgroupId == null && other.workgroupId != null) || (this.workgroupId != null && !this.workgroupId.equals(other.workgroupId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "org.jlab.dtm.persistence.entity.WorkGroup[ workgroupId=" + workgroupId + " ]";
    }

}
