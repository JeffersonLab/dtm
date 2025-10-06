<%@tag description="Hierarchical selection option tag" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="jakarta.tags.core"%>
<%@taglib prefix="fn" uri="jakarta.tags.functions"%>
<%@taglib prefix="fmt" uri="jakarta.tags.fmt"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%> 
<%@attribute name="node" required="true" type="org.jlab.dtm.persistence.model.Node"%>
<%@attribute name="level" required="true" type="java.lang.Integer"%>
<%@attribute name="parameterName" required="true" type="java.lang.String"%>
<option value="${node.id}"${param[parameterName] eq node.id ? ' selected="selected"' : ''}><c:forEach begin="1" end="${level}">&nbsp;&nbsp;&nbsp;&nbsp;</c:forEach><c:out value="${node.name}"/></option>
<c:forEach items="${node.children}" var="child">
    <t:hierarchical-select-option node="${child}" level="${level + 1}" parameterName="${parameterName}"/>    
</c:forEach>
