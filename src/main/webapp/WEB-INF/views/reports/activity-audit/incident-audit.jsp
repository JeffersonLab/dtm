<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@taglib prefix="s" uri="http://jlab.org/jsp/smoothness"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%> 
<%@taglib prefix="dtm" uri="http://jlab.org/dtm/functions"%>
<c:set var="title" value="Incident Audit"/>
<t:reports-page title="${title}">  
    <jsp:attribute name="stylesheets"> 
    </jsp:attribute>
    <jsp:attribute name="scripts">
    </jsp:attribute>        
    <jsp:body>
        <section>
            <s:filter-flyout-widget requiredMessage="true">
                <form class="filter-form" method="get" action="incident-audit">
                    <fieldset>
                        <legend>Filter</legend>
                        <ul class="key-value-list">                      
                            <li>
                                <div class="li-key">
                                    <label class="required-field" for="incident-id">Incident ID</label>
                                </div>
                                <div class="li-value">
                                    <input type="text" id="incident-id" name="incidentId" value="${fn:escapeXml(param.incidentId)}"/>
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
                    <input type="hidden" id="offset-input" name="offset" value="0"/>
                    <input id="filter-form-submit-button" type="submit" value="Apply"/>
                </form>
            </s:filter-flyout-widget>
            <h2 id="page-header-title">Activity Audit: Incident <c:out value="${param.incidentId}"/></h2>
            <ul class="bracket-horizontal-nav">
                <li><a href="${pageContext.request.contextPath}/reports/activity-audit">Transactions</a></li>
                <li><a href="${pageContext.request.contextPath}/reports/activity-audit/event-audit">Event</a></li>
                <li>Incident</li>
            </ul>                             
            <c:choose>
                <c:when test="${param.incidentId == null}">
                    <div class="message-box">Provide an incident ID to continue</div>
                </c:when>
                <c:when test="${fn:length(incidentList) == 0}">
                    <div class="message-box">Found 0 Incident Revisions</div>
                </c:when>
                <c:otherwise>                                
                    <div class="message-box">Showing Incident Revisions <fmt:formatNumber value="${paginator.startNumber}"/> - <fmt:formatNumber value="${paginator.endNumber}"/> of <fmt:formatNumber value="${paginator.totalRecords}"/></div>           
                    <table id="revision-table" class="data-table stripped-table">
                        <thead>
                            <tr>
                                <th>Revision #:</th>
                                    <c:forEach items="${incidentList}" var="incident" varStatus="status">
                                    <th>
                                        <c:out value="${status.count + paginator.offset}"/>
                                    </th>
                                </c:forEach>
                            </tr>
                        </thead>
                        <tfoot>
                            <tr>
                                <th>Modified By:</th>
                                    <c:forEach items="${incidentList}" var="incident">
                                    <th>
                                        <c:out value="${incident.revision.username != null ? s:formatUsername(incident.revision.username) : incident.revision.username}"/>
                                    </th>
                                </c:forEach>
                            </tr>
                            <tr>
                                <th>Modified Date:</th>
                                    <c:forEach items="${incidentList}" var="incident">
                                    <th>
                                        <fmt:formatDate pattern="${s:getFriendlyDateTimePattern()}" value="${incident.revision.revisionDate}"/>
                                    </th>
                                </c:forEach>
                            </tr>
                            <tr>
                                <th>Computer:</th>
                                    <c:forEach items="${incidentList}" var="incident">
                                    <th>
                                        <c:out value="${dtm:getHostnameFromIp(incident.revision.address)}"/>
                                    </th>
                                </c:forEach>
                            </tr>                            
                            <tr>
                                <th>Revision ID:</th>
                                    <c:forEach items="${incidentList}" var="incident">
                                    <th>
                                        <c:out value="${incident.revision.id}"/>
                                    </th>
                                </c:forEach>
                            </tr>
                            <tr>
                                <th>Revision Type:</th>
                                    <c:forEach items="${incidentList}" var="incident">
                                    <th>
                                        <c:out value="${incident.type}"/>
                                    </th>
                                </c:forEach>
                            </tr>
                        </tfoot>
                        <tbody>
                            <tr>
                                <th>Title:</th>
                                    <c:forEach items="${incidentList}" var="incident">
                                    <td>
                                        <c:out value="${incident.title}"/>
                                    </td>
                                </c:forEach>
                            </tr>
                            <tr>
                                <th>Summary:</th>
                                    <c:forEach items="${incidentList}" var="incident">
                                    <td>
                                        <c:out value="${incident.summary}"/>
                                    </td>
                                </c:forEach>
                            </tr>                            
                            <tr>
                                <th>Time Down:</th>
                                    <c:forEach items="${incidentList}" var="incident">
                                    <td>
                                        <fmt:formatDate pattern="${s:getFriendlyDateTimePattern()}" value="${incident.timeDown}"/>
                                    </td>
                                </c:forEach>
                            </tr>
                            <tr>
                                <th>Time Up:</th>
                                    <c:forEach items="${incidentList}" var="incident">
                                    <td>
                                        <fmt:formatDate pattern="${s:getFriendlyDateTimePattern()}" value="${incident.timeUp}"/>
                                    </td>
                                </c:forEach>
                            </tr>
                            <tr>
                                <th>System:</th>
                                    <c:forEach items="${incidentList}" var="incident">
                                    <td>
                                        <c:out value="${incident.system.name}"/>
                                    </td>
                                </c:forEach>
                            </tr>
                            <tr>
                                <th>Component:</th>
                                    <c:forEach items="${incidentList}" var="incident">
                                    <td>
                                        <c:out value="${incident.component.name}"/>
                                    </td>
                                </c:forEach>
                            </tr>
                            <tr>
                                <th>Resolution:</th>
                                    <c:forEach items="${incidentList}" var="incident">
                                    <td>
                                        <c:out value="${incident.resolution}"/>
                                    </td>
                                </c:forEach>
                            </tr>    
                            <tr>
                                <th>Reviewed By:</th>
                                    <c:forEach items="${incidentList}" var="incident">
                                    <td>
                                        <c:out value="${incident.reviewedUsername != null ? s:formatUsername(incident.reviewedUsername) : ''}"/>
                                    </td>
                                </c:forEach>
                            </tr>
                            <tr>
                                <th>Acknowledged:</th>
                                    <c:forEach items="${incidentList}" var="incident">
                                    <td>
                                        <c:out value="${incident.expertAcknowledged}"/>
                                    </td>
                                </c:forEach>
                            </tr>                            
                            <tr>
                                <th>Root Cause:</th>
                                    <c:forEach items="${incidentList}" var="incident">
                                    <td>
                                        <c:out value="${incident.rootCause}"/>
                                    </td>
                                </c:forEach>
                            </tr>      
                            <tr>
                                <th>RAR ID:</th>
                                    <c:forEach items="${incidentList}" var="incident">
                                    <td>
                                        <c:out value="${incident.rarId}"/>
                                    </td>
                                </c:forEach>
                            </tr>
                            <tr>
                                <th>Permit To Work:</th>
                                <c:forEach items="${incidentList}" var="incident">
                                    <td>
                                        <c:out value="${incident.permitToWork}"/>
                                    </td>
                                </c:forEach>
                            </tr>
                        </tbody>
                    </table>
                    <div class="event-controls">
                        <button id="previous-button" type="button" data-offset="${paginator.previousOffset}" value="Previous"${paginator.previous ? '' : ' disabled="disabled"'}>Previous</button>                        
                        <button id="next-button" type="button" data-offset="${paginator.nextOffset}" value="Next"${paginator.next ? '' : ' disabled="disabled"'}>Next</button>                 
                    </div>
                </c:otherwise>
            </c:choose>
        </section>
    </jsp:body>         
</t:reports-page>
