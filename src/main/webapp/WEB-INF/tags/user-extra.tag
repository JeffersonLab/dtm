<%@tag description="User Extra Tag" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="jakarta.tags.core" %>
<%@taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:choose>
    <c:when test="${pageContext.request.isUserInRole('dtm-reviewer')}">
        <form id="role-form" action="${pageContext.request.contextPath}/change-role" method="post">
            <select class="change-submit" name="role">
                <option value="REVIEWER"${sessionScope.effectiveRole eq 'REVIEWER' ? ' selected="selected"' : ''}>
                    Reviewer
                </option>
                <option value="OPERATOR"${sessionScope.effectiveRole ne 'REVIEWER' ? ' selected="selected"' : ''}>
                    Operator
                </option>
            </select>
            <input type="hidden" name="returnUrl"
                   value="${fn:escapeXml(domainRelativeReturnUrl)}"/>
        </form>
    </c:when>
    <c:otherwise>
        Operator
    </c:otherwise>
</c:choose>