<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!DOCTYPE html>
<html>
    <head>        
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>DTM - Wall Display</title>
        <link rel="shortcut icon" href="${pageContext.request.contextPath}/resources/v${initParam.releaseNumber}/img/favicon.ico"/>
        <c:choose>
            <c:when test="${'CDN' eq resourceLocation}">
                <link rel="stylesheet" type="text/css" href="//${env['CDN_SERVER']}/jquery-ui/1.13.2/theme/smoothness/jquery-ui.min.css"/>
                <link rel="stylesheet" type="text/css" href="//${env['CDN_SERVER']}/jlab-theme/smoothness/${env['CDN_VERSION']}/css/smoothness.min.css"/>
            </c:when>
            <c:otherwise><!-- LOCAL -->
                <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/resources/jquery-ui-1.13.2/jquery-ui.min.css"/>
                <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/resources/v${initParam.releaseNumber}/css/smoothness.css"/>
            </c:otherwise>
        </c:choose>
        <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/resources/v${initParam.releaseNumber}/css/dtm.css"/>
        <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/resources/v${initParam.releaseNumber}/css/wall.css"/>
    </head>
    <body>
        <c:if test="${initParam.notification ne null}">
            <div id="notification-bar"><c:out value="${initParam.notification}"/></div>
        </c:if>        
        <div id="page">
            <header>
                <h1><a href="/dtm"><span id="page-header-logo"></span> <span id="page-header-text"><c:out value="${initParam.appName}"/></span></a></h1>               
            </header>
            <div id="content"> 
                <section>
                    <div id="event-block">

                    </div>
                </section>
            </div>
            <footer>
            </footer>
        </div>
        <c:choose>
            <c:when test="${'CDN' eq resourceLocation}">
                <script src="//${env['CDN_SERVER']}/jquery/3.6.1.min.js"></script>
                <script src="//${env['CDN_SERVER']}/jquery-ui/1.13.2/jquery-ui.min.js"></script>
            </c:when>
            <c:otherwise><!-- LOCAL -->
                <script src="${pageContext.request.contextPath}/resources/js/jquery-3.6.1.min.js"></script>
                <script src="${pageContext.request.contextPath}/resources/jquery-ui-1.13.2/jquery-ui.min.js"></script>
            </c:otherwise>
        </c:choose>
        <script type="text/javascript" src="${pageContext.request.contextPath}/resources/v${initParam.releaseNumber}/js/wall.js"></script>
    </body>
</html>