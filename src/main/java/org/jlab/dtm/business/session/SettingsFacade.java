package org.jlab.dtm.business.session;

import org.jlab.dtm.persistence.entity.Settings;

import javax.annotation.security.DeclareRoles;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.math.BigInteger;

/**
 * @author ryans
 */
@Stateless
@DeclareRoles("dtm-admin")
public class SettingsFacade extends AbstractFacade<Settings> {
    @PersistenceContext(unitName = "dtmPU")
    private EntityManager em;

    public SettingsFacade() {
        super(Settings.class);
    }

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    @PermitAll
    public Settings findSettings() {
        return find(BigInteger.ONE);
    }

    @RolesAllowed("dtm-admin")
    public void setAutoEmail(boolean autoEmail) {
        Settings settings = findSettings();

        settings.setAutoEmail(autoEmail);
    }
}
