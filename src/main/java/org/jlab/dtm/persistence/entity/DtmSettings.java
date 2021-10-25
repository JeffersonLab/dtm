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

/**
 *
 * @author ryans
 */
@Entity
@Table(name = "DTM_SETTINGS", schema = "DTM_OWNER")
public class DtmSettings implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @NotNull
    @Column(name = "DTM_SETTINGS_ID", nullable = false, precision = 22, scale = 0)
    private BigInteger dtmSettingsId;
    @Size(max = 1024)
    @Column(name = "BULLETIN_BOARD_PATH", length = 1024)
    private String bulletinBoardPath;    
    @Size(max = 128)
    @Column(name = "BULLETIN_BOARD_CATEGORY", length = 128)    
    private String bulletinBoardCategory;
    @Column(name = "AUTO_EMAIL_YN")
    @Size(min = 1, max = 1)
    @NotNull
    private String autoEmailYn;    
    
    public BigInteger getDtmSettingsId() {
        return dtmSettingsId;
    }

    public String getBulletinBoardPath() {
        return bulletinBoardPath;
    }

    public void setBulletinBoardPath(String bulletinBoardPath) {
        this.bulletinBoardPath = bulletinBoardPath;
    }

    public String getBulletinBoardCategory() {
        return bulletinBoardCategory;
    }

    public void setBulletinBoardCategory(String bulletinBoardCategory) {
        this.bulletinBoardCategory = bulletinBoardCategory;
    }
    
    public boolean isAutoEmail() {
        return "Y".equals(autoEmailYn); 
    }
    
    public void setAutoEmail(boolean autoEmail) {
        this.autoEmailYn = autoEmail == true ? "Y" : "N";
    }     
}
