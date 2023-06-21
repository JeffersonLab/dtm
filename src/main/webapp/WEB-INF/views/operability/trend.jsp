<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@taglib prefix="s" uri="http://jlab.org/jsp/smoothness"%>
<%@taglib prefix="dtm" uri="http://jlab.org/dtm/functions"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%> 
<c:set var="title" value="Trend Report"/>
<t:operability-page title="${title}">  
    <jsp:attribute name="stylesheets">        
    </jsp:attribute>
    <jsp:attribute name="scripts">       
        <script type="text/javascript" src="${pageContext.request.contextPath}/resources/v${initParam.releaseNumber}/js/availability-trend.js"></script>
    </jsp:attribute>        
    <jsp:body>
        <section>
            <fmt:formatDate var="startFmt" value="${start}" pattern="${s:getFriendlyDateTimePattern()}"/>
            <fmt:formatDate var="startMonthFmt" value="${start}" pattern="MMMM yyyy"/>
            <fmt:formatDate var="endFmt" value="${end}" pattern="${s:getFriendlyDateTimePattern()}"/>
            <fmt:formatDate var="endMonthFmt" value="${end}" pattern="MMMM yyyy"/>       
            <div id="report-page-actions">               
                <button id="fullscreen-button">Full Screen</button>
                <div id="export-widget">
                    <button id="export-menu-button">Export</button>
                    <ul id="export-menu">
                        <li id="excel-menu-item">Excel</li>                        
                    </ul>
                </div>      
            </div>
            <s:filter-flyout-widget  requiredMessage="true">
                <form id="filter-form" method="get" action="trend">
                    <fieldset>
                        <legend>Time</legend>
                        <s:date-range required="${true}" datetime="${true}" sevenAmOffset="{true}"/>
                    </fieldset>
                    <fieldset>
                        <legend>Chart</legend>
                        <ul class="key-value-list">
                            <li>
                                <div class="li-key">
                                    <label for="type">Type</label>
                                </div>
                                <div class="li-value">
                                    <select id="type" name="type">
                                        <option value="table"${'table' eq param.type ? ' selected="selected"' : ''}>Table</option>
                                    </select>
                                </div>
                            </li>
                            <li>
                                <div class="li-key">
                                    <label for="size">Bin Size</label>
                                </div>
                                <div class="li-value">
                                    <select id="size" name="size">
                                        <option value="none"${'none' eq param.size ? ' selected="selected"' : ''}>None</option>
                                        <option value="day"${'day' eq param.size ? ' selected="selected"' : ''}>Day</option>
                                        <option value="week"${'week' eq param.size ? ' selected="selected"' : ''}>Week</option>
                                        <option value="month"${'month' eq param.size ? ' selected="selected"' : ''}>Month</option>
                                        <option value="quarter"${'quarter' eq param.size ? ' selected="selected"' : ''}>Quarter</option>
                                        <option value="year"${'year' eq param.size ? ' selected="selected"' : ''}>Year</option>
                                    </select>
                                </div>
                            </li>
                        </ul>
                    </fieldset>
                    <input type="hidden" name="qualified" value=""/>
                    <input type="hidden" id="offset-input" name="offset" value="0"/>
                    <input id="filter-form-submit-button" type="submit" value="Apply"/>                                
                </form>                                                    
            </s:filter-flyout-widget>
            <h2 id="page-header-title"><c:out value="${title}"/></h2>
            <c:choose>
                <c:when test="${start == null || end == null}">
                    <div class="message-box">Select a date range to continue</div>
                </c:when>
                <c:when test="${errorMessage ne null}">
                    <div class="message-box error-message"><c:out value="${errorMessage}"/></div>
                    <div class="message-box"><c:out value="${selectionMessage}"/></div>
                </c:when>
                <c:otherwise>                      
                    <div class="message-box"><c:out value="${selectionMessage}"/></div>
                    <div id="table-scroll-viewport" style="overflow: auto;">
                        <table id="record-table" class="data-table">
                            <thead>
                                <tr>
                                    <th rowspan="3" style="min-width: 100px;">Bin</th>
                                    <th rowspan="2" colspan="3">Overall</th>
                                    <th rowspan="2" colspan="5">Trips</th>
                                    <th rowspan="2" colspan="5">Events</th>
                                    <th colspan="${alphaCatList.size() * 5}">Categories</th>
                                </tr>
                                <tr>
                                        <c:forEach items="${alphaCatList}" var="category">
                                        <th colspan="5"><c:out value="${category.name}"/></th>
                                        </c:forEach>
                                </tr>
                                <tr>
                                    <th>Downtime (Hours)</th> 
                                    <th>Uptime (Hours)</th>
                                    <th>Availability (%)</th>
                                    <th>Downtime (Hours)</th> 
                                    <th>Count</th>
                                    <th>MTTR (Minutes)</th>
                                    <th>MTBF (Minutes)</th>
                                    <th>Availability (%)</th>
                                    <th>Downtime (Hours)</th> 
                                    <th>Count</th>
                                    <th>MTTR (Hours)</th>
                                    <th>MTBF (Hours)</th>
                                    <th>Availability (%)</th> 
                                        <c:forEach items="${alphaCatList}" var="category">
                                        <th>Repair Time (Hours)</th>
                                        <th>Count</th>
                                        <th>MTTR (Hours)</th>
                                        <th>MTBF (Hours)</th>
                                        <th>Availability (%)</th>
                                        </c:forEach>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach items="${recordList}" var="record">
                                    <tr>
                                        <!-- Month Name -->
                                        <td data-date-utc="${dtm:getLocalTime(record.bin)}"><fmt:formatDate pattern="yyyy-MM-dd" value="${record.bin}"/></td>

                                        <!-- Overall Metrics -->
                                        <td class="right-aligned"><fmt:formatNumber pattern="#,###,##0.0" value="${record.accDownHours}"/></td>
                                        <td class="right-aligned"><fmt:formatNumber pattern="#,###,##0.0" value="${record.accUptimeHours}"/></td>
                                        <td class="right-aligned availability"><fmt:formatNumber pattern="#,###,##0.0" value="${record.accAvailability}"/></td>

                                        <!-- Trip Metrics -->
                                        <td class="downtime right-aligned"><fmt:formatNumber pattern="#,###,##0.0" value="${record.tripHours}"/></td>
                                        <td class="count right-aligned"><fmt:formatNumber pattern="#,###,##0" value="${record.tripCount}"/></td>
                                        <td class="mttr right-aligned"><fmt:formatNumber pattern="#,###,##0.0" value="${record.tripMttrHours * 60}"/></td>
                                        <td class="mtbf right-aligned"><fmt:formatNumber pattern="#,###,##0.0" value="${record.mtbtHours * 60}"/></td>
                                        <td class="right-aligned availability"><fmt:formatNumber pattern="#,###,##0.0" value="${record.tripAvailability}"/></td>

                                        <!-- Event Metrics -->
                                        <td class="downtime right-aligned"><fmt:formatNumber pattern="#,###,##0.0" value="${record.eventHours}"/></td>
                                        <td class="count right-aligned"><fmt:formatNumber pattern="#,###,##0" value="${record.eventCount}"/></td>
                                        <td class="mttr right-aligned"><fmt:formatNumber pattern="#,###,##0.0" value="${record.eventMttrHours}"/></td>
                                        <td class="mtbf right-aligned"><fmt:formatNumber pattern="#,###,##0.0" value="${record.eventHours}"/></td>
                                        <td class="right-aligned availability"><fmt:formatNumber pattern="#,###,##0.0" value="${record.eventAvailability}"/></td>

                                        <!-- Category Metrics -->
                                        <c:forEach items="${alphaCatList}" var="category">
                                            <c:set value="${record.downtimeMap[category.categoryId.longValue()]}" var="downtime"/>
                                            <td class="downtime right-aligned"><fmt:formatNumber value="${downtime.duration * 24}" pattern="#,##0.0"/></td>
                                            <td class="count right-aligned"><fmt:formatNumber value="${downtime.incidentCount}" pattern="#,##0"/></td>
                                            <td class="mttr right-aligned"><fmt:formatNumber value="${downtime.incidentCount == 0 ? 0 : downtime.duration / downtime.incidentCount * 24}" pattern="#,##0.0"/></td>
                                            <c:set var="uptime" value="${record.programHours - (downtime.duration * 24)}"/>
                                            <c:set var="uptime" value="${uptime < 0 ? 0 : uptime}"/>
                                            <c:set var="mtbf" value="${downtime.incidentCount == 0 ? '' : uptime / downtime.incidentCount}"/>
                                            <c:set var="availability" value="${record.programHours == 0 ? 0 : uptime / record.programHours * 100}"/>
                                            <td class="mtbf right-aligned"><fmt:formatNumber value="${mtbf}" pattern="#,##0.0"/></td>
                                            <td class="availability right-aligned"><fmt:formatNumber value="${availability}" pattern="#,##0.0"/></td>
                                        </c:forEach>
                                    </tr>
                                </c:forEach>
                            </tbody>
                        </table>    
                    </div>
                </c:otherwise>
            </c:choose>
        </section>
        <div id="exit-fullscreen-panel">
            <button id="exit-fullscreen-button">Exit Full Screen</button>
        </div>
        <form id="excel-form" method="get" action="${pageContext.request.contextPath}/excel/trend.xlsx">
            <input type="hidden" name="start" value="${param.start}"/>
            <input type="hidden" name="end" value="${param.end}"/>
            <input type="hidden" name="size" value="${param.size}"/>
            <input type="hidden" name="qualified" value=""/>
            <button id="excel" type="submit" style="display: none;">Excel</button>
        </form>            
    </jsp:body>         
</t:operability-page>