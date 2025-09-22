var jlab = jlab || {};
jlab.dtm = jlab.dtm || {};

jlab.dtm.acknowledgedMap = {N: 'No', Y: 'Yes', R: 'Reassign'};

jlab.dtm.initAccordionPanel = function () {
    $(this).addClass("ui-accordion-header ui-helper-reset ui-state-default ui-corner-top ui-corner-bottom")
            .hover(function () {
                $(this).toggleClass("ui-state-hover");
            })
            .click(function () {
                $(this).toggleClass("ui-accordion-header-active ui-state-active ui-state-default ui-corner-bottom")
                        .find("> .event-header-toggle .ui-icon").toggleClass("ui-icon-triangle-1-e ui-icon-triangle-1-s").end()
                        .next().toggleClass("ui-accordion-content-active").slideToggle();
                return false;
            })
            .next()
            .addClass("ui-accordion-content ui-helper-reset ui-widget-content ui-corner-bottom")
            .hide();

    $(this).find(".event-header-toggle").prepend('<span class="ui-icon ui-icon-triangle-1-e"></span>');

    $(this).click();
};

jlab.dtm.updateEventInPlace = function (eventId) {

    var request = jQuery.ajax({
        url: "/dtm/all-events",
        type: "GET",
        data: {
            eventId: eventId
        },
        dataType: "html"
    });

    request.done(function (data) {
        var reload = false;

        if ($(".always-refresh-page").length > 0) {
            reload = true;
        } else if (!eventId) {
            reload = true;
        } else {
            var $oldHeader = $("#header-" + eventId);
            var $oldContent = $("#content-" + eventId);
            var $newHeader = $("#header-" + eventId, data);
            var $newContent = $("#content-" + eventId, data);

            if ($newHeader.length > 0) {
                /*window.console && console.log('replacing');*/
                $oldHeader.replaceWith($newHeader);
                $oldContent.replaceWith($newContent);
                jlab.dtm.initAccordionPanel.call($newHeader);

                if(jlab.logbookEnabled) {
                    $(".incident-table tbody tr", $newContent).each(function () {
                        jlab.dtm.loadLogbookReference.call(this);
                    });
                }
            } else {
                reload = true;
            }
        }

        if (reload) {
            /*window.console && console.log('reloading');*/
            document.location.reload(true);
        }
    });

    request.fail(function (xhr, textStatus) {
        window.console && console.log('Unable to Update Event: Text Status: ' + textStatus + ', Ready State: ' + xhr.readyState + ', HTTP Status Code: ' + xhr.status);
        alert('Unable to Update Event: server did not handle request');
    });

    request.always(function () {
    });
};

jlab.dtm.validateIncidentForm = function (action) {

    if ($("#incident-dialog-event-title").val() === '') {
        alert('Please specify an event title');
        return false;
    }

    if ('add-event' === action) {
        if ($("#incident-dialog-event-type").val() === '') {
            alert('Please specify a type');
            return false;
        }
    }

    if ($("#title").val() === '') {
        alert('Please specify an incident title');
        return false;
    }

    if ($("#summary").val() === '') {
        alert('Please specify an incident summary');
        return false;
    }

    if ($("#time-down").val() === '') {
        alert('Please specify an incident time down');
        return false;
    }

    if ($("#component").val() === '') {
        alert('Please select a component');
        return false;
    }

    if ($("#incident-dialog-event-type").val() === '1' && ($("#component").val() === 'End Station Procedure' || $("#component").val() === 'Controlled Access' || $("#component").val() === 'Hardware Failure')) {
        return confirm("Hall Downtime should only be part of hall events, not accelerator events.   Continue anyways (this may make Randy angry)?");
    }

    return true;
};
jlab.dtm.doIncidentAction = function (reload) {

    if (jlab.isRequest()) {
        window.console && console.log("Ajax already in progress");
        return;
    }

    var incidentId = $("#incident").val(),
            eventTitle = $("#incident-dialog-event-title").val(),
            eventId = $("#incident-dialog-event-id").val(),
            eventTypeId = $("#incident-dialog-event-type").val(),
            eventTimeUp = $("#incident-dialog-event-time-up").val(),
            timeDown = $("#time-down").val(),
            timeUp = $("#time-up").val(),
            title = $("#title").val(),
            summary = $("#summary").val(),
            permitToWork = $("#permit-to-work").val(),
            research = $("#research").val(),
            halla = $("#halla").is(":checked"),
            hallb = $("#hallb").is(":checked"),
            hallc = $("#hallc").is(":checked"),
            halld = $("#halld").is(":checked"),
            componentId = $("#component").attr("data-component-id"),
            componentName = $("#component").val(),
            action = $("#incident-action-button").attr("data-action"),
            explanation = $("#explanation").val(),
            solution = $("#solution").val(),
            repairedByArray = $("#repaired-by").val(),
            reviewedBy = $("#reviewed-by").val(),
            expertUsernames = $("#edit-incident-dialog-sys-reviewer").val(),
            rarId = $("#edit-incident-dialog-rar-id").val();

    if (eventTimeUp === "  -   -       :  ") {
        window.console && console.log("timepicker input placeholder mask is erroneously set as eventTimeUp value!");
        eventTimeUp = '';
    }

    if (timeDown === "  -   -       :  ") {
        window.console && console.log("timepicker input placeholder mask is erroneously set as incidentTimeDown value!");
        timeDown = '';
    }

    if (timeUp === "  -   -       :  ") {
        window.console && console.log("timepicker input placeholder mask is erroneously set as incidentTimeUp value!");
        timeUp = '';
    }

    if(parseInt(eventTypeId) === 8 && !halla && !hallb && !hallc && !halld) {
        alert('You must select at least one hall with a Hall Event Type');
        return;
    }

    if (!jlab.dtm.validateIncidentForm(action)) {
        return;
    }

    if (eventTypeId > 1 && eventTypeId < 6 && ('add-event' === action || 'add-incident' === action)) {
        alert("REMINDER: Don't forget to set BOOM to BNR mode, if applicable!");
    }

    if (componentName === '') {
        componentId = '';
    }

    if (componentName === 'Unknown/Missing') {
        explanation = prompt('Please explain the component that is Unknown/Missing', explanation);

        if (explanation === null || explanation === '') {
            return;
        }
    } else {
        explanation = null;
    }
    
    var expertUsernameArray = expertUsernames.split(' ');

    jlab.requestStart();

    $("#incident-action-button").html("<span class=\"button-indicator\"></span>");
    $("#incident-action-button").attr("disabled", "disabled");

    var leaveSpinning = false;

    var friendlyActionName = action.replace("-", " ");

    var request = jQuery.ajax({
        url: "/dtm/ajax/incident-action",
        type: "POST",
        data: {
            incidentId: incidentId,
            eventId: eventId,
            eventTitle: eventTitle,
            eventTypeId: eventTypeId,
            eventTimeUp: eventTimeUp,
            timeDown: timeDown,
            timeUp: timeUp,
            title: title,
            summary: summary,
            permitToWork: permitToWork,
            research: research,
            halla: halla ? 'Y' : 'N',
            hallb: hallb ? 'Y' : 'N',
            hallc: hallc ? 'Y' : 'N',
            halld: halld ? 'Y' : 'N',
            componentId: componentId,
            componentName: componentName,
            action: action,
            explanation: explanation,
            solution: solution,
            'repairedBy[]': repairedByArray,
            reviewedBy: reviewedBy,
            'expertUsername[]': expertUsernameArray
        },
        dataType: "html"
    });

    request.done(function (data) {
        if ($(".status", data).html() !== "Success") {
            alert('Unable to ' + friendlyActionName + ': ' + $(".reason", data).html());
        } else {
            /* Success */
            if (reload) {
                leaveSpinning = true;
                document.location.reload(true);
            } else {
                jlab.dtm.updateEventInPlace(eventId);
                $("#incident-dialog").dialog("close");
            }
        }

    });

    request.fail(function (xhr, textStatus) {
        window.console && console.log('Unable to ' + friendlyActionName + ': Text Status: ' + textStatus + ', Ready State: ' + xhr.readyState + ', HTTP Status Code: ' + xhr.status);
        alert('Unable to ' + friendlyActionName + ': server did not handle request');
    });

    request.always(function () {
        jlab.requestEnd();
        if (!leaveSpinning) {
            $("#incident-action-button").html("Save");
            $("#incident-action-button").removeAttr("disabled");
        }
    });
};
jlab.dtm.removeIncident = function () {

    if (jlab.isRequest()) {
        window.console && console.log("Ajax already in progress");
        return;
    }

    jlab.requestStart();

    var incidentId = $(this).closest("tr").attr("data-incident-id"),
            $removeButton = $(this),
            eventId = $(this).closest(".event-detail").attr("data-event-id");

    $removeButton.html("<span class=\"button-indicator\"></span>");
    $removeButton.attr("disabled", "disabled");

    var leaveSpinning = false;

    var request = jQuery.ajax({
        url: "/dtm/ajax/remove-incident",
        type: "POST",
        data: {
            incidentId: incidentId
        },
        dataType: "html"
    });

    request.done(function (data) {
        if ($(".status", data).html() !== "Success") {
            alert('Unable to delete incident: ' + $(".reason", data).html());
        } else {
            /* Success */
            /*leaveSpinning = true;
             document.location.reload(true);*/
            jlab.dtm.updateEventInPlace(eventId);
        }

    });

    request.fail(function (xhr, textStatus) {
        window.console && console.log('Unable to delete incident: Text Status: ' + textStatus + ', Ready State: ' + xhr.readyState + ', HTTP Status Code: ' + xhr.status);
        alert('Unable to delete incident: server did not handle request');
    });

    request.always(function () {
        jlab.requestEnd();
        if (!leaveSpinning) {
            $removeButton.html("Delete Incident");
            $removeButton.removeAttr("disabled");
        }
    });
};
jlab.dtm.uploadFile = function() {
    var incidentId = $("#root-cause-dialog-incident-id").val(),
        form = $("#file-upload-form")[0];

    return jlab.uploadRARFile(incidentId, form);
};
jlab.dtm.editSystemExpertReview = function () {

    if (jlab.isRequest()) {
        window.console && console.log("Ajax already in progress");
        return;
    }

    jlab.requestStart();

    var incidentId = $("#root-cause-dialog-incident-id").val(),
            acknowledged = $("input[name='acknowledged']:checked").val(),
            rootCause = $("#root-cause").val(),
            $saveButton = $("#edit-expert-review-button");

    $saveButton.html("<span class=\"button-indicator\"></span>");
    $saveButton.attr("disabled", "disabled");

    var leaveSpinning = false;

    var request = jQuery.ajax({
        url: "/dtm/ajax/edit-system-expert-review",
        type: "POST",
        data: {
            incidentId: incidentId,
            acknowledged: acknowledged,
            rootCause: rootCause
        },
        dataType: "html"
    });

    request.done(function (data) {
        if ($(".status", data).html() !== "Success") {
            alert('Unable to update incident review: ' + $(".reason", data).html());
        } else {
            /* Success */
            leaveSpinning = true;
            document.location.reload(true);       
            
            /*var $tr = $("tr[data-incident-id=" + incidentId + "]");
            $tr.attr("data-acknowledged", acknowledged);
            $tr.attr("data-root-cause", rootCause);
            $("#edit-expert-review-dialog").dialog("close");*/
        }

    });

    request.fail(function (xhr, textStatus) {
        window.console && console.log('Unable to edit root cause: Text Status: ' + textStatus + ', Ready State: ' + xhr.readyState + ', HTTP Status Code: ' + xhr.status);
        alert('Unable to edit root cause: server did not handle request');
    });

    request.always(function () {
        jlab.requestEnd();
        if (!leaveSpinning) {
            $saveButton.html("Save");
            $saveButton.removeAttr("disabled");
        }
    });
};
jlab.dtm.prepareIncidentFormForEdit = function (skipSystemListLoad) {
    var $table = $(this).closest("table"),
            $tr = $(this).closest("tr"),
            eventType = parseInt($table.attr("data-event-type-id")),
            timeDown = $tr.find(".incident-table-time-down").text(),
            timeUp = $tr.find(".incident-table-time-up").text(),
            title = $tr.find(".incident-table-title").text(),
            summary = $tr.find(".incident-table-summary").text(),
            permitToWork = $tr.find(".incident-table-permit-to-work").text(),
            research = $tr.find(".incident-table-research-affected").attr("data-research") === 'Y',
            halla = $tr.find(".incident-table-halls-affected").attr("data-halla") === 'Y',
            hallb = $tr.find(".incident-table-halls-affected").attr("data-hallb") === 'Y',
            hallc = $tr.find(".incident-table-halls-affected").attr("data-hallc") === 'Y',
            halld = $tr.find(".incident-table-halls-affected").attr("data-halld") === 'Y',
            systemId = $tr.find(".incident-table-system").attr("data-system-id"),
            componentName = $tr.find(".incident-table-component").text(),
            componentId = $tr.find(".incident-table-component").attr("data-component-id"),
            incident = $tr.attr("data-incident-id"),
            explanation = $tr.find(".explanation-link").attr("data-explanation"),
            solution = $tr.attr("data-solution"),
            repairedByIdCsv = $tr.attr("data-repaired-by-id-csv"),
            reviewedBy = $tr.attr("data-reviewed-by"),
            expertReviewers = $tr.attr("data-reviewed-by-username-ssv"),
            reviewLevel = $tr.attr("data-review-level"),
            acknowledged = $tr.attr("data-acknowledged"),
            rootCause = $tr.attr("data-root-cause"),
            rarId = $tr.attr("data-rar-id"),
            rarExt = $tr.attr("data-rar-ext");

    if(permitToWork === 'None') {
        permitToWork = '';
    }

    $("#research-li").removeClass("invisible");

    if(research) {
        $("#research").val("Y");
    } else {
        $("#research").val("N");
    }

    if(halla) {
        $("#halla").prop("checked", true);
    }

    if(hallb) {
        $("#hallb").prop("checked", true);
    }

    if(hallc) {
        $("#hallc").prop("checked", true);
    }

    if(halld) {
        $("#halld").prop("checked", true);
    }

    $("#category").val('');

    jlab.dtm.filterCategorySelect(eventType);

    if (skipSystemListLoad === true) {
        $("#system").empty();
        $("#system").append('<option selected="selected" value="' + String(systemId).encodeXml() + '"> </option>');
    } else {
        jlab.dtm.filterSystemSelect(systemId);
    }

    jlab.dtm.toggleMultiHall(eventType);
    jlab.dtm.toggleResearch(eventType);

    $("#incident-dialog-event-type").val(eventType);
    $("#incident-dialog-event-time-up").val('');
    $("#time-down").val(timeDown);
    $("#time-up").val(timeUp);
    $("#title").val(title);
    $("#summary").val(summary);
    $("#permit-to-work").val(permitToWork);
    $("#component").val(componentName);
    $("#component").attr("data-component-id", componentId);

    $("#incident-dialog-event-id").val('');

    $("#incident").val(incident);

    $("#explanation").val(explanation);

    $("#solution").val(solution);
    $("#repaired-by").val(repairedByIdCsv.split(",")).trigger("change");
    $("#reviewed-by").val(reviewedBy);
    $("#edit-incident-dialog-review-level").text(reviewLevel);
    $("#edit-incident-dialog-sys-reviewer").val(expertReviewers);
    $("#edit-incident-dialog-acknowledged").text(jlab.dtm.acknowledgedMap[acknowledged]);
    $("#edit-incident-dialog-root-cause").text(rootCause);

    $("#incident-action-button").attr("data-action", "edit-incident");
};
jlab.dtm.prepareEventFormForEdit = function () {
    var event = $(this).closest(".event-detail").attr("data-event-id");

    $("#event-dialog-event-title").val($(this).closest(".ui-accordion-content").prev().find(".accordion-event-title").text());
    $("#event-dialog-event-type").val($(this).closest(".ui-accordion-content").prev().find(".event-header-type").attr("data-type-id"));
    $("#event-time-up").val($(this).closest(".event-detail").attr("data-event-time-up"));

    //$("#event-time-up").datetimepicker("refresh");

    $("#event").val(event);
};
jlab.dtm.removeEvent = function () {

    if (jlab.isRequest()) {
        window.console && console.log("Ajax already in progress");
        return;
    }

    jlab.requestStart();

    var eventId = $(this).closest(".event-detail").attr("data-event-id"),
            $removeButton = $(this);

    $removeButton.html("<span class=\"button-indicator\"></span>");
    $removeButton.attr("disabled", "disabled");

    var leaveSpinning = false;

    var request = jQuery.ajax({
        url: "/dtm/ajax/remove-event",
        type: "POST",
        data: {
            eventId: eventId
        },
        dataType: "html"
    });

    request.done(function (data) {
        if ($(".status", data).html() !== "Success") {
            alert('Unable to delete event: ' + $(".reason", data).html());
        } else {
            /* Success */
            leaveSpinning = true;
            document.location.reload(true);
        }

    });

    request.fail(function (xhr, textStatus) {
        window.console && console.log('Unable to delete event: Text Status: ' + textStatus + ', Ready State: ' + xhr.readyState + ', HTTP Status Code: ' + xhr.status);
        alert('Unable to delete event: server did not handle request');
    });

    request.always(function () {
        jlab.requestEnd();
        if (!leaveSpinning) {
            $removeButton.html("Delete Event");
            $removeButton.removeAttr("disabled");
        }
    });
};
jlab.dtm.loadPanelLogbookReferences = function () {
    $(".incident-table tbody tr", this).each(function () {
        jlab.dtm.loadLogbookReference.call(this);
    });
};

jlab.dtm.loadLogbookReference = function () {

    var $tr = $(this),
            $td = $tr.find(".log-entry-cell .cell-subfield"),
            incidentId = $tr.attr("data-incident-id");

    $td.html("<span class=\"button-indicator\"></span>");

    /*window.console && console.log('Loading log entries for incident: ' + incidentId);*/

    var request = jQuery.ajax({
        url: "/dtm/data/references",
        type: "GET",
        data: {
            'id': incidentId
        },
        dataType: "json"
    });

    request.done(function (json) {
        if (json.stat === "fail") {
            console.log('Unable to load log entries: ' + json.error);
            $td.html('<span class="error-message">Unable to load</span>');
        } else {
            /* Success */
            $td.html(''); // clear indicator

            /* Filter out DTM generated logs */
            var filteredData = [];
            if (json.data.length > 0) {
                $(json.data).each(function () {
                    if (this.title.startsWith('Downtime Incident CREATED: ') || this.title.startsWith('Downtime Incident CLOSED: ') || this.title.startsWith('Downtime Incident REVISED: ')) {
                        return true; // continue
                    }
                    filteredData.push(this);
                });
            }

            if (filteredData.length > 0) {
                $td.append('<ul class="table-cell-list"></ul>');
                var $list = $td.find('.table-cell-list');
                $(filteredData).each(function () {
                    $list.append('<li class="table-cell-list-item"><a title="' + this.title.encodeXml() + '" target="_blank" href="' + jlab.logbookServerUrl + '/entry/' + this.lognumber + '">' + this.lognumber + '</a><button title="Unlink Log Entry (Coming soon...)" class="unlink-log-entry-button" type="button" style="display:none;">-</button></li>');
                });
            }
        }

    });

    request.fail(function (xhr, textStatus) {
        window.console && console.log('Unable to log log entries: Text Status: ' + textStatus + ', Ready State: ' + xhr.readyState + ', HTTP Status Code: ' + xhr.status);
        $td.html('<span class="error-message">Unable to load</span>');
    });

    request.always(function () {
    });
};
jlab.dtm.toggleMultiHall = function (type) {
    if(jlab.affectedHallTypes.includes(type)) {
        $("#halls-affected-fieldset").show();
    } else {
        $("#halls-affected-fieldset").hide();
    }
};
jlab.dtm.toggleResearch = function (type) {
    if(jlab.affectedResearchTypes.includes(type)) {
        $("#research-li").removeClass("invisible");
    } else {
        $("#research-li").addClass("invisible");
    }
};
jlab.dtm.filterSystemSelect = function (setToSystemId) {
    if (jlab.isRequest()) {
        window.console && console.log("Ajax already in progress");
        return;
    }

    var type = parseInt($("#incident-dialog-event-type").val());

    jlab.requestStart();

    $("#system-indicator").html("<span class=\"button-indicator\"></span>");

    var params = {};

    if ($("#category").val() !== null && $("#category").val() !== '') {
        params = {
            category_id: $("#category").val(),
            application_id: 2
        };
    } else {
        params = {
            application_id: 2
        };

        jQuery.ajaxSettings.traditional = true; /*array bracket serialization*/

        if(!Number.isNaN(type)) {
            params.category_id = jlab.typeCategoryMap.get(type);
        }
    }

    var request = jQuery.ajax({
        url: "/dtm/data/systems",
        type: "GET",
        data: params,
        dataType: "json"
    });

    request.done(function (json) {
        if (json.stat === "fail") {
            alert('Unable to filter system select: ' + json.error);
        } else {
            /* Success */
            var currentSelection = $("#system").val();
            $("#system").empty();
            $("#system").append('<option value=""> </option>');
            $(json.data).each(function () {
                $("#system").append('<option value="' + String(this.id).encodeXml() + '">' + String(this.name).encodeXml() + '</option>');
            });
            if (json.data.length === 1) {
                $("#system").val(json.data[0].id);
            } else if (setToSystemId === undefined) {
                $("#system").val(currentSelection);
            } else {
                $("#system").val(setToSystemId);
            }
        }
    });

    request.fail(function (xhr, textStatus) {
        window.console && console.log('Unable to filter system select: Text Status: ' + textStatus + ', Ready State: ' + xhr.readyState + ', HTTP Status Code: ' + xhr.status);
        alert('Unable to filter system select: server did not handle request');
    });

    request.always(function () {
        jlab.requestEnd();
        $("#system-indicator").html('');
    });
};
jlab.dtm.filterCategorySelect = function(type) {
    $("#category").empty();
    $("#category").append('<option value=""> </option>');
    if(!Number.isNaN(type)) {
        let categoryRootIdArray = jlab.typeCategoryMap.get(type);
        if (categoryRootIdArray) {
            categoryRootIdArray.forEach((id) => {
                $("#category").append($("#category-cache-" + id).clone().children());
            });
        }
    }

    $("#category").val("");
};
jlab.dtm.selectCategoryBasedOnSystem = function (refreshSystemList) {
    if (jlab.isRequest()) {
        window.console && console.log("Ajax already in progress");
        return;
    }

    var success = false,
            params = {};

    if ($("#system").val() !== null && $("#system").val() !== '') {
        params = {
            system_id: $("#system").val(),
            application_id: 2
        };
    } else {
        return;
    }

    jlab.requestStart();

    $("#category-indicator").html("<span class=\"button-indicator\"></span>");

    var request = jQuery.ajax({
        url: "/dtm/data/systems",
        type: "GET",
        data: params,
        dataType: "json"
    });

    request.done(function (json) {
        if (json.stat === "fail") {
            alert('Unable to filter category select: ' + json.error);
        } else {
            /* Success */
            success = true;
            if(json.data[0]) { /*If non-empty result set*/
                $("#category").val(json.data[0].category_id);
            }
        }
    });

    request.fail(function (xhr, textStatus) {
        window.console && console.log('Unable to filter system select: Text Status: ' + textStatus + ', Ready State: ' + xhr.readyState + ', HTTP Status Code: ' + xhr.status);
        alert('Unable to filter system select: server did not handle request');
    });

    request.always(function () {
        jlab.requestEnd();
        $("#category-indicator").html('');

        if (success && refreshSystemList) {
            jlab.dtm.filterSystemSelect($("#system").val());
        }
    });
};
jlab.dtm.editEvent = function () {

    if (jlab.isRequest()) {
        window.console && console.log("Ajax already in progress");
        return;
    }

    var timeUp = $("#event-time-up").val(),
            eventId = $("#event").val(),
            eventTitle = $("#event-dialog-event-title").val(),
            eventTypeId = $("#event-dialog-event-type").val();

    if (eventTitle === '') {
        alert('Please specify a title');
        return false;
    }

    if (eventTypeId === '') {
        alert('Please specify a type');
        return false;
    }

    if (timeUp === "  -   -       :  ") {
        window.console && console.log("timepicker input placeholder mask is erroneously set as eventTimeUp value!");
        timeUp = '';
    }

    jlab.requestStart();

    $("#edit-event-button").html("<span class=\"button-indicator\"></span>");
    $("#edit-event-button").attr("disabled", "disabled");

    var leaveSpinning = false;

    var request = jQuery.ajax({
        url: "/dtm/ajax/edit-event",
        type: "POST",
        data: {
            eventId: eventId,
            eventTitle: eventTitle,
            eventTypeId: eventTypeId,
            timeUp: timeUp
        },
        dataType: "html"
    });

    request.done(function (data) {
        if ($(".status", data).html() !== "Success") {
            alert('Unable to edit event: ' + $(".reason", data).html());
        } else {
            /* Success */
            /*leaveSpinning = true;
             document.location.reload(true);*/
            jlab.dtm.updateEventInPlace(eventId);
            $("#event-dialog").dialog("close");
        }

    });

    request.fail(function (xhr, textStatus) {
        window.console && console.log('Unable to edit event: Text Status: ' + textStatus + ', Ready State: ' + xhr.readyState + ', HTTP Status Code: ' + xhr.status);
        alert('Unable to edit event: server did not handle request');
    });

    request.always(function () {
        jlab.requestEnd();
        if (!leaveSpinning) {
            $("#edit-event-button").html("Save");
            $("#edit-event-button").removeAttr("disabled");
        }
    });
};
jlab.dtm.clearIncidentForm = function (skipReloadSystem) {
    $("#incident-dialog-event-type").val('');
    $("#incident-dialog-event-title").val('');
    $("#copy-title-checkbox").prop("checked", false).change();
    $("#append-title-checkbox").prop("checked", false).change();
    $("#incident-dialog-event-time-up").val('');
    $("#time-down").val('');
    $("#time-up").val('');
    $("#title").val('');
    $("#summary").val('');
    $("#permit-to-work").val('');
    $("#research-li").removeClass("invisible");
    $("#research").val("Y");
    $("#halla").prop("checked", false);
    $("#hallb").prop("checked", false);
    $("#hallc").prop("checked", false);
    $("#halld").prop("checked", false);
    $("#system").val('');
    $("#component").attr("data-component-id", "");
    $("#component").val('');
    $("#category").val('');

    if (skipReloadSystem !== 1) {
        jlab.dtm.filterSystemSelect();
    }

    $("#incident-dialog-event-id").val('');
    $("#incident").val('');

    $("#solution").val('');
    $("#repaired-by").val(null).trigger('change');
    $("#reviewed-by").val('');
};
$(document).on("click", ".open-edit-incident-dialog-button", function () {

    jlab.dtm.prepareIncidentFormForEdit.call(this, true);

    $("#copy-title-checkbox").prop("checked", false).change();
    $("#append-title-checkbox").prop("checked", false).change();

    $(".incident-dialog-new-li").hide();
    $(".incident-dialog-existing-li").show();

    $("#append-checkbox-field").hide();

    $("#incident-dialog-event-id").val($(this).closest(".event-detail").attr("data-event-id"));
    var eventTitle = $(this).closest(".ui-accordion-content").prev().find(".accordion-event-title").text();
    $("#incident-dialog-event-title").val(eventTitle);
    $("#incident-dialog-event-title").attr("data-original-title", eventTitle);

    $("#incident-dialog").dialog('option', 'title', 'Edit Incident');

    jlab.dtm.selectCategoryBasedOnSystem(true);

    $("#start-with-checkbox").prop("checked", false).change();

    $("#incident-form-tabs").tabs("option", "active", 0);

    $("#incident-dialog").dialog("open");
});
$(document).on("click", "#open-add-event-dialog-button", function () {
    jlab.dtm.clearIncidentForm();

    $(".incident-dialog-new-li").show();
    $(".incident-dialog-existing-li").hide();

    $("#time-down").val(jlab.dateTimeToJLabString(new Date()));

    $("#copy-title-checkbox").prop("checked", "true").change();

    $("#incident-action-button").attr("data-action", "add-event");

    $("#incident-dialog").dialog('option', 'title', 'Add Event');

    /*$("#incident-dialog-event-type").val(1);*/
    $("#start-with-checkbox").prop("checked", false).change();

    $("#incident-form-tabs").tabs("option", "active", 0);

    $("#incident-dialog").dialog("open");
});
$(document).on("click", ".open-add-incident-dialog-button", function () {

    var type = parseInt($(this).closest(".event-detail").find(".incident-table").attr("data-event-type-id"));

    jlab.dtm.clearIncidentForm(1);
    $("#incident-dialog-event-type").val(type);

    jlab.dtm.toggleMultiHall(type);
    jlab.dtm.toggleResearch(type);

    jlab.dtm.filterCategorySelect(type);

    jlab.dtm.filterSystemSelect();

    $("#time-down").val(jlab.dateTimeToJLabString(new Date()));

    $("#incident-dialog-event-id").val($(this).closest(".event-detail").attr("data-event-id"));
    var eventTitle = $(this).closest(".ui-accordion-content").prev().find(".accordion-event-title").text();
    $("#incident-dialog-event-title").val(eventTitle);
    $("#incident-dialog-event-title").attr("data-original-title", eventTitle);

    $("#append-title-checkbox").prop("checked", "true").change();

    $(".incident-dialog-new-li").hide();
    $(".incident-dialog-existing-li").show();

    $("#incident-action-button").attr("data-action", "add-incident");

    $("#incident-dialog").dialog('option', 'title', 'Add Incident');

    $("#start-with-checkbox").prop("checked", false).change();

    $("#incident-form-tabs").tabs("option", "active", 0);

    $("#incident-dialog").dialog("open");
});
$(document).on("click", ".open-edit-expert-review-dialog-button", function () {

    var $tr = $(this).closest("tr"),
            incidentId = $tr.attr("data-incident-id"),
            reviewLevel = $tr.attr("data-review-level"),
            expertReviewerTsv = $tr.attr("data-reviewed-by-experts-formatted-tsv"),
            acknowledged = $tr.attr("data-acknowledged"),
            cause = $tr.attr("data-root-cause"),
            rarId = $tr.attr("data-rar-id"),
            rarExt = $tr.attr("data-rar-ext");

    $("#review-level").text(reviewLevel);
    $("input[name=acknowledged][value=" + acknowledged + "]").prop('checked', true)
    $("#root-cause").val(cause);
    $("#root-cause-dialog-incident-id").val(incidentId);
    
    $("#edit-expert-reviewers").empty();
    
    var experts = expertReviewerTsv.split("\t");
    
    for(var e of experts) {
        $("#edit-expert-reviewers").append("<div>" + String(e).encodeXml() + "</div>");
    }    
    
    $("#rar-link").empty();    
    
    if(rarExt !== '') {
        $("#rar-link").append('<a href="' + String(jlab.contextPath + jlab.rarLink + incidentId).encodeXml() + '">RAR Document</a>');
    }    

    var $acknowledgedKey = $("#acknowledged-key"); 
    if('Level Ⅰ' === reviewLevel) {
        $acknowledgedKey.addClass("required-field");
    } else {
        $acknowledgedKey.removeClass("required-field");
    }

    var $rootCauseKey = $("#root-cause").closest("li").find(".li-key"); 
    if('Level Ⅱ' === reviewLevel) {
        $rootCauseKey.addClass("required-field");
    } else {
        $rootCauseKey.removeClass("required-field");
    }

    var $rarKey = $("#rar-link").closest("li").find(".li-key"); 
    if('Level Ⅲ+' === reviewLevel) {
        $rarKey.addClass("required-field");
    } else {
        $rarKey.removeClass("required-field");
    }

    $("#edit-expert-review-dialog").dialog("open");
});
$(document).on("click", "#edit-expert-review-button", function () {
    jlab.dtm.editSystemExpertReview();
});
$(document).on("click", "#incident-action-button", function () {
    jlab.dtm.doIncidentAction($(".reload-after-edit").length > 0);
});
$(document).on("click", ".remove-incident-button", function () {
    if (confirm('Are you sure you want to delete this incident?')) {
        jlab.dtm.removeIncident.call(this);
    }
});
$(document).on("click", ".close-incident-button", function () {
    jlab.dtm.prepareIncidentFormForEdit.call(this, true);
    $("#incident-dialog-event-title").val($(this).closest(".ui-accordion-content").prev().find(".accordion-event-title").text());
    var timeUpStr = jlab.dateTimeToJLabString(new Date());
    $("#time-up").val(timeUpStr);
    jlab.dtm.doIncidentAction();
});
$(document).on("click", ".remove-event-button", function () {
    if (confirm('Are you sure you want to delete this event?')) {
        jlab.dtm.removeEvent.call(this);
    }
});
$(document).on("click", ".open-edit-event-dialog-button", function () {
    jlab.dtm.prepareEventFormForEdit.call(this);

    //$(".date-field").datepicker("option", "disabled", true);
    $("#event-dialog").dialog("open");
    //$(".date-field").datepicker("option", "disabled", false);
});
$(document).on("click", "#edit-event-button", function () {
    jlab.dtm.editEvent();
});
$(document).on("click", ".close-event-button", function () {
    jlab.dtm.prepareEventFormForEdit.call(this);
    var timeUpStr = jlab.dateTimeToJLabString(new Date());
    $("#event-time-up").val(timeUpStr);
    jlab.dtm.editEvent();
});

$(document).on("change", "#incident-dialog-event-type", function () {
    let type = parseInt($("#incident-dialog-event-type").val());
    jlab.dtm.toggleMultiHall(type);
    jlab.dtm.toggleResearch(type);
    jlab.dtm.filterCategorySelect(type);
    jlab.dtm.filterSystemSelect();
});
$(document).on("change", "#category", function () {
    jlab.dtm.filterSystemSelect();
});
$(document).on("change", "#system", function () {
    jlab.dtm.selectCategoryBasedOnSystem();
});

$(document).on("click", ".explanation-link", function () {
    $(".flyout-handle").remove();
    $(".explanation-handle").remove();
    $(".resolution-handle").remove();

    var explanation = $(this).attr("data-explanation");
    $(this).append('<div class="explanation-handle"><div class="explanation-panel"><button class="close-bubble">X</button><div><h4>Explanation:</h4><div class="explanation-content">' + String(explanation).encodeXml() + '</div></div></div></div>');
    return false;
});
$(document).on("click", ".review-link", function () {
    var $tr = $(this).closest("tr"),
            solution = $tr.attr("data-solution"),
            repairedBy = $tr.attr("data-repaired-by-formatted"),
            opsReviewer = $tr.attr("data-reviewed-by-formatted"),
            reviewLevel = $tr.attr("data-review-level"),
            expertReviewerTsv = $tr.attr("data-reviewed-by-experts-formatted-tsv"),
            acknowledged = $tr.attr("data-acknowledged"),
            rootCause = $tr.attr("data-root-cause"),
            rarId = $tr.attr("data-rar-id"),
            incidentId = $tr.attr("data-incident-id"),
            rarExt = $tr.attr("data-rar-ext");

    $("#review-dialog-ops-reviewer").text(opsReviewer);
    $("#review-dialog-repairer").text(repairedBy);
    $("#review-dialog-solution").text(solution);
    $("#review-dialog-review-level").text(reviewLevel);
    $("#review-dialog-acknowledged").text(jlab.dtm.acknowledgedMap[acknowledged]);
    $("#review-dialog-root-cause").text(rootCause);
    
    $("#review-dialog-sys-reviewer").empty();
    
    var experts = expertReviewerTsv.split("\t");
    
    for(var e of experts) {
        $("#review-dialog-sys-reviewer").append("<div>" + String(e).encodeXml() + "</div>");
    }
    
    $("#review-dialog-rar-link").empty();    
    
    if(rarExt !== '') {
        $("#review-dialog-rar-link").append('<a href="' + String(jlab.contextPath + jlab.rarLink + incidentId).encodeXml() + '">RAR Document</a>');
    }

    $("#review-dialog").dialog("open");

    return false;
});
$(document).on("click", ".flyout-link", function () {
    $(".flyout-handle").remove();
    $(".explanation-handle").remove();
    $(".resolution-handle").remove();

    $(this).append('<div class="flyout-handle"><div class="flyout-panel"><button class="close-bubble">X</button><div><h4>Incident Link:</h4><input class="copy-link" type="text" readonly="readonly" value="all-events?incidentId="/></div></div></div>');
    $(".copy-link").val($(this).attr("href"));
    $(".copy-link").focus().select();
    return false;
});
$(document).on("click", ".close-bubble", function () {
    $(this).parent().parent().remove();
    return false;
});

$(document).on("keyup", "#incident-dialog-event-title", function () {
    if ($("#copy-title-checkbox").is(":checked")) {
        $("#title").val(this.value);
    }
});

$(document).on("blur", "#incident-dialog-event-title", function () {
    if ($("#copy-title-checkbox").is(":checked")) {
        $("#title").val(this.value);
    }
});

$(document).on("change", "#copy-title-checkbox", function () {
    if ($("#copy-title-checkbox").is(":checked")) {
        $("#title").attr("readonly", "readonly");
        $("#title").val($("#incident-dialog-event-title").val());
    } else {
        $("#title").removeAttr("readonly");
    }
});

$(document).on("keyup", "#title", function () {
    if ($("#append-title-checkbox").is(":checked")) {
        var appendedTitle = $("#incident-dialog-event-title").attr("data-original-title") + ' + ' + this.value;

        if (appendedTitle.length > 128) {
            appendedTitle = appendedTitle.substr(0, 128);
        }

        $("#incident-dialog-event-title").val(appendedTitle);
    }
});

$(document).on("blur", "#title", function () {
    if ($("#append-title-checkbox").is(":checked")) {
        var appendedTitle = $("#incident-dialog-event-title").attr("data-original-title") + ' + ' + this.value;

        if (appendedTitle.length > 128) {
            appendedTitle = appendedTitle.substr(0, 128);
        }

        $("#incident-dialog-event-title").val(appendedTitle);
    }
});
$(document).on("change", "#file-upload-input", function(){
    /*if($(this).get(0).files.length > 0){*/
    if($("#file-upload-input").val()) {
        var fileSize = $(this).get(0).files[0].size;

        if (fileSize > 10485760) {
            alert('File too big: must be no more than 10MB');
        } else {
            jlab.dtm.uploadFile();
        }
    }
});
$(document).on("change", "#append-title-checkbox", function () {
    if ($("#append-title-checkbox").is(":checked")) {
        $("#incident-dialog-event-title").attr("readonly", "readonly");
        var appendedTitle = $("#incident-dialog-event-title").attr("data-original-title") + ' + ' + $("#title").val();

        if (appendedTitle.length > 128) {
            appendedTitle = appendedTitle.substr(0, 128);
        }

        $("#incident-dialog-event-title").val(appendedTitle);
    } else {
        $("#incident-dialog-event-title").removeAttr("readonly");
    }
});

$(document).on("change", "#start-with-checkbox", function () {
    if ($("#start-with-checkbox").is(":checked")) {
        $(".category-start-item").show();
    } else {
        $(".category-start-item").hide();
    }
});

$(document).on("click", "#current-shift-summary-link", function () {
    var now = new Date(),
            start = jlab.getCcShiftStart(now),
            end = jlab.getCcShiftEnd(now),
            startParamValue = jlab.dateTimeToJLabString(start),
            endParamValue = jlab.dateTimeToJLabString(end);

    window.location = '/dtm/reports/incident-downtime?type=&system=&component=&transport=&start=' + encodeURIComponent(startParamValue) + '&end=' + encodeURIComponent(endParamValue) + '&qualified=';
    return false;
});

$(document).on("click", "#previous-shift-summary-link", function () {
    var now = new Date(),
            dateInPreviousShift = jlab.getCcShiftStart(now);
    dateInPreviousShift.setHours(dateInPreviousShift.getHours() - 1);

    var start = jlab.getCcShiftStart(dateInPreviousShift),
            end = jlab.getCcShiftEnd(dateInPreviousShift),
            startParamValue = jlab.dateTimeToJLabString(start),
            endParamValue = jlab.dateTimeToJLabString(end);

    window.location = '/dtm/reports/incident-downtime?type=&system=&component=&transport=&start=' + encodeURIComponent(startParamValue) + '&end=' + encodeURIComponent(endParamValue) + '&qualified=';
    return false;
});
$(document).on("click", ".default-clear-panel", function () {
    $("#date-range").val('custom').change();
    $("#start").val('');
    $("#end").val('');
    $("#event-type").val('');
    $("#transport").val('');
    $("#event-id").val('');
    $("form .incident-id-wrap").remove();
    $("#acknowledged").val('');
    $("#sme-username").val('');
    return false;
});
$(document).on("click", "#add-incident-id-button", function() {
    $("#incident-add-template .incident-id-wrap").clone().appendTo("#incident-add-anchor");
});
$(document).on("click", ".remove-incident-id-button", function() {
    $(this).closest(".incident-id-wrap").remove();
});
$(document).on("click", ".me-button", function () {
    var username = $("#username-container").text().trim();
    $(this).prevAll(".username-autocomplete").first().val(username);
});
$(function () {

    $("#accordion")
            /*.addClass("ui-accordion ui-accordion-icons ui-widget ui-helper-reset")*/
            .find("h3")
            /*.addClass("ui-accordion-header ui-helper-reset ui-state-default ui-corner-top ui-corner-bottom")*/
            .hover(function () {
                $(this).toggleClass("ui-state-hover");
            })
            .click(function () {
                $(this).toggleClass("ui-accordion-header-active ui-state-active ui-state-default ui-corner-bottom")
                        .find("> .event-header-toggle .ui-icon").toggleClass("ui-icon-triangle-1-e ui-icon-triangle-1-s").end()
                        .next().toggleClass("ui-accordion-content-active").slideToggle();
                if ($(this).hasClass("ui-accordion-header-active")) {
                    if (jlab.logbookEnabled) {
                        jlab.dtm.loadPanelLogbookReferences.call($(this).next());
                    }
                }
                return false;
            })
            .next()
            /*.addClass("ui-accordion-content ui-helper-reset ui-widget-content ui-corner-bottom")*/
            .hide();

    $("#accordion").find(".event-header-toggle").prepend('<span class="ui-icon ui-icon-triangle-1-e"></span>');

    if ($("#accordion h3").length === 1) {
        $("#accordion h3").click();
    }

    $("#edit-expert-review-dialog").dialog("option", "height", 500);
    $("#edit-expert-review-dialog").dialog("option", "width", 550);
    $("#edit-expert-review-dialog").dialog("option", "resizable", false);

    $("#review-dialog").dialog("option", "height", 500);
    $("#review-dialog").dialog("option", "width", 550);
    $("#review-dialog").dialog("option", "resizable", false);

    $("#incident-dialog").dialog("option", "height", 900);
    $("#incident-dialog").dialog("option", "minHeight", 900);
    $("#incident-dialog").dialog("option", "width", 950);
    $("#incident-dialog").dialog("option", "minWidth", 950);
    $(".dialog").dialog("option", "draggable", true);

    $("#repaired-by").select2({
        width: 405
    });

    $("#incident-form-tabs").tabs();

    $(".event-list").show();
});