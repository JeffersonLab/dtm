var jlab = jlab || {};

jlab.getDataSource = function (bar) {
    var dataMap = {};

    /*Keep consistent colors when toggling series*/
    var colorMap = {
        'Dump (Multi/Other)': '#33a02c',
        'Dump (Insert.)': '#6a3d9a',
        'Dump (Station.)': 'silver',
        'Beam Transport': 'navy',
        'Control System': 'green',
        'Cryo': 'silver',
        'Diagnostics': 'aqua',
        'Facilities': '#f5f5dc', /*beige*/
        'Gun/Laser': '#b15928',
        'Info Systems': '#008080', /*teal; not recognized by bar chart by name (fill color)!*/
        'Magnets': 'pink',
        'Multiple/Other': 'fuchsia',
        'Nature': 'gold',
        'Operations': 'orange', /*Change me to something similar*/
        'RF': 'brown',
        'RF (C75/100)': '#e31a1c',
        'RF (C25/50)': '#ff7f00',
        'RF (Separator)': '#fdbf6f',
        'RF (Multi/Other)': '#fb9a99',
        'Radiation Controls': 'olive', /*Change me to something similar*/
        'MPS (Multi/Other)': '#a6cee3',
        'MPS (BLM)': '#1f78b4',
        'MPS (IC)': 'blue',
        'MPS (BCM/BLA)': '#cab2d6',
        'Unknown/Missing': '#ffff99',
        'Vacuum': 'lime',
        'Hall': '#f5f5dc' /*beige*/
    }; /* see: https://developer.mozilla.org/en-US/docs/Web/CSS/color_value*/

    /*Group by Area colors*/
    colorMap['Accelerator'] = 'orange';
    colorMap['Accelerator (RF)'] = 'brown';
    colorMap['Hall A'] = 'blue';
    colorMap['Hall B'] = 'red';
    colorMap['Hall C'] = 'white';
    colorMap['Hall D'] = 'silver';
    colorMap['Multiple'] = 'green';
    colorMap['Unknown'] = '#ffff99';

    var durationMap = {},
            programBasis = $("#rateBasis").val() === 'program',
            programHours = $("#filter-form").attr("data-program-hours") * 1;

    $("#bar-chart-data-table tbody tr").each(function (index, value) {
        var timestamp = $("td:nth-child(1)", value).attr("data-date-utc"),
                yvalue = parseFloat($("td." + jlab.flotSourceColumnClass, value).text().replace(/,/g, '')),
                duration = parseFloat($("td." + "duration-data", value).text().replace(/,/g, '')),
                grouping = $("td:last-child", value).text(),
                series = dataMap[grouping] || {};

        series[timestamp] = yvalue;

        dataMap[grouping] = series;

        var groupDuration = durationMap[grouping] || 0;
        durationMap[grouping] = groupDuration + duration;
    });

    var ds = [], groupingNames = [];

    for (var key in dataMap) {
        groupingNames.push(key);
    }

    /*var beforeSpringBoundary = new Date(2015, 02, 08, 01, 0, 0, 0); // 1 AM Local time before dst boundary (EST)
     var doesNotExist = new Date(2015, 02, 08, 02, 0, 0, 0); // 2 AM local time doesn't exist...    
     var afterSpringBoundary = new Date(2015, 02, 08, 03, 0, 0, 0); // 3 AM local time (EDT)
     
     console.log('beforeSpringBoundary: ' + beforeSpringBoundary);
     console.log('doesNotExist: ' + doesNotExist);
     console.log('afterSpringBoundary: ' + afterSpringBoundary);
     
     var beforeFallBoundary = new Date(2015, 10, 01, 00, 0, 0, 0); // 1 AM Local time before dst boundary (EDT)
     var ambiguous = new Date(2015, 10, 01, 01, 0, 0, 0); // 1 AM local time ambiguous (there are two)...    
     var afterFallBoundary = new Date(2015, 10, 01, 02, 0, 0, 0); // 2 AM local time (EST)
     
     console.log('beforeFallBoundary: ' + beforeFallBoundary);
     console.log('ambiguous: ' + ambiguous);
     console.log('afterFallBoundary: ' + afterFallBoundary);
     
     var beforeBoundaryPlusDay = new Date(beforeSpringBoundary);
     beforeBoundaryPlusDay.setDate(beforeSpringBoundary.getDate() + 1);
     console.log('beforeSpringBoundaryPlusDay: ' + beforeBoundaryPlusDay);
     var beforeBoundaryPlusHour = new Date(beforeSpringBoundary);
     beforeBoundaryPlusHour.setHours(beforeSpringBoundary.getHours() + 1);
     console.log('beforeSpringBoundaryPlusHour: ' + beforeBoundaryPlusHour);
     var beforeFallBoundaryPlusHour = new Date(beforeFallBoundary);
     beforeFallBoundaryPlusHour.setHours(beforeFallBoundary.getHours() + 1);
     console.log('beforeFallBoundaryPlusHour: ' + beforeFallBoundaryPlusHour);
     var ambiguousPlusHour = new Date(ambiguous);
     ambiguousPlusHour.setHours(ambiguous.getHours() + 1);
     console.log('ambiguousPlusHour: ' + ambiguousPlusHour);*/

    /*var beforeFallBoundary = new Date(2015, 10, 01, 00, 0, 0, 0); // 0 AM (midnight) Local time before dst boundary (EDT)  
     var afterFallBoundary = new Date(2015, 10, 01, 03, 0, 0, 0); // 3 AM local time (EST)
     
     for(var d = new Date(beforeFallBoundary); d <= afterFallBoundary; d.setHours(d.getHours() + 1)) {
     console.log("d: " + d);
     }
     
     console.log('this time with timezone.js');
     
     beforeFallBoundary = new timezoneJS.Date(2015, 10, 01, 00, 0, 0, 0, 'America/New_York'); // 0 AM (midnight) Local time before dst boundary (EDT)  
     afterFallBoundary = new timezoneJS.Date(2015, 10, 01, 03, 0, 0, 0, 'America/New_York'); // 3 AM local time (EST)
     
     for(var d = new timezoneJS.Date(beforeFallBoundary, 'America/New_York'); d <= afterFallBoundary; d.setHours(d.getHours() + 1)) {
     console.log("d: " + d);
     }
     
     console.log('using UTC methods of regular javascript date object');
     beforeFallBoundary = new Date(2015, 10, 01, 00, 0, 0, 0); // 0 AM (midnight) Local time before dst boundary (EDT)  
     afterFallBoundary = new Date(2015, 10, 01, 03, 0, 0, 0); // 3 AM local time (EST)
     
     for(var d = new Date(beforeFallBoundary); d <= afterFallBoundary; d.setUTCHours(d.getUTCHours() + 1)) {
     console.log("d: " + d);
     }*/

    groupingNames.sort();

    /*Move all RF stuff to bottom of list*/
    var rfNames = [],
            regularNames = [];

    for (var i = 0; i < groupingNames.length; i++) {
        if (groupingNames[i].indexOf('RF') === 0) {
            rfNames.push(groupingNames[i]);
        } else {
            regularNames.push(groupingNames[i]);
        }
    }

    /*Further sort tweaks, Jay wants 'C25/C50' to come first*/
    if (rfNames.indexOf('RF (C25/C50)') !== -1) {
        var tmpArray = [];
        tmpArray.push("RF (C25/C50)");
        for (var i = 0; i < rfNames.length; i++) {
            if (rfNames[i] !== 'RF (C25/C50)') {
                tmpArray.push(rfNames[i]);
            }
        }
        rfNames = tmpArray;
    }

    groupingNames = regularNames.concat(rfNames);


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
        var globalDurationSum = 0,
                tripRateDenominator = series.length;

        if (binSize === 'DAY') {
            tripRateDenominator = tripRateDenominator * 24;
        } else if (binSize === 'MONTH') {
            // Nothing for now...
        }

        if (programBasis) {
            tripRateDenominator = programHours;
        }

        for (var i = 0; i < groupingNames.length; i++) {
            var series = fullDataMap[groupingNames[i]],
                    countSum = 0;

            /*console.log('series length: ' + series.length);*/

            for (var j = 0; j < series.length; j++) {
                countSum = countSum + series[j][1];
            }

            /*console.log(groupingNames[i] + ' count sum: ' + countSum);*/

            countMap[groupingNames[i]] = countSum;
            var tripRate = countSum / tripRateDenominator;
            tripPerHourMap[groupingNames[i]] = tripRate;
            globalCountSum = globalCountSum + countSum;
            globalDurationSum = globalDurationSum + durationMap[groupingNames[i]];
        }

        var globalPerHour = globalCountSum / tripRateDenominator;

        var rateHeader = '<th title="Trips per hour" class="legend-per">Trips /Hr</th>';

        /*console.log('--> globalCountSum: ' + globalCountSum);*/

        var includeCount = false,
                includeRate = false,
                includeLost = false,
                includeMins = false;

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

        var maxDuration = $("#filter-form").attr("data-max-duration"),
                maxDurationUnits = $("#filter-form").attr("data-max-duration-units"),
                maxTypes = $("#filter-form").attr("data-max-types"),
                startFmt = $("#filter-form").attr("data-start"),
                endFmt = $("#filter-form").attr("data-end"),
                includeSadTrips = $("#filter-form").attr("data-sad-trips") === 'Y';

        for (var i = 0; i < groupingNames.length; i++) {
            var url = '/dtm/trips?';

                    if($("#grouping").val() === 'cause') {
                        url = url + 'cause=';
                    } else {
                        url = url + 'area=';
                    }

                    url = url
                    + encodeURIComponent(groupingNames[i])
                    + '&start=' + encodeURIComponent(startFmt)
                    + '&end=' + encodeURIComponent(endFmt)
                    + '&maxDuration=' + encodeURIComponent(maxDuration)
                    + '&maxDurationUnits=' + encodeURIComponent(maxDurationUnits)
                    + '&maxTypes=' + encodeURIComponent(maxTypes)
                    + '&qualified=';


            if (!includeSadTrips) {
                url = url + '&accState=NULL&accState=DOWN&accState=ACC&accState=RESTORE&accState=MD';
            }


            var rowStr = '<tr><th><a target="_blank" href="' + url + '"><div class="color-box" style="background-color: ' + colorMap[groupingNames[i]] + ';"></div></a></th><td>' + groupingNames[i] + '</td>';

            if (includeCount) {
                rowStr = rowStr + '<td>' + jlab.integerWithCommas(countMap[groupingNames[i]]) + '</td>';
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

    chartData.groupingNames = groupingNames;
    chartData.colorMap = colorMap;
    chartData.tripPerHourMap = tripPerHourMap;
    chartData.ds = ds;
    chartData.grouped = grouped;
    chartData.globalTripsPerHour = globalPerHour;
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

    var yAxisText = "Number of Trips",
            xAxisText = "Hour";

    if (binSize === 'DAY') {
        xAxisText = "Day";
    } else if (binSize === 'MONTH') {
        xAxisText = "Month";
    }

    jlab.addXAxisLabel(xAxisText);
    jlab.addYAxisLabel(yAxisText);
};

jlab.doLineChart = function (includePoints) {
    var startMillis = $("#bar-chart-data-table").attr("data-start-millis"),
            endMillis = $("#bar-chart-data-table").attr("data-end-millis");

    var chartData = jlab.getDataSource();
    var ds = chartData.ds;

    var binSize = $("#binSize").val();
    var minTickSizeX = 'day';
    var approximateHours = 24;

    if (binSize === 'HOUR') {
        minTickSizeX = 'hour';
        approximateHours = 1;
    } else if (binSize === 'MONTH') {
        minTickSizeX = 'month';
        approximateHours = 672; //28 days (Feb)
    }

    if (ds.length === 0) {
        $('<div>No data to chart</div>').insertBefore($("#chart-placeholder"));
        return;
    }

    jlab.addTooltips(false, binSize === 'HOUR');

    $("#chart-wrap").addClass("has-y-axis-label").addClass("has-x-axis-label");

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
            show: false
        },
        xaxis: {
            mode: "time",
            //timezone: "browser",
            timezone: null, /*UTC data (timezone-less) - converted to America/New_York server side*/

            minTickSize: [1, minTickSizeX],
            min: startMillis,
            max: endMillis
        },
        yaxes: [{
                min: 0,
                minTickSize: 1,
                tickDecimals: 0,
                ticks: 5
            }, {
                position: 'right', /*Last x-axis label may wrap if we don't do this*/
                reserveSpace: true
            }],
        grid: {
            borderWidth: 1,
            hoverable: true,
            borderColor: 'gray',
            backgroundColor: {colors: ["#fff", "#eee"]}
        }
    });

    jlab.addAxisLabels(binSize);
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

    /*if (chartData.grouped) {
     for (var i = 0; i < chartData.groupingNames.length; i++) {
     var yValue = chartData.tripPerHourMap[chartData.groupingNames[i]].toFixed(1) * 1;
     markings.push({color: chartData.colorMap[chartData.groupingNames[i]], lineWidth: 2, yaxis: {from: yValue, to: yValue}});
     }
     }*/

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

    $("#chart-placeholder").resize(function () {
        jlab.doMarkingLines(chartData, minX, maxX);
    });

    jlab.doMarkingLines(chartData, minX, maxX);

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

                var maxDuration = $("#filter-form").attr("data-max-duration"),
                        maxDurationUnits = $("#filter-form").attr("data-max-duration-units"),
                        maxTypes = $("#filter-form").attr("data-max-types"),
                        includeSadTrips = $("#filter-form").attr("data-sad-trips") === 'Y',
                        url = '/dtm/trips?';

                if($("#grouping").val() === 'cause') {
                    url = url + 'cause=';
                } else {
                    url = url + 'area=';
                }

                        url = url + encodeURIComponent(item.series.label)
                        + '&start=' + encodeURIComponent(jlab.dateTimeToGlobalUTCString(start))
                        + '&end=' + encodeURIComponent(jlab.dateTimeToGlobalUTCString(end))
                        + '&maxDuration=' + encodeURIComponent(maxDuration)
                        + '&maxDurationUnits=' + encodeURIComponent(maxDurationUnits)
                        + '&maxTypes=' + encodeURIComponent(maxTypes)
                        + '&qualified=';


                if (!includeSadTrips) {
                    url = url + '&accState=NULL&accState=DOWN&accState=ACC&accState=RESTORE&accState=MD';
                }

                window.open(url);
            }
        });
    }
};

jlab.doMarkingLines = function (chartData, minX, maxX) {
    $(".marking-line").remove();
    $(".marking-label-holder").remove();
    $(".known-marking-line").remove();

    var maxY = jlab.flotplot.getAxes().yaxis.max,
            yValue,
            hourlyRate,
            dailyRate,
            p1 = jlab.flotplot.pointOffset({x: minX, y: 0}),
            p2 = jlab.flotplot.pointOffset({x: maxX + 1, y: 0}),
            rightValue = ($("#chart-placeholder").width() - p2.left);

    if (chartData.grouped) {
        var c75_100 = chartData.tripPerHourMap['RF (C75/100)'] || 0,
                c25_50 = chartData.tripPerHourMap['RF (C25/50)'] || 0,
                separator = chartData.tripPerHourMap['RF (Separator)'] || 0,
                multi_other = chartData.tripPerHourMap['RF (Multi/Other)'] || 0;


        yValue = (c75_100 + c25_50 + separator + multi_other).toFixed(1) * 1;

        hourlyRate = yValue.toFixed(1) + " /Hr";
        dailyRate = "";

        if (chartData.binSize === 'DAY') {
            yValue = yValue * 24;
            dailyRate = " (" + yValue.toFixed(0) + " /Day)";
        } else if (chartData.binSize === 'MONTH') {
            yValue = yValue * 672;
            dailyRate = " (" + jlab.integerWithCommas(yValue.toFixed(0)) + " /Month)";
        }

        p1 = jlab.flotplot.pointOffset({x: minX, y: yValue}),
                p2 = jlab.flotplot.pointOffset({x: maxX + 1, y: yValue}),
                rightValue = ($("#chart-placeholder").width() - p2.left);

        if (yValue > 0 && yValue < maxY) {
            $("#chart-placeholder").append("<div class='marking-line' style='border-color: " + chartData.colorMap['RF'] + ";color:brown;position:absolute;left:" + (p1.left) + "px;right:" + rightValue + "px;top:" + (p1.top - 2) + "px;'></div>");
            $("#chart-placeholder").append("<div class='marking-label-holder' style='z-index:2;color:brown;position:absolute;left:" + (p1.left) + "px;right:" + rightValue + "px;top:" + (p1.top - 2) + "px;'><span style='float:right;'><span style='position:relative;bottom:1.3em;background-color:white;opacity:0.8;'>Avg RF " + hourlyRate + " " + dailyRate + "</span></span></div>");
        }

        /*for (var i = 0; i < chartData.groupingNames.length; i++) {
         var yValue = chartData.tripPerHourMap[chartData.groupingNames[i]].toFixed(1) * 1;
         
         var p1 = jlab.flotplot.pointOffset({x: minX, y: yValue}),
         p2 = jlab.flotplot.pointOffset({x: maxX + 1, y: yValue});
         $("#chart-placeholder").append("<div class='marking-line' style='border-color: " + chartData.colorMap[chartData.groupingNames[i]] + ";position:absolute;left:" + (p2.left + 1) + "px;right:" + 0 + "px;top:" + (p1.top - 2) + "px;'></div>");
         }*/
        yValue = chartData.globalTripsPerHour.toFixed(1) * 1;

        hourlyRate = yValue.toFixed(1) + " /Hr";
        dailyRate = "";

        if (chartData.binSize === 'DAY') {
            yValue = yValue * 24;
            dailyRate = " (" + yValue.toFixed(0) + " /Day)";
        } else if (chartData.binSize === 'MONTH') {
            yValue = yValue * 672;
            dailyRate = " (" + jlab.integerWithCommas(yValue.toFixed(0)) + " /Month)";
        }

        p1 = jlab.flotplot.pointOffset({x: minX, y: yValue}),
                p2 = jlab.flotplot.pointOffset({x: maxX + 1, y: yValue});
        if (yValue > 0 && yValue < maxY) {
            $("#chart-placeholder").append("<div class='marking-line' style='border-color: " + 'black' + ";position:absolute;left:" + (p1.left) + "px;right:" + rightValue + "px;top:" + (p1.top - 2) + "px;'></div>");
            $("#chart-placeholder").append("<div class='marking-label-holder' style='z-index:2;position:absolute;left:" + (p1.left) + "px;right:" + rightValue + "px;top:" + (p1.top - 2) + "px;'><span style='position:relative;bottom:1.3em;background-color:white;opacity:0.8;left:0px;'>Avg " + hourlyRate + " " + dailyRate + "</span></div>");
        }
    }


    /*fixed marking lines*/
    yValue = 15;

    hourlyRate = yValue.toFixed(0) + " /Hr";
    dailyRate = "";
    var dailyClass = "";

    if (chartData.binSize === 'DAY') {
        yValue = yValue * 24;
        dailyRate = " " + yValue.toFixed(0) + " /Day";
        dailyClass = "daily-marking";
    } else if (chartData.binSize === 'MONTH') {
        yValue = yValue * 672;
        dailyRate = " (" + jlab.integerWithCommas(yValue.toFixed(0)) + "<br/>/Month)";
        dailyClass = "monthly-marking";
    }

    p1 = jlab.flotplot.pointOffset({x: minX, y: yValue}),
            p2 = jlab.flotplot.pointOffset({x: maxX + 1, y: yValue});
    if (yValue < maxY) {
        $("#chart-placeholder").append("<div class='known-marking-line' style='border-color: " + 'gray' + ";position:absolute;left:" + (p1.left) + "px;right:" + rightValue + "px;top:" + (p1.top - 2) + "px;'><span class='known-markings " + dailyClass + "'>15 /Hr<br/>" + dailyRate + "</span></div>");
    }

    yValue = 10;

    hourlyRate = yValue.toFixed(0) + " /Hr";
    dailyRate = "";
    dailyClass = "";

    if (chartData.binSize === 'DAY') {
        yValue = yValue * 24;
        dailyRate = " " + yValue.toFixed(0) + " /Day";
        dailyClass = "daily-marking";
    } else if (chartData.binSize === 'MONTH') {
        yValue = yValue * 672;
        dailyRate = " (" + jlab.integerWithCommas(yValue.toFixed(0)) + "<br/>/Month)";
        dailyClass = "monthly-marking";
    }

    p1 = jlab.flotplot.pointOffset({x: minX, y: yValue}),
            p2 = jlab.flotplot.pointOffset({x: maxX + 1, y: yValue});
    if (yValue < maxY) {
        $("#chart-placeholder").append("<div class='known-marking-line' style='border-color: " + 'gray' + ";position:absolute;left:" + (p1.left) + "px;right:" + rightValue + "px;top:" + (p1.top - 2) + "px;'><span class='known-markings " + dailyClass + "'>10 /Hr<br/>" + dailyRate + "</span></div>");
    }


    yValue = 5;

    hourlyRate = yValue.toFixed(0) + " /Hr";
    dailyRate = "";
    dailyClass = "";

    if (chartData.binSize === 'DAY') {
        yValue = yValue * 24;
        dailyRate = "" + yValue.toFixed(0) + " /Day";
        dailyClass = "daily-marking";
    } else if (chartData.binSize === 'MONTH') {
        yValue = yValue * 672;
        dailyRate = " (" + jlab.integerWithCommas(yValue.toFixed(0)) + "<br/>/Month)";
        dailyClass = "monthly-marking";
    }

    p1 = jlab.flotplot.pointOffset({x: minX, y: yValue}),
            p2 = jlab.flotplot.pointOffset({x: maxX + 1, y: yValue});
    if (yValue < maxY) {
        $("#chart-placeholder").append("<div class='known-marking-line' style='border-color: " + 'gray' + ";position:absolute;left:" + (p1.left) + "px;right:" + rightValue + "px;top:" + (p1.top - 2) + "px;'><span class='known-markings " + dailyClass + "'>5 /Hr<br/>" + dailyRate + "</span></div>");
    }
};

$(document).on("click", ".default-reset-panel", function () {
    $("#date-range").val('past7days').trigger('change');
    $("#maxDuration").val(5);
    $("#maxDurationUnits").val('Minutes');
    $("#sadTrips").val('N');
    $("#masked-select").val('');
    $("#maxTypes").val('');
    $("#rateBasis").val('program');
    $("#chart").val('bar');
    $("#binSize").val('DAY');
    $("#grouping").val('cause');
    $("#maxY").val(17);
    $("#legendData").val("rate").trigger('change');
    $("#cause").val(null).trigger('change');
    return false;
});

$(function () {

    $("#fullscreen-button, #exit-fullscreen-button").button();

    $(".date-only-field").datepicker({
        dateFormat: 'dd-M-yy'
    });

    /*timezoneJS.timezone.zoneFileBasePath = '/dtm/resources/tz';
     timezoneJS.timezone.defaultZoneFile = [];    
     timezoneJS.timezone.init({async: false});*/

    jlab.flotplot = null;
    jlab.flotSourceColumnClass = 'count-data';

    /*if ($(".selected-column").hasClass("count-data")) {
     jlab.flotSourceColumnClass = 'count-data';
     } else if ($(".selected-column").hasClass("mttr-data")) {
     jlab.flotSourceColumnClass = 'mttr-data';
     }*/

    if ($("#chart-placeholder").length > 0) {
        var selected = $("#chart option:selected").val();

        if (selected === 'line') {
            jlab.doLineChart(false);
        } else if (selected === 'linewithpoints') {
            jlab.doLineChart(true);
        } else if (selected.startsWith('bar')) {
            jlab.doBarChart($("#grouping").val() != '');
        }
    }

    $("#legendData, #cause").select2({
        width: 290
    });
});