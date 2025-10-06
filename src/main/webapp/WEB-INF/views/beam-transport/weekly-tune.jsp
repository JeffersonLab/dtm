<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="jakarta.tags.core"%>
<%@taglib prefix="fn" uri="jakarta.tags.functions"%>
<%@taglib prefix="fmt" uri="jakarta.tags.fmt"%>
<%@taglib prefix="s" uri="jlab.tags.smoothness"%>
<%@taglib prefix="dtm" uri="http://jlab.org/dtm/functions"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%> 
<jsp:useBean id="now" class="java.util.Date"/>
<c:set var="title" value="Accelerator Beam Tune Report"/>
<t:beam-transport-page title="${title}">
    <jsp:attribute name="stylesheets">
        <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/resources/v${initParam.releaseNumber}/css/downtime-report.css"/>
        <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/resources/v${initParam.releaseNumber}/css/event-list.css"/>
        <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/resources/v${initParam.releaseNumber}/css/weekly-repair.css"/>
        <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/resources/v${initParam.releaseNumber}/css/weekly-tune.css"/>
    </jsp:attribute>
    <jsp:attribute name="scripts"> 
        <script type="text/javascript" src="${pageContext.request.contextPath}/resources/v${initParam.releaseNumber}/js/event-list.js"></script>
        <script type="text/javascript" src="${pageContext.request.contextPath}/resources/v${initParam.releaseNumber}/js/weekly-repair.js"></script>
    </jsp:attribute>        
    <jsp:body>
        <section>
            <fmt:formatDate var="startFmt" value="${start}" pattern="${s:getFriendlyDateTimePattern()}"/>
            <fmt:formatDate var="endFmt" value="${end}" pattern="${s:getFriendlyDateTimePattern()}"/>
            <fmt:formatDate var="fourWeeksAgoInclusiveFmt" value="${fourWeeksAgoInclusive}" pattern="${s:getFriendlyDateTimePattern()}"/>
            <div id="report-page-actions">
                <button id="fullscreen-button">Full Screen</button>
                <div id="export-widget">
                    <button id="export-menu-button">Export</button>
                    <ul id="export-menu">
                        <li id="image-menu-item">Image</li>
                        <li id="print-menu-item">Print</li>
                        <li id="excel-menu-item">Excel</li>                         
                    </ul>
                </div>
            </div>            
            <s:filter-flyout-widget  requiredMessage="true">
                <form class="filter-form" method="get" action="weekly-tune">
                    <fieldset>
                        <ul class="key-value-list">                         
                            <li>
                                <div class="li-key">
                                    <label class="required-field" for="start" title="Inclusive">Start Date</label>
                                </div>
                                <div class="li-value">
                                    <input type="text" class="date-field" id="start" name="start" placeholder="DD-MMM-YYYY hh:mm" value="${param.start != null ? fn:escapeXml(param.start) : sevenDaysAgoFmt}"/>
                                </div>
                            </li>                     
                            <li>
                                <div class="li-key">
                                    <label class="required-field" for="max">Top N</label>
                                </div>
                                <div class="li-value">
                                    <input type="number" min="1" max="100" id="max" name="max" value="${max}"/>
                                </div>
                            </li>
                        </ul>
                    </fieldset>
                    <input type="hidden" class="offset-input" name="offset" value="0"/>
                    <input class="filter-form-submit-button" type="submit" value="Apply"/>
                </form>                                                    
            </s:filter-flyout-widget>
            <h2 class="page-header-title"><c:out value="${title}"/></h2>
            <c:choose>
                <c:when test="${start == null || end == null}">
                    <div class="message-box">Select a start date to continue</div>
                </c:when>
                <c:otherwise>
                    <div class="message-box">
                        <c:out value="${selectionMessage}"/>
                    </div>
                    <c:choose>
                        <c:when test="${fn:length(incidentList) > 0}">
                            <table class="data-table stripped-table downtime-data${sessionScope.effectiveRole eq 'REVIEWER' ? ' editable-table' : ''}">
                                <thead>
                                    <tr>
                                        <th style="width: 75px;">Duration (Hours)</th>
                                        <th style="width: 285px;">Problem / Symptom</th>
                                        <th style="width: 285px;">Solution</th>                                        
                                        <th style="width: 125px;">Component</th>
                                        <th style="width: 85px;">Incidents Last 4 Weeks</th>
                                        <th style="width: 125px;">Repaired By</th>
                                    </tr>
                                </thead>
                                <tfoot>
                                    <c:if test="${totalRecords > max}">
                                        <tr>
                                            <th class="center-aligned"><fmt:formatNumber value="${totalTuneTime - topDowntime}" pattern="#,##0.0"/></th>
                                            <th colspan="2" class="left-aligned"><c:out value="${totalRecords - max}"/> Other Incidents</th>
                                            <th colspan="4" class="unused-cells"></th>
                                        </tr>
                                    </c:if>
                                    <tr class="total-repair-row">
                                        <th class="center-aligned"><fmt:formatNumber value="${totalTuneTime}" pattern="#,##0.0"/></th>
                                        <th colspan="2" class="left-aligned" style="padding: 0;"><div class="div-cell" style="width: 600px; vertical-align: middle;">Total Tune Time</div><div class="div-cell" style="border-left: 1px solid black; text-align: center;"><fmt:formatNumber value="${totalTuneTime / totalRecords}" pattern="#,##0.0"/> <span title="Mean Time To Recover">MTTR</span></div></th>
                                        <th colspan="3" class="unused-cells"></th>
                                    </tr>
                                </tfoot>
                                <tbody class="reload-after-edit">
                                    <c:forEach items="${incidentList}" var="incident">
                                        <fmt:formatNumber value="${incident.downtimeHours}" var="formattedUnbounded" pattern="#,##0.0"/>
                                        <fmt:formatDate value="${incident.timeDown}" var="formattedIncidentDown" pattern="${s:getFriendlyDateTimePattern()}"/>
                                        <fmt:formatDate value="${incident.timeUp}" var="formattedIncidentUp" pattern="${s:getFriendlyDateTimePattern()}"/>
                                        <tr data-incident-id="${incident.incidentId}" data-event-id="${incident.eventId}" data-event-title="${incident.eventTitle}" data-incident-down="${formattedIncidentDown}" data-incident-up="${formattedIncidentUp}" data-system-id="${incident.systemId}" data-component-name="${fn:escapeXml(incident.componentName)}" data-component-id="${incident.componentId}" data-explanation="${fn:escapeXml(incident.explanation)}" data-repaired-by-id-csv="${incident.repairedByIdCsv}" data-reviewed-by="${fn:escapeXml(incident.reviewedByUsername)}">
                                            <td title="Not Bounded: ${formattedUnbounded}"><fmt:formatNumber value="${incident.downtimeHoursBounded}" pattern="#,##0.0"/></td>
                                            <td class="problem-td">
                                                <a class="incident-title-link" href="${pageContext.request.contextPath}/incidents/${incident.incidentId}" title="${fn:escapeXml(incident.summary)}"><c:out value="${incident.title}"/></a>
                                                <div class="start-time-subcell" title="Not Bounded: ${fn:escapeXml(dtm:formatSmartDate(incident.timeDown))}">
                                                    <c:choose>
                                                        <c:when test="${incident.timeDown.time < start.time}">
                                                            <c:out value="${dtm:formatSmartDate(start)}"/>
                                                        </c:when>
                                                        <c:otherwise>
                                                            <c:out value="${dtm:formatSmartDate(incident.timeDown)}"/>
                                                        </c:otherwise>
                                                    </c:choose>
                                                </div>
                                            </td>
                                            <td class="resolution-field"><span class="read-field"><c:out value="${incident.resolution}"/></span><span style="display: none;" class="write-field"><textarea><c:out value="${incident.resolution}"/></textarea></span></td>
                                                        <c:url var="url" value="/beam-transport/tune-incidents">
                                                            <c:param name="start" value="${fourWeeksAgoInclusiveFmt}"/>
                                                            <c:param name="end" value="${endFmt}"/>
                                                            <c:param name="type" value=""/>
                                                            <c:param name="component" value="${incident.componentName}"/>
                                                            <c:param name="print" value="${fn:escapeXml(param.print)}"/>
                                                            <c:param name="fullscreen" value="${fn:escapeXml(param.fullscreen)}"/>
                                                            <c:param name="qualified" value=""/>
                                                        </c:url>
                                            <td><c:out value="${incident.componentName}"/></td>
                                            <td class="center-aligned${incident.frequency > 1 ? ' repeat-offender' : ''}"><a href="${fn:escapeXml(url)}"><c:out value="${incident.frequency}"/></a></td>
                                            <td class="repaired-by-field">
                                                <span class="read-field">
                                                    <c:forEach items="${incident.repairedByList}" var="group" varStatus="status">
                                                        <c:choose>
                                                            <c:when test="${status.last}">
                                                                <c:out value="${group.name}"/>
                                                            </c:when>
                                                            <c:otherwise>
                                                                <c:out value="${group.name.concat(';')}"/>
                                                            </c:otherwise>    
                                                        </c:choose>
                                                    </c:forEach>
                                                </span>
                                                <span style="display: none;" class="write-field">
                                                <select class="repaired-by-select" multiple="multiple">
                                                        <c:forEach items="${groupList}" var="group">
                                                        <option value="${group.workgroupId}"${s:inArray(incident.repairedByList.toArray(), group) ? ' selected="selected"' : ''}><c:out value="${group.name}"/></option>
                                                        </c:forEach>
                                                </select>
                                                </span>
                                            </td>                                               
                                        </tr>
                                    </c:forEach>
                                </tbody>
                            </table>
                        </c:when>
                        <c:otherwise>
                            <div>No incidents this week!</div>
                        </c:otherwise>
                    </c:choose>
                </c:otherwise>
            </c:choose>
        </section>
        <div id="exit-fullscreen-panel">
            <button id="exit-fullscreen-button">Exit Full Screen</button>
        </div>
        <form id="excel-form" method="get" action="${pageContext.request.contextPath}/excel/tune-incident-list.xlsx">
            <input type="hidden" name="start" value="${startFmt}"/>
            <input type="hidden" name="end" value="${endFmt}"/>
            <input type="hidden" name="type" value="1"/>
            <input type="hidden" name="component" value=""/>
            <button id="excel" type="submit" style="display: none;">Excel</button>
        </form>            
        <t:event-list-dialogs eventTypeList="${eventTypeList}" systemList="${systemList}"/>        
    </jsp:body>         
</t:beam-transport-page>