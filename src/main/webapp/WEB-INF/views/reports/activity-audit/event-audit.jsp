<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="jakarta.tags.core"%>
<%@taglib prefix="fn" uri="jakarta.tags.functions"%>
<%@taglib prefix="fmt" uri="jakarta.tags.fmt"%>
<%@taglib prefix="s" uri="http://jlab.org/jsp/smoothness"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%> 
<%@taglib prefix="dtm" uri="http://jlab.org/dtm/functions"%>
<c:set var="title" value="Event Audit"/>
<t:reports-page title="${title}">  
    <jsp:attribute name="stylesheets">       
    </jsp:attribute>
    <jsp:attribute name="scripts">
    </jsp:attribute>        
    <jsp:body>
        <section>
            <s:filter-flyout-widget requiredMessage="true">
                <form class="filter-form" method="get" action="event-audit">
                    <fieldset>
                        <legend>Filter</legend>
                        <ul class="key-value-list">                      
                            <li>
                                <div class="li-key">
                                    <label class="required-field" for="event-id">Event ID</label>
                                </div>
                                <div class="li-value">
                                    <input type="text" id="event-id" name="eventId" value="${fn:escapeXml(param.eventId)}"/>
                                </div>
                            </li>
                            <li>
                                <div class="li-key">
                                    <label for="revision-id">Revision ID</label>
                                </div>
                                <div class="li-value">
                                    <input type="text" id="revision-id" name="revisionId" value="${fn:escapeXml(param.revisionId)}"/>
                                </div>
                            </li>                         
                        </ul>
                    </fieldset>
                        <input type="hidden" class="offset-input" name="offset" value="0"/>
                        <input class="filter-form-submit-button" type="submit" value="Apply"/>
                </form>
            </s:filter-flyout-widget>
            <h2 class="page-header-title">Activity Audit: Event <c:out value="${param.eventId}"/></h2>
            <ul class="bracket-horizontal-nav">
                <li><a href="${pageContext.request.contextPath}/reports/activity-audit">Transactions</a></li>
                <li>Event</li>
                <li><a href="${pageContext.request.contextPath}/reports/activity-audit/incident-audit">Incident</a></li>
            </ul>                             
            <c:choose>
                <c:when test="${param.eventId == null}">
                    <div class="message-box">Provide an event ID to continue</div>
                </c:when>
                <c:when test="${fn:length(eventList) == 0}">
                    <div class="message-box">Found 0 Event Revisions</div>
                </c:when>
                <c:otherwise>                                
                    <div class="message-box">Showing Event Revisions <fmt:formatNumber value="${paginator.startNumber}"/> - <fmt:formatNumber value="${paginator.endNumber}"/> of <fmt:formatNumber value="${paginator.totalRecords}"/></div>           
                    <table id="revision-table" class="data-table stripped-table">
                        <thead>
                            <tr>
                                <th>Revision #:</th>
                                    <c:forEach items="${eventList}" var="event" varStatus="status">
                                    <th>
                                        <c:out value="${status.count + paginator.offset}"/>
                                    </th>
                                </c:forEach>
                            </tr>
                        </thead>
                        <tfoot>
                            <tr>
                                <th>Modified By:</th>
                                    <c:forEach items="${eventList}" var="event">
                                    <th>
                                        <c:out value="${event.revision.username != null ? s:formatUsername(event.revision.username) : event.revision.username}"/>
                                    </th>
                                </c:forEach>
                            </tr>
                            <tr>
                                <th>Modified Date:</th>
                                    <c:forEach items="${eventList}" var="event">
                                    <th>
                                        <fmt:formatDate pattern="${s:getFriendlyDateTimePattern()}" value="${event.revision.revisionDate}"/>
                                    </th>
                                </c:forEach>
                            </tr>
                            <tr>
                                <th>Computer:</th>
                                    <c:forEach items="${eventList}" var="event">
                                    <th>
                                        <c:out value="${dtm:getHostnameFromIp(event.revision.address)}"/>
                                    </th>
                                </c:forEach>
                            </tr>                            
                            <tr>
                                <th>Revision ID:</th>
                                    <c:forEach items="${eventList}" var="event">
                                    <th>
                                        <c:out value="${event.revision.id}"/>
                                    </th>
                                </c:forEach>
                            </tr>
                            <tr>
                                <th>Revision Type:</th>
                                    <c:forEach items="${eventList}" var="event">
                                    <th>
                                        <c:out value="${event.type}"/>
                                    </th>
                                </c:forEach>
                            </tr>                            
                        </tfoot>
                        <tbody>
                            <tr>                  
                                <th>Type:</th>
                                    <c:forEach items="${eventList}" var="event">
                                    <td>
                                        <c:out value="${event.eventType.abbreviation}"/>
                                    </td>
                                </c:forEach>
                            </tr>                             
                            <tr>                  
                                <th>Title</th>
                                    <c:forEach items="${eventList}" var="event">
                                    <td>
                                        <c:out value="${event.title}"/>
                                    </td>
                                </c:forEach>
                            </tr>                            
                            <tr>                  
                                <th>Time Up:</th>
                                    <c:forEach items="${eventList}" var="event">
                                    <td>
                                        <fmt:formatDate pattern="${s:getFriendlyDateTimePattern()}" value="${event.timeUp}"/>
                                    </td>
                                </c:forEach>
                            </tr>
                        </tbody>
                    </table>
                    <div class="event-controls">
                        <button class="previous-button" type="button" data-offset="${paginator.previousOffset}" value="Previous"${paginator.previous ? '' : ' disabled="disabled"'}>Previous</button>
                        <button class="next-button" type="button" data-offset="${paginator.nextOffset}" value="Next"${paginator.next ? '' : ' disabled="disabled"'}>Next</button>
                    </div>
                </c:otherwise>
            </c:choose>
        </section>
    </jsp:body>         
</t:reports-page>
