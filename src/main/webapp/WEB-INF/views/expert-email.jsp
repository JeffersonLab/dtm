<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@taglib prefix="s" uri="http://jlab.org/jsp/smoothness" %>
<fmt:setLocale value="en_US" scope="session"/>
<c:set var="pathPrefix" value="${pageContext.request.contextPath}"/>
<c:if test="${param.email eq 'Y'}">
    <c:set var="pathPrefix" value="https://ace.jlab.org/dtm"/>
</c:if>
<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title><c:out value="${initParam.appShortName}"/> - Expert Action Needed</title>
    <link rel="shortcut icon" href="${pathPrefix}/resources/v${initParam.releaseNumber}/img/favicon.ico"/>
    <style>
        th {font-weight: normal;}
        td {padding: 0.5em;}
        td {border-right: 1px solid black;}
        td:last-child {border-right: 1px solid white;}
    </style>
</head>
<body>
<h2 style="margin-left: 16px;"><c:out value="${initParam.appShortName}"/> - Expert Action Needed</h2>
<div style="margin-left: 16px;" id="email-content" class="content-section dialog-content">
    This email is in reference to a system failure recorded in DTM within the last <c:out value="${numberOfHours}"/> hours that requires your review.<br/>
    <c:choose>
        <c:when test="${fn:length(incidentList) > 0}">
            <ul>
                <c:forEach items="${incidentList}" var="incident">
                    <li><a href="${pathPrefix}/incidents/${incident.incidentId}"><c:out value="${incident.title}"/></a></li>
                </c:forEach>
            </ul>
        </c:when>
        <c:otherwise>
            <div id="doNotSend" class="error-message">
                <c:out value="${willNotBeSentMessage}"/>
            </div>
        </c:otherwise>
    </c:choose>

    <b>Please conduct a repair assessment and review the incident(s) here:</b>
    <br/><b><a href="${pathPrefix}/events?acknowledged=N&smeUsername=${username}&qualified=">DTM-RAR Review</a></b>
    <br/><br/>
    <h3>Action Level Reference</h3>
    <table style="border-bottom: 1px solid black; border-collapse: collapse;">
        <tbody>
            <tr style="background-color: rgb(128,0,0); color: white;">
                <th style="border-right: 1px solid white;"></th>
                <th style="border-right: 1px solid white;">Triggers</th>
                <th style="border-right: 1px solid white;">Action</th>
                <th style="border-right: 1px solid white;">Time</th>
                <th></th>
            </tr>
            <tr style="background-color: rgb(255,255,204);">
                <td>Level Ⅰ</td>
                <td>Short repairs (5–30 minutes)</td>
                <td>Group Leader or SME review (check a box)</td>
                <td>2 days</td>
                <td></td>
            </tr>
            <tr>
                <td>Level Ⅱ</td>
                <td>Single system, >30 minute repair</td>
                <td>Group Leader or SME review and root cause statement (a couple of sentences)</td>
                <td>2 days</td>
                <td></td>
            </tr>
            <tr style="background-color: rgb(255,255,204);">
                <td>Level&nbsp;Ⅲ</td>
                <td>4-hour escalation or Director of Operations discretion</td>
                <td>Group Leader or SME root-cause memo (a more lengthy analysis of an event) and a 3–4 minute report at the next Weekly Summary Meeting (Wednesdays at 1330)</td>
                <td>Next Weekly Summary Meeting</td>
                <td><a href="https://ace.jlab.org/cdn/doc/dtm/RARTemplate3.docx">Template</a></td>
            </tr>
            <tr>
                <td>Level&nbsp;Ⅳ</td>
                <td>Program change, safety issue, compounded event, or Director of Operations discretion</td>
                <td>Formal investigation/report by a Repair Investigation Team and follow-up presentation at the Weekly Summary Meeting</td>
                <td>3 weeks</td>
                <td><a href="https://ace.jlab.org/cdn/doc/dtm/RARTemplate4.docx">Template</a></td>
            </tr>
        </tbody>
    </table>
    <br/>Upon completion of Level Ⅲ+ reports upload the document to the DTM incident.
    <br/><br/>The Repair Assessment Report procedure is available online at the following location:
    <br/><a href="https://ace.jlab.org/cdn/doc/dtm/RARProcedure.pdf">RAR Procedure</a>
</div>
</body>
</html>