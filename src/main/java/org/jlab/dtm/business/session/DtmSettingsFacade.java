package org.jlab.dtm.business.session;

import java.math.BigInteger;
import javax.annotation.security.PermitAll;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.jlab.dtm.persistence.entity.DtmSettings;

/**
 *
 * @author ryans
 */
@Stateless
public class DtmSettingsFacade extends AbstractFacade<DtmSettings> {
    @PersistenceContext(unitName = "dtmPU")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public DtmSettingsFacade() {
        super(DtmSettings.class);
    }

    @PermitAll
    public DtmSettings findSettings() {
        return find(BigInteger.ONE);
    }

    @PermitAll
    public void setAutoEmail(boolean autoEmail) {
        DtmSettings settings = findSettings();
        
        settings.setAutoEmail(autoEmail);
    }    
}
