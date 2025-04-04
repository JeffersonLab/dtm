<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@taglib prefix="s" uri="http://jlab.org/jsp/smoothness"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%> 
<%@taglib prefix="dtm" uri="http://jlab.org/dtm/functions"%>
<c:set var="title" value="${eventType.name} Root Cause Report"/>
<t:reports-page title="${title}">
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
            <s:filter-flyout-widget ribbon="false" clearButton="true" resetButton="true">
                <form id="root-filter-form" class="filter-form" method="get" action="root-cause">
                    <fieldset>
                        <legend>Time</legend>
                        <s:date-range datetime="${true}" sevenAmOffset="${true}"/>
                    </fieldset>
                    <fieldset>
                        <legend>Taxonomy</legend>
                        <ul class="key-value-list">                      
                            <li>
                                <div class="li-key">
                                    <label for="event-type">Type</label>
                                </div>
                                <div class="li-value">
                                    <select id="event-type" name="type">
                                        <option value="">&nbsp;</option>
                                        <c:forEach items="${eventTypeList}" var="eventType">
                                            <option value="${eventType.eventTypeId}"${param.type eq eventType.eventTypeId ? ' selected="selected"' : ''}><c:out value="${eventType.name}"/> (<c:out value="${eventType.abbreviation}"/>)</option>
                                        </c:forEach>
                                    </select>
                                </div>
                            </li>  
                            <li>
                                <div class="li-key">
                                    <label for="event-id">Event ID</label>
                                </div>
                                <div class="li-value">
                                    <input type="number" id="event-id" name="eventId" value="${fn:escapeXml(param.eventId)}"/>
                                </div>
                            </li>
                            <li>
                                <div class="li-key">
                                    <label for="incident-id">Incident ID</label>
                                </div>
                                <div class="li-value">
                                    <input type="number" id="incident-id" name="incidentId" value="${fn:escapeXml(param.incidentId)}"/>
                                </div>
                            </li>
                            <li>
                                <div class="li-key">
                                    <label for="sme-username">Reviewer</label>
                                </div>
                                <div class="li-value">
                                    <input type="text" id="sme-username" name="smeUsername" class="username-autocomplete" value="${fn:escapeXml(param.smeUsername)}" placeholder="username"/>
                                </div>
                            </li>                             
                        </ul>
                    </fieldset>
                    <input type="hidden" name="print" value="${fn:escapeXml(param.print)}">
                    <input type="hidden" name="fullscreen" value="${fn:escapeXml(param.fullscreen)}">
                    <input type="hidden" name="qualified" value=""/>            
                    <input type="hidden" class="offset-input" name="offset" value="0"/>
                    <input type="hidden" name="max" value="${fn:escapeXml(param.max)}"/>
                    <input class="filter-form-submit-button" type="submit" value="Apply"/>
                </form> 
            </s:filter-flyout-widget>
            <h2 class="page-header-title"><c:out value="${title}"/></h2>
            <div class="message-box">
                <c:out value="${selectionMessage}"/>
            </div>          
            <div>
                <h3>Overall Metrics</h3>
                <table id="overall-table" class="data-table">
                    <thead>
                        <tr>
                            <th rowspan="2"></th> 
                            <th><input form="root-filter-form" type="radio" name="incidentMask" value="NONE" ${param.incidentMask eq 'NONE' ? 'checked="checked"' : ''}/></th>
                            <th><input form="root-filter-form" type="radio" name="incidentMask" value="DEADBEATS" ${param.incidentMask eq 'DEADBEATS' ? 'checked="checked"' : ''}/></th>
                            <th><input form="root-filter-form" type="radio" name="incidentMask" value="LEVEL_ONE" ${param.incidentMask eq 'LEVEL_ONE' ? 'checked="checked"' : ''}/></th>
                            <th><input form="root-filter-form" type="radio" name="incidentMask" value="LEVEL_TWO" ${param.incidentMask eq 'LEVEL_TWO' ? 'checked="checked"' : ''}/></th>
                            <th><input form="root-filter-form" type="radio" name="incidentMask" value="LEVEL_THREE_PLUS" ${param.incidentMask eq 'LEVEL_THREE_PLUS' ? 'checked="checked"' : ''}/></th>
                        </tr>
                        <tr>
                            <th>All Incidents</th>
                            <th>Not Reviewed</th>
                            <th>Level Ⅰ</th>
                            <th>Level Ⅱ</th>
                            <th>Level Ⅲ+</th>
                        </tr>
                    </thead>
                    <tbody>
                        <tr>
                            <c:set var="deadbeatCountPercent" value=""/>
                            <c:set var="deadbeatHoursPercent" value=""/>
                            <c:set var="levelOneCountPercent" value=""/>
                            <c:set var="LevelOneHoursPercent" value=""/>
                            <c:set var="levelTwoCountPercent" value=""/>
                            <c:set var="levelTwoHoursPercent" value=""/>
                            <c:set var="levelThreePlusCountPercent" value=""/>
                            <c:set var="levelThreePlusHoursPercent" value=""/>
                            <c:if test="${overallMetric.repairs > 0}">
                                <fmt:formatNumber var="val" pattern="#,###,##0" value="${overallMetric.deadbeats / overallMetric.repairs * 100}"/>
                                <c:set var="deadbeatCountPercent" value="(${val}%)"/>
                                <fmt:formatNumber var="val" pattern="#,###,##0" value="${overallMetric.levelOne / overallMetric.repairs * 100}"/>
                                <c:set var="levelOneCountPercent" value="(${val}%)"/>
                                <fmt:formatNumber var="val" pattern="#,###,##0" value="${overallMetric.levelTwo / overallMetric.repairs * 100}"/>
                                <c:set var="levelTwoCountPercent" value="(${val}%)"/>
                                <fmt:formatNumber var="val" pattern="#,###,##0" value="${overallMetric.levelThreePlus / overallMetric.repairs * 100}"/>
                                <c:set var="levelThreePlusCountPercent" value="(${val}%)"/>
                            </c:if>
                            <c:if test="${overallMetric.repairHours > 0}">
                                <fmt:formatNumber var="val" pattern="#,###,##0" value="${overallMetric.deadbeatHours / overallMetric.repairHours * 100}"/>
                                <c:set var="deadbeatHoursPercent" value="(${val}%)"/>
                                <fmt:formatNumber var="val" pattern="#,###,##0" value="${overallMetric.levelOneHours / overallMetric.repairHours * 100}"/>
                                <c:set var="levelOneHoursPercent" value="(${val}%)"/>
                                <fmt:formatNumber var="val" pattern="#,###,##0" value="${overallMetric.levelTwoHours / overallMetric.repairHours * 100}"/>
                                <c:set var="levelTwoHoursPercent" value="(${val}%)"/>
                                <fmt:formatNumber var="val" pattern="#,###,##0" value="${overallMetric.levelThreePlusHours / overallMetric.repairHours * 100}"/>
                                <c:set var="levelThreePlusHoursPercent" value="(${val}%)"/>
                            </c:if>                            
                            <th>Count</th>
                            <td class="right-aligned"><fmt:formatNumber pattern="#,###,##0" value="${overallMetric.repairs}"/></td> 
                            <td class="right-aligned"><fmt:formatNumber pattern="#,###,##0" value="${overallMetric.deadbeats}"/> ${deadbeatCountPercent}</td>
                            <td class="right-aligned"><fmt:formatNumber pattern="#,###,##0" value="${overallMetric.levelOne}"/> ${levelOneCountPercent}</td>
                            <td class="right-aligned"><fmt:formatNumber pattern="#,###,##0" value="${overallMetric.levelTwo}"/> ${levelTwoCountPercent}</td>
                            <td class="right-aligned"><fmt:formatNumber pattern="#,###,##0" value="${overallMetric.levelThreePlus}"/> ${levelThreePlusCountPercent}</td>
                        </tr>
                        <tr>
                            <th>Hours</th>
                            <td class="right-aligned"><fmt:formatNumber pattern="#,###,##0.0" value="${overallMetric.repairHours}"/></td> 
                            <td class="right-aligned"><fmt:formatNumber pattern="#,###,##0.0" value="${overallMetric.deadbeatHours}"/> ${deadbeatHoursPercent}</td>
                            <td class="right-aligned"><fmt:formatNumber pattern="#,###,##0.0" value="${overallMetric.levelOneHours}"/> ${levelOneHoursPercent}</td>
                            <td class="right-aligned"><fmt:formatNumber pattern="#,###,##0.0" value="${overallMetric.levelTwoHours}"/> ${levelTwoHoursPercent}</td>
                            <td class="right-aligned"><fmt:formatNumber pattern="#,###,##0.0" value="${overallMetric.levelThreePlusHours}"/> ${levelThreePlusHoursPercent}</td>
                        </tr>                                
                    </tbody>
                </table>  
                <c:choose>
                    <c:when test="${fn:length(incidentList) > 0}"> 
                        <h3 id="incident-detail-header"><c:out value="${incidentMask.label}"/> Incidents</h3>
                        <span id="sort-controls"><label for="sort-select">Sort: </label>
                            <select form="root-filter-form" id="sort-select" name="sort">
                                <option value="DURATION"${param.sort eq 'DURATION' ? ' selected="selected"' : ''}>Duration</option>
                                <option value="TIME_DOWN"${param.sort eq 'TIME_DOWN' ? ' selected="selected"' : ''}>Time Down</option>
                                <option value="RAR_UPLOADED"${param.sort eq 'RAR_UPLOADED' ? ' selected="selected"' : ''}>RAR Uploaded</option>
                            </select>
                        </span>
                        <table class="data-table stripped-table downtime-data">
                            <thead style="display: table-row-group;">
                                <tr>
                                    <th style="width: 75px;">Hours</th>
                                    <th style="width: 285px;">Problem / Component</th>
                                    <th style="width: 385px;">Root Cause</th>                                        
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
                                                <div><a href="${pageContext.request.contextPath}/ajax/rar-download?incidentId=${incident.incidentId}">RAR Document</a></div>
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
                                <button class="previous-button" type="button" data-offset="${paginator.previousOffset}" value="Previous"${paginator.previous ? '' : ' disabled="disabled"'}>Previous</button>
                                <button class="next-button" type="button" data-offset="${paginator.nextOffset}" value="Next"${paginator.next ? '' : ' disabled="disabled"'}>Next</button>
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
</t:reports-page>
