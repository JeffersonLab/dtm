<%@tag description="The Report Page Template Tag" pageEncoding="UTF-8"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%> 
<%@taglib prefix="dtm" uri="http://jlab.org/dtm/functions"%>
<%@attribute name="title"%>
<%@attribute name="stylesheets" fragment="true"%>
<%@attribute name="scripts" fragment="true"%>
<t:page title="${title}" category="Reports">
    <jsp:attribute name="stylesheets">       
        <jsp:invoke fragment="stylesheets"/>
    </jsp:attribute>
    <jsp:attribute name="scripts">
        <jsp:invoke fragment="scripts"/>
    </jsp:attribute>
    <jsp:attribute name="secondaryNavigation">
                        <ul>
                            <li${'/reports/downtime-summary' eq currentPath ? ' class="current-secondary"' : ''}><a href="${pageContext.request.contextPath}/reports/downtime-summary">Summary</a></li>
                            <li${'/reports/root-cause' eq currentPath ? ' class="current-secondary"' : ''}><a href="${pageContext.request.contextPath}/reports/root-cause">Root Cause</a></li>
                            <li${'/reports/category-downtime' eq currentPath ? ' class="current-secondary"' : ''}><a href="${pageContext.request.contextPath}/reports/category-downtime">Category Down</a></li>
                            <li${'/reports/system-downtime' eq currentPath ? ' class="current-secondary"' : ''}><a href="${pageContext.request.contextPath}/reports/system-downtime">System Down</a></li>
                            <li${'/reports/component-downtime' eq currentPath ? ' class="current-secondary"' : ''}><a href="${pageContext.request.contextPath}/reports/component-downtime">Component Down</a></li>
                            <li${'/reports/incident-downtime' eq currentPath ? ' class="current-secondary"' : ''}><a href="${pageContext.request.contextPath}/reports/incident-downtime">Incident Down</a></li>
                            <li${'/reports/event-downtime' eq currentPath ? ' class="current-secondary"' : ''}><a href="${pageContext.request.contextPath}/reports/event-downtime">Event Down</a></li>
                            <li${'/reports/event-timeline' eq currentPath ? ' class="current-secondary"' : ''}><a href="${pageContext.request.contextPath}/reports/event-timeline">Event Timeline</a></li>
                            <li${'/reports/fsd-summary' eq currentPath ? ' class="current-secondary"' : ''}><a href="${pageContext.request.contextPath}/reports/fsd-summary">FSD Summary</a></li>
                            <li${fn:startsWith(currentPath, '/reports/activity-audit') ? ' class="current-secondary"' : ''}><a href="${pageContext.request.contextPath}/reports/activity-audit">Activity Audit</a></li>
                            <li${'/reports/expert' eq currentPath ? ' class="current-secondary"' : ''}><a href="${pageContext.request.contextPath}/reports/expert">Expert</a></li>
                        </ul>
    </jsp:attribute>
    <jsp:body>
        <jsp:doBody/>
    </jsp:body>         
</t:page>
