package org.jlab.dtm.persistence.model;

import java.math.BigInteger;
import java.util.Date;

/**
 *
 * @author ryans
 */
public class AnnualRepairReportRecord {
    private final String category;
    private final BigInteger categoryId;
    private final Date month; // Year is important too so we use Date object to encapsulate both
    private double downtimeHours;

    public AnnualRepairReportRecord(String category, BigInteger categoryId, Date month, double downtimeHours) {
        this.category = category;
        this.categoryId = categoryId;
        this.month = month;
        this.downtimeHours = downtimeHours;
    }

    public String getCategory() {
        return category;
    }

    public BigInteger getCategoryId() {
        return categoryId;
    }

    public Date getMonth() {
        return month;
    }

    public double getDowntimeHours() {
        return downtimeHours;
    }
    
    public void addDowntimeHours(double hours) {
        downtimeHours = downtimeHours + hours;
    }
}
