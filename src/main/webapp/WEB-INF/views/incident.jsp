<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="jakarta.tags.core"%>
<%@taglib prefix="fn" uri="jakarta.tags.functions"%>
<%@taglib prefix="fmt" uri="jakarta.tags.fmt"%>
<%@taglib prefix="s" uri="jlab.tags.smoothness"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%> 
<%@taglib prefix="dtm" uri="jlab.tags.dtm"%>
<c:set var="title" value="Incident"/>
<s:page title="${title}">
    <jsp:attribute name="stylesheets"> 
        <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/resources/v${initParam.releaseNumber}/css/event-list.css"/>
        <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/resources/v${initParam.releaseNumber}/css/incident.css"/>
        <style>
            #log-entries-table button {
                display: block;
                margin-bottom: 0.5em;
                width: 175px;
            }
        </style>
    </jsp:attribute>
    <jsp:attribute name="scripts">      
        <script type="text/javascript" src="${pageContext.request.contextPath}/resources/v${initParam.releaseNumber}/js/event-list.js"></script>
        <script>
            if(jlab.logbookEnabled) {
                jlab.dtm.loadLogbookReference.call($("#log-entries-table tbody tr"));
            }
        </script>
    </jsp:attribute>
    <jsp:body>
        <div class="banner-breadbox">
            <ul>
                <li>
                    <span>Incidents</span>
                </li>
                <li>
                    <h2 class="page-header-title"><c:out value="#${param.incidentId}: ${incident.title}"/></h2>
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
                            <dt>Period:</dt>
                            <dd>
                                <c:out value="${dtm:millisToHumanReadable(incident.elapsedMillis, true)}"/>
                                <div>
                                    <fmt:formatDate pattern="${s:getFriendlyDateTimePattern()}" value="${incident.timeDown}"/> to
                                    <c:choose>
                                        <c:when test="${incident.timeUp ne null}">
                                            <span class="incident-table-time-up"><fmt:formatDate pattern="${s:getFriendlyDateTimePattern()}" value="${incident.timeUp}"/></span>
                                        </c:when>
                                        <c:otherwise>
                                            <span>(Open)</span>
                                        </c:otherwise>
                                    </c:choose>
                                </div>
                            </dd>
                            <dt>Cause:</dt>
                            <dd>
                                <c:out value="${incident.system.category.name}"/> &gt;
                                <c:out value="${incident.system.name}"/> &gt;
                                <c:out value="${incident.component.name}"/>
                            </dd>
                            <dt>ePAS:</dt>
                            <dd><c:out value="${empty incident.permitToWork ? 'None' : incident.permitToWork}"/></dd>
                        </dl>
                        <c:if test="${settings.is('LOGBOOK_ENABLED')}">
                            <h3>Log Entries</h3>
                            <table id="log-entries-table">
                                <tbody>
                                <tr data-incident-id="${incident.incidentId}">
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
                                </tr>
                                </tbody>
                            </table>
                        </c:if>
                        <h3>Review</h3>
                        <h4>Operability (OPR)</h4>
                        <dl class="indented-dl">
                            <dt>Reviewer:</dt>
                            <dd><c:out value="${s:formatUsername(incident.reviewedUsername)}"/></dd>
                            <dt>Repairer:</dt>
                            <ul>
                                <c:forEach items="${incident.repairedByList}" var="repair">
                                    <li><c:out value="${repair.repairedBy.name}"/></li>
                                </c:forEach>
                            </ul>
                            <dt>Solution:</dt>
                            <dd><c:out value="${incident.resolution}"/></dd>
                        </dl>
                        <h4>Subject Matter Expert (SME)</h4>
                        <c:if test="${pageContext.request.userPrincipal ne null}">
                            <table>
                                <tbody>
                                <tr data-incident-id="${incident.incidentId}" data-solution="${fn:escapeXml(incident.resolution)}" data-repaired-by-id-csv="${incident.repairedByIdCsv}" data-reviewed-by-username-ssv="${incident.reviewedByUsernameSsv}" data-repaired-by-formatted="${incident.repairedByIdCsv != null ? dtm:formatGroupList(incident.repairedByIdCsv, groupList) : '--None--'}" data-reviewed-by="${fn:escapeXml(incident.reviewedUsername)}" data-reviewed-by-formatted="${incident.reviewedUsername != null ? s:formatUsername(incident.reviewedUsername) : ''}" data-reviewed-by-experts-formatted-tsv="${incident.reviewedByExpertsFormattedTsv}" data-acknowledged="${fn:escapeXml(incident.expertAcknowledged)}" data-root-cause="${fn:escapeXml(incident.rootCause)}" data-rar-id="${incident.rarId}" data-rar-ext="${incident.rarExt}" data-review-level="${incident.reviewLevelString}">
                                    <td>
                                        <button class="open-edit-expert-review-dialog-button">Edit SME Review</button>
                                    </td>
                                </tr>
                                </tbody>
                            </table>
                        </c:if>
                        <dl class="indented-dl">
                            <dt>Review Level:</dt>
                            <dd><c:out value="${incident.reviewLevelString}"/></dd>
                            <dt>Reviewers:</dt>
                            <dd>
                                <ul>
                                <c:forEach items="${incident.incidentReviewList}" var="review">
                                    <li><c:out value="${s:formatUsername(review.reviewer)}"/></li>
                                </c:forEach>
                                </ul>
                            </dd>
                            <dt>Acknowledged:</dt>
                            <dd>
                                <c:choose>
                                    <c:when test="${incident.expertAcknowledged eq 'Y'}">
                                        Yes
                                    </c:when>
                                    <c:when test="${incident.expertAcknowledged eq 'N'}">
                                        No
                                    </c:when>
                                    <c:when test="${incident.expertAcknowledged eq 'R'}">
                                        Reassign
                                    </c:when>
                                </c:choose>
                            </dd>
                            <dt>Root Cause:</dt>
                            <dd><c:out value="${incident.rootCause}"/></dd>
                            <dt>Repair Assessment Report (RAR):</dt>
                            <dd>
                                <c:choose>
                                    <c:when test="${incident.rarExt ne null}">
                                        <a href="${pageContext.request.contextPath}/ajax/rar-download?incidentId=${incident.incidentId}">RAR Document</a>
                                    </c:when>
                                    <c:otherwise>
                                        None
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
        <t:event-list-dialogs eventTypeList="${eventTypeList}" systemList="${systemList}"/>
    </jsp:body>         
</s:page>
