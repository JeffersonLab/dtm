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

    request.error(function(xhr, textStatus) {
        window.console && console.log('Unable to send email: Text Status: ' + textStatus + ', Ready State: ' + xhr.readyState + ', HTTP Status Code: ' + xhr.status);
        alert('Unable to send email');
    });
    
    request.always(function() { 
        jlab.requestEnd();
    });                
};

$(document).on("click", "#email-now-button", function() {
    var username = $("#username").val();
    if(confirm('Are you sure you want to send email to user ' + username + '?')) {
        jlab.dtm.ondemand.call(this, username);
    }
});