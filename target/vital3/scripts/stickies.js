var stickyHooks = {loaded:[], moved:[], resized:[], hidden:[], shown:[], contentChanged:[],
colorChanged:[],rolledUp:[],rolledDown:[],deleted:[]
};


function registerStickyHook(hook,f) {
	stickyHooks[hook].push(f);
}

function clearStickyHook(hook) {
	stickyHooks[hook] = [];
}

function callStickyHook(hook,sticky) {
	for (var i = 0; i < stickyHooks[hook].length; i++) {
	    f = stickyHooks[hook][i];
	    f(sticky);
        }
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


var showing = new Array();

function setZindices() {
	var base = 5;
   	for (var i = 0; i < showing.length; i++) {
		var s = new Sticky(showing[i]);
 		if(s.el) { 
        		s.el.style.zIndex = i + base;
	  		s.saveZindex();
        	}
   	}
}


function hideSticky(sticky_id) {
    	var sticky = new Sticky(sticky_id);
	if (sticky) { sticky.hide(); }    
}

function showSticky(sticky_id) {
        var sticky = new Sticky(sticky_id);
	if (sticky) { sticky.show(); }        
}


function rollup (id) {
	var s = new Sticky(id);
	s.rollup();
}

function startDrag(id) {
 	var s = new Sticky(id);
	if (s) { s.startDrag(); }
}

function endDrag(id) {
	var s = new Sticky(id);
	if (s) { s.endDrag(); }
}

function inDrag(id,x,y) {
	var s = new Sticky(id);
	if (s) { s.inDrag(x,y); }
}


var startX;
var startY;

var origWidth;
var origHeight;

function resizeStart(id,x,y) {
	startX = x;
	startY = y;
	var s = new Sticky(id);
	s.resizeStart();
}


function resize(id,x,y) {
	var s = new Sticky(id);
	var dx = (x - startX);
	var dy = (y - startY);
	s.resize(dx,dy,origWidth,origHeight);
}

function resizeEnd(id,x,y) {
	var s = new Sticky(id);
	s.resizeEnd();
}

function setColor(id,color) {
	var s = new Sticky(id);
	s.setColor(color);
	s.saveColor();
}


function Sticky(id) {
	this.id = id;
	this.el = $(this.id);
}

Sticky.prototype.startDrag = function () {
	this.el.style.opacity = 0.9;
	this.moveToTop();
}

Sticky.prototype.endDrag = function () {
	callStickyHook("moved",this);
	this.el.style.opacity = 0.9999;
	this.saveTop();
	this.saveLeft();
}

Sticky.prototype.inDrag = function (x,y) {
	var top = parseInt(this.el.style.top);
	if (top < 0) {
	   this.el.style.top = top + "px";
	}
}

Sticky.prototype.hide = function () {
    	callStickyHook("hidden",this);
        this.el.className = this.el.className.replace("showsticky", "stickynote"); 
	this.saveState();
}

Sticky.prototype.show = function () {
	 callStickyHook("shown",this);
	 this.el.className = this.el.className.replace("stickynote", "showsticky");
	 this.saveState();
	 this.moveToTop();
}

Sticky.prototype.rollup = function () {
	 var body = this.getBody();
	 if (body.style.display == "none") {
	    	callStickyHook("rolledUp",this);
	    	this.rollstickydown();
	 } else {
            	callStickyHook("rolledDown",this);
		this.savePropertyCookie("unrolled_height",this.el.style.height,futureDate());
	    	this.rollstickyup();
         }
         this.saveRolled();
}

Sticky.prototype.getResizer = function () {
	return getElementsByTagAndClassName("div","resizer",this.el)[0];
}

Sticky.prototype.getCloseButton = function () {
	var d = getElementsByTagAndClassName("div","stickyclose",this.el)[0];
	return d.getElementsByTagName('a')[0];
}

Sticky.prototype.createContent = function () {
	var title = this.getTitle();
	var body = this.getBody();
	this.el.removeChild(title);
	this.el.removeChild(body);
	var content = createDOM("div",{"class" : "stickycontent"},title,body);
	this.el.appendChild(content);
	this.createHeader();
	this.createFooter();
}

Sticky.prototype.createHeader = function () {
	var title = this.getTitle();
	title.setAttribute("id","handle" + this.id);
	var header = createDOM("div",{"class" : "stickyhead"});
	var parent = title.parentNode;
	var body = this.getBody();
	parent.removeChild(title);
	parent.insertBefore(header,body);
	header.appendChild(title);
	this.createCloseButton();
}

Sticky.prototype.createCloseButton = function () {
	var a = createDOM("a",{"href" : "", "title" : "close sticky"},"X");
       	a.onclick = new Function("hideSticky('" + this.id + "');return false;");
	var d = createDOM("div",{"class" : "stickyclose"},a);
	var title = this.getTitle();
	var header = title.parentNode;
	header.insertBefore(d,title);
}

Sticky.prototype.createFooter = function () {
	var content = this.getContent();
	var f = createDOM("div",{"class" : "stickyfooter"})
	content.appendChild(f);
	this.createColorBoxes();
	this.createResizer();
}

Sticky.prototype.createResizer = function () {
	var footer = this.getFooter();
	var r = createDOM("div",{"class" : "resizer", "id" : "resizer" + this.id});
	footer.appendChild(r);
       	Drag.init(r,null);
	r.onDragStart = new Function("x,y","resizeStart('" + this.id + "',x,y)");
	r.onDrag      = new Function("x,y","resize('"      + this.id + "',x,y)");
	r.onDragEnd   = new Function("x,y","resizeEnd('"   + this.id + "',x,y)");
}

var colors = ["yellow","blue","green","pink","purple"];

Sticky.prototype.createColorBoxes = function () {
	var footer = this.getFooter();
       	for (var j = 0; j < colors.length; j++) {
		var color = colors[j];
		var c = createDOM("div",{"class" : "stickycolor " + color});
	       	c.onclick = new Function("setColor('" + this.id + "','" + color + "')");
		footer.appendChild(c);
       	}
}

Sticky.prototype.getFooter = function () {
	return getElementsByTagAndClassName("div","stickyfooter",this.el)[0];
}

Sticky.prototype.getHeader = function () {
	return getElementsByTagAndClassName("div","stickyhead",this.el)[0];
}

Sticky.prototype.getTitle = function () {
	return getElementsByTagAndClassName("div","stickytitle",this.el)[0];
}

Sticky.prototype.getHandle = function () {
	return getElementsByTagAndClassName("div","stickytitle",this.el)[0];
}

Sticky.prototype.getContent = function () {
	return getElementsByTagAndClassName("div","stickycontent",this.el)[0];
}

Sticky.prototype.getBody = function () {
	return getElementsByTagAndClassName("div","stickybody",this.el)[0];
}

Sticky.prototype.rollstickyup = function () {
	 var body    = this.getBody();
	 var content = this.getContent();
	 var resizer = this.getResizer();
	 body.style.display    = "none";
	 resizer.style.display = "none";
	 this.el.style.height  = "24px";
	 content.style.height  = "18px";
}

var BODY_DH = 37;
var CONTENT_DH = 6;
var DEFAULT_HEIGHT = 200;
var DEFAULT_WIDTH = 200;


Sticky.prototype.rollstickydown = function () {
	 var body    = this.getBody();
	 var content = this.getContent();
	 var resizer = this.getResizer();
	 body.style.display    = "block";
	 resizer.style.display = "block";

	 var unrolled_height = getCookie("unrolled_height_" + this.cookie_name());
	 unrolled_height = unrolled_height ? unrolled_height : DEFAULT_HEIGHT + "px";

	 this.el.style.height = unrolled_height;
	 body.style.height    = parseInt(unrolled_height) - BODY_DH + "px";
	 content.style.height = parseInt(unrolled_height) - CONTENT_DH + "px";
}

Sticky.prototype.loadTop = function () {
	var top = this.getPropertyCookie("top");
	if (top) {
	   log("setting top to " + top);
	   this.el.style.top = top; 
        } else {
	   log("no top property");
        }
}

Sticky.prototype.loadLeft = function () {
	var left = this.getPropertyCookie("left");
	if (left) { 
            log("setting left to " + left);
            this.el.style.left = left; 
        } else {
            log("no left property");
        }
}

Sticky.prototype.loadZindex = function () {
	var zindex = this.getPropertyCookie("zindex");
    	if (zindex) { 
	        log("setting zindex to " + zindex);
        	this.el.style.zIndex = zindex; 
		showing[zindex] = this.id;
    	} else {
	        log("no zindex set");
        }
}

Sticky.prototype.loadState = function () {
    	var state = this.getPropertyCookie("state");
    	if (state) { 
                log("setting state to " + state);
       		if (state == "show") {
			setElementClass(this.el,"showsticky");
       		} else {
			setElementClass(this.el,"stickynote");
       		}
	} else {
	    log("no state property set");
        }
}

Sticky.prototype.loadWidth = function () {
	var width = this.getPropertyCookie("width");
    	if (width) { 
           log("setting width to " + width);
	   this.el.style.width = width; 
	} else {
	   log("no width property set");
	}
}

Sticky.prototype.loadHeight = function () {
   	var height = this.getPropertyCookie("height");
    	if (height) { 
	        log("setting height to " + height);
      		this.el.style.height = parseInt(height) + "px"; 
      		var content = this.getContent();
		log(content);
      		content.style.height = parseInt(height) - CONTENT_DH + "px";
      		body = this.getBody();
      		body.style.height = parseInt(height) - BODY_DH + "px";
    	} else {
	    log("no height property set");
	}
}

Sticky.prototype.loadRolled = function () {
    	var rolled = this.getPropertyCookie("rolled");
    	if (rolled == "rolled") {
       		this.rollstickyup();
    	} 
}

Sticky.prototype.loadColor = function () {
    	var color = this.getPropertyCookie("color");
    	if (!color) {
        	color = "yellow";
    	} 
    	this.setColor(color);
}

Sticky.prototype.loadPos = function () {
	this.loadTop();
	this.loadLeft();
	this.loadZindex();
	this.loadState();
    	this.loadWidth();
	this.loadHeight();
	this.loadRolled();
	this.loadColor();
}

Sticky.prototype.cookie_name = function () {
    name =  document.location + "#" + this.id;
    return name.replace(/\W/g,"_");
}

Sticky.prototype.getPropertyCookie = function (prop) {
	return getCookie(prop + "_" + this.cookie_name());
}

Sticky.prototype.savePropertyCookie = function (prop,value,d) {
	setCookie(prop + "_" + this.cookie_name(), value, d);
}

function futureDate() {
	var d = new Date();
	d.setTime(Date.parse('October, 4 2030 07:04:11'));
	return d;
}

Sticky.prototype.saveTop = function () {
	this.savePropertyCookie("top",this.el.style.top,futureDate());
}

Sticky.prototype.saveLeft = function () {
	this.savePropertyCookie("left",this.el.style.left,futureDate());
}

Sticky.prototype.saveZindex = function () {
	this.savePropertyCookie("zindex",this.el.style.zIndex,futureDate());
}

Sticky.prototype.saveState = function () {
        log("saving state " + this.el.className.indexOf("showsticky") == -1 ? 'hide' : 'show');
	this.savePropertyCookie("state", this.el.className.indexOf("showsticky") == -1 ? 'hide' : 'show', futureDate());
}

Sticky.prototype.saveWidth = function () {
	this.savePropertyCookie("width", this.el.style.width,  futureDate());
}

Sticky.prototype.saveHeight = function () {
	this.savePropertyCookie("height", this.el.style.height, futureDate());
}

Sticky.prototype.saveRolled = function () {
	body = this.getBody();
   	this.savePropertyCookie("rolled", body.style.display == "none" ? "rolled" : "", futureDate());
}

Sticky.prototype.saveColor = function () {
   	this.savePropertyCookie("color", this.getColor(), futureDate());
}

Sticky.prototype.savePos = function () {
	this.saveTop();
	this.saveLeft();
	this.saveZindex();
	this.saveState();	
	this.saveWidth();
    	this.saveHeight();
    	this.saveRolled();
 	this.saveColor();
}

Sticky.prototype.getColor = function () {
	var result = this.el.className.match(/stickycolor(\w+)/);
	if (result != null) {
	    return result[1];
	} else {
	    return "yellow";
	}
}

Sticky.prototype.moveToTop = function () {
   	var idx = -1;
   	for(var i = 0; i < showing.length; i++) {
      		if (showing[i] == this.id) {
	  		idx = i;
      		}
   	}
   	if (idx > -1) {
     		// take it out and put it at the beginning of the list
     		showing.splice(idx,1);
   	} 
   	showing.push(this.id);
   	setZindices();
}

Sticky.prototype.getColorBoxes = function () {
	return getElementsByTagAndClassName("div","stickycolor",this.el);
}

Sticky.prototype.setColor = function(color) {
	if (this.el.className.indexOf("stickycolor") != -1) {
		this.el.className = this.el.className.replace(/stickycolor\w+/,"stickycolor" + color);
	} else {
		addElementClass(this.el,"stickycolor" + color);
	}
	
}

Sticky.prototype.resizeStart = function () {
	origWidth  = parseInt(this.el.style.width  ? this.el.style.width  : DEFAULT_WIDTH + "px");
	origHeight = parseInt(this.el.style.height ? this.el.style.height : DEFAULT_HEIGHT + "px");
	var content = this.getContent();
	if (!content) {
	   log("no content for " + this.el.id);
	   return;
        }
	log("height: " + content.style.height);
	origContentHeight = parseInt(content.style.height ? content.style.height : (DEFAULT_HEIGHT - CONTENT_DH) + "px");
	body = this.getBody();
	origBodyHeight = parseInt(body.style.height ? body.style.height : (DEFAULT_HEIGHT - BODY_DH) + "px");
	this.moveToTop();
	this.el.style.opacity = 0.9;

}

Sticky.prototype.resize = function (dx,dy,origWidth,origHeight) {
	this.el.style.width  = origWidth  + dx + "px";
	this.el.style.height = origHeight + dy + "px";
	var content = this.getContent();
	content.style.height = origHeight - CONTENT_DH + dy + "px";
	var body = this.getBody();
	body.style.height = origHeight - BODY_DH + dy + "px";
}

Sticky.prototype.resizeEnd = function () {
	callStickyHook("resized",this);
	this.saveWidth();
	this.saveHeight();
	this.el.style.opacity = 0.9999;
}

Sticky.prototype.initDrag = function () {
	var handle = this.getHandle();
	Drag.init(handle,this.el,0,50000,0,50000);
	this.el.onDragStart = new Function("startDrag('" + this.id + "');");
	this.el.onDrag      = new Function("x,y","inDrag('" + this.id + "',x,y);");
    	this.el.onDragEnd   = new Function("endDrag('"   + this.id + "');");
}

Sticky.prototype.initRollup = function () {
	this.getHandle().ondblclick = new Function("rollup('" + this.id + "');");
}

Sticky.prototype.init = function () {
	this.createContent();
	this.initDrag();
	this.initRollup();
	this.loadPos();
}

function initStickies() {
      	var divs = document.getElementsByTagName("div");
	var stickies = getElementsByTagAndClassName("div","stickynote");
      	for(var i = 0; i < stickies.length; i++) {
       		var s = new Sticky(stickies[i].id);
		s.init();
      	}
}

addLoadEvent(initStickies);

