<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@taglib prefix="s" uri="http://jlab.org/jsp/smoothness"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%> 
<%@taglib prefix="dtm" uri="http://jlab.org/dtm/functions"%>
<c:set var="title" value="Incident"/>
<t:page title="${title}">  
    <jsp:attribute name="stylesheets"> 
        <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/resources/v${initParam.releaseNumber}/css/event-list.css"/>
        <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/resources/v${initParam.releaseNumber}/css/incident.css"/>
    </jsp:attribute>
    <jsp:attribute name="scripts">      
        <script type="text/javascript" src="${pageContext.request.contextPath}/resources/v${initParam.releaseNumber}/js/event-list.js"></script>
    </jsp:attribute>        
    <jsp:body>
        <div class="banner-breadbox">
            <ul>
                <li>
                    <span>Incidents</span>
                </li>
                <li>
                    <h2 id="page-header-title"><c:out value="#${param.incidentId}: ${incident.title}"/></h2>
                </li>
            </ul>
        </div>
        <section>
            <div class="dialog-content">
                <c:choose>
                    <c:when test="${incident ne null}">
                        <dl>
                            <dt>Event:</dt>
                            <dd>
                                <a href="${env['FRONTEND_SERVER_URL']}/dtm/events/${incident.event.eventId}">#<c:out value="${incident.event.eventId}"/></a>
                            </dd>
                            <dt>Summary:</dt>
                            <dd>
                                <c:out value="${incident.summary}"/>
                            </dd>
                            <dt>Duration:</dt>
                            <dd>
                                <c:out value="${dtm:millisToHumanReadable(incident.elapsedMillis, true)}"/>
                            </dd>
                            <dt>Time Down/Up:</dt>
                            <dd>
                                <fmt:formatDate pattern="${s:getFriendlyDateTimePattern()}" value="${incident.timeDown}"/> /
                                <c:choose>
                                    <c:when test="${incident.timeUp ne null}">
                                        <span class="incident-table-time-up"><fmt:formatDate pattern="${s:getFriendlyDateTimePattern()}" value="${incident.timeUp}"/></span>
                                    </c:when>
                                    <c:otherwise>
                                        <span>(Open)</span>
                                    </c:otherwise>
                                </c:choose>
                            </dd>
                        </dl>
                    </c:when>
                    <c:otherwise>
                        <div>Incident not found with ID: <c:out value="${param.incidentId}"/></div>
                    </c:otherwise>
                </c:choose>
            </div>                    
        </section>
    </jsp:body>         
</t:page>
