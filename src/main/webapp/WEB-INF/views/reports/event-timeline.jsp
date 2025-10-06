<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="jakarta.tags.core"%>
<%@taglib prefix="fn" uri="jakarta.tags.functions"%>
<%@taglib prefix="fmt" uri="jakarta.tags.fmt"%>
<%@taglib prefix="s" uri="jlab.tags.smoothness"%>
<%@taglib prefix="dtm" uri="http://jlab.org/dtm/functions"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%> 
<jsp:useBean id="now" class="java.util.Date"/>
<c:set var="title" value="Event Timeline"/>
<t:reports-page title="${title}">  
    <jsp:attribute name="stylesheets">
        <style type="text/css">
            .timeline {
                margin: 5em 3.5em 0 6.5em;
            }
            .timeline-block {
                width: 100%;
                position: relative;
                border: 1px solid gray;
                min-height: 2.5em;
                background: linear-gradient(#fff, #eee) repeat scroll 0 0 rgba(0, 0, 0, 0);
            }
            .timeline-event-list {
                overflow: hidden;
                width: 100%;
                height: 100%;
                position: absolute;
            }
            .timeline-scale {
                width: 100%;
                height: 100%;
                position: absolute;
            }
            .timeline-event {
                position: absolute;
                background-color: lightblue;
                height: 1.5em;
                border-radius: 1em;
                box-shadow: 0.25em 0.25em 0.25em #979797;
                z-index: 2;
            }
            .event-title {
                height: 1.5em;
                line-height: 1.5em;                
                margin: 0 1em;
                display: block;
                position: absolute;
                left: 0;
                right: 0;
                overflow: hidden;
                text-align: center;
                text-overflow: ellipsis;
            }
            .tick {
                border-left: 1px solid black; 
                width: 0; 
                position: absolute; 
                z-index: 0;                
            }
            .timeline-scale .tick:nth-child(1) {
                border-left: 0;
            }
            .tick-label {
                position: absolute; 
                top: -3em; 
                width: 8em; 
                left: -4em; 
                text-align: center;
            }
            .type-label {
                position: absolute;
                left: -6.5em;
                width: 6em;
                height: 1.5em;
                line-height: 1.5em;
                text-align: right;
            }

            #start,
            #end {
                position: relative;
                z-index: 4;
            }
        </style>
    </jsp:attribute>
    <jsp:attribute name="scripts">
        <script type="text/javascript">
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
                <form class="filter-form" method="get" action="event-timeline">
                    <fieldset>
                        <legend>Time</legend>
                        <s:date-range required="${true}" datetime="${true}" sevenAmOffset="${true}"/>
                    </fieldset>
                    <input class="filter-form-submit-button" type="submit" value="Apply"/>
                </form>   
            </s:filter-flyout-widget>
            <h2 class="page-header-title"><c:out value="${title}"/></h2>
            <c:choose>
                <c:when test="${eventList == null}">
                    <div class="message-box">Select a start and end date to continue</div>
                </c:when>
                <c:otherwise>
                    <div class="message-box">
                        <c:out value="${selectionMessage}"/>
                    </div>                     
                    <c:if test="${fn:length(eventList) > 0}">
                        <c:set var="timelineHeight" value="${fn:length(eventTypeList) * 2 + 0.5}em;"/>
                        <div class="timeline">
                            <div class="timeline-block" style="height: ${timelineHeight}">
                                <div class="timeline-event-type-list">
                                    <c:forEach items="${eventTypeList}" var="type" varStatus="status">
                                        <div class="type-label" style="top: ${status.index * 2 + 0.5}em;"><c:out value="${type.name}"/></div>
                                        <div class="timeline-event-list">
                                            <c:forEach items="${eventList}" var="event">
                                                <c:if test="${event.eventType.eventTypeId eq type.eventTypeId}">
                                                    <c:set var="eventTimeDown" value="${event.timeDown}"/>
                                                    <c:set var="eventTimeUp" value="${event.timeUp == null ? now : event.timeUp}"/>
                                                    <c:set var="eventDuration" value="${eventTimeUp.time - eventTimeDown.time}"/>        
                                                    <div title="${event.title} &#013;${fn:length(event.incidentList)} Incidents &#013;${dtm:millisToHumanReadable(eventDuration, false)} &#013;Event ID ${event.eventId}" class="timeline-event" style="top: ${status.index * 2 + 0.5}em; width: ${(eventDuration / timelineDuration) * 100}%; left: ${((eventTimeDown.time - start.time) / timelineDuration) * 100}%;">
                                                        <span class="event-title"><c:out value="${incident.title}"/></span>
                                                    </div>
                                                </c:if>
                                            </c:forEach>
                                        </div>
                                    </c:forEach>
                                </div>
                                <div class="timeline-scale">
                                    <c:forEach begin="0" end="5" varStatus="status">
                                        <c:set var="percent" value="${status.index * 20}"/>
                                        <div class="tick" style="height: ${timelineHeight}; left: ${percent}%;">
                                            <div class="tick-label"><fmt:formatDate pattern="${s:getFriendlyDateTimePattern()}" value="${dtm:getTimelineTickDate(percent, timelineDuration, start)}"/></div>
                                        </div>            
                                    </c:forEach>
                                </div>
                            </div>
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