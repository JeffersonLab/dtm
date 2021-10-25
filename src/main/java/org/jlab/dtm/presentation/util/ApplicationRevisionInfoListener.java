package org.jlab.dtm.presentation.util;

import org.hibernate.envers.RevisionListener;
import org.jlab.dtm.persistence.entity.ApplicationRevisionInfo;
import org.jlab.smoothness.presentation.filter.AuditContext;

/**
 *
 * @author ryans
 */
public class ApplicationRevisionInfoListener implements RevisionListener {

    @Override
    public void newRevision(Object o) {
        ApplicationRevisionInfo revisionInfo = (ApplicationRevisionInfo)o;
        
        AuditContext context = AuditContext.getCurrentInstance();
        
        String ip = context.getIp();
        String username = context.getUsername();
        
        revisionInfo.setAddress(ip);
        revisionInfo.setUsername(username);
    }
    
}
