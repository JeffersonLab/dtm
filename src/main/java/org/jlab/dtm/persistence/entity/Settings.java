package org.jlab.dtm.persistence.entity;

import javax.mail.Address;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * @author ryans
 */
@Entity
@Table(name = "SETTINGS", schema = "DTM_OWNER")
public class Settings implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @NotNull
    @Column(name = "SETTINGS_ID", nullable = false, precision = 22, scale = 0)
    private BigInteger settingsId;
    @Column(name = "AUTO_EMAIL_YN")
    @Size(min = 1, max = 1)
    @NotNull
    private String autoEmailYn;
    @Basic(optional = true)
    @Column(name = "EXPERT_EMAIL_CC_CSV", nullable = true)
    private String expertEmailCcCsv;

    public BigInteger getSettingsId() {
        return settingsId;
    }

    public void setSettingsId(BigInteger settingsId) {
        this.settingsId = settingsId;
    }

    public boolean isAutoEmail() {
        return "Y".equals(autoEmailYn);
    }

    public void setAutoEmail(boolean autoEmail) {
        this.autoEmailYn = autoEmail ? "Y" : "N";
    }

    public String getExpertEmailCcCsv() {
        return expertEmailCcCsv;
    }
}
