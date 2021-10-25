<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@taglib prefix="s" uri="http://jlab.org/jsp/smoothness"%>
<%@taglib prefix="dtm" uri="http://jlab.org/dtm/functions"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%> 
<jsp:useBean id="now" class="java.util.Date"/>
<c:set var="title" value="System Downtime"/>
<t:reports-page title="${title}">  
    <jsp:attribute name="stylesheets">
        <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/resources/v${initParam.resourceVersionNumber}/css/downtime-report.css"/>
    </jsp:attribute>
    <jsp:attribute name="scripts">
        <script type="text/javascript" src="${cdnContextPath}/jquery-plugins/flot/0.8.3/jquery.flot.min.js"></script>
        <script type="text/javascript" src="${cdnContextPath}/jquery-plugins/flot/0.8.3/jquery.flot.pie.min.js"></script>
        <script type="text/javascript" src="${cdnContextPath}/jquery-plugins/flot/0.8.3/jquery.flot.resize.min.js"></script>
        <script type="text/javascript" src="${pageContext.request.contextPath}/resources/v${initParam.resourceVersionNumber}/js/downtime-report.js"></script>
    </jsp:attribute>        
    <jsp:body>
        <section>
            <fmt:formatDate var="todayFmt" value="${today}" pattern="${s:getFriendlyDateTimePattern()}"/>
            <fmt:formatDate var="sevenDaysAgoFmt" value="${sevenDaysAgo}" pattern="${s:getFriendlyDateTimePattern()}"/>
            <c:set var="scheduledDowntime" value="0"/>
            <div id="report-page-actions">
                <button id="fullscreen-button">Full Screen</button>                
                <div id="export-widget">
                    <button id="export-menu-button">Export</button>
                    <ul id="export-menu">
                        <li id="image-menu-item">Image</li>
                        <li id="print-menu-item">Print</li>
                    </ul>
                </div>
            </div>            
            <s:filter-flyout-widget  requiredMessage="true" resetButton="true">
                <form id="filter-form" method="get" action="system-downtime">
                    <fieldset>
                        <legend>Time</legend>
                        <s:date-range required="${true}" datetime="${true}" sevenAmOffset="${true}"/>
                    </fieldset>
                    <fieldset>
                        <legend>Taxonomy</legend>
                        <ul class="key-value-list">                        
                            <li>
                                <div class="li-key">
                                    <label for="type">Type</label>
                                </div>
                                <div class="li-value">
                                    <select id="type" name="type">
                                        <option value=""> </option>
                                        <c:forEach items="${eventTypeList}" var="type">
                                            <option value="${type.eventTypeId}"${(param.type eq type.eventTypeId) or (param.type eq null and type.eventTypeId eq 1) ? ' selected="selected"' : ''}><c:out value="${type.name}"/> (<c:out value="${type.abbreviation}"/>)</option>
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
                                    <label for="category">Category</label>
                                </div>
                                <div class="li-value">
                                    <select id="category" name="category">
                                        <option value=""> </option>
                                        <c:forEach items="${categoryList}" var="category">
                                            <option value="${category.categoryId}" ${category.categoryId eq param.category ? 'selected="selected"' : ''}><c:out value="${category.name}"/></option>
                                        </c:forEach>
                                    </select>
                                </div>
                            </li>                        
                        </ul>                                        
                    </fieldset>
                    <fieldset id="display-fieldset">
                        <legend>Display</legend>
                        <ul class="key-value-list">           
                            <li>
                                <div class="li-key">
                                    <label for="chart">Chart</label>
                                </div>
                                <div class="li-value">
                                    <select id="chart" name="chart">
                                        <option value="bar"${param.chart eq 'bar' ? ' selected="selected"' : ''}>Bar</option>
                                        <option value="pie"${param.chart eq 'pie' ? ' selected="selected"' : ''}>Pie</option>
                                        <option value="pareto"${param.chart eq 'pareto' ? ' selected="selected"' : ''}>Pareto</option>
                                        <option value="table"${param.chart eq 'table' ? ' selected="selected"' : ''}>Table</option>
                                    </select>
                                </div>
                            </li>     
                            <li>
                                <div class="li-key">
                                    <label for="data">Chart Data</label>
                                </div>
                                <div class="li-value">
                                    <select id="data" name="data">
                                        <option value="downtime"${param.data eq 'downtime' ? ' selected="selected"' : ''}>Downtime (Hours)</option>
                                        <option value="count"${param.data eq 'count' ? ' selected="selected"' : ''}>Count</option>
                                        <option value="mttr"${param.data eq 'mttr' ? ' selected="selected"' : ''}>Mean Time to Recover (Hours)</option>
                                    </select>
                                </div>
                            </li>
                            <li>
                                <div class="li-key">
                                    <label for="packed">Non-Overlapping</label>
                                </div>
                                <div class="li-value">
                                    <select id="packed" name="packed">
                                        <option value="Y"${param.packed eq 'Y' ? ' selected="selected"' : ''}>Yes</option>
                                        <option value="N"${param.packed ne 'Y' ? ' selected="selected"' : ''}>No</option>
                                    </select>
                                </div>
                            </li>                             
                        </ul>
                    </fieldset>                      
                    <input type="hidden" name="qualified" value=""/>                                    
                    <input id="filter-form-submit-button" type="submit" value="Apply"/>                                
                </form>                                                    
            </s:filter-flyout-widget>
            <h2 id="page-header-title"><c:out value="${title}"/></h2> 
            <c:choose>
                <c:when test="${start == null || end == null}">
                    <div class="message-box">Select a start date and end date to continue</div>
                </c:when>
                <c:otherwise>
                    <div class="message-box">
                        <c:out value="${selectionMessage}"/>
                    </div>
                    <c:choose>
                        <c:when test="${fn:length(downtimeList) > 0}">
                            <s:chart-widget></s:chart-widget>
                        </c:when>
                        <c:otherwise>
                            <div>No data to chart</div>
                        </c:otherwise>
                    </c:choose>
                    <div id="data-table-panel" class="chart-wrap-backdrop" style="display: none;">
                        <h3>Summary</h3>
                        <table class="data-table summary-table">
                            <tbody>
                                <tr class="major-row">
                                    <th>Total Incident Downtime (Hours): </th>
                                    <td><fmt:formatNumber value="${downtimeHours}" pattern="#,##0.0"/></td>
                                </tr>
                                <tr>
                                    <th>Intra-Category Non-Overlapping Incident Downtime (Hours): </th>
                                    <td><fmt:formatNumber value="${nonOverlappingCategoryDowntimeHours}" pattern="#,##0.0"/></td>
                                </tr>                                
                                <tr>
                                    <th>Intra-System Non-Overlapping Incident Downtime (Hours): </th>
                                    <td><fmt:formatNumber value="${nonOverlappingSystemDowntimeHours}" pattern="#,##0.0"/></td>
                                </tr>
                                <tr>
                                    <th>Period Duration (Hours): </th>
                                    <td><fmt:formatNumber value="${periodDurationHours}" pattern="#,##0.0"/></td>
                                </tr>
                                <c:if test="${type.eventTypeId eq 1}">
                                    <tr>
                                        <th>Accelerator Program Time (Hours): </th>
                                            <c:url var="url" value="/reports/beam-time-summary" context="/btm">
                                                <c:param name="start" value="${param.start eq null ? sevenDaysAgoFmt : param.start}"/>
                                                <c:param name="end" value="${param.end eq null ? todayFmt : param.end}"/>
                                                <c:param name="print" value="${param.print}"/>
                                                <c:param name="fullscreen" value="${param.fullscreen}"/>                                         
                                            </c:url>
                                        <td><a target="_blank" href="${url}" title="BTM Report"><fmt:formatNumber pattern="#,###,##0.0" value="${programHours}"/></a></td>
                                    </tr> 
                                </c:if>
                            </tbody>
                        </table>                            
                        <c:if test="${fn:length(downtimeList) > 0}">
                            <h3>Systems</h3>
                            <h4>Cell Format: <span style="display: inline-block; vertical-align: top;">&quot;Incident Downtime&quot; <br/>(&quot;Intra-System Non-Overlapping Incident Downtime&quot;)</span></h4>
                            <table id="bar-chart-data-table" class="data-table stripped-table" data-x-label="System">
                                <thead>
                                    <tr>
                                        <th>System</th>
                                        <th class="selected-column downtime">Downtime (Hours) <span class="sort-desc" title="Descending">▼</span></th>
                                        <th class="coua">Number of Incidents <span class="sort-desc" title="Descending">▼</span></th>
                                        <th class="mttr">Mean Time to Recover (Hours) <span class="sort-desc" title="Descending">▼</span></th>
                                            <c:if test="${type.eventTypeId eq 1}">
                                            <th class="uptime">Uptime (Hours) <span class="sort-desc" title="Descending">▼</span></th>
                                            <th class="mtbf">Mean Time between Failures (Hours) <span class="sort-desc" title="Descending">▼</span></th>
                                            <th class="failure">Hourly Failure Rate <span class="sort-desc" title="Descending">▼</span></th>
                                            <th class="availability">Availability <span class="sort-desc" title="Descending">▼</span></th>
                                            <th class="loss">Loss <span class="sort-desc" title="Descending">▼</span></th>
                                            </c:if>
                                    </tr>
                                </thead>
                                <tbody>
                                    <c:forEach items="${downtimeList}" var="downtime">
                                        <c:set var="nonOverlappingDowntime" value="${nonOverlappingSystemDowntimeMap[downtime.systemId]}"/>
                                        <tr>
                                            <td>
                                                <c:url var="url" value="/reports/component-downtime">
                                                    <c:param name="start" value="${param.start eq null ? sevenDaysAgoFmt : param.start}"/>
                                                    <c:param name="end" value="${param.end eq null ? todayFmt : param.end}"/>
                                                    <c:param name="type" value="${param.type eq null ? '1' : param.type}"/>
                                                    <c:param name="transport" value="${param.transport eq null ? 'N' : param.transport}"/>
                                                    <c:param name="system" value="${downtime.systemId}"/>
                                                    <c:param name="chart" value="${param.chart}"/>
                                                    <c:param name="data" value="${param.data}"/>                                                    
                                                    <c:param name="print" value="${param.print}"/>
                                                    <c:param name="fullscreen" value="${param.fullscreen}"/>     
                                                    <c:param name="qualified" value=""/>   
                                                </c:url>
                                                <a href="${url}" title="Component Downtime Report"><c:out value="${downtime.systemName}"/></a>
                                            </td>
                                            <td class="downtime right-aligned"><fmt:formatNumber value="${downtime.duration * 24}" pattern="#,##0.0"/><br/>(<fmt:formatNumber value="${nonOverlappingDowntime.duration * 24}" pattern="#,##0.0"/>)</td>
                                            <td class="count right-aligned"><fmt:formatNumber value="${downtime.incidentCount}" pattern="#,##0"/><br/>(<fmt:formatNumber value="${nonOverlappingDowntime.incidentCount}" pattern="#,##0"/>)</td>
                                            <td class="mttr right-aligned"><fmt:formatNumber value="${downtime.duration / downtime.incidentCount * 24}" pattern="#,##0.0"/><br/>(<fmt:formatNumber value="${nonOverlappingDowntime.duration / nonOverlappingDowntime.incidentCount * 24}" pattern="#,##0.0"/>)</td>
                                                <c:if test="${type.eventTypeId eq 1}">
                                                    <c:set var="uptime" value="${programHours - (downtime.duration * 24)}"/>
                                                    <c:set var="uptime" value="${uptime < 0 ? 0 : uptime}"/>
                                                    <c:set var="nonOverlappingUptime" value="${programHours - (nonOverlappingDowntime.duration * 24)}"/>
                                                    <c:set var="nonOverlappingUptime" value="${nonOverlappingUptime < 0 ? 0 : nonOverlappingUptime}"/>
                                                <td class="uptime right-aligned"><fmt:formatNumber value="${uptime}" pattern="#,##0.0"/><br/>(<fmt:formatNumber value="${nonOverlappingUptime}" pattern="#,##0.0"/>)</td>
                                                    <c:set var="mtbf" value="${uptime / downtime.incidentCount}"/>
                                                    <c:set var="nonOverlappingMtbf" value="${nonOverlappingUptime / nonOverlappingDowntime.incidentCount}"/>
                                                <td class="mtbf right-aligned"><fmt:formatNumber value="${mtbf}" pattern="#,##0.0"/><br/>(<fmt:formatNumber value="${nonOverlappingMtbf}" pattern="#,##0.0"/>)</td>
                                                    <c:set var="failure" value="${uptime == 0 ? '' : downtime.incidentCount / uptime}"/>    
                                                    <c:set var="nonOverlappingFailure" value="${nonOverlappingUptime == 0 ? '' : nonOverlappingDowntime.incidentCount / nonOverlappingUptime}"/>      
                                                <td class="failure right-aligned"><fmt:formatNumber value="${failure}" pattern="#,##0.000"/><br/>(<fmt:formatNumber value="${nonOverlappingFailure}" pattern="#,##0.000"/>)</td>
                                                    <c:set var="availability" value="${programHours == 0 ? 0 : uptime / programHours * 100}"/>
                                                    <c:set var="nonOverlappingAvailability" value="${programHours == 0 ? 0 : nonOverlappingUptime / programHours * 100}"/>
                                                <td class="availability right-aligned"><fmt:formatNumber value="${availability}" pattern="#,##0.0"/>%<br/>(<fmt:formatNumber value="${nonOverlappingAvailability}" pattern="#,##0.0"/>%)</td>
                                                <td class="loss right-aligned"><fmt:formatNumber value="${100 - availability}" pattern="#,##0.0"/>%<br/>(<fmt:formatNumber value="${100 - nonOverlappingAvailability}" pattern="#,##0.0"/>%)</td>     
                                                </c:if>
                                        </tr>
                                    </c:forEach>
                                </tbody>
                            </table>
                        </c:if>
                    </div>
                </c:otherwise>
            </c:choose>
        </section>
        <div id="exit-fullscreen-panel">
            <button id="exit-fullscreen-button">Exit Full Screen</button>
        </div>            
    </jsp:body>         
</t:reports-page>