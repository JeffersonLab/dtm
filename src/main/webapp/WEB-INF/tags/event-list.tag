<%@tag description="Event Timeline Template Tag" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@taglib prefix="s" uri="http://jlab.org/jsp/smoothness"%>
<%@taglib prefix="dtm" uri="http://jlab.org/dtm/functions"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%>
<%@attribute name="logbookHostname" required="true" type="java.lang.String"%>
<%@attribute name="eventList" required="true" type="java.util.List"%>
<div class="event-list">
    <div class="event-header top-event-header">
        <span class="event-header-toggle"></span>
        <span class="event-header-type">Type</span>
        <span class="event-header-title">Title</span>
        <span class="event-header-period">Down</span>
        <span class="event-header-duration">Duration</span>
        <div class="event-header-reviewed">
            Reviewed<br/>
            SME | OPR
        </div>
    </div>
    <div id="accordion" class="ui-accordion ui-accordion-icons ui-widget ui-helper-reset">
        <c:forEach items="${eventList}" var="event">
            <h3 id="header-${event.eventId}" class="event-header bottom-event-header ui-accordion-header ui-helper-reset ui-state-default ui-corner-top ui-corner-bottom">
                    <span class="event-header-toggle"></span>
                    <span class="event-header-type" data-type-id="${event.eventType.eventTypeId}" title="${event.eventType.name}"><c:out value="${event.eventType.abbreviation}"/></span>
                    <span class="event-header-title">
                        <span class="accordion-event-title"><c:out value="${event.title}"/></span>
                        ${fn:length(event.incidentList) > 1 ? '<span class="more-incidents" title="Event contains '.concat(fn:length(event.incidentList)).concat(' incidents"> (').concat(fn:length(event.incidentList)).concat(')</span>') : ''} ${event.timeUp eq null ? '<span class="still-open-asterisk" title="Event is open">^</span>' : ''}
                    </span>
                    <span class="event-header-period">
                        <fmt:formatDate pattern="${s:getFriendlyDateTimePattern()}" value="${event.timeDown}"/>
                    </span>
                    <span class="event-header-duration ${dtm:elapsedClasses(event.elapsedMillis)}">
                        <span class="event-header-time-elapsed-wrap"><c:out value="${dtm:millisToHumanReadable(event.elapsedMillis, true)}"/></span>
                    </span>
                    <span class="event-header-reviewed">      
                        <span title="${event.expertReviewed ? 'SME Reviewed' : event.containsAtLeastOneExpertReview() ? 'Partial SME Review' : 'Not SME Reviewed'}" class="small-icon ${event.expertReviewed ? 'event-reviewed' : event.containsAtLeastOneExpertReview() ? 'event-partially-reviewed' : 'event-not-reviewed'}"></span>
                        &nbsp;
                        <span title="${event.reviewed ? 'OPR Reviewed' : event.containsAtLeastOneReview() ? 'Partial OPR Review' : 'Not OPR Reviewed'}" class="small-icon ${event.reviewed ? 'event-reviewed' : event.containsAtLeastOneReview() ? 'event-partially-reviewed' : 'event-not-reviewed'}"></span>
                    </span>
            </h3>
            <div id="content-${event.eventId}" class="ui-accordion-content ui-helper-reset ui-widget-content ui-corner-bottom">
                <t:event-detail event="${event}" logbookHostname="${logbookHostname}"/>
            </div>
        </c:forEach>
    </div>
    <input type="hidden" name="logbookHostname" id="logbookHostname" value="${env['LOGBOOK_HOSTNAME']}"/>
</div>