<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="s" uri="http://jlab.org/jsp/smoothness"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%> 
<c:set var="title" value="Help"/>
<t:page title="${title}">  
    <jsp:attribute name="stylesheets">
        <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/resources/v${initParam.resourceVersionNumber}/css/help.css"/>        
    </jsp:attribute>
    <jsp:attribute name="scripts">
        <script type="text/javascript" src="${pageContext.request.contextPath}/resources/v${initParam.resourceVersionNumber}/js/help.js"></script>              
    </jsp:attribute>            
    <jsp:body>
        <s:help-panel title="${title}">
            <ul>
                <li><a href="https://devweb.acc.jlab.org/twiki/pub/SWDocs/DowntimeManager/DowntimeManager.pdf">Guidance</a></li>
            </ul>
        </s:help-panel>
    </jsp:body>         
</t:page>