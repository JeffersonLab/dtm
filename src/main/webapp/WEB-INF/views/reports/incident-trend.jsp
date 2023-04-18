<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@taglib prefix="s" uri="http://jlab.org/jsp/smoothness"%>
<%@taglib prefix="dtm" uri="http://jlab.org/dtm/functions"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%> 
<jsp:useBean id="now" class="java.util.Date"/>
<c:set var="title" value="Incident Trend <EXPERIMENTAL>"/>
<t:reports-page title="${title}">  
    <jsp:attribute name="stylesheets">
        <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/resources/v${initParam.releaseNumber}/css/trend-report.css"/>
    </jsp:attribute>
    <jsp:attribute name="scripts">
        <c:choose>
            <c:when test="${'CDN' eq resourceLocation}">
                <script src="${cdnContextPath}/jquery-plugins/flot/0.8.3/jquery.flot.min.js"></script>
                <script src="${cdnContextPath}/jquery-plugins/flot/0.8.3/jquery.flot.stack.min.js"></script>
                <script src="${cdnContextPath}/jquery-plugins/flot/0.8.3/jquery.flot.time.min.js"></script>
                <script src="${cdnContextPath}/jquery-plugins/flot/0.8.3/jquery.flot.resize.min.js"></script>
            </c:when>
            <c:otherwise><!-- LOCAL -->
                <script src="${pageContext.request.contextPath}/resources/jquery-plugins/flot/0.8.3/jquery.flot.min.js"></script>
                <script src="${pageContext.request.contextPath}/resources/jquery-plugins/flot/0.8.3/jquery.flot.stack.min.js"></script>
                <script src="${pageContext.request.contextPath}/jquery-plugins/flot/0.8.3/jquery.flot.time.min.js"></script>
                <script src="${pageContext.request.contextPath}/resources/jquery-plugins/flot/0.8.3/jquery.flot.resize.min.js"></script>
            </c:otherwise>
        </c:choose>
        <script type="text/javascript" src="${pageContext.request.contextPath}/resources/v${initParam.releaseNumber}/js/trend-report.js"></script>
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
                    </ul>
                </div>
            </div>            
            <s:filter-flyout-widget  requiredMessage="true">
                <form id="filter-form" method="get" action="incident-trend">
                    <div id="filter-form-panel">
                        <fieldset>
                            <legend>Time</legend>
                            <ul class="key-value-list"> 
                                <li class="required-field">
                                    <div class="li-key"><label for="range">Date Range</label></div>
                                    <div class="li-value">
                                        <select id="range">
                                            <option value="7days"${range eq '7days' ? ' selected="selected"' : ''}>Past 7 Days (From 7:00)</option>
                                            <option value="3days"${range eq '3days' ? ' selected="selected"' : ''}>Past 3 Days (From 7:00)</option>
                                            <option value="1day"${range eq '1day' ? ' selected="selected"' : ''}>Past 1 Day (From 7:00)</option>
                                            <option value="1shift"${range eq '1shift' ? ' selected="selected"' : ''}>Previous Shift (${previousShift})</option>
                                            <option value="0shift"${range eq '0shift' ? ' selected="selected"' : ''}>Current Shift (${currentShift})</option>
                                            <option value="custom"${range eq 'custom' ? ' selected="selected"' : ''}>Custom...</option>
                                        </select>
                                    </div>
                                </li>                        
                            </ul>
                            <ul id="custom-date-range-list" class="key-value-list" ${range ne 'custom' ? 'style="display: none;"' : ''}>                         
                                <li class="required-field">
                                    <div class="li-key">
                                        <label for="start" title="Inclusive">Start Date</label>
                                    </div>
                                    <div class="li-value">
                                        <input type="text" class="date-field" id="start" name="start" placeholder="DD-MMM-YYYY hh:mm" value="${param.start != null ? param.start : sevenDaysAgoFmt}"/>
                                    </div>
                                </li>
                                <li class="required-field">
                                    <div class="li-key">
                                        <label for="end" title="Exclusive">End Date</label>
                                    </div>
                                    <div class="li-value">
                                        <input type="text" class="date-field nowable-field" id="end" name="end" placeholder="DD-MMM-YYYY hh:mm" value="${param.end != null ? param.end : todayFmt}"/>
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
                                            <option value="N"${param.transport eq 'N' ? ' selected="selected"' : ''}>Exclude</option>
                                            <option value="Y"${param.transport eq 'Y' ? ' selected="selected"' : ''}>Include</option>
                                        </select>
                                    </div>
                                </li> 
                                <li>
                                    <div class="li-key">
                                        <label for="category-select">Category</label>
                                    </div>
                                    <div class="li-value">
                                        <select id="category-select" name="category" multiple="multiple">
                                            <c:forEach items="${categoryList}" var="category">
                                                <option value="${category.categoryId}" ${s:inArray(paramValues.category, category.categoryId.toString()) ? 'selected="selected"' : ''}><c:out value="${category.name}"/></option>
                                            </c:forEach>
                                        </select>
                                        <span id="system-indicator" class="form-control-indicator"></span>
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
                                        <label for="interval">Interval</label>
                                    </div>
                                    <div class="li-value">
                                        <select id="interval" name="interval">         
                                            <option value="1"${param.interval eq '1' ? ' selected="selected"' : ''}>Hourly</option>
                                            <option value="24"${param.interval eq '24' ? ' selected="selected"' : ''}>Daily</option>
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
                                            <option value="category"${param.grouping eq 'category' ? ' selected="selected"' : ''}>Category</option>
                                        </select>
                                    </div>
                                </li>                                            
                            </ul>
                        </fieldset>
                    </div>
                    <input type="hidden" name="referrer" value="form"/>
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
                        <c:out value="${selectionMessage}"/>
                    </div>
                    <c:choose>
                        <c:when test="${fn:length(trendList) > 0}">
                            <div id="chart-placeholder" style="${param.chart eq 'table' ? 'display: none;' : ''}"></div>    
                        </c:when>
                        <c:otherwise>
                            <div>No data to chart</div>
                        </c:otherwise>
                    </c:choose>
                    <c:if test="${fn:length(trendList) > 0}">
                        <div id="data-table-panel" style="${param.chart eq 'table' ? '' : 'display: none;'}">
                            <table id="bar-chart-data-table" data-start-millis="${dtm:getLocalTime(start)}" data-end-millis="${dtm:getLocalTime(endInclusive)}" data-y-axis-prefix="Incident">
                                <thead>
                                    <tr>
                                        <th>Date <span style="display: inline-block;" class="sort-asc" title="Ascending">▲</span></th>
                                        <th class="${param.data eq 'downtime' ? 'selected-column' : ''} downtime-data">Downtime (Hours)</th>
                                        <th class="${param.data eq 'count' ? 'selected-column' : ''} count-data">Number of Incidents</th>
                                        <th class="${param.data eq 'mttr' ? 'selected-column' : ''} mttr-data">Mean Time to Recover (Hours)</th>
                                        <th>Grouping</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <c:forEach items="${trendList}" var="downtime">
                                        <tr>

                                            <td data-date-utc="${dtm:getLocalTime(downtime.date)}"><fmt:formatDate value="${downtime.date}" pattern="dd-MMM-YYYY HH"/></td>
                                            <td class="downtime-data right-aligned"><fmt:formatNumber value="${downtime.duration}" pattern="#,##0.0"/></td>
                                            <td class="count-data right-aligned"><fmt:formatNumber value="${downtime.count}" pattern="#,##0"/></td>
                                            <td class="mttr-data right-aligned"><fmt:formatNumber value="${downtime.count eq 0 ? 0 : downtime.duration / downtime.count}" pattern="#,##0.0"/></td>
                                            <td><c:out value="${downtime.categoryName}"/></td>
                                        </tr>
                                    </c:forEach>
                                </tbody>
                            </table>
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