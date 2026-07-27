<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="jakarta.tags.core"%>
<%@taglib prefix="fn" uri="jakarta.tags.functions"%>
<%@taglib prefix="fmt" uri="jakarta.tags.fmt"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%>
<%@taglib prefix="s" uri="jlab.tags.smoothness"%>
<c:set var="title" value="Expert"/>
<t:reports-page title="${title}">  
    <jsp:attribute name="stylesheets">
    </jsp:attribute>
    <jsp:attribute name="scripts">
    </jsp:attribute>        
    <jsp:body>
        <section>
            <s:filter-flyout-widget>
                <form class="filter-form" method="get" action="expert">
                    <div id="filter-form-panel">
                        <fieldset>
                            <legend>Taxonomy</legend>
                            <ul class="key-value-list">
                                <li>
                                    <div class="li-key">
                                        <label for="category">Category</label>
                                    </div>
                                    <div class="li-value">
                                        <select id="category" name="category">
                                            <option value=""> </option>
                                            <c:forEach items="${categoryList}" var="category">
                                                <option value="${category.categoryId}" ${category.categoryId eq param.category ? 'selected="selected"' : ''}><c:out value="${category.name}"/></option>
                                            </c:forEach>
                                        </select>
                                    </div>
                                </li>
                            </ul>
                        </fieldset>
                    </div>
                    <input class="filter-form-submit-button" type="submit" value="Apply"/>
                </form>
            </s:filter-flyout-widget>
            <h2 class="page-header-title"><c:out value="${title}"/></h2>
            <div class="message-box">
                <c:out value="${selectionMessage}"/>
            </div>
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
                                        <c:set value="${s:lookupUserByUsername(se.username)}" var="user"/>
                                        <tr>
                                            <td><c:out value="${user.lastname}"/></td>
                                            <td><c:out value="${user.firstname}"/></td>
                                            <td><c:out value="${user.username}"/></td>
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