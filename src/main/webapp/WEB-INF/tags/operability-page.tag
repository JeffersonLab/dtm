<%@tag description="The Report Page Template Tag" pageEncoding="UTF-8" %>
<%@taglib prefix="s" uri="jlab.tags.smoothness"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@attribute name="title" %>
<%@attribute name="stylesheets" fragment="true" %>
<%@attribute name="scripts" fragment="true" %>
<s:page title="${title}" category="Operability">
    <jsp:attribute name="stylesheets">       
        <jsp:invoke fragment="stylesheets"/>
    </jsp:attribute>
    <jsp:attribute name="scripts">
        <jsp:invoke fragment="scripts"/>
    </jsp:attribute>
    <jsp:attribute name="secondaryNavigation">
        <ul>
            <li${'/operability/weekly-repair' eq currentPath ? ' class="current-secondary"' : ''}>
                <a href="${pageContext.request.contextPath}/operability/weekly-repair">Weekly Repair</a>
            </li>
            <li${'/operability/weekly-root-cause' eq currentPath ? ' class="current-secondary"' : ''}><a href="${pageContext.request.contextPath}/operability/weekly-root-cause">Weekly Cause</a></li>
            <li${'/operability/monthly-repair' eq currentPath ? ' class="current-secondary"' : ''}>
                <a href="${pageContext.request.contextPath}/operability/monthly-repair">Monthly Avail.</a>
            </li>
            <li${'/operability/run-compare' eq currentPath ? ' class="current-secondary"' : ''}>
                <a href="${pageContext.request.contextPath}/operability/run-compare">Run Compare</a>
            </li>
            <li${'/operability/trend' eq currentPath ? ' class="current-secondary"' : ''}>
                <a href="${pageContext.request.contextPath}/operability/trend">Trend</a>
            </li>
            <li${'/operability/annual-repair' eq currentPath ? ' class="current-secondary"' : ''}>
                <a href="${pageContext.request.contextPath}/operability/annual-repair">Annual Repair</a>
            </li>
            <li${'/operability/joule' eq currentPath ? ' class="current-secondary"' : ''}>
                <a href="${pageContext.request.contextPath}/operability/joule">Joule</a>
            </li>
        </ul>
    </jsp:attribute>
    <jsp:body>
        <jsp:doBody/>
    </jsp:body>
</s:page>
