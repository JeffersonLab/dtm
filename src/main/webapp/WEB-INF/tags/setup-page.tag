<%@tag description="The Setup Page Template Tag" pageEncoding="UTF-8"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%>
<%@attribute name="title"%>
<%@attribute name="stylesheets" fragment="true"%>
<%@attribute name="scripts" fragment="true"%>
<t:page title="${title}" category="Setup">
    <jsp:attribute name="stylesheets">    
        <jsp:invoke fragment="stylesheets"/>
    </jsp:attribute>
    <jsp:attribute name="scripts">
        <jsp:invoke fragment="scripts"/>
    </jsp:attribute>
    <jsp:attribute name="secondaryNavigation">
        <ul>
            <li${'/setup/settings' eq currentPath ? ' class="current-secondary"' : ''}><a href="${pageContext.request.contextPath}/setup/settings">Settings</a></li>
            <li${'/setup/subsystem-expert' eq currentPath ? ' class="current-secondary"' : ''}><a href="${pageContext.request.contextPath}/setup/subsystem-expert">Expert List</a></li>
            <li${'/setup/email' eq currentPath ? ' class="current-secondary"' : ''}><a href="${pageContext.request.contextPath}/setup/email">Email</a></li>
        </ul>
    </jsp:attribute>
    <jsp:body>
        <jsp:doBody/>
    </jsp:body>         
</t:page>
