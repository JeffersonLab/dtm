<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="jakarta.tags.core"%>
<%@taglib prefix="fn" uri="jakarta.tags.functions"%>
<%@taglib prefix="fmt" uri="jakarta.tags.fmt"%>
<%@taglib prefix="s" uri="http://jlab.org/jsp/smoothness"%>
<%@taglib prefix="dtm" uri="http://jlab.org/dtm/functions"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%> 
<jsp:useBean id="now" class="java.util.Date"/>
<c:set var="title" value="Accelerator Monthly Availability Report"/>
<t:operability-page title="${title}">  
    <jsp:attribute name="stylesheets">      
        <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/resources/v${initParam.releaseNumber}/css/weekly-repair.css"/>
        <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/resources/v${initParam.releaseNumber}/css/monthly-repair.css"/>
    </jsp:attribute>
    <jsp:attribute name="scripts">
        <c:choose>
            <c:when test="${'CDN' eq resourceLocation}">
                <script src="//${env['CDN_SERVER']}/jquery-plugins/flot/0.8.3/jquery.flot.min.js"></script>
                <script src="//${env['CDN_SERVER']}/jquery-plugins/flot/0.8.3/jquery.flot.stack.min.js"></script>
                <script src="//${env['CDN_SERVER']}/jquery-plugins/flot/0.8.3/jquery.flot.resize.min.js"></script>
            </c:when>
            <c:otherwise><!-- LOCAL -->
                <script src="${pageContext.request.contextPath}/resources/jquery-plugins/flot/0.8.3/jquery.flot.min.js"></script>
                <script src="${pageContext.request.contextPath}/resources/jquery-plugins/flot/0.8.3/jquery.flot.stack.min.js"></script>
                <script src="${pageContext.request.contextPath}/resources/jquery-plugins/flot/0.8.3/jquery.flot.resize.min.js"></script>
            </c:otherwise>
        </c:choose>
        <script type="text/javascript" src="${pageContext.request.contextPath}/resources/v${initParam.releaseNumber}/js/flot-time.js"></script>
        <script type="text/javascript" src="${pageContext.request.contextPath}/resources/v${initParam.releaseNumber}/js/monthly-repair.js"></script>
    </jsp:attribute>        
    <jsp:body>
        <section>
            <fmt:formatDate var="startFmt" value="${start}" pattern="${s:getFriendlyDateTimePattern()}"/>
            <fmt:formatDate var="formattedDate" value="${start}" pattern="MMMM yyyy"/>
            <fmt:formatDate var="endFmt" value="${end}" pattern="${s:getFriendlyDateTimePattern()}"/>
            <fmt:formatDate var="fourWeeksAgoInclusiveFmt" value="${fourWeeksAgoInclusive}" pattern="${s:getFriendlyDateTimePattern()}"/>
            <div id="report-page-actions">               
                <button id="fullscreen-button">Full Screen</button>
                <div id="export-widget">
                    <button id="export-menu-button">Export</button>
                    <ul id="export-menu">
                        <li id="image-menu-item">Image</li>
                        <li id="print-menu-item">Print</li>
                    </ul>
                </div>
                <c:if test="${sessionScope.effectiveRole eq 'REVIEWER'}">
                    <div id="secondary-page-actions">
                        <br/>                    
                        <button id="edit-button">Edit</button>
                        <button id="save-button">Save</button>
                        <button id="cancel-button">Cancel</button>
                    </div> 
                </c:if>       
            </div>
            <s:filter-flyout-widget  requiredMessage="true">
                <form class="filter-form" method="get" action="monthly-repair">
                    <fieldset>
                        <ul class="key-value-list">                         
                            <li>
                                <div class="li-key">
                                    <label class="required-field" for="date" title="Inclusive">Month</label>
                                </div>
                                <div class="li-value">
                                    <div class="li-value"><input id="date" name="date" class="monthpicker" placeholder="MMMM YYYY" type="text" value="${formattedDate}"/></div>
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
                    <fmt:formatDate var="previousMonthFmt" value="${previousMonth}" pattern="MMMM yyyy"/>
                    <fmt:formatDate var="nextMonthFmt" value="${nextMonth}" pattern="MMMM yyyy"/>
                    <c:url var="previousUrl" value="/operability/monthly-repair">
                        <c:param name="date" value="${previousMonthFmt}"/>
                        <c:param name="max" value="${param.max}"/>
                        <c:param name="print" value="${param.print}"/>
                        <c:param name="fullscreen" value="${param.fullscreen}"/>
                        <c:param name="qualified" value=""/>
                    </c:url>                     
                    <c:url var="nextUrl" value="/operability/monthly-repair">
                        <c:param name="date" value="${nextMonthFmt}"/>
                        <c:param name="max" value="${param.max}"/> 
                        <c:param name="print" value="${param.print}"/>
                        <c:param name="fullscreen" value="${param.fullscreen}"/>
                        <c:param name="qualified" value=""/>
                    </c:url>                     
                    <div class="float-breadbox">
                        <ul>
                            <li>
                                <a href="${previousUrl}">Previous</a>
                            </li>
                            <li>
                                <a href="${nextUrl}">Next</a>
                            </li>
                        </ul>
                    </div>
                    <div class="message-box"><c:out value="${selectionMessage}"/></div>
                    <table class="two-column-table">   
                        <tbody>
                            <tr>
                                <td class="left-cell">
                                    <h3>Accelerator Overall Metrics<sup>1</sup></h3>
                                    <table id="overall-table" class="data-table">
                                        <thead>
                                            <tr>
                                                <th style="width: 75px;">Downtime (Hours)</th> 
                                                <th style="width: 75px;">Uptime (Hours)</th>
                                                <th style="width: 75px;" title="Availability">AVAIL /Trend</th>
                                                <th style="width: 75px;" title="Availability Goal">AVAIL Goal</th>
                                            </tr>
                                        </thead>
                                        <tbody>
                                            <tr>
                                                <td class="right-aligned"><fmt:formatNumber pattern="#,###,##0.0" value="${data.accDownHours}"/></td>
                                                <td class="right-aligned"><fmt:formatNumber pattern="#,###,##0.0" value="${data.accUptimeHours}"/></td> 

                                                <fmt:formatNumber value="${lastMonthData.accAvailability}" pattern="#,##0.0" var="lastMonthAvailabilityFormatted"/>
                                                <c:set var="trendUp" value="${data.accAvailability > lastMonthData.accAvailability + 1}"/>
                                                <c:set var="trendDown" value="${data.accAvailability < lastMonthData.accAvailability - 1}"/>
                                                <c:set var="trendSame" value="${not trendUp and not trendDown}"/>                                                

                                                <td class="right-aligned availability"><fmt:formatNumber pattern="#,###,##0.0" value="${data.accAvailability}"/>% <span class="${trendUp ? 'trend-up' : ''} ${trendDown ? 'trend-down' : ''} ${trendSame ? 'trend-same' : ''}" title="Last Month: ${lastMonthAvailabilityFormatted}%">${trendUp ? '▲' : ''}${trendDown ? '▼' : ''}${trendSame ? '-' : ''}</span></td>
                                                <fmt:formatNumber value="${monthInfo.machineGoal}" pattern="##0.0" var="machineGoal"/>
                                                <td class="right-aligned"><span class="goal-output">${monthInfo.machineGoal ne null ? machineGoal.concat('%') : ''}</span><span class="goal-input"><input id="machineGoalInput" type="number" step="0.1" max="100" min="0" value="${machineGoal}"/></span></td>
                                            </tr>
                                        </tbody>
                                    </table>  
                                    <c:url var="url" value="/reports/downtime-summary">
                                        <c:param name="start" value="${startFmt}"/>
                                        <c:param name="end" value="${endFmt}"/>
                                        <c:param name="type" value="1"/>
                                        <c:param name="print" value="${param.print}"/>
                                        <c:param name="fullscreen" value="${param.fullscreen}"/>
                                        <c:param name="qualified" value=""/>
                                    </c:url>
                                    <div class="table-footnotes">1 <a href="${url}">Includes both trips and repairs; trend vs last month</a></div>


                                </td>
                                <td>





                                    <h3>Occurrences By Duration<sup>2</sup></h3>
                                    <table class="data-table">
                                        <thead>
                                            <tr>
                                                <th style="width: 150px;">Duration</th>
                                                <th style="width: 75px;" class="downtime">Downtime (Hours)</th>
                                                <th style="width: 75px;" class="count" title="Number of Failures">Count</th>
                                                <th style="width: 75px;" class="mttr" title="Mean Time to Recover">MTTR</th>
                                                <th style="width: 75px;" class="mtbf" title="Mean time between Failures">MTBF</th>
                                                <th style="width: 75px;" class="availability" title="Availability">AVAIL /Trend</th>
                                                <th style="width: 75px;" class="goal" title="Availability Goal">AVAIL Goal</th>
                                            </tr>
                                        </thead>
                                        <tbody>
                                            <tr>
                                                <td>
                                                    <c:url var="url" value="/reports/fsd-summary">
                                                        <c:param name="start" value="${startFmt}"/>
                                                        <c:param name="end" value="${endFmt}"/>
                                                        <c:param name="maxDuration" value="5"/>
                                                        <c:param name="maxDurationUnits" value="Minutes"/>
                                                        <c:param name="chart" value="bar"/>
                                                        <c:param name="binSize" value="DAY"/>
                                                        <c:param name="maxY" value="17"/>
                                                        <c:param name="legendData" value="rate"/>
                                                        <c:param name="legendData" value="lost"/>
                                                        <c:param name="grouping" value="category"/>
                                                        <c:param name="maxTypes" value=""/>
                                                        <c:param name="sadTrips" value="N"/>
                                                        <c:param name="rateBasis" value="program"/>                                                        
                                                        <c:param name="print" value="${param.print}"/>
                                                        <c:param name="fullscreen" value="${param.fullscreen}"/>
                                                        <c:param name="qualified" value=""/>
                                                    </c:url>
                                                    <a href="${url}" title="Trip Report">Trips <br/>(&lt;= 5 mins)</a>
                                                </td>
                                                <td class="downtime right-aligned"><fmt:formatNumber pattern="#,###,##0.0" value="${data.tripHours}"/></td>
                                                <td class="count right-aligned"><fmt:formatNumber pattern="#,###,##0" value="${data.tripCount}"/></td>
                                                <td class="mttr right-aligned"><fmt:formatNumber pattern="#,###,##0.0" value="${data.tripMttrHours * 60}"/>m</td>
                                                <td class="mtbf right-aligned"><fmt:formatNumber pattern="#,###,##0.0" value="${data.mtbtHours * 60}"/>m</td>

                                                <fmt:formatNumber value="${lastMonthData.tripAvailability}" pattern="#,##0.0" var="lastMonthAvailabilityFormatted"/>
                                                <c:set var="trendUp" value="${data.tripAvailability > lastMonthData.tripAvailability + 1}"/>
                                                <c:set var="trendDown" value="${data.tripAvailability < lastMonthData.tripAvailability - 1}"/>
                                                <c:set var="trendSame" value="${not trendUp and not trendDown}"/>    

                                                <td class="right-aligned availability${data.tripAvailability < monthInfo.tripGoal ? ' less-than-goal' : ''}"><fmt:formatNumber pattern="#,###,##0.0" value="${data.tripAvailability}"/>% <span class="${trendUp ? 'trend-up' : ''} ${trendDown ? 'trend-down' : ''} ${trendSame ? 'trend-same' : ''}" title="Last Month: ${lastMonthAvailabilityFormatted}%">${trendUp ? '▲' : ''}${trendDown ? '▼' : ''}${trendSame ? '-' : ''}</span></td>
                                                <fmt:formatNumber value="${monthInfo.tripGoal}" pattern="##0.0" var="tripGoal"/>
                                                <td class="right-aligned"><span class="goal-output">${monthInfo.tripGoal ne null ? tripGoal.concat('%') : ''}</span><span class="goal-input"><input id="tripGoalInput" type="number" step="0.1" max="100" min="0" value="${tripGoal}"/></span></td>                                                
                                            </tr>
                                            <tr>
                                                <td>
                                                    <c:url var="url" value="/reports/event-downtime">
                                                        <c:param name="start" value="${startFmt}"/>
                                                        <c:param name="end" value="${endFmt}"/>
                                                        <c:param name="type" value="1"/>
                                                        <c:param name="transport" value=""/>
                                                        <c:param name="print" value="${param.print}"/>
                                                        <c:param name="fullscreen" value="${param.fullscreen}"/>
                                                        <c:param name="qualified" value=""/>
                                                    </c:url>
                                                    <a href="${url}" title="Event Report">Repairs <br/>(&gt; 5 mins)</a></td>
                                                <td class="right-aligned"><fmt:formatNumber pattern="#,###,##0.0" value="${data.eventHours}"/></td>
                                                <td class="right-aligned"><fmt:formatNumber pattern="#,###,##0" value="${data.eventCount}"/></td>
                                                <td class="right-aligned"><fmt:formatNumber pattern="#,###,##0.0" value="${data.eventMttrHours}"/>h</td>
                                                <td class="right-aligned"><fmt:formatNumber pattern="#,###,##0.0" value="${data.eventMtbfHours}"/>h</td>

                                                <fmt:formatNumber value="${lastMonthData.eventAvailability}" pattern="#,##0.0" var="lastMonthAvailabilityFormatted"/>
                                                <c:set var="trendUp" value="${data.eventAvailability > lastMonthData.eventAvailability + 1}"/>
                                                <c:set var="trendDown" value="${data.eventAvailability < lastMonthData.eventAvailability - 1}"/>
                                                <c:set var="trendSame" value="${not trendUp and not trendDown}"/>    

                                                <td class="right-aligned availability${data.eventAvailability < 88 ? ' less-than-goal' : ''}"><fmt:formatNumber pattern="#,###,##0.0" value="${data.eventAvailability}"/>% <span class="${trendUp ? 'trend-up' : ''} ${trendDown ? 'trend-down' : ''} ${trendSame ? 'trend-same' : ''}" title="Last Month: ${lastMonthAvailabilityFormatted}%">${trendUp ? '▲' : ''}${trendDown ? '▼' : ''}${trendSame ? '-' : ''}</span></td>                                                
                                                <fmt:formatNumber value="${monthInfo.eventGoal}" pattern="##0.0" var="eventGoal"/>
                                                <td class="right-aligned"><span class="goal-output">${monthInfo.eventGoal ne null ? eventGoal.concat('%') : ''}</span><span class="goal-input"><input id="eventGoalInput" type="number" step="0.1" max="100" min="0" value="${eventGoal}"/></span></td>
                                            </tr>
                                        </tbody>
                                    </table>
                                    <c:url var="url" value="/reports/downtime-summary">
                                        <c:param name="start" value="${startFmt}"/>
                                        <c:param name="end" value="${endFmt}"/>
                                        <c:param name="type" value="1"/>
                                        <c:param name="print" value="${param.print}"/>
                                        <c:param name="fullscreen" value="${param.fullscreen}"/>
                                        <c:param name="qualified" value=""/>
                                    </c:url>
                                    <div class="table-footnotes">2 <a href="${url}">AVAIL represents max machine availability considering duration</a></div>



                                </td>
                            </tr>
                            <tr>
                                <td class="left-cell">
                                    <h3>Top Repairs<sup>3</sup></h3>
                                    <c:choose>                        
                                        <c:when test="${fn:length(incidentList) > 0}">                      
                                            <table class="data-table stripped-table downtime-data">
                                                <thead>
                                                    <tr>
                                                        <th style="width: 75px;">Duration (Hours)</th>
                                                        <th style="width: 285px;">Problem / Symptom</th>
                                                    </tr>
                                                </thead>
                                                <tfoot>
                                                    <c:if test="${totalRecords > max}">
                                                        <tr>
                                                            <th class="center-aligned"><fmt:formatNumber value="${totalRepairTime - topDowntime}" pattern="#,##0.0"/></th>
                                                            <th colspan="1" class="left-aligned"><c:out value="${totalRecords - max}"/> Other Repairs</th>
                                                        </tr>
                                                    </c:if>
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
                                                                            <c:out value="${dtm:formatConciseSmartDate(start)}"/>
                                                                        </c:when>
                                                                        <c:otherwise>
                                                                            <c:out value="${dtm:formatConciseSmartDate(incident.timeDown)}"/>
                                                                        </c:otherwise>
                                                                    </c:choose>
                                                                </div>
                                                                <div class="category-subcell">
                                                                    <c:out value="${incident.alphaCatName}"/>
                                                                </div>
                                                            </td>                                            
                                                        </tr>
                                                    </c:forEach>
                                                </tbody>
                                            </table>
                                            <c:url var="url" value="/reports/downtime-summary">
                                                <c:param name="start" value="${startFmt}"/>
                                                <c:param name="end" value="${endFmt}"/>
                                                <c:param name="type" value="1"/>
                                                <c:param name="print" value="${param.print}"/>
                                                <c:param name="fullscreen" value="${param.fullscreen}"/>
                                                <c:param name="qualified" value=""/>
                                            </c:url>
                                            <div class="table-footnotes">3 <a href="${url}">Excluding Beam Transport</a></div>
                                        </c:when>
                                        <c:otherwise>
                                            <div>None</div>
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                                <td>                                
                                    <h3>Repairs By Category<sup>4</sup></h3>
                                    <c:choose>
                                        <c:when test="${fn:length(data.categoryDowntimeList) > 0}">
                                            <table id="bar-chart-data-table" class="data-table stripped-table" data-x-label="Category">
                                                <thead>
                                                    <tr>
                                                        <th style="width: 150px;">Category</th>
                                                        <th style="width: 75px;" class="downtime">Repair Time (Hours)</th>
                                                        <th style="width: 75px;" class="count" title="Number of Incidents">Count</th>
                                                        <th style="width: 75px;" class="mttr" title="Mean Time to Recover">MTTR (Hours)</th>
                                                        <th style="width: 75px;" class="mtbf" title="Mean time between Failures">MTBF (Hours)</th>
                                                        <th style="width: 75px;" class="availability" title="Availability">AVAIL /Trend</th>
                                                        <th style="width: 75px;" class="goal" title="Availability Goal">AVAIL Goal</th>
                                                    </tr>
                                                </thead>
                                                <tbody>
                                                    <c:forEach items="${data.categoryDowntimeList}" var="downtime">
                                                        <c:set var="lastMonthDowntime" value="${lastMonthDowntimeMap[downtime.id]}"/>
                                                        <tr data-id="${downtime.id}">
                                                            <td>
                                                                <c:url var="url" value="/reports/system-downtime">
                                                                    <c:param name="start" value="${startFmt}"/>
                                                                    <c:param name="end" value="${endFmt}"/>
                                                                    <c:param name="type" value="1"/>
                                                                    <c:param name="transport" value=""/>
                                                                    <c:param name="category" value="${downtime.id}"/>
                                                                    <c:param name="chart" value="table"/>
                                                                    <c:param name="data" value="downtime"/>
                                                                    <c:param name="packed" value="Y"/>
                                                                    <c:param name="print" value="${param.print}"/>
                                                                    <c:param name="fullscreen" value="${param.fullscreen}"/>
                                                                    <c:param name="qualified" value=""/>
                                                                </c:url>
                                                                <a href="${url}" title="System Downtime Report"><c:out value="${downtime.name}"/></a>
                                                            </td>
                                                            <td class="downtime right-aligned"><fmt:formatNumber value="${downtime.duration * 24}" pattern="#,##0.0"/></td>
                                                            <td class="count right-aligned"><fmt:formatNumber value="${downtime.incidentCount}" pattern="#,##0"/></td>
                                                            <td class="mttr right-aligned"><fmt:formatNumber value="${downtime.incidentCount == 0 ? 0 : downtime.duration / downtime.incidentCount * 24}" pattern="#,##0.0"/></td>
                                                            <c:set var="uptime" value="${data.programHours - (downtime.duration * 24)}"/>
                                                            <c:set var="uptime" value="${uptime < 0 ? 0 : uptime}"/>
                                                            <c:set var="mtbf" value="${downtime.incidentCount == 0 ? '' : uptime / downtime.incidentCount}"/>
                                                            <c:set var="availability" value="${data.programHours == 0 ? 0 : uptime / data.programHours * 100}"/>

                                                            <c:set var="lastMonthUptime" value="${lastMonthData.programHours - (lastMonthDowntime.duration * 24)}"/>
                                                            <c:set var="lastMonthUptime" value="${lastMonthUptime < 0 ? 0 : lastMonthUptime}"/>
                                                            <c:set var="lastMonthAvailability" value="${lastMonthData.programHours == 0 ? 0 : lastMonthUptime / lastMonthData.programHours * 100}"/>
                                                            <fmt:formatNumber value="${lastMonthAvailability}" pattern="#,##0.0" var="lastMonthAvailabilityFormatted"/>
                                                            <c:set var="trendUp" value="${availability > lastMonthAvailability + 1}"/>
                                                            <c:set var="trendDown" value="${availability < lastMonthAvailability - 1}"/>
                                                            <c:set var="trendSame" value="${not trendUp and not trendDown}"/>

                                                            <td class="mtbf right-aligned"><fmt:formatNumber value="${mtbf}" pattern="#,##0.0"/></td>
                                                            <td class="availability right-aligned${availability < goalMap[downtime.id].goal ? ' less-than-goal' : ''}"><fmt:formatNumber value="${availability}" pattern="#,##0.0"/>% <span class="${trendUp ? 'trend-up' : ''} ${trendDown ? 'trend-down' : ''} ${trendSame ? 'trend-same' : ''}" title="Last Month: ${lastMonthAvailabilityFormatted}%">${trendUp ? '▲' : ''}${trendDown ? '▼' : ''}${trendSame ? '-' : ''}</span></td>
                                                            <fmt:formatNumber value="${goalMap[downtime.id].goal}" pattern="##0.0" var="catGoal"/>
                                                            <td class="goal right-aligned"><span class="goal-output">${goalMap[downtime.id].goal ne null ? catGoal.concat('%') : ''}</span><span class="goal-input"><input type="number" step="0.1" max="100" min="0" value="${catGoal}"/></span></td>
                                                        </tr>
                                                    </c:forEach>
                                                </tbody>
                                            </table>   
                                            <c:url var="url" value="/reports/category-downtime">
                                                <c:param name="start" value="${startFmt}"/>
                                                <c:param name="end" value="${endFmt}"/>
                                                <c:param name="transport" value=""/>
                                                <c:param name="type" value="1"/>
                                                <c:param name="packed" value="Y"/>
                                                <c:param name="chart" value="pareto"/>
                                                <c:param name="data" value="downtime"/>
                                                <c:param name="print" value="${param.print}"/>
                                                <c:param name="fullscreen" value="${param.fullscreen}"/>
                                                <c:param name="qualified" value=""/>
                                            </c:url>
                                            <div class="table-footnotes">4 <a href="${url}">Repairs may be concurrent</a></div>
                                        </c:when>
                                        <c:otherwise>
                                            <div>None</div>
                                        </c:otherwise>
                                    </c:choose>    
                                </td>
                            </tr>
                        </tbody>
                    </table>

                    <div id="second-page" data-start="${startFmt}" data-end="${endFmt}">
                        <h3 class="second-page-chart-header">Repairs By Category and Day<sup>5</sup></h3>
                        <c:choose>
                            <c:when test="${fn:length(chartRecordList) > 0}">
                                <c:if test="${param.chart ne 'table'}">
                                    <s:chart-widget>
                                        <div class="footnote-wrapper" style="display: none;">
                                            <div class="chart-footnote">
                                                <ul>
                                                    <c:url var="url" value="/reports/downtime-summary">
                                                        <c:param name="start" value="${startFmt}"/>
                                                        <c:param name="end" value="${endFmt}"/>
                                                        <c:param name="type" value="1"/>
                                                        <c:param name="print" value="${param.print}"/>
                                                        <c:param name="fullscreen" value="${param.fullscreen}"/>
                                                        <c:param name="qualified" value=""/>
                                                    </c:url>
                                                    <li>5 <a href="${url}">Repairs may be concurrent (may exceed 24 repair hours a day)</a></li>
                                                        <c:forEach items="${footnoteList}" var="note">
                                                        <li><c:out value="${note}"/></li>
                                                        </c:forEach>
                                                </ul>
                                            </div>
                                        </div>
                                    </s:chart-widget>
                                </c:if>
                                <div class="data-table-panel" style="${param.chart eq 'table' ? '' : 'display: none;'}">
                                    <table id="graph-data-table" class="data-table stripped-table" data-start-millis="${dtm:getLocalTime(start)}" data-end-millis="${dtm:getLocalTime(end)}">
                                        <thead>
                                            <tr>
                                                <th>Category</th>
                                                <th>Day</th>
                                                <th>Incident Downtime (Hours)</th>
                                            </tr>
                                        </thead>
                                        <tbody>
                                            <c:forEach items="${chartRecordList}" var="record">
                                                <tr>
                                                    <td data-id="${record.categoryId}"><c:out value="${record.category}"/></td>
                                                    <td data-date-utc="${dtm:getLocalTime(record.day)}"><fmt:formatDate pattern="dd MMM yyyy" value="${record.day}"/></td>
                                                    <td><fmt:formatNumber pattern="###,###,##0.0" value="${record.downtimeHours}"/></td>
                                                </tr>
                                            </c:forEach>
                                        </tbody>
                                    </table>
                                </div>
                            </c:when>
                            <c:otherwise>
                                <div>None</div>
                            </c:otherwise>
                        </c:choose>
                        <h3 id="notes-header">Notes</h3>
                        <div id="notes-output">
                            <c:choose>
                                <c:when test="${monthInfo.note ne null}">
                                    <span><c:out value="${monthInfo.note}"/></span>
                                </c:when>
                                <c:otherwise>
                                    None
                                </c:otherwise>
                            </c:choose>
                        </div>
                        <div id="notes-input">
                            <textarea id="noteTextArea" maxlength="3500"><c:out value="${monthInfo.note}"/></textarea>
                        </div>
                    </div>
                </c:otherwise>
            </c:choose>
        </section>
        <div id="exit-fullscreen-panel">
            <button id="exit-fullscreen-button">Exit Full Screen</button>
        </div>
    </jsp:body>         
</t:operability-page>