<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%> 
<t:page title="Open Events">  
    <jsp:attribute name="stylesheets">
        <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/resources/v${initParam.releaseNumber}/css/event-list.css"/>
        <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/resources/v${initParam.releaseNumber}/css/open-events.css"/>
    </jsp:attribute>
    <jsp:attribute name="scripts">       
        <script type="text/javascript" src="${pageContext.request.contextPath}/resources/v${initParam.releaseNumber}/js/event-list.js"></script>
    </jsp:attribute>        
    <jsp:body>
        <section class="always-refresh-page">
            <ul class="fork-option-pair quick-nav">
                <li><a class="right-fork-option" id="previous-shift-summary-link" href="#">Previous Shift Summary</a></li>  
                <li><a class="center-fork-option" id="wall-view-link" href="wall">Real-time (wall) Summary</a></li>
                <li><a class="left-fork-option" id="current-shift-summary-link" href="#">Current Shift Summary</a></li>
            </ul>
            <h2>Open Events</h2>
            <div class="event-controls">
                <c:if test="${pageContext.request.userPrincipal ne null}">
                    <button type="button" id="open-add-event-dialog-button">Add Event</button>
                </c:if>
            </div>
            <c:choose>
                <c:when test="${fn:length(openEventList) > 0}">
                    <t:event-list eventList="${openEventList}" logbookHostname="${env['LOGBOOK_HOSTNAME']}"/>
                </c:when>
                <c:otherwise>
                    <div class="message-box">There are no open events</div>
                </c:otherwise>
            </c:choose>
        </section>
        <t:event-list-dialogs eventTypeList="${eventTypeList}" systemList="${systemList}"/>            
    </jsp:body>         
</t:page>