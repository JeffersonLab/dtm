<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@taglib prefix="dtm" uri="http://jlab.org/dtm/functions"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%> 
<!DOCTYPE html>
<html>
    <head>        
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>DTM - Incident List</title>      
    </head>
    <body>
        <div class="message-box">
            <c:out value="${selectionMessage}"/>
        </div>
        <c:if test="${fn:length(incidentList) > 0}">
            <ul>
                <c:forEach items="${incidentList}" var="incident">
                    <li>
                        <span><a href="${pageContext.request.contextPath}/all-events?incidentId=${incident.incidentId}&amp;qualified=" title="${fn:escapeXml(incident.title)}"><c:out value="${incident.title}"/></a></span>
                            <fmt:formatNumber value="${incident.downtimeHours}" var="formattedUnbounded"/>
                        (<span title="Total Unbounded Hours: ${formattedUnbounded}"><c:out value="${dtm:millisToAbbreviatedHumanReadable(incident.downtimeHoursBounded * 3600000)}"/></span>)
                    </li>
                </c:forEach>
            </ul>
        </c:if>
    </body>
</html>