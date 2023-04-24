package org.jlab.dtm.persistence.model;

import java.math.BigInteger;
import org.hibernate.envers.RevisionType;

/**
 *
 * @author ryans
 */
public class AuditedEntityChange {
    private final long revision;
    private final RevisionType type;
    private final BigInteger entityId;
    private final Class entityClass;

    public AuditedEntityChange(long revision, RevisionType type, BigInteger entityId, Class entityClass) {
        this.revision = revision;
        this.type = type;
        this.entityId = entityId;
        this.entityClass = entityClass;
    }

    public long getRevision() {
        return revision;
    }
    
    public RevisionType getType() {
        return type;
    }

    public BigInteger getEntityId() {
        return entityId;
    }

    public Class getEntityClass() {
        return entityClass;
    }
}
