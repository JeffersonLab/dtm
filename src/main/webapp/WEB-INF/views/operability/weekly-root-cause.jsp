<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@taglib prefix="s" uri="http://jlab.org/jsp/smoothness"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%> 
<%@taglib prefix="dtm" uri="http://jlab.org/dtm/functions"%>
<c:set var="title" value="${eventType.name} Root Cause Report"/>
<t:operability-page title="${title}">
    <jsp:attribute name="stylesheets"> 
        <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/resources/v${initParam.releaseNumber}/css/root-cause.css"/>
    </jsp:attribute>
    <jsp:attribute name="scripts">      
        <script src="${pageContext.request.contextPath}/resources/v${initParam.releaseNumber}/js/root-cause.js"></script>
    </jsp:attribute>        
    <jsp:body>
        <section>
            <div id="report-page-actions">
                <button id="fullscreen-button">Full Screen</button>
            </div>
            <s:filter-flyout-widget ribbon="false" clearButton="false" resetButton="false">
                <form id="filter-form" method="get" action="weekly-root-cause">
                    <fieldset>
                        <ul class="key-value-list">
                        <li>
                            <div class="li-key">
                                <label class="required-field" for="start" title="Inclusive">Start Date</label>
                            </div>
                            <div class="li-value">
                                <input type="text" class="date-field" id="start" name="start" placeholder="DD-MMM-YYYY hh:mm" value="${param.start != null ? param.start : sevenDaysAgoFmt}"/>
                            </div>
                        </li>
                        </ul>
                    </fieldset>
                    <input type="hidden" name="print" value="${param.print}">
                    <input type="hidden" name="fullscreen" value="${param.fullscreen}">
                    <input type="hidden" name="qualified" value=""/>            
                    <input type="hidden" id="offset-input" name="offset" value="0"/>
                    <input type="hidden" name="max" value="${param.max}"/>
                    <input id="filter-form-submit-button" type="submit" value="Apply"/>
                </form> 
            </s:filter-flyout-widget>
            <h2 id="page-header-title"><c:out value="${title}"/></h2>                                                         
            <div class="message-box">
                <c:out value="${selectionMessage}"/>
            </div>          
            <div>
                <c:choose>
                    <c:when test="${fn:length(incidentList) > 0}"> 
                        <h3 id="incident-detail-header">Level Ⅲ+ Incidents</h3>
                        <table class="data-table stripped-table downtime-data">
                            <thead style="display: table-row-group;">
                                <tr>
                                    <th style="width: 75px;">Hours</th>
                                    <th style="width: 285px;">Problem / Component</th>
                                    <th style="width: 385px;">Document</th>
                                    <th style="width: 125px;">Reviewer</th>
                                </tr>
                            </thead>
                            <tbody class="reload-after-edit">
                                <c:forEach items="${incidentList}" var="incident">
                                    <fmt:formatNumber value="${incident.downtimeHours}" var="formattedUnbounded" pattern="#,##0.0"/>
                                    <fmt:formatDate value="${incident.timeDown}" var="formattedIncidentDown" pattern="${s:getFriendlyDateTimePattern()}"/>
                                    <fmt:formatDate value="${incident.timeUp}" var="formattedIncidentUp" pattern="${s:getFriendlyDateTimePattern()}"/>
                                    <tr data-incident-id="${incident.incidentId}" data-event-id="${incident.event.eventId}" data-event-title="${incident.event.title}" data-incident-down="${formattedIncidentDown}" data-incident-up="${formattedIncidentUp}" data-system-id="${incident.system.systemId}" data-component-name="${fn:escapeXml(incident.component.name)}" data-component-id="${incident.component.componentId}" data-explanation="${fn:escapeXml(incident.explanation)}" data-repaired-by-id-csv="${incident.repairedByIdCsv}" data-reviewed-by="${fn:escapeXml(incident.reviewedUsername)}">
                                        <td class="relative-td">
                                            <div title="Not Bounded: ${formattedUnbounded}"><fmt:formatNumber value="${incident.getDowntimeHoursBounded(request.start, request.end)}" pattern="#,##0.0"/></div>
                                            &nbsp;
                                            <div class="absolute-subcell"><c:out value="${incident.reviewLevelString}"/></div>
                                        </td>
                                        <td class="relative-td">
                                            <div><a class="incident-title-link" href="${pageContext.request.contextPath}/incidents/${incident.incidentId}" title="${fn:escapeXml(incident.summary)}"><c:out value="${incident.title}"/></a></div>
                                            <div><c:out value="${incident.system.name}"/>; <c:out value="${incident.component.name}"/></div>
                                            <div class="absolute-subcell" title="Not Bounded: ${fn:escapeXml(dtm:formatSmartDate(incident.timeDown))}">
                                                <c:choose>
                                                    <c:when test="${request.start ne null and incident.timeDown.time < request.start.time}">
                                                        <fmt:formatDate value="${request.start}" pattern="EEE, MMM d, HH:mm, yyyy"/>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <fmt:formatDate value="${incident.timeDown}" pattern="EEE, MMM d, HH:mm, yyyy"/>
                                                    </c:otherwise>
                                                </c:choose>
                                            </div>
                                        </td>
                                        <td class="resolution-field relative-td">
                                            <div><c:out value="${incident.rootCause}"/></div>
                                            <c:if test="${incident.rarExt ne null}">
                                                <div><a href="${pageContext.request.contextPath}/ajax/rar-download?incidentId=${incident.incidentId}">Download</a></div>
                                                <c:if test="${incident.rarUploadedDate ne null}">
                                                    <div class="absolute-subcell">
                                                        Uploaded <fmt:formatDate value="${incident.rarUploadedDate}" pattern="EEE, MMM d, HH:mm, yyyy"/>
                                                    </div>
                                                </c:if>
                                            </c:if>
                                        </td>
                                        <td class="relative-td repaired-by-field">
                                            <div>
                                                <c:forEach items="${incident.incidentReviewList}" var="review" varStatus="status">
                                                    <c:out value="${s:lookupUserByUsername(review.reviewer).lastname}"/>
                                                        <c:if test="${not status.last}">
                                                            <br/>
                                                        </c:if>
                                                </c:forEach>
                                            </div>
                                            <div class="absolute-subcell ${incident.expertReviewed ? 'reviewed' : 'not-reviewed'}"><c:out value="${incident.expertReviewed ?  'REVIEWED' : 'NOT REVIEWED'}"/></div>
                                        </td>                                               
                                    </tr>
                                </c:forEach>
                            </tbody>
                        </table>
                        <c:if test="${paginator.totalRecords > param.max}">
                            <div class="pagination-controls">
                                <button id="previous-button" type="button" data-offset="${paginator.previousOffset}" value="Previous"${paginator.previous ? '' : ' disabled="disabled"'}>Previous</button>                        
                                <button id="next-button" type="button" data-offset="${paginator.nextOffset}" value="Next"${paginator.next ? '' : ' disabled="disabled"'}>Next</button>                 
                                <span><c:out value="${paginationMessage}"/></span>
                            </div>
                        </c:if>
                    </c:when>
                    <c:otherwise>
                        <div id="no-incidents-message">No incidents found.</div>
                    </c:otherwise>
                </c:choose>
            </div>                    
        </section>
        <div id="exit-fullscreen-panel">
            <button id="exit-fullscreen-button">Exit Full Screen</button>
        </div>
    </jsp:body>         
</t:operability-page>
