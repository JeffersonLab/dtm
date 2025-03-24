<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@taglib prefix="s" uri="http://jlab.org/jsp/smoothness"%>
<%@taglib prefix="dtm" uri="http://jlab.org/dtm/functions"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%> 
<jsp:useBean id="now" class="java.util.Date"/>
<c:set var="title" value="${typeQualifier}Downtime Summary"/>
<t:reports-page title="${title}">  
    <jsp:attribute name="stylesheets">
        <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/resources/v${initParam.releaseNumber}/css/summary-table.css"/>
    </jsp:attribute>
    <jsp:attribute name="scripts">
        <script type="text/javascript">
            $(document).on("click", ".flyout-link", function () {
                $(".definition-flyout-handle").remove();
                var flyout = $("." + $(this).attr("data-flyout-type") + " .flyout-panel").clone();
                $(this).parent().append('<div class="definition-flyout-handle"></div>');
                $(".definition-flyout-handle").append(flyout);
                return false;
            });
            $(document).on("click", ".close-bubble", function () {
                $(".definition-flyout-handle").remove();
                return false;
            });
            $(function () {
                $("#fullscreen-button, #exit-fullscreen-button").button();
            });
        </script>
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
            <s:filter-flyout-widget  requiredMessage="true">
                <form class="filter-form" method="get" action="downtime-summary">
                    <div id="filter-form-panel">
                        <fieldset>
                            <legend>Time</legend>
                            <s:date-range required="${true}" datetime="${true}" sevenAmOffset="${true}"/>
                        </fieldset>
                        <fieldset>
                            <legend>Taxonomy</legend>
                            <ul class="key-value-list">                         
                                <li class="required-field">
                                    <div class="li-key">
                                        <label for="type">Type</label>
                                    </div>
                                    <div class="li-value">
                                        <select id="type" name="type">
                                            <option value="">&nbsp;</option>
                                            <c:forEach items="${eventTypeList}" var="type">
                                                <option value="${type.eventTypeId}"${(param.type eq type.eventTypeId) or (param.type eq null and type.eventTypeId eq 1) ? ' selected="selected"' : ''}><c:out value="${type.name}"/> (<c:out value="${type.abbreviation}"/>)</option>
                                            </c:forEach>
                                        </select>                                
                                    </div>
                                </li>
                                <li class="required-field" style="display: none;">
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
                            </ul>
                        </fieldset>
                    </div>
                    <input type="hidden" name="referrer" value="form"/>
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
                    <table class="data-table summary-table">
                        <thead>
                            <tr>
                                <th>Metric</th>
                                <th>Hours</th>
                            </tr>
                        </thead>
                        <tbody>
                            <tr class="major-row">
                                <th>Event Downtime<a title="Explanation" class="flyout-link" data-flyout-type="event-flyout" href="#">*</a>: </th>
                                    <c:url var="url" value="/reports/event-downtime">
                                        <c:param name="start" value="${param.start eq null ? sevenDaysAgoFmt : param.start}"/>
                                        <c:param name="end" value="${param.end eq null ? todayFmt : param.end}"/>
                                        <c:param name="type" value="${param.type eq null ? '1' : param.type}"/>
                                        <c:param name="transport" value=""/>
                                        <c:param name="data" value="downtime"/>
                                        <c:param name="chart" value="table"/>
                                        <c:param name="print" value="${param.print}"/>
                                        <c:param name="fullscreen" value="${param.fullscreen}"/>                                
                                        <c:param name="qualified" value=""/>
                                    </c:url>
                                <td><a href="${url}" title="Event Downtime Report"><fmt:formatNumber pattern="#,###,###.#" value="${eventDowntime}"/></a></td>
                            </tr>
                            <tr>
                                <th class="sub-total">Suspend<a title="Explanation" class="flyout-link" data-flyout-type="suspend-flyout" href="#">*</a>: </th>
                                <td><fmt:formatNumber pattern="#,###,###.#" value="${nonRestore}"/></td>
                            </tr>                             
                            <tr>
                                <th class="sub-total">Restore<a title="Explanation" class="flyout-link" data-flyout-type="restore-flyout" href="#">*</a>: </th>
                                <td><fmt:formatNumber pattern="#,###,###.#" value="${restore}"/></td>
                            </tr>
                            <tr>
                                <th>Event Count: </th>
                                <td><fmt:formatNumber value="${eventCount}"/></td>
                            </tr>                 
                            <tr>
                                <th>Event MTTR<a title="Explanation" class="flyout-link" data-flyout-type="event-mttr-flyout" href="#">*</a>: </th>
                                <td><fmt:formatNumber pattern="#,###,###.#" value="${eventMttr}"/></td>
                            </tr>
                            <tr class="major-row">
                                <th>Incident Downtime<a title="Explanation" class="flyout-link" data-flyout-type="incident-flyout" href="#">*</a>: </th>
                                    <c:url var="url" value="/reports/incident-downtime">
                                        <c:param name="start" value="${param.start eq null ? sevenDaysAgoFmt : param.start}"/>
                                        <c:param name="end" value="${param.end eq null ? todayFmt : param.end}"/>
                                        <c:param name="type" value="${param.type eq null ? '1' : param.type}"/>
                                        <c:param name="system" value=""/>
                                        <c:param name="component" value=""/>
                                        <c:param name="transport" value=""/>
                                        <c:param name="print" value="${param.print}"/>
                                        <c:param name="fullscreen" value="${param.fullscreen}"/>                                
                                        <c:param name="qualified" value=""/>
                                    </c:url>
                                <td><a href="${url}" title="Incident Downtime Report"><fmt:formatNumber pattern="#,###,###.#" value="${incidentDowntime}"/></a></td>
                            </tr>               
                            <tr>
                                <th>Incident Count: </th>
                                <td><fmt:formatNumber value="${incidentCount}"/></td>
                            </tr>   
                            <tr>
                                <th>Incident MTTR<a title="Explanation" class="flyout-link" data-flyout-type="incident-mttr-flyout" href="#">*</a>: </th>
                                <td><fmt:formatNumber pattern="#,###,###.#" value="${incidentMttr}"/></td>
                            </tr>
                            <tr>
                                <th>Intra-Category Non-Overlapping Incident Downtime<a title="Explanation" class="flyout-link" data-flyout-type="category-downtime-flyout" href="#">*</a>: </th>
                                    <c:url var="url" value="/reports/category-downtime">
                                        <c:param name="start" value="${param.start eq null ? sevenDaysAgoFmt : param.start}"/>
                                        <c:param name="end" value="${param.end eq null ? todayFmt : param.end}"/>
                                        <c:param name="type" value="${param.type eq null ? '1' : param.type}"/>
                                        <c:param name="transport" value=""/>
                                        <c:param name="data" value="downtime"/>
                                        <c:param name="chart" value="table"/>
                                        <c:param name="packed" value="Y"/>
                                        <c:param name="print" value="${param.print}"/>
                                        <c:param name="fullscreen" value="${param.fullscreen}"/>                                
                                        <c:param name="qualified" value=""/>
                                    </c:url>
                                <td><a href="${url}" title="Category Downtime Report"><fmt:formatNumber pattern="#,###,###.#" value="${categoryNonOverlappingDowntimeHours}"/></a></td>
                            </tr>
                            <tr>
                                <th>Intra-System Non-Overlapping Incident Downtime<a title="Explanation" class="flyout-link" data-flyout-type="system-downtime-flyout" href="#">*</a>: </th>
                                    <c:url var="url" value="/reports/system-downtime">
                                        <c:param name="start" value="${param.start eq null ? sevenDaysAgoFmt : param.start}"/>
                                        <c:param name="end" value="${param.end eq null ? todayFmt : param.end}"/>
                                        <c:param name="type" value="${param.type eq null ? '1' : param.type}"/>
                                        <c:param name="category" value=""/>
                                        <c:param name="transport" value=""/>
                                        <c:param name="data" value="downtime"/>
                                        <c:param name="chart" value="table"/>
                                        <c:param name="packed" value="Y"/>
                                        <c:param name="print" value="${param.print}"/>
                                        <c:param name="fullscreen" value="${param.fullscreen}"/>
                                        <c:param name="qualified" value=""/>
                                    </c:url>
                                <td><a href="${url}" title="System Downtime Report"><fmt:formatNumber pattern="#,###,###.#" value="${systemNonOverlappingDowntimeHours}"/></a></td>
                            </tr>    
                            <tr class="major-row">
                                <th>Trip Downtime<a title="Explanation" class="flyout-link" data-flyout-type="trip-flyout" href="#">*</a>: </th>
                                    <c:url var="url" value="/reports/fsd-summary">
                                        <c:param name="start" value="${param.start}"/>
                                        <c:param name="end" value="${param.end}"/>
                                        <c:param name="maxDuration" value="5"/>
                                        <c:param name="maxDurationUnits" value="Minutes"/>
                                        <c:param name="chart" value="bar"/>
                                        <c:param name="binSize" value="DAY"/>
                                        <c:param name="maxY" value="17"/>                                
                                        <c:param name="grouping" value="category"/>
                                        <c:param name="maxTypes" value=""/>
                                        <c:param name="sadTrips" value="N"/>
                                        <c:param name="rateBasis" value="program"/>
                                        <c:param name="print" value="${param.print}"/>
                                        <c:param name="fullscreen" value="${param.fullscreen}"/>
                                        <c:param name="qualified" value=""/>
                                    </c:url>
                                <td><a href="${url}" title="FSD Summary Report"><fmt:formatNumber pattern="#,###,##0.0" value="${fsdSummary.hours}"/></a></td>
                            </tr>
                            <tr>
                                <th>Trip Count: </th>
                                <td><fmt:formatNumber pattern="#,###,##0" value="${fsdSummary.count}"/></td>
                            </tr> 
                            <tr>
                                <th>Trip MTTR (Minutes)<a title="Explanation" class="flyout-link" data-flyout-type="trip-mttr-flyout" href="#">*</a>: </th>
                                <td>(<fmt:formatNumber pattern="#,###,##0.#" value="${tripMttr}"/>)</td>
                            </tr>                           
                            <c:if test="${type.eventTypeId eq 1}">
                                <tr class="major-row">
                                    <th>Accelerator Program Time<a title="Explanation" class="flyout-link" data-flyout-type="program-flyout" href="#">*</a>: </th>
                                        <c:url var="url" value="/reports/beam-time-summary" context="/btm">
                                            <c:param name="start" value="${param.start eq null ? sevenDaysAgoFmt : param.start}"/>
                                            <c:param name="end" value="${param.end eq null ? todayFmt : param.end}"/>
                                            <c:param name="print" value="${param.print}"/>
                                            <c:param name="fullscreen" value="${param.fullscreen}"/>  
                                        </c:url>
                                    <td><a target="_blank" href="${url}" title="BTM Report"><fmt:formatNumber pattern="#,###,###.#" value="${programHours}"/></a></td>
                                </tr>
                                <tr>
                                    <th>Period Duration: </th>
                                    <td><fmt:formatNumber value="${periodDuration}"/></td>
                                </tr> 
                                <tr>
                                    <th>Accelerator Downtime:<a title="Explanation" class="flyout-link" data-flyout-type="machine-downtime-flyout" href="#">*</a>: </th>
                                    <td><fmt:formatNumber value="${accDownHours}" pattern="#,###,###.#"/></td>
                                </tr>        
                                <tr>
                                    <th>Accelerator MTTR (Minutes):<a title="Explanation" class="flyout-link" data-flyout-type="acc-mttr-flyout" href="#">*</a>: </th>
                                    <td>(<fmt:formatNumber value="${accMttr}" pattern="#,###,###.#"/>)</td>
                                </tr>
                                <tr class="major-row">
                                    <th>Accelerator Uptime<a title="Explanation" class="flyout-link" data-flyout-type="uptime-flyout" href="#">*</a>: </th>
                                    <td><fmt:formatNumber pattern="#,###,###.#" value="${periodUptimeHours}"/></td>
                                </tr>
                                <tr>
                                    <th>Event Uptime<a title="Explanation" class="flyout-link" data-flyout-type="event-uptime-flyout" href="#">*</a>: </th>
                                    <td><fmt:formatNumber pattern="#,###,###.#" value="${eventUptimeHours}"/></td>
                                </tr>       
                                <tr>
                                    <th>Trip Uptime<a title="Explanation" class="flyout-link" data-flyout-type="trip-uptime-flyout" href="#">*</a>: </th>
                                    <td><fmt:formatNumber pattern="#,###,###.#" value="${tripUptimeHours}"/></td>
                                </tr>
                                <tr class="major-row">
                                    <th>Accelerator MTBF (Minutes)<a title="Explanation" class="flyout-link" data-flyout-type="mtbf-flyout" href="#">*</a>: </th>
                                    <td>(<fmt:formatNumber pattern="#,###,###.#" value="${accMtbf}"/><c:out value="${accMtbf == null ? 'N/A' : ''}"/>)</td>
                                </tr>
                                <tr>
                                    <th>Event MTBF<a title="Explanation" class="flyout-link" data-flyout-type="mtbe-flyout" href="#">*</a>: </th>
                                    <td><fmt:formatNumber pattern="#,###,###.#" value="${eventMtbf}"/><c:out value="${eventMtbf == null ? 'N/A' : ''}"/></td>
                                </tr>                                
                                <tr>
                                    <th>Trip MTBF (Minutes)<a title="Explanation" class="flyout-link" data-flyout-type="mtbt-flyout" href="#">*</a>: </th>
                                    <td class="right-aligned">(<fmt:formatNumber pattern="#,###,##0.#" value="${tripMtbf}"/><c:out value="${tripMtbf == null ? 'N/A' : ''}"/>)</td> 
                                </tr>
                                <tr class="major-row">
                                    <th>Accelerator Hourly Failure Rate<a title="Explanation" class="flyout-link" data-flyout-type="failure-rate-flyout" href="#">*</a>: </th>
                                    <td><fmt:formatNumber pattern="#,###,###.###" value="${accFailureRate}"/><c:out value="${accFailureRate == null ? 'N/A' : ''}"/></td>
                                </tr> 
                                <tr>
                                    <th>Hourly Event Rate<a title="Explanation" class="flyout-link" data-flyout-type="event-rate-flyout" href="#">*</a>: </th>
                                    <td><fmt:formatNumber pattern="#,###,###.###" value="${eventFailureRate}"/><c:out value="${eventFailureRate == null ? 'N/A' : ''}"/></td>
                                </tr>                                
                                <tr>
                                    <th>Hourly Trip Rate<a title="Explanation" class="flyout-link" data-flyout-type="trip-rate-flyout" href="#">*</a>: </th>
                                    <td><fmt:formatNumber pattern="#,###,###.###" value="${tripFailureRate}"/><c:out value="${tripFailureRate == null ? 'N/A' : ''}"/></td>
                                </tr>                                 
                                <tr class="major-row">
                                    <th>Accelerator Availability<a title="Explanation" class="flyout-link" data-flyout-type="availability-flyout" href="#">*</a>: </th>
                                    <td><fmt:formatNumber pattern="#,###,###.#" value="${accAvailability}"/>%</td>
                                </tr> 
                                <tr>
                                    <th>Loss Due to Events<a title="Explanation" class="flyout-link" data-flyout-type="event-loss-flyout" href="#">*</a>: </th>
                                        <c:url var="url" value="/reports/category-downtime">
                                            <c:param name="start" value="${param.start eq null ? sevenDaysAgoFmt : param.start}"/>
                                            <c:param name="end" value="${param.end eq null ? todayFmt : param.end}"/>
                                            <c:param name="print" value="${param.print}"/>
                                            <c:param name="fullscreen" value="${param.fullscreen}"/>
                                            <c:param name="chart" value="table"/>
                                            <c:param name="type" value="1"/>
                                            <c:param name="data" value="downtime"/>
                                            <c:param name="packed" value="Y"/>
                                            <c:param name="qualified" value=""/>
                                        </c:url>
                                    <td><a target="_blank" href="${url}" title="Event Category Breakdown"><fmt:formatNumber pattern="#,###,###.#" value="${eventAvailabilityLoss}"/>%</a></td>
                                </tr>  
                                <tr>
                                    <th>Loss Due to Trips<a title="Explanation" class="flyout-link" data-flyout-type="trip-loss-flyout" href="#">*</a>: </th>
                                    <c:url var="url" value="/reports/fsd-summary">
                                        <c:param name="start" value="${param.start}"/>
                                        <c:param name="end" value="${param.end}"/>
                                        <c:param name="maxDuration" value="5"/>
                                        <c:param name="maxDurationUnits" value="Minutes"/>
                                        <c:param name="chart" value="bar"/>
                                        <c:param name="binSize" value="DAY"/>
                                        <c:param name="maxY" value="17"/>                                
                                        <c:param name="grouping" value="category"/>
                                        <c:param name="maxTypes" value=""/>
                                        <c:param name="sadTrips" value="N"/>
                                        <c:param name="rateBasis" value="program"/>
                                        <c:param name="print" value="${param.print}"/>
                                        <c:param name="fullscreen" value="${param.fullscreen}"/>
                                        <c:param name="legendData" value="lost"/>
                                        <c:param name="qualified" value=""/>
                                        </c:url>
                                    <td><a target="_blank" href="${url}" title="Trip Cause Breakdown"><fmt:formatNumber pattern="#,###,###.#" value="${tripAvailabilityLoss}"/>%</a></td>
                                </tr>                                  
                                <tr>
                                    <th>Event Availability<a title="Explanation" class="flyout-link" data-flyout-type="event-availability-flyout" href="#">*</a>: </th>
                                    <td><fmt:formatNumber pattern="#,###,###.#" value="${eventAvailability}"/>%</td>
                                </tr>                                 
                                <tr>
                                    <th>Trip Availability<a title="Explanation" class="flyout-link" data-flyout-type="trip-availability-flyout" href="#">*</a>: </th>
                                    <td><fmt:formatNumber pattern="#,###,###.#" value="${tripAvailability}"/>%</td>
                                </tr> 
                            </c:if>
                        </tbody>
                    </table>
                    <div id="unofficial-footnote">Note: These numbers are not official.  The data shown may not have been audited / reviewed.</div>
                </c:otherwise>
            </c:choose>
        </section>
        <div id="flyouts">
            <div class="mtbf-flyout">
                <div class="flyout-panel">
                    <button class="close-bubble">X</button>
                    <div class="definition-bubble-title">Accelerator Mean Time Between Failures (MTBF)</div>
                    <div class="definition-bubble-body">
                        <p>The average time between accelerator downtime events or trips.</p>
                        <p class="equation">MTBF = <span class="fraction"><span class="numerator">Uptime</span><span class="denominator">Event Count + Trip Count</span></span></p>
                        <p>Note: Hall downtimes (hall-owned equipment failures, procedures) are excluded.  This is one way to measure reliability.</p>
                    </div>
                </div>
            </div>
            <div class="mtbe-flyout">
                <div class="flyout-panel">
                    <button class="close-bubble">X</button>
                    <div class="definition-bubble-title">Event Mean Time Between Failures (MTBF)</div>
                    <div class="definition-bubble-body">
                        <p>The average time between downtime events.</p>
                        <p class="equation">Event MTBF = <span class="fraction"><span class="numerator">Event Uptime</span><span class="denominator">Event Count</span></span></p>
                        <p>Alias: Mean Time between Events (MTBE)</p>
                    </div>
                </div>
            </div>    
            <div class="mtbt-flyout">
                <div class="flyout-panel">
                    <button class="close-bubble">X</button>
                    <div class="definition-bubble-title">Trip Mean Time Between Failures (MTBF)</div>
                    <div class="definition-bubble-body">
                        <p>The average time between trips.</p>
                        <p class="equation">Trip MTBF = <span class="fraction"><span class="numerator">Trip Uptime</span><span class="denominator">Trip Count</span></span></p>
                        <p>Alias: Mean Time between Trips (MTBT)</p>
                    </div>
                </div>
            </div>
            <div class="failure-rate-flyout">
                <div class="flyout-panel">
                    <button class="close-bubble">X</button>
                    <div class="definition-bubble-title">Accelerator Hourly Failure Rate</div>
                    <div class="definition-bubble-body">
                        <p>The hourly failure rate.</p>
                        <p class="equation">Hourly Failure Rate = <span class="fraction"><span class="numerator">Event Count + Trip Count</span><span class="denominator">Uptime</span></span></p>
                        <p>Note: Hall downtimes (hall-owned equipment failures, procedures) are excluded.  This is one way to measure reliability.</p>
                    </div>
                </div>
            </div>            
            <div class="event-flyout">
                <div class="flyout-panel">
                    <button class="close-bubble">X</button>
                    <div class="definition-bubble-title">Event Downtime</div>
                    <div class="definition-bubble-body">
                        <p>A disruption caused by one or more component failures known as incidents (machine operation is suspended).  An event may also include restore time needed to restart the machine.  Events are tracked independently for each hall and the accelerator (event type).</p>
                        <p>Events of the same type cannot overlap, but overlapping incidents are allowed. For example: A hall can only experience one downtime event at a time, but the event may be due to multiple simultaneous component failures (incidents).</p>
                    </div>
                </div>
            </div>
            <div class="restore-flyout">
                <div class="flyout-panel">
                    <button class="close-bubble">X</button>
                    <div class="definition-bubble-title">Restore</div>
                    <div class="definition-bubble-body">
                        <span>The part of an event downtime in which the machine is being brought back up to operation after being suspended.</span>
                        <p>Suspend + Restore = Event Downtime</p>
                        <p>Note: This may be called recovery to avoid confusion with SAD Restore, a separate metric.</p>
                    </div>
                </div>
            </div>
            <div class="suspend-flyout">
                <div class="flyout-panel">
                    <button class="close-bubble">X</button>
                    <div class="definition-bubble-title">Suspend</div>
                    <div class="definition-bubble-body">
                        <span>The part of an event downtime in which one or more incidents are ongoing.</span>
                        <p>Suspend + Restore = Event Downtime</p>
                        <p>Alias: Non-Overlapping Incident Downtime</p>
                    </div>
                </div>
            </div>
            <div class="incident-flyout">
                <div class="flyout-panel">
                    <button class="close-bubble">X</button>
                    <div class="definition-bubble-title">Incident Downtime</div>
                    <div class="definition-bubble-body">
                        <span>An incident captures a single component failure.  Multiple incidents may occur simultaneously and make up a single event.</span>
                        <p>Note: Incident Downtime &#8805; Suspend</p>
                        <p>Aliases: Repair Time, System Repair Time, Component Repair Time</p>
                        <p>Note: The word "Downtime" is used so much that it requires qualification to be meaningful.  Some of the different meanings partially overlap, but are ultimately unique.  Downtime can mean:</p>
                        <ol>
                            <li>Time a component is down (an incident)</li>
                            <li>Time a series of possibly overlapping incidents cause down (an event, which ends once the machine is restored)</li>
                            <li>Time a system of components are down (can optionally be calculated as non-overlapping incidents of same system)</li>
                            <li>Time a category of components are down (can optionally be calculated as non-overlapping incidents of same category)</li>
                            <li>Time a hall is down (hall down during physics, not necessarily blocking other halls)</li>
                            <li>Time the accelerator is down while NOT doing physics (SRA Down)</li>
                            <li>Time the accelerator is down preventing any physics (Down Hard)</li>
                        </ol>
                    </div>
                </div>
            </div>
            <div class="acc-mttr-flyout">
                <div class="flyout-panel">
                    <button class="close-bubble">X</button>
                    <div class="definition-bubble-title">Accelerator Mean Time To Recover (MTTR)</div>
                    <div class="definition-bubble-body">
                        <p>The average failure duration (events and trips).</p>
                        <p class="equation">MTTR = <span class="fraction"><span class="numerator">Downtime</span><span class="denominator">Event Count + Trip Count</span></span></p>
                    </div>
                </div>
            </div>
            <div class="event-mttr-flyout">
                <div class="flyout-panel">
                    <button class="close-bubble">X</button>
                    <div class="definition-bubble-title">Event Mean Time To Recover (MTTR)</div>
                    <div class="definition-bubble-body">
                        <p>The average event duration.</p>
                        <p class="equation">Event MTTR = <span class="fraction"><span class="numerator">Event Downtime</span><span class="denominator">Event Count</span></span></p>
                    </div>
                </div>
            </div>
            <div class="trip-mttr-flyout">
                <div class="flyout-panel">
                    <button class="close-bubble">X</button>
                    <div class="definition-bubble-title">Trip Mean Time To Recover (MTTR)</div>
                    <div class="definition-bubble-body">
                        <p>The average trip duration.</p>
                        <p class="equation">Trip MTTR = <span class="fraction"><span class="numerator">Trip Downtime</span><span class="denominator">Trip Count</span></span></p>
                    </div>
                </div>
            </div>            
            <div class="incident-mttr-flyout">
                <div class="flyout-panel">
                    <button class="close-bubble">X</button>
                    <div class="definition-bubble-title">Incident Mean Time To Recover (MTTR)</div>
                    <div class="definition-bubble-body">
                        <p>The average incident duration.</p>
                        <p class="equation">Incident MTTR = <span class="fraction"><span class="numerator">Incident Downtime</span><span class="denominator">Incident Count</span></span></p>
                    </div>
                </div>
            </div>
            <div class="availability-flyout">
                <div class="flyout-panel">
                    <button class="close-bubble">X</button>
                    <div class="definition-bubble-title">Accelerator Availability</div>
                    <div class="definition-bubble-body">
                        <p>The percent of time the machine is up (operational).</p>
                        <p class="equation">Availability = <span class="fraction"><span class="numerator">Uptime</span><span class="denominator">Program Time</span></span> x 100</p>
                        <p>Note: This metric does not take into account hall multiplicity and is not the same as the official DOE accelerator availability metric; Read the definitions of Accelerator Uptime and Accelerator Program Time carefully to understand the precise meaning of this metric.<p>
                    </div>
                </div>
            </div>
            <div class="event-availability-flyout">
                <div class="flyout-panel">
                    <button class="close-bubble">X</button>
                    <div class="definition-bubble-title">Event Availability</div>
                    <div class="definition-bubble-body">
                        <p>The percent of time the machine is up (operational) only considering events (ignore trips).</p>
                        <p class="equation">Event Availability = <span class="fraction"><span class="numerator">Event Uptime</span><span class="denominator">Program Time</span></span> x 100</p>
                    </div>
                </div>
            </div>
            <div class="trip-availability-flyout">
                <div class="flyout-panel">
                    <button class="close-bubble">X</button>
                    <div class="definition-bubble-title">Trip Availability</div>
                    <div class="definition-bubble-body">
                        <p>The percent of time the machine is up (operational) only considering trips (ignore events).</p>
                        <p class="equation">Trip Availability = <span class="fraction"><span class="numerator">Trip Uptime</span><span class="denominator">Program Time</span></span> x 100</p>
                    </div>
                </div>
            </div>
            <div class="uptime-flyout">
                <div class="flyout-panel">
                    <button class="close-bubble">X</button>
                    <div class="definition-bubble-title">Accelerator Uptime</div>
                    <div class="definition-bubble-body">
                        <p>The amount of time the machine is up (operational).</p>
                        <p class="equation">Uptime = Program Time - Downtime</p>
                        <p>Note: A downtime is the time in which program requirements are not being satisfied.  Accelerator Downtime is Event Downtime + Trip Downtime.</p>
                        <p>Note: Often the program will be adapted to problems and downtimes are shortened because the program changes.  As a side effect, component failure durations are sometimes under-measured in the downtime software (tracking component failures vs actual machine downtime are sometimes competing goals).  Further, a negative uptime or incorrect uptime may occur if downtimes are erroneously recorded.  This can also happen if program time is recorded incorrectly.</p>
                        <p>Note: Restore (after component failure) and tuning (Beam Transport) are included as part of downtime by this definition (due to use of event downtime).</p>
                    </div>
                </div>
            </div>
            <div class="event-uptime-flyout">
                <div class="flyout-panel">
                    <button class="close-bubble">X</button>
                    <div class="definition-bubble-title">Event Uptime</div>
                    <div class="definition-bubble-body">
                        <p>The amount of time the machine is up (operational) only considering events (ignore trips).</p>
                        <p class="equation">Uptime = Program Time - Event Downtime</p>
                    </div>
                </div>
            </div>  
            <div class="trip-uptime-flyout">
                <div class="flyout-panel">
                    <button class="close-bubble">X</button>
                    <div class="definition-bubble-title">Trip Uptime</div>
                    <div class="definition-bubble-body">
                        <p>The amount of time the machine is up (operational) only considering trips (ignore events).</p>
                        <p class="equation">Uptime = Program Time - Trip Downtime</p>
                    </div>
                </div>
            </div>            
            <div class="machine-downtime-flyout">
                <div class="flyout-panel">
                    <button class="close-bubble">X</button>
                    <div class="definition-bubble-title">Accelerator Downtime</div>
                    <div class="definition-bubble-body">
                        <p>The amount of time the machine is down (not operational) due to a component failure or trip.</p>
                        <p class="equation">Downtime = Event Down + Trip Down</p>
                        <p>Note: The max downtime is equal to the period duration.</p>
                    </div>
                </div>
            </div>            
            <div class="program-flyout">
                <div class="flyout-panel">
                    <button class="close-bubble">X</button>
                    <div class="definition-bubble-title">Accelerator Program Time</div>
                    <div class="definition-bubble-body">
                        <p>The amount of time the machine is being used for any program.  This information is obtained from the Crew Chief timesheet in the Beam Time Manager (BTM).  Any time other than OFF (SAD) and Implicit Off (no timesheet) are treated as program time.</p>
                        <p class="equation">Program Time = PHYSICS + STUDIES + RESTORE + ACC + DOWN</p>
                        <p>Note: BTM (scheduled) Restore is restoration after a SAD and differs from DTM (unscheduled) Restore, which is restoration after a DOWN.  Also, BTM SRA Down is a subset of DTM downtime and only represents downtimes when not doing PHYSICS.</p>
                    </div>
                </div>
            </div>       
            <div class="category-downtime-flyout">
                <div class="flyout-panel">
                    <button class="close-bubble">X</button>
                    <div class="definition-bubble-title">Intra-Category Non-Overlapping Incident Downtime</div>
                    <div class="definition-bubble-body">
                        <p>The amount of downtime in which overlapping incidents of the same category are merged/packed as to avoid over counting the overall category downtime.  This is easier to explain with a picture, so observe the diagram below.  If all three incidents have the same category then the non-overlapping downtime is 4 hours.   If the incidents have different categories or we are simply looking at total incident downtime then we have 6 hours.</p>
                        <p class="explanation-image"><img width="200" height="200" alt="Overlap Diagram" src="${pageContext.request.contextPath}/resources/v${initParam.releaseNumber}/img/overlap-diagram.png"/></p>
                        <p>Note: Although incidents of the same category do not overlap, incidents of different categories still can.  Therefore:</p>
                        <p class="equation">Category Non-Overlapping &#8805; Non-Overlapping Incident</p>
                        <p>where Non-Overlapping Incident Downtime is also known as Suspend.</p>
                    </div>
                </div>
            </div> 
            <div class="system-downtime-flyout">
                <div class="flyout-panel">
                    <button class="close-bubble">X</button>
                    <div class="definition-bubble-title">Intra-System Non-Overlapping Incident Downtime</div>
                    <div class="definition-bubble-body">
                        <p>The amount of downtime in which overlapping incidents of the same system are merged/packed as to avoid over counting the overall system downtime.  This is easier to explain with a picture, so observe the diagram below.  If all three incidents have the same system then the non-overlapping downtime is 4 hours.   If the incidents have different systems or we are simply looking at total incident downtime then we have 6 hours.</p>
                        <p class="explanation-image"><img width="200" height="200" alt="Overlap Diagram" src="${pageContext.request.contextPath}/resources/v${initParam.releaseNumber}/img/overlap-diagram.png"/></p>
                        <p>Note: Although incidents of the same system do not overlap, incidents of different systems still can.  Therefore:</p>
                        <p class="equation">System Non-Overlapping &#8805; Non-Overlapping Incident</p>
                        <p>where Non-Overlapping Incident Downtime is also known as Suspend.</p>
                        <p>Since categories generally contain multiple systems the chance of overlap is greater within a category than within a single system.  Therefore:</p>
                        <p class="equation">System Non-Overlapping &#8805; Category Non-Overlapping</p>
                    </div>
                </div>
            </div>             
            <div class="trip-flyout">
                <div class="flyout-panel">
                    <button class="close-bubble">X</button>
                    <div class="definition-bubble-title">Trip Downtime</div>
                    <div class="definition-bubble-body">
                        <p>The amount of downtime due to FSD trips lasting no more than 5 minutes.  An FSD trip lasting more than 5 minutes is expected to be recorded as an incident downtime (component failure).</p>
                        <p>Trips that are recorded while in BOOM SAD (machine OFF) state are also ignored.</p>
                    </div>
                </div>
            </div>
            <div class="event-rate-flyout">
                <div class="flyout-panel">
                    <button class="close-bubble">X</button>
                    <div class="definition-bubble-title">Hourly Event Rate</div>
                    <div class="definition-bubble-body">
                        <p>The hourly event failure rate.</p>
                        <p class="equation">Hourly Event Rate = <span class="fraction"><span class="numerator">Event Count</span><span class="denominator">Event Uptime</span></span></p>
                    </div>
                </div>
            </div>                        
            <div class="trip-rate-flyout">
                <div class="flyout-panel">
                    <button class="close-bubble">X</button>
                    <div class="definition-bubble-title">Hourly Trip Rate</div>
                    <div class="definition-bubble-body">
                        <p>The hourly trip rate.</p>
                        <p class="equation">Hourly Trip Rate = <span class="fraction"><span class="numerator">Trip Count</span><span class="denominator">Trip Uptime</span></span></p>
                    </div>
                </div>
            </div>
            <div class="event-loss-flyout">
                <div class="flyout-panel">
                    <button class="close-bubble">X</button>
                    <div class="definition-bubble-title">Availability Lost to Events</div>
                    <div class="definition-bubble-body">
                        <p>The percent of the accelerator availability that was lost to events.</p>
                        <p class="equation">Event Loss = <span class="fraction"><span class="numerator">Event Downtime</span><span class="denominator">Program Time</span></span> x 100</p>
                    </div>
                </div>
            </div>
            <div class="trip-loss-flyout">
                <div class="flyout-panel">
                    <button class="close-bubble">X</button>
                    <div class="definition-bubble-title">Availability Lost to Trips</div>
                    <div class="definition-bubble-body">
                        <p>The percent of the accelerator availability that was lost to trips.</p>
                        <p class="equation">Trip Loss = <span class="fraction"><span class="numerator">Trip Downtime</span><span class="denominator">Program Time</span></span> x 100</p>
                    </div>
                </div>
            </div>
        </div>
        <div id="exit-fullscreen-panel">
            <button id="exit-fullscreen-button">Exit Full Screen</button>
        </div>            
    </jsp:body>         
</t:reports-page>