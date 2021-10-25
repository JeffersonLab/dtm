var jlab = jlab || {};
jlab.rarLink = '/ajax/rar-download?incidentId=';
jlab.uploadRARFile = function(incidentId, form) {

    var data = new FormData(form);

    data.append("incidentId", incidentId);

    var promise = $.ajax({
        type: "POST",
        enctype: "multipart/form-data",
        url: jlab.contextPath + "/ajax/rar-upload",
        data: data,
        processData: false,
        contentType: false,
        cache: false
    });

    promise.done(function(data){
        $("#rar-link").empty();
        $("#rar-link").append('<a href="' + jlab.contextPath + jlab.rarLink + incidentId + '">RAR Document</a>');
    });

    promise.error(function(xhr, textStatus){
        window.console && console.log('Unable to upload file: Text Status: ' + textStatus + ', Ready State: ' + xhr.readyState + ', HTTP Status Code: ' + xhr.status);
        alert('Unable to upload file: server did not handle request');
    });

    return promise;
};
/*Formatting*/
jlab.addS = function (x) {
    if (x !== 1) {
        return 's ';
    } else {
        return ' ';
    }
};
jlab.millisecondsToHumanReadable = function (milliseconds) {
    var days = Math.floor(milliseconds / 86400000),
            remainingMilliseconds = milliseconds % 86400000,
            hours = Math.floor((remainingMilliseconds) / 3600000),
            remainingMilliseconds2 = remainingMilliseconds % 3600000,
            minutes = Math.floor(remainingMilliseconds2 / 60000);

    return (days > 0 ? days + ' day' + jlab.addS(days) : '') + (hours > 0 ? hours + ' hour' + jlab.addS(hours) : '') + minutes + ' minute' + jlab.addS(minutes);
};
jlab.dateTimeToJLabString = function (x) {
    var year = x.getFullYear(),
            month = x.getMonth(),
            day = x.getDate(),
            hour = x.getHours(),
            minute = x.getMinutes();

    return jlab.pad(day, 2) + '-' + jlab.triCharMonthNames[month] + '-' + year + ' ' + jlab.pad(hour, 2) + ':' + jlab.pad(minute, 2);
};
/* String Date format: YYYY-MM-DD hh:mm */
jlab.dateTimeFromIsoString = function (x) {
    var year = parseInt(x.substring(0, 4)),
            month = parseInt(x.substring(5, 7)),
            day = parseInt(x.substring(8, 10)),
            hour = parseInt(x.substring(11, 13)),
            minute = parseInt(x.substring(14, 16));

    return new Date(year, month - 1, day, hour, minute);
};
jlab.dateTimeToIsoString = function (x) {
    var year = x.getFullYear(),
            month = x.getMonth() + 1,
            day = x.getDate(),
            hour = x.getHours(),
            minute = x.getMinutes();

    return year + '-' + jlab.pad(month, 2) + '-' + jlab.pad(day, 2) + ' ' + jlab.pad(hour, 2) + ':' + jlab.pad(minute, 2);
};
jlab.dateTimeToGlobalUTCString = function (x) {
    var year = x.getUTCFullYear(),
            month = x.getUTCMonth(), /* zero based */
            day = x.getUTCDate(),
            hour = x.getUTCHours(),
            minute = x.getUTCMinutes();

    return jlab.pad(day, 2) + '-' + jlab.triCharMonthNames[month] + '-' + year + ' ' + jlab.pad(hour, 2) + ':' + jlab.pad(minute, 2);
};
jlab.dateTimeToGlobalString = function (x) {
    var year = x.getFullYear(),
            month = x.getMonth(), /* zero based */
            day = x.getDate(),
            hour = x.getHours(),
            minute = x.getMinutes();

    return jlab.pad(day, 2) + '-' + jlab.triCharMonthNames[month] + '-' + year + ' ' + jlab.pad(hour, 2) + ':' + jlab.pad(minute, 2);
};
/*Make sure to right-align your cells and use the same number of numbers after the decimal in all cells, then call this function to decimal align centered*/
jlab.fakeDecimalAlign = function ($rows, cellSelectors) {
    var maxTextWidths = [];
    for (var columnIndex = 0; columnIndex < cellSelectors.length; columnIndex++) {
        maxTextWidths[columnIndex] = 0;
    }

    $rows.each(function () {
        for (var columnIndex = 0; columnIndex < cellSelectors.length; columnIndex++) {
            var $td = $(this).find(cellSelectors[columnIndex]);
            $td.wrapInner('<span></span>');
            var length = $td.find("span").width();
            if (length > maxTextWidths[columnIndex]) {
                maxTextWidths[columnIndex] = length;
            }
        }
    });

    $rows.each(function () {
        for (var columnIndex = 0; columnIndex < cellSelectors.length; columnIndex++) {
            var $td = $(this).find(cellSelectors[columnIndex]);
            $td.find('span').css('margin-right', ($td.width() - maxTextWidths[columnIndex]) / 2 + 'px');
        }
    });
};


jlab.filterSystemListByCategory = function (categoryId, systemSelectSelector, applicationId, keephidden, multiselect) {
    if (jlab.isRequest()) {
        window.console && console.log("Ajax already in progress");
        return;
    }

    systemSelectSelector = typeof systemSelectSelector !== 'undefined' ? systemSelectSelector : '#system-select';
    applicationId = typeof applicationId !== 'undefined' ? applicationId : 1;

    jlab.requestStart();

    var request = jQuery.ajax({
        url: "/hco/ajax/filter-system-list-by-category",
        type: "GET",
        data: {
            categoryId: categoryId,
            applicationId: applicationId
        },
        dataType: "json"
    });

    request.done(function (data) {
        if (data.status !== "success") {
            alert('Unable to filter system list : ' + data.errorReason);
        } else {
            /* Success */
            var $select = $(systemSelectSelector),
                    $selectedId = $select.val();
            $select.hide();
            $select.empty();
            if (!multiselect) {
                $select.append('<option></option>');
            }
            $(data.optionList).each(function () {
                $select.append('<option value="' + this.value + '">' + this.name + '</option>');
            });
            if (!keephidden) {
                $select.slideDown();
            }
            $select.val($selectedId);
        }

    });

    request.error(function (xhr, textStatus) {
        window.console && console.log('Unable to filter system list: Text Status: ' + textStatus + ', Ready State: ' + xhr.readyState + ', HTTP Status Code: ' + xhr.status);
        alert('Unable to filter system list; server did not handle request');
    });

    request.always(function () {
        jlab.requestEnd();
    });
};

$(".username-autocomplete").autocomplete({
    minLength: 2,
    source: function (request, response) {
        $.ajax({
            data: {
                term: request.term,
                max: 10
            },
            url: '/hco/ajax/search-user',
            success: function (json) {
                response($.map(json.records, function (item) {
                    return {
                        id: item.id,
                        label: item.last + ', ' + item.first + ' (' + item.username + ')',
                        value: item.value,
                        first: item.first,
                        last: item.last
                    };
                }));

                if (json.total_records > 10) {
                    $(".ui-autocomplete").append($("<li class=\"plus-more\">Plus " + jlab.integerWithCommas(json.total_records - 10) + " more...</li>"));
                }
            }
        });
    },
    select: function (event, ui) {
        $(".username-autocomplete").attr("data-user-id", ui.item.id);
        $(".username-autocomplete").attr("data-first", ui.item.first);
        $(".username-autocomplete").attr("data-last", ui.item.last);
    }
});

/*Now Button Support 1 of 2*/
$(document).on("click", ".now-button", function () {
    $(this).prevAll(".date-field").first().val(jlab.dateTimeToJLabString(new Date()));
});
/*Date Range Presets*/
jlab.updateDateRange = function (start, end) {
    $("#custom-date-range-list").hide();
    $("#start").val(jlab.dateTimeToJLabString(start));
    $("#end").val(jlab.dateTimeToJLabString(end));
};
jlab.getCcShiftStart = function (dateInShift) {
    var start = new Date(dateInShift.getTime());

    start.setMinutes(0);
    start.setSeconds(0);
    start.setMilliseconds(0);

    var hour = start.getHours();

    if (hour === 23) {
        // Already start!
    } else if (hour <= 6) {
        start.setDate(start.getDate() - 1);
        start.setHours(23);
    } else if (hour <= 14) {
        start.setHours(7);
    } else {
        start.setHours(15);
    }

    return start;
};
jlab.getCcShiftEnd = function (dateInShift) {
    var end = new Date(dateInShift.getTime());

    end.setMinutes(0);
    end.setSeconds(0);
    end.setMilliseconds(0);

    var hour = end.getHours();

    if (hour === 23) {
        end.setDate(end.getDate() + 1);
        end.setHours(7);
    } else if (hour <= 6) {
        end.setHours(7);
    } else if (hour <= 14) {
        end.setHours(15);
    } else {
        end.setHours(23);
    }

    return end;
};
$(document).on("change", "#range", function () {
    var selected = $("#range option:selected").val();

    switch (selected) {
        case '1year':
            var start = new Date(),
                    end = new Date();

            end.setMonth(0);
            end.setDate(1);
            end.setMilliseconds(0);
            end.setSeconds(0);
            end.setMinutes(0);
            end.setHours(0);

            start.setTime(end.getTime());
            start.setFullYear(end.getFullYear() - 1);

            jlab.updateDateRange(start, end);
            break;
        case '0year':
            var start = new Date(),
                    end = new Date();

            end.setMonth(0);
            end.setDate(1);
            end.setMilliseconds(0);
            end.setSeconds(0);
            end.setMinutes(0);
            end.setHours(0);

            start.setTime(end.getTime());
            end.setFullYear(end.getFullYear() + 1);

            jlab.updateDateRange(start, end);
            break;        
        case '1month':
            var start = new Date(),
                    end = new Date();

            end.setDate(1);
            end.setMilliseconds(0);
            end.setSeconds(0);
            end.setMinutes(0);
            end.setHours(0);

            start.setTime(end.getTime());
            start.setMonth(end.getMonth() - 1);

            jlab.updateDateRange(start, end);
            break;
        case '0month':
            var start = new Date(),
                    end = new Date();

            end.setDate(1);
            end.setMilliseconds(0);
            end.setSeconds(0);
            end.setMinutes(0);
            end.setHours(0);

            start.setTime(end.getTime());
            end.setMonth(end.getMonth() + 1);

            jlab.updateDateRange(start, end);
            break;
        case '7days':
            var start = new Date(),
                    end = new Date();

            end.setMilliseconds(0);
            end.setSeconds(0);
            end.setMinutes(0);
            end.setHours(7);

            start.setTime(end.getTime());
            start.setDate(start.getDate() - 7);

            jlab.updateDateRange(start, end);
            break;
        case '3days':
            var start = new Date(),
                    end = new Date();

            end.setMilliseconds(0);
            end.setSeconds(0);
            end.setMinutes(0);
            end.setHours(7);

            start.setTime(end.getTime());
            start.setDate(start.getDate() - 3);

            jlab.updateDateRange(start, end);
            break;
        case '1day':
            var start = new Date(),
                    end = new Date();

            end.setMilliseconds(0);
            end.setSeconds(0);
            end.setMinutes(0);
            end.setHours(7);

            start.setTime(end.getTime());
            start.setDate(start.getDate() - 1);

            jlab.updateDateRange(start, end);
            break;
        case '1shift':
            var now = new Date(),
                    dateInPreviousShift = jlab.getCcShiftStart(now);
            dateInPreviousShift.setHours(dateInPreviousShift.getHours() - 1);

            var start = jlab.getCcShiftStart(dateInPreviousShift),
                    end = jlab.getCcShiftEnd(dateInPreviousShift);
            jlab.updateDateRange(start, end);
            break;
        case '0shift':
            var now = new Date(),
                    start = jlab.getCcShiftStart(now),
                    end = jlab.getCcShiftEnd(now);
            jlab.updateDateRange(start, end);
            break;
        case 'custom':
            $("#custom-date-range-list").show();
            break;
    }

});
$(function () {
    $(".dialog").dialog({
        autoOpen: false,
        width: 640,
        height: 480,
        modal: true,
        draggable: false
    });

    /*Custom time picker*/
    var myControl = {
        create: function (tp_inst, obj, unit, val, min, max, step) {
            $('<input class="ui-timepicker-input" value="' + val + '" style="width:50%">')
                    .appendTo(obj)
                    .spinner({
                        min: min,
                        max: max,
                        step: step,
                        change: function (e, ui) { // key events
                            // don't call if api was used and not key press
                            if (e.originalEvent !== undefined)
                                tp_inst._onTimeChange();
                            tp_inst._onSelectHandler();
                        },
                        spin: function (e, ui) { // spin events
                            tp_inst.control.value(tp_inst, obj, unit, ui.value);
                            tp_inst._onTimeChange();
                            tp_inst._onSelectHandler();
                        }
                    });
            return obj;
        },
        options: function (tp_inst, obj, unit, opts, val) {
            if (typeof (opts) === 'string' && val !== undefined)
                return obj.find('.ui-timepicker-input').spinner(opts, val);
            return obj.find('.ui-timepicker-input').spinner(opts);
        },
        value: function (tp_inst, obj, unit, val) {
            if (val !== undefined)
                return obj.find('.ui-timepicker-input').spinner('value', val);
            return obj.find('.ui-timepicker-input').spinner('value');
        }
    };

    $(".date-field").datetimepicker({
        dateFormat: 'dd-M-yy',
        controlType: myControl,
        timeFormat: 'HH:mm'
    }).mask("99-aaa-9999 99:99", {placeholder: " "});

    /*Now Button Support 2 of 2*/
    $('<span> </span><button class="now-button" type="button">Now</button>').insertAfter(".nowable-field");

    $("#component").autocomplete({
        minLength: 2,
        search: function (event, ui) {
            $("#component-indicator").html("<span class=\"button-indicator\"></span>");
        },
        response: function (event, ui) {
            $("#component-indicator").html('');
        },
        source: function (request, response) {
            var categoryId = null,
                systemId = null,
                    params = {};

            var type = $("#incident-dialog-event-type").val(),
                accDowntime = type == '1' || type == '',
                hallDowntime = type == '2' || type == '3' || type == '4' || type == '5',
                lerfDowntime = type == '6';

            if (hallDowntime) {
                systemId = 828;
            } else if ($("#hidden-system").length > 0 && $("#hidden-system").val() > 0) {
                systemId = $("#hidden-system").val();
            } else if ($("#system").css("display") !== 'none' && $("#system").val() > 0) {
                systemId = $("#system").val();
            } else if($("#category").val() > 0) {
                categoryId = $("#category").val();
            } else if(accDowntime) {
                categoryId = [1, 4, 5, 3]; /*CEBAF, Cryo, Facilities, Other*/;
            } else if(lerfDowntime) {
                categoryId = [2, 3]; /*LERF, Other*/
            }

            params = {
                q: request.term,
                application_id: 2
            };

            if(categoryId != null) {
                params.category_id = categoryId;
            }

            if(systemId != null) {
                params.system_id = systemId;
            }

            jQuery.ajaxSettings.traditional = true; /*array bracket serialization*/

            $.ajax({
                data: params,
                url: '/hco/data/components',
                success: function (json) {
                    response($.map(json.data, function (item) {
                        return {
                            label: item.name,
                            value: item.name,
                            id: item.id,
                            system_id: item.system_id
                        };
                    }));
                }
            });
        },
        select: function (event, ui) {
            $("#component").attr("data-component-id", ui.item.id);
            $("#system").val(ui.item.system_id);
            if (typeof jlab.dtm !== "undefined" && typeof jlab.dtm.selectCategoryBasedOnSystem === 'function') {
                jlab.dtm.selectCategoryBasedOnSystem();
            }
        }
    });
});
$(document).on("click", "#list-system-components-button", function () {
    $("#component").focus();
    if ($("#system").val() !== null && $("#system").val() !== '') {
        $("#component").autocomplete("search", "%%");
    }
});
$(document).on("click", "#excel-menu-item", function () {
    $("#excel").click();
});
$(document).on("click", "#shiftlog-menu-item", function () {
    $("#shiftlog").click();
});
$(document).on("click", "#csv-menu-item", function () {
    $("#csv").click();
});
$(document).on("change", "#max-select", function () {
    $("#max-input").val($(this).val());
    $("#filter-form").submit();
});
$(document).on("click", ".reload-button", function(){
    document.location.reload(true);
});