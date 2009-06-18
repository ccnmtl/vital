var hs_controls = new Array();

var hs_ids = {};
var HS_COOKIE_PREFIX = "hsstate_";

function cookie_name(el) {
    var name =  HS_COOKIE_PREFIX + document.location;
    return name.replace(/\W/g,"_");
}

function path() {
    //specific to the filename, so there are that many fewer cookies sent per request
    return window.location.pathname.toString(); 
}

function saveStateCookie(el,value,d) {
    //THIS FUNCTION WILL NOT WORK, call hs_saveCookie() instead
    setCookie(cookie_name(el), value, d, path());
}

function futureDate() {
	var d = new Date();
	d.setTime(Date.parse('October, 4 2030 07:04:11'));
	return d;
}
function pastDate() {
	var d = new Date();
	d.setTime(0);
	return d;
}

function getCookie(name) {
    	var dc = document.cookie;
    	var prefix = name + "=";
    	var begin = dc.indexOf("; " + prefix);
    	if (begin == -1) {
        	begin = dc.indexOf(prefix);
        	if (begin != 0) return null;
    	} else {
        	begin += 2;
    	}
    	var end = document.cookie.indexOf(";", begin);
    	if (end == -1) {
        	end = dc.length;
    	}
    	return unescape(dc.substring(begin + prefix.length, end));
}

function setCookie(name, value, expires, path, domain, secure) {   
    	document.cookie= name + "=" + escape(value) +
        	((expires) ? "; expires=" + expires.toGMTString() : "") +
        	((path) ? "; path=" + path : "") +
        	((domain) ? "; domain=" + domain : "") +
        	((secure) ? "; secure" : "");
}

function hs_loadCookie() {
    x = evalJSON(getCookie(cookie_name()));
    if (x) {
	hs_ids = x;
    }
}

function mallocCookie(size, prefix) {
    //if cookies are too big/many clear them or return false
    var MAX_COOKIE_SIZE = 3000; //this is conservative
    if (document.cookie.length + size < MAX_COOKIE_SIZE) {
	return true;
    }
    clearCookies(prefix);
    //need to recheck, because maybe size is too big or cookies haven't been pared down enough
    return (document.cookie.length + size < MAX_COOKIE_SIZE);
}


function clearCookies(prefix) {
    var dc_arr = document.cookie.split("; ");
    forEach(dc_arr, function (dc) {
	if (dc.substr(0,prefix.length) == prefix) {
	    var cname = dc.substr(0,dc.indexOf("="));
	    //expire cookie
	    setCookie(cname, "", pastDate(), path());
	}
    });

}

function hs_saveCookie() {
    var cookie_val = serializeJSON(hs_ids);
    if (mallocCookie(cookie_val.length,HS_COOKIE_PREFIX)) {
	setCookie(cookie_name(), cookie_val, futureDate(), path());
    }
}

function hs_addControlCallback(a) {
    //log("adding callback to " + a);
    a.onclick = hs_toggle;
    hs_controls[hs_getTarget(a).id] = a;
    addElementClass(a,"hs-control-show");
}

function hs_getState(el) {
    return hs_ids[el.id];
}

function hs_setState(el, hs) {
    hs_ids[el.id] = hs;
    hs_saveCookie();
    //setCookie(cookie_name(el),hs,futureDate());
}

function hs_lookForSetting(a) {
   var e = hs_getTarget(a);
   var s = hs_getState(e);
   if (s == 0) {
      hs_hide(e);
   } 
   if (s == 1) {
      hs_show(e);
   }
}

function hs_getTarget(a) {
	return $(a.href.split("#")[1]);
}

function hs_toggle() {
	var target = hs_getTarget(this);
	if (hasElementClass(target,"hs-hide")) {
	   hs_show(target);
	   hs_setState(target,1);
        } else {
	   hs_hide(target);
	   hs_setState(target,0);
	}
	return false;
}

function hs_hide(e) {
	//log("hiding " + e);
	removeElementClass(e,"hs-show");
	addElementClass(e,"hs-hide");

	var control = hs_controls[e.id];
	removeElementClass(control,"hs-control-show");
	addElementClass(control,"hs-control-hide");
}

function hs_show(e) {
	//log("showing " + e);
	removeElementClass(e,"hs-hide");
	addElementClass(e,"hs-show");
	
	var control = hs_controls[e.id];
	removeElementClass(control,"hs-control-hide");
	addElementClass(control,"hs-control-show");
}

function hs_expand_all(dontreset) {
    forEach(getElementsByTagAndClassName("*","hs-hide"),hs_show);
    if (!dontreset) {
	hs_ids = {'*all':'expand'};
	hs_saveCookie();
    }
}

function hs_collapse_all(dontreset) {
    forEach(getElementsByTagAndClassName("*","hs-show"),hs_hide);
    if (!dontreset) {
	hs_ids = {'*all':'collapse'};
	hs_saveCookie();
    }
}

function hs_init() {
    log("initializing");
    hs_loadCookie();
    log("adding callbacks to controls");
    forEach(getElementsByTagAndClassName("a","hs-control"),hs_addControlCallback);
    log("hiding any divs that need to be initially hidden");
    forEach(getElementsByTagAndClassName("*","hs-init-hide"),hs_hide);
    if ('*all' in hs_ids) {
	//this is done after the above so any hs-init-hides
	if (hs_ids['*all'] == 'expand') {
	    hs_expand_all(/*dontreset=*/true);
	}
	else if (hs_ids['*all'] == 'collapse') {
	    hs_collapse_all(/*dontreset=*/true);
	}
    }
    log("check for cookies setting the state for any...");
    forEach(getElementsByTagAndClassName("a","hs-control"),hs_lookForSetting);

    if (window.location.href.split('#')[1]=='fb') {
        // If #fb anchor is specified, hide the instructions,
        // select the feedback tab, and focus on the feedback textarea.
        //(Adding this so faculty can rapidly enter feedback from ReviewAllResponses.smvc.
        accesstab($("tab_fb"));
        excerpt($('ainstr'));
        $('comment_form_textarea').focus();
    }
}

addLoadEvent(hs_init);
