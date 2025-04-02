<%@tag description="Primary Navigation Tag" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@taglib prefix="s" uri="http://jlab.org/jsp/smoothness"%>
<ul>
    <li${('/open' eq currentPath) or ('/open-events' eq currentPath) ? ' class="current-primary"' : ''}><a
            href="${pageContext.request.contextPath}/open">Open</a></li>
    <li${fn:startsWith(currentPath, '/events') or fn:startsWith(currentPath, '/all-events')  ? ' class="current-primary"' : ''}><a
            href="${pageContext.request.contextPath}/events">Events</a></li>
    <li${'/trips' eq currentPath ? ' class="current-primary"' : ''}><a
            href="${pageContext.request.contextPath}/trips">Trips</a></li>
    <li${fn:startsWith(currentPath, '/reports') ? ' class="current-primary"' : ''}><a
            href="${pageContext.request.contextPath}/reports/downtime-summary">Reports</a></li>
    <li${fn:startsWith(currentPath, '/operability') ? ' class="current-primary"' : ''}><a
            href="${pageContext.request.contextPath}/operability/weekly-repair">Operability</a></li>
    <li${'/rar' eq currentPath ? ' class="current-primary"' : ''}><a
            href="${pageContext.request.contextPath}/rar">RAR</a></li>
    <li${fn:startsWith(currentPath, '/beam-team') ? ' class="current-primary"' : ''}><a
            href="${pageContext.request.contextPath}/beam-team/weekly-tune">Beam Team</a></li>
    <c:if test="${pageContext.request.isUserInRole('dtm-admin')}">
        <li${fn:startsWith(currentPath, '/setup') ? ' class="current-primary"' : ''}><a
                href="${pageContext.request.contextPath}/setup/settings">Setup</a></li>
    </c:if>
    <li${'/help' eq currentPath ? ' class="current-primary"' : ''}><a
            href="${pageContext.request.contextPath}/help">Help</a></li>
</ul>