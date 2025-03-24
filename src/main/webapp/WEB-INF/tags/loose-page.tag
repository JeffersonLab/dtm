<%@tag description="A Loose Page (no navigation) Template" pageEncoding="UTF-8" trimDirectiveWhitespaces="true"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@attribute name="stylesheets" fragment="true"%>
<%@attribute name="scripts" fragment="true"%>
<%@attribute name="title"%>
<%@attribute name="description"%>
<%@attribute name="category"%>
<c:choose>
    <c:when test="${param.partial eq 'Y'}">
        <div id="partial" data-title="${title}">
            <div id="partial-css">
                <jsp:invoke fragment="stylesheets"/>
            </div>
            <div id="partial-html">
                <div class="partial">
                    <jsp:doBody/>
                </div>
            </div>
            <div id="partial-js">
                <jsp:invoke fragment="scripts"/>
            </div>
        </div>
    </c:when>
    <c:otherwise>
        <c:url var="domainRelativeReturnUrl" scope="request" context="/" value="${requestScope['javax.servlet.forward.request_uri']}${requestScope['javax.servlet.forward.query_string'] ne null ? '?'.concat(requestScope['javax.servlet.forward.query_string']) : ''}"/>
        <c:set var="currentPath" scope="request" value="${requestScope['javax.servlet.forward.servlet_path']}"/>
        <c:set var="resourceLocation" value="${env[initParam.appSpecificEnvPrefix.concat('_RESOURCE_LOCATION')]}"/>
        <!DOCTYPE html>
        <html lang="en">
        <head>
            <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
            <meta name="description" content="${fn:escapeXml(description)}">
            <title><c:out value="${initParam.appShortName}"/> - ${empty category ? '' : category.concat(' - ')}${title}</title>
            <link rel="shortcut icon" href="${pageContext.request.contextPath}/resources/v${initParam.releaseNumber}/img/favicon.ico"/>
            <jsp:invoke fragment="stylesheets"/>
        </head>
        <body class="${param.print eq 'Y' ? 'print ' : ''} ${param.fullscreen eq 'Y' ? 'fullscreen' : ''}">
            <jsp:doBody/>
            <jsp:invoke fragment="scripts"/>
        </body>
    </c:otherwise>
</c:choose>