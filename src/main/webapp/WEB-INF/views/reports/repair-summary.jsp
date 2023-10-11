<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@taglib prefix="s" uri="http://jlab.org/jsp/smoothness"%>
<%@taglib prefix="dtm" uri="http://jlab.org/dtm/functions"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%> 
<jsp:useBean id="now" class="java.util.Date"/>
<c:set var="title" value="Downtime Incident Repair Summary"/>
<t:reports-page title="${title}">  
    <jsp:attribute name="stylesheets">
        <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/resources/v${initParam.releaseNumber}/css/repair-summary.css"/>
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
        <script type="text/javascript" src="${pageContext.request.contextPath}/resources/v${initParam.releaseNumber}/js/repair-summary.js"></script>
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
                        <li id="csv-menu-item">CSV</li>
                    </ul>
                </div>
            </div>            
            <s:filter-flyout-widget  requiredMessage="true" resetButton="true">
                <form id="filter-form" method="get" action="repair-summary" data-max-duration="${fn:escapeXml(param.maxDuration)}" data-max-duration-units="${fn:escapeXml(param.maxDurationUnits)}" data-max-types="${fn:escapeXml(param.maxTypes)}" data-start="${fn:escapeXml(startFmt)}" data-end="${fn:escapeXml(endFmt)}" data-program-hours="${fn:escapeXml(programHours)}" data-period-hours="${fn:escapeXml(periodHours)}" data-sad-trips="${fn:escapeXml(param.sadTrips)}">
                    <div id="filter-form-panel" class="scrollable-filter-form">
                        <fieldset>
                            <legend>Time</legend>
                            <s:date-range required="${true}" datetime="${true}" sevenAmOffset="${true}"/>
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
                                            <option value="bar"${param.chart eq 'bar' ? ' selected="selected"' : ''}>Bar (Histogram)</option>
                                            <option value="table"${param.chart eq 'table' ? ' selected="selected"' : ''}>Table</option>
                                        </select>
                                    </div>
                                </li>     
                                <li>
                                    <div class="li-key">
                                        <label for="binSize">Bin Size</label>
                                    </div>
                                    <div class="li-value">
                                        <select id="binSize" name="binSize">         
                                            <c:forEach items="${binSizeArray}" var="binSize">
                                                <option value="${binSize.name()}"${s:inArray(paramValues.binSize, binSize.name()) ? ' selected="selected"' : ''}><c:out value="${binSize.label}"/></option>
                                            </c:forEach>
                                        </select>
                                    </div>
                                </li>                                                
                                <li>
                                    <div class="li-key">
                                        <label for="grouping">Grouping</label>
                                    </div>
                                    <div class="li-value">
                                        <select id="grouping" name="grouping">
                                            <option value="repairedby"${param.grouping eq 'repairedby' ? ' selected="selected"' : ''}>Repaired By</option>
                                        </select>
                                    </div>
                                </li>
                                <li>
                                    <div class="li-key">
                                        <label for="legendData">Legend Data</label>
                                    </div>
                                    <div class="li-value">
                                        <select id="legendData" name="legendData" multiple="multiple">
                                            <option value="count" ${s:inArray(paramValues.legendData, 'count') ? 'selected="selected"' : ''}>Incident Count</option>
                                            <option value="lost" ${s:inArray(paramValues.legendData, 'lost') ? 'selected="selected"' : ''}>Repair Hours</option>
                                        </select>
                                    </div>
                                </li>
                                <li>
                                    <div class="li-key">
                                        <label for="repairedby">Repaired By</label>
                                    </div>
                                    <div class="li-value">
                                        <select id="repairedby" name="repairedBy" multiple="multiple">
                                            <c:forEach items="${groupList}" var="repairer">
                                                <option value="${repairer.workgroupId}"${s:inArray(paramValues.repairedBy, repairer.workgroupId.toString()) ? ' selected="selected"' : ''}><c:out value="${repairer.name}"/></option>
                                            </c:forEach>
                                        </select>
                                    </div>
                                </li>                                
                            </ul>
                        </fieldset>
                    </div>
                    <input type="hidden" name="qualified" value=""/>
                    <input id="filter-form-submit-button" type="submit" value="Apply"/>     
                </form>                                                    
            </s:filter-flyout-widget>
            <h2 id="page-header-title"><c:out value="${title}"/></h2>
            <c:choose>
                <c:when test="${start == null || end == null}">
                    <div class="message-box">Select a start and end date to continue</div>
                </c:when>
                <c:otherwise>
                    <div class="message-box">
                        <c:out value="${subtitle}"/>
                    </div>
                    <c:choose>
                        <c:when test="${fn:length(trendList) > 0}">
                            <c:if test="${param.chart ne 'table'}">
                                <s:chart-widget>
                                    <div class="footnote-wrapper" style="display: none;">
                                        <div class="chart-footnote">
                                            <c:if test="${!empty footnoteList}">
                                                <ul>
                                                    <c:forEach items="${footnoteList}" var="note">
                                                        <li><c:out value="${note}"/></li>
                                                        </c:forEach>
                                                </ul>
                                            </c:if>
                                        </div>
                                    </div>
                                </s:chart-widget>
                            </c:if>
                            <div id="data-table-panel" class="chart-wrap-backdrop" style="${param.chart eq 'table' ? '' : 'display: none;'}">
                                <table id="bar-chart-data-table" class="data-table stripped-table" data-start-millis="${dtm:getLocalTime(start)}" data-end-millis="${dtm:getLocalTime(endInclusive)}" data-y-axis-prefix="FSD Trip">
                                    <thead>
                                        <tr>
                                            <th>Date <span style="display: inline-block;" class="sort-asc" title="Ascending">▲</span></th>
                                            <th>Incidents In Bin</th>
                                            <th>New Incidents In Bin</th>
                                            <th>Duration (Hours)</th>
                                            <th>Grouping</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <c:forEach items="${trendList}" var="bin">
                                            <tr>
                                                <fmt:formatDate value="${bin.start}" pattern="dd-MMM-YYYY HH z" var="dateWithTz"/>
                                                <fmt:formatDate value="${bin.start}" pattern="dd-MMM-YYYY HH:mm" var="startParam"/>
                                                <fmt:formatDate value="${dtm:getBinEnd(bin.start, param.binSize)}" pattern="dd-MMM-YYYY HH:mm" var="endParam"/>
                                                <c:url value="/reports/incident-downtime" var="incidentReportUrl">
                                                    <c:param name="start" value="${fn:escapeXml(startParam)}"/>
                                                    <c:param name="end" value="${fn:escapeXml(endParam)}"/>
                                                    <c:param name="qualified" value=""/>
                                                </c:url>
                                                <td data-date-utc="${dtm:getLocalTime(bin.start)}" title="${dateWithTz}"><a href="${incidentReportUrl}"><fmt:formatDate value="${bin.start}" pattern="dd-MMM-YYYY HH"/></a></td>
                                                <td class="count-data right-aligned"><fmt:formatNumber value="${bin.count}" pattern="#,##0"/></td>
                                                <td class="new-count-data"><fmt:formatNumber value="${bin.newCount}" pattern="#,##0"/></td>
                                                <td class="duration-data right-aligned"><fmt:formatNumber value="${bin.durationMillis / 3600000}" pattern="#,##0.0000"/></td>
                                                <td class="group-data"><c:out value="${bin.grouping}"/></td>
                                            </tr>
                                        </c:forEach>
                                    </tbody>
                                </table>
                            </div>
                        </c:when>
                        <c:otherwise>
                            <div>No data to chart</div>
                        </c:otherwise>
                    </c:choose>
                </c:otherwise>
            </c:choose>
            <form id="csv-form" method="get" action="${pageContext.request.contextPath}/csv/repair-summary.csv">
                <input type="hidden" name="start" value="${fn:escapeXml(param.start)}"/>
                <input type="hidden" name="end" value="${fn:escapeXml(param.end)}"/>
                <input type="hidden" name="maxDuration" value="${fn:escapeXml(param.maxDuration)}"/>
                <input type="hidden" name="maxDurationUnits" value="${fn:escapeXml(param.maxDurationUnits)}"/>
                <input type="hidden" name="rateBasis" value="${fn:escapeXml(param.rateBasis)}"/>
                <input type="hidden" name="sadTrips" value="${fn:escapeXml(param.sadTrips)}"/>
                <input type="hidden" name="grouping" value="${fn:escapeXml(param.grouping)}"/>
                <input type="hidden" name="binSize" value="${fn:escapeXml(param.binSize)}"/>
                <input type="hidden" name="maxTypes" value="${fn:escapeXml(param.maxTypes)}"/>
                <button id="csv" type="submit" style="display: none;">CSV</button>
            </form>
        </section>
        <div id="exit-fullscreen-panel">
            <button id="exit-fullscreen-button">Exit Full Screen</button>
        </div>
        <script>
            let groupMap = {};
            <c:forEach items="${groupList}" var="group">
                groupMap['${group.name}'] = ${group.workgroupId};
            </c:forEach>
        </script>
    </jsp:body>         
</t:reports-page>