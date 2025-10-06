<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="jakarta.tags.core"%>
<%@taglib prefix="fn" uri="jakarta.tags.functions"%>
<%@taglib prefix="s" uri="jlab.tags.smoothness"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%> 
<%@taglib prefix="dtm" uri="jlab.tags.dtm"%>
<jsp:useBean id="typeList" scope="request" type="java.util.List"/>
<c:set var="title" value="Event Types"/>
<s:setup-page title="${title}">
    <jsp:attribute name="stylesheets">
    </jsp:attribute>
    <jsp:attribute name="scripts">
    </jsp:attribute>        
    <jsp:body>
        <section>
            <h2><c:out value="${title}"/></h2>
            <table id="alpha-table" class="data-table stripped-table">
                <thead>
                <tr>
                    <th>Name</th>
                    <th>Description</th>
                    <th>Abbreviation</th>
                </tr>
                </thead>
                <tbody>
                <c:forEach var="type" items="${typeList}">
                    <tr>
                        <td><c:out value="${type.name}"/></td>
                        <td><c:out value="${type.description}"/></td>
                        <td><c:out value="${type.abbreviation}"/></td>
                    </tr>
                </c:forEach>
                </tbody>
            </table>
        </section>
    </jsp:body>         
</s:setup-page>