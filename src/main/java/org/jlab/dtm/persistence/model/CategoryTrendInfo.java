package org.jlab.dtm.persistence.model;

import java.util.Date;

/**
 *
 * @author ryans
 */
public class CategoryTrendInfo extends TrendInfo  {
    private final String categoryName;

    public CategoryTrendInfo(Date date, Number incidentCount, Number duration, String categoryName) {
        super(date, incidentCount, duration);
        this.categoryName = categoryName;
    }

    public String getCategoryName() {
        return categoryName;
    }
}
