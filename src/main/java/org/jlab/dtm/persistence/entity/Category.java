package org.jlab.dtm.persistence.entity;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.List;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.ColumnResult;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.SqlResultSetMapping;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import org.jlab.dtm.persistence.model.Node;

/**
 *
 * @author ryans
 */
@Entity
@Table(name = "CATEGORY", schema = "DTM_OWNER", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"NAME"})})
@NamedQueries({
    @NamedQuery(name = "Category.findAll", query = "SELECT c FROM Category c")})
@SqlResultSetMapping(name = "CategoryNameOnly",
        columns = {
            @ColumnResult(name = "name")})
public class Category implements Serializable, Node {

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @NotNull
    @Column(name = "CATEGORY_ID", nullable = false, precision = 22, scale = 0)
    private BigInteger categoryId;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 128)
    @Column(nullable = false, length = 128)
    private String name;
    private BigInteger weight;
    @OneToMany(mappedBy = "parentId")
    @OrderBy("weight asc, name asc")
    private List<Category> categoryList;
    @JoinColumn(name = "PARENT_ID", referencedColumnName = "CATEGORY_ID")
    @ManyToOne
    private Category parentId;
    @OneToMany(mappedBy = "category")
    private List<SystemEntity> systemList;

    public Category() {
    }

    public Category(BigInteger categoryId) {
        this.categoryId = categoryId;
    }

    public Category(BigInteger categoryId, String name) {
        this.categoryId = categoryId;
        this.name = name;
    }

    public BigInteger getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(BigInteger categoryId) {
        this.categoryId = categoryId;
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

    public List<Category> getCategoryList() {
        return categoryList;
    }

    public void setCategoryList(List<Category> categoryList) {
        this.categoryList = categoryList;
    }

    public Category getParentId() {
        return parentId;
    }

    public void setParentId(Category parentId) {
        this.parentId = parentId;
    }

    public List<SystemEntity> getSystemList() {
        return systemList;
    }

    public void setSystemList(List<SystemEntity> systemList) {
        this.systemList = systemList;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (categoryId != null ? categoryId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Category)) {
            return false;
        }
        Category other = (Category) object;
        if ((this.categoryId == null && other.categoryId != null) || (this.categoryId != null
                && !this.categoryId.equals(other.categoryId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "org.jlab.dtm.persistence.entity.Category[ categoryId=" + categoryId + " ]";
    }

    @Override
    public BigInteger getId() {
        return getCategoryId();
    }

    @Override
    public List<? extends Node> getChildren() {
        return getCategoryList();
    }

}
