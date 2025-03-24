<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@taglib prefix="s" uri="http://jlab.org/jsp/smoothness"%>
<%@taglib prefix="dtm" uri="http://jlab.org/dtm/functions"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%> 
<jsp:useBean id="now" class="java.util.Date"/>
<c:set var="title" value="Tune Incidents"/>
<t:beam-team-page title="${title}">  
    <jsp:attribute name="stylesheets">
        <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/resources/v${initParam.releaseNumber}/css/downtime-report.css"/>
    </jsp:attribute>
    <jsp:attribute name="scripts">
        <script type="text/javascript">
            $(document).on("click", ".default-reset-panel", function () {
                $("#date-range").val('custom').change();
                $("#start").val('');
                $("#end").val('');
                $("#type").val('');
                $("#component").val('');
                return false;
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
                    </ul>
                </div>
            </div>            
            <s:filter-flyout-widget  resetButton="true">
                <form class="filter-form" method="get" action="tune-incidents">
                    <fieldset>
                        <legend>Time</legend>
                        <s:date-range datetime="${true}" sevenAmOffset="${true}"/>
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
                                    <label for="component">Component</label>
                                </div>
                                <div class="li-value">
                                    <select style="display:none;" id="hidden-system" name="hidden-system"><option selected="selected" value="616">Beam Transport</option></select>
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
                            <c:if test="${periodDurationHours ne null}">
                                <tr>
                                    <th>Period Duration (Hours): </th>
                                    <td><fmt:formatNumber value="${periodDurationHours}" pattern="#,##0.0"/></td>
                                </tr>
                            </c:if>
                        </tbody>
                    </table>
                    <h3>Incidents</h3>
                    <table class="data-table stripped-table">
                        <thead>
                            <tr>
                                <th>Time Down</th>
                                <th>Downtime (Hours)</th>                                        
                                <th>Type</th>
                                <th>Title</th>
                                <th>Summary</th>
                                <th>Component</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach items="${incidentList}" var="incident">
                                <tr>
                                    <td><fmt:formatDate value="${incident.timeDown}" pattern="${s:getFriendlyDateTimePattern()}"/></td>
                                    <td title="Not Bounded: ${formattedUnbounded}" class="right-aligned"><fmt:formatNumber value="${incident.downtimeHoursBounded}" pattern="#,##0.0"/></td>                                            
                                    <td><c:out value="${incident.type}"/></td>
                                    <td><a href="${pageContext.request.contextPath}/incidents/${incident.incidentId}" title="Incident Detail"><c:out value="${incident.title}"/></a></td>
                                        <fmt:formatNumber value="${incident.downtimeHours}" var="formattedUnbounded" pattern="#,##0.0"/>
                                    <td><c:out value="${incident.summary}"/></td>
                                    <td><c:out value="${incident.componentName}"/></td>
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
                            <input type="hidden" name="type" value="${type.eventTypeId}"/>
                            <input type="hidden" name="system" value="${fn:escapeXml(param.system)}"/>
                            <input type="hidden" name="component" value="${fn:escapeXml(param.component)}"/>
                            <input type="hidden" name="transport" value="${fn:escapeXml(param.transport)}"/>
                            <button id="shiftlog" type="submit" style="display: none;">Shift Log</button>
                        </form>                            
                        <form id="excel-form" method="get" action="${pageContext.request.contextPath}/excel/tune-incident-list.xlsx">
                            <input type="hidden" name="start" value="${startFmt}"/>
                            <input type="hidden" name="end" value="${endFmt}"/>
                            <input type="hidden" name="type" value="${type.eventTypeId}"/>
                            <input type="hidden" name="component" value="${fn:escapeXml(param.component)}"/>
                            <button id="excel" type="submit" style="display: none;">Excel</button>
                        </form>
                    </div>
                </div>
            </c:if>
        </section>
        <div id="exit-fullscreen-panel">
            <button id="exit-fullscreen-button">Exit Full Screen</button>
        </div>           
    </jsp:body>         
</t:beam-team-page>