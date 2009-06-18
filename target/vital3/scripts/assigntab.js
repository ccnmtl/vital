function inittabs() {
log('inittabs: hiding comments');
document.getElementById('am').style.display = 'none';
document.getElementById('am').style.visibility = 'hidden';
//document.getElementById('um').style.display = 'none';
//document.getElementById('um').style.visibility = 'hidden';
document.getElementById('mn').style.display = 'block';
document.getElementById('mn').style.visibility = 'visible';
document.getElementById('tab3').className = null;
document.getElementById('tab3').className = "tab-3 active";
document.getElementById('fb').style.display = 'none';
document.getElementById('fb').style.visibility = 'hidden';
}

function inittabs_notmine() {
log('inittabs_notmine: hiding comments');
document.getElementById('am').style.display = 'block';
document.getElementById('am').style.visibility = 'visible';
//document.getElementById('um').style.display = 'none';
//document.getElementById('um').style.visibility = 'hidden';
document.getElementById('mn').style.display = 'none';
document.getElementById('mn').style.visibility = 'hidden';
document.getElementById('tab1').className = null;
document.getElementById('tab1').className = "tab-1 active";
document.getElementById('fb').style.display = 'none';
document.getElementById('fb').style.visibility = 'hidden';
}

function inittabs_feedback() {
log('inittabs_feedback: showing faculty feedback form.');
document.getElementById('am').style.display = 'none';
document.getElementById('am').style.visibility = 'hidden';
//document.getElementById('um').style.display = 'none';
//document.getElementById('um').style.visibility = 'hidden';
document.getElementById('mn').style.display = 'none';
document.getElementById('mn').style.visibility = 'hidden';
document.getElementById('tab4').className = null;
document.getElementById('tab4').className = "tab-1 active";
document.getElementById('fb').style.display = 'block';
document.getElementById('fb').style.visibility = 'visible';
}


function getURLID(a) {
	var temp = new Array();
	temp = a.href.split("#")[1];
}

function changetab(a) {
    logDebug ("Change tab called with " + a.name);
	var thistab = a.parentNode.parentNode.className;
	temp = thistab.split(" ")[1];
	if (temp == null) {
		a.parentNode.parentNode.className = thistab + " " + "active";
	}
}

function resettabs() {
    log('Resettabs');
	var assigntab = new Array();
	var assigntabclass = new Array();
	for (i=0; i<4; i++) {
		if (i!=1) {
    		tabindex = "tab" + (i+1);
            log('Tabindex: ' + tabindex);
            if (document.getElementById(tabindex)) { // sometimes the back-end decides to hide certain tabs.
    			assigntab[i] = document.getElementById(tabindex);
    			assigntabclass[i] = assigntab[i].className;
    			splitclass = assigntabclass[i].split(" ")[1];
    			if (splitclass == 'active') {
    				assigntab[i].className = null;
    				assigntab[i].className = "tab-" + (i+1);
    			}
			}
		}
	}
	document.getElementById('am').style.display = 'none';
	document.getElementById('am').style.visibility = 'hidden';
	//document.getElementById('um').style.display = 'none';
	//document.getElementById('um').style.visibility = 'hidden';
	document.getElementById('mn').style.display = 'none';
	document.getElementById('mn').style.visibility = 'hidden';
	document.getElementById('fb').style.display = 'none';
	document.getElementById('fb').style.visibility = 'hidden';
}

function mynewfunc(a) {
	var temp = new Array();
	temp = a.href.split("#")[1];
	log ('temp is ' + temp);
	if ((document.getElementById(temp).style.display == 'none')) {
		document.getElementById(temp).style.visibility = 'visible';
		document.getElementById(temp).style.display = 'block';
	}
}

function accesstab(a){
	resettabs();
	changetab(a);
	mynewfunc(a);
}

/*
toggle preview mode for the assignment edit box
*/
function toggleAssignmentView(){
	 viewbox = document.getElementById('sticky_question_view');
	 editbox = document.getElementById('essaybox');
	 toggleButton  = document.getElementById('preview_edit_button');

	 // if we are in view mode, switch to edit mode
	 if (viewbox.style.display == 'block') {
	    hideElement(viewbox);	 
	    showElement(editbox);
	    toggleButton.value = 'Preview';
	 }
	 else {
	    hideElement(editbox);	 
	    // copy contents out of tinymce for preview
	    $('sticky_question_view').innerHTML = tinyMCE.getContent(); 
	    showElement(viewbox);
	    toggleButton.value = 'Edit';	      
	 }
	 return true;
}

function wordCount() {
    if ($("wordcounter") == null) { return };
    var content = ($('sticky_question_view')) ? $('sticky_question_view').innerHTML : tinyMCE.getContent();
    if (typeof(content) == 'string') {
	wordNum = content.split(" ").length;
	$("wordcounter").innerHTML = wordNum;
    }
}
addLoadEvent(wordCount);
