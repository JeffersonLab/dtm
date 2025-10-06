<%@tag description="Event Timeline Template Tag" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@taglib prefix="s" uri="http://jlab.org/jsp/smoothness"%>
<%@taglib prefix="dtm" uri="http://jlab.org/dtm/functions"%>
<%@attribute name="event" required="true" type="org.jlab.dtm.persistence.entity.Event"%>
<jsp:useBean id="now" class="java.util.Date"/>
<c:set var="eventTimeDown" value="${event.timeDown}"/>
<c:set var="eventTimeUp" value="${event.timeUp == null ? now : event.timeUp}"/>
<c:set var="eventDuration" value="${eventTimeUp.time - eventTimeDown.time}"/>
<c:set var="timelineHeight" value="${fn:length(event.incidentList) * 2 + 0.5}em;"/>
<fmt:formatDate pattern="${s:getFriendlyDateTimePattern()}" value="${event.timeUp}" var="eventTimeUpStr"/>
<div class="event-detail" data-event-id="${event.eventId}" data-event-time-up="${eventTimeUpStr}">
    <div class="event-control-toolbar ui-widget ui-widget-header">                
        <c:if test="${(pageContext.request.userPrincipal ne null) and ((!event.containsAtLeastOneReview() and !event.closedLongAgo()) || sessionScope.effectiveRole eq 'REVIEWER')}">
            <button type="button" class="open-add-incident-dialog-button">Add Incident</button>             
            <button type="button" class="open-edit-event-dialog-button">Edit${event.timeUp == null ? '/Close' : ''} Event</button>
            <button type="button" class="remove-event-button">Delete Event</button>
        </c:if>
        <span class="closed-by-panel">
            <c:choose>
                <c:when test="${event.timeUp ne null}">
                    <span>Closed by: </span><a title="Activity Audit" href="${pageContext.request.contextPath}/reports/activity-audit?eventId=${event.eventId}"><c:out value="${s:formatUser(event.closedBy)}"/></a>
                </c:when>
                <c:otherwise>
                    <a title="Activty Audit" href="${pageContext.request.contextPath}/reports/activity-audit?eventId=${event.eventId}">Event is open</a>
                </c:otherwise>
            </c:choose>
        </span>
    </div>
    <div class="event-id-container"><span class="event-id-label">Event ID: </span><span class="event-id-value">${event.eventId}</span></div>
    <h4>Timeline</h4>
    <div class="event-timeline-buffer">
        <div class="event-timeline" style="height: ${timelineHeight}">
            <div class="event-timeline-incidents">
                <c:forEach items="${event.incidentList}" var="incident" varStatus="status">
                    <c:set var="incidentTimeDown" value="${incident.timeDown}"/>
                    <c:set var="incidentTimeUp" value="${incident.timeUp == null ? now : incident.timeUp}"/>
                    <c:set var="incidentDuration" value="${incidentTimeUp.time - incidentTimeDown.time}"/>        
                    <div title="${incident.title} (${dtm:millisToHumanReadable(incidentDuration, true)})" class="event-timeline-incident" style="top: ${status.index * 2 + 0.5}em; width: ${(incidentDuration / eventDuration) * 100}%; left: ${((incidentTimeDown.time - eventTimeDown.time) / eventDuration) * 100}%;">
                        <span class="incident-title"><c:out value="${incident.title}"/></span>
                    </div>
                </c:forEach>
            </div>
            <div class="event-timeline-scale">
                <c:forEach begin="0" end="5" varStatus="status">
                    <c:set var="percent" value="${status.index * 20}"/>
                    <div class="tick" style="height: ${timelineHeight}; left: ${percent}%;">
                        <c:choose>
                            <c:when test="${status.index eq 0}">
                                <div class="tick-label" style="top: -4em;">(Down)<br/><fmt:formatDate pattern="${s:getFriendlyDateTimePattern()}" value="${dtm:getTimelineTickDate(percent, eventDuration, eventTimeDown)}"/></div>
                            </c:when>
                            <c:when test="${status.index eq 5}">
                                <div class="tick-label" style="top: -4em;">(Up)<br/><fmt:formatDate pattern="${s:getFriendlyDateTimePattern()}" value="${dtm:getTimelineTickDate(percent, eventDuration, eventTimeDown)}"/>${event.timeUp eq null ? '<span class="still-open-asterisk" title="Event is open">^</span>' : ''}</div>
                            </c:when>
                            <c:otherwise>
                                <div class="tick-label"><fmt:formatDate pattern="${s:getFriendlyDateTimePattern()}" value="${dtm:getTimelineTickDate(percent, eventDuration, eventTimeDown)}"/></div>
                            </c:otherwise>
                        </c:choose>
                    </div>            
                </c:forEach>
            </div>
        </div>            
        <div class="event-timeline-restore">
            <span class="restore-header">Restore: </span><span class="elapsed-time">${dtm:millisToHumanReadable(event.restoreMillis, true)}</span>
        </div>
    </div>
    <h4>Incidents</h4>
    <table class="data-table stripped-table incident-table" data-event-type-id="${event.eventType.eventTypeId}">
        <thead>
            <tr>
                <th class="incident-id-header"></th>
                <th>Description</th>
                <th class="incident-period-header">Period</th>           
                <th class="incident-cause-header">Cause / Effect</th>
                <c:if test="${settings.is('LOGBOOK_ENABLED')}">
                    <th class="incident-log-entries-header">Log Entries</th>
                </c:if>
            </tr>
        </thead>
        <tbody>
            <c:forEach items="${event.incidentList}" var="incident">
                <tr data-incident-id="${incident.incidentId}" data-solution="${fn:escapeXml(incident.resolution)}" data-repaired-by-id-csv="${incident.repairedByIdCsv}" data-reviewed-by-username-ssv="${incident.reviewedByUsernameSsv}" data-repaired-by-formatted="${incident.repairedByIdCsv != null ? dtm:formatGroupList(incident.repairedByIdCsv, groupList) : '--None--'}" data-reviewed-by="${fn:escapeXml(incident.reviewedUsername)}" data-reviewed-by-formatted="${incident.reviewedUsername != null ? s:formatUsername(incident.reviewedUsername) : ''}" data-reviewed-by-experts-formatted-tsv="${incident.reviewedByExpertsFormattedTsv}" data-acknowledged="${fn:escapeXml(incident.expertAcknowledged)}" data-root-cause="${fn:escapeXml(incident.rootCause)}" data-rar-id="${incident.rarId}" data-rar-ext="${incident.rarExt}" data-review-level="${incident.reviewLevelString}">
                    <td>
                        <span>
                            <span class="cell-sublabel">ID:<a title="Incident Link" class="flyout-link" href="${env['FRONTEND_SERVER_URL']}/dtm/incidents/${incident.incidentId}">*</a></span>
                            <span><a href="${env['FRONTEND_SERVER_URL']}/dtm/incidents/${incident.incidentId}"><c:out value="${incident.incidentId}"/></a></span>
                        </span>
                        <span class="cell-subfield">
                            <span class="cell-sublabel">Review:<a title="Resolution Information" class="review-link" href="#">*</a></span>
                            <span>
                                <span title="Subject Matter Expert">SME:</span>
                                <c:choose>
                                    <c:when test="${incident.expertReviewed}">
                                        <span class="reviewed-link">Yes</span>
                                    </c:when>
                                    <c:otherwise>
                                        <span class="not-reviewed-link">No</span>
                                    </c:otherwise>
                                </c:choose>                                
                                <span title="Operability">OPR:</span>
                                <c:choose>
                                    <c:when test="${incident.reviewedUsername ne null}">
                                        <span class="reviewed-link">Yes</span>
                                    </c:when>
                                    <c:otherwise>
                                        <span class="not-reviewed-link">No</span>
                                    </c:otherwise>
                                </c:choose>
                            </span>
                        </span>                         
                        <span>
                            <c:if test="${(pageContext.request.userPrincipal ne null) and ((incident.reviewedUsername == null and !event.closedLongAgo()) || sessionScope.effectiveRole eq 'REVIEWER')}">
                                <hr/>
                                <button type="button" class="open-edit-incident-dialog-button">Edit${incident.timeUp == null ? '/Close' : ''} Incident</button>
                                <button type="button" class="remove-incident-button">Delete Incident</button>
                            </c:if> 
                            <c:if test="${pageContext.request.userPrincipal ne null}">
                            <button class="open-edit-expert-review-dialog-button">Edit SME Review</button>  
                            </c:if>
                        </span>
                    </td>
                    <td>
                        <span class="cell-subfield">
                            <span class="cell-sublabel">Title:</span>
                            <span class="incident-table-title"><c:out value="${incident.title}"/></span>
                        </span>
                        <span class="cell-subfield">
                            <span class="cell-sublabel">Summary:</span>
                            <span class="incident-table-summary"><c:out value="${incident.summary}"/></span>
                        </span>                        
                    </td>
                    <td>
                        <span class="cell-subfield">
                            <span class="cell-sublabel">Duration:</span>
                            <span class="incident-table-duration"><c:out value="${dtm:millisToHumanReadable(incident.elapsedMillis, true)}"/></span>
                        </span>
                        <span class="cell-subfield">
                            <span class="cell-sublabel">Review Level:</span>
                            <span class="incident-table-review-level"><c:out value="${incident.reviewLevelString}"/></span>
                        </span>                        
                        <span class="cell-subfield">
                            <span class="cell-sublabel">Time Down:</span>
                            <span class="incident-table-time-down"><fmt:formatDate pattern="${s:getFriendlyDateTimePattern()}" value="${incident.timeDown}"/></span>
                        </span>
                        <span class="cell-subfield">
                            <span class="cell-sublabel">Time Up:</span>
                            <c:choose>
                                <c:when test="${incident.timeUp ne null}">
                                    <span class="incident-table-time-up"><fmt:formatDate pattern="${s:getFriendlyDateTimePattern()}" value="${incident.timeUp}"/></span>
                                </c:when>
                                <c:otherwise>
                                    <span>(Open)</span>
                                </c:otherwise>
                            </c:choose>
                        </span>
                        <span class="cell-subfield">
                            <span class="cell-sublabel">ePAS:</span>
                            <span class="incident-table-permit-to-work"><c:out value="${empty incident.permitToWork ? 'None' : incident.permitToWork}"/></span>
                        </span>
                    </td>                
                    <td>
                        <span class="cell-subfield">
                            <span class="cell-sublabel">Category:</span>
                            <span class="incident-table-category"><c:out value="${incident.system.category.name}"/></span>
                        </span>                        
                        <span class="cell-subfield">
                            <span class="cell-sublabel">System:</span>
                            <span class="incident-table-system" data-system-id="${incident.system.systemId}"><c:out value="${incident.system.name}"/></span>
                        </span>
                        <span class="cell-subfield">
                            <span class="cell-sublabel">Component:</span>
                            <c:choose>
                                <c:when test="${fn:length(incident.explanation) > 0}">
                                    <span class="incident-table-component" data-component-id="${incident.component.componentId}"><c:out value="${incident.component.name}"/></span><a href="#" class="explanation-link" title="Explanation" data-explanation="${incident.explanation}">*</a>
                                    </c:when>  
                                    <c:otherwise>
                                    <span class="incident-table-component" data-component-id="${incident.component.componentId}"><c:out value="${incident.component.name}"/></span>
                                </c:otherwise>
                            </c:choose>
                            <c:if test="${incident.system.isSrmSystem() and settings.is('SRM_DOWNGRADE_ENABLED')}">
                                <c:url var="url" context="/" value="${settings.get('SRM_DOWNGRADE_URL')}/signoff">
                                    <c:param name="systemId" value="${incident.system.systemId}"/>
                                    <c:param name="componentName" value="${incident.component.name}"/>
                                    <c:param name="groupId" value="-1"/>
                                    <c:param name="subsystemFirst" value="Y"/>
                                    <c:param name="pop" value="true"/>
                                    <c:param name="qualified" value=""/>
                                    <c:param name="signoffStatus" value="Not Ready"/>
                                    <c:param name="comments" value="Downgrade per DTM incident ${incident.incidentId}: ${incident.title}"/>
                                </c:url>
                                <div><a href="${url}">Downgrade Readiness</a></div>
                            </c:if>
                        </span>
                    </td>
                    <c:if test="${settings.is('LOGBOOK_ENABLED')}">
                        <td class="log-entry-cell">
                        <span class="cell-subfield">
                        </span>
                            <form method="get" action="${env['LOGBOOK_SERVER_URL']}/node/add/logentry" target="_blank">
                                <input type="hidden" name="reference" value="dtm:${incident.incidentId}"/>
                                <button type="submit">Create New Log Entry</button>
                            </form>
                            <form method="get" action="${env['LOGBOOK_SERVER_URL']}/entries" target="_blank">
                                <input type="hidden" name="start_date" value="${dtm:formatLogbookDate(incident.timeDown, -1)}"/>
                                <input type="hidden" name="end_date" value="${dtm:formatLogbookDate(incident.timeUp, 1)}"/>
                                <input type="hidden" name="logbooks[0]" value="1"/>
                                <button type="submit">View Interval ± 1Hr</button>
                            </form>
                        </td>
                    </c:if>
                </tr>
            </c:forEach>                                        
        </tbody>
    </table>
</div>