<%@tag description="Event Timeline Template Tag" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@taglib prefix="s" uri="http://jlab.org/jsp/smoothness"%>
<%@taglib prefix="dtm" uri="http://jlab.org/dtm/functions"%>
<%@attribute name="trip" required="true" type="org.jlab.dtm.persistence.model.FsdTrip"%>
<jsp:useBean id="now" class="java.util.Date"/>
<c:set var="timeDown" value="${trip.start}"/>
<c:set var="timeUp" value="${trip.end == null ? now : trip.end}"/>
<fmt:formatDate pattern="${s:getFriendlyDateTimePattern()}" value="${timeUp}" var="timeUpStr"/>
<div class="event-detail">
    <div class="event-control-toolbar ui-widget ui-widget-header">
        <span class="cause-span">Cause: <c:out value="${trip.rootCause}"/></span>
        <span class="area-span">Area: <c:out value="${trip.area}"/></span>
    </div>
    <div class="event-id-container"><span class="event-id-label">Trip ID: </span><span class="event-id-value">${trip.fsdTripId}</span></div>
    <h4>Machine State</h4>
    <div class="state-panel">
        <table class="horizontal-key-value-table">   
            <thead>
                <tr>
                    <th>Accelerator</th>
                    <th>Hall A</th>
                    <th>Hall B</th>
                    <th>Hall C</th>
                    <th>Hall D</th>
                </tr>
            </thead>
            <tbody>
                <tr>
                    <td><c:out value="${trip.accState.label}"/></td>
                    <td><c:out value="${trip.hallAState.label}"/></td>
                    <td><c:out value="${trip.hallBState.label}"/></td>
                    <td><c:out value="${trip.hallCState.label}"/></td>
                    <td><c:out value="${trip.hallDState.label}"/></td>
                </tr>
            </tbody>
        </table> 
    </div>
    <h4>Faulted FSD Channel</h4>
    <table class="data-table stripped-table incident-table">
        <thead>
            <tr>
                <th class="fault-id-header"></th>
                <th>Faulted FSD Device</th>
            </tr>
        </thead>
        <tbody>
            <c:forEach items="${trip.faultMap.values()}" var="fault">
                <tr>
                    <td>
                        <span class="cell-subfield">
                            <span class="cell-sublabel">Node:</span>
                            <span><c:out value="${fault.node}"/></span>
                        </span>
                        <span class="cell-subfield">
                            <span class="cell-sublabel">Channel #:</span>
                            <span><c:out value="${fault.channel}"/></span>
                        </span>         
                        <span class="cell-subfield">
                            <span class="cell-sublabel">Path</span>
                            <span><c:out value="${fault.disjoint ? 'Secondary' : 'Primary'}"/></span>
                        </span> 
                    </td>
                    <td>
                        <table class="data-table stripped-table">
                            <thead>
                                <tr>
                                    <th>Category</th>
                                    <th>System</th>
                                    <th>CED Type</th>
                                    <th>CED Name</th>
                                    <th>Confirmed</th>
                                    <th>Region</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach items="${fault.deviceMap.values()}" var="device">
                                    <tr>
                                        <td><c:out value="${device.category}"/></td>
                                        <td><c:out value="${device.system}"/></td>
                                        <td><c:out value="${device.cedType}"/></td>
                                        <td><c:out value="${device.cedName}"/></td>
                                        <td>${device.confirmed ? 'Yes' : 'No'}</td>
                                        <td><c:out value="${device.region}"/></td>
                                    </tr>
                                </c:forEach>
                            </tbody>
                        </table>
                    </td>                
                </tr>
            </c:forEach>                                        
        </tbody>
    </table>
</div>