function updateNoteList(formName, defs) {
    var f=document.forms[formName];
    var worksiteId;
    var noteAction;
    var url;
    if (typeof(f.worksiteId) == 'undefined') {
	//PLONE implementation
	noteAction = worksiteId = null;
	url = 'note_query';
    }
    else {
	//JAVA implementation
	noteAction = f.elements['action'].value;
	worksiteId = f.worksiteId.value;
	url = (typeof(f.queryUrl) != 'undefined') ? f.queryUrl.value : 'annotations.smvc';
    }
    defs = (defs) ? defs : {def:null};
    defs.def=doSimpleXMLHttpRequest(url,
	{'template':f.template.value,
	 'limitBy':f.limitBy.value,
	 'groupBy':f.groupBy.value,
	 'action':noteAction,
	 'worksiteId':worksiteId,
	 'recent': ( (f.recent) ? f.recent.value : null )
	});
    defs.def.addCallbacks(replaceNoteList,failedNoteUpdate);
}

function replaceNoteList(xmlhttp) {
    //createLoggingPane();
    
    var resXML=xmlhttp.responseXML;
    var curNoteList=$('noteList');

    if (typeof(myNoteDetails) != 'undefined') {
	myNoteDetails={}; //clear myNoteDetails to be repopulated
    }
    var noteListScrollPos=curNoteList.scrollTop;

    try {
	if (typeof(resXML.xml) != 'undefined') {
	    //IE HACK
	    //IE doesn't work because XML DOM isn't as rich as HTML DOM
	    //logDebug(typeof(resXML.xml));
	    var newNoteList=resXML.childNodes[1].firstChild.firstChild;
	    curNoteList.innerHTML=newNoteList.xml;
	}
	else {
	    //logDebug('DOM');
	    var newNoteList=resXML.getElementById('noteList');
	    if (newNoteList.outerHTML) {
		//SAFARI HACK
		//SAFARI fails because XML dom node can't be added into a replaceChild HTML function
		//logDebug('Safari');
		curNoteList.innerHTML=newNoteList.innerHTML;
	    }
	    else {
		//logDebug('probably Firefox');
		swapDOM(curNoteList,newNoteList);
	    }
	}
	//refresh the turnbuckle event listeners
	hs_init(); 
	$('noteList').scrollTop=noteListScrollPos;

	//refresh myNoteList and other JS obj's
	var newScripts=$('noteList').getElementsByTagName('script');
	forEach (newScripts, function(scrip) {
	    eval(scrip.innerHTML);
	});

	//refresh selections, etc.
	if (typeof(refreshEnvironment) != 'undefined') 
	    refreshEnvironment();
	/* clipStrip with NoteStrips disabled 
	//refresh clipStrip
	if ($('clipStripTrack')) {
	    replaceChildNodes('clipStripTrack');
	    addNoteStrips();
	}
	*/
    }
    catch(err) {
	logDebug(err);
	if (resXML.parseError) {
	    logDebug('xml error:', resXML.parseError.errorCode);
	    logDebug(resXML.parseError.reason);
	    logDebug(resXML.parseError.line);
	}
    }
}

function failedNoteUpdate(err) {
    logDebug('FAILED NOTE UPDATE:');
    for (a in err) {
	logDebug(a,": ",err[a]);
    }
}
