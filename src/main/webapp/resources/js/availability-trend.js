var jlab = jlab || {};

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
$(function () {

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
});


