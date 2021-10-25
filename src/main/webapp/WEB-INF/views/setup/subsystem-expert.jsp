<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@taglib prefix="s" uri="http://jlab.org/jsp/smoothness"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%> 
<%@taglib prefix="dtm" uri="http://jlab.org/dtm/functions"%>
<c:set var="title" value="Expert List"/>
<t:setup-page title="${title}">  
    <jsp:attribute name="stylesheets">
        <style type="text/css">
            .dialog textarea {
                width: 20em;
                height: 8em;
            }
            #update-checklistUrl,
            #create-checklistUrl {
                width: 20em;
            }
            #no-experts-defined {
                margin-top: 1em;
            }
        </style>
    </jsp:attribute>
    <jsp:attribute name="scripts"> 
        <script type="text/javascript" src="${pageContext.request.contextPath}/resources/v${initParam.resourceVersionNumber}/js/subsystem-expert.js"></script>     
    </jsp:attribute>        
    <jsp:body>
        <section>          
            <c:choose>
                <c:when test="${selectedSystem eq null}">
                    <h2 id="page-header-title"><c:out value="${title}"/></h2> <span style="font-weight: bold;">(<span class="required-field"></span> required)
                        <form id="filter-form" action="subsystem-expert" method="get">
                            <div id="filter-form-panel">
                                <fieldset>
                                    <jsp:include page="/WEB-INF/includes/expert-report-form.jsp"/>
                                </fieldset>
                            </div>
                            <input type="submit" style="margin-top: 1em;" value="Apply"/>
                        </form>                        
                        <div class="message-box">Select a subsystem to continue</div>
                    </c:when>                                
                    <c:otherwise>
                        <s:filter-flyout-widget requiredMessage="true" clearButton="true">
                            <form id="filter-form" action="subsystem-expert" method="get">
                                <div id="filter-form-panel">
                                    <fieldset>
                                        <legend>Filter</legend>
                                        <jsp:include page="/WEB-INF/includes/expert-report-form.jsp"/>
                                    </fieldset>
                                </div>
                                <input type="submit" id="filter-form-submit-button" value="Apply"/>
                            </form>
                        </s:filter-flyout-widget>
                        <h2 id="page-header-title"><c:out value="${title}"/></h2>                      
                        <div class="message-box"><c:out value="${selectionMessage}"/></div>
                        <s:editable-row-table-controls excludeEdit="true"/>
                        <table id="expert-table" class="data-table stripped-table uniselect-table editable-row-table">
                            <thead>
                                <tr>
                                    <th>Name</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="se" items="${selectedSystem.systemExpertList}">
                                    <tr data-expert-id="${se.systemExpertId}" data-staff-id="${se.expert.staffId}">
                                        <td><c:out value="${dtm:formatStaff(se.expert)}"/></td>
                                    </tr>
                                </c:forEach>
                            </tbody>
                        </table>
                    </c:otherwise>
                </c:choose>
                <s:editable-row-table-dialog>
                    <form id="row-form">
                        <ul class="key-value-list">
                            <li>
                                <div class="li-key">
                                    Subsystem:
                                </div>
                                <div class="li-value">
                                    <span class="system-placeholder"></span>
                                </div>
                            </li>                          
                            <li>
                                <div class="li-key">
                                    <label for="username">Username</label>
                                </div>
                                <div class="li-value">
                                    <input type="text" id="username" class="username-autocomplete"/>
                                </div>
                            </li>                                                 
                        </ul>
                    </form>
                </s:editable-row-table-dialog>
        </section>
    </jsp:body>         
</t:setup-page>