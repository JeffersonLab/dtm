var jlab = jlab || {};
jlab.dtm = jlab.dtm || {};  

jlab.dtm.ondemand = function(username) {
    if(jlab.isRequest()) {
        window.console && console.log("Ajax already in progress");        
        return;
    }
    
    jlab.requestStart();        
        
    var request = jQuery.ajax({
        url: jlab.contextPath + "/setup/ajax/email-on-demand",
        type: "POST",
        data: {
            username : username
        },
        dataType: "html"
    });

    request.done(function(data) {
        if($(".status", data).html() !== "Success") {
            alert('Unable to send email: ' + $(".reason", data).html());
        } else {
            alert('Email sent');
            $("#username").val('');
        }
            
    });

    request.fail(function(xhr, textStatus) {
        window.console && console.log('Unable to send email: Text Status: ' + textStatus + ', Ready State: ' + xhr.readyState + ', HTTP Status Code: ' + xhr.status);
        alert('Unable to send email');
    });
    
    request.always(function() { 
        jlab.requestEnd();
    });                
};

$(document).on("click", "#email-now-button", function() {
    var username = $("#username").val();

    if(username == '') {
        alert('username is empty');
        return false;
    }

    if(confirm('Are you sure you want to send email to user ' + username + '?')) {
        jlab.dtm.ondemand.call(this, username);
    }
});

$(document).on("click", "#email-preview-button", function() {
    var username = $("#username").val();

    if(username == '') {
        alert('username is empty');
        return false;
    }

    jlab.closePageDialogs();
    jlab.openPageInDialog(jlab.contextPath + "/expert-email?username=" + username, "Expert Email Preview");
    return false;
});

$(document).on("keydown", ":input:not(textarea):not(:submit)", function(event) {
    if (event.key == "Enter") {
        event.preventDefault();
        console.log('blocking enter key; click submit button with mouse');
    }
});