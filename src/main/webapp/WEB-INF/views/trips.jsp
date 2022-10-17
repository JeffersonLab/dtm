<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@taglib prefix="s" uri="http://jlab.org/jsp/smoothness"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%> 
<%@taglib prefix="dtm" uri="http://jlab.org/dtm/functions"%>
<c:set value="FSD Trips" var="title"/>
<t:page title="${title}">  
    <jsp:attribute name="stylesheets"> 
        <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/resources/v${initParam.releaseNumber}/css/event-list.css"/>
        <style type="text/css">
            .state-panel {
                border: 1px dashed black;
                padding: 0.5em;
            }
            .state-panel > ul {
                margin: 0;
            }
            .fault-id-header {
                width: 150px;
            }
            #filter-flyout-panel {
                width: 650px;
            }
            /*#accordion .event-header {
                height: 73px;
            }*/
            #export-menu {
                width: 135px;
            }
            .area-span {
                float: right;
            }
        </style>
    </jsp:attribute>
    <jsp:attribute name="scripts">      
        <script type="text/javascript" src="${pageContext.request.contextPath}/resources/v${initParam.releaseNumber}/js/trips.js"></script>
    </jsp:attribute>        
    <jsp:body>       
        <section>
            <div id="report-page-actions">                
                <div id="export-widget">
                    <button id="export-menu-button">Export</button>
                    <ul id="export-menu">
                        <li id="csv-agg-menu-item">CSV</li>
                        <li id="csv-exp-menu-item">CSV (Exploded)</li>  
                    </ul>
                </div>
            </div> 
            <s:filter-flyout-widget ribbon="true" resetButton="true">
                <form id="filter-form" method="get" action="trips">
                    <div id="filter-form-panel" class="scrollable-filter-form">
                        <fieldset>
                            <legend>Time</legend>
                            <s:date-range datetime="${true}" sevenAmOffset="${true}"/>
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
                                <li>
                                    <div class="li-key">
                                        <label for="minDuration" title="Inclusive">Min Trip Duration</label>
                                    </div>
                                    <div class="li-value">
                                        <input type="number" min="1" id="minDuration" name="minDuration" value="${fn:escapeXml(param.minDuration)}"/>
                                        <select id="minDurationUnits" name="minDurationUnits">
                                            <option value="Seconds"${(param.minDurationUnits eq 'Seconds') ? ' selected="selected"' : ''}>Seconds</option>
                                            <option value="Minutes"${(param.minDurationUnits eq 'Minutes') ? ' selected="selected"' : ''}>Minutes</option>
                                            <option value="Hours"${(param.minDurationUnits eq 'Hours') ? ' selected="selected"' : ''}>Hours</option>
                                        </select>
                                    </div>
                                </li>
                            </ul>
                        </fieldset>
                        <fieldset>
                            <legend>Machine State</legend>
                            <ul class="key-value-list">                       
                                <li>
                                    <div class="li-key">
                                        <label for="accState">Accelerator</label>
                                    </div>
                                    <div class="li-value">
                                        <select id="accState" name="accState" multiple="multiple">
                                            <c:forEach items="${accMachineStateArray}" var="state">
                                                <option value="${state.name()}"${s:inArray(paramValues.accState, state.name()) ? ' selected="selected"' : ''}><c:out value="${state.label}"/></option>
                                            </c:forEach>
                                        </select>                                
                                    </div>
                                </li>                      
                                <li>
                                    <div class="li-key">
                                        <label for="hallAState">Hall A</label>
                                    </div>
                                    <div class="li-value">
                                        <select id="hallAState" name="hallAState" multiple="multiple">
                                            <c:forEach items="${hallMachineStateArray}" var="state">
                                                <option value="${state.name()}" ${s:inArray(paramValues.hallAState, state.name()) ? 'selected="selected"' : ''}><c:out value="${state.label}"/></option>
                                            </c:forEach>
                                        </select>
                                    </div>
                                </li>
                                <li>
                                    <div class="li-key">
                                        <label for="hallBState">Hall B</label>
                                    </div>
                                    <div class="li-value">
                                        <select id="hallBState" name="hallBState" multiple="multiple">
                                            <c:forEach items="${hallMachineStateArray}" var="state">
                                                <option value="${state.name()}" ${s:inArray(paramValues.hallBState, state.name()) ? 'selected="selected"' : ''}><c:out value="${state.label}"/></option>
                                            </c:forEach>
                                        </select>
                                    </div>
                                </li>
                                <li>
                                    <div class="li-key">
                                        <label for="hallCState">Hall C</label>
                                    </div>
                                    <div class="li-value">
                                        <select id="hallCState" name="hallCState" multiple="multiple">
                                            <c:forEach items="${hallMachineStateArray}" var="state">
                                                <option value="${state.name()}" ${s:inArray(paramValues.hallCState, state.name()) ? 'selected="selected"' : ''}><c:out value="${state.label}"/></option>
                                            </c:forEach>
                                        </select>
                                    </div>
                                </li>
                                <li>
                                    <div class="li-key">
                                        <label for="hallDState">Hall D</label>
                                    </div>
                                    <div class="li-value">
                                        <select id="hallDState" name="hallDState" multiple="multiple">
                                            <c:forEach items="${hallMachineStateArray}" var="state">
                                                <option value="${state.name()}" ${s:inArray(paramValues.hallDState, state.name()) ? 'selected="selected"' : ''}><c:out value="${state.label}"/></option>
                                            </c:forEach>
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
                                        <label for="cause">Cause</label>
                                    </div>
                                    <div class="li-value">
                                        <input type="text" id="cause" name="cause" value="${fn:escapeXml(param.cause)}"/>
                                        (use % as wildcard)
                                    </div>
                                </li>
                                <li>
                                    <div class="li-key">
                                        <label for="area">Area</label>
                                    </div>
                                    <div class="li-value">
                                        <input type="text" id="area" name="area" value="${fn:escapeXml(param.area)}"/>
                                        (use % as wildcard)
                                    </div>
                                </li>
                                <li>
                                    <div class="li-key">
                                        <label for="system">System</label>
                                    </div>
                                    <div class="li-value">
                                        <input type="text" id="system" name="system" value="${fn:escapeXml(param.system)}"/>
                                        (use % as wildcard)
                                    </div>
                                </li>
                                <li>
                                    <div class="li-key">
                                        <label for="cedType">CED Type</label>
                                    </div>
                                    <div class="li-value">
                                        <input type="text" id="cedType" name="cedType" value="${fn:escapeXml(param.cedType)}"/>
                                        (use % as wildcard)
                                    </div>
                                </li>                                            
                                <li>
                                    <div class="li-key">
                                        <label for="cedName">CED Name</label>
                                    </div>
                                    <div class="li-value">
                                        <input type="text" id="cedName" name="cedName" value="${fn:escapeXml(param.cedName)}"/>
                                        (use % as wildcard)
                                    </div>
                                </li>
                                <li style="display: none;">
                                    <div class="li-key">
                                        <label for="exceptionType">Fault Type</label>
                                    </div>
                                    <div class="li-value">
                                        <select id="exceptionType" name="exceptionType">
                                            <option value=""> </option>
                                            <option value="Standard"${param.exceptionType eq 'Standard' ? ' selected="selected"' : ''}>Standard</option>
                                            <option value="Phantom"${param.exceptionType eq 'Phantom' ? ' selected="selected"' : ''}>Phantom</option>
                                            <option value="Ambiguous"${param.exceptionType eq 'Ambiguous' ? ' selected="selected"' : ''}>Ambiguous</option>
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
                                <li>
                                    <div class="li-key">
                                        <label for="maxDevices" title="Inclusive">Max Devices Per Trip</label>
                                    </div>
                                    <div class="li-value">
                                        <input type="number" min="1" id="maxDevices" name="maxDevices" value="${fn:escapeXml(param.maxDevices)}"/>
                                    </div>
                                </li>                                
                            </ul>
                        </fieldset>
                        <fieldset>
                            <legend>Faulted FSD Channel</legend>
                            <ul class="key-value-list">                       
                                <li>
                                    <div class="li-key">
                                        <label for="node">Node</label>
                                    </div>
                                    <div class="li-value">
                                        <input type="text" id="node" name="node" value="${fn:escapeXml(param.node)}"/>     
                                        (use % as wildcard)
                                    </div>
                                </li>                      
                                <li>
                                    <div class="li-key">
                                        <label for="channel">Channel #</label>
                                    </div>
                                    <div class="li-value">
                                        <input type="number" min="0" id="channel" name="channel" value="${fn:escapeXml(param.channel)}"/>
                                    </div>
                                </li>
                            </ul>  
                        </fieldset>                                                        
                        <fieldset>                                            
                            <legend>Identity</legend>
                            <ul class="key-value-list">                                              
                                <li>
                                    <div class="li-key">
                                        <label for="tripId">Trip ID</label>
                                    </div>
                                    <div class="li-value">
                                        <input type="number" min="0" id="tripId" name="tripId" value="${fn:escapeXml(param.tripId)}"/>
                                    </div>
                                </li>                                            
                                <li style="display: none;">
                                    <div class="li-key">
                                        <label for="faultId">Fault ID</label>
                                    </div>
                                    <div class="li-value">
                                        <input type="number" min="0" id="faultId" name="faultId" value="${fn:escapeXml(param.faultId)}"/>
                                    </div>
                                </li>
                                <li style="display: none;">
                                    <div class="li-key">
                                        <label for="exceptionId">Exception ID</label>
                                    </div>
                                    <div class="li-value">
                                        <input type="number" min="0" id="exceptionId" name="exceptionId" value="${fn:escapeXml(param.exceptionId)}"/>
                                    </div>
                                </li> 
                            </ul>
                        </fieldset>
                    </div>
                    <input type="hidden" name="qualified" value=""/>                                                    
                    <input type="hidden" id="offset-input" name="offset" value="0"/>
                    <input type="hidden" id="max-input" name="max" value="${max}"/>
                    <input id="filter-form-submit-button" type="submit" value="Apply"/>                                
                </form>                                                    
            </s:filter-flyout-widget>
            <h2 id="page-header-title"><c:out value="${title}"/></h2>                
            <div class="message-box">
                <c:out value="${selectionMessage}"/>
            </div>
            <div>
                <c:if test="${fn:length(tripList) > 0}"> 
                    <div class="dialog-content">
                        <t:trip-list tripList="${tripList}"/>
                    </div>
                    <div class="event-controls">
                        <button id="previous-button" type="button" data-offset="${paginator.previousOffset}" value="Previous"${paginator.previous ? '' : ' disabled="disabled"'}>Previous</button>                        
                        <button id="next-button" type="button" data-offset="${paginator.nextOffset}" value="Next"${paginator.next ? '' : ' disabled="disabled"'}>Next</button>                 
                        <div id="max-select-div" style="float: right;">
                            <select id="max-select">
                                <option ${max eq '10' ? ' selected="selected"' : ''}>10</option>
                                <option ${max eq '25' ? ' selected="selected"' : ''}>25</option>
                                <option ${max eq '100' ? ' selected="selected"' : ''}>100</option>
                            </select>
                            <label for="max-select">per page</label>
                        </div>
                    </div>
                </c:if>
            </div>
            <form id="excel-form" method="get" action="${pageContext.request.contextPath}/excel/trips.xlsx">
                <input type="hidden" name="start" value="${fn:escapeXml(param.start)}"/>
                <input type="hidden" name="end" value="${fn:escapeXml(param.end)}"/>
                <input type="hidden" name="maxDuration" value="${fn:escapeXml(param.maxDuration)}"/>
                <input type="hidden" name="minDuration" value="${fn:escapeXml(param.minDuration)}"/>    
                <input type="hidden" name="maxDurationUnits" value="${fn:escapeXml(param.maxDurationUnits)}"/>
                <input type="hidden" name="minDurationUnits" value="${fn:escapeXml(param.minDurationUnits)}"/>
                <input type="hidden" name="cause" value="${fn:escapeXml(param.cause)}"/>
                <input type="hidden" name="system" value="${fn:escapeXml(param.system)}"/>
                <input type="hidden" name="cedType" value="${fn:escapeXml(param.cedType)}"/>
                <input type="hidden" name="cedName" value="${fn:escapeXml(param.cedName)}"/>
                <input type="hidden" name="maxTypes" value="${fn:escapeXml(param.maxTypes)}"/>
                <input type="hidden" name="maxDevices" value="${fn:escapeXml(param.maxDevices)}"/>
                <input type="hidden" name="node" value="${fn:escapeXml(param.node)}"/>
                <input type="hidden" name="channel" value="${fn:escapeXml(param.channel)}"/>
                <input type="hidden" name="tripId" value="${fn:escapeXml(param.tripId)}"/>
                <input id="excel-format" type="hidden" name="format" value="normal"/>
                <button id="excel" type="submit" style="display: none;">Excel</button>
            </form>
            <form id="csv-form" method="get" action="${pageContext.request.contextPath}/csv/trips.csv">
                <input type="hidden" name="start" value="${fn:escapeXml(param.start)}"/>
                <input type="hidden" name="end" value="${fn:escapeXml(param.end)}"/>
                <input type="hidden" name="maxDuration" value="${fn:escapeXml(param.maxDuration)}"/>
                <input type="hidden" name="minDuration" value="${fn:escapeXml(param.minDuration)}"/>    
                <input type="hidden" name="maxDurationUnits" value="${fn:escapeXml(param.maxDurationUnits)}"/>
                <input type="hidden" name="minDurationUnits" value="${fn:escapeXml(param.minDurationUnits)}"/>
                <input type="hidden" name="cause" value="${fn:escapeXml(param.cause)}"/>
                <input type="hidden" name="system" value="${fn:escapeXml(param.system)}"/>
                <input type="hidden" name="cedType" value="${fn:escapeXml(param.cedType)}"/>
                <input type="hidden" name="cedName" value="${fn:escapeXml(param.cedName)}"/>
                <input type="hidden" name="maxTypes" value="${fn:escapeXml(param.maxTypes)}"/>
                <input type="hidden" name="maxDevices" value="${fn:escapeXml(param.maxDevices)}"/>
                <input type="hidden" name="node" value="${fn:escapeXml(param.node)}"/>
                <input type="hidden" name="channel" value="${fn:escapeXml(param.channel)}"/>
                <input type="hidden" name="tripId" value="${fn:escapeXml(param.tripId)}"/>
                <input id="csv-format" type="hidden" name="format" value=""/>
                <button id="csv" type="submit" style="display: none;">CSV</button>
            </form>
        </section>
    </jsp:body>         
</t:page>
