<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!DOCTYPE html>
<html>
    <head>        
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>DTM - Wall Display</title>
        <link rel="shortcut icon" href="${pageContext.request.contextPath}/resources/v${initParam.releaseNumber}/img/favicon.ico"/>
        <link rel="stylesheet" type="text/css" href="${cdnContextPath}/jquery-ui/1.10.3/theme/smoothness/jquery-ui.min.css"/> 
        <link rel="stylesheet" type="text/css" href="${cdnContextPath}/jlab-theme/smoothness/1.6/css/smoothness.min.css"/>        
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
        <script type="text/javascript" src="${cdnContextPath}/jquery/1.10.2.min.js"></script>
        <script type="text/javascript" src="${cdnContextPath}/jquery-ui/1.10.3/jquery-ui.min.js"></script>            
        <script type="text/javascript" src="${pageContext.request.contextPath}/resources/v${initParam.releaseNumber}/js/wall.js"></script>
    </body>
</html>