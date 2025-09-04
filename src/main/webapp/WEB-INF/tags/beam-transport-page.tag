<%@tag description="The Beam Transport Page Template Tag" pageEncoding="UTF-8"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@taglib prefix="s" uri="http://jlab.org/jsp/smoothness"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%>
<%@taglib prefix="dtm" uri="http://jlab.org/dtm/functions"%>
<%@attribute name="title"%>
<%@attribute name="stylesheets" fragment="true"%>
<%@attribute name="scripts" fragment="true"%>
<s:page title="${title}" category="Beam Team">
    <jsp:attribute name="stylesheets">       
        <jsp:invoke fragment="stylesheets"/>
    </jsp:attribute>
    <jsp:attribute name="scripts">
        <jsp:invoke fragment="scripts"/>
    </jsp:attribute>
    <jsp:attribute name="secondaryNavigation">
        <ul>
            <li${'/beam-transport/weekly-tune' eq currentPath ? ' class="current-secondary"' : ''}><a href="${pageContext.request.contextPath}/beam-transport/weekly-tune">Weekly Tune</a></li>
            <li${'/beam-transport/tune-incidents' eq currentPath ? ' class="current-secondary"' : ''}><a href="${pageContext.request.contextPath}/beam-transport/tune-incidents">Tune Incidents</a></li>
            <li${'/beam-transport/tune-comparison' eq currentPath ? ' class="current-secondary"' : ''}><a href="${pageContext.request.contextPath}/beam-transport/tune-comparison">Tune Comparison</a></li>
        </ul>
    </jsp:attribute>
    <jsp:body>
        <jsp:doBody/>
    </jsp:body>         
</s:page>
