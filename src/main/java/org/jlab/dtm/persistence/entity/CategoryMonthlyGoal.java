package org.jlab.dtm.persistence.entity;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Date;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;

/**
 *
 * @author ryans
 */
@Entity
@Table(name = "CATEGORY_MONTHLY_GOAL", schema = "DTM_OWNER", uniqueConstraints
        = {
            @UniqueConstraint(columnNames = {"CATEGORY_ID", "MONTH"})})
@NamedQueries({
    @NamedQuery(name = "CategoryMonthlyGoal.findAll", query
            = "SELECT c FROM CategoryMonthlyGoal c")})
public class CategoryMonthlyGoal implements Serializable {

    private static final long serialVersionUID = 1L;
    // @Max(value=?)  @Min(value=?)//if you know range of your decimal fields consider using these annotations to enforce field validation
    @Id
    @SequenceGenerator(name = "CategoryMonthlyGoalId", sequenceName = "CATEGORY_MONTHLY_GOAL_ID", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "CategoryMonthlyGoalId")
    @Basic(optional = false)
    @NotNull
    @Column(name = "GOAL_ID", nullable = false, precision = 22, scale = 0)
    private BigInteger goalId;
    @Basic(optional = false)
    @NotNull
    @Column(name = "CATEGORY_ID", nullable = false)
    private Long categoryId;
    @Basic(optional = false)
    @NotNull
    @Column(nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date month;
    private Float goal;

    public CategoryMonthlyGoal() {
    }

    public CategoryMonthlyGoal(BigInteger goalId) {
        this.goalId = goalId;
    }

    public CategoryMonthlyGoal(BigInteger goalId, Long categoryId, Date month) {
        this.goalId = goalId;
        this.categoryId = categoryId;
        this.month = month;
    }

    public BigInteger getGoalId() {
        return goalId;
    }

    public void setGoalId(BigInteger goalId) {
        this.goalId = goalId;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public Date getMonth() {
        return month;
    }

    public void setMonth(Date month) {
        this.month = month;
    }

    public Float getGoal() {
        return goal;
    }

    public void setGoal(Float goal) {
        this.goal = goal;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (goalId != null ? goalId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof CategoryMonthlyGoal)) {
            return false;
        }
        CategoryMonthlyGoal other = (CategoryMonthlyGoal) object;
        if ((this.goalId == null && other.goalId != null) ||
                (this.goalId != null && !this.goalId.equals(other.goalId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "org.jlab.dtm.persistence.entity.CategoryMonthlyGoal[ goalId=" + goalId + " ]";
    }
    
}
