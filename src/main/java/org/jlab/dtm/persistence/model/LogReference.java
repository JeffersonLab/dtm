package org.jlab.dtm.persistence.model;

public class LogReference {
    private String title;
    private String lognumber;

    public LogReference(String lognumber, String title) {
        this.lognumber = lognumber;
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLognumber() {
        return lognumber;
    }

    public void setLognumber(String lognumber) {
        this.lognumber = lognumber;
    }
}
