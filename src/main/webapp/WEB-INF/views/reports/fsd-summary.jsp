<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="jakarta.tags.core"%>
<%@taglib prefix="fn" uri="jakarta.tags.functions"%>
<%@taglib prefix="fmt" uri="jakarta.tags.fmt"%>
<%@taglib prefix="s" uri="jlab.tags.smoothness"%>
<%@taglib prefix="dtm" uri="http://jlab.org/dtm/functions"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%> 
<jsp:useBean id="now" class="java.util.Date"/>
<c:set var="title" value="FSD Trip Summary"/>
<t:reports-page title="${title}">  
    <jsp:attribute name="stylesheets">
        <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/resources/v${initParam.releaseNumber}/css/fsd-summary.css"/>
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
        <script type="text/javascript" src="${pageContext.request.contextPath}/resources/v${initParam.releaseNumber}/js/fsd-summary.js"></script>
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
                <form class="filter-form" method="get" action="fsd-summary" data-max-duration="${fn:escapeXml(param.maxDuration)}" data-max-duration-units="${fn:escapeXml(param.maxDurationUnits)}" data-max-types="${fn:escapeXml(param.maxTypes)}" data-start="${fn:escapeXml(startFmt)}" data-end="${fn:escapeXml(endFmt)}" data-program-hours="${fn:escapeXml(programHours)}" data-period-hours="${fn:escapeXml(periodHours)}" data-sad-trips="${fn:escapeXml(param.sadTrips)}">
                    <div id="filter-form-panel" class="scrollable-filter-form">
                        <fieldset>
                            <legend>Time</legend>
                            <s:date-range required="${true}" datetime="${true}" sevenAmOffset="${true}"/>
                            <ul class="key-value-list">
                                <li>
                                    <div class="li-key">
                                        <label for="maxDuration" title="Inclusive">Max Trip Duration</label>
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
                            </ul>
                        </fieldset>
                        <fieldset>
                            <legend>Taxonomy</legend>
                            <ul class="key-value-list"> 
                                <li>
                                    <div class="li-key">
                                        <label for="sadTrips" title="Inclusive">SAD Trips</label>
                                    </div>
                                    <div class="li-value">
                                        <select id="sadTrips" name="sadTrips">
                                            <option value="Y"${param.sadTrips eq 'Y' ? ' selected="selected"' : ''}>Include</option>
                                            <option value="N"${param.sadTrips eq 'N' ? ' selected="selected"' : ''}>Exclude</option>
                                        </select>
                                    </div>
                                </li>                                
                                <li>
                                    <div class="li-key">
                                        <label for="maxTypes" title="Inclusive">Max CED Types Per Trip</label>
                                    </div>
                                    <div class="li-value">
                                        <input type="number" min="1" id="maxTypes" name="maxTypes" value="${fn:escapeXml(param.maxTypes)}"/>
                                    </div>
                                </li>       
                            </ul>
                        </fieldset>
                        <fieldset>
                            <legend>Trip Rate</legend>
                            <ul class="key-value-list">
                                <li>
                                    <div class="li-key">
                                        <label for="rateBasis" title="Inclusive">Rate Basis</label>
                                    </div>
                                    <div class="li-value">
                                        <select id="rateBasis" name="rateBasis">
                                            <option value="program"${param.rateBasis eq 'program' ? ' selected="selected"' : ''}>Program</option>
                                            <option value="period"${param.rateBasis eq 'period' ? ' selected="selected"' : ''}>Period</option>
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
                                            <option value="line"${param.chart eq 'line' ? ' selected="selected"' : ''}>Line</option>
                                            <option value="linewithpoints"${param.chart eq 'linewithpoints' ? ' selected="selected"' : ''}>Line With Points</option>
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
                                            <option value=""> </option>
                                            <option value="cause"${param.grouping eq 'cause' ? ' selected="selected"' : ''}>Cause</option>
                                            <option value="area"${param.grouping eq 'area' ? ' selected="selected"' : ''}>Area</option>
                                        </select>
                                    </div>
                                </li>
                                <li>
                                    <div class="li-key">
                                        <label for="maxY">Max Trips/Hr</label>
                                        <div class="date-note">(Fixed Y Axis)</div>
                                    </div>
                                    <div class="li-value">
                                        <input type="number" name="maxY" id="maxY" min="1" max="100" value="${fn:escapeXml(param.maxY)}"/>
                                        <div class="date-note">(x 24 for Daily Interval)</div>
                                    </div>
                                </li>
                                <li>
                                    <div class="li-key">
                                        <label for="legendData">Legend Data</label>
                                    </div>
                                    <div class="li-value">
                                        <select id="legendData" name="legendData" multiple="multiple">
                                            <option value="count" ${s:inArray(paramValues.legendData, 'count') ? 'selected="selected"' : ''}>Trip Count</option>
                                            <option value="rate" ${s:inArray(paramValues.legendData, 'rate') ? 'selected="selected"' : ''}>Trip Rate</option>
                                            <option value="lost" ${s:inArray(paramValues.legendData, 'lost') ? 'selected="selected"' : ''}>Lost Hours</option>
                                            <option value="mins" ${s:inArray(paramValues.legendData, 'mins') ? 'selected="selected"' : ''}>Average Duration</option>
                                        </select>
                                    </div>
                                </li>
                                <li>
                                    <div class="li-key">
                                        <label for="cause">Cause</label>
                                    </div>
                                    <div class="li-value">
                                        <select id="cause" name="cause" multiple="multiple">
                                            <c:forEach items="${causeArray}" var="cause">
                                                <option value="${cause.name()}"${s:inArray(paramValues.cause, cause.name()) ? ' selected="selected"' : ''}><c:out value="${cause.label}"/></option>
                                            </c:forEach>
                                        </select>
                                    </div>
                                </li>                                
                            </ul>
                        </fieldset>
                    </div>
                    <input type="hidden" name="qualified" value=""/>
                    <input class="filter-form-submit-button" type="submit" value="Apply"/>
                </form>                                                    
            </s:filter-flyout-widget>
            <h2 class="page-header-title"><c:out value="${title}"/></h2>
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
                                            <th class="${param.data eq 'count' ? 'selected-column' : ''} count-data">Number of Trips</th>
                                            <th>Duration (Hours)</th>
                                            <th>Grouping</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <c:forEach items="${trendList}" var="bin">
                                            <tr>
                                                <fmt:formatDate value="${bin.start}" pattern="dd-MMM-YYYY HH z" var="dateWithTz"/>
                                                <td data-date-utc="${dtm:getLocalTime(bin.start)}" title="${dateWithTz}"><fmt:formatDate value="${bin.start}" pattern="dd-MMM-YYYY HH"/></td>
                                                <td class="count-data right-aligned"><fmt:formatNumber value="${bin.count}" pattern="#,##0"/></td>
                                                <td class="duration-data right-aligned"><fmt:formatNumber value="${bin.durationMillis / 3600000}" pattern="#,##0.0000"/></td>
                                                <td><c:out value="${bin.grouping}"/></td>
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
            <form id="csv-form" method="get" action="${pageContext.request.contextPath}/csv/trip-summary.csv">
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
    </jsp:body>         
</t:reports-page>