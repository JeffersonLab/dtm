<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@taglib prefix="s" uri="http://jlab.org/jsp/smoothness"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%> 
<%@taglib prefix="dtm" uri="http://jlab.org/dtm/functions"%>
<t:reports-page title="Activity Audit">  
    <jsp:attribute name="stylesheets"> 
    </jsp:attribute>
    <jsp:attribute name="scripts">
    </jsp:attribute>        
    <jsp:body>
        <section>
            <s:filter-flyout-widget>
                <form id="filter-form" method="get" action="activity-audit">
                    <fieldset>
                        <legend>Filter</legend>
                        <ul class="key-value-list">                      
                            <li>
                                <div class="li-key">
                                    <label>Modified between</label>
                                </div>
                                <div class="li-value">
                                    <input type="text" class="date-field" id="modified-start" name="modifiedStart" placeholder="DD-MMM-YYYY hh:mm" value="${param.modifiedStart}"/>
                                    <span>and</span>
                                    <input type="text" class="date-field nowable-field" id="modified-end" name="modifiedEnd" placeholder="DD-MMM-YYYY hh:mm" value="${param.modifiedEnd}"/>
                                </div>
                            </li>  
                            <li>
                                <div class="li-key">
                                    <label for="event-id">Event ID</label>
                                </div>
                                <div class="li-value">
                                    <input type="text" id="event-id" name="eventId" value="${param.eventId}"/>
                                    (includes incidents)
                                </div>
                            </li>
                            <li>
                                <div class="li-key">
                                    <label for="incident-id">Incident ID</label>
                                </div>
                                <div class="li-value">
                                    <input type="text" id="incident-id" name="incidentId" value="${param.incidentId}"/>
                                    (includes event, sibling incidents)
                                </div>
                            </li>                         
                        </ul>
                    </fieldset>
                    <input type="hidden" id="offset-input" name="offset" value="0"/>
                    <input id="filter-form-submit-button" type="submit" value="Apply"/>
                </form>   
            </s:filter-flyout-widget>
            <h2 id="page-header-title">Activity Audit: Transactions</h2>
            <ul class="bracket-horizontal-nav">
                <li>Transactions</li>
                <li><a href="${pageContext.request.contextPath}/reports/activity-audit/event-audit">Event</a></li>
                <li><a href="${pageContext.request.contextPath}/reports/activity-audit/incident-audit">Incident</a></li>
            </ul>             
            <c:choose>
                <c:when test="${fn:length(revisionList) == 0}">
                    <div class="message-box">Found 0 Transactions</div>
                </c:when>
                <c:otherwise>                                
                    <div class="message-box">Showing Transactions <fmt:formatNumber value="${paginator.startNumber}"/> - <fmt:formatNumber value="${paginator.endNumber}"/> of <fmt:formatNumber value="${paginator.totalRecords}"/></div>           
                </c:otherwise>
            </c:choose>
            <div>
                <c:if test="${fn:length(revisionList) > 0}">     
                    <table class="data-table stripped-table">
                        <thead>
                            <tr>
                                <th>Revision ID</th>
                                <th>Modified Date</th>
                                <th>Modified By</th>
                                <th>Computer</th>
                                <th>Changes</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach items="${revisionList}" var="revision">
                                <tr>
                                    <td><c:out value="${revision.id}"/></td>
                                    <td><fmt:formatDate value="${revision.revisionDate}" pattern="${s:getFriendlyDateTimePattern()}"/></td>
                                    <td><c:out value="${revision.username != null ? s:formatUsername(revision.username) : revision.username}"/></td>
                                    <td><c:out value="${dtm:getHostnameFromIp(revision.address)}"/></td>
                                    <td>
                                        <c:if test="${fn:length(revision.changeList) > 0}">
                                            <ul class="table-cell-list">
                                                <c:forEach items="${revision.changeList}" var="change">
                                                    <li class="table-cell-list-item">
                                                        <a title="${change.entityClass.simpleName} Audit" href="${pageContext.request.contextPath}/reports/activity-audit/${change.entityClass.simpleName eq 'Event' ? 'event-audit?eventId' : 'incident-audit?incidentId'}=${change.entityId}"><c:out value="${change.type} ${change.entityClass.simpleName} ${change.entityId}"/></a>
                                                    </li>
                                                </c:forEach>
                                            </ul>
                                        </c:if>
                                    </td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                    <div class="event-controls">
                        <button id="previous-button" type="button" data-offset="${paginator.previousOffset}" value="Previous"${paginator.previous ? '' : ' disabled="disabled"'}>Previous</button>                        
                        <button id="next-button" type="button" data-offset="${paginator.nextOffset}" value="Next"${paginator.next ? '' : ' disabled="disabled"'}>Next</button>                 
                    </div>
                </c:if>
            </div>                    
        </section>
    </jsp:body>         
</t:reports-page>
