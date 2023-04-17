var jlab = jlab || {};

jlab.uploadFile = function() {
    var incidentId = $("#sad-rar-incident").val(),
        form = $("#sad-rar-upload-pane")[0];

    return jlab.uploadRARFile(incidentId, form);
};

jlab.saveSADRAR = function (title, down, up) {

    if (jlab.isRequest()) {
        window.console && console.log("Ajax already in progress");
        return;
    }

    var incidentId = "",
        eventTitle = title,
        eventId = "",
        eventTypeId = 1,
        eventTimeUp = up,
        timeDown = down,
        timeUp = up,
        title = title,
        summary = 'SAD RAR',
        componentId = "",
        componentName = "Unknown/Missing",
        action = "add-event",
        explanation = "SAD RAR",
        solution = "",
        repairedByArray = "",
        reviewedBy = "",
        expertUsernameArray = "";

    jlab.requestStart();

    $("#save-sad-rar-button").html("<span class=\"button-indicator\"></span>");
    $("#save-sad-rar-button").attr("disabled", "disabled");

    var leaveSpinning = false;

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
            alert('Unable to add SAD RAR: ' + $(".reason", data).html());
        } else {
            var incidentId = $(".incident", data).html();

            $("#sad-rar-dialog").addClass("file-upload-dialog");
            $("#sad-rar-incident").val(incidentId);
        }

    });

    request.fail(function (xhr, textStatus) {
        window.console && console.log('Unable to add SAD RAR: Text Status: ' + textStatus + ', Ready State: ' + xhr.readyState + ', HTTP Status Code: ' + xhr.status);
        alert('Unable to add SAD RAR: server did not handle request');
    });

    request.always(function () {
        jlab.requestEnd();
        $("#save-sad-rar-button").html("Next");
        $("#save-sad-rar-button").removeAttr("disabled");
    });
};
$(document).on("change", "#file-upload-input", function(){
    /*if($(this).get(0).files.length > 0){*/
    if($("#file-upload-input").val()) {
        var fileSize = $(this).get(0).files[0].size;

        if (fileSize > 10485760) {
            alert('File too big: must be no more than 10MB');
        } else {
            jlab.uploadFile();
        }
    }
});

$(function () {
    $("#exit-fullscreen-button, #report-page-actions button, #report-page-actions a").button();
});
$(document).on("click", ".default-clear-panel", function () {
    $("#date-range").val('custom').change();
    $("#start").val('');
    $("#end").val('');
    $("#event-type").val('');
    $("#transport").val('');
    $("#event-id").val('');
    $("#incident-id").val('');
    $("#acknowledged").val('');
    $("#sme-username").val('');
    return false;
});

$(document).on("click", ".default-reset-panel", function () {
    $("#date-range").val('1month').change();
    $("#event-type").val('1');
    $("#event-id").val('');
    $("#incident-id").val('');
    $("#acknowledged").val('');
    $("#sme-username").val('');    
    return false;
});

$(document).on("click", "#show-sad-rar-dialog-button", function () {
    $("#sad-rar-dialog").dialog('open');
});

$(document).on("click", "#save-sad-rar-button", function () {
    var title = $("#event-title").val(),
        down = $("#time-down").val(),
        up = $("#time-up").val();

    if(title.trim() === '') {
        alert('Title is required');
        return;
    }

    if(down.trim() === '') {
        alert('Time Down is required');
        return;
    }

    if(up.trim() === '') {
        alert('Time Up is required');
        return;
    }

    jlab.saveSADRAR(title, down, up);
});

$(document).on("change", "input[name=incidentMask], #sort-select", function () {
    $("#filter-form").submit();
});

$(document).on("change", "#time-down", function () {
    if($("#time-up").val() === '') {
        try {
            var downStr = $("#time-down").val(),
                downDate = jlab.fromFriendlyDateTimeString(downStr);

            downDate.setHours(downDate.getHours() + 4);

            var upStr = jlab.toFriendlyDateTimeString(downDate);

            $("#time-up").val(upStr);
        } catch(error) {
            /*Ignore, down probably isn't formatted properly or is empty*/
        }
    }
});
