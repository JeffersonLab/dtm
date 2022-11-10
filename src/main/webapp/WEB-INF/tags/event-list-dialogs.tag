<%@tag description="The Incident Table Template Tag" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%> 
<%@taglib prefix="s" uri="http://jlab.org/jsp/smoothness"%>
<%@attribute name="eventTypeList" required="true" type="java.util.List"%>
<%@attribute name="systemList" required="true" type="java.util.List"%>
<div id="incident-dialog" class="dialog" title="Add/Edit Incident">
    <form method="post" action="incident-action">
        <div id="incident-form-tabs">
            <ul>
                <li><a href="#problem-tab">Observed</a></li>
                <li><a href="#solution-tab">Explained</a></li>
            </ul>
            <div id="problem-tab">
                <fieldset>
                    <legend>Event Information</legend>
                    <ul class="key-value-list">             
                        <li>
                            <div class="li-key">
                                <label class="required-field" for="incident-dialog-event-title">Title</label>
                            </div>
                            <div class="li-value">
                                <input type="text" id="incident-dialog-event-title" name="event-title" maxlength="128" class="long-title-input"/>
                                <span id="append-checkbox-field" class="incident-dialog-existing-li"><input type="checkbox" id="append-title-checkbox" name="append-title-checkbox" value="append"/> Append Incident Title</span>
                            </div>
                        </li>
                        <li class="incident-dialog-new-li">
                            <div class="li-key">
                                <label class="required-field" for="incident-dialog-event-type">Type</label>
                            </div>
                            <div class="li-value">
                                <select id="incident-dialog-event-type" name="event-type">
                                    <option value=""> </option>
                                    <c:forEach items="${eventTypeList}" var="eventType">
                                        <option value="${eventType.eventTypeId}"><c:out value="${eventType.name}"/></option>
                                    </c:forEach>
                                </select>
                            </div>
                        </li>
                        <li class="incident-dialog-new-li">
                            <div class="li-key">
                                <label for="incident-dialog-event-time-up">Time Up</label>
                            </div>
                            <div class="li-value">
                                <input type="text" class="date-field nowable-field" id="incident-dialog-event-time-up" name="event-time-up" placeholder="DD-MMM-YYYY hh:mm"/>
                            </div>
                        </li>            
                        <li class="incident-dialog-existing-li-none">
                            <div class="li-key">
                                <label class="required-field" for="incident-dialog-event-id">Event ID</label>
                            </div>
                            <div class="li-value">
                                <input type="text" id="incident-dialog-event-id" name="event-id" readonly="readonly"/>
                            </div>
                        </li>            
                    </ul>
                </fieldset>       
                <fieldset>
                    <legend>Incident Description</legend>
                    <ul class="key-value-list">
                        <li>
                            <div class="li-key">
                                <label class="required-field" for="title">Title</label>
                            </div>
                            <div class="li-value">
                                <input type="text" id="title" name="title" maxlength="128" class="long-title-input"/>
                                <span class="incident-dialog-new-li"><input type="checkbox" id="copy-title-checkbox" name="copy-title-checkbox" value="copy"/> Same as Event</span>
                            </div>
                        </li>      
                        <li>
                            <div class="li-key">
                                <label class="required-field" for="summary">Summary</label>
                            </div>
                            <div class="li-value">
                                <textarea id="summary" maxlength="2048"></textarea>
                            </div>
                        </li>
                    </ul>
                </fieldset>  
                <fieldset>
                    <legend>Incident Period</legend>
                    <ul class="key-value-list">
                        <li>
                            <div class="li-key">
                                <label class="required-field" for="time-down">Time Down</label>
                            </div>
                            <div class="li-value">
                                <input type="text" class="date-field nowable-field" id="time-down" name="time-down" placeholder="DD-MMM-YYYY hh:mm"/>
                            </div>
                        </li>
                        <li>
                            <div class="li-key">
                                <label for="time-up">Time Up</label>
                            </div>
                            <div class="li-value">
                                <input type="text" class="date-field nowable-field" id="time-up" name="time-up" placeholder="DD-MMM-YYYY hh:mm"/>
                            </div>
                        </li>                
                    </ul>
                </fieldset>
                <fieldset>
                    <legend>Incident Cause</legend>
                    <div id="start-with-panel">
                        <input id="start-with-checkbox" type="checkbox"/>Filter by Category/System
                    </div>                 
                    <ul class="key-value-list">                 
                        <li>
                            <div class="li-key">
                                <label class="category-start-item" for="category">Category</label>
                            </div>
                            <div class="li-value">
                                <select class="category-start-item" id="category" name="category">
                                    <option value=""> </option>
                                    <c:forEach items="${categoryRoot.children}" var="child">
                                        <t:hierarchical-select-option node="${child}" level="0" parameterName="categoryId"/>
                                    </c:forEach>
                                </select>
                                <select class="category-cache" id="cebaf-select">
                                    <option value="1">CEBAF</option>
                                    <c:forEach items="${cebafRoot.children}" var="child">
                                        <t:hierarchical-select-option node="${child}" level="1" parameterName="categoryId"/>
                                    </c:forEach>
                                </select>
                                <select class="category-cache" id="lerf-select">
                                    <option value="2">LERF</option>
                                    <c:forEach items="${lerfRoot.children}" var="child">
                                        <t:hierarchical-select-option node="${child}" level="1" parameterName="categoryId"/>
                                    </c:forEach>
                                </select>
                                <select class="category-cache" id="other-select">
                                    <option value="3">Other</option>
                                    <c:forEach items="${otherRoot.children}" var="child">
                                        <t:hierarchical-select-option node="${child}" level="1" parameterName="categoryId"/>
                                    </c:forEach>
                                </select>
                                <select class="category-cache" id="cryo-select">
                                    <option value="4">Cryo</option>
                                    <c:forEach items="${cryoRoot.children}" var="child">
                                        <t:hierarchical-select-option node="${child}" level="1" parameterName="categoryId"/>
                                    </c:forEach>
                                </select>
                                <select class="category-cache" id="facilities-select">
                                    <option value="5">Facilities</option>
                                    <c:forEach items="${facilitiesRoot.children}" var="child">
                                        <t:hierarchical-select-option node="${child}" level="1" parameterName="categoryId"/>
                                    </c:forEach>
                                </select>
                                <select class="category-cache" id="hall-select">
                                    <option value="465">Hall Downtime</option>
                                    <c:forEach items="${hallRoot.children}" var="child">
                                        <t:hierarchical-select-option node="${child}" level="1" parameterName="categoryId"/>
                                    </c:forEach>
                                </select>
                                <span id="category-indicator" class="form-control-indicator category-start-item"></span>
                            </div>
                        </li>                
                        <li>
                            <div class="li-key">
                                <label class="category-start-item" for="system">System</label>
                            </div>
                            <div class="li-value">
                                <select id="system" name="system" class="category-start-item">
                                    <option value=""> </option>
                                    <c:forEach items="${systemList}" var="system">
                                        <option value="${system.systemId}" data-category-id="${system.category.categoryId}"><c:out value="${system.name}"/></option>
                                    </c:forEach>
                                </select>
                                <span id="system-indicator" class="form-control-indicator category-start-item"></span>
                            </div>
                        </li>                                      
                        <li>
                            <div class="li-key">
                                <label class="required-field" for="component">Component</label>
                            </div>
                            <div class="li-value">
                                <input type="text" id="component" name="component" placeholder="search for name"/>
                                <button id="list-system-components-button" title="List components in selected System" type="button" class="ui-button category-start-item"><span class="ui-icon-primary ui-icon ui-icon-triangle-1-s"></span></button>
                                <span id="component-indicator" class="form-control-indicator"></span>
                            </div>
                        </li>
                    </ul>
                </fieldset>     
            </div>
            <div id="solution-tab">
                <fieldset>
                    <legend>Subject Matter Expert (SME) Review</legend>
                    <ul class="key-value-list"> 
                        <li>
                            <div class="li-key">
                                <label>Review Level</label>
                            </div>
                            <div class="li-value">
                                <span id="edit-incident-dialog-review-level"></span>
                            </div>
                        </li>                        
                        <li>
                            <div class="li-key">
                                <label for="edit-incident-dialog-sys-reviewer">Reviewer(s)</label>
                            </div>
                            <div class="li-value">
                                <input type="text" id="edit-incident-dialog-sys-reviewer" class="" maxlength="256" placeholder="usernames"/>
                                (space separated)
                            </div>
                        </li>     
                        <li>
                            <div class="li-key">
                                <label>Acknowledged</label>
                            </div>
                            <div class="li-value">
                                <span id="edit-incident-dialog-acknowledged"></span>
                            </div>
                        </li>                        
                        <li>
                            <div class="li-key">
                                <label>Root Cause</label>
                            </div>
                            <div class="li-value">
                                <span id="edit-incident-dialog-root-cause"></span>
                            </div>
                        </li>
                    </ul>
                </fieldset>                 
                <fieldset>
                    <legend>Operability (OPR) Review</legend>
                    <ul class="key-value-list">  
                        <li>
                            <div class="li-key">
                                <label for="reviewed-by">Reviewer</label>
                            </div>
                            <div class="li-value">
                                <input type="text" id="reviewed-by" class="username-autocomplete" maxlength="128" placeholder="username"/>
                                <span> </span>
                                <button type="button" class="me-button">Me</button>
                            </div>
                        </li>         
                        <li>
                            <div class="li-key">
                                <label for="repaired-by">Repairer</label>
                            </div>
                            <div class="li-value">
                                <select id="repaired-by" multiple="multiple">
                                    <c:forEach items="${groupList}" var="group">
                                        <option value="${group.workgroupId}"${s:inArray(incident.repairedByList.toArray(), group) ? ' selected="selected"' : ''}><c:out value="${group.name}"/></option>
                                    </c:forEach>
                                </select>
                            </div>
                        </li>                        
                        <li>
                            <div class="li-key">
                                <label for="solution">Solution</label>
                            </div>
                            <div class="li-value">
                                <textarea id="solution" maxlength="2048"></textarea>
                            </div>
                        </li>
                    </ul>
                </fieldset>               
            </div>
        </div>
        <div class="dialog-button-panel">
            <c:if test="${sessionScope.effectiveRole ne 'REVIEWER'}">
                <div id="elog-note-panel">
                    <span>Note: An Elog will be generated for Create/Close incident actions</span>
                </div>
            </c:if>
            <button id="incident-action-button" class="dialog-submit" type="button">Save</button>
            <button class="dialog-close-button" type="button">Cancel</button>
        </div>
        <input type="hidden" id="incident" name="incident"/>
        <input type="hidden" id="explanation" name="explanation"/>
    </form>
</div>
<div id="event-dialog" class="dialog" title="Edit Event">
    <form method="post" action="edit-event">
        <ul class="key-value-list">  
            <li>
                <div class="li-key">
                    <label class="required-field" for="event-dialog-event-title">Title</label>
                </div>
                <div class="li-value">
                    <input type="text" id="event-dialog-event-title" name="event-title" maxlength="128" class="long-title-input"/>
                </div>
            </li>
            <li>
                <div class="li-key">
                    <label class="required-field" for="event-dialog-event-type">Type</label>
                </div>
                <div class="li-value">
                    <select id="event-dialog-event-type" name="event-type">
                        <option value=""> </option>
                        <c:forEach items="${eventTypeList}" var="eventType">
                            <option value="${eventType.eventTypeId}"><c:out value="${eventType.name}"/></option>
                        </c:forEach>
                    </select>
                </div>
            </li>            
            <li>
                <div class="li-key">
                    <label for="event-time-up">Time Up</label>
                </div>
                <div class="li-value">
                    <input type="text" class="date-field nowable-field" id="event-time-up" name="time-up" placeholder="DD-MMM-YYYY hh:mm"/>
                </div>
            </li>                    
        </ul>
        <div class="dialog-button-panel">
            <button id="edit-event-button" class="dialog-submit" type="button">Save</button>
            <button class="dialog-close-button" type="button">Cancel</button>
        </div>
        <input type="hidden" id="event" name="event"/>
    </form>
</div>
<div id="edit-expert-review-dialog" class="dialog" title="Edit Subject Matter Expert Review">
    <span style="float: right;">(<span class="required-field"></span> required)</span>
        <ul class="key-value-list">
            <li>
                <div class="li-key">
                    <label>Review Level</label>
                </div>
                <div class="li-value">
                    <span id="review-level"></span>
                </div>
            </li>             
            <li>
                <div class="li-key">
                    <label>Reviewer(s)</label>
                </div>
                <div class="li-value">
                    <div id="edit-expert-reviewers"></div>
                </div>
            </li>            
            <li>
                <div id="acknowledged-key" class="li-key">
                    <label>Acknowledged</label>
                    <br/>(Level Ⅰ)
                </div>
                <div class="li-value acknowledge-radios">
                    <input id="acknowledged-no" checked="checked" name="acknowledged" value="N" type="radio"/>
                    <span>No</span>
                    <input id="acknowledged-yes" name="acknowledged" value="Y" type="radio"/>
                    <span>Yes</span>
                    <input id="acknowledged-reassign" name="acknowledged" value="R" type="radio"/>
                    <span>Reassign</span>
                </div>
            </li>
            <li>
                <div class="li-key">
                    <label for="root-cause">Root Cause</label>
                    <br/>(Level Ⅱ)
                </div>
                <div class="li-value">
                    <textarea id="root-cause" maxlength="512" name="root"></textarea>
                </div>
            </li>
            <li>
                <div class="li-key">
                    <label>Repair<br/>Assessment<br/>Report [RAR]</label>
                    <br/>(Level Ⅲ+)
                </div>
                <div class="li-value">
                    <span id="rar-link"></span>
                    <form id="file-upload-form" method="post" action="../ajax/rar-upload" enctype="multipart/form-data">
                        <p><input id="file-upload-input" type="file" name="rar" accept=".pdf"/></p>
                    </form>
                </div>
            </li>            
        </ul>
        <div class="dialog-button-panel">
            <button id="edit-expert-review-button" class="dialog-submit" type="button">Save</button>
            <button class="dialog-close-button" type="button">Cancel</button>
        </div>
        <input type="hidden" id="root-cause-dialog-incident-id" name="incident"/>
</div>
<div id="review-dialog" class="dialog" title="Review">    
    <fieldset>
        <legend>Subject Matter Expert (SME)</legend>
        <ul class="key-value-list"> 
            <li>
                <div class="li-key">
                    <label>Review Level</label>
                </div>
                <div class="li-value">
                    <span id="review-dialog-review-level"></span>
                </div>
            </li>            
            <li>
                <div class="li-key">
                    <label>Reviewer(s)</label>
                </div>
                <div class="li-value">
                    <div id="review-dialog-sys-reviewer">
                        <div>Example user 1</div>
                        <div>Example user 2</div>                      
                    </div>
                </div>
            </li>      
            <li>
                <div class="li-key">
                    <label>Acknowledged</label>
                </div>
                <div class="li-value">
                    <span id="review-dialog-acknowledged"></span>
                </div>
            </li>             
            <li>
                <div class="li-key">
                    <label>Root Cause</label>
                </div>
                <div class="li-value">
                    <span id="review-dialog-root-cause"></span>
                </div>
            </li>
            <li>
                <div class="li-key">
                    <label>Repair<br/>Assessment<br/>Report [RAR]</label>
                </div>
                <div class="li-value">
                    <span id="review-dialog-rar-link"></span>
                </div>
            </li>            
        </ul>
    </fieldset>
    <fieldset>
        <legend>Operability (OPR)</legend>
        <ul class="key-value-list">  
            <li>
                <div class="li-key">
                    <label>Reviewer</label>
                </div>
                <div class="li-value">
                    <span id="review-dialog-ops-reviewer"></span>
                </div>
            </li>            
            <li>
                <div class="li-key">
                    <label>Repairer</label>
                </div>
                <div class="li-value">
                    <span id="review-dialog-repairer"></span>
                </div>
            </li>
            <li>
                <div class="li-key">
                    <label>Solution</label>
                </div>
                <div class="li-value">
                    <span id="review-dialog-solution"></span>
                </div>
            </li>            
        </ul>        
    </fieldset>
</div>