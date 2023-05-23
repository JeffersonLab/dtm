var jlab = jlab || {};
jlab.incident = jlab.incident || {};
jlab.event = jlab.event || {};

jlab.event.initAccordionPanel = function() {
    $(this).addClass("ui-accordion-header ui-helper-reset ui-state-default ui-corner-top ui-corner-bottom")
            .hover(function() {
                $(this).toggleClass("ui-state-hover");
            })
            .click(function() {
                $(this).toggleClass("ui-accordion-header-active ui-state-active ui-state-default ui-corner-bottom")
                        .find("> .event-header-toggle .ui-icon").toggleClass("ui-icon-triangle-1-e ui-icon-triangle-1-s").end()
                        .next().toggleClass("ui-accordion-content-active").slideToggle();
                return false;
            })
            .next()
            .addClass("ui-accordion-content ui-helper-reset ui-widget-content ui-corner-bottom")
            .hide();

    $(this).find(".event-header-toggle").prepend('<span class="ui-icon ui-icon-triangle-1-e"></span>');

    $(this).click();
};
$(document).on("click", ".flyout-link", function() {
    $(".flyout-handle").remove();
    $(this).append('<div class="flyout-handle"><div class="flyout-panel"><button class="close-bubble">X</button><div><h4>Incident Link:</h4><input class="copy-link" type="text" readonly="readonly" value="all-events?incidentId="/></div></div></div>');
    $(".copy-link").val($(this).attr("href"));
    $(".copy-link").focus().select();
    return false;
});
$(document).on("click", ".close-bubble", function() {
    $(this).parent().parent().remove();
    return false;
});
$(document).on("click", ".default-reset-panel", function() {
    $("#date-range").val('custom').change();
    $("#start").val('');
    $("#end").val('');
    $("#maxDuration").val('');
    $("#maxDurationUnits").val('Minutes');
    $("#minDuration").val('');
    $("#minDurationUnits").val('Minutes');
    /*$("#accState").val('');
    $("#hallAState").val('');
    $("#hallBState").val('');
    $("#hallCState").val('');
    $("#hallDState").val('');*/
    $("#accState").val(null).trigger('change');
    $("#hallAState").val(null).trigger('change');
    $("#hallBState").val(null).trigger('change');
    $("#hallCState").val(null).trigger('change');
    $("#hallDState").val(null).trigger('change');
    $("#node").val('');
    $("#channel").val('');
    $("#area").val('');
    $("#cause").val('');
    $("#system").val('');
    $("#cedType").val('');
    $("#cedName").val('');
    $("#maxTypes").val('');
    $("#maxDevices").val('');    
    $("#exceptionType").val('');
    $("#tripId").val('');
    $("#faultId").val('');
    $("#exceptionId").val('');
    return false;
});
$(document).on("click", "#csv-agg-menu-item", function() {
    $("#csv-format").val('agg');
    $("#csv").click();
});
$(document).on("click", "#csv-exp-menu-item", function() {
    $("#csv-format").val('exp');
    $("#csv").click();
});
$(function() {

    $("#accordion")
            /*.addClass("ui-accordion ui-accordion-icons ui-widget ui-helper-reset")*/
            .find("h3")
            /*.addClass("ui-accordion-header ui-helper-reset ui-state-default ui-corner-top ui-corner-bottom")*/
            .hover(function() {
                $(this).toggleClass("ui-state-hover");
            })
            .click(function() {
                $(this).toggleClass("ui-accordion-header-active ui-state-active ui-state-default ui-corner-bottom")
                        .find("> .event-header-toggle .ui-icon").toggleClass("ui-icon-triangle-1-e ui-icon-triangle-1-s").end()
                        .next().toggleClass("ui-accordion-content-active").slideToggle();
                return false;
            })
            .next()
            /*.addClass("ui-accordion-content ui-helper-reset ui-widget-content ui-corner-bottom")*/
            .hide();

    $("#accordion").find(".event-header-toggle").prepend('<span class="ui-icon ui-icon-triangle-1-e"></span>');

    if ($("#accordion h3").length === 1) {
        $("#accordion h3").click();
    }
    
    $("#accState, #hallAState, #hallBState, #hallCState, #hallDState").select2({
        width: 290
    });    
});