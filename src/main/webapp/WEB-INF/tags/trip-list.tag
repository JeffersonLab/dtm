<%@tag description="Event Timeline Template Tag" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@taglib prefix="s" uri="http://jlab.org/jsp/smoothness"%>
<%@taglib prefix="dtm" uri="http://jlab.org/dtm/functions"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%>
<%@attribute name="tripList" required="true" type="java.util.List"%>
<div class="event-list">
    <div class="event-header top-event-header">
        <span class="event-header-toggle"></span>
        <span class="event-header-type"></span>
        <span class="event-header-title">System</span>
        <span class="event-header-period">Down</span>
        <span class="event-header-duration">Duration</span>
    </div>
    <div id="accordion" class="ui-accordion ui-accordion-icons ui-widget ui-helper-reset">
        <c:forEach items="${tripList}" var="trip">
            <h3 id="header-${trip.fsdTripId}" class="event-header bottom-event-header ui-accordion-header ui-helper-reset ui-state-default ui-corner-top ui-corner-bottom">
                <span class="event-header-toggle"></span>
                <span class="event-header-type"></span>
                <span class="event-header-title">
                    <span class="accordion-event-title"><c:out value="${trip.title}"/></span>
                    ${trip.deviceCount > 1 ? '<span class="more-incidents" title="Trip contains '.concat(trip.deviceCount).concat(' faulted devices"> (').concat(trip.deviceCount).concat(')</span>') : ''} ${trip.end eq null ? '<span class="still-open-asterisk" title="Trip is open">^</span>' : ''}                        
                </span>
                <fmt:formatDate var="fullTimeDown" value="${trip.start}" pattern="dd-MMM-yyyy HH:mm:ss"/>
                <span class="event-header-period" title="${fullTimeDown}">
                    <fmt:formatDate pattern="${s:getFriendlyDateTimePattern()}" value="${trip.start}"/>
                </span>
                <c:if test="${trip.end ne null}">
                    <fmt:formatDate var="fullTimeUp" value="${trip.end}" pattern="dd-MMM-yyyy HH:mm:ss"/>
                </c:if>
                <span class="event-header-duration ${dtm:elapsedClasses(trip.elapsedMillis)}" title="Time Up: ${fullTimeUp}">
                    <span class="event-header-time-elapsed-wrap"><c:out value="${dtm:millisToHumanReadable(trip.elapsedMillis, true)}"/></span>
                </span>
            </h3>
            <div id="content-${trip.fsdTripId}" class="ui-accordion-content ui-helper-reset ui-widget-content ui-corner-bottom">
                <t:trip-detail trip="${trip}"/>
            </div>
        </c:forEach>
    </div>
</div>