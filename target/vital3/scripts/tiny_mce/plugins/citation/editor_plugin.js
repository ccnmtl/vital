var TinyMCE_CitationPlugin = {
	/**
	 * Returns information about the plugin as a name/value array.
	 * The current keys are longname, author, authorurl, infourl and version.
	 *
	 * @returns Name/value array containing information about the plugin.
	 * @type Array 
	 */
	getInfo : function() {
		return {
			longname : 'Citation Plugin',
			author : 'Schuyler Duveen',
			authorurl : 'http://ccnmtl.columbia.edu',
			infourl : 'http://www.yoursite.com/docs/template.html',
			version : "1.0"
		};
	},

	/**
	 * Gets executed when a TinyMCE editor instance is initialized.
	 *
	 * @param {TinyMCE_Control} Initialized TinyMCE editor control instance. 
	 */
	// This function doesn't get called in time to use it in Firefox.
	initInstance : function(inst) {
		// You can take out plugin specific parameters
		//log("running citation initInstance");
		// Register custom keyboard shortcut
		//inst.addToLang('',{citation_desc : 'Add Citation (Ctrl+Y)'});
		//inst.addShortcut('ctrl', ';', 'lang_citation_desc', 'mceAddCitation');
	},

	/**
	 * Returns the HTML code for a specific control or empty string if this plugin doesn't have that control.
	 * A control can be a button, select list or any other HTML item to present in the TinyMCE user interface.
	 * The variable {$editor_id} will be replaced with the current editor instance id and {$pluginurl} will be replaced
	 * with the URL of the plugin. Language variables such as {$lang_somekey} will also be replaced with contents from
	 * the language packs.
	 *
	 * @param {string} cn Editor control/button name to get HTML for.
	 * @return HTML code for a specific control or empty string.
	 * @type string
	 */
	/*
	getControlHTML : function(cn) {
		switch (cn) {
			case "CitationControl":
				return tinyMCE.getButtonHTML(cn, 'lang_add_citation_desc', '{$pluginurl}/images/someimage.gif', 'mceAddCitation');
		}

		return "";
	},
	*/
	/**
	 * Executes a specific command, this function handles plugin commands.
	 *
	 * @param {string} editor_id TinyMCE editor instance id that issued the command.
	 * @param {HTMLElement} element Body or root element for the editor instance.
	 * @param {string} command Command name to be executed.
	 * @param {string} user_interface True/false if a user interface should be presented.
	 * @param {mixed} value Custom value argument, can be anything.
	 * @return true/false if the command was executed by this plugin or not.
	 * @type
	 */
	/*
	execCommand : function(editor_id, element, command, user_interface, value) {
		// Handle commands
		switch (command) {
			// Remember to have the "mce" prefix for commands so they don't intersect with built in ones in the browser.
			case "mceAddCitation":
				// Do your custom command logic here.
				log('hi from mceAddCitation');
				selectedNote=DIV(null,'howdy thar partner');
				log(selectedNote);
				if (selectedNote) {
					tinyMCE.execCommand("mceInsertContent",false,selectedNote.innerHTML);
				}
				else {
					alert('Please select the note you wish to insert by clicking on its title.');
				}
				return true;
		}

		// Pass to next handler in chain
		return false;
	},
	*/
	
	/**
	 * Gets called when a TinyMCE editor instance gets filled with content on startup.
	 *
	 * @param {string} editor_id TinyMCE editor instance id that was filled with content.
	 * @param {HTMLElement} body HTML body element of editor instance.
	 * @param {HTMLDocument} doc HTML document instance.
	 */
	/** This function doesn't get called in time in Firefox
	setupContent : function(editor_id, body, doc) {
		log('setupContent');
	},
	**/
	/**
	 * Gets called ones the cursor/selection in a TinyMCE instance changes. This is useful to enable/disable
	 * button controls depending on where the user are and what they have selected. This method gets executed
	 * alot and should be as performance tuned as possible.
	 *
	 * @param {string} editor_id TinyMCE editor instance id that was changed.
	 * @param {HTMLNode} node Current node location, where the cursor is in the DOM tree.
	 * @param {int} undo_index The current undo index, if this is -1 custom undo/redo is disabled.
	 * @param {int} undo_levels The current undo levels, if this is -1 custom undo/redo is disabled.
	 * @param {boolean} visual_aid Is visual aids enabled/disabled ex: dotted lines on tables.
	 * @param {boolean} any_selection Is there any selection at all or is there only a cursor.
	 */
	/*
	handleNodeChange : function(editor_id, node, undo_index, undo_levels, visual_aid, any_selection) {
	    logDebug('handleNodeChange', [node]);
	    if (!any_selection && node.tagName.toLowerCase() === 'input' && node.className === 'materialCitation')
	    logDebug('handleNodeChange', [node]);
	},
	*/
	/**
	 * Gets called when the contents of a TinyMCE area is modified, in other words when a undo level is
	 * added.
	 *
	 * @param {TinyMCE_Control} inst TinyMCE editor area control instance that got modified.
	 */
	onChange : function(inst) {
		var dok=inst.getDoc();

		if (typeof(wordCount) == 'function') {
		    wordCount();//window.setTimeout(wordCount,0);
		}
		var triggerChange = false;
		var klass='materialCitation';
		var citations=getElementsByTagAndClassName(null,klass, dok);
		var newCitation;
		for (i=0; i<citations.length; i++) {
		    var c=citations[i];
		    switch (c.tagName) {
		    case 'IMG':
			var linkName=c.getAttribute("name");
			var linkTitle=c.getAttribute("title");
			
			//removing extraneous 0's in the timecode
			linkTitle=linkTitle.replace(/([ -])0:/g,"$1");
			linkTitle=linkTitle.replace(/([ -])0/g,"$1");
			
			//temporarily swap which document MochiKit uses for DOM manipulation
			//This is necessary, because the A tag must be created with dok.createElement()
			var mochi_doc = MochiKit.DOM._document;
			MochiKit.DOM._document = dok;
			newCitation = SPAN();//null, '&#160;', INPUT({'type':'button','class':klass,'value':linkTitle}),'&#160;');
			MochiKit.DOM._document = mochi_doc;
			
			//don't understand why 'onclick' can't be set with Mochi, but it can't
			//newCitation.childNodes[1].setAttribute('onclick',"openCitation('"+linkName+"')");
			newCitation.innerHTML = '&#160;<input type="button" class="'+klass+'" value="'+linkTitle+'" onclick="openCitation(\''+linkName+'\')" />&#160;';
			swapDOM(c,newCitation);

			//failed alternative method
			//tinyMCE.execCommand("mceSelectNode",false,c);
			//tinyMCE.execCommand("mceReplaceContent",false,newCitation);
			triggerChange = true;
			break;
		    case 'INPUT':
			/*This is for cleaning up, or rather, DE-cleaning up the spaces 
			  around the input element which protect it from weird deletion.
			  Basically, tinyMCE cleans up spaces around the INPUT element,
			  but without a non-breaking space on each side, INPUT is subject to
			  some weird DOM deletions, or copying the value as text outside.
			 */
			//logDebug('nextsibling',typeof(c.nextSibling));
			if (typeof(c.nextSibling) == 'object') {
			    if (c.nextSibling == null) {
				//logDebug('  next  null');
			    } else if (c.nextSibling.nodeType == 3) {
				var x = c.nextSibling.textContent;
				//logDebug('x'+c.nextSibling.data+'x',c.nextSibling.textContent.length);
				if (x == '' || x == ' ') {
				    //logDebug('  next space');
				    c.nextSibling.nodeValue= '\xa0'; //nbsp
				}
			    }
			}
			if (typeof(c.previousSibling) == 'object') {
			    if (c.previousSibling == null) {
				//logDebug('  previous  null');
				var p = c.parentNode;
				p.insertBefore(dok.createTextNode('\xa0'),c);
			    } else if (c.previousSibling.nodeType == 3) {
				var x = c.previousSibling.textContent;
				//logDebug('x'+c.previousSibling.data+'x',c.previousSibling.textContent.length);
				if (x == '' || x == ' ') {
				    //logDebug('  previous space');
				    c.previousSibling.nodeValue= '\xa0'; //nbsp
				}
			    }
			}
			break;
		    }
		    /*logDebug([c,c.tagName]);
		    try {logDebug(c.parentNode.innerHTML);
		    } catch(e) {logDebug(dok.innerHTML);
		    } //*/
		}
		if (triggerChange) {
		    tinyMCE.triggerNodeChange();
		}
	}

	// PUT COMMA HERE IF YOU ENABLE ANY FUNCTIONS BELOW!!!!!!!!

	/**
	 * Gets called when TinyMCE handles events such as keydown, mousedown etc. TinyMCE
	 * doesn't listen on all types of events so custom event handling may be required for
	 * some purposes.
	 *
	 * @param {Event} e HTML editor event reference.
	 * @return true - pass to next handler in chain, false - stop chain execution
	 * @type boolean
	 */
	/**
	handleEvent : function(e) {
		log(e.type);
		return true;
	},
	**/
	/**
	 * Gets called when HTML contents is inserted or retrived from a TinyMCE editor instance.
	 * The type parameter contains what type of event that was performed and what format the content is in.
	 * Possible valuses for type is get_from_editor, insert_to_editor, get_from_editor_dom, insert_to_editor_dom.
	 *
	 * @param {string} type Cleanup event type.
	 * @param {mixed} content Editor contents that gets inserted/extracted can be a string or DOM element.
	 * @param {TinyMCE_Control} inst TinyMCE editor instance control that performes the cleanup.
	 * @return New content or the input content depending on action.
	 * @type string
	 */
	/*
	,cleanup : function(type, content, inst) {
	    logDebug(type,[content]);
	    return content;
	}//*/

};
tinyMCE.addPlugin("citation", TinyMCE_CitationPlugin); 
