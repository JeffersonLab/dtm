var jlab = jlab || {};

jlab.eventTypes = ['', 'ACC', 'HLA', 'HLB', 'HLC', 'HLD'];
jlab.triCharMonthNames = ["Jan", "Feb", "Mar", "Apr", "May", "Jun",
    "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"];

jlab.pad = function(n, width, z) {
    z = z || '0';
    n = n + '';
    return n.length >= width ? n : new Array(width - n.length + 1).join(z) + n;
};

/* String Date To format: DD-MMM-YYYY hh:mm */
jlab.dateToApplicationFormatedString = function(x) {
    var year = x.getFullYear(),
            month = x.getMonth(),
            day = x.getDate(),
            hour = x.getHours(),
            minute = x.getMinutes();

    return jlab.pad(day, 2) + '-' + jlab.triCharMonthNames[month] + '-' + year + ' ' + jlab.pad(hour, 2) + ':' + jlab.pad(minute, 2);
};

/* String Date From format: YYYY-MM-DDThh:mm */
jlab.dateFromJsonIsoString = function(x) {
    var year = parseInt(x.substring(0, 4)),
            month = parseInt(x.substring(5, 7)),
            day = parseInt(x.substring(8, 10)),
            hour = parseInt(x.substring(11, 13)),
            minute = parseInt(x.substring(14, 16));

    return new Date(year, month - 1, day, hour, minute);
};

jlab.refreshEvents = function() {
    var request = jQuery.ajax({
        url: "/dtm/data/events",
        type: "GET",
        dataType: "json"
    });

    request.done(function(json) {
        $("#event-block").empty();
        if (json.data.length > 0) {
            
            json.data.sort(function(a, b){
                return a.event_type_id - b.event_type_id;
            });
            
            $("#event-block").append('<ul id="event-list">');
            $(json.data).each(function() {
                var downDate = jlab.dateFromJsonIsoString(this.time_down),
                        fmtDate = jlab.dateToApplicationFormatedString(downDate),
                        incidentCount = this.incidents.length,
                        incidentIndicator = '',
                        escalationClass = '',
                        fmtDuration = this.duration;

                if (incidentCount > 1) {
                    incidentIndicator = ' <span class="incident-count">(' + incidentCount + ')</span>';
                }

                if (this.escalation === 1) {
                    escalationClass = 'escalation-warning';
                } else if (this.escalation === 2) {
                    escalationClass = 'escalation-danger';
                }
                
                /* The whole point of this is to insert a line break */
                if(fmtDuration.indexOf('minute') !== -1) { // If duration contains minutes
                    if(fmtDuration.indexOf('hours') !== -1) { // If contains hours; plural
                        fmtDuration = fmtDuration.replace('hours', 'hours\n');
                    } else { // May contain an hour (not plural), or none at all
                        fmtDuration = fmtDuration.replace('hour', 'hour\n');
                    }
                }

                $("#event-list").append('<li class="ui-accordion-header ui-helper-reset ui-corner-top ui-state-default ui-corner-bottom"><div class="event-type">' + jlab.eventTypes[this.event_type_id] + '</div><div class="event-title">' + this.title + incidentIndicator + '</div><div class="event-down">' + fmtDate + '</div><div class="event-duration"><div class="event-duration-panel ' + escalationClass + '">' + fmtDuration + '</div></div></li>');
            });
            $("#event-block").append("</ul>");
        } else {
            $("#event-block").empty().append('<span>No open events</span>');
        }
    });

    request.error(function(xhr, textStatus) {
        window.console && console.log('Unable to query for events: Text Status: ' + textStatus + ', Ready State: ' + xhr.readyState + ', HTTP Status Code: ' + xhr.status);
        $("#event-block").empty().append('<span class="error">Unable to query for events: server did not handle request: ' + xhr.status + '</span>');
    });

    request.always(function() {
        jlab.setTimer();
    });
};
jlab.setTimer = function() {
    setTimeout(function() {
        jlab.refreshEvents();
    }, 60000);
};
$(function() {
    jlab.refreshEvents();
});