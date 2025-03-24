<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@taglib prefix="s" uri="http://jlab.org/jsp/smoothness"%>
<%@taglib prefix="dtm" uri="http://jlab.org/dtm/functions"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%> 
<jsp:useBean id="now" class="java.util.Date"/>
<c:set var="title" value="Component Downtime"/>
<t:reports-page title="${title}">  
    <jsp:attribute name="stylesheets">
        <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/resources/v${initParam.releaseNumber}/css/downtime-report.css"/>
    </jsp:attribute>
    <jsp:attribute name="scripts">
        <c:choose>
            <c:when test="${'CDN' eq resourceLocation}">
                <script src="//${env['CDN_SERVER']}/jquery-plugins/flot/0.8.3/jquery.flot.min.js"></script>
                <script src="//${env['CDN_SERVER']}/jquery-plugins/flot/0.8.3/jquery.flot.pie.min.js"></script>
                <script src="//${env['CDN_SERVER']}/jquery-plugins/flot/0.8.3/jquery.flot.resize.min.js"></script>
            </c:when>
            <c:otherwise><!-- LOCAL -->
                <script src="${pageContext.request.contextPath}/resources/jquery-plugins/flot/0.8.3/jquery.flot.min.js"></script>
                <script src="${pageContext.request.contextPath}/resources/jquery-plugins/flot/0.8.3/jquery.flot.pie.min.js"></script>
                <script src="${pageContext.request.contextPath}/resources/jquery-plugins/flot/0.8.3/jquery.flot.resize.min.js"></script>
            </c:otherwise>
        </c:choose>
        <script type="text/javascript" src="${pageContext.request.contextPath}/resources/v${initParam.releaseNumber}/js/downtime-report.js"></script>
    </jsp:attribute>        
    <jsp:body>
        <section>
            <fmt:formatDate var="todayFmt" value="${today}" pattern="${s:getFriendlyDateTimePattern()}"/>
            <fmt:formatDate var="sevenDaysAgoFmt" value="${sevenDaysAgo}" pattern="${s:getFriendlyDateTimePattern()}"/>
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
                <form class="filter-form" method="get" action="component-downtime">
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
                                    <td><fmt:formatNumber value="${grandTotalDuration * 24}" pattern="#,##0.#"/></td>
                                </tr>
                                <tr>
                                    <th>Period Duration (Hours): </th>
                                    <td><fmt:formatNumber value="${periodDurationHours}" pattern="#,##0.#"/></td>
                                </tr>
                                <tr>
                                    <th>Component Count: </th>
                                    <td><fmt:formatNumber value="${fn:length(downtimeList)}" pattern="#,##0"/></td>
                                </tr>  
                                <tr>
                                    <th>Incident Count: </th>
                                    <td><fmt:formatNumber value="${incidentCount}" pattern="#,##0"/></td>
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
                                        <td><a target="_blank" href="${url}" title="BTM Report"><fmt:formatNumber pattern="#,###,##0.#" value="${programHours}"/></a></td>
                                    </tr>                                    
                                </c:if>           
                            </tbody>
                        </table>
                        <c:if test="${fn:length(downtimeList) > 0}">
                            <h3>Components</h3>
                            <table id="bar-chart-data-table" class="data-table stripped-table" data-x-label="Component">
                                <thead>
                                    <tr>
                                        <th>System and Component</th>
                                        <th class="selected-column downtime">Downtime (Hours) <span class="sort-desc" title="Descending">▼</span></th>
                                        <th class="count">Number of Incidents <span class="sort-desc" title="Descending">▼</span></th>
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
                                        <tr>
                                            <td>
                                                <c:url var="url" value="/reports/incident-downtime">
                                                    <c:param name="start" value="${param.start eq null ? sevenDaysAgoFmt : param.start}"/>
                                                    <c:param name="end" value="${param.end eq null ? todayFmt : param.end}"/>
                                                    <c:param name="type" value="${param.type eq null ? '1' : param.type}"/>
                                                    <c:param name="transport" value="${param.transport eq null ? 'N' : param.transport}"/>
                                                    <c:param name="system" value="${param.system eq null ? '' : param.system}"/>
                                                    <c:param name="component" value="${downtime.name}"/>
                                                    <c:param name="print" value="${param.print}"/>
                                                    <c:param name="fullscreen" value="${param.fullscreen}"/>      
                                                    <c:param name="qualified" value=""/>
                                                </c:url>
                                                <c:out value="${downtime.systemName}"/>; <a href="${url}" title="Incident Downtime Report"><c:out value="${downtime.name}"/></a>
                                            </td>
                                            <td class="downtime right-aligned"><fmt:formatNumber value="${downtime.duration * 24}" pattern="#,##0.0"/></td>
                                            <td class="count right-aligned"><fmt:formatNumber value="${downtime.incidentCount}" pattern="#,##0"/></td>
                                            <td class="mttr right-aligned"><fmt:formatNumber value="${downtime.duration / downtime.incidentCount * 24}" pattern="#,##0.0"/></td>
                                            <c:if test="${type.eventTypeId eq 1}">
                                                <c:set var="uptime" value="${programHours - (downtime.duration * 24)}"/>
                                                <c:set var="uptime" value="${uptime < 0 ? 0 : uptime}"/>
                                                <td class="uptime right-aligned"><fmt:formatNumber value="${uptime}" pattern="#,##0.0"/></td>
                                                <c:set var="mtbf" value="${uptime / downtime.incidentCount}"/>
                                                <td class="mtbf right-aligned"><fmt:formatNumber value="${mtbf}" pattern="#,##0.0"/></td>
                                                <c:set var="failure" value="${uptime == 0 ? '' : downtime.incidentCount / uptime}"/>                          
                                                <td class="failure right-aligned"><fmt:formatNumber value="${failure}" pattern="#,##0.000"/></td>
                                                <c:set var="availability" value="${programHours == 0 ? 0 : uptime / programHours * 100}"/>
                                                <td class="availability right-aligned"><fmt:formatNumber value="${availability}" pattern="#,##0.0"/>%</td>
                                                <td class="loss right-aligned"><fmt:formatNumber value="${100 - availability}" pattern="#,##0.0"/>%</td>
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