<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@taglib prefix="s" uri="http://jlab.org/jsp/smoothness"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%> 
<%@taglib prefix="dtm" uri="http://jlab.org/dtm/functions"%>
<c:set var="title" value="Downtime Events"/>
<t:page title="${title}">  
    <jsp:attribute name="stylesheets"> 
        <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/resources/v${initParam.releaseNumber}/css/event-list.css"/>
    </jsp:attribute>
    <jsp:attribute name="scripts">      
        <script type="text/javascript" src="${pageContext.request.contextPath}/resources/v${initParam.releaseNumber}/js/event-list.js"></script>
    </jsp:attribute>        
    <jsp:body>
        <section>
            <s:filter-flyout-widget ribbon="true" clearButton="true">
                <form id="filter-form" method="get" action="${pageContext.request.contextPath}/events">
                    <fieldset>
                        <legend>Time</legend>
                        <s:date-range datetime="${true}" sevenAmOffset="${true}"/>
                    </fieldset>
                    <fieldset>
                        <legend>Taxonomy</legend>
                        <ul class="key-value-list">                      
                            <li>
                                <div class="li-key">
                                    <label for="event-type">Type</label>
                                </div>
                                <div class="li-value">
                                    <select id="event-type" name="type">
                                        <option value=""> </option>
                                        <c:forEach items="${eventTypeList}" var="eventType">
                                            <option value="${eventType.eventTypeId}"${param.type eq eventType.eventTypeId ? ' selected="selected"' : ''}><c:out value="${eventType.name}"/> (<c:out value="${eventType.abbreviation}"/>)</option>
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
                                    <label for="acknowledged">Expert Acknowledged</label>
                                </div>
                                <div class="li-value">
                                    <select id="acknowledged" name="acknowledged">
                                        <option value=""> </option>
                                        <option value="N"${param.acknowledged eq 'N' ? ' selected="selected"' : ''}>No</option>
                                        <option value="Y"${param.acknowledged eq 'Y' ? ' selected="selected"' : ''}>Yes</option>
                                        <option value="R"${param.acknowledged eq 'R' ? ' selected="selected"' : ''}>Reassign</option>
                                    </select>
                                </div>
                            </li>  
                            <li>
                                <div class="li-key">
                                    <label for="event-id">Event ID</label>
                                </div>
                                <div class="li-value">
                                    <input type="number" id="event-id" name="eventId" value="${param.eventId}"/>
                                </div>
                            </li>
                            <li>
                                <div class="li-key">
                                    <label for="sme-username">SME</label>
                                </div>
                                <div class="li-value">
                                    <input type="text" id="sme-username" name="smeUsername" class="username-autocomplete" value="${param.smeUsername}" placeholder="username"/>
                                </div>
                            </li>                             
                        </ul>
                    </fieldset>
                    <fieldset>
                        <legend>Incident ID</legend>
                        <ul class="key-value-list">
                            <li>
                                <div class="li-key">
                                </div>
                                <div class="li-value">
                                    <div id="incident-add-anchor">
                                        <c:forEach items="${paramValues.incidentId}" var="incidentId">
                                            <div class="incident-id-wrap"><input type="number" name="incidentId" value="${incidentId}"/><button class="remove-incident-id-button" type="button" title="Remove Incident ID">X</button></div>
                                        </c:forEach>
                                    </div>
                                    <div id="incident-add-control"><button id="add-incident-id-button" type="button" title="Add Incident ID">Add</button></div>
                                </div>
                            </li>                         
                        </ul>
                    </fieldset>
                    <input type="hidden" name="qualified" value=""/>            
                    <input type="hidden" id="offset-input" name="offset" value="0"/>
                    <input id="filter-form-submit-button" type="submit" value="Apply"/>
                </form> 
                <div style="display: none;" id="incident-add-template"><div class="incident-id-wrap"><input type="number" id="incident-id" name="incidentId" value=""/><button class="remove-incident-id-button" type="button" title="Remove Incident ID">X</button></div></div>
            </s:filter-flyout-widget>
            <h2 id="page-header-title"><c:out value="${title}"/></h2>                            
            <div class="event-controls">
                <c:if test="${pageContext.request.userPrincipal ne null}">
                    <button id="open-add-event-dialog-button" type="button">Add Event</button>
                </c:if>
            </div>                              
            <div class="message-box">
                <c:out value="${selectionMessage}"/>
            </div>          
            <div>
                <c:if test="${fn:length(eventList) > 0}"> 
                    <div class="dialog-content">
                        <t:event-list eventList="${eventList}"/>
                    </div>
                    <div class="event-controls">
                        <button id="previous-button" type="button" data-offset="${paginator.previousOffset}" value="Previous"${paginator.previous ? '' : ' disabled="disabled"'}>Previous</button>                        
                        <button id="next-button" type="button" data-offset="${paginator.nextOffset}" value="Next"${paginator.next ? '' : ' disabled="disabled"'}>Next</button>                 
                    </div>
                </c:if>
            </div>                    
        </section>
        <t:event-list-dialogs eventTypeList="${eventTypeList}" systemList="${systemList}"/>
    </jsp:body>         
</t:page>
