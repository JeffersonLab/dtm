package org.jlab.dtm.persistence.entity.aud;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Objects;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.validation.constraints.NotNull;

/**
 * @author ryans
 */
@Embeddable
public class IncidentAudPK implements Serializable {
  @Basic(optional = false)
  @NotNull
  @Column(name = "INCIDENT_ID", nullable = false)
  private BigInteger incidentId;

  @Basic(optional = false)
  @NotNull
  @Column(name = "REV", nullable = false)
  private BigInteger rev;

  public IncidentAudPK() {}

  public IncidentAudPK(BigInteger incidentId, BigInteger rev) {
    this.incidentId = incidentId;
    this.rev = rev;
  }

  public BigInteger getIncidentId() {
    return incidentId;
  }

  public void setIncidentId(BigInteger incidentId) {
    this.incidentId = incidentId;
  }

  public BigInteger getRev() {
    return rev;
  }

  public void setRev(BigInteger rev) {
    this.rev = rev;
  }

  @Override
  public int hashCode() {
    int hash = 3;
    hash = 23 * hash + (this.incidentId != null ? this.incidentId.hashCode() : 0);
    hash = 23 * hash + (this.rev != null ? this.rev.hashCode() : 0);
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
    final IncidentAudPK other = (IncidentAudPK) obj;
    if (!Objects.equals(this.incidentId, other.incidentId)) {
      return false;
    }
    return Objects.equals(this.rev, other.rev);
  }
}
