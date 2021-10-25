var jlab = jlab || {};

jlab.sumDurationArray = function(myArray) {
    var sum = 0;
    for (var i = 0; i < myArray.length; i++) {
        sum = sum + myArray[i][1];
    }
    return sum;
};

jlab.getDataSource = function() {
    var dataMap = {};

    /*Keep consistent colors when toggling series*/
    var colorMap = {
        'Beam Dumps': 'red',
        'Beam Transport': 'blue',
        'Control System': 'green',
        'Cryo': 'orange',
        'Diagnostics': 'purple',
        'Facilities': 'gold',
        'Gun/Laser': 'pink',
        'Hall Downtime': 'maroon',
        'Info Systems': '#008080', /*teal; not recognized by bar chart by name (fill color)!*/
        'Magnets': 'fuchsia',
        'Nature': 'yellow',
        'Operations': 'lime',
        'RF': 'aqua',
        'Radiation Controls': 'olive',
        'Safety Systems': 'navy',
        'Unknown/Missing': 'black',
        'Vacuum': 'silver'
    }; /* see: https://developer.mozilla.org/en-US/docs/Web/CSS/color_value*/


    $("#bar-chart-data-table tbody tr").each(function(index, value) {
        var timestamp = $("td:nth-child(1)", value).attr("data-date-utc"),
                duration = parseFloat($("td." + jlab.flotSourceColumnClass, value).text().replace(/,/g, '')),
                category = $("td:last-child", value).text(),
                series = dataMap[category] || [];

        series.push([timestamp, duration]);

        dataMap[category] = series;
    });

    var ds = [];

    for (var key in dataMap) {
        var sum = jlab.sumDurationArray(dataMap[key]);

        if (sum > 0) {
            ds.push({
                label: key,
                data: dataMap[key],
                color: colorMap[key]
            });
        }
    }

    if (ds.length === 1 && ds[0].label === '') { // Use consistent color for total series
        ds[0].color = 'red';
    }

    return ds;
};

jlab.addTooltips = function(stack, includeHours) {
    $("<div id='tooltip'></div>").css({
        position: "absolute",
        display: "none",
        border: "1px solid #fdd",
        padding: "2px",
        "background-color": "#fee",
        opacity: 0.80
    }).appendTo("body");

    $("#chart-placeholder").bind("plothover", function(event, pos, item) {

        if (item) {
            var x = item.datapoint[0].toFixed(2) * 1,
                    y = item.datapoint[1].toFixed(2) * 1,
                    label = '',
                    d = new Date(x),
                    dStr = jlab.triCharMonthNames[d.getUTCMonth()] + ' ' + jlab.pad(d.getUTCDate(), 2); /*Timezone Handling*/

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
            $("#tooltip").stop().hide();
        }
    });
};

jlab.addAxisLabelsAndPositionLegend = function() {
    $(".legend > div").css("right", "3.75em").css("top", "0.75em");
    $(".legend table").css("right", "3.75em").css("top", "0.75em");

    var yPrefix = $("#bar-chart-data-table").attr("data-y-axis-prefix");

    var selectedValue = $("#data option:selected").val(),
            yAxisText = yPrefix + " Downtime (Hours)",
            xAxisText = "Date";

    if (selectedValue === 'count') {
        yAxisText = yPrefix + " Count";
    } else if (selectedValue === 'mttr') {
        yAxisText = "Mean Time To Recover (Hours)";
    }

    jlab.addXAxisLabel(xAxisText);
    jlab.addYAxisLabel(yAxisText);
};

jlab.doLineChart = function(includePoints) {
    var startMillis = $("#bar-chart-data-table").attr("data-start-millis"),
            endMillis = $("#bar-chart-data-table").attr("data-end-millis");

    var ds = jlab.getDataSource();

    var intervalHours = $("#interval").val();
    var minTickSizeX = 'day';

    if (intervalHours < 24) {
        minTickSizeX = 'hour';
    }

    var minTickSizeY = undefined,
            tickDecimalsY = undefined;

    if (jlab.flotSourceColumnClass === 'count-data') {
        minTickSizeY = 1;
        tickDecimalsY = 0;
    }

    if (ds.length === 0) {
        $('<div>No data to chart</div>').insertBefore($("#chart-placeholder"));
        return;
    }

    /*If only one series then plot trend line too*/
    if (ds.length === 1) {
        var lineFitSeries = jlab.lineFit(ds[0].data);

        ds.push({
            data: lineFitSeries,
            color: 'blue'
        });
    }

    jlab.addTooltips(false, (intervalHours < 24));

    jlab.flotplot = $.plot($("#chart-placeholder"), ds, {
        series: {
            lines: {
                show: true
            },
            points: {
                show: includePoints
            }
        },
        legend: {
            show: true
        },
        xaxis: {
            mode: "time",
            /*timezone: "browser",*/  /*Timezone Handling*/
            minTickSize: [1, minTickSizeX],
            min: startMillis,
            max: endMillis
        },
        yaxes: [{
                min: 0,
                minTickSize: minTickSizeY,
                tickDecimals: tickDecimalsY
            }, {
                position: 'right', /*Last x-axis label may wrap if we don't do this*/
                reserveSpace: true
            }],
        grid: {
            borderWidth: 1,
            hoverable: true,
            backgroundColor: {colors: ["#fff", "#eee"]}
        }
    });

    jlab.addAxisLabelsAndPositionLegend();
};

jlab.doBarChart = function(stack) {
    var startMillis = $("#bar-chart-data-table").attr("data-start-millis"),
            endMillis = $("#bar-chart-data-table").attr("data-end-millis");

    var ds = jlab.getDataSource();

    var intervalHours = $("#interval").val();
    var minTickSizeX = 'day';

    if (intervalHours < 24) {
        minTickSizeX = 'hour';
    }

    var minTickSizeY = undefined,
            tickDecimalsY = undefined;

    if (jlab.flotSourceColumnClass === 'count-data') {
        minTickSizeY = 1;
        tickDecimalsY = 0;
    }


    if (ds.length === 0) {
        $('<div>No data to chart</div>').insertBefore($("#chart-placeholder"));
        return;
    }

    var lineWidth = 1;
    var fill = 0.6;

    if (stack) {
        lineWidth = 0;
        fill = 0.9;
    }


    jlab.addTooltips(stack, (intervalHours < 24));


    jlab.flotplot = $.plot($("#chart-placeholder"), ds, {
        series: {
            stack: stack,
            bars: {
                show: true,
                lineWidth: lineWidth,
                fill: fill,
                align: "center", /*need to adjust min and max ticks for this*/
                barWidth: 60 * 60 * 1000 * intervalHours /* milliseconds to 1 hour */
            }
        },
        legend: {
            show: true
        },
        xaxis: {
            mode: "time",
            /*timezone: "browser",*/ /*Timezone Handling*/
            minTickSize: [1, minTickSizeX],
            min: startMillis - (60 * 60 * 1000 * (intervalHours / 2)), /* half interval offset due to 'centered' bars*/
            max: (endMillis * 1) + (60 * 60 * 1000 * (intervalHours / 2)) /* x 1 to idicate number not string; half interval offset due to 'centered' bars*/
        },
        yaxes: [{
                min: 0,
                minTickSize: minTickSizeY,
                tickDecimals: tickDecimalsY
            }, {
                position: 'right', /*Last x-axis label may wrap if we don't do this*/
                reserveSpace: true
            }],
        grid: {
            borderWidth: 1,
            hoverable: true,
            backgroundColor: {colors: ["#fff", "#eee"]}
        }
    });

    jlab.addAxisLabelsAndPositionLegend();
};

// calc slope and intercept
// then use resulting y = mx + b to create trendline
jlab.lineFit = function(points) {
    var sI = jlab.slopeAndIntercept(points);
    if (sI) {
        // we have slope/intercept, get points on fit line
        var N = points.length;
        var rV = [];
        /*rV.push([points[0][0], sI.slope * points[0][0] + sI.intercept]);
         rV.push([points[N - 1][0], sI.slope * points[N - 1][0] + sI.intercept]);*/
        rV.push([points[0][0], sI.slope * 1 + sI.intercept]);
        rV.push([points[N - 1][0], sI.slope * points.length + sI.intercept]);
        return rV;
    }
    return [];
};

// simple linear regression
jlab.slopeAndIntercept = function(points) {
    var rV = {},
            N = points.length,
            sumX = 0,
            sumY = 0,
            sumXx = 0,
            sumYy = 0,
            sumXy = 0;

    // can't fit with 0 or 1 point
    if (N < 2) {
        return rV;
    }

    for (var i = 0; i < N; i++) {
        var x = i,
                /*var x = points[i][0],*/
                y = points[i][1];
        sumX += x;
        sumY += y;
        sumXx += (x * x);
        sumYy += (y * y);
        sumXy += (x * y);
    }

    // calc slope and intercept
    rV['slope'] = ((N * sumXy) - (sumX * sumY)) / (N * sumXx - (sumX * sumX));
    rV['intercept'] = (sumY - rV['slope'] * sumX) / N;
    rV['rSquared'] = Math.abs((rV['slope'] * (sumXy - (sumX * sumY) / N)) / (sumYy - ((sumY * sumY) / N)));

    /*console.log('slope: ' + rV.slope);
     console.log('intercept: ' + rV.intercept);
     console.log('rSquared: ' + rV.rSquared);*/

    return rV;
};

$(document).on("change", "#weekly-range", function() {
    var selected = $("#weekly-range option:selected").val();

    switch (selected) {
        case '1week':
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
        case '2weeks':
            var start = new Date(),
                    end = new Date();

            end.setMilliseconds(0);
            end.setSeconds(0);
            end.setMinutes(0);
            end.setHours(7);

            start.setTime(end.getTime());
            start.setDate(start.getDate() - 14);

            jlab.updateDateRange(start, end);
            break;
        case '4weeks':
            var start = new Date(),
                    end = new Date();

            end.setMilliseconds(0);
            end.setSeconds(0);
            end.setMinutes(0);
            end.setHours(7);

            start.setTime(end.getTime());
            start.setDate(start.getDate() - 28);

            jlab.updateDateRange(start, end);
            break;
        case 'custom':
            $("#custom-date-range-list").show();
            break;
    }

});

$(function() {

    $("#fullscreen-button, #exit-fullscreen-button").button();

    $(".date-only-field").datepicker({
        dateFormat: 'dd-M-yy'
    });

    /*timezoneJS.timezone.zoneFileBasePath = '/dtm/resources/tz';
     timezoneJS.timezone.init({ async: false });*/

    jlab.flotplot = null;
    jlab.flotSourceColumnClass = 'downtime-data';

    if ($(".selected-column").hasClass("count-data")) {
        jlab.flotSourceColumnClass = 'count-data';
    } else if ($(".selected-column").hasClass("mttr-data")) {
        jlab.flotSourceColumnClass = 'mttr-data';
    }

    if ($("#chart-placeholder").length > 0) {
        var selected = $("#chart option:selected").val();

        if (selected === 'line') {
            jlab.doLineChart(false);
        } else if (selected === 'linewithpoints') {
            jlab.doLineChart(true);
        } else if (selected.startsWith('bar')) {
            jlab.doBarChart($("#grouping").val() === 'category');
        }
    }

    $("#category-select").select2({
        width: 290
    });
});

/**
 * Timezone handling note:
 * 
 * JavaScript has no official way to calculate timezone offset and daylight 
 * savings offsets for a given date; it only knows the client browser's current
 * offset for the present moment in time.   In other words it doesn't have access
 * to a database of the worlds timezone and daylight savings rules past, 
 * present, and future.
 * 
 * There are third party libraries such as timezone.js and moment-timezone.js, 
 * but they are bulky and in the case of the former limited w.r.t. daylight 
 * savings.
 * 
 * The solution used here is to assume everything is in UTC.  We use the "UTC" 
 * accessor methods of the JavaScript Date object and we create the Date object
 * using milliseconds since the Epoch in UTC.   This works, except our users 
 * actually want to see dates in timezone EST/EDT "America/New_York", which has 
 * either a -5 or a -4 offset depending on daylight savings.  The browser can be 
 * told to use a constant offset, but that isn't good enough and will be wrong 
 * roughly half of the year.  Instead, we do the offset
 * calculation on the server and provide the milliseconds since the Epoch in UTC,
 * but they are already offset to "America/New_York" (actually server local time).
 */