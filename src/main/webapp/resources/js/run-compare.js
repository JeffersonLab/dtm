var jlab = jlab || {};

jlab.defaultChartOptions = {
    legend: {
        show: true,
        position: 'nw'
    },
    shadowSize: 0,
    xaxis: {
    },
    yaxes: [{
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

jlab.getXAxisInfo = function() {
    var binSize = $("#size").val() || 'day',
        binLabel = 'Day';

    switch(binSize) {
        case 'week':
            binLabel = 'Week';
            break;
        case 'month':
            binLabel = 'Month';
            break;
        case 'quarter':
            binLabel = 'Quarter';
            break;
        case 'year':
            binLabel = 'Year';
            break;
    }

    return {binLabel: binLabel};
};

jlab.getLineData = function(xAxisInfo) {
    var ds = [],
        nowMillisUtc = Date.now();

    $("#source-table tbody").each(function(i, v) {
        var series = {lines: {show: true}, data: []},
            downtimeTotal = 0;
            ds.push(series);

        $(this).find("tr").each(function(index, value){
            if(index === 0 ) {
                series.label = $(this).find("th").text();
            } else {
                var timestamp = $("td:first-child", value).attr("data-date-utc"),
                    downtime = $("td:nth-child(2)", value).text().replace(/,/g, '');

                if (timestamp <= nowMillisUtc) {
                    if (downtime !== '') {
                        downtimeTotal = downtimeTotal + (downtime * 1);
                        series.data.push([index, downtimeTotal]);
                    }
                }
            }
        });
    });

    return ds;
};


jlab.doChart = function(ds, options, xAxisInfo) {
    $(".chart-wrap").addClass("has-y-axis-label has-x-axis-label");

    options.xaxis.minTickSize = 1;
    options.xaxis.tickDecimals = 0;

    jlab.flotplot = $.plot($(".chart-placeholder"), ds, options);
};

jlab.doLineChart = function() {
    var xAxisInfo = jlab.getXAxisInfo(),
        ds = jlab.getLineData(xAxisInfo),
        options = jlab.defaultChartOptions;

    jlab.doChart(ds, options, xAxisInfo);

    jlab.addYAxisLabel("Accumulated Downtime Hours");
    jlab.addXAxisLabel(xAxisInfo.binLabel);
};

$(document).on("click", "#add-run-button", function() {
    $("#add-run-dialog").dialog("open");
});

$(document).on("click", "#add-selected-run-button", function(){


    let label = $("#label").val(),
        start =  $("#start").val(),
        end = $("#end").val();

    if(label === '') {
        alert('Label is required');
        return;
    }

    if(start === '') {
        alert('Start date is required');
        return;
    }

    if(end === '') {
        alert('End date is required');
        return;
    }

    $("#run-list").append('<li>' + String(label).encodeXml()  + '<input type="hidden" name="label" value="' + String(label).encodeXml() + '"/><input type="hidden" name="start" value="' + String(start).encodeXml() + '"/><input type="hidden" name="end" value="' + String(end).encodeXml() + '"/> <button type="button">X</button></li>');
    $("#add-run-dialog").dialog("close");
});

$(document).on("click", "#run-list button", function(){
    $(this).parent("li").remove();
});

$(document).on("click", "#lookup-button", function() {
    let year = $("#year").val(),
        runNumber = $("#run-number").val();

    $("#label").val(year + ' Run ' + runNumber);
});

$(function () {
    var type = $("#type").val(),
        size = $("#size").val();

    if(size === 'none' && type !== 'table') {
        $(".message-box").append(' - Please choose a bin size to continue');
        return;
    }

    switch(type) {
        case 'line':
            jlab.doLineChart();
            $(".chart-placeholder").addClass("line-chart");
            break;
    }
});


