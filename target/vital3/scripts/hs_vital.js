// overriding two hs.js functions

function cookie_name(el) {
    var loc = window.location.toString();
    //remove the path info (for the path variable)
    loc = loc.replace(/.*\//,'');
    //remove the message from the url
    loc = loc.replace( /([?&])message=.*?(\&|$)/ ,'$1');
    loc = loc.replace(/[?&]$/,''); 
    
    var name =  HS_COOKIE_PREFIX + loc;
    return name.replace(/\W/g,"_");
}

