<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@taglib prefix="s" uri="http://jlab.org/jsp/smoothness" %>
<%@taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="dtm" uri="http://jlab.org/dtm/functions" %>
<c:set var="title" value="Settings"/>
<t:setup-page title="${title}">  
    <jsp:attribute name="stylesheets">
    </jsp:attribute>
    <jsp:attribute name="scripts">
        <script>
            jlab.editableRowTable = jlab.editableRowTable || {};
            jlab.editableRowTable.entity = 'Setting';
            jlab.editableRowTable.dialog.width = 400;
            jlab.editableRowTable.dialog.height = 300;
            jlab.editRow = function() {
                var key = $("#row-key").val(),
                    value = $("#row-value").val(),
                    reloading = false;

                $(".dialog-submit-button")
                    .height($(".dialog-submit-button").height())
                    .width($(".dialog-submit-button").width())
                    .empty().append('<div class="button-indicator"></div>');
                $(".dialog-close-button").attr("disabled", "disabled");
                $(".ui-dialog-titlebar button").attr("disabled", "disabled");

                var request = jQuery.ajax({
                    url: "/jaws/setup/ajax/edit-setting",
                    type: "POST",
                    data: {
                        key: key,
                        value: value
                    },
                    dataType: "json"
                });

                request.done(function(json) {
                    if (json.stat === 'ok') {
                        reloading = true;
                        window.location.reload();
                    } else {
                        alert(json.error);
                    }
                });

                request.fail(function(xhr, textStatus) {
                    window.console && console.log('Unable to edit setting; Text Status: ' + textStatus + ', Ready State: ' + xhr.readyState + ', HTTP Status Code: ' + xhr.status);
                    alert('Unable to Save: Server unavailable or unresponsive');
                });

                request.always(function() {
                    if (!reloading) {
                        $(".dialog-submit-button").empty().text("Save");
                        $(".dialog-close-button").removeAttr("disabled");
                        $(".ui-dialog-titlebar button").removeAttr("disabled");
                    }
                });
            };
            $(document).on("click", ".default-clear-panel", function () {
                $("#key").val('');
                $("#tag-select").val('');
                return false;
            });
            $(document).on("click", "#open-edit-row-dialog-button", function() {
                var $selectedRow = $(".editable-row-table tr.selected-row");
                $("#row-key").text($selectedRow.find("td:first-child div:nth-child(2)").text());
                $("#row-value").val($selectedRow.find("td:nth-child(3)").text());
            });
            $(document).on("table-row-edit", function() {
                jlab.editRow();
            });
        </script>
    </jsp:attribute>
    <jsp:body>
        <section>
            <s:filter-flyout-widget clearButton="true">
                <form id="filter-form" action="settings" method="get">
                    <div id="filter-form-panel">
                        <fieldset>
                            <legend>Filter</legend>
                            <ul class="key-value-list">
                                <li>
                                    <div class="li-key">
                                        <label for="key">Key</label>
                                    </div>
                                    <div class="li-value">
                                        <input type="text" id="key" name="key" value="${fn:escapeXml(param.key)}"/>
                                    </div>
                                </li>
                                <li>
                                    <div class="li-key">
                                        <label for="tag-select">Tag</label>
                                    </div>
                                    <div class="li-value">
                                        <select id="tag-select" name="tag">
                                            <option value=""> </option>
                                            <c:forEach items="${tagList}" var="tag">
                                                <option value="${tag}"${(param.tag eq tag) ? ' selected="selected"' : ''}><c:out value="${tag}"/></option>
                                            </c:forEach>
                                        </select>
                                    </div>
                                </li>
                            </ul>
                        </fieldset>
                    </div>
                    <input type="submit" id="filter-form-submit-button" value="Apply"/>
                </form>
            </s:filter-flyout-widget>
            <h2 id="page-header-title"><c:out value="${title}"/></h2>
            <div class="message-box"><c:out value="${selectionMessage}"/></div>
            <s:editable-row-table-controls excludeAdd="true" excludeDelete="true"/>
            <table id="settings-table" class="data-table stripped-table uniselect-table editable-row-table">
                <thead>
                <tr>
                    <th>Tag / Key</th>
                    <th>Type / Description</th>
                    <th>Value</th>
                </tr>
                </thead>
                <tbody>
                <c:forEach var="setting" items="${settingList}">
                    <tr>
                        <td><div><c:out value="${setting.tag}"/> /</div><div><c:out value="${setting.key}"/></div></td>
                        <td><div><c:out value="${setting.type}"/> /</div><div><c:out value="${setting.description}"/></div></td>
                        <td><c:out value="${setting.value}"/></td>
                    </tr>
                </c:forEach>
                </tbody>
            </table>
            <s:editable-row-table-dialog>
                <form id="row-form">
                    <ul class="key-value-list">
                        <li>
                            <div class="li-key">
                                Key:
                            </div>
                            <div class="li-value">
                                <span id="row-key"></span>
                            </div>
                        </li>
                        <li>
                            <div class="li-key">
                                <label for="row-value">Value</label>
                            </div>
                            <div class="li-value">
                                <input type="text" id="row-value" autocomplete="off"/>
                            </div>
                        </li>
                    </ul>
                </form>
            </s:editable-row-table-dialog>
        </section>
    </jsp:body>
</t:setup-page>