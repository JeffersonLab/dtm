<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%> 
<c:set var="title" value="Expert"/>
<t:reports-page title="${title}">  
    <jsp:attribute name="stylesheets">
    </jsp:attribute>
    <jsp:attribute name="scripts">
    </jsp:attribute>        
    <jsp:body>
        <section>
            <h2><c:out value="${title}"/></h2>
            <div>                  
                <c:forEach items="${systemList}" var="system">
                    <h3><c:out value="${system.name}"/></h3>
                    <c:choose>
                        <c:when test="${fn:length(system.systemExpertList) > 0}">
                            <table class="data-table stripped-table">
                                <thead>
                                    <tr>
                                        <th>Lastname</th>
                                        <th>Firstname</th>
                                        <th>Username</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <c:forEach items="${system.systemExpertList}" var="se">
                                        <tr>
                                            <td><c:out value="${se.expert.lastname}"/></td>
                                            <td><c:out value="${se.expert.firstname}"/></td>
                                            <td><c:out value="${se.expert.username}"/></td>
                                        </tr>
                                    </c:forEach>
                                </tbody>
                            </table>
                        </c:when>
                        <c:otherwise>
                            <div>No Experts</div>
                        </c:otherwise>
                    </c:choose>
                </c:forEach>
            </div>
        </section>
    </jsp:body>         
</t:reports-page>