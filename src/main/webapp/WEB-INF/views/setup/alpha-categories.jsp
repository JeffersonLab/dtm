<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@taglib prefix="s" uri="http://jlab.org/jsp/smoothness"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%> 
<%@taglib prefix="dtm" uri="http://jlab.org/dtm/functions"%>
<c:set var="title" value="Alpha Categories"/>
<s:setup-page title="${title}">
    <jsp:attribute name="stylesheets">
    </jsp:attribute>
    <jsp:attribute name="scripts">
    </jsp:attribute>        
    <jsp:body>
        <section>
            <h2><c:out value="${title}"/></h2>
            <p>Categories are hierarchical, but many reports benefit from use of a subset of "alpha categories", which are a carefully chosen set that still captures all downtime, but is "flat", i.e. no alpha category is a subset of another alpha category, and therefore the alpha set is smaller than the full set.</p>
            <p>It's up to the admins to ensure they don't include alpha categories here that are already covered by another alpha category or leave out a branch that would miss downtime.  Including the top-level category, for example, would immediately disqualify all other categories.  Any category that has one or more systems as a sibling is not a candidate for an alpha category (as the parent category would be needed to capture the downtime in the systems).</p>
            <table id="alpha-table" class="data-table stripped-table">
                <thead>
                <tr>
                    <th>Category Name</th>
                </tr>
                </thead>
                <tbody>
                <c:forEach var="category" items="${categoryList}">
                    <tr>
                        <td><c:out value="${category.name}"/></td>
                    </tr>
                </c:forEach>
                </tbody>
            </table>
        </section>
    </jsp:body>         
</s:setup-page>