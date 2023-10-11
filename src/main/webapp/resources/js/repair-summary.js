var jlab = jlab || {};

jlab.getDataSource = function (bar) {
    var dataMap = {};

    /*Keep consistent colors when toggling series*/
    var colorMap = {
        'Alignment': '#33a02c',
        'CASA': '#6a3d9a',
        'Cryo': 'silver',
        'DC Power': 'navy',
        'ENP ESH&Q': 'green',
        'ENP Target': 'silver',
        'ESH&Q': 'aqua',
        'Facilities': '#f5f5dc', /*beige*/
        'Gun (CIS)': '#b15928',
        'HCO Committee': '#008080', /*teal; not recognized by bar chart by name (fill color)!*/
        'Hall A': 'pink',
        'Hall B': 'fuchsia',
        'Hall C': 'gold',
        'Hall D': 'orange', /*Change me to something similar*/
        'High Level Apps': 'brown',
        'I&C Hardware': '#e31a1c',
        'Installation': '#ff7f00',
        'Low Level Apps': '#fdbf6f',
        'Magnet Measurement': '#fb9a99',
        'Operability': 'olive', /*Change me to something similar*/
        'Ops': '#a6cee3',
        'RADCON': '#1f78b4',
        'RF': 'blue',
        'SRF': '#cab2d6',
        'SSG': '#ffff99',
        'SysAdmin': 'lime',
        'Vacuum': '#f5f5dc' /*beige*/
    }; /* see: https://developer.mozilla.org/en-US/docs/Web/CSS/color_value*/

    var backupColors = ['red', 'blue', 'green', 'pink', 'teal', 'yellow'];

    var durationMap = {},
        countMap = {},
        programBasis = $("#rateBasis").val() === 'program',
        programHours = $("#filter-form").attr("data-program-hours") * 1;

    $("#bar-chart-data-table tbody tr").each(function (index, value) {
        var timestamp = $("td:nth-child(1)", value).attr("data-date-utc"),
            count = parseFloat($("td." + "new-count-data", value).text().replace(/,/g, '')),
            duration = parseFloat($("td." + "duration-data", value).text().replace(/,/g, '')),
            grouping = $("td.group-data", value).text(),
            series = dataMap[grouping] || {};

        series[timestamp] = duration;

        dataMap[grouping] = series;

        var groupDuration = durationMap[grouping] || 0;
        durationMap[grouping] = groupDuration + duration;

        var groupCount = countMap[grouping] || 0;
        countMap[grouping] = groupCount + count;
    });

    var ds = [], groupingNames = [];

    for (var key in dataMap) {
        groupingNames.push(key);
    }

    groupingNames.sort();

    console.log('dataMap', dataMap);
    console.log('groupingNames', groupingNames);


    /* We must fill in sparse data with zeros to satisfy stacked bars plugin */
    var startMillis = $("#bar-chart-data-table").attr("data-start-millis") * 1,
        endMillis = $("#bar-chart-data-table").attr("data-end-millis") * 1,
        xValues = [],
        binSize = $("#binSize").val();

    var startDate = new Date(startMillis);
    var endDate = new Date(endMillis);

    if (binSize === 'DAY') {
        /*console.log('start: ' + new Date(startMillis).toUTCString());
         console.log('end: ' + new Date(endMillis).toUTCString());*/
        for (var d = new Date(startDate); d.getTime() <= endDate.getTime(); d.setUTCDate(d.getUTCDate() + 1)) {
            xValues.push(new Date(d));
            /*console.log('tick: ' + d);*/
        }
    } else if (binSize === 'HOUR') {
        /*console.log('doing hourly; start: ' + startDate + '; end: ' + endDate);*/
        for (var d = new Date(startDate); d.getTime() <= endDate.getTime(); d.setUTCHours(d.getUTCHours() + 1)) {
            /*console.log('pushing: ' + d);*/
            xValues.push(new Date(d));
        }
    } else if (binSize === 'MONTH') {
        for (var d = new Date(startDate); d.getTime() <= endDate.getTime(); d.setUTCMonth(d.getUTCMonth() + 1)) {
            xValues.push(new Date(d));
        }
    } else {
        window.console && console.log("Can only graph monthly, daily, or hourly interval");
        return ds; /*Bail out... */
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

        var color = colorMap[groupingNames[i]];

        if(!color) {
            color = backupColors.pop();

            colorMap[groupingNames[i]] = color;
        }

        if (bar) {
            ds.push({
                label: groupingNames[i],
                data: fullDataMap[groupingNames[i]],
                color: 'black',
                bars: {
                    fillColor: color
                }
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

        var globalCountSum = 0;
        var globalDurationSum = 0;


        for (var i = 0; i < groupingNames.length; i++) {
            globalCountSum = globalCountSum + countMap[groupingNames[i]];
            globalDurationSum = globalDurationSum + durationMap[groupingNames[i]];
        }

        /*console.log('--> globalCountSum: ' + globalCountSum);*/

        var includeCount = false,
            includeLost = false;

        $("#legendData :selected").each(function () {
            var value = $(this).val();

            if (value === 'count') {
                includeCount = true;
            } else if (value === 'lost') {
                includeLost = true;
            }
        });

        var tableStr = '<table class="chart-legend">',
            headStr = '<thead><th></th><th></th>',
            footStr = '<tfoot><th></th><th>Total:</th>';

        if (includeCount) {
            headStr = headStr + '<th>Incidents</th>';
            footStr = footStr + '<th>' + jlab.integerWithCommas(globalCountSum) + '</th>';
        }
        if (includeLost) {
            headStr = headStr + '<th>Repair Hrs</th>';
            footStr = footStr + '<th>' + globalDurationSum.toFixed(1) * 1 + '</th>';
        }

        if (includeCount || includeLost) {
            headStr = headStr + '</thead>';
            footStr = footStr + '</tfoot>';
        } else {
            headStr = ''; /*No header*/
            footStr = ''; /*No footer*/
        }

        tableStr = tableStr + headStr + footStr + '<tbody></tbody></table>';

        var $legendTable = $(tableStr);

        var startFmt = $("#filter-form").attr("data-start"),
            endFmt = $("#filter-form").attr("data-end");

        for (var i = 0; i < groupingNames.length; i++) {
            var url = '/dtm/reports/incident-downtime?';

            if($("#grouping").val() === 'repairedby') {
                //url = url + 'group=' + encodeURIComponent(groupingNames[i]);
            }

            url = url
                + '&start=' + encodeURIComponent(startFmt)
                + '&end=' + encodeURIComponent(endFmt)
                + '&qualified=';

            var rowStr = '<tr><th><a target="_blank" href="' + url + '"><div class="color-box" style="background-color: ' + colorMap[groupingNames[i]] + ';"></div></a></th><td>' + groupingNames[i] + '</td>';

            if (includeCount) {
                rowStr = rowStr + '<td>' + jlab.integerWithCommas(countMap[groupingNames[i]]) + '</td>';
            }
            if (includeLost) {
                rowStr = rowStr + '<td>' + durationMap[groupingNames[i]].toFixed(1) + '</td>';
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

    chartData.groupingNames = groupingNames;
    chartData.colorMap = colorMap;
    chartData.ds = ds;
    chartData.grouped = grouped;
    chartData.binSize = binSize;

    console.log(chartData);

    return chartData;
};

jlab.addTooltips = function (stack, includeHours, includeDays) {
    $("<div id='tooltip'></div>").css({
        position: "absolute",
        display: "none",
        border: "1px solid #fdd",
        padding: "2px",
        "background-color": "#fee",
        opacity: 0.80,
        "z-index": 3
    }).appendTo("body");

    $("#chart-placeholder").bind("plothover", function (event, pos, item) {

        if (item) {
            $("#chart-placeholder").css("cursor", "pointer");

            /*WARNING: using utc as it is our "fake" local time!*/
            var x = item.datapoint[0].toFixed(2) * 1,
                y = item.datapoint[1].toFixed(2) * 1,
                label = '',
                d = new Date(x),
                dStr = jlab.triCharMonthNames[d.getUTCMonth()];

            if (includeDays) {
                dStr = dStr + ' ' + jlab.pad(d.getUTCDate(), 2);
            }

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

jlab.addAxisLabels = function (binSize) {

    var yAxisText = "Duration (Hours)",
        xAxisText = "Hour";

    if (binSize === 'DAY') {
        xAxisText = "Day";
    } else if (binSize === 'MONTH') {
        xAxisText = "Month";
    }

    jlab.addXAxisLabel(xAxisText);
    jlab.addYAxisLabel(yAxisText);
};

jlab.doBarChart = function (stack) {
    var startMillis = $("#bar-chart-data-table").attr("data-start-millis"),
        endMillis = $("#bar-chart-data-table").attr("data-end-millis");

    var chartData = jlab.getDataSource(true);
    var ds = chartData.ds;

    var binSize = $("#binSize").val();
    var minTickSizeX = 'day';
    var approximateHours = 24;

    if (binSize === 'HOUR') {
        minTickSizeX = 'hour';
        approximateHours = 1;
    } else if (binSize === 'MONTH') {
        minTickSizeX = 'month';
        approximateHours = 720; //30 days
    }

    if (ds.length === 0) {
        $('<div>No data to chart</div>').insertBefore($("#chart-placeholder"));
        return;
    }

    jlab.addTooltips(stack, binSize === 'HOUR', binSize !== 'MONTH');

    var markings = [];

    if (binSize === 'DAY') {
        markings = [
            {yaxis: {from: 119, to: 121}, color: "gray"},
            {yaxis: {from: 239, to: 241}, color: "gray"},
            {yaxis: {from: 359, to: 361}, color: "gray"}
        ];
    } else if (binSize === 'MONTH') {
        markings = [
            {yaxis: {from: 5 * 672, to: 5 * 672}, color: "gray"},
            {yaxis: {from: 10 * 672, to: 10 * 672}, color: "gray"},
            {yaxis: {from: 15 * 672, to: 15 * 672}, color: "gray"}
        ];
    } else { // HOUR
        markings = [
            {yaxis: {from: 5, to: 5}, color: "gray"},
            {yaxis: {from: 10, to: 10}, color: "gray"},
            {yaxis: {from: 15, to: 15}, color: "gray"}
        ];
    }

    var minX = startMillis - (60 * 60 * 1000 * (approximateHours / 2)), /* half interval offset due to 'centered' bars*/
        maxX = (endMillis * 1) + (60 * 60 * 1000 * (approximateHours / 2)); /* x 1 to idicate number not string; half interval offset due to 'centered' bars*/

    $("#chart-wrap").addClass("has-y-axis-label").addClass("has-x-axis-label");

    var options = {
        series: {
            stack: stack,
            bars: {
                show: true,
                lineWidth: 1,
                fill: 0.1,
                align: "center", /*need to adjust min and max ticks for this*/
                barWidth: 60 * 60 * 1000 * approximateHours / 2 /* milliseconds to 1 hour */
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
            backgroundColor: {colors: ["#fff", "#eee"]},
            markings: markings
        }
    };

    var maxY = $("#maxY").val();

    if (maxY !== '') {

        if (binSize === 'DAY') {
            maxY = maxY * 24;
        } else if (binSize === 'MONTH') {
            maxY = maxY * 672;
        }

        options.yaxes[0].max = maxY;
    }

    jlab.flotplot = $.plot($("#chart-placeholder"), ds, options);

    jlab.addAxisLabels(binSize);

    if (stack) {
        $("#chart-placeholder").on("plotclick", function (event, pos, item) {
            if (item) {

                /*WARNING: using our "fake" UTC local time*/
                var x = item.datapoint[0].toFixed(2) * 1,
                    start = new Date(x),
                    end = new Date(x);


                if (binSize === 'HOUR') {
                    end.setUTCHours(end.getUTCHours() + 1);
                } else if (binSize === 'DAY') {
                    end.setUTCDate(end.getUTCDate() + 1);
                } else { // MONTH
                    end.setUTCMonth(end.getUTCMonth() + 1);
                }

                var url = '/dtm/reports/incident-downtime?';

                url = url
                    + 'start=' + encodeURIComponent(jlab.dateTimeToGlobalUTCString(start))
                    + '&end=' + encodeURIComponent(jlab.dateTimeToGlobalUTCString(end))
                    + '&qualified=';

                window.open(url);
            }
        });
    }
};

$(document).on("click", ".default-reset-panel", function () {
    $("#date-range").val('past7days').trigger('change');
    $("#chart").val('bar');
    $("#binSize").val('DAY');
    $("#grouping").val('cause');
    $("#legendData").val(["rate", "lost"]).trigger('change');
    $("#repairedby").val(null).trigger('change');
    return false;
});

$(function () {

    $("#fullscreen-button, #exit-fullscreen-button").button();

    jlab.flotplot = null;
    jlab.flotSourceColumnClass = 'duration-data';

    if ($("#chart-placeholder").length > 0) {
        var selected = $("#chart option:selected").val();

        if (selected.startsWith('bar')) {
            jlab.doBarChart($("#grouping").val() != '');
        }
    }

    $("#legendData, #repairedby").select2({
        width: 290
    });
});