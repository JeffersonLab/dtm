var jlab = jlab || {};
jlab.dtm.acknowledgedMap = {N: 'No', Y: 'Yes', R: 'Reassign'};
jlab.prepareIncidentFormForEdit = function(skipSystemListLoad) {
    var $selectedRow = $("tr.selected-row"),
            eventType = $selectedRow.attr("data-event-type-id"),
            timeDown = $selectedRow.attr("data-incident-down"),
            timeUp = $selectedRow.attr("data-incident-up"),
            title = $selectedRow.find(".incident-title-link").text(),
            summary = $selectedRow.find(".incident-title-link").attr("title"),
            systemId = $selectedRow.attr("data-system-id"),
            componentName = $selectedRow.attr("data-component-name"),
            componentId = $selectedRow.attr("data-component-id"),
            incident = $selectedRow.attr("data-incident-id"),
            explanation = $selectedRow.attr("data-explanation"),
            solution = $selectedRow.find(".resolution-field .read-field").text(),
            repairedByIdCsv = $selectedRow.attr("data-repaired-by-id-csv"),
            reviewedBy = $selectedRow.attr("data-reviewed-by"),
            expertReviewers = $selectedRow.attr("data-reviewed-by-username-ssv"),
            reviewLevel = $selectedRow.attr("data-review-level"),
            acknowledged = $selectedRow.attr("data-acknowledged"),
            rootCause = $selectedRow.attr("data-root-cause"),
            rarExt = $selectedRow.attr("data-rar-ext");

    if ($selectedRow.length === 0) {
        return;
    }

    $("#category").val('');

    if (skipSystemListLoad === true) {
        $("#system").empty();
        $("#system").append('<option selected="selected" value="' + String(systemId).encodeXml() + '"> </option>');
    } else {
        jlab.dtm.filterSystemSelect(systemId);
    }

    $("#incident-dialog-event-type").val(eventType);
    $("#incident-dialog-event-time-up").val('');
    $("#time-down").val(timeDown);
    $("#time-up").val(timeUp);
    $("#title").val(title);
    $("#summary").val(summary);
    $("#component").val(componentName);
    $("#component").attr("data-component-id", componentId);

    $("#incident-dialog-event-id").val('');

    $("#incident").val(incident);

    $("#explanation").val(explanation);

    $("#solution").val(solution);

    $("#repaired-by").val(repairedByIdCsv.split(",")).trigger('change');

    $("#reviewed-by").val(reviewedBy);
    
    $("#edit-incident-dialog-review-level").text(reviewLevel);
    $("#edit-incident-dialog-sys-reviewer").val(expertReviewers);
    $("#edit-incident-dialog-acknowledged").text(jlab.dtm.acknowledgedMap[acknowledged]);
    $("#edit-incident-dialog-root-cause").text(rootCause);
    
    $("#incident-action-button").attr("data-action", "edit-incident");
};
$(document).on("click", "tbody tr", function() {
    $("#open-edit-selected-dialog-button").button("enable");
});
$(document).on("click", function(event) {

  if (!$(event.target).closest('tbody').length) { // If you clicked outside the tbody
        $("tbody tr.selected-row").removeClass("selected-row"); 
        $("#open-edit-selected-dialog-button").button("disable");
  }

});
$(document).on("click", "#open-edit-selected-dialog-button", function() {

    var $selectedRow = $("tr.selected-row");

    if($selectedRow.length === 0) {
        return;
    }

    jlab.prepareIncidentFormForEdit.call(this, true);

    $("#copy-title-checkbox").prop("checked", false).change();
    $("#append-title-checkbox").prop("checked", false).change();

    $(".incident-dialog-new-li").hide();
    $(".incident-dialog-existing-li").show();

    $("#append-checkbox-field").hide();

    $("#incident-dialog-event-id").val($selectedRow.attr("data-event-id"));
    var eventTitle = $selectedRow.attr("data-event-title");
    $("#incident-dialog-event-title").val(eventTitle);
    $("#incident-dialog-event-title").attr("data-original-title", eventTitle);

    $("#incident-dialog").dialog('option', 'title', 'Edit Incident');

    jlab.dtm.selectCategoryBasedOnSystem(true);

    $("#start-with-checkbox").prop("checked", false).change();

    $( "#incident-form-tabs" ).tabs( "option", "active", 0 );
    
    $("#incident-dialog").dialog("open");
});
/* Link the log entries that reference each incident under its Solution, where the details go */
jlab.dtm.loadSolutionLogEntries = function() {
    var $container = $(this),
            incidentId = $container.closest("tr").attr("data-incident-id");

    var request = jQuery.ajax({
        url: jlab.contextPath + "/data/references",
        type: "GET",
        data: {
            'id': incidentId
        },
        dataType: "json"
    });

    request.done(function(json) {
        if (json.stat === "fail") {
            window.console && console.log('Unable to load log entries: ' + json.error);
            $container.html('<span class="solution-log-entries-error">Log entries: unable to load</span>');
            return;
        }

        var references = jlab.dtm.filterGeneratedLogReferences(json.data);

        if (references.length > 0) {
            $container.append('Details: ');
            $(references).each(function(index) {
                if (index > 0) {
                    $container.append(', ');
                }
                $('<a target="_blank"></a>')
                        .attr("href", jlab.logbookServerUrl + '/entry/' + encodeURIComponent(this.lognumber))
                        .attr("title", this.title)
                        .text(this.lognumber)
                        .appendTo($container);
            });
        }
    });

    request.fail(function(xhr, textStatus) {
        window.console && console.log('Unable to load log entries: Text Status: ' + textStatus + ', HTTP Status Code: ' + xhr.status);
        $container.html('<span class="solution-log-entries-error">Log entries: unable to load</span>');
    });
};
$(function() {
    $("#open-edit-selected-dialog-button, #fullscreen-button, #exit-fullscreen-button").button();

    if (jlab.logbookEnabled) {
        $(".solution-log-entries").each(jlab.dtm.loadSolutionLogEntries);
    }

    $("#open-edit-selected-dialog-button").button("option", "disabled", "disabled");

    $("#type, #hall").select2({
        width: 360
    });
});