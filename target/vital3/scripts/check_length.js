function limitText(ev, limitNum, limitCount) {
    //logDebug ('a');
    logDebug ($('comment_form_textarea').value.length );
    var limitField = ev.src();
	if (( limitField.value.length)> limitNum) {
		limitField.value = limitField.value.substring(0, limitNum);
	} else {
		limitCount.value = limitNum - (limitField.value.length );
	}
}

var max_comment_length = 3500;

function limit_text_comment_form (ev) {
    limitText (ev, max_comment_length,  $('limit_count'));
}


function setup_comment_limit() {
    if ($('comment_form_textarea')) {
        $('limit_count').value = max_comment_length - $('comment_form_textarea').value.length;
        connect('comment_form_textarea', 'onkeyup', $('comment_form_textarea'), limit_text_comment_form);         
        
    }
}

addLoadEvent (setup_comment_limit);
