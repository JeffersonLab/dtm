var jlab = jlab || {};

jlab.xTickLabelHeight = 120;

jlab.getDataSource = function () {
    var dataMap = {};

    /*Keep consistent colors when toggling series*/
    var colorMap = {
        'Jan': '#a6cee3',
        'Feb': '#1f78b4',
        'Mar': '#b2df8a',
        'Apr': '#33a02c',
        'May': '#fb9a99',
        'Jun': '#e31a1c',
        'Jul': '#fdbf6f',
        'Aug': '#ff7f00',
        'Sep': '#cab2d6',
        'Oct': '#6a3d9a',
        'Nov': '#ffff99',
        'Dec': '#b15928'

    }; /* see: http://colorbrewer2.org/#type=qualitative&scheme=Paired&n=12*/

    var durationPerCategoryMap = {},
            durationPerMonthMap = {},
            categoryNameToIdMap = {},
            totalDuration = 0,
            programTotalDuration = 0,
            programDurationMap = {};

    $("#graph-data-table tbody tr").each(function (index, value) {
        var category = $("td:first-child()", value).text(),
                categoryId = $("td:first-child()", value).attr("data-id"),
                month = $("td:nth-child(2)", value).text(),
                downtimeHours = $("td:last-child()", value).text().replace(/,/g, '') * 1,
                series = dataMap[month] || {};

        categoryNameToIdMap[category] = categoryId;

        series[category] = downtimeHours;

        dataMap[month] = series;

        var categoryDuration = (durationPerCategoryMap[category] || 0) * 1;
        durationPerCategoryMap[category] = categoryDuration + downtimeHours;

        var monthDuration = (durationPerMonthMap[month] || 0) * 1;
        durationPerMonthMap[month] = monthDuration + downtimeHours;

        totalDuration = totalDuration + downtimeHours;
    });



    $("#program-table tbody tr").each(function (index, value) {
        var month = $("td:nth-child(1)", value).text(),
                programHours = $("td:last-child()", value).text().replace(/,/g, '') * 1;

        programDurationMap[month] = programHours;
        programTotalDuration = programTotalDuration + programHours;
    });


    var ds = [], monthNames = [], categoryNames = [];

    for (var month in programDurationMap) {
        monthNames.push(month);

        // Put in zeros for months with no data
        var series = dataMap[month] || {};
        dataMap[month] = series;
        var monthDuration = (durationPerMonthMap[month] || 0) * 1;
        durationPerMonthMap[month] = monthDuration;
    }

    for (var key in durationPerCategoryMap) {
        categoryNames.push(key);
    }

    /*Sort by duration asc*/
    categoryNames.sort(function (x, y) {
        var xDuration = durationPerCategoryMap[x],
                yDuration = durationPerCategoryMap[y];

        if (xDuration > yDuration) {
            return -1;
        }

        if (xDuration < yDuration) {
            return 1;
        }

        return 0;
    });

    /* We must fill in sparse data with zeros to satisfy stacked bars plugin */

    var fullDataMap = {};

    for (var i = 0; i < categoryNames.length; i++) {
        for (var j = 0; j < monthNames.length; j++) {
            var series = fullDataMap[monthNames[j]] || [];
            /*var yValue = j + 1;*/
            var yValue = dataMap[monthNames[j]][categoryNames[i]] || 0;
            /*console.log('yValue: ' + yValue);*/
            series.push([i, yValue * 1]);
            fullDataMap[monthNames[j]] = series;
        }
    }

    for (var i = monthNames.length - 1; i >= 0; i--) {
        ds.push({
            label: monthNames[i],
            data: fullDataMap[monthNames[i]],
            color: 'black',
            bars: {
                fillColor: colorMap[monthNames[i].substring(0, 3)]
            }
        });
    }

    var includeCount = false,
            includeDownPercent = false,
            includeProgramPercent = true,
            includeProgram = true,
            includeLost = false;

    /*$("#legendData :selected").each(function() {
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
     });*/

    var tableStr = '<table class="chart-legend">',
            headStr = '<thead><th></th><th></th>',
            footStr = '<tfoot><th></th><th>Total:</th>';

    if (includeCount) {
        headStr = headStr + '<th>Incidents</th>';
        footStr = footStr + '<th></th>';
    }
    if (includeLost) {
        headStr = headStr + '<th title="Repair Hours">Rep<br/>Hrs</th>';
        footStr = footStr + '<th>' + jlab.integerWithCommas(totalDuration.toFixed(0) * 1) + '</th>';
    }
    if (includeDownPercent) {
        headStr = headStr + '<th title="Repair Percent">Rep<br/>%</th>';
        footStr = footStr + '<th></th>';
    }
    if (includeProgram) {
        headStr = headStr + '<th title="Program Hours">Prg<br/>Hrs</th>';
        footStr = footStr + '<th>' + jlab.integerWithCommas(programTotalDuration.toFixed(0) * 1) + '</th>';
    }
    if (includeProgramPercent) {
        headStr = headStr + '<th title="Program Percent">Prg<br/>%</th>';
        footStr = footStr + '<th></th>';
    }

    if (includeCount || includeDownPercent || includeLost || includeProgramPercent) {
        headStr = headStr + '</thead>';
        footStr = footStr + '</tfoot>';
    } else {
        headStr = '';
        footStr = '';
    }

    tableStr = tableStr + headStr + footStr + '<tbody></tbody></table>';

    /*var tableStr = '<table class="chart-legend"><tbody></tbody></table>';*/

    var $legendTable = $(tableStr);

    for (var i = 0; i < monthNames.length; i++) {
        var yearMonth = monthNames[i],
                pieces = yearMonth.split(" "),
                year = pieces[1] * 1,
                month = jlab.triCharMonthNames.indexOf(pieces[0]),
                day = 1,
                start = new Date(year, month, day),
                end = new Date(year, month, day);

        end.setMonth(end.getMonth() + 1);

        if (start < jlab.minDate) {
            start = jlab.minDate;
        }

        if (end > jlab.maxDate) {
            end = jlab.maxDate;
        }

        var url = '/dtm/reports/category-downtime?transport='
                + '&start=' + encodeURIComponent(jlab.dateTimeToGlobalString(start))
                + '&end=' + encodeURIComponent(jlab.dateTimeToGlobalString(end))
                + '&type=1'
                + '&packed=N'
                + '&chart=table';

        if (jlab.fullscreen) {
            url = url + '&fullscreen=Y&print=Y';
        }

        url = url
                + '&data=downtime'
                + '&qualified=';



        var rowStr = '<tr><th><a target="_blank" href="' + url + '"><div class="color-box" style="background-color: ' + colorMap[monthNames[i].substring(0, 3)] + ';"></div></a></th><td>' + monthNames[i].substring(0, 3) + ' \'' + monthNames[i].substring(6) + '</td>';

        if (includeCount) {
            rowStr = rowStr + '<td></td>';
        }
        if (includeLost) {
            rowStr = rowStr + '<td>' + durationPerMonthMap[monthNames[i]].toFixed(0) + '</td>';
        }
        if (includeDownPercent) {
            rowStr = rowStr + '<td>' + (durationPerMonthMap[monthNames[i]] / totalDuration * 100).toFixed(0) + '%</td>';
        }
        if (includeProgram) {
            rowStr = rowStr + '<td>' + programDurationMap[monthNames[i]].toFixed(0) + '</td>';
        }
        if (includeProgramPercent) {
            rowStr = rowStr + '<td>' + (programDurationMap[monthNames[i]] / programTotalDuration * 100).toFixed(0) + '%</td>';
        }

        rowStr = rowStr + '</tr>';
        $legendTable.find("tbody").append(rowStr);
    }

    var footnote = $(".legend-panel .footnote-wrapper").html();

    $(".legend-panel").html($legendTable);
    $(".legend-panel").append(footnote);

    //jlab.fakeDecimalAlign($(".chart-legend tbody"), ["td:nth-child(3)", "td:nth-child(4)", "td:nth-child(5)"]);


    var chartData = {};

    chartData.categoryNames = categoryNames;
    chartData.monthNames = monthNames;
    chartData.colorMap = colorMap;
    chartData.ds = ds;
    chartData.totalDuration = totalDuration;
    chartData.durationPerCategoryMap = durationPerCategoryMap;
    chartData.categoryNameToIdMap = categoryNameToIdMap;
    chartData.programDurationMap = programDurationMap;
    chartData.programTotalDuration = programTotalDuration;

    return chartData;
};

jlab.addTooltips = function (categoryNames) {
    $("<div id='tooltip'></div>").css({
        position: "absolute",
        display: "none",
        border: "1px solid #fdd",
        padding: "2px",
        "background-color": "#fee",
        opacity: 0.80
    }).appendTo("body");

    $(".chart-placeholder").bind("plothover", function (event, pos, item) {

        if (item) {
            $(".chart-placeholder").css("cursor", "pointer");
            var x = item.datapoint[0].toFixed(2) * 1,
                    y = item.datapoint[1].toFixed(2) * 1,
                    label = '',
                    category = categoryNames[x];

            y = (item.datapoint[1] - item.datapoint[2]).toFixed(2) * 1;

            if (item.series.label !== '') {
                label = " {" + item.series.label + "}";
            }

            $("#tooltip").html(category + label + " (" + jlab.integerWithCommas(y) + ")")
                    .css({top: item.pageY - 30, left: item.pageX + 5})
                    .fadeIn(200);
        } else {
            $(".chart-placeholder").css("cursor", "default");
            $("#tooltip").stop().hide();
        }
    });
};

jlab.addAxisLabels = function () {

    var yAxisText = "Repair Hours",
            xAxisText = "Category (% Repair Time)";

    jlab.addXAxisLabel(xAxisText);
    jlab.addYAxisLabel(yAxisText);
};

/*Re-define function to subtract xTickLabelHeight before finding midpoint*/
jlab.addYAxisLabel = function (label) {
    var yaxisLabel = $("<div class='axis-label y-axis-label'></div>")
            .text(label)
            .appendTo($(".chart-placeholder"));
    yaxisLabel.css("margin-top", (yaxisLabel.width() - jlab.xTickLabelHeight) / 2);
};

jlab.doBarChart = function () {
    var chartData = jlab.getDataSource();
    var ds = chartData.ds;

    if (ds.length === 0) {
        $('<div>No data to chart</div>').insertBefore($(".chart-placeholder"));
        return;
    }

    if ($("body").hasClass("print")) {
        jlab.xTickLabelHeight = 160;
    }

    jlab.addTooltips(chartData.categoryNames);

    var ticks = [], minX = -0.6, maxX = chartData.categoryNames.length - 0.4; /*leave 0.6 on both sides*/

    for (var i = 0; i < chartData.categoryNames.length; i++) {
        var percent = (chartData.durationPerCategoryMap[chartData.categoryNames[i]] / chartData.totalDuration) * 100;
        ticks.push([i, chartData.categoryNames[i] + " (" + percent.toFixed(0) + "%)"]);
    }

    $(".chart-wrap").addClass("has-y-axis-label").addClass("has-x-axis-label");

    jlab.flotplot = $.plot($(".chart-placeholder"), ds, {
        series: {
            stack: true,
            bars: {
                show: true,
                lineWidth: 1,
                /*fill: 0.1,*/
                align: "center", /*need to adjust min and max ticks for this*/
                barWidth: 0.6
            }
        },
        /* We create the legend ourselves since we want to avoid haveing the plot area determined first then the legend inserted (we want legend to take up the space it needs first then the plot can determine what is left over*/
        legend: {
            show: false
                    /*sorted: true, 
                     container: $(".key-panel")*/
        },
        xaxis: {
            min: minX,
            max: maxX,
            ticks: ticks,
            labelWidth: 10, /*Actual width is overriden in css once rotated (but small width here tells flot to not add a ton of padding on side of chart)*/
            labelHeight: jlab.xTickLabelHeight
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
        }
    });

    jlab.addAxisLabels();

    $(".chart-placeholder").resize(function () {
        jlab.doMarkingLines(chartData, minX, maxX);
    });

    jlab.doMarkingLines(chartData, minX, maxX);

    $(".chart-placeholder").on("plotclick", function (event, pos, item) {
        /*console.log(item);*/
        if (item) {

            var x = item.datapoint[0].toFixed(2) * 1,
                    //y = item.datapoint[1].toFixed(2) * 1,
                    yearMonth = item.series.label,
                    category = chartData.categoryNames[x],
                    categoryId = chartData.categoryNameToIdMap[category],
                    pieces = yearMonth.split(" "),
                    year = pieces[1] * 1,
                    month = jlab.triCharMonthNames.indexOf(pieces[0]),
                    day = 1,
                    start = new Date(year, month, day),
                    end = new Date(year, month, day);

            end.setMonth(end.getMonth() + 1);

            if (start < jlab.minDate) {
                start = jlab.minDate;
            }

            if (end > jlab.maxDate) {
                end = jlab.maxDate;
            }



            /*console.log(category);
             console.log(categoryId);
             console.log(yearMonth);
             console.log(year);
             console.log(month);
             console.log(jlab.minDate);
             console.log(jlab.maxDate);            
             console.log(start);
             console.log(end);*/

            var url = '/dtm/reports/system-downtime?transport='
                    + '&start=' + encodeURIComponent(jlab.dateTimeToGlobalString(start))
                    + '&end=' + encodeURIComponent(jlab.dateTimeToGlobalString(end))
                    + '&type=1'
                    + '&packed=N'
                    + '&chart=table';

            if (jlab.fullscreen) {
                url = url + '&fullscreen=Y&print=Y';
            }

            url = url
                    + '&data=downtime'
                    + '&category=' + categoryId
                    + '&qualified=';

            window.open(url);
        }
    });
};

jlab.doMarkingLines = function (chartData, minX, maxX) {
    $(".marking-line").remove();

    var yValue = (chartData.programTotalDuration.toFixed(1) * 1) * 0.01,
            p1 = jlab.flotplot.pointOffset({x: minX, y: yValue}),
            p2 = jlab.flotplot.pointOffset({x: maxX, y: yValue}),
            rightValue = ($(".chart-placeholder").width() - p2.left);
    if (yValue > 0) {
        $(".chart-placeholder").append("<div class='marking-line' style='border-color: black;color:black;position:absolute;left:" + (p1.left) + "px;right:" + rightValue + "px;top:" + (p1.top - 2) + "px;'><span style='float:right;'><span style='position:relative;bottom:28px;background-color:white;opacity:0.8;'>1% of program</span></span></div>");
    }
};

$(function () {

    /*$(".month-only-field").datepicker({
     dateFormat: 'M-yy'
     });*/

    jlab.flotplot = null;
    jlab.flotSourceColumnClass = 'count-data';

    if ($(".chart-placeholder").length > 0) {
        var selected = $("#chart option:selected").val();

        if (selected.startsWith('bar')) {
            jlab.doBarChart();
        }
    }
});