<%@tag description="Hierarchical selection option tag" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%> 
<%@attribute name="node" required="true" type="org.jlab.dtm.persistence.model.Node"%>
<%@attribute name="level" required="true" type="java.lang.Integer"%>
<%@attribute name="parameterName" required="true" type="java.lang.String"%>
<option value="${node.id}"${param[parameterName] eq node.id ? ' selected="selected"' : ''}><c:forEach begin="1" end="${level}">&nbsp;&nbsp;&nbsp;&nbsp;</c:forEach><c:out value="${node.name}"/></option>
<c:forEach items="${node.children}" var="child">
    <t:hierarchical-select-option node="${child}" level="${level + 1}" parameterName="${parameterName}"/>    
</c:forEach>
