<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="jakarta.tags.core"%>
<%@taglib prefix="fn" uri="jakarta.tags.functions"%>
<%@taglib prefix="fmt" uri="jakarta.tags.fmt"%>
<%@taglib prefix="s" uri="jlab.tags.smoothness"%>
<%@taglib prefix="dtm" uri="jlab.tags.dtm"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%> 
<jsp:useBean id="now" class="java.util.Date"/>
<c:set var="title" value="Incident Downtime"/>
<t:reports-page title="${title}">  
    <jsp:attribute name="stylesheets">
        <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/resources/v${initParam.releaseNumber}/css/downtime-report.css"/>
    </jsp:attribute>
    <jsp:attribute name="scripts">
        <script type="text/javascript">
            $(document).on("click", ".default-reset-panel", function () {
                $("#date-range").val('past7days').change();
                $("#type").val([1]).trigger('change');
                $("#transport").val('N');
                $("#chart").val('bar');
                $("#data").val('downtime');
                $("#system").val('');
                $("#component").val('');
                $("#group").val('');
                $("#maxDuration").val('');
                $("#maxDurationUnits").val('Minutes');
                $("#minDuration").val('');
                $("#minDurationUnits").val('Minutes');
                return false;
            });

            $(function () {
                $("#type").select2({
                width: 360
                });
            });
        </script>
    </jsp:attribute>        
    <jsp:body>
        <section>
            <fmt:formatDate var="todayFmt" value="${today}" pattern="${s:getFriendlyDateTimePattern()}"/>
            <fmt:formatDate var="sevenDaysAgoFmt" value="${sevenDaysAgo}" pattern="${s:getFriendlyDateTimePattern()}"/>
            <fmt:formatDate var="startFmt" value="${start}" pattern="${s:getFriendlyDateTimePattern()}"/>
            <fmt:formatDate var="endFmt" value="${end}" pattern="${s:getFriendlyDateTimePattern()}"/>
            <div id="report-page-actions">
                <button id="fullscreen-button">Full Screen</button>                 
                <div id="export-widget">
                    <button id="export-menu-button">Export</button>
                    <ul id="export-menu">
                        <li id="image-menu-item">Image</li>
                        <li id="print-menu-item">Print</li>
                        <li id="excel-menu-item">Excel</li>
                        <li id="shiftlog-menu-item">Shiftlog</li>
                    </ul>
                </div>
            </div>            
            <s:filter-flyout-widget  requiredMessage="true" resetButton="true">
                <form class="filter-form" method="get" action="incident-downtime">
                    <fieldset>
                        <legend>Time</legend>
                        <s:date-range required="${true}" datetime="${true}" sevenAmOffset="${true}"/>
                        <ul class="key-value-list">
                            <li>
                                <div class="li-key">
                                    <label for="maxDuration" title="Inclusive">Max Duration</label>
                                </div>
                                <div class="li-value">
                                    <input type="number" min="1" id="maxDuration" name="maxDuration" value="${fn:escapeXml(param.maxDuration)}"/>
                                    <select id="maxDurationUnits" name="maxDurationUnits">
                                        <option value="Seconds"${(param.maxDurationUnits eq 'Seconds') ? ' selected="selected"' : ''}>Seconds</option>
                                        <option value="Minutes"${(param.maxDurationUnits eq 'Minutes') ? ' selected="selected"' : ''}>Minutes</option>
                                        <option value="Hours"${(param.maxDurationUnits eq 'Hours') ? ' selected="selected"' : ''}>Hours</option>
                                    </select>
                                </div>
                            </li>
                            <li>
                                <div class="li-key">
                                    <label for="minDuration" title="Inclusive">Min Duration</label>
                                </div>
                                <div class="li-value">
                                    <input type="number" min="1" id="minDuration" name="minDuration" value="${fn:escapeXml(param.minDuration)}"/>
                                    <select id="minDurationUnits" name="minDurationUnits">
                                        <option value="Seconds"${(param.minDurationUnits eq 'Seconds') ? ' selected="selected"' : ''}>Seconds</option>
                                        <option value="Minutes"${(param.minDurationUnits eq 'Minutes') ? ' selected="selected"' : ''}>Minutes</option>
                                        <option value="Hours"${(param.minDurationUnits eq 'Hours') ? ' selected="selected"' : ''}>Hours</option>
                                    </select>
                                </div>
                            </li>
                        </ul>
                    </fieldset>
                    <fieldset>
                        <legend>Taxonomy</legend>
                        <ul class="key-value-list">
                            <li>
                                <div class="li-key">
                                    <label for="type">Type</label>
                                </div>
                                <div class="li-value">
                                    <select id="type" name="type" multiple="multiple">
                                        <c:forEach items="${eventTypeList}" var="type">
                                            <option value="${type.eventTypeId}"${s:inArray(paramValues.type, type.eventTypeId.toString()) ? ' selected="selected"' : ''}><c:out value="${dtm:formatType(type)}"/></option>
                                        </c:forEach>
                                    </select>
                                </div>
                            </li>
                            <li>
                                <div class="li-key">
                                    <label for="transport">Beam Transport</label>
                                </div>
                                <div class="li-value">
                                    <select id="transport" name="transport">
                                        <option value=""> </option>
                                        <option value="N"${param.transport eq 'N' ? ' selected="selected"' : ''}>Exclude</option>
                                        <option value="Y"${param.transport eq 'Y' ? ' selected="selected"' : ''}>Only</option>
                                    </select>
                                </div>
                            </li>                        
                            <li>
                                <div class="li-key">
                                    <label for="system">System</label>
                                </div>
                                <div class="li-value">
                                    <select id="system" name="system">
                                        <option value=""> </option>
                                        <c:forEach items="${systemList}" var="system">
                                            <option value="${system.systemId}" ${system.systemId eq param.system ? 'selected="selected"' : ''}><c:out value="${system.name}"/></option>
                                        </c:forEach>
                                    </select>
                                </div>
                            </li>
                            <li>
                                <div class="li-key">
                                    <label for="group">Repaired By</label>
                                </div>
                                <div class="li-value">
                                    <select id="group" name="group">
                                        <option value=""> </option>
                                        <c:forEach items="${groupList}" var="group">
                                            <option value="${group.workgroupId}"${param.group eq group.workgroupId ? ' selected="selected"' : ''}><c:out value="${group.name}"/></option>
                                        </c:forEach>
                                    </select> 
                                </div>
                            </li>
                            <li>
                                <div class="li-key">
                                    <label for="component">Component</label>
                                </div>
                                <div class="li-value">
                                    <input type="text" id="component" name="component" placeholder="name" value="${fn:escapeXml(param.component)}"/>
                                    (use % as wildcard)
                                </div>
                            </li>
                        </ul>
                    </fieldset>
                    <input type="hidden" class="offset-input" name="offset" value="0"/>
                    <input type="hidden" name="qualified" value=""/>                                     
                    <input class="filter-form-submit-button" type="submit" value="Apply"/>
                </form>                                                    
            </s:filter-flyout-widget>
            <h2 class="page-header-title"><c:out value="${title}"/></h2>
            <c:choose>
                <c:when test="${start == null || end == null}">
                    <div class="message-box">Select a start date and end date to continue</div>
                </c:when>
                <c:otherwise>
                    <div class="message-box">
                        <c:out value="${selectionMessage}"/>
                    </div>
                    <c:if test="${fn:length(incidentList) > 0}">
                        <div class="chart-wrap-backdrop">
                            <h3>Summary</h3>
                            <table class="data-table summary-table">
                                <tbody>
                                    <tr class="major-row">
                                        <th>Total Downtime (Hours):</th>
                                        <td><fmt:formatNumber value="${totalRepairTime}" pattern="#,##0.0"/></td>
                                    </tr>
                                    <tr>
                                        <th title="Mean Time To Recover">MTTR (Hours):</th>
                                        <td><fmt:formatNumber value="${totalRepairTime / paginator.totalRecords}" pattern="#,##0.0"/></td>
                                    </tr>
                                    <tr>
                                        <th title="Downtime from between 19:00 - 7:00">Overnight Downtime (Hours):</th>
                                        <td><fmt:formatNumber value="${overnightRepairTime}" pattern="#,##0.0"/></td>
                                    </tr>
                                    <tr>
                                        <th title="Count of incidents opened between 19:00 - 7:00">Overnight Opened Incident Count:</th>
                                        <td><fmt:formatNumber value="${overnightOpenedCount}" pattern="#,##0"/></td>
                                    </tr>   
                                    <tr>
                                        <th title="Count of incidents closed between 19:00 - 7:00">Overnight Closed Incident Count:</th>
                                        <td><fmt:formatNumber value="${overnightClosedCount}" pattern="#,##0"/></td>
                                    </tr>
                                    <tr>
                                        <th title="Downtime of incidents opened between 19:00 - 7:00">Overnight Opened Incident Downtime:</th>
                                        <td><fmt:formatNumber value="${overnightOpenedHours}" pattern="#,##0"/></td>
                                    </tr> 
                                    <tr>
                                        <th title="Downtime of incidents closed between 19:00 - 7:00">Overnight Closed Incident Downtime:</th>
                                        <td><fmt:formatNumber value="${overnightClosedHours}" pattern="#,##0"/></td>
                                    </tr>                                    
                                    <tr>
                                        <th title="MTTR of incidents opened between 19:00 - 7:00">Overnight Opened Incident MTTR:</th>
                                        <td><fmt:formatNumber value="${overnightOpenedCount > 0 ? overnightOpenedHours / overnightOpenedCount : 0}" pattern="#,##0.0"/></td>
                                    </tr> 
                                    <tr>
                                        <th title="MTTR of incidents closed between 19:00 - 7:00">Overnight Closed Incident MTTR:</th>
                                        <td><fmt:formatNumber value="${overnightClosedCount > 0 ? overnightClosedHours / overnightClosedCount : 0}" pattern="#,##0.0"/></td>
                                    </tr> 
                                    <c:set value="${totalRepairTime - overnightRepairTime}" var="daytimeRepairTime"/>
                                    <c:set value="${paginator.totalRecords - overnightOpenedCount}" var="daytimeOpenedCount"/>
                                    <c:set value="${paginator.totalRecords - overnightClosedCount}" var="daytimeClosedCount"/>
                                    <c:set value="${totalRepairTime - overnightOpenedHours}" var="daytimeOpenedHours"/>
                                    <c:set value="${totalRepairTime - overnightClosedHours}" var="daytimeClosedHours"/>
                                    <tr>
                                        <th title="Downtime from between 7:00 - 19:00">Daytime Downtime (Hours):</th>
                                        <td><fmt:formatNumber value="${daytimeRepairTime}" pattern="#,##0.0"/></td>
                                    </tr>
                                    <tr>
                                        <th title="Count of incidents opened between 7:00 - 19:00">Daytime Opened Incident Count:</th>
                                        <td><fmt:formatNumber value="${daytimeOpenedCount}" pattern="#,##0"/></td>
                                    </tr>   
                                    <tr>
                                        <th title="Count of incidents closed between 7:00 - 19:00">Daytime Closed Incident Count:</th>
                                        <td><fmt:formatNumber value="${daytimeClosedCount}" pattern="#,##0"/></td>
                                    </tr>
                                    <tr>
                                        <th title="Downtime of incidents opened between 7:00 - 19:00">Daytime Opened Incident Downtime:</th>
                                        <td><fmt:formatNumber value="${daytimeOpenedHours}" pattern="#,##0"/></td>
                                    </tr> 
                                    <tr>
                                        <th title="Downtime of incidents closed between 7:00 - 19:00">Daytime Closed Incident Downtime:</th>
                                        <td><fmt:formatNumber value="${daytimeClosedHours}" pattern="#,##0"/></td>
                                    </tr>                                    
                                    <tr>
                                        <th title="MTTR of incidents opened between 7:00 - 19:00">Daytime Opened Incident MTTR:</th>
                                        <td><fmt:formatNumber value="${daytimeOpenedCount > 0 ? daytimeOpenedHours / daytimeOpenedCount : 0}" pattern="#,##0.0"/></td>
                                    </tr> 
                                    <tr>
                                        <th title="MTTR of incidents closed between 7:00 - 19:00">Daytime Closed Incident MTTR:</th>
                                        <td><fmt:formatNumber value="${daytimeClosedCount > 0 ? daytimeClosedHours / daytimeClosedCount : 0}" pattern="#,##0.0"/></td>
                                    </tr>                                     
                                    <tr>
                                        <th>Period Duration (Hours): </th>
                                        <td><fmt:formatNumber value="${periodDurationHours}" pattern="#,##0.0"/></td>
                                    </tr>
                                </tbody>
                            </table>
                            <h3>Incidents</h3>
                            <table class="data-table stripped-table">
                                <thead>
                                    <tr>
                                        <th>Type</th>
                                        <th>Title</th>
                                        <th>Summary</th>
                                        <th>Downtime (Hours) <span title="Descending">▼</span></th>
                                        <th>System</th>
                                        <th>Component</th>
                                        <th>Repaired By</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <c:forEach items="${incidentList}" var="incident">
                                        <tr>
                                            <td><c:out value="${incident.type}"/></td>
                                            <td><a href="${pageContext.request.contextPath}/incidents/${incident.incidentId}" title="Incident Detail"><c:out value="${incident.title}"/></a></td>
                                                <fmt:formatNumber value="${incident.downtimeHours}" var="formattedUnbounded" pattern="#,##0.0"/>
                                            <td><c:out value="${incident.summary}"/></td>
                                            <td title="Not Bounded: ${formattedUnbounded}" class="right-aligned"><fmt:formatNumber value="${incident.downtimeHoursBounded}" pattern="#,##0.0"/></td>
                                            <td><c:out value="${incident.systemName}"/></td>
                                            <td><c:out value="${incident.componentName}"/></td>
                                            <td><c:out value="${incident.repairedByNameCsv}"/></td>
                                        </tr>
                                    </c:forEach>
                                </tbody>
                            </table>
                            <div class="paginator-button-panel">
                                <button class="previous-button" type="button" data-offset="${paginator.previousOffset}" value="Previous"${paginator.previous ? '' : ' disabled="disabled"'}>Previous</button>
                                <button class="next-button" type="button" data-offset="${paginator.nextOffset}" value="Next"${paginator.next ? '' : ' disabled="disabled"'}>Next</button>
                                <form id="shift-log-form" method="get" action="${pageContext.request.contextPath}/shiftlog/incident-list.html">
                                    <input type="hidden" name="start" value="${startFmt}"/>
                                    <input type="hidden" name="end" value="${endFmt}"/>
                                    <c:forEach items="${paramValues.type}" var="type">
                                        <input type="hidden" name="type" value="${fn:escapeXml(type)}"/>
                                    </c:forEach>
                                    <input type="hidden" name="system" value="${fn:escapeXml(param.system)}"/>
                                    <input type="hidden" name="component" value="${fn:escapeXml(param.component)}"/>
                                    <input type="hidden" name="transport" value="${fn:escapeXml(param.transport)}"/>
                                    <button id="shiftlog" type="submit" style="display: none;">Shift Log</button>
                                </form>                            
                                <form id="excel-form" method="get" action="${pageContext.request.contextPath}/excel/incident-list.xlsx">
                                    <input type="hidden" name="start" value="${startFmt}"/>
                                    <input type="hidden" name="end" value="${endFmt}"/>
                                    <c:forEach items="${paramValues.type}" var="type">
                                        <input type="hidden" name="type" value="${fn:escapeXml(type)}"/>
                                    </c:forEach>
                                    <input type="hidden" name="system" value="${fn:escapeXml(param.system)}"/>
                                    <input type="hidden" name="component" value="${fn:escapeXml(param.component)}"/>
                                    <input type="hidden" name="transport" value="${fn:escapeXml(param.transport)}"/>
                                    <button id="excel" type="submit" style="display: none;">Excel</button>
                                </form>
                            </div>
                        </div>
                    </c:if>
                </c:otherwise>
            </c:choose>
        </section>
        <div id="exit-fullscreen-panel">
            <button id="exit-fullscreen-button">Exit Full Screen</button>
        </div>           
    </jsp:body>         
</t:reports-page>