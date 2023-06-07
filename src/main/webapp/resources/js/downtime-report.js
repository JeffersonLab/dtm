var jlab = jlab || {};

jlab.ticks = [];

jlab.mousePosition = {x: 0, y: 0};

$(document).on("mousemove", "#chart-placeholder", function (e) {
    jlab.mousePosition.x = e.clientX || e.pageX;
    jlab.mousePosition.y = e.clientY || e.pageY;
});

/*document.addEventListener('mousemove', function(e){ 
 jlab.mousePosition.x = e.clientX || e.pageX; 
 jlab.mousePosition.y = e.clientY || e.pageY;
 }, false);*/

jlab.flotPercentFormatter = function (v, axis) {
    return v.toFixed(axis.tickDecimals) + "%";
};

jlab.addAxisLabels = function () {
    var selectedValue = $("#data option:selected").val(),
            yAxisText = "Incident Downtime (Hours)",
            xAxisText = $("#bar-chart-data-table").attr("data-x-label");

    if (selectedValue === 'count') {
        yAxisText = "Incident Count";
    } else if (selectedValue === 'mttr') {
        yAxisText = "Mean Time To Recover (Hours)";
    } else if (selectedValue === 'restore') {
        yAxisText = "Restore (Hours)";
    }

    jlab.addXAxisLabel(xAxisText);
    jlab.addYAxisLabel(yAxisText);
};

jlab.addParetoTooltips = function () {
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
            var x = item.datapoint[0],
                    y = item.datapoint[1].toFixed(1) * 1,
                    label = jlab.ticks[x][1];

            if (item.series.isPercent) {
                y = y + "%";
            }

            $("#tooltip").html(label + " (" + y + ")")
                    .css({top: item.pageY - 30, left: item.pageX + 5})
                    .fadeIn(200);
        } else {
            $("#tooltip").stop().hide();
        }
    });
};

jlab.addPieTooltips = function () {
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
            var x = item.datapoint[0].toFixed(1) * 1,
                    y = item.datapoint[1],
                    label = item.series.label;

            $("#tooltip").html(label + " [" + (y[0][1]).toFixed(1) * 1 + "] (" + x + "%)")
                    .css({top: jlab.mousePosition.y - 30, left: jlab.mousePosition.x + 5})
                    .fadeIn(200);
        } else {
            $("#tooltip").stop().hide();
        }
    });
};

jlab.doParetoChart = function () {
    var series1 = []; /* Bars */
    var series2 = []; /* Points, Lines */
    var totalDuration = 0.0;
    var limit = 5;
    var otherDuration = 0;
    var totalRecords = $("#bar-chart-data-table tbody tr").length;
    var otherNumIncidents = 0;

    $("#bar-chart-data-table tbody tr").each(function (index, value) {
        var entity;
        if ($("td:nth-child(1) a").length > 0) {
            entity = $("td:nth-child(1) a", value).text(); /*take only text in a if exists for component report*/
        } else {
            entity = $("td:nth-child(1)", value).text();
        }
        entity = entity.trim();
        entity = jlab.truncateStr(entity, 19);
        var cellText = $("td." + jlab.flotSourceColumnClass, value).text().replace(/,/g, '');
        if (jlab.flotNonOverlapping) {
            var cellTextArray = cellText.split("(");
            if (cellTextArray.length > 1) {
                cellText = cellTextArray[1];
            }
        }
        var duration = parseFloat(cellText);

        if (index >= (limit - 1)) {
            if (jlab.flotSourceColumnClass === 'mttr') {
                cellText = $("td." + 'downtime', value).text().replace(/,/g, '');
                if (jlab.flotNonOverlapping) {
                    var cellTextArray = cellText.split("(");
                    if (cellTextArray.length > 1) {
                        cellText = cellTextArray[1];
                    }
                }
                var duration = parseFloat(cellText);
                cellText = $("td." + 'count', value).text().replace(/,/g, '');
                if (jlab.flotNonOverlapping) {
                    var cellTextArray = cellText.split("(");
                    if (cellTextArray.length > 1) {
                        cellText = cellTextArray[1];
                    }
                }
                var numIncidents = parseFloat(cellText);

                otherNumIncidents = otherNumIncidents + numIncidents;
            } else {
                totalDuration = totalDuration + duration;
            }

            otherDuration = otherDuration + duration;
            return;
        } else {
            totalDuration = totalDuration + duration;
        }
        jlab.ticks.push([index, entity]);
        series1.push([index, duration]);
    });

    if (totalRecords >= limit) {
        if (jlab.flotSourceColumnClass === 'mttr') {
            otherDuration = otherDuration / otherNumIncidents;
            totalDuration = totalDuration + otherDuration;
        }

        /*Insert Other in sorted order*/
        var inserted = false;
        for (var i = 0; i < limit - 1; i++) {
            if (series1[i][1] < otherDuration) {
                jlab.ticks.splice(i, 0, [i, 'Misc']);
                series1.splice(i, 0, [i, otherDuration]);
                inserted = true;
                break;
            }
        }
        if (!inserted) {
            jlab.ticks.push([limit - 1, 'Misc']);
            series1.push([limit - 1, otherDuration]);
        } else {
            /*Must fix internal numbering*/
            for (var i = 0; i < limit; i++) {
                jlab.ticks[i][0] = i;
                series1[i][0] = i;
            }
        }
    }

    var runningTotal = 0.0;
    for (var i = 0; i < series1.length; i++) {
        var duration = series1[i][1];
        runningTotal = runningTotal + duration;
        series2.push([i, (runningTotal / totalDuration) * 100]);
    }

    var ds = [];

    ds.push({
        data: series1,
        bars: {
            show: true,
            align: 'center',
            barWidth: 0.9
        },
        color: "lightblue"
    });

    ds.push({
        data: series2,
        points: {
            show: true
        },
        lines: {
            show: true
        },
        yaxis: 2,
        color: "orange",
        isPercent: true
    });

    jlab.addParetoTooltips();

    var fontSize = parseInt($("#chart-placeholder").css("font-size"));

    $("#chart-wrap").addClass("has-x-axis-label").addClass("has-y-axis-label");

    jlab.flotplot = $.plot($("#chart-placeholder"), ds, {
        yaxes: [{
                min: 0,
                max: totalDuration,
                labelWidth: (fontSize * 2.4)
            }, {
                position: 'right',
                min: 0,
                max: 100.01, /*0.01 fudge factor so last point always shows up*/
                tickFormatter: jlab.flotPercentFormatter,
                tickLength: 0
            }],
        xaxis: {
            ticks: jlab.ticks,
            labelWidth: (fontSize * 6)
        },
        grid: {
            hoverable: true,
            backgroundColor: {colors: ["#fff", "#eee"]}
            /*backgroundColor: { colors: ['#ffff00', '#fff8c6', '#ffffff'] }*/
        }
    });

    var mIndex = 0;

    $(".flot-x-axis .flot-tick-label").each(function () {
        var label = $(this).text();

        var href = false;
        var title = "";

        if (label !== 'Misc') {
            href = $("#bar-chart-data-table tbody tr:eq(" + mIndex + ")").find("td:nth-child(1) a").attr("href");
            title = $("#bar-chart-data-table tbody tr:eq(" + mIndex + ")").find("td:nth-child(1) a").attr("title");
            mIndex = mIndex + 1;
        }

        if (!title) {
            title = "";
        }

        if (href) {
            $(this).text("");
            $(this).append('<a href="' + href + '" title="' + title + '">' + label + '</a>');
        }
    });

    jlab.addAxisLabels();
};

jlab.truncateStr = function (str, len) {
    if (str.length > len) {
        str = str.substr(0, len - 1);
    }
    return str;
};

jlab.doRowChart = function () {
    var series1 = [];
    var ticks = [];

    var totalRecords = $("#bar-chart-data-table tbody tr").length;

    $("#bar-chart-data-table tbody tr").each(function (index, value) {
        index = totalRecords - 1 - index;
        var entity;
        if ($("td:nth-child(1) a").length > 0) {
            entity = $("td:nth-child(1) a", value).text(); /*take only text in a if exists for component report*/
        } else {
            entity = $("td:nth-child(1)", value).text();
        }
        entity = entity.trim();
        entity = jlab.truncateStr(entity, 19);

        var cellText = $("td." + jlab.flotSourceColumnClass, value).text().replace(/,/g, '');
        if (jlab.flotNonOverlapping) {
            var cellTextArray = cellText.split("(");
            if (cellTextArray.length > 1) {
                cellText = cellTextArray[1];
            }
        }
        var duration = parseFloat(cellText);

        series1.push([duration, index]);
        ticks.push([index, entity]);
    });

    var ds = [];

    ds.push({
        data: series1,
        bars: {
            show: true,
            align: 'center',
            barWidth: 0.9,
            horizontal: true
        },
        grow: {growings: [{stepMode: "maximum", valueIndex: 0}], valueIndex: 0},
        color: "lightblue"
    });

    $("#chart-placeholder").height(ticks.length * 75);

    jlab.flotplot = $.plot($("#chart-placeholder"), ds, {
        series: {
            grow: {
                active: true,
                duration: 2000
            }
        },
        xaxis: {
            axisLabelPadding: 10,
            position: "top",
            min: 0
        },
        yaxis: {
            ticks: ticks,
            tickLength: 0
        },
        grid: {
            borderWidth: 0.5,
            backgroundColor: {colors: ["#fff", "#eee"]}
        }
    });

    $(".flot-y-axis .flot-tick-label").each(function (index) {
        var label = $(this).text();
        var href = $("#bar-chart-data-table tbody tr:eq(" + index + ")").find("td:nth-child(1) a").attr("href");
        var title = $("#bar-chart-data-table tbody tr:eq(" + index + ")").find("td:nth-child(1) a").attr("title");

        if (!title) {
            title = "";
        }

        if (href) {
            $(this).text("");
            $(this).append('<a href="' + href + '" title="' + title + '">' + label + '</a>');
        }
    });
};

jlab.doPieChart = function () {
    var ds = [];
    var limit = 8;
    var colors = ['lightblue', 'orange', 'green', 'red', 'purple', 'brown', 'pink', 'gold'];
    var otherDuration = 0;
    var otherNumIncidents = 0;

    var totalRecords = $("#bar-chart-data-table tbody tr").length;

    $("#bar-chart-data-table tbody tr").each(function (index, value) {
        var entity;
        if ($("td:nth-child(1) a").length > 0) {
            entity = $("td:nth-child(1) a", value).text(); /*take only text in a if exists for component report*/
        } else {
            entity = $("td:nth-child(1)", value).text();
        }
        entity = entity.trim();
        entity = jlab.truncateStr(entity, 19);
        var cellText = $("td." + jlab.flotSourceColumnClass, value).text().replace(/,/g, '');
        if (jlab.flotNonOverlapping) {
            var cellTextArray = cellText.split("(");
            if (cellTextArray.length > 1) {
                cellText = cellTextArray[1];
            }
        }
        var duration = parseFloat(cellText);

        if (index >= (limit - 1)) {
            if (jlab.flotSourceColumnClass === 'mttr') {

                cellText = $("td." + 'downtime', value).text().replace(/,/g, '');
                if (jlab.flotNonOverlapping) {
                    var cellTextArray = cellText.split("(");
                    if (cellTextArray.length > 1) {
                        cellText = cellTextArray[1];
                    }
                }
                var duration = parseFloat(cellText);
                cellText = $("td." + 'count', value).text().replace(/,/g, '');
                if (jlab.flotNonOverlapping) {
                    var cellTextArray = cellText.split("(");
                    if (cellTextArray.length > 1) {
                        cellText = cellTextArray[1];
                    }
                }
                var numIncidents = parseFloat(cellText);

                otherNumIncidents = otherNumIncidents + numIncidents;
            }

            otherDuration = otherDuration + duration;
            return;
        }

        ds.push({
            label: entity,
            data: duration,
            color: colors[index]
        });
    });

    if (totalRecords >= limit) {
        if (jlab.flotSourceColumnClass === 'mttr') {
            otherDuration = otherDuration / otherNumIncidents;
        }

        ds.push({
            label: 'Misc',
            data: otherDuration,
            color: colors[limit - 1]
        });
    }

    //$("#chart-placeholder").height(500);

    $("#chart-wrap").addClass("chart-wrap-backdrop");

    jlab.flotplot = $.plot($("#chart-placeholder"), ds, {
        series: {
            pie: {
                show: true
            }
        },
        grid: {
            hoverable: true
        },
        legend: {
            backgroundOpacity: 0
        }
    });

    var mIndex = 0;
    var i = 0;

    $(".legend .legendLabel").each(function () {
        var label = $(this).text();

        var data = jlab.flotplot.getData();
        var record = data[i++];

        var extra = "[" + (record.data[0][1]).toFixed(1) * 1 + "] (" + record.percent.toFixed(1) + "%)";

        var href = false;
        var title = "";

        if (label !== 'Misc') {
            href = $("#bar-chart-data-table tbody tr:eq(" + mIndex + ")").find("td:nth-child(1) a").attr("href");
            title = $("#bar-chart-data-table tbody tr:eq(" + mIndex + ")").find("td:nth-child(1) a").attr("title");
            mIndex = mIndex + 1;
        }

        if (!title) {
            title = "";
        }

        if (href) {
            $(this).text("");
            $(this).append('<a href="' + href + '" title="' + title + '">' + label + '</a> ' + extra);
        }
    });

    jlab.addPieTooltips();
};

jlab.replaceUrlParam = function (url, paramName, paramValue) {
    var pattern = new RegExp('(' + paramName + '=).*?(&|$)'),
            newUrl = url.replace(pattern, '$1' + paramValue + '$2');
    if (newUrl === url && newUrl.indexOf(paramName + '=') === -1) {
        newUrl = newUrl + (newUrl.indexOf('?') > 0 ? '&' : '?') + paramName + '=' + paramValue;
    }
    return newUrl;
};

jlab.chartChange = function () {
    if (jlab.flotplot !== null) {
        jlab.flotplot.shutdown();
    }

    $("#chart-placeholder").show();
    $("#data-table-panel").hide();

    var selected = $("#chart option:selected").val();

    /* Update URL without reloading page */
    /*var currentUrl = new String(window.location.href);
     var newUrl = jlab.replaceUrlParam(currentUrl, 'chart', selected);
     window.history.replaceState && window.history.replaceState('', '', newUrl);*/

    /* Update links without reloading page */
    /*$("#bar-chart-data-table tbody tr").each(function() {
     var $a = $(this).find("td:nth-child(1) a"),
     replacedUrl = jlab.replaceUrlParam($a.attr("href"), 'chart', selected);
     $a.attr("href", replacedUrl);
     });*/

    if (selected === 'bar') {
        jlab.doRowChart();
    } else if (selected.startsWith('pie')) {
        jlab.doPieChart();
    } else if (selected.startsWith('pareto')) {
        jlab.doParetoChart();
    } else {
        $("#chart-placeholder").hide();
        $("#data-table-panel").show();
    }
};

jlab.dataChange = function () {
    jlab.flotSourceColumnClass = $("#data option:selected").val();
    jlab.flotNonOverlapping = $("#packed option:selected").val() === "Y";

    /* Update URL without reloading page */
    /*var currentUrl = new String(window.location.href);
     var newUrl = jlab.replaceUrlParam(currentUrl, 'data', jlab.flotSourceColumnClass);
     window.history.replaceState && window.history.replaceState('', '', newUrl); */

    /* Update links without reloading page */
    /*$("#bar-chart-data-table tbody tr").each(function() {
     var $a = $(this).find("td:nth-child(1) a"),
     replacedUrl = jlab.replaceUrlParam($a.attr("href"), 'data', jlab.flotSourceColumnClass);
     $a.attr("href", replacedUrl);
     });*/

    var sorted = $("#bar-chart-data-table tbody tr").sort(jlab.tableRowSortReverse);

    $("#bar-chart-data-table tbody").append(sorted);

    $("#bar-chart-data-table thead th").removeClass("selected-column");
    $("#bar-chart-data-table thead").find("." + jlab.flotSourceColumnClass).addClass("selected-column");
};

jlab.tableRowSortReverse = function (a, b) {
    var aText = parseFloat($(a).find("." + jlab.flotSourceColumnClass).text().replace(/,/g, ''));
    var bText = parseFloat($(b).find("." + jlab.flotSourceColumnClass).text().replace(/,/g, ''));
    if (aText > bText)
        return -1;
    if (aText < bText)
        return 1;
    return 0;
};

jlab.doChartLoad = function () {
    jlab.dataChange();
    jlab.chartChange();
};

/*$(document).on("change", "#chart", function() {
 jlab.chartChange();
 });
 
 $(document).on("change", "#data", function() {
 jlab.dataChange();
 jlab.chartChange();
 });*/

$(document).on("click", ".default-reset-panel", function () {
    $("#date-range").val('past7days').change();
    /*$("#start").val('');
     $("#end").val('');*/
    $("#type").val('1');
    $("#packed").val('Y');
    $("#transport").val('N');
    $("#chart").val('bar');
    $("#data").val('downtime');
    $("#category").val('');
    $("#system").val('');
    return false;
});

$(function () {

    $("#fullscreen-button, #exit-fullscreen-button").button();

    jlab.flotplot = null;
    jlab.flotSourceColumnClass = 'downtime';

    if ($("#chart-placeholder").length > 0) {
        /*jlab.doRowChart();*/
        /*$("#data").change();*/
        jlab.doChartLoad();
    }
});