/* structured text toolbar functions 
 *
 * copied and adapted from mediawiki src (wikibits.js)
*/

var clientPC = navigator.userAgent.toLowerCase(); // Get client info

var is_gecko = ((clientPC.indexOf('gecko')!=-1) && (clientPC.indexOf('spoofer')==-1)
                && (clientPC.indexOf('khtml') == -1) && (clientPC.indexOf('netscape/7.0')==-1));
var is_safari = ((clientPC.indexOf('applewebkit')!=-1) && (clientPC.indexOf('spoofer')==-1));
var is_khtml = (navigator.vendor == 'KDE' || ( document.childNodes && !document.all && !navigator.taintEnabled ));
if (clientPC.indexOf('opera')!=-1) {
    var is_opera = true;
    var is_opera_preseven = (window.opera && !document.childNodes);
    var is_opera_seven = (window.opera && document.childNodes);
}


// apply tagOpen/tagClose to selection in textarea,
// use sampleText instead of selection if there is none
function insertTags(form, textarea_id, tagOpen, tagClose, sampleText) {
    // alert ("entering insertTags " + form);
	var textarea = document.getElementById(textarea_id);
	// IE
	if(document.selection  && !is_gecko) {

		var theSelection = document.selection.createRange().text;
		if(!theSelection) { theSelection=sampleText;}
		textarea.focus();
		if(theSelection.charAt(theSelection.length - 1) == " "){// exclude ending space char, if any
			theSelection = theSelection.substring(0, theSelection.length - 1);
			document.selection.createRange().text = tagOpen + theSelection + tagClose + " ";
		} else {
			document.selection.createRange().text = tagOpen + theSelection + tagClose;
		}

	// Mozilla
	} else if(textarea.selectionStart || textarea.selectionStart == '0') {
 		var startPos = textarea.selectionStart;
		var endPos = textarea.selectionEnd;
        // modified by jsb for safari
		var scrollTop=textarea.scrollTop;
		var myText = (textarea.value).substring(startPos, endPos);
		if(!myText) { myText=sampleText;}
		if(myText.charAt(myText.length - 1) == " "){ // exclude ending space char, if any
			subst = tagOpen + myText.substring(0, (myText.length - 1)) + tagClose + " ";
		} else {
			subst = tagOpen + myText + tagClose;
		}
		textarea.value = textarea.value.substring(0, startPos) + subst +
		  textarea.value.substring(endPos, textarea.value.length);
		textarea.focus();

		var cPos=startPos+(tagOpen.length+myText.length+tagClose.length);
		textarea.selectionStart=cPos;
		textarea.selectionEnd=cPos;
		textarea.scrollTop=scrollTop;

	} // we dont use this popup prompt technique.  just a simpler fomratting insertion

      else {
        // safari doesnt support selected text w/in an imput area
        // best we can do is plop in the sample
        subst = tagOpen + sampleText + tagClose;
		textarea.value = textarea.value  + "\n" + subst;
    }

	// reposition cursor if possible
	if (textarea.createTextRange) textarea.caretPos = document.selection.createRange().duplicate();
}
