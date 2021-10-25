var jlab = jlab || {};

jlab.defaultChartOptions = {
    legend: {
        show: true,
        noColumns: 2,
        position: 'nw',
        sorted: "reverse"
    },
    shadowSize: 0,
    xaxis: {
        mode: "time",
        timezone: null,

        /*minTickSize: [1, minTickSizeX],
        min: startMillis,
        max: endMillis*/
    },
    yaxes: [{
        /*min: 0,
        max: 100,
        minTickSize: 1,
        tickDecimals: 0,
        ticks: 5*/
    }, {
        position: 'right',
        reserveSpace: true
    }],
    grid: {
        borderWidth: 1,
        borderColor: 'gray',
        backgroundColor: {colors: ["#fff", "#eee"]}
    }
};

jlab.getPointData = function(xAxisInfo) {
    var data1 = [],
        data2 = [],
        data3 = [],
        series1 = {label: xAxisInfo.binLabel + ' Reliability', color: 'rgb(68,114,196)', points: {show: true, fill: true, fillColor: 'rgb(68,114,196)'}, data: data1},
        series2 = {label: 'Overall Reliability', color: 'rgb(68,114,196)', lines: {show: true}, data: data2},
        series3 = {label: 'DOE Min', color: 'black', dashes: {show: true}, data: data3},
        deliveredTotal = 0,
        scheduledTotal = 0;

    $("#source-table tbody tr").each(function(index, value){
        var timestamp = $("td:first-child", value).attr("data-date-utc"),
            delivered = $("td:nth-child(5)", value).text().replace(/,/g, '').replace(/%/g, ''),
            scheduled = $("td:nth-child(9)", value).text().replace(/,/g, '').replace(/%/g, ''),
            value = $("td:last-child", value).text().replace(/%/g, '');

        if(value !== '') {
            data1.push([timestamp * 1, value * 1]);
        }

        if(delivered !== '') {
            deliveredTotal = deliveredTotal + (delivered * 1);
        }

        if(scheduled !== '') {
            scheduledTotal = scheduledTotal + (scheduled * 1);
        }
    });

    if(data1.length > 1) {
        var overall = deliveredTotal / scheduledTotal * 100;

        data2.push([xAxisInfo.minX, overall]);
        data2.push([xAxisInfo.maxX, overall]);

        data3.push([xAxisInfo.minX, 80]);
        data3.push([xAxisInfo.maxX, 80]);
    }

    /*Order controls stack order*/
    return ds = [series3, series2, series1];
};

jlab.doPointChart = function() {
    var xAxisInfo = jlab.getXAxisInfo(),
        ds = jlab.getPointData(xAxisInfo),
        options = jlab.defaultChartOptions;

    options.legend.labelBoxBorderColor = 'transparent';
    options.legend.noColumns = 3;

    jlab.doChart(ds, options, xAxisInfo);

    jlab.addYAxisLabel("Reliability (%)");
};

jlab.getLineData = function(xAxisInfo) {
    var data1 = [],
        data2 = [],
        series1 = {label: 'Delivered', color: 'rgb(68,114,196)', lines: {show: true}, data: data1},
        series2 = {label: 'Budgeted', color: 'black', dashes: {show: true}, data: data2},
        budgetedTotal = 0,
        deliveredTotal = 0,
        nowMillisUtc = Date.now();

    $("#source-table tbody tr").each(function(index, value){
        var timestamp = $("td:first-child", value).attr("data-date-utc"),
            delivered = $("td:nth-child(5)", value).text().replace(/,/g, '').replace(/%/g, ''),
            budgeted = $("td:nth-child(6)", value).text().replace(/,/g, '');

        if(timestamp <= nowMillisUtc) {
            if (delivered !== '') {
                deliveredTotal = deliveredTotal + (delivered * 1);
                data1.push([timestamp * 1, deliveredTotal]);
            }
        }

        if (budgeted !== '') {
            budgetedTotal = budgetedTotal + (budgeted * 1);
            data2.push([timestamp * 1, budgetedTotal]);
        }
    });

    /*Order controls stack order*/
    return ds = [series2, series1];
};

jlab.doLineChart = function() {
    var xAxisInfo = jlab.getXAxisInfo(),
        ds = jlab.getLineData(xAxisInfo),
        options = jlab.defaultChartOptions;

    /*console.log(ds);*/
    options.legend.labelBoxBorderColor = 'transparent';

    /*var maxY = Math.max(ds[0].data[ds[0].data.length - 1][1], ds[1].data[ds[1].data.length - 1][1]);
    options.yaxes[0].max = maxY + (maxY * 0.1); /*Add 10% space for legend*/


    jlab.doChart(ds, options, xAxisInfo);

    jlab.addYAxisLabel("Hours");
};

jlab.getBarData = function(xAxisInfo) {
    var data1 = [],
        data2 = [],
        series1 = {label: 'Delivered', color: 'green', bars: {show: true, align: 'center', barWidth: xAxisInfo.binMillis / 2}, data: data1},
        series2 = {label: 'Failures', color: 'red', bars: {show: true, align: 'center', barWidth: xAxisInfo.binMillis / 2}, data: data2};

    $("#source-table tbody tr").each(function(index, value){
        var timestamp = $("td:first-child", value).attr("data-date-utc"),
            delivered = $("td:nth-child(5)", value).text().replace(/,/g, '').replace(/%/g, ''),
            failures = $("td:nth-child(8)", value).text().replace(/,/g, '');

        if(delivered !== '') {
            data1.push([timestamp * 1, delivered * 1]);
        }

        if(failures !== '') {
            data2.push([timestamp * 1, failures * -1]);
        }
    });

    /*Order controls stack order*/
    return ds = [series2, series1];
};


jlab.doBarChart = function() {
    var xAxisInfo = jlab.getXAxisInfo(),
        ds = jlab.getBarData(xAxisInfo),
        options = jlab.defaultChartOptions;

    jlab.doChart(ds, options, xAxisInfo);

    jlab.addYAxisLabel("Hours");
};

jlab.getXAxisInfo = function() {
    var startMillis = $("#source-table").attr("data-start-millis") * 1,
        endMillis = $("#source-table").attr("data-end-millis") * 1,
        binSize = $("#size").val() || 'day',
        minTickSizeX = [1, 'day'],
        binHours = 24,
        binLabel = 'Daily';
        timeformat = '%d';

    switch(binSize) {
        case 'week':
            minTickSizeX = [7, 'day'];
            binHours = 168;
            binLabel = 'Weekly';
            break;
        case 'month':
            minTickSizeX = [1, 'month'];
            binHours = 730.08; //30.42 days
            binLabel = 'Monthly';
            break;
        case 'quarter':
            minTickSizeX = [3, 'month'];
            binHours = 2190.24; //30.42 days X 3
            binLabel = 'Quarterly';
            break;
        case 'year':
            minTickSizeX = [1, 'year'];
            binHours = 8760; //365 days
            binLabel = 'Yearly';
            break;
    }

    var binMillis = binHours * 3.6e+6;

    /* half interval offset due to 'centered' bars - plus adds padding so points don't hang off*/
    var minX = startMillis - (binMillis / 2),
        maxX = endMillis + (binMillis / 2);

    /*var durationMillis = maxX - minX;
    if(durationMillis >= 17520 * 3.6e+6) { // 2 years
        timeformat = '%Y';
    } else if(durationMillis >= 1460.16 * 3.6e+6) { // 2 months
        timeformat = '%b';
    } else {
        timeformat = '%d'
    }*/

    return {startMillis: startMillis, endMillis: endMillis, minTickSizeX: minTickSizeX, binLabel: binLabel, binMillis: binMillis, minX: minX, maxX: maxX, timeformat: timeformat};
};

jlab.doChart = function(ds, options, xAxisInfo) {
    $("#chart-wrap").addClass("has-y-axis-label");

    options.xaxis.min = xAxisInfo.minX;
    options.xaxis.max = xAxisInfo.maxX;
    options.xaxis.minTickSize = xAxisInfo.minTickSizeX;
    //options.xaxis.timeformat = xAxisInfo.timeformat;

    jlab.flotplot = $.plot($("#chart-placeholder"), ds, options);
};

$(document).on("click", ".default-reset-panel", function () {
    $("#date-range").val('1month').change();
    $("#type").val('table').change();
    $("#size").val('none').change();
    $("#maintenance-hours").val('0.0').change();
    $("#quality-hours").val('0.0').change();
    $("#budget-scaler").val('1.0').change();
    return false;
});

$(document).on("click", ".flyout-link", function () {
    $(".definition-flyout-handle").remove();
    var flyout = $("." + $(this).attr("data-flyout-type") + " .flyout-panel").clone();
    $(this).parent().append('<div class="definition-flyout-handle"></div>');
    $(".definition-flyout-handle").append(flyout);
    return false;
});
$(document).on("click", ".close-bubble", function () {
    $(".definition-flyout-handle").remove();
    return false;
});
$(document).on("click", "#csv-menu-item", function() {
    $("#csv").click();
});

$(function(){
    var type = $("#type").val(),
        size = $("#size").val();

    if(size === 'none' && type !== 'table') {
        $(".message-box").append(' - Please choose a bin size to continue');
        return;
    }

    switch(type) {
        case 'point':
            jlab.doPointChart();
            $("#chart-placeholder").addClass("point-chart");
            break;
        case 'line':
            jlab.doLineChart();
            $("#chart-placeholder").addClass("line-chart");
            break;
        case 'bar':
            jlab.doBarChart();
            $("#chart-placeholder").addClass("bar-chart");
            break;
    }
});