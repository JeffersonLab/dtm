<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="jakarta.tags.core"%>
<%@taglib prefix="fn" uri="jakarta.tags.functions"%>
<%@taglib prefix="fmt" uri="jakarta.tags.fmt"%>
<%@taglib prefix="s" uri="jlab.tags.smoothness"%>
<%@taglib prefix="dtm" uri="jlab.tags.dtm"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%> 
<c:set var="title" value="Accelerator System Repair Report"/>
<t:operability-page title="${title}">  
    <jsp:attribute name="stylesheets">      
        <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/resources/v${initParam.releaseNumber}/css/annual-repair.css"/>
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
        <script type="text/javascript" src="${pageContext.request.contextPath}/resources/v${initParam.releaseNumber}/js/annual-repair.js"></script>
    </jsp:attribute>        
    <jsp:body>
        <section>
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
                <form class="filter-form" method="get" action="annual-repair">
                    <fieldset>
                        <ul class="key-value-list">                         
                            <li>
                                <div class="li-key">
                                    <label class="required-field" for="start" title="Inclusive">Start Date</label>
                                </div>
                                <div class="li-value">
                                    <input type="text" class="date-field" id="start" name="start" placeholder="DD-MMM-YYYY hh:mm" value="${startFmt}"/>
                                </div>
                            </li>  
                            <li>
                                <div class="li-key">
                                    <label for="chart">Chart</label>
                                </div>
                                <div class="li-value">
                                    <select id="chart" name="chart">
                                        <option value="bar"${param.chart eq 'bar' ? ' selected="selected"' : ''}>Bar</option>
                                        <option value="table"${param.chart eq 'table' ? ' selected="selected"' : ''}>Table</option>
                                    </select>
                                </div>
                            </li> 
                        </ul>
                    </fieldset>
                    <input class="filter-form-submit-button" type="submit" value="Apply"/>
                </form>                                                    
            </s:filter-flyout-widget>
            <h2 class="page-header-title"><c:out value="${title}"/></h2>
            <c:choose>
                <c:when test="${start == null || end == null}">
                    <div class="message-box">Select a start date to continue</div>
                </c:when>
                <c:otherwise>
                    <div class="message-box">
                        <c:out value="${selectionMessage}"/>
                    </div>
                    <c:choose>
                        <c:when test="${fn:length(recordList) > 0}">
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
                            <div class="data-table-panel chart-wrap-backdrop" style="${param.chart eq 'table' ? '' : 'display: none;'}">
                                <h3>Incident Downtime Grouped By Category and Month</h3>
                                <table id="graph-data-table" class="data-table stripped-table">
                                    <thead>
                                        <tr>
                                            <th>Category</th>
                                            <th>Month</th>
                                            <th>Incident Downtime (Hours)</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <c:forEach items="${recordList}" var="record">
                                            <tr>
                                                <td data-id="${record.categoryId}"><c:out value="${record.category}"/></td>
                                                <td><fmt:formatDate pattern="MMM yyyy" value="${record.month}"/></td>
                                                <td><fmt:formatNumber pattern="###,###,##0.0" value="${record.downtimeHours}"/></td>
                                            </tr>
                                        </c:forEach>
                                    </tbody>
                                </table>
                                <h3>Program Time Grouped By Month</h3>
                                <table id="program-table" class="data-table stripped-table">
                                    <thead>
                                        <tr>
                                            <th>Month</th>
                                            <th>Program Time (Hours)</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <c:forEach items="${monthTotals}" var="record">
                                            <tr>
                                                <td><fmt:formatDate pattern="MMM yyyy" value="${record.month}"/></td>
                                                <td><fmt:formatNumber pattern="###,###,##0.0" value="${record.totals.calculateProgramSeconds() / 3600.0}"/></td>
                                            </tr>
                                        </c:forEach>
                                    </tbody>
                                </table>
                            </div>
                        </c:when>
                        <c:otherwise>
                            <div>No incidents this period!</div>
                        </c:otherwise>
                    </c:choose>
                </c:otherwise>
            </c:choose>
        </section>
        <div id="exit-fullscreen-panel">
            <button id="exit-fullscreen-button">Exit Full Screen</button>
        </div>
        <script type="text/javascript">
            var jlab = jlab || {};
            jlab.minDate = new Date(${start.time});
            jlab.maxDate = new Date(${end.time});
            jlab.fullscreen = ${param.fullscreen == "Y" ? true : false};
        </script>
    </jsp:body>         
</t:operability-page>