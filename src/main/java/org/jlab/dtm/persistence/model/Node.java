package org.jlab.dtm.persistence.model;

import java.math.BigInteger;
import java.util.List;

/**
 *
 * @author ryans
 */
public interface Node {
    public abstract String getName();
    public abstract BigInteger getId();
    public abstract List<? extends Node> getChildren();
}
