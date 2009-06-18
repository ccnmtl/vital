
/*
	var mce_char_limit_settings = {
	    max_chars: 20
	    ,field_name: 'comment_text'
	    , form_name: 'enter_discussion_entry'
	    , feedback_div : 'response_tinymce_textarea_div_title'
	};
*/
if (typeof (mce_char_limit_settings == 'object')) {
    
    var check_length_callbacks =
    {
            handle_event_callback : "myHandleEvent",
            onchange_callback : "myCustomOnChangeHandler",
            oninit : "myCustomOnInit"
    }
    
    var text1_count=0;//init int
    var text1_undobuffer="";//init string
   
   // get values from settings in template:
    st = mce_char_limit_settings; 
    var text1_maxchars= st.max_chars;// max plain text chars allowed
    var tinymce_field_name = st.field_name ;
    var tinymce_form_name = st.form_name;
    var tinymce_char_count_message_display_id = st.feedback_div;
    
    

    //init when unit TinyMCE editor is loaded
    function myCustomOnInit(){
        tinyMCE_timer1=setTimeout("countchars(tinymce_form_name, tinymce_field_name ,text1_maxchars,tinymce_char_count_message_display_id)",1000); //wait a bit to avoid IE error
    }

    //event when something in TinyMCE changes (which is when an undo level is added. e.g. paste)
    function myCustomOnChangeHandler(inst){
        tinyMCE_timer1=setTimeout("countchars(tinymce_form_name, tinymce_field_name ,text1_maxchars,tinymce_char_count_message_display_id)",250); //slight delay before counting
    }

    //event such as key or mouse press
    function myHandleEvent(e){
        //window.status = "event:" + e.type;
        if(e.type=="keyup"){
            clearTimeout(tinyMCE_timer1);
            countchars(tinymce_form_name, tinymce_field_name ,text1_maxchars,tinymce_char_count_message_display_id)
        }
        return true;
    }

    function getHTML_TinyMCE(txtfield){
        obj=document.getElementById('mce_editor_0');
        if(obj.contentDocument){
            content=obj.contentDocument.body.innerHTML; //FireFox (getContent() messes up cursor position)
        }else{
            content=tinyMCE.getContent(txtfield); //IE
        }
        return content;
    }

    function stripTags_TinyMCE(txtfield) { //strips html tags leaving plain text
        content=getHTML_TinyMCE(txtfield);
        var re = /(<([^>]+)>)/ig ; //strip all tags
        plaintext = content.replace(re, "");
        return plaintext;
    }

    function countchars_TinyMCE(txtfield){
        // CUSTOMIZING on March 2 2009:
        // we're actually interested in the length of the raw HTML:
        
        // ORIGINAL VERSION:
        // content=stripTags_TinyMCE(txtfield);
        
        // NEW VERSION:
        //content = getHTML_TinyMCE(txtfield);
        
        // NEW VERSION 2:
        content = tinyMCE.getContent();
        
        cnt=content.length;
        //alert(cnt);
        return cnt;
    }

    function countchars(formname,txtfield,maxchars,displayID){
        //editor_id=tinyMCE.selectedInstance.formTargetElementId; // get ID of focused TinyMCE editor
        thefield=eval("document."+formname+"."+txtfield);
        if(isNaN(maxchars)){ //arg could contain a var name rather than a number
            maxchars=eval(maxchars); //convert var to number
        }
        currCount=countchars_TinyMCE(txtfield); // current length of TinyMCE field
        remainCount=maxchars-currCount+1; //fix: allow +1
        tmpvar=txtfield+'_count';   
        eval(tmpvar + '=currCount'); //assign dynamic var to char count
        displayObj=document.getElementById(displayID);
        displayObj.innerHTML="You have " + eval(remainCount-1) + " characters remaining"; //-1 to counteract above +1
        if(remainCount<=0){ //typed too much
            if(text1_undobuffer.length > 0){
                tinyMCE_timer2=setTimeout("tinyMCE.setContent(text1_undobuffer)",250); // undo event - replace content with most recent buffer
            };
            displayObj.innerHTML="<B>You have typed the maximum text allowed</B>";
        } else {
            text1_undobuffer=getHTML_TinyMCE(txtfield); //store content in buffer
        }
    }

}
