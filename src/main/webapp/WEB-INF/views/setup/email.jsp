<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%> 
<c:set var="title" value="Email"/>
<t:setup-page title="${title}">  
    <jsp:attribute name="stylesheets">
        <style type="text/css">
            .email-section {
                background-color: black;
                color: white;
                padding: 1em;
            }
        </style>
    </jsp:attribute>
    <jsp:attribute name="scripts">
        <script type="text/javascript" src="${pageContext.request.contextPath}/resources/v${initParam.resourceVersionNumber}/js/email.js"></script>
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
                <button id="email-now-button" type="button">Send Email Now</button>
            </form>
        </section>
    </jsp:body>         
</t:setup-page>  