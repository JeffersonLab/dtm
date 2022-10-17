<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@taglib prefix="s" uri="http://jlab.org/jsp/smoothness"%>
<%@taglib prefix="dtm" uri="http://jlab.org/dtm/functions"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%> 
<jsp:useBean id="now" class="java.util.Date"/>
<c:set var="title" value="Event Downtime"/>
<t:reports-page title="${title}">  
    <jsp:attribute name="stylesheets">
        <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/resources/v${initParam.releaseNumber}/css/downtime-report.css"/>
    </jsp:attribute>
    <jsp:attribute name="scripts">
        <script type="text/javascript" src="${cdnContextPath}/jquery-plugins/flot/0.8.3/jquery.flot.min.js"></script>
        <script type="text/javascript" src="${cdnContextPath}/jquery-plugins/flot/0.8.3/jquery.flot.pie.min.js"></script>
        <script type="text/javascript" src="${cdnContextPath}/jquery-plugins/flot/0.8.3/jquery.flot.resize.min.js"></script>
        <script type="text/javascript" src="${pageContext.request.contextPath}/resources/v${initParam.releaseNumber}/js/downtime-report.js"></script>
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
                    </ul>
                </div>
            </div>            
            <s:filter-flyout-widget  requiredMessage="true" resetButton="true">
                <form id="filter-form" method="get" action="event-downtime">
                    <fieldset>
                        <legend>Time</legend>
                        <s:date-range required="${true}" datetime="${true}" sevenAmOffset="${true}"/>
                    </fieldset>
                    <fieldset>
                        <legend>Taxonomy</legend>
                        <ul class="key-value-list">                         
                            <li>
                                <div class="li-key">
                                    <label class="required-field" for="type">Type</label>
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
                                        <option value="N"${param.transport eq 'N' ? ' selected="selected"' : ''}>Exclude Events With Any</option>
                                        <option value="Y"${param.transport eq 'Y' ? ' selected="selected"' : ''}>Only Include Events With Any</option>
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
                                        <option value="restore"${param.data eq 'restore' ? ' selected="selected"' : ''}>Restore (Hours)</option>
                                        <option value="count"${param.data eq 'count' ? ' selected="selected"' : ''}>Count</option>
                                        <option value="mttr"${param.data eq 'mttr' ? ' selected="selected"' : ''}>Mean Time to Recover (Hours)</option>
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
                <c:when test="${start == null || end == null || type == null}">
                    <div class="message-box">Select a start date, end date, and type to continue</div>
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
                        <c:if test="${fn:length(downtimeList) > 0}">
                            <h3>Summary</h3>
                            <table class="data-table summary-table">
                                <tbody>
                                    <tr class="major-row">
                                        <th>Total Downtime (Hours): </th>
                                        <td><fmt:formatNumber value="${grandTotalDuration}" pattern="#,##0.0"/></td>
                                    </tr>
                                    <tr>
                                        <th>MTTR (Hours): </th>
                                        <td><fmt:formatNumber value="${meanTimeToRecover}" pattern="#,##0.0"/></td>
                                    </tr>
                                    <tr>
                                        <th>Total Suspend (Hours): </th>
                                        <td><fmt:formatNumber value="${(grandTotalDuration - restoreTotal)}" pattern="#,##0.0"/></td>
                                    </tr> 
                                    <tr>
                                        <th>Total Restore (Hours): </th>
                                        <td><fmt:formatNumber value="${restoreTotal}" pattern="#,##0.0"/></td>
                                    </tr>   
                                    <tr>
                                        <th>Period Duration (Hours): </th>
                                        <td><fmt:formatNumber value="${periodDurationHours}" pattern="#,##0.0"/></td>
                                    </tr>
                                    <tr>
                                        <th>Event Count: </th>
                                        <td><fmt:formatNumber value="${fn:length(downtimeList)}" pattern="#,##0"/></td>
                                    </tr>  
                                    <tr>
                                        <th>Incident Count: </th>
                                        <td><fmt:formatNumber value="${incidentCount}" pattern="#,##0"/></td>
                                    </tr>                                     
                                </tbody>
                            </table>                        
                            <h3>Events</h3>
                            <table id="bar-chart-data-table" class="data-table stripped-table" data-x-label="Event">
                                <thead>
                                    <tr>
                                        <th>Event</th>
                                        <th class="selected-column downtime">Downtime (Hours) <span class="sort-desc" title="Descending">▼</span></th>
                                        <th class="restore">Restore (Hours) <span class="sort-desc" title="Descending">▼</span></th>
                                        <th class="count">Number of Incidents <span class="sort-desc" title="Descending">▼</span></th>
                                        <th class="mttr">Mean Time to Recover (Hours) <span class="sort-desc" title="Descending">▼</span></th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <c:forEach items="${downtimeList}" var="downtime">
                                        <tr>
                                            <td><a href="${pageContext.request.contextPath}/all-events?eventId=${downtime.eventId}&amp;qualified=" title="Event Details"><c:out value="${downtime.title}"/></a></td>
                                                <fmt:formatNumber value="${downtime.downtimeHours}" var="formattedUnbounded"/>
                                            <td class="downtime right-aligned" title="Not Bounded: ${formattedUnbounded}"><fmt:formatNumber value="${downtime.downtimeHoursBounded}" pattern="#,##0.0"/></td>
                                            <td class="restore right-aligned"><fmt:formatNumber value="${downtime.restoreHoursBounded}" pattern="#,##0.0"/></td>
                                            <td class="count right-aligned"><fmt:formatNumber value="${downtime.incidentCount}" pattern="#,##0"/></td>
                                            <td class="mttr right-aligned"><fmt:formatNumber value="${downtime.downtimeHoursBounded / downtime.incidentCount}" pattern="#,##0.0"/></td>
                                        </tr>
                                    </c:forEach>
                                </tbody>
                            </table>
                            <div class="paginator-button-panel"> 
                                <form id="excel-form" method="get" action="${pageContext.request.contextPath}/excel/event-list.xlsx">
                                    <input type="hidden" name="start" value="${startFmt}"/>
                                    <input type="hidden" name="end" value="${endFmt}"/>
                                    <input type="hidden" name="type" value="${type.eventTypeId}"/>
                                    <input type="hidden" name="transport" value="${param.transport}"/>
                                    <button id="excel" type="submit" style="display: none;">Excel</button>
                                </form>
                            </div>
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