<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%>
<ul class="key-value-list">
    <li>
        <div class="li-key">
            <label for="category-select">Category</label>
        </div>
        <div class="li-value">
            <select id="category-select" name="categoryId">
                <option value="">&nbsp;</option>
                <c:forEach items="${categoryRoot.children}" var="child">
                    <t:hierarchical-select-option node="${child}" level="0" parameterName="categoryId"/>
                </c:forEach>
            </select>
        </div>
    </li>                     
    <li>
        <div class="li-key">
            <span class="sub-level-symbol">↳</span>
        </div>
        <div class="li-value">
            <div class="sub-table">
                <div class="sub-key">
                    <label class="required-field" for="system-select">Subsystem</label>
                </div>
                <div class="sub-value">
                    <select id="system-select" name="systemId">
                        <option value="">&nbsp;</option>
                        <c:forEach items="${systemList}" var="system">
                            <option value="${system.systemId}"${param.systemId eq system.systemId ? ' selected="selected"' : ''}><c:out value="${system.name}"/></option>
                        </c:forEach>                            
                    </select>
                </div>
            </div>
        </div>
    </li>
</ul>