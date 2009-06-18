function resizecontainer() {
	//var leftboxwidth = document.getElementById('noteside').offsetWidth;
	var rulerwidth = document.getElementById('tapemeasure').offsetWidth;
	if  (rulerwidth < 950) {
		document.getElementById('binderbox').style.width = "905px";
		getElementsByTagAndClassName("DIV", "portal-footer")[0].style.width = "905px";
		getElementsByTagAndClassName("DIV", "portal-top")[0].style.width = "905px";
		getElementsByTagAndClassName("DIV", "portal-globalnav-search")[0].style.width = "905px";
	}
	else if (rulerwidth >= 950) {
		document.getElementById('binderbox').style.width = "auto";
		getElementsByTagAndClassName("DIV", "portal-footer")[0].style.width = "auto";
		getElementsByTagAndClassName("DIV", "portal-top")[0].style.width = "auto";
		getElementsByTagAndClassName("DIV", "portal-globalnav-search")[0].style.width = "auto";
	}
}

//if (navigator.userAgent.indexOf('MSIE') != - 1) {
	addLoadEvent(resizecontainer);
	window.onresize = resizecontainer;
//}
