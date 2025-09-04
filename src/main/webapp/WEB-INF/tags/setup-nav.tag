<%@tag description="Setup Navigation Tag" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@taglib prefix="s" uri="http://jlab.org/jsp/smoothness"%>
<ul>
    <li${'/setup/settings' eq currentPath ? ' class="current-secondary"' : ''}>
        <a href="${pageContext.request.contextPath}/setup/settings">Settings</a>
    </li>
    <li${'/setup/directory-cache' eq currentPath ? ' class="current-secondary"' : ''}>
        <a href="${pageContext.request.contextPath}/setup/directory-cache">Directory Cache</a>
    </li>
    <li${'/setup/types' eq currentPath ? ' class="current-secondary"' : ''}><a href="${pageContext.request.contextPath}/setup/types">Types</a></li>
    <li${'/setup/categories' eq currentPath ? ' class="current-secondary"' : ''}><a href="${pageContext.request.contextPath}/setup/categories">Categories</a></li>
    <li${'/setup/subsystem-expert' eq currentPath ? ' class="current-secondary"' : ''}><a href="${pageContext.request.contextPath}/setup/subsystem-expert">Expert List</a></li>
    <li${'/setup/email' eq currentPath ? ' class="current-secondary"' : ''}><a href="${pageContext.request.contextPath}/setup/email">Email</a></li>
</ul>