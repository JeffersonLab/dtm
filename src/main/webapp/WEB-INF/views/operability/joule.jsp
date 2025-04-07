<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@taglib prefix="s" uri="http://jlab.org/jsp/smoothness"%>
<%@taglib prefix="dtm" uri="http://jlab.org/dtm/functions"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%>
<c:set var="title" value="Joule Report"/>
<t:operability-page title="${title}">  
    <jsp:attribute name="stylesheets">
        <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/resources/v${initParam.releaseNumber}/css/summary-table.css"/>
        <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/resources/v${initParam.releaseNumber}/css/joule.css"/>
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
        <script src="${pageContext.request.contextPath}/resources/v${initParam.releaseNumber}/js/flot-time.js"></script>
        <script src="${pageContext.request.contextPath}/resources/v${initParam.releaseNumber}/js/jquery.flot.dashes.js"></script>
        <script src="${pageContext.request.contextPath}/resources/v${initParam.releaseNumber}/js/joule.js"></script>
    </jsp:attribute>
    <jsp:body>
        <section>
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
                <form class="filter-form" method="get" action="joule">
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
                                        <option value="point"${'point' eq param.type ? ' selected="selected"' : ''}>Reliability Trend</option>
                                        <option value="line"${'line' eq param.type ? ' selected="selected"' : ''}>Delivered vs Budgeted Progress</option>
                                        <option value="bar"${'bar' eq param.type ? ' selected="selected"' : ''}>Delivered vs Failures History</option>
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
                    <fieldset>
                        <legend>Adjustments (Evenly distributed into bins)</legend>
                        <ul class="key-value-list">
                            <li>
                                <div class="li-key">
                                    <label for="maintenance-hours">Maintenance Hours</label>
                                </div>
                                <div class="li-value">
                                    <input type="number" min="0" step="0.1" id="maintenance-hours" name="maintenance" value="${fn:escapeXml(param.maintenance)}"/>
                                    <div>Allowance of Scheduled Failures to be subtracted from Unscheduled Failures.  Maintenance is expected and generally negotiated as 4 hours per research week and 24 hours per restore week.</div>
                                </div>
                            </li>
                            <li>
                                <div class="li-key">
                                    <label for="quality-hours">Quality Hours</label>
                                </div>
                                <div class="li-value">
                                    <input type="number" min="0" step="0.1" id="quality-hours" name="quality" value="${fn:escapeXml(param.quality)}"/>
                                    <div>Allowance of extra hours to be added to Delivered Research.  Quality is a bonus adjustment for program difficulty such as hall multiplicity.</div>
                                </div>
                            </li>
                            <li>
                                <div class="li-key">
                                    <label for="budget-scaler">Budget Scaler</label>
                                </div>
                                <div class="li-value">
                                    <input type="number" min="-1" max="2" step="0.05" id="budget-scaler" name="scaler" value="${fn:escapeXml(param.scaler)}"/>
                                    <div>Scale factor to be multiplied with Budgeted Operations.  Scaler is an adjustment to accommodate situations such as when the NPES schedules double the expected delivered as a buffer to account for failures, crises, and maintenance.</div>
                                </div>
                            </li>
                        </ul>
                    </fieldset>
                    <input type="hidden" name="qualified" value=""/>
                    <input class="filter-form-submit-button" type="submit" value="Apply"/>
                </form>
            </s:filter-flyout-widget>
            <h2 class="page-header-title"><c:out value="${title}"/></h2>
            <c:choose>
                <c:when test="${start == null || end == null}">
                    <div class="message-box">Select a start date, and end date to continue</div>
                </c:when>
                <c:otherwise>
                    <div class="message-box">
                        <c:out value="${selectionMessage}"/>
                    </div>
                    <c:if test="${param.type ne 'table'}">
                        <div class="chart-wrap">
                            <div class="chart-placeholder"></div>
                        </div>
                    </c:if>
                    <div class="${fn:length(recordList) eq 1 ? 'summary-wrap' : 'multi-wrap'}">
                        <table id="source-table" class="${param.type eq 'table' ? '' : 'hidden-table'} data-table stripped-table ${fn:length(recordList) eq 1 ? 'summary-table' : 'multi-records'}" data-start-millis="${dtm:getLocalTime(start)}" data-end-millis="${dtm:getLocalTime(endInclusive)}">
                            <c:choose>
                                <c:when test="${fn:length(recordList) eq 1}">
                                    <c:set value="${recordList[0]}" var="record"/>
                                    <tbody>
                                    <tr>
                                        <th>Delivered Research (Hours)<a title="Explanation" class="flyout-link" data-flyout-type="delivered-research-flyout" href="#">*</a>: </th>
                                        <td><fmt:formatNumber pattern="###,##0.0" value="${record.deliveredResearchHours}"/></td>
                                    </tr>
                                    <tr>
                                        <th>Delivered Beam Studies (Hours)<a title="Explanation" class="flyout-link" data-flyout-type="beam-studies-flyout" href="#">*</a>: </th>
                                        <td><fmt:formatNumber pattern="###,##0.0" value="${record.deliveredBeamStudiesHours}"/></td>
                                    </tr>
                                    <tr>
                                        <th>Delivered Tuning &amp; Restore (Hours)<a title="Explanation" class="flyout-link" data-flyout-type="tuning-restore-flyout" href="#">*</a>: </th>
                                        <td><fmt:formatNumber pattern="###,##0.0" value="${record.deliveredTuningAndRestoreHours}"/></td>
                                    </tr>
                                    <tr>
                                        <th>Total Delivered (Hours)<a title="Explanation" class="flyout-link" data-flyout-type="total-delivered-flyout" href="#">*</a>: </th>
                                        <td><fmt:formatNumber pattern="###,##0.0" value="${record.totalDeliveredHours}"/></td>
                                    </tr>
                                    <tr>
                                        <th>Budgeted Operations (Hours)<a title="Explanation" class="flyout-link" data-flyout-type="budgeted-flyout" href="#">*</a>: </th>
                                        <td><fmt:formatNumber pattern="###,##0.0" value="${record.budgetedOperationsHours}"/></td>
                                    </tr>
                                    <tr>
                                        <th>Total Delivered / Budgeted (%)<a title="Explanation" class="flyout-link" data-flyout-type="budgeted-availability-flyout" href="#">*</a>: </th>
                                        <td><fmt:formatNumber pattern="###,##0.0" value="${record.budgetedAvailability}"/>%</td>
                                    </tr>
                                    <tr>
                                        <th>Unscheduled Failures (Hours)<a title="Explanation" class="flyout-link" data-flyout-type="unscheduled-failures-flyout" href="#">*</a>: </th>
                                        <td><fmt:formatNumber pattern="###,##0.0" value="${record.unscheduledFailuresHours}"/></td>
                                    </tr>
                                    <tr>
                                        <th>Total Scheduled (Hours)<a title="Explanation" class="flyout-link" data-flyout-type="scheduled-flyout" href="#">*</a>: </th>
                                        <td><fmt:formatNumber pattern="###,##0.0" value="${record.totalScheduledHours}"/></td>
                                    </tr>
                                    <tr>
                                        <th>Research / Scheduled (%)<a title="Explanation" class="flyout-link" data-flyout-type="research-availability-flyout" href="#">*</a>: </th>
                                        <td><fmt:formatNumber pattern="###,##0.0" value="${record.researchAvailability}"/>%</td>
                                    </tr>
                                    <tr>
                                        <th>Reliability (%)<a title="Explanation" class="flyout-link" data-flyout-type="reliability-flyout" href="#">*</a>: </th>
                                        <td><fmt:formatNumber pattern="###,##0.0" value="${record.actualAvailability}"/>%</td>
                                    </tr>
                                </tbody>
                                </c:when>
                                <c:otherwise>
                                    <thead>
                                    <tr>
                                        <th>Bin</th>
                                        <th>Research</th>
                                        <th>Studies</th>
                                        <th>Tuning &amp; Restore</th>
                                        <th>Delivered</th>
                                        <th>Budgeted</th>
                                        <th>Delivered Ratio</th>
                                        <th>Failures</th>
                                        <th>Scheduled</th>
                                        <th>Research Ratio</th>
                                        <th>Reliability</th>
                                    </tr>
                                    </thead>
                                    <tbody>
                                    <c:forEach items="${recordList}" var="record">
                                        <tr>
                                            <td data-date-utc="${dtm:getLocalTime(record.bin)}"><fmt:formatDate pattern="yyyy-MM-dd" value="${record.bin}"/></td>
                                            <td><fmt:formatNumber pattern="###,##0.0" value="${record.deliveredResearchHours}"/></td>
                                            <td><fmt:formatNumber pattern="###,##0.0" value="${record.deliveredBeamStudiesHours}"/></td>
                                            <td><fmt:formatNumber pattern="###,##0.0" value="${record.deliveredTuningAndRestoreHours}"/></td>
                                            <td><fmt:formatNumber pattern="###,##0.0" value="${record.totalDeliveredHours}"/></td>
                                            <td><fmt:formatNumber pattern="###,##0.0" value="${record.budgetedOperationsHours}"/></td>
                                            <td><fmt:formatNumber pattern="###,##0.0" value="${record.budgetedAvailability}"/>%</td>
                                            <td><fmt:formatNumber pattern="###,##0.0" value="${record.unscheduledFailuresHours}"/></td>
                                            <td><fmt:formatNumber pattern="###,##0.0" value="${record.totalScheduledHours}"/></td>
                                            <td><fmt:formatNumber pattern="###,##0.0" value="${record.researchAvailability}"/>%</td>
                                            <td><fmt:formatNumber pattern="###,##0.0" value="${record.actualAvailability}"/>%</td>
                                        </tr>
                                    </c:forEach>
                                </tbody>
                                </c:otherwise>
                            </c:choose>
                        </table>
                    </div>
                </c:otherwise>
            </c:choose>
        </section>
        <div id="exit-fullscreen-panel">
            <button id="exit-fullscreen-button">Exit Full Screen</button>
        </div>
        <div id="flyouts">
        <div class="delivered-research-flyout">
            <div class="flyout-panel">
                <button class="close-bubble">X</button>
                <div class="definition-bubble-title">Delivered Research</div>
                <div class="definition-bubble-body">
                    <p>Time in which the Accelerator division provided the machine to the Physics division minus any downtime events during this time and plus any quality adjustment.</p>
                    <p class="equation">Research = Physics Hours [BTM] + Quality - Physics Downtime</p>
                    <p class="equation">Quality = Bonus hours adjustment for program difficulty such as hall multiplicity</p>
                    <p class="equation">Physics Downtime = Event Down [DTM] - Internal Down [BTM]</p>
                    <p><b>Note:</b> Downtime from FSD Trips are not subtracted from research time.</p>
                    <p><b>Note:</b> We don't directly track downtime during Physics - we track downtime of each hall during Physics.  We also track total downtime regardless of machine program in two places: DTM downtime events (excludes trips) and BTM Down Hard (includes trips).  Finally we track downtime separately during internal machine mode (Accelerator mode instead of Physics mode).  We subtract Internal Down from Total Down to obtain Physics Down, which can then be subtracted from Physics to obtain Research hours.</p>
                </div>
            </div>
        </div>
            <div class="beam-studies-flyout">
                <div class="flyout-panel">
                    <button class="close-bubble">X</button>
                    <div class="definition-bubble-title">Delivered Beam Studies</div>
                    <div class="definition-bubble-body">
                        <p>Time in which the Accelerator division used the machine to study/test/develop it (formerly named machine development).</p>
                        <p><b>Note:</b> Recorded directly in BTM by Crew Chief on each shift.</p>
                        <p><b>Note:</b> Often we switch to an alternate program during an extended down, which may result in beam studies replacing down (or the NPES Long Term Schedule / Budget is retroactively updated)</p>
                    </div>
                </div>
            </div>
            <div class="tuning-restore-flyout">
                <div class="flyout-panel">
                    <button class="close-bubble">X</button>
                    <div class="definition-bubble-title">Delivered Tuning and Restore (T&amp;R)</div>
                    <div class="definition-bubble-body">
                        <p>Time in which the Accelerator division used the machine for initial setup and also program changes.  For example to configure the machine for a particular energy and or pass.</p>
                        <p class="equation">T&amp;R = ACC [BTM] + SAM Restore [BTM]</p>
                        <p><b>Note:</b> Recovery from downtime is included in downtime, not here.</p>
                        <p><b>Note:</b> T&amp;R is converted to downtime if it is excessive - if it takes longer than limits negotiated in the Budget (NPES Long Term Schedule) schedule.</p>
                    </div>
                </div>
            </div>
            <div class="total-delivered-flyout">
                <div class="flyout-panel">
                    <button class="close-bubble">X</button>
                    <div class="definition-bubble-title">Total Delivered</div>
                    <div class="definition-bubble-body">
                        <p>Time in which either the Accelerator division or the Physics division used the machine minus any event downtime.   The machine was "used" if the Machine Control Center (MCC) was staffed and the Crew Chief recorded anything other than SAM.</p>
                        <p class="equation">Total Delivered = Delivered Research + Delivered Beam Studies + Delivered T&amp;R</p>
                        <p><b>Note:</b> Downtime from FSD Trips are not subtracted from Program time.</p>
                        <p><b>Note:</b> Event Uptime = Total Delivered plus zero quality adjustments.</p>
                    </div>
                </div>
            </div>
            <div class="budgeted-flyout">
                <div class="flyout-panel">
                    <button class="close-bubble">X</button>
                    <div class="definition-bubble-title">Budgeted Operations</div>
                    <div class="definition-bubble-body">
                        <p>Number of Nuclear Physics Experiment Scheduling (NPES) scheduled days X 24 hours.   Scheduled days are days in which NPES schedule has any program other than OFF.</p>
                        <p><b>Note:</b> OFF is often called Scheduled Accelerator Maintenance (SAM).   Days which are unspecified (Implicit Off due to no timesheet) count as OFF.</p>
                        <p><b>Source:</b> <a href="${env['FRONTEND_SERVER_URL']}/btm/schedule">NPES Schedule</a></p>
                    </div>
                </div>
            </div>
            <div class="budgeted-availability-flyout">
                <div class="flyout-panel">
                    <button class="close-bubble">X</button>
                    <div class="definition-bubble-title">Total Delivered / Budgeted</div>
                    <div class="definition-bubble-body">
                        <p>Ratio of delivered beam vs NPES scheduled (budgeted).</p>
                        <p class="equation">Delivered Ratio =
                            <span class="fraction">
                        <span class="numerator">Total Delivered</span>
                        <span class="denominator">Budgeted Operations</span>
                        </span>
                            x 100
                        </p>
                    </div>
                </div>
            </div>
            <div class="unscheduled-failures-flyout">
                <div class="flyout-panel">
                    <button class="close-bubble">X</button>
                    <div class="definition-bubble-title">Unscheduled Failures</div>
                    <div class="definition-bubble-body">
                        <p>Unplanned downtime exceeding allowance of maintenance.</p>
                        <p class="equation">Unscheduled Failures = Event Downtime - Maintenance</p>
                        <p class="equation">Maintenance = Allowance of scheduled failures</p>
                        <p><b>Note:</b> Downtime from FSD Trips are not included in Event Downtime.</p>
                    </div>
                </div>
            </div>
            <div class="scheduled-flyout">
                <div class="flyout-panel">
                    <button class="close-bubble">X</button>
                    <div class="definition-bubble-title">Total Scheduled</div>
                    <div class="definition-bubble-body">
                        <p>Time machine was expected to be used.  Specifically the time the MCC was staffed and the Crew Chief recorded something other than SAM (Off).</p>
                        <p class="equation">Total Scheduled = Total Delivered + Unscheduled Failures</p>
                        <p><b>Note:</b> Program Time = Total Scheduled plus zero quality and maintenance adjustments.</p>
                        <p><b>Note:</b> Total Scheduled can be thought of as Crew Chief Scheduled (Realized Schedule) and is not to be confused with Budgeted (NPES Long Term Schedule), MCC Whiteboard Scheduled, or PD Scheduled (all four schedules may differ).</p>
                    </div>
                </div>
            </div>
            <div class="research-availability-flyout">
                <div class="flyout-panel">
                    <button class="close-bubble">X</button>
                    <div class="definition-bubble-title">Research / Scheduled</div>
                    <div class="definition-bubble-body">
                        <p>Machine availability ignoring beam studies and T&amp;R and excluding FSD trips and adjusted for maintenance and quality allowances.</p>
                        <p class="equation">Research Ratio =
                            <span class="fraction">
                        <span class="numerator">Delivered Research</span>
                        <span class="denominator">Total Scheduled</span>
                        </span>
                            x 100
                        </p>
                    </div>
                </div>
            </div>
            <div class="reliability-flyout">
                <div class="flyout-panel">
                    <button class="close-bubble">X</button>
                    <div class="definition-bubble-title">Reliability</div>
                    <div class="definition-bubble-body">
                        <p>Machine availability excluding FSD trips and adjusted for maintenance and quality allowances.</p>
                        <p class="equation">Reliability =
                        <span class="fraction">
                        <span class="numerator">Total Delivered</span>
                        <span class="denominator">Total Scheduled</span>
                        </span>
                        x 100
                        </p>
                        <p><b>Note:</b> Event Availability = Reliability plus zero quality and maintenance adjustments.</p>
                    </div>
                </div>
            </div>
        </div>
        <form id="csv-form" method="get" action="${pageContext.request.contextPath}/csv/joule.csv">
            <input type="hidden" name="start" value="${fn:escapeXml(param.start)}"/>
            <input type="hidden" name="end" value="${fn:escapeXml(param.end)}"/>
            <input type="hidden" name="maintenance" value="${fn:escapeXml(param.maintenance)}"/>
            <input type="hidden" name="quality" value="${fn:escapeXml(param.quality)}"/>
            <input type="hidden" name="size" value="${fn:escapeXml(param.size)}"/>
            <input type="hidden" name="qualified" value=""/>
            <button id="csv" type="submit" style="display: none;">CSV</button>
        </form>
    </jsp:body>
</t:operability-page>