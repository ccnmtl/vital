/*----------------------------------------------------------------------------\
|                                Table Sort                                   |
|-----------------------------------------------------------------------------|
|                         Created by Erik Arvidsson                           |
|                  (http://webfx.eae.net/contact.html#erik)                   |
|                      For WebFX (http://webfx.eae.net/)                      |
|-----------------------------------------------------------------------------|
| A DOM 1 based script that allows an ordinary HTML table to be sortable.     |
|-----------------------------------------------------------------------------|
|                  Copyright (c) 1998 - 2002 Erik Arvidsson                   |
|-----------------------------------------------------------------------------|
| This software is provided "as is", without warranty of any kind, express or |
| implied, including  but not limited  to the warranties of  merchantability, |
| fitness for a particular purpose and noninfringement. In no event shall the |
| authors or  copyright  holders be  liable for any claim,  damages or  other |
| liability, whether  in an  action of  contract, tort  or otherwise, arising |
| from,  out of  or in  connection with  the software or  the  use  or  other |
| dealings in the software.                                                   |
| - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - |
| This  software is  available under the  three different licenses  mentioned |
| below.  To use this software you must chose, and qualify, for one of those. |
| - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - |
| The WebFX Non-Commercial License          http://webfx.eae.net/license.html |
| Permits  anyone the right to use the  software in a  non-commercial context |
| free of charge.                                                             |
| - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - |
| The WebFX Commercial license           http://webfx.eae.net/commercial.html |
| Permits the  license holder the right to use  the software in a  commercial |
| context. Such license must be specifically obtained, however it's valid for |
| any number of  implementations of the licensed software.                    |
| - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - |
| GPL - The GNU General Public License    http://www.gnu.org/licenses/gpl.txt |
| Permits anyone the right to use and modify the software without limitations |
| as long as proper  credits are given  and the original  and modified source |
| code are included. Requires  that the final product, software derivate from |
| the original  source or any  software  utilizing a GPL  component, such  as |
| this, is also licensed under the GPL license.                               |
|-----------------------------------------------------------------------------|
| 1998-??-?? | First version                                                  |
|-----------------------------------------------------------------------------|
| Created 1998-??-?? | All changes are in the log above. | Updated 2001-??-?? |
|-----------------------------------------------------------------------------|
| Also, modified by Anders Pearson to make this work with Safari, and to make |
| the HTML more standard compliant.                                           |
| Zarina Mustapha made some changes to highlight the sorted columns.          |
|-----------------------------------------------------------------------------|
| 2004 Summer | Modified                                                      |
\----------------------------------------------------------------------------*/

function addLoadEvent(func) {
  var oldonload = window.onload;
  if (typeof window.onload != 'function') {
    window.onload = func;
  } else {
    window.onload = function() {
      oldonload();
      func();
    }
  }
}

addLoadEvent(sortables_init);


/*function getCookie(name)
{
    var dc = document.cookie;
    var prefix = name + "=";
    var begin = dc.indexOf("; " + prefix);
    if (begin == -1)
    {
        begin = dc.indexOf(prefix);
        if (begin != 0) return null;
    }
    else
    {
        begin += 2;
    }
    var end = document.cookie.indexOf(";", begin);
    if (end == -1)
    {
        end = dc.length;
    }
    return unescape(dc.substring(begin + prefix.length, end));
}

function setCookie(name, value, expires, path, domain, secure)
{   
    document.cookie= name + "=" + escape(value) +
        ((expires) ? "; expires=" + expires.toGMTString() : "") +
        ((path) ? "; path=" + path : "") +
        ((domain) ? "; domain=" + domain : "") +
        ((secure) ? "; secure" : "");
}
*/

var SORT_COLUMN_INDEX;

function sortables_init() {
    // Find all tables with class sortable
    if (!document.getElementsByTagName) return;
    tbls = document.getElementsByTagName("table");
    for (ti=0;ti<tbls.length;ti++) {
        thisTbl = tbls[ti];
        if (thisTbl.id) {
            // only bother with tables that have ids
            default_sort(thisTbl);
        }
    }
}

/* function cookie_name(table) {
    name =  document.location + "#" + table.id;
    alert (name);
    return name.replace(/\W/g,"_");
} */

function default_sort(table) {
    // first, see if there's a cookie with our sort column and/or order
    /* column = getCookie("sortcolumn_" + cookie_name(table));
    order = getCookie("sortorder_" + cookie_name(table)); */
    // look for a column with the 'DefaultSort'
    ths = document.getElementsByTagName("th");
    /*if (column != null) {
        th = ths[column];
        th._descending = false;
        if (order != null) {
            if (order == "desc") {
                th._descending = true;
            }
        }
        sortColumn(th);
    } else {*/ 
        for (i=0; i<ths.length; i++) {
            th = ths[i];
            if (th.className.indexOf("DefaultSort") != -1) {
                th._descending = false;
                if (th.className.indexOf("DESC") != -1) {
                    th._descending = true;
                }
                
                sortColumn(th);
            }
        }
    //}
}

var dom = (document.getElementsByTagName) ? true : false;
var ie5 = (document.getElementsByTagName && document.all) ? true : false;
var arrowUp, arrowDown;

if (ie5 || dom)
	initSortTable();

function initSortTable() {
// i'd really like to remove the image src's from the javascript if
// possible. ideally, we would add a span with a class and any images could
// be specified in CSS somewhere else.
	imgup = this.document.createElement("img");
	useImageLib =(typeof(ImageLib)!='undefined');
	if (useImageLib && ImageLib.up) {
   	   imgup.src = ImageLib.up;
	} else {
	   imgup.src = "images/up.gif";
	}
	arrowUp = document.createElement("span");
	arrowUp.appendChild(imgup);
	arrowUp.className = "arrow";

	imgdwn = this.document.createElement("img");
	if (useImageLib && ImageLib.down) {
	   imgdwn.src = ImageLib.down;
	} else {
	   imgdwn.src = "images/down.gif";
	}
	arrowDown = document.createElement("span");
	arrowDown.appendChild(imgdwn);
	arrowDown.className = "arrow";
}



function hilite(tableNode,nCol)
{
	var thead = tableNode.getElementsByTagName('THEAD')[0];
	var trows = thead.getElementsByTagName('TR')[0];
	var ths = trows.getElementsByTagName('TH');
	for (var i = 0; i < ths.length; i++) {
            classes = ths[i].className.split(" ");
            new_classes = "";
            for (var j = 0; j < classes.length; j++) {
                if ((classes[j] == "TableSortSelected") || 
                    (classes[j] == "TableSortUnselected")) {
                } else {
                    new_classes += " " + classes[j];
                }
            }
            new_classes += " TableSortUnselected";
            ths[i].className = new_classes;
        }
        classes = ths[nCol].className.split(" ");
        new_classes = "";
        for (var j = 0; j < classes.length; j++) {
            if ((classes[j] == "TableSortSelected") || 
                (classes[j] == "TableSortUnselected")) {
            } else {
                new_classes += " " + classes[j];
            }
        }
        new_classes += " TableSortSelected";
        ths[nCol].className = new_classes;
        
}



function sortTable(tableNode, nCol, bDesc, sType) {
	var tBody = tableNode.tBodies[0];
	var trs = tBody.rows;
	var trl= trs.length;
	var a = new Array();
	for (var i = 0; i < trl; i++) {
		a[i] = trs[i];
	}
	
	var start = new Date;
	window.status = "Sorting data...";
	a.sort(compareByColumn(nCol,bDesc,sType));
	window.status = "Sorting data done";
	
	for (var i = 0; i < trl; i++) {
		tBody.appendChild(a[i]);
//		window.status = "Updating row " + (i + 1) + " of " + trl +
//						" (Time spent: " + (new Date - start) + "ms)";
	}
	
	// check for onsort
	if (typeof tableNode.onsort == "string")
		tableNode.onsort = new Function("", tableNode.onsort);
	if (typeof tableNode.onsort == "function")
		tableNode.onsort();
}

function CaseInsensitiveString(s) {
	return String(s).toUpperCase();
}

// Eddie adding this function Jan 22 2007.
// Returns an int that can be used to compare strings of the form 'Spring 2006'
function parseSemester(s) {
    s = s.replace(/^\s+|\s+$/g,"").toUpperCase();
    //logDebug ("ParseSemester called on " + s);
    semesterid = {
        'SPRING':0
        ,'SUMMER':1
        ,'FALL':2
    };
    a = s.split (" ");
    if (a.length != 2) {
        //logDebug (s + " is not a valid semester name.");
        return -1;
    }
    term_label = a[0];
    if(keys(semesterid).indexOf(term_label) == -1) {
        //logDebug (a[0] + " needs to be 'Spring', 'Summer' or 'Fall'.");
        return -1;
    }
    semester = semesterid[term_label];
	year = a[1];
	if (isNaN(parseInt(year))) {
	    //logDebug (year + " doesn't look like a year.");
        return -1;
    }
    sorthash = year * 10 + semester;
	return sorthash;
}
//End Eddie addition

function parseDate(s) {
    return Number(s.replace(/\-/g, ""));
//	return Date.parse(s.replace(/\-/g, "/"));
}

/* alternative to number function
 * This one is slower but can handle non numerical characters in
 * the string allow strings like the follow (as well as a lot more)
 * to be used:
 *    "1,000,000"
 *    "1 000 000"
 *    "100cm"
 */

function Numeric(s) {
    return Number(s.replace(/[^0-9\.]/g, ""));
}

function compareByColumn(nCol, bDescending, sType) {
	var c = nCol;
	var d = bDescending;
	
	var fTypeCast = String;
	if (sType == "Number")
		fTypeCast = Numeric;
	else if (sType == "Date")
		fTypeCast = parseDate;
	else if (sType == "CaseInsensitiveString")
		fTypeCast = CaseInsensitiveString;
    //Eddie adding this Jan 22 2007
	else if (sType == "Semester")
		fTypeCast = parseSemester;
    //End Eddie addition  

	return function (n1, n2) {
		if (fTypeCast(getInnerText(n1.cells[c])) < fTypeCast(getInnerText(n2.cells[c])))
			return d ? -1 : +1;
		if (fTypeCast(getInnerText(n1.cells[c])) > fTypeCast(getInnerText(n2.cells[c])))
			return d ? +1 : -1;
		return 0;
	};
}

function sortColumnWithHold(e) {
	// find table element
	var el = ie5 ? e.srcElement : e.target;
	var table = getParent(el, "table");
	
	// backup old cursor and onclick
	var oldCursor = table.style.cursor;
	var oldClick = table.onclick;
	
	// change cursor and onclick	
	table.style.cursor = "wait";
	table.onclick = null;
	
	// the event object is destroyed after this thread but we only need
	// the srcElement and/or the target
	var fakeEvent = {srcElement : e.srcElement, target : e.target};
	
	// call sortColumn in a new thread to allow the ui thread to be updated
	// with the cursor/onclick
	window.setTimeout(function () {
		sortColumn(fakeEvent);
		// once done resore cursor and onclick
		table.style.cursor = oldCursor;
		table.onclick = oldClick;
	}, 100);
}

function sortColumn(e) {
	var tmp = e;
        if (e.className == undefined) {
            tmp = e.target ? e.target : e.srcElement;
        }
	var tHeadParent = getParent(tmp, "thead");
	var el = getParent(tmp, "th");

	if (tHeadParent == null)
		return;
		
	if (el != null) {
        // Eric added this part:
        if (el.id.indexOf("noSort") != -1) return;
        // end Eric's addition
        
		var p = el.parentNode;
		var i;

		// typecast to Boolean
		el._descending = !Boolean(el._descending);

		if (tHeadParent.arrow != null) {
			if (tHeadParent.arrow.parentNode != el) {
				tHeadParent.arrow.parentNode._descending = null;	//reset sort order		
			}
			tHeadParent.arrow.parentNode.removeChild(tHeadParent.arrow);
		}

		if (el._descending)
			tHeadParent.arrow = arrowUp.cloneNode(true);
		else
			tHeadParent.arrow = arrowDown.cloneNode(true);

		el.appendChild(tHeadParent.arrow);

			

		// get the index of the td
		var cells = p.cells;
        var cs = p.childNodes;
		var l = cs.length;
        th_idx = 0;
		for (i = 0; i < l; i++) {
			if (cs[i] == el) break;
            if (cs[i].nodeType == 1) {
                if (cs[i].nodeName == "TH") {
                   th_idx++;
                   }
                if (cs[i].nodeName == "TD") {
                   th_idx++;
                }
            }
		}

		var table = getParent(el, "table");
		// can't fail

        var uA = navigator.userAgent;
        var aN = navigator.appName;
        var aV = navigator.vendor;
        var IEbrowser = (uA.indexOf('MSIE') != - 1);

        classes = ""
        if (IEbrowser) {
            classes = el.className
        } else {
        classes = el.getAttribute("class")
        }
        classes = classes ? classes : ""
        classes = classes.split(" ")
        type = null;
        for (var idx = 0; idx < classes.length; idx++) {
           if (classes[idx] == "Number") {
                type = "Number"
           }
           if (classes[idx] == "Date") {
                type = "Date"
           } 
           if (classes[idx] == "CaseInsensitiveString") {
                type = "CaseInsensitiveString"
           }
           //Eddie adding this Jan 22 2007
           if (classes[idx] == "Semester") {
                type = "Semester"
           }
           //End Eddie addition  
        } 
        sortTable(table,th_idx,el._descending, type);
	hilite(table,th_idx);
        // set a cookie with this info
       /*  var cookiename = "sortcolumn_" + cookie_name(table);
        var d = new Date();
        d.setTime(Date.parse('October, 4 2030 07:04:11'));
        setCookie(cookiename,th_idx,d);
        setCookie("sortorder_" + cookie_name(table), el._descending ? "asc"
        : "desc",d);*/
	}
}

function getInnerText(el) {
	if (ie5) return el.innerText;	//Not needed but it is faster
	
	var str = "";
	
	var cs = el.childNodes;
	var l = cs.length;
	for (var i = 0; i < l; i++) {
		switch (cs[i].nodeType) {
			case 1: //ELEMENT_NODE
				str += getInnerText(cs[i]);
				break;
			case 3:	//TEXT_NODE
				str += cs[i].nodeValue;
				break;
		}
		
	}
	
	return str;
}

function getParent(el, pTagName) {
	if (el == null) return null;
	else if (el.nodeType == 1 && el.tagName.toLowerCase() == pTagName.toLowerCase())	
// Gecko bug, supposed to be uppercase
		return el;
	else
		return getParent(el.parentNode, pTagName);
}
