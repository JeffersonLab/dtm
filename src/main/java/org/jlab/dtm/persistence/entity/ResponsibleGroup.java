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
@Table(name = "RESPONSIBLE_GROUP", schema = "HCO_OWNER")
public class ResponsibleGroup implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @NotNull
    @Column(name = "GROUP_ID", nullable = false, precision = 22, scale = 0)
    private BigInteger groupId;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 128)
    @Column(nullable = false, length = 128)
    private String name;

    public ResponsibleGroup() {
    }

    public ResponsibleGroup(BigInteger groupId) {
        this.groupId = groupId;
    }

    public ResponsibleGroup(BigInteger groupId, String name) {
        this.groupId = groupId;
        this.name = name;
    }

    public BigInteger getGroupId() {
        return groupId;
    }

    public void setGroupId(BigInteger groupId) {
        this.groupId = groupId;
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
        hash += (groupId != null ? groupId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof ResponsibleGroup)) {
            return false;
        }
        ResponsibleGroup other = (ResponsibleGroup) object;
        if ((this.groupId == null && other.groupId != null) || (this.groupId != null && !this.groupId.equals(other.groupId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "org.jlab.hco.persistence.entity.ResponsibleGroup[ groupId=" + groupId + " ]";
    }

}
