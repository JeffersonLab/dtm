var jlab = jlab || {};
jlab.dtm = jlab.dtm || {};

jlab.editableRowTable.entity = 'Expert';
jlab.editableRowTable.dialog.width = 360;
jlab.editableRowTable.dialog.height = 210;

$(function(){
    $("#table-row-dialog").dialog({width: 360, height: 210});
});

jlab.dtm.deleteRow = function () {
    var $selectedRow = $("#expert-table tbody tr.selected-row");

    if ($selectedRow.length < 1) {
        return;
    }

    if (jlab.isRequest()) {
        window.console && console.log("Ajax already in progress");
        return;
    }

    var username = $selectedRow.find("td:nth-child(1)").text();

    if (!confirm('Are you sure you want to remove the expert ' + username)) {
        return;
    }

    jlab.requestStart();

    var expertId = $selectedRow.attr("data-expert-id");

    var request = jQuery.ajax({
        url: jlab.contextPath + "/setup/ajax/delete-expert",
        type: "POST",
        data: {
            expertId: expertId
        },
        dataType: "html"
    });

    request.done(function (data) {
        if ($(".status", data).html() !== "Success") {
            alert('Unable to delete expert: ' + $(".reason", data).html());
        } else {
            /* Success */
            $selectedRow.remove();
            $("#unselect-all-button").click();
        }

    });

    request.fail(function (xhr, textStatus) {
        window.console && console.log('Unable to delete expert: Text Status: ' + textStatus + ', Ready State: ' + xhr.readyState + ', HTTP Status Code: ' + xhr.status);
        alert('Unable to delete expert; server did not handle request');
    });

    request.always(function () {
        jlab.requestEnd();
    });
};

jlab.dtm.addRow = function (expertId, username, first, last) {
    var $row = ("<tr data-expert-id=\"" + String(expertId).encodeXml() + "\"><td>" + String(last).encodeXml() + ", " + String(first).encodeXml() + " (" + String(username).encodeXml() + ")</td></tr>");
    $("#expert-table tbody").append($row);
};

jlab.dtm.addExpert = function () {

    var systemId = $("#system-select").val(),
            username = $("#username").val(),
            first = $("#username").attr("data-first"),
            last = $("#username").attr("data-last"),
            url = jlab.contextPath + "/setup/ajax/add-expert",
            data = {systemId: systemId, username: username},
    $dialog = $("#table-row-dialog");

    var promise = jlab.doAjaxJsonPostRequest(url, data, $dialog, false);

    promise.done(function (json) {
        if (json.stat !== "ok") {
            var errorMsg = json.error || "Unknown Error";
            alert('Unable to add expert: ' + errorMsg);
        } else {
            var expertId = json.id;
            jlab.dtm.addRow(expertId, username, first, last);
        }
    });
};

$(document).on("click", "#remove-row-button", function () {
    jlab.dtm.deleteRow();
});

$(document).on("click", "#open-add-row-dialog-button", function () {
    var system = $("#system-select option:selected").text();

    $("#table-row-dialog").find(".system-placeholder").text(system);
});

$(document).on("table-row-add", function () {
    jlab.dtm.addExpert();
});

$(document).on("click", ".default-clear-panel", function () {
    $("#category-select").val('').trigger('change');
    $("#system-select").val('');
    return false;
});
$(document).on("change", "#category-select", function () {
    var categoryId = $(this).val();
    jlab.filterSystemListByCategory(categoryId, "#system-select", "");
});