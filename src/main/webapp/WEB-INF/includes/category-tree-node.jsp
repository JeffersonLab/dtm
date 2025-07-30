<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<li id="node-CATEGORY-${parent.categoryId}" data-node-type="CATEGORY" data-jstree='{"type":"CATEGORY"}'>
    <a href="#"><c:out value="${parent.name}"/></a>
    <c:set var="parentForSystemUse" value="${parent}"/>
    <c:if test="${(fn:length(parent.categoryList) > 0)}">
        <ul class="category-list">
            <c:forEach var="child" items="${parent.categoryList}">
                <c:set var="parent" value="${child}" scope="request"/>
                <jsp:include page="category-tree-node.jsp"/>
            </c:forEach>
        </ul>
    </c:if>
    <c:set var="parent" value="${parentForSystemUse}"/>
    <c:if test="${(fn:length(parent.systemList) > 0)}">
        <ul class="system-list">
            <c:forEach var="child" items="${parent.systemList}">
                <li data-node-type="SYSTEM" data-jstree='{"type":"SYSTEM"}'>
                    <span><c:out value="${child.name}"/></span>
                </li>
            </c:forEach>
        </ul>
    </c:if>
</li>