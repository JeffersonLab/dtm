<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@taglib prefix="s" uri="http://jlab.org/jsp/smoothness"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%> 
<%@taglib prefix="dtm" uri="http://jlab.org/dtm/functions"%>
<c:set var="title" value="Repair Assessment Reports"/>
<t:page title="${title}">
    <jsp:attribute name="stylesheets"> 
        <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/resources/v${initParam.releaseNumber}/css/root-cause.css"/>
        <style>
            #page {
                min-width: 1076px;
            }
        </style>
    </jsp:attribute>
    <jsp:attribute name="scripts">      
        <script src="${pageContext.request.contextPath}/resources/v${initParam.releaseNumber}/js/root-cause.js"></script>
    </jsp:attribute>        
    <jsp:body>
        <section>
            <div id="report-page-actions">
                <a target="_blank" href="https://ace.jlab.org/cdn/doc/dtm/RARProcedure.pdf" id="procedure-button">Procedure</a>
                <a target="_blank" href="https://ace.jlab.org/cdn/doc/dtm/RARTemplate3.docx" id="template-3-button">Level Ⅲ Template</a>
                <a target="_blank" href="https://ace.jlab.org/cdn/doc/dtm/RARTemplate4.docx" id="template-4-button">Level Ⅳ Template</a>
                <button id="fullscreen-button">Full Screen</button>
            </div>
            <s:filter-flyout-widget ribbon="true" clearButton="true" resetButton="true">
                <form class="filter-form" method="get" action="rar">
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
                <c:if test="${pageContext.request.userPrincipal ne null}">
                    <button type="button" id="show-sad-rar-dialog-button">Add SAD RAR</button>
                </c:if>
                <c:choose>
                    <c:when test="${fn:length(incidentList) > 0}">
                        <span id="sort-controls"><label for="sort-select">Sort: </label>
                            <select form="filter-form" id="sort-select" name="sort">
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
        <div id="sad-rar-dialog" class="dialog" title="Add SAD RAR">
            <form id="sad-rar-event-pane">
                <ul class="key-value-list">
                    <li>
                        <div class="li-key">
                            <label for="event-title">Title</label>
                        </div>
                        <div class="li-value">
                            <input type="text" id="event-title" name="title"/>
                        </div>
                    </li>
                </ul>
                <fieldset>
                    <ul class="key-value-list">
                        <li>
                            <div class="li-key">
                                <label for="time-down">Time Down</label>
                            </div>
                            <div class="li-value">
                                <input type="text" class="date-field" id="time-down" name="time-down" placeholder="DD-MMM-YYYY hh:mm"/>
                            </div>
                        </li>
                        <li>
                            <div class="li-key">
                                <label for="time-up">Time Up</label>
                            </div>
                            <div class="li-value">
                                <input type="text" class="date-field" id="time-up" name="time-up" placeholder="DD-MMM-YYYY hh:mm"/>
                            </div>
                        </li>
                    </ul>
                    <div><b>Note: </b> SAD RAR Events are still subject to event non-overlap rules so if exact timing is not important (example: RAR summarizes reoccurring issue from previous run) then simply select a four hour block of time starting at midnight on first SAD day and if multiple SAD RARs are needed then either place them on different days or bump events with a time collision later in the day.</div>
                </fieldset>
            </form>
            <form id="sad-rar-upload-pane" method="post" action="./ajax/rar-upload" enctype="multipart/form-data">
                <p><span id="rar-link"></span></p>
                <p><input id="file-upload-input" type="file" name="rar" accept=".pdf"/></p>
                <input id="sad-rar-incident" type="hidden" name="incidentId" value=""/>
            </form>
            <div class="dialog-button-panel">
                <button id="save-sad-rar-button" class="dialog-submit" type="button">Next</button>
                <button id="sad-rar-done-button" class="reload-button" type="button">Done</button>
                <button class="dialog-close-button" type="button">Cancel</button>
            </div>
        </div>
        <div id="exit-fullscreen-panel">
            <button id="exit-fullscreen-button">Exit Full Screen</button>
        </div>
    </jsp:body>         
</t:page>
