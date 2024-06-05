<%@tag description="The Site Page Template Tag" pageEncoding="UTF-8" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@taglib prefix="dtm" uri="http://jlab.org/dtm/functions" %>
<%@taglib prefix="s" uri="http://jlab.org/jsp/smoothness" %>
<jsp:useBean id="now" class="java.util.Date"/>
<%@attribute name="title" %>
<%@attribute name="category" %>
<%@attribute name="stylesheets" fragment="true" %>
<%@attribute name="scripts" fragment="true" %>
<%@attribute name="secondaryNavigation" fragment="true" %>
<s:tabbed-page title="${title}" category="${category}">
    <jsp:attribute name="stylesheets">
        <link rel="stylesheet" type="text/css"
              href="${pageContext.request.contextPath}/resources/v${initParam.releaseNumber}/css/dtm.css"/>
        <jsp:invoke fragment="stylesheets"/>
    </jsp:attribute>
    <jsp:attribute name="scripts">
        <script src="${pageContext.request.contextPath}/resources/v${initParam.releaseNumber}/js/dtm.js"></script>
        <jsp:invoke fragment="scripts"/>
    </jsp:attribute>
    <jsp:attribute name="userExtra">
        <c:choose>
            <c:when test="${pageContext.request.isUserInRole('dtm-reviewer')}">
                <form id="role-form" action="${pageContext.request.contextPath}/change-role" method="post">
                    <select class="change-submit" name="role">
                        <option value="REVIEWER"${sessionScope.effectiveRole eq 'REVIEWER' ? ' selected="selected"' : ''}>
                            Reviewer
                        </option>
                        <option value="OPERATOR"${sessionScope.effectiveRole ne 'REVIEWER' ? ' selected="selected"' : ''}>
                            Operator
                        </option>
                    </select>
                    <input type="hidden" name="returnUrl"
                           value="${fn:escapeXml(domainRelativeReturnUrl)}"/>
                </form>
            </c:when>
            <c:otherwise>
                Operator
            </c:otherwise>
        </c:choose>
    </jsp:attribute>
    <jsp:attribute name="primaryNavigation">
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
                                    href="${pageContext.request.contextPath}/setup/subsystem-expert">Setup</a></li>
                            </c:if>
                        <li${'/help' eq currentPath ? ' class="current-primary"' : ''}><a
                                href="${pageContext.request.contextPath}/help">Help</a></li>
                    </ul>
    </jsp:attribute>
    <jsp:attribute name="secondaryNavigation">
        <jsp:invoke fragment="secondaryNavigation"/>
    </jsp:attribute>
    <jsp:body>
        <jsp:doBody/>
    </jsp:body>
</s:tabbed-page>