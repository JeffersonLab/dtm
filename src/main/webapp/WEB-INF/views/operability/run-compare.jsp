<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@taglib prefix="s" uri="http://jlab.org/jsp/smoothness"%>
<%@taglib prefix="dtm" uri="http://jlab.org/dtm/functions"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%> 
<c:set var="title" value="Run Comparison Report"/>
<t:operability-page title="${title}">  
    <jsp:attribute name="stylesheets">
        <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/resources/v${initParam.releaseNumber}/css/run-compare.css"/>
    </jsp:attribute>
    <jsp:attribute name="scripts">
        <c:choose>
            <c:when test="${'CDN' eq resourceLocation}">
                <script src="//${env['CDN_SERVER']}/jquery-plugins/flot/0.8.3/jquery.flot.min.js"></script>
                <script src="//${env['CDN_SERVER']}/jquery-plugins/flot/0.8.3/jquery.flot.resize.min.js"></script>
            </c:when>
            <c:otherwise><!-- LOCAL -->
                <script src="${pageContext.request.contextPath}/resources/jquery-plugins/flot/0.8.3/jquery.flot.min.js"></script>
                <script src="${pageContext.request.contextPath}/resources/jquery-plugins/flot/0.8.3/jquery.flot.resize.min.js"></script>
            </c:otherwise>
        </c:choose>
        <script type="text/javascript" src="${pageContext.request.contextPath}/resources/v${initParam.releaseNumber}/js/run-compare.js"></script>
    </jsp:attribute>        
    <jsp:body>
        <section>
            <div id="report-page-actions">               
                <button id="fullscreen-button">Full Screen</button>
            </div>
            <s:filter-flyout-widget  requiredMessage="true">
                <form id="filter-form" method="get" action="run-compare">
                    <fieldset>
                        <legend>Runs</legend>
                        <ul id="run-list" class="key-value-list">
                            <c:forEach items="${paramValues['label']}" varStatus="status">
                                <li>${paramValues['label'][status.index]}<input type="hidden" name="label" value="${paramValues['label'][status.index]}"/><input type="hidden" name="start" value="${paramValues['start'][status.index]}"/><input type="hidden" name="end" value="${paramValues['end'][status.index]}"/> <button type="button">X</button></li>
                            </c:forEach>
                        </ul>
                        <button type="button" id="add-run-button">Add Run</button>
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
                                        <option value="line"${'line' eq param.type ? ' selected="selected"' : ''}>Accumulated Downtime</option>
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
                <c:when test="${recordListList.isEmpty()}">
                    <div class="message-box">Select a date range to continue</div>
                </c:when>
                <c:when test="${errorMessage ne null}">
                    <div class="message-box error-message"><c:out value="${errorMessage}"/></div>
                    <div class="message-box"><c:out value="${selectionMessage}"/></div>
                </c:when>
                <c:otherwise>                      
                    <div class="message-box"><c:out value="${selectionMessage}"/></div>
                    <c:if test="${param.type ne 'table'}">
                        <div id="chart-wrap">
                            <div id="chart-placeholder"></div>
                        </div>
                    </c:if>
                    <div id="table-scroll-viewport" style="overflow: auto;">
                        <table id="source-table" class="${param.type eq 'table' ? '' : 'hidden-table'} data-table">
                            <thead>
                                <tr>
                                    <th rowspan="3" style="min-width: 100px;">Bin</th>
                                    <th rowspan="2" colspan="3">Overall</th>
                                    <th rowspan="2" colspan="5">Trips</th>
                                    <th rowspan="2" colspan="5">Events</th>
                                    <c:if test="${!empty alphaCatList}">
                                        <th colspan="${alphaCatList.size() * 5}">Categories</th>
                                    </c:if>
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
                            <c:forEach items="${recordListList}" var="recordList" varStatus="status">
                            <tbody>
                                <tr>
                                    <th colspan="${14 + (alphaCatList.size() * 5)}"><c:out value="${paramValues['label'][status.index]}"/></th>
                                </tr>
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
                            </c:forEach>
                        </table>    
                    </div>
                </c:otherwise>
            </c:choose>
        </section>
        <div id="exit-fullscreen-panel">
            <button id="exit-fullscreen-button">Exit Full Screen</button>
        </div>
        <div id="add-run-dialog" title="Add Run" class="dialog">
            <fieldset style="display: none;">
                <legend>Run Lookup</legend>
            <ul class="key-value-list">
                <li>
                    <div class="li-key">
                        <label for="year">Year</label>
                    </div>
                    <div class="li-value">
                        <input id="year" type="number" min="2015"/>
                    </div>
                </li>
                <li>
                    <div class="li-key">
                        <label for="run-number">Run #</label>
                    </div>
                    <div class="li-value">
                        <input id="run-number" type="number" min="1" max="20"/>
                    </div>
                </li>
            </ul>
            <button id="lookup-button" type="button">Lookup</button>
            </fieldset>
            <br/>
            <ul id="custom-date-range-list" class="key-value-list" style="">
                <li>
                    <div class="li-key">
                        <label for="start" title="Inclusive (Closed)">Start
                            Date</label>
                        <div class="date-note">(Inclusive)</div>
                    </div>
                    <div class="li-value">
                        <input type="text" class="datetime-input" id="start" name="start" autocomplete="off" placeholder="DD-MMM-YYYY hh:mm" value="">
                    </div>
                </li>
                <li>
                    <div class="li-key">
                        <label for="end" title="Exclusive (Open)">End Date</label>
                        <div class="date-note">(Exclusive)</div>
                    </div>
                    <div class="li-value">
                        <input type="text" class="datetime-input" id="end" name="end" autocomplete="off" placeholder="DD-MMM-YYYY hh:mm" value="">
                    </div>
                </li>
                <li>
                    <div class="li-key">
                        <label for="label">Label</label>
                    </div>
                    <div class="li-value">
                        <input type="text" id="label" name="label" autocomplete="off" value="">
                    </div>
                </li>
            </ul>
            <div class="dialog-button-panel">
                <button id="add-selected-run-button" type="button">Add</button>
                <button class="dialog-close-button" type="button">Cancel</button>
            </div>
        </div>
    </jsp:body>         
</t:operability-page>