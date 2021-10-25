package org.jlab.dtm.presentation.filter;

import org.jlab.smoothness.presentation.filter.AuditContext;

import java.io.IOException;
import javax.servlet.DispatcherType;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 *
 * @author ryans
 */
@WebFilter(filterName = "EffectiveRoleFilter", urlPatterns = {"/*"}, dispatcherTypes = {
    DispatcherType.REQUEST, DispatcherType.FORWARD})
public class EffectiveRoleFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
            FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;

        String username = httpRequest.getRemoteUser();

        HttpSession session = httpRequest.getSession();
        String effectiveRole = (String) session.getAttribute("effectiveRole");

        //System.out.println("username: " + username);
        //System.out.println("effectiveRole: " + effectiveRole);
        // BEGIN [1]
        // We are setting the default effective role in case user skips login page, such as when
        // using single sign on shared session / SPNEGO
        //
        // WARNING: In case of shared session login a new session is not created!
        // We must only set default role if username is not empty as logout creates a new session, 
        // but auto-login does not so logout clears username and of course null username means 
        // OPERATOR and we are not given a chance to properly set role.   Unfortunately GlassFish
        // SSO means the same session can have null username one moment and a valid one another.  
        // Over SSL though...
        if (username != null && !username.trim().isEmpty() && (effectiveRole == null
                || effectiveRole.trim().isEmpty())) {
            boolean reviewer = httpRequest.isUserInRole("dtreview");

            //System.out.println("isUserInRole('REVIEWER'): " + reviewer);
            if (reviewer) {
                effectiveRole = "REVIEWER";
            } else {
                effectiveRole = "OPERATOR";
            }

            //System.out.println("effectiveRole set to: " + effectiveRole);
            session.setAttribute("effectiveRole", effectiveRole);
        }
        // END [1]

        AuditContext auditCtx = AuditContext.getCurrentInstance();

        auditCtx.putExtra("EffectiveRole", effectiveRole);

        chain.doFilter(request, response);
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void destroy() {
    }
}
