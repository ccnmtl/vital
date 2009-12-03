

// Duplicate of function in notetaking.js

// TODO: break it out into its own escaping file
// and subsequently include it in all places that also include notetaking.js
// right before notetaking.js.

function unescape_from_xml(s) {
    s = s.replace(/&apos;/g,"'");
    s = s.replace(/&amp;/g,"&");
    s = s.replace(/&quote;/g,'"');
    s = s.replace(/&lt;/g,"<");
    s = s.replace(/&gt;/g,">");
    return s;
}


function escape_titles() {
    //logDebug ("ok, now escaping those titles.");
    escaped_titles = getElementsByTagAndClassName('div', 'escaped_titles');
    forEach (escaped_titles,  function (a) { 
        a.innerHTML = unescape_from_xml(unescape_from_xml(a.innerHTML));
    });    
}
