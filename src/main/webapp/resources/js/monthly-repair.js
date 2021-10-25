var jlab = jlab || {};

jlab.getDataSource = function (bar) {
    var dataMap = {};

    /*Keep consistent colors when toggling series*/
    var colorMap = {
        'Radiation Controls': '#FFCC00',
        'Diagnostics': '#9370DB',
        'Cryo': 'aqua',
        'RF': '#89CFF0',
        'Other': 'green',
        'Magnets': 'silver',
        'Vacuum': 'lime',
        'Safety Systems': '#f5f5dc', /*beige*/
        'Operations': '#d2b48c', /*tan; not recognized by bar chart fill (line okay)*/
        'Control System': '#1F75FE',
        'Facilities': 'pink',
        'Gun/Laser': 'fuchsia',
        'Beam Dumps': 'red',
        'Info Systems': '#FF9F00',
        'Beam Transport': '#FDFF00'
    }; /* see: https://developer.mozilla.org/en-US/docs/Web/CSS/color_value*/

    var durationMap = {},
            nameToIdMap = {};

    $("#graph-data-table tbody tr").each(function (index, value) {
        var timestamp = $("td:nth-child(2)", value).attr("data-date-utc"),
                /*yvalue = parseFloat($("td." + jlab.flotSourceColumnClass, value).text().replace(/,/g, '')),*/
                duration = parseFloat($("td:nth-child(3)", value).text().replace(/,/g, '')),
                grouping = $("td:first-child", value).text(),
                id = $("td:first-child", value).attr("data-id"),
                series = dataMap[grouping] || {};

        nameToIdMap[grouping] = id;

        series[timestamp] = duration;

        dataMap[grouping] = series;

        var groupDuration = durationMap[grouping] || 0;
        durationMap[grouping] = groupDuration + duration;
    });

    var ds = [], groupingNames = [];

    for (var key in dataMap) {
        groupingNames.push(key);
    }

    groupingNames.sort(function (a, b) {
        if (durationMap[a] > durationMap[b]) {
            return -1;
        }
        if (durationMap[a] < durationMap[b]) {
            return 1;
        }
        return 0;
    });

    /* We must fill in sparse data with zeros to satisfy stacked bars plugin */
    var startMillis = $("#graph-data-table").attr("data-start-millis") * 1,
            endMillis = $("#graph-data-table").attr("data-end-millis") * 1,
            xValues = [],
            intervalHours = 24;

    var startDate = new Date(startMillis);
    var endDate = new Date(endMillis);
    endDate.setDate(endDate.getDate() - 1); /* Convert exclusive to inclusive interval */

    /*console.log('start: ' + new Date(startMillis).toUTCString());
     console.log('end: ' + new Date(endMillis).toUTCString());*/
    for (var d = new Date(startDate); d.getTime() <= endDate.getTime(); d.setUTCDate(d.getUTCDate() + 1)) {
        xValues.push(new Date(d));
        /*console.log('tick: ' + d);*/
    }


    var fullDataMap = {};

    for (var i = 0; i < xValues.length; i++) {
        for (var j = 0; j < groupingNames.length; j++) {
            var series = fullDataMap[groupingNames[j]] || [];
            /*var yValue = j + 1;*/
            var yValue = dataMap[groupingNames[j]][xValues[i].getTime()] || 0;
            /*console.log('yValue: ' + yValue);*/
            series.push([xValues[i].getTime(), yValue]);
            fullDataMap[groupingNames[j]] = series;
        }
    }

    for (var i = groupingNames.length - 1; i >= 0; i--) {
        if (bar) {
            ds.push({
                label: groupingNames[i],
                data: fullDataMap[groupingNames[i]],
                color: 'black',
                bars: {
                    fillColor: colorMap[groupingNames[i]]
                }
            });
        } else { // line
            ds.push({
                label: groupingNames[i],
                data: fullDataMap[groupingNames[i]],
                color: colorMap[groupingNames[i]]
            });
        }
    }

    var grouped = !(ds.length === 1 && ds[0].label === '');

    if (!grouped) { // Use consistent color for total series
        if (bar) {
            ds[0].color = 'black';
            ds[0].bars.fillColor = 'red';
        } else { // line
            ds[0].color = 'red';
        }
    } else { /*If more than one grouping then create legend*/

        /*Calculate trips per hour per category*/
        var countMap = {};
        var tripPerHourMap = {};

        var globalCountSum = 0;
        var globalDurationSum = 0;
        for (var i = 0; i < groupingNames.length; i++) {
            var series = fullDataMap[groupingNames[i]],
                    countSum = 0;

            /*console.log('series length: ' + series.length);*/

            for (var j = 0; j < series.length; j++) {
                countSum = countSum + series[j][1];
            }

            /*console.log(groupingNames[i] + ' count sum: ' + countSum);*/

            countMap[groupingNames[i]] = countSum;
            tripPerHourMap[groupingNames[i]] = countSum / series.length;
            globalCountSum = globalCountSum + countSum;
            globalDurationSum = globalDurationSum + durationMap[groupingNames[i]];
        }

        var globalPerHour = globalCountSum / series.length;

        var rateHeader = '<th title="Trips per hour" class="legend-per">Trips /Hr</th>';

        if (intervalHours === 24) {
            rateHeader = '<th title="Trips per day" class="legend-per">Trips /Day</th>';
        }
        /*console.log('--> globalCountSum: ' + globalCountSum);*/

        var includeCount = false,
                includeRate = false,
                includeLost = false,
                includeMins = false,
                includePercent = true;

        $("#legendData :selected").each(function () {
            var value = $(this).val();

            if (value === 'count') {
                includeCount = true;
            } else if (value === 'rate') {
                includeRate = true;
            } else if (value === 'lost') {
                includeLost = true;
            } else if (value === 'mins') {
                includeMins = true;
            }
        });

        var tableStr = '<table class="chart-legend">',
                headStr = '<thead><th></th><th></th>',
                footStr = '<tfoot><th></th><th>Total:</th>';

        if (includeCount) {
            headStr = headStr + '<th>Trips</th>';
            footStr = footStr + '<th>' + jlab.integerWithCommas(globalCountSum) + '</th>';
        }
        if (includeRate) {
            headStr = headStr + rateHeader;
            footStr = footStr + '<th>' + globalPerHour.toFixed(1) * 1 + '</th>';
        }
        if (includeLost) {
            headStr = headStr + '<th>Lost Hrs</th>';
            footStr = footStr + '<th>' + globalDurationSum.toFixed(1) * 1 + '</th>';
        }
        if (includeMins) {
            headStr = headStr + '<th>Mins /Trip</th>';
            footStr = footStr + '<th>' + (globalDurationSum * 60 / globalCountSum).toFixed(1) + '</th>';
        }

        if (includeCount || includeRate || includeLost || includeMins) {
            headStr = headStr + '</thead>';
            footStr = footStr + '</tfoot>';
        } else {
            headStr = ''; /*No header*/
            footStr = ''; /*No footer*/
        }

        tableStr = tableStr + headStr + footStr + '<tbody></tbody></table>';

        var $legendTable = $(tableStr);

        var startFmt = $("#second-page").attr("data-start"),
                endFmt = $("#second-page").attr("data-end");

        for (var i = 0; i < groupingNames.length; i++) {
            var url = '/dtm/reports/system-downtime?category='
                    + nameToIdMap[groupingNames[i]]
                    + '&start=' + encodeURIComponent(startFmt)
                    + '&end=' + encodeURIComponent(endFmt)
                    + '&chart=table'
                    + '&data=downtime'
                    + '&type=1'
                    + '&transport='
                    + '&qualified=';

            var rowStr = '<tr><th><a target="_blank" href="' + url + '"><div class="color-box" style="background-color: ' + colorMap[groupingNames[i]] + ';"></div></a></th><td>' + groupingNames[i] + '</td>';

            if (includePercent) {
                var v = 0;
                if (globalDurationSum !== 0) {
                    v = (durationMap[groupingNames[i]] / globalDurationSum * 100).toFixed(1);
                }
                rowStr = rowStr + '<td>' + v + '%</td>';
            }

            if (includeCount) {
                rowStr = rowStr + '<td>' + countMap[groupingNames[i]] + '</td>';
            }
            if (includeRate) {
                rowStr = rowStr + '<td>' + tripPerHourMap[groupingNames[i]].toFixed(1) + '</td>';
            }
            if (includeLost) {
                rowStr = rowStr + '<td>' + durationMap[groupingNames[i]].toFixed(1) + '</td>';
            }
            if (includeMins) {
                rowStr = rowStr + '<td>' + (durationMap[groupingNames[i]] * 60 / countMap[groupingNames[i]]).toFixed(1) + '</td>';
            }

            rowStr = rowStr + '</tr>';
            $legendTable.find("tbody").append(rowStr);
        }

        var footnote = $(".legend-panel .footnote-wrapper").html();

        $(".legend-panel").html($legendTable);
        $(".legend-panel").append(footnote);

        jlab.fakeDecimalAlign($(".chart-legend tbody"), ["td:nth-child(3)", "td:nth-child(4)", "td:nth-child(5)", "td:nth-child(6)"]);
    }

    var chartData = {};

    chartData.nameToIdMap = nameToIdMap;
    chartData.groupingNames = groupingNames;
    chartData.colorMap = colorMap;
    chartData.tripPerHourMap = tripPerHourMap;
    chartData.ds = ds;
    chartData.grouped = grouped;
    chartData.globalTripsPerHour = globalPerHour;
    chartData.hourly = (intervalHours === 1);

    return chartData;
};

jlab.addTooltips = function (stack, includeHours) {
    $("<div id='tooltip'></div>").css({
        position: "absolute",
        display: "none",
        border: "1px solid #fdd",
        padding: "2px",
        "background-color": "#fee",
        opacity: 0.80
    }).appendTo("body");

    $("#chart-placeholder").bind("plothover", function (event, pos, item) {

        if (item) {
            $("#chart-placeholder").css("cursor", "pointer");

            var x = item.datapoint[0].toFixed(2) * 1,
                    y = item.datapoint[1].toFixed(2) * 1,
                    label = '',
                    d = new Date(x),
                    dStr = jlab.triCharMonthNames[d.getUTCMonth()] + ' ' + jlab.pad(d.getUTCDate(), 2); /*WARNING: using utc as it is our "fake" local time!*/

            if (includeHours) {
                dStr = dStr + ' ' + jlab.pad(d.getUTCHours(), 2) + ':00';
            }

            if (stack) {
                y = (item.datapoint[1] - item.datapoint[2]).toFixed(2) * 1;
            }

            if (item.series.label !== '') {
                label = " {" + item.series.label + "}";
            }

            $("#tooltip").html(dStr + label + " (" + y + ")")
                    .css({top: item.pageY - 30, left: item.pageX + 5})
                    .fadeIn(200);
        } else {
            $("#chart-placeholder").css("cursor", "default");
            $("#tooltip").stop().hide();
        }
    });
};

jlab.addAxisLabels = function (daily) {

    var yAxisText = "Repair Hours",
            xAxisText = "Hour";

    if (daily) {
        xAxisText = "Day";
    }

    jlab.addXAxisLabel(xAxisText);
    jlab.addYAxisLabel(yAxisText);
};

jlab.doBarChart = function (stack) {
    var startMillis = $("#graph-data-table").attr("data-start-millis"),
            endMillis = $("#graph-data-table").attr("data-end-millis");

    var chartData = jlab.getDataSource(true);
    var ds = chartData.ds;

    var intervalHours = 24;
    var minTickSizeX = 'day';
    var daily = true;

    if (intervalHours < 24) {
        minTickSizeX = 'hour';
        daily = false;
    }

    if (ds.length === 0) {
        $('<div>No data to chart</div>').insertBefore($("#chart-placeholder"));
        return;
    }

    jlab.addTooltips(stack, (intervalHours < 24));

    /*var markings = [];*/

    /*if (chartData.grouped) {
     for (var i = 0; i < chartData.groupingNames.length; i++) {
     var yValue = chartData.tripPerHourMap[chartData.groupingNames[i]].toFixed(1) * 1;
     markings.push({color: chartData.colorMap[chartData.groupingNames[i]], lineWidth: 2, yaxis: {from: yValue, to: yValue}});
     }
     }*/

    var minX = startMillis - (60 * 60 * 1000 * (intervalHours / 2)), /* half interval offset due to 'centered' bars*/
            maxX = (endMillis * 1) + (60 * 60 * 1000 * (intervalHours / 2)); /* x 1 to idicate number not string; half interval offset due to 'centered' bars*/

    $("#chart-wrap").addClass("has-y-axis-label").addClass("has-x-axis-label");

    jlab.flotplot = $.plot($("#chart-placeholder"), ds, {
        series: {
            stack: stack,
            bars: {
                show: true,
                lineWidth: 1,
                fill: 0.1,
                align: "center", /*need to adjust min and max ticks for this*/
                barWidth: 60 * 60 * 1000 * intervalHours / 2 /* milliseconds to 1 hour */
            }
        },
        /* We create the legend ourselves since we want to avoid haveing the plot area determined first then the legend inserted (we want legend to take up the space it needs first then the plot can determine what is left over*/
        legend: {
            show: false
                    /*sorted: true, 
                     container: $(".key-panel")*/
        },
        xaxis: {
            mode: "time",
            timezone: null, /*UTC data (timezone-less) - converted to America/New_York server side*/
            //timezone: "browser",
            //timezone: "America/New_York", /*Timezone Handling*/
            minTickSize: [1, minTickSizeX],
            min: minX,
            max: maxX
        },
        yaxes: [{
                min: 0,
                minTickSize: 1,
                tickDecimals: 0,
                ticks: 5
            }, {
                position: 'right', /*Last x-axis label may wrap if we don't do this*/
                reserveSpace: true,
                labelWidth: 16
            }],
        grid: {
            borderWidth: 1,
            borderColor: 'gray',
            hoverable: true,
            clickable: true,
            backgroundColor: {colors: ["#fff", "#eee"]}
            /*markings: markings*/
        }
    });

    jlab.addAxisLabels(daily);

    $("#chart-placeholder").resize(function () {
        jlab.doMarkingLines(chartData, minX, maxX);
    });

    /*jlab.doMarkingLines(chartData, minX, maxX);*/

    if (stack) {
        $("#chart-placeholder").on("plotclick", function (event, pos, item) {
            if (item) {

                var x = item.datapoint[0].toFixed(2) * 1,
                        start = new Date(x),
                        end = new Date(x);


                if (intervalHours < 24) { /*WARNING: using our "fake" UTC local time*/
                    end.setUTCHours(end.getUTCHours() + 1);
                } else {
                    end.setUTCDate(end.getUTCDate() + 1);
                }


                var url = '/dtm/reports/system-downtime?category='
                        + chartData.nameToIdMap[item.series.label]
                        + '&start=' + encodeURIComponent(jlab.dateTimeToGlobalUTCString(start))
                        + '&end=' + encodeURIComponent(jlab.dateTimeToGlobalUTCString(end))
                        + '&chart=table'
                        + '&data=downtime'
                        + '&type=1'
                        + '&transport='
                        + '&qualified=';

                window.open(url);
            }
        });
    }
};




var monthNames = ["January", "February", "March", "April",
    "May", "June", "July", "August", "September",
    "October", "November", "December"];
setMonthPicker = function () {
    var datestr, year, month, monthStr, tokens;
    if ((datestr = $(this).val()).length > 0) {
        tokens = datestr.split(" ");
        if (tokens.length > 1) {
            year = tokens[1] * 1;
            monthStr = tokens[0];
            month = monthNames.indexOf(monthStr);
        }
        $(this).datepicker('option', 'defaultDate', new Date(year, month, 1));
        $(this).datepicker('setDate', new Date(year, month, 1));
    }
};
doSave = function () {

    if (jlab.isRequest()) {
        window.console && console.log("Ajax already in progress");
        return;
    }

    jlab.requestStart();

    $("#save-button").width($("#save-button").width());
    $("#save-button").height($("#save-button").height());
    $("#save-button").html("<span class=\"button-indicator\"></span>");
    $("#save-button").attr("disabled", "disabled");
    $("#cancel-button").attr("disabled", "disabled");

    var month = $("#second-page").attr("data-start"),
            machineGoal = $("#machineGoalInput").val(),
            tripGoal = $("#tripGoalInput").val(),
            eventGoal = $("#eventGoalInput").val(),
            note = $("#noteTextArea").val(),
            catIdArray = [],
            catGoalArray = [];

    $("#bar-chart-data-table tbody tr").each(function () {
        var $tr = $(this),
                id = $tr.attr("data-id"),
                goal = $tr.find(".goal-input input").val();
        catIdArray.push(id);
        catGoalArray.push(goal);
    });

    var request = jQuery.ajax({
        url: "/dtm/ajax/save-monthly-info",
        type: "POST",
        data: {
            month: month,
            machineGoal: machineGoal,
            tripGoal: tripGoal,
            eventGoal: eventGoal,
            note: note,
            'catId[]': catIdArray,
            'catGoal[]': catGoalArray
        },
        dataType: "html"
    });

    request.done(function (data) {
        if ($(".status", data).html() !== "Success") {
            alert('Unable to save: ' + $(".reason", data).html());
        } else {
            /* Success */

            var sign = "%";

            if (machineGoal === '') {
                sign = "";
            }
            $("#machineGoalInput").closest("td").find(".goal-output").text(machineGoal + sign);

            sign = "%";
            if (tripGoal === '') {
                sign = "";
            }
            $("#tripGoalInput").closest("td").find(".goal-output").text(tripGoal + sign);

            sign = "%";
            if (eventGoal === '') {
                sign = "";
            }
            $("#eventGoalInput").closest("td").find(".goal-output").text(eventGoal + sign);


            $("#notes-output").text(note);

            $("#bar-chart-data-table tbody tr").each(function () {
                var $tr = $(this),
                        goal = $tr.find(".goal-input input").val(),
                        outSpan = $tr.find(".goal-output"),
                        sign = "%";

                if (goal === '') {
                    sign = "";
                }
                outSpan.text(goal + sign);
            });

            $("#edit-button").show();
            $("#save-button").hide();
            $("#cancel-button").hide();

            $("#notes-output").show();
            $("#notes-input").hide();

            $(".goal-output").show();
            $(".goal-input").hide();
        }

    });

    request.error(function (xhr, textStatus) {
        window.console && console.log('Unable to save: Text Status: ' + textStatus + ', Ready State: ' + xhr.readyState + ', HTTP Status Code: ' + xhr.status);
        alert('Unable to save: server did not handle request');
    });

    request.always(function () {
        jlab.requestEnd();
        $("#save-button").html("Save");
        $("#save-button").removeAttr("disabled");
        $("#cancel-button").removeAttr("disabled");
    });
};
$(document).on("click", "#edit-button", function () {
    $("#edit-button").hide();
    $("#save-button").show();
    $("#cancel-button").show();

    $("#notes-output").hide();
    $("#notes-input").show();

    $(".goal-output").hide();
    $(".goal-input").show();
});
$(document).on("click", "#cancel-button", function () {
    $("#edit-button").show();
    $("#save-button").hide();
    $("#cancel-button").hide();

    $("#notes-output").show();
    $("#notes-input").hide();

    $(".goal-output").show();
    $(".goal-input").hide();
});
$(document).on("click", "#save-button", function () {
    doSave();
});
$(function () {

    /*timezoneJS.timezone.zoneFileBasePath = '/dtm/resources/tz';
    timezoneJS.timezone.init({async: false});*/

    $(".monthpicker").datepicker({
        monthNamesShort: monthNames,
        dateFormat: 'MM yy',
        changeMonth: true,
        changeYear: true,
        showButtonPanel: true,
        onClose: function () {
            var month = $("#ui-datepicker-div .ui-datepicker-month :selected").val();
            var year = $("#ui-datepicker-div .ui-datepicker-year :selected").val();
            $(this).val($.datepicker.formatDate('MM yy', new Date(year, month, 1)));
        },
        beforeShow: function (input, inst) {
            setMonthPicker.call(this);
        }
    });

    if ($("#chart-placeholder").length > 0) {
        jlab.doBarChart(true);
    }
});


