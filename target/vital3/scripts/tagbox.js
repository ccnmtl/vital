var uAgent = navigator.userAgent;
var aName = navigator.appName;
var aVendor = navigator.vendor;
var aVersion = parseInt(navigator.appVersion);

var macOS = (uAgent.indexOf('Mac') != -1 || uAgent.indexOf('mac') != -1);
var winOS = (uAgent.indexOf('Windows') != -1 || uAgent.indexOf('windows') != -1);

var IEbrowser = (uAgent.indexOf('MSIE') != - 1);
var IEbrowser6 = (uAgent.indexOf("MSIE 6")!=-1);


var open_box = false;


function opentagbox() {
	var tagboxdiv = document.getElementById('tags_box_id');
	tagboxdiv.style.visibility = 'visible';

	tagboxdiv.style.display = 'block';
	
	if (IEbrowser) {
// this is actually *breaking* ie6, so i'm turning it off...
//		document.getElementById('ietogglespecial').style.visibility = 'hidden';
	}
	
	$('newtagsfield').focus();

}

function closetagbox() {

	var tagboxdiv = document.getElementById('tags_box_id');
	
	tagboxdiv.style.visibility = 'hidden';
	tagboxdiv.style.display = 'none';
	if (IEbrowser) {

// this is actually *breaking* ie6, so i'm turning it off...
//		document.getElementById('ietogglespecial').style.visibility = 'visible'; 


	}

	//this is so all items are selected before a submit
	v_inout_selectAllWords('assignedTagsList','noteTagsList')

}

function openhelptagbox() {
	var tagboxdiv = document.getElementById('whataretags');
	if (open_box == false) {
		tagboxdiv.style.visibility = 'visible';
		tagboxdiv.style.display = 'block';
		open_box = true;
	}
	else {
		tagboxdiv.style.visibility = 'hidden';
		tagboxdiv.style.display = 'none';
		open_box = false;
	}
}

function closehelptagbox() {
	var tagboxdiv = document.getElementById('whataretags');
	tagboxdiv.style.visibility = 'hidden';
	tagboxdiv.style.display = 'none';
	open_box = false;
}

function assignTags() {
    v_inout_moveKeywords('existingTagsList','assignedTagsList')
}
function removeTags() {
    v_inout_moveKeywords('assignedTagsList','existingTagsList')
}
function addTags() {
    var newTags=$('newtagsfield').value;
    if (newTags=='') {
	return;
    }
    var tagArray=newTags.split(',');
    for (x=0; x<tagArray.length; x++) {
	var newTag = tagArray[x];
	//get rid of leading and trailing whitespace
	newTag = newTag.replace(/^\s*/,'');
	newTag = newTag.replace(/\s*$/,'');
	if (newTag != '') {
	    v_inout_addNewKeyword('assignedTagsList',newTag,newTag);
	}
    }
    $('newtagsfield').value='';
}

function setSelected(obj,val) {
    //IE6 resilient version
    try {
	obj.selected = val;
    }
    catch(err) {
	/*IE6 returns an 'unspecified error' on the 
	  last index but successfully sets the value */
    }
}

function setTags(tag_array) {
    //shuffles selection to conform with tag_array
    var newTags={};
    var i;
    for (i=0;i<tag_array.length;i++) {
	newTags[tag_array[i]]=1;
    }
    curList=$('assignedTagsList');
    otherList=$('existingTagsList');

    i = otherList.length;
    while (--i >= 0) {
	//select the ones that should be added
	setSelected(otherList[i],
		    ((otherList[i].value in newTags) ? true : false));
    }
    assignTags();
    i = curList.length;
    while (--i >= 0) {
	//select the ones that should be removed
	setSelected(curList[i],
		    ((curList[i].value in newTags) ? false : true));
    }
    removeTags();
    v_inout_selectAllWords('assignedTagsList','noteTagsList');
    if (IEbrowser6) {//this is so IE keeps the fields hidden
	opentagbox(); 
	closetagbox();
    }
}

addLoadEvent(function() {
    tagfield=$('newtagsfield');
    if (tagfield) {
	tagfield.onkeypress=function(evt) {
	    evt = (evt) ? evt : window.event;
	    if (evt.keyCode==13) {
		addTags();
		try {
		    evt.cancelBubble=true;
		    evt.reason = 1;
		    evt.stopPropagation();
		    evt.preventDefault();
		}
		catch(err) {}
		return false;
	    }
	};
    }
});

/* stolen from 
 * Archetypes/skins/archetypes/widgets/js/inandout.js 
 * 'v_' is for VITAL
 */
function v_inout_selectAllWords(theList,stringList) {
  myList = document.getElementById(theList);
  myString = '';
  for (var x=0; x < myList.length; x++) {
    setSelected(myList[x],true);
    if (x>0) {
	myString+=", ";
    }
    myString+=myList[x].text;
  }
  replaceChildNodes(stringList, myString);
}

function v_inout_addNewKeyword(toList, newText, newValue) {
  theToList=document.getElementById(toList);
  for (var x=0; x < theToList.length; x++) {
    if (theToList[x].text == newText) {
      return false;
    }
  }
  theLength = theToList.length;
  theToList[theLength] = new Option(newText);
  theToList[theLength].value = newValue;
}

function v_inout_moveKeywords(fromList,toList) {
  theFromList=document.getElementById(fromList);
  theToList=document.getElementById(toList);
  for (var x=0; x < theFromList.length; x++) {
    if (theFromList[x].selected) {
	appendChildNodes(theToList,theFromList[x]);
	--x;  //just deleted one, so backtrack the counter
	//inout_addNewKeyword(toList, theFromList[x].text, theFromList[x].value);
    }
  }
  /*
  for (var x=theToList.length-1; x >= 0 ; x--) {
    if (theToList[x].selected) {
      theToList[x] = null;
    }
  }
  */
}
