<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@taglib prefix="s" uri="http://jlab.org/jsp/smoothness"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%> 
<c:set var="title" value="Email"/>
<t:setup-page title="${title}">  
    <jsp:attribute name="stylesheets">
        <style>
            .email-section {
                background-color: black;
                color: white;
                padding: 1em;
            }
        </style>
    </jsp:attribute>
    <jsp:attribute name="scripts">
        <script type="text/javascript" src="${pageContext.request.contextPath}/resources/v${initParam.releaseNumber}/js/email.js"></script>
    </jsp:attribute>        
    <jsp:body>
        <section>
            <h2><c:out value="${title}"/></h2>
            <h3 class="email-section">Scheduled Daily Emails
                <form style="position: relative; top: -5px; display: inline-block; float: right; font-size: 10px;" method="post" action="email">
                    <fieldset>
                        <label for="on-notification">On</label>
                        <input style="vertical-align: bottom;" class="change-submit" id="on-notification" type="radio" name="schedulerEnabled" value="Y"${schedulerEnabled ? ' checked="checked"' : ''}/>
                        <label style="margin-left: 10px;" for="off-notification">Off</label>
                        <input style="vertical-align: bottom;" class="change-submit" id="off-notification" type="radio" name="schedulerEnabled" value="N"${not schedulerEnabled ? ' checked="checked"' : ''}/>
                    </fieldset>
                </form>
            </h3>
            <form method="post" action="ajax/email-on-demand">
                <label for="username">SME</label>
                <input id="username" type="text" class="username-autocomplete" name="username" placeholder="username"/>
                <button id="email-preview-button" type="button">Preview</button>
                <button id="email-now-button" type="button">Send Email Now</button>
            </form>
            <p>If scheduled emails are enabled then each day (Monday - Friday only) a check is done at 9:55 AM.  Any Subject Matter Expert (SME) that has associated incidents that were closed in the last 24 hours (or 72 hours if Monday) is notified via email.</p>
            <fmt:formatDate pattern="${s:getFriendlyDateTimePattern()}" value="${start}" var="startFmt"/>
            <fmt:formatDate pattern="${s:getFriendlyDateTimePattern()}" value="${end}" var="endFmt"/>
            <c:url value="/reports/root-cause" var="url">
                <c:param name="start" value="${startFmt}"/>
                <c:param name="end" value="${endFmt}"/>
                <c:param name="incidentMask" value="DEADBEATS"/>
                <c:param name="qualified" value=""/>
            </c:url>
            <p><a href="${url}">Unreviewed last <c:out value="${numberOfHours}"/> hours</a></p>
            <p><b>Note</b>: All emails are configured to CC: <c:out value="${ccCsv}"/></p>
        </section>
    </jsp:body>         
</t:setup-page>  