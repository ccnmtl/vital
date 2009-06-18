function updatemyform(radioObject) {
    try {
	var isActive;
	var typeselect;
	if (radioObject && radioObject.id) {
	    isActive = radioObject.checked;
	    typeselect = radioObject.id;
	}
	else {
	    typeselect= ($('Clipbutton').checked) ? 'Clipbutton' : 'Markerbutton';
	    isActive = true;
	}
	
	var clipEndbox = document.getElementById("clipEndform");
	
	if ((isActive == true) && (typeselect == 'Markerbutton')) {
	    clipEndbox.style.visibility = 'hidden';
	    clipEndbox.style.display = 'none';
	    document.getElementById("changingtxt").innerHTML = 'Set marker time:';
	}
	else if ((isActive == true) && (typeselect == 'Clipbutton')) {
	    clipEndbox.style.visibility = 'visible';
	    clipEndbox.style.display = 'block';
	    document.getElementById("changingtxt").innerHTML = 'Set start time:';
	}
    } catch(err) {/* probably no form found */}

}


//i.e. updateState()
function refreshEnvironment() {
    updatemyform();
    initClipStrip();
    //If we are in Create or Edit mode
    if (noteID = currentUID()) { //EDIT mode
	highlightNote(noteID);
	//for clipStrip model
	//swapElementClass('clipStrip_'+noteID, 'noteStrip', 'noteSelected');
	var clip_h2 =  document.getElementById('vvheaderbanner');
	if (clip_h2.parentNode.className == 'vv_action_create') {
	    removeElementClass(clip_h2.parentNode,"vv_action_create");
	    addElementClass(clip_h2.parentNode,"vv_action_edit");
	    clip_h2.innerHTML =  "EDIT CLIP";
	}
	document.getElementById('newclipbutton').style.visibility = 'visible';
    }
    else { //CREATE mode
	highlightNote(); //remove any highlighting
	var clip_h2 =  document.getElementById('vvheaderbanner');
	if (clip_h2.parentNode.className == 'vv_action_edit') {
	    removeElementClass(clip_h2.parentNode,"vv_action_edit");
	    addElementClass(clip_h2.parentNode,"vv_action_create");
	    clip_h2.innerHTML =  "CREATE CLIP";
	}
	document.getElementById('newclipbutton').style.visibility = 'hidden';
    }
    
}

function currentUID() {
    var f=document.forms['videonoteform'];
    return (f.uid && f.uid.name=='uid') ? f.uid.value : null;
}

function clearUID(f) {
    if (typeof(f.uid) != 'undefined') {
	//if we were editing a note
	//var p=f.uid.parentNode;
	//p.removeChild(f.uid);
	f.uid.value="";
	f.uid.name="randomVar";
	return true;
    }
    return false;
}

function checkNoteType(myform) {
    if (document.testform['clipType'][0].checked) {
	document.testform.clipEnd.value = null;
	alert (document.testform.clipEnd.value);
    }
    return false;
}

var noteFields={
   'title':function(domNode) {
       //span[@class="expandtitle"]/child::text()
       var x=getElementsByTagAndClassName('a','expandtitle',domNode);
       return x[0].innerHTML;
   },
   'note':function(domNode) {
       //span[@class="notehtml"]/child::
       var x=getElementsByTagAndClassName('span','notehtml',domNode);
       return x[0].innerHTML;
   },
   'stickytags':function(domNode) {
       //span[@class="notetags"]/child::
       var x=getElementsByTagAndClassName('span','notetags',domNode);
       return x[0].innerHTML;
   }
};

//this will be decorated by each note displayed with its info
var myNoteDetails = {}; 


function unescape_from_xml(s) {
    s = s.replace(/&apos;/g,"'");
    s = s.replace(/&amp;/g,"&");
    s = s.replace(/&quote;/g,'"');
    s = s.replace(/&lt;/g,"<");
    s = s.replace(/&gt;/g,">");
    return s;
}

function editNote(noteID,noteGrp,dontPlayClip) {
    
    
    
    /* need id, title, description, tags, type, start/end time */
    noteGrp = (noteGrp) ? noteGrp : myNoteDetails[noteID].someGroup;
    var f=document.forms['videonoteform'];
    var id_string='FullNote_grp'+noteGrp+'_note'+noteID;
    var domNode = $(id_string);

    /* fill in form */
    if (typeof(f.uid) == 'undefined') {
	f.appendChild(INPUT({name:'uid',value:noteID,type:'hidden'}));
    }
    else {
	/* disabling note strips on ClipStrip
	var oldClipStrip=$('clipStrip_'+f.uid.value);
	if (oldClipStrip) {
	    swapElementClass(oldClipStrip, 'noteSelected', 'noteStrip');
	}
	*/
	f.uid.value=noteID;
	f.uid.name='uid';
    }
    
    title_escaped_twice = myNoteDetails[noteID].title;
    raw_title = unescape_from_xml (unescape_from_xml (title_escaped_twice));
    
    f.title.value = raw_title;
    f.clipBegin.value = myNoteDetails[noteID].clipBegin;
    f.clipEnd.value = myNoteDetails[noteID].clipEnd;
    if (myNoteDetails[noteID].clipType == 'Clip') {
	$('Clipbutton').checked = true;
	$('Markerbutton').checked = false;
    }
    else {
	$('Clipbutton').checked = false;
	$('Markerbutton').checked = true;
    }

    var noteBody = noteFields['note'](domNode);
    f.note.value = noteBody;
    if (typeof tinyMCE != "undefined") tinyMCE.setContent(noteBody);

    tagString = noteFields['stickytags'](domNode);
    setTags(tagString.split(', '));

    /* Clean up VV environment */
    refreshEnvironment();
    if (!dontPlayClip) {
	refresh_mymovie(myNoteDetails[noteID].clipBegin+':00.0', 
			myNoteDetails[noteID].clipEnd+':00.0', 
			myNoteDetails[noteID].clipType);
    }
    
}

function validTitle(title) {
    var validchars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ 1234567890-=+_-&*()";
    var badchars = ''; // anything other than the characters above will break either the Ajax calls, the Javascript, or both.
    for (var i=0; i<=title.length; i++) {
        if (validchars.indexOf(title.charAt(i)) == -1) {
            badchars = badchars + title.charAt(i);
        }
    }
    if (badchars != "") {
        alert ("Sorry, you can't use any of the characters '" + badchars + "' in the title of your note.");
        return false;
    }
    return true
}


function verifyIntegrity(formID) {
    var noteData=document.forms[formID];
    //logDebug(noteData.clipType[0].checked,'XX',noteData.clipBegin.value,'XX',noteData.title.value);
    if (!(noteData.clipType[0].checked || noteData.clipType[1].checked) ||
        !noteData.clipBegin.value || !noteData.title.value) {
        
	alert('Please fill in a Time and Title before saving.');
	return false;
    }
    
    if (!validTitle(noteData.title.value)) {
        return false;
    }
    
    try {
	formToClip();
    }
    catch(err) {
	alert(err);
	return false;
    }
    saveNote(noteData)
}

function saveNote(f) {

    if (typeof tinyMCE != "undefined") tinyMCE.triggerSave();

    var reqQueryString = queryString(f);

    var url;
    if (typeof(f.worksiteId) == 'undefined') {
	//PLONE implementation
	url = 'submitSticky';
    }
    else {
	//JAVA implementation
	url = 'annotations.smvc';
	if (f.uid && f.uid.name == 'uid') {
	    reqQueryString = reqQueryString+'&id='+f.uid.value;
	}
    }
    //logDebug(f.note.value,'innerHTML:',f.note.innerHTML);

    var req=getXMLHttpRequest();
    req.open("POST",url,true);
    req.setRequestHeader("Content-Type", 'application/x-www-form-urlencoded');
    
    def= sendXMLHttpRequest(req,reqQueryString);
   //def= sendXMLHttpRequest(req,postString);
    def.addCallbacks(saveNoteSuccess,saveNoteFailed);

    var myStatus=$('submitNoteStatus');
    myStatus.innerHTML='Saving...  Please wait.';
    myStatus.style.visibility='visible';
}

function saveNoteSuccess(req) {
    var noteID = evalJSON(req.responseText)['id']; //from json data
    //?clear data?
    if (top.opener) {
	try {
	    if (top.opener.document.forms['noteDisplay']) {
		top.opener.document.forms['noteDisplay'].submit();
	    }
	    materialID=document.forms['noteDisplay'].elements['limitBy'].value;
	    a=top.opener.document.getElementById('assnHasNotes_'+materialID);
	    u=top.opener.document.getElementById('unitHasNotes_'+materialID);
	    if (a) {
		a.innerHTML='Yes';
		a.style.color='green';
	    }
	    if (u) {
		u.innerHTML='Yes';
		u.style.color='green';
	    }
	} catch(err) {
	    //logDebug("couldn't refresh parent screen");
	    for (a in err) {logError(a,':',err[a]); }
	}
    }
    
    //tell the user save succeeded
    var myStatus=$('submitNoteStatus');
    myStatus.innerHTML='Your clip has been saved...';
    myStatus.style.visibility='visible';

    window.setTimeout(function() {
	//myStatus.style.display='none';
	myStatus.style.visibility='hidden';
    },5000); //5 seconds


    clearNote(); //alternative is to uncomment myDefs.def.(...editNote...) below
    //refresh bottom notes
    myDefs = {def:new Deferred()};
    updateNoteList('noteDisplay',myDefs);
    //document.forms['noteDisplay'].submit(); //equivalent to updateNoteList('noteDisplay')

    //go into edit-mode and refresh contents based on what the server saved
    //myDefs.def.addCallback(partial(editNote,noteID,false,true));
    //logDebug('my noteid',noteID,myDefs.def.chain);
    
    //editNote(noteID);
}

function clearNote() {
    f=document.forms['videonoteform'];
    f.clipBegin.value = '00:00:00';
    //logDebug(f.clipBegin.value);
    f.clipEnd.value = '00:00:00';
    f.title.value='';
    //logDebug(f.title.value);
    
    if ( typeof tinyMCE != "undefined") tinyMCE.setContent('<br />');
    else f.note.value = "";
    
    setTags([]);

    if (clearUID(f)) refresh_mymovie();

    refreshEnvironment();

    document.getElementById('currtime').value = '00:00:00';
    
}


function saveNoteFailed(err) {
    logError('failed to save note',err);
    var myStatus=$('submitNoteStatus');
    myStatus.innerHTML='The clip failed to save';
    myStatus.style.color='red';
    myStatus.style.visibility='visible';
}

function getNoteListings() {
    return getElementsByTagAndClassName(null, 'noteset', $('noteList'));
}


function highlightNote(noteID) {
    if (!noteID) {
	var selectednotedivs = getElementsByTagAndClassName("DIV", "selectedNoteSet");
	forEach(selectednotedivs,
		function(n){ removeElementClass(n,"selectedNoteSet"); }
		);
	return;
    }
    var jumpNode = false;
    var nodes = getNoteListings();
    forEach(nodes, function(n) {
	//logDebug('n.name:',n.name);
	if (n.getAttribute('name') == noteID) {
	    addElementClass(n, 'selectedNoteSet');
	    if (jumpNode == false) {
		jumpNode=n;
	    }
	}
	else {
	    removeElementClass(n, 'selectedNoteSet');
	}
    });
    if (jumpNode) {
	//if (typeof(window.getComputedStyle) != 'undefined') { 
	    //Mozilla HACK (but Safari seems to need it too)
	    $('noteList').scrollTop=jumpNode.offsetTop-jumpNode.offsetHeight/2;
	    /*}
	else {
	    $('noteList').scrollTop=jumpNode.offsetTop;
	    }*/
    }
}

function deleteNote(noteID,noteGrp) {
    //var noteTitle= myNoteDetails[noteID].title;
     var noteTitle= noteFields['title']($('FullNote_grp'+noteGrp+'_note'+noteID));
    //evaluate(noteFields['title'],$(noteID), null, 0, null).iterateNext().nodeValue;
    if (confirm('Are you sure you want to delete "'+noteTitle+'"?')) {
	var url;
	var f = document.forms['videonoteform'];
	if (typeof(f.worksiteId) == 'undefined') {
	    //PLONE implementation
	    url = '/vital/stickies_tool/deleteSticky?stickyUID='+noteID;
	}
	else {
	    //JAVA implementation
	    url = 'annotations.smvc?action=deleteNote&id='+noteID+'&worksiteId='+f.worksiteId.value;
	}
	if (currentUID() == noteID) {
	    /*if they were editing the note they're deleting
	      then leave the data, but switch to create-mode;
	     */
	    clearUID(f);
	    refreshEnvironment();
	}
	def=doSimpleXMLHttpRequest(url);
	def.addCallback(function(){
	    forEach(getNoteListings(), function(n) {
		nm=n.getAttribute('name');
		if (noteID==nm) {
		    removeElement(n);
		}
	    });
	});
    }
}

