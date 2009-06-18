var default_group_by = "date_posted_asc";
var default_show = "all";
var group_by_categories;
var date_show_categories;
var author_show_categories;
var chooser_function_table;
var show_functions;
var midnight = {}
var group_functions;
var group_by_code; // currently selected group_by - either author or date
var DAY_NAMES=new Array('Sun','Mon','Tue','Wed','Thu','Fri','Sat');
var gebtac = getElementsByTagAndClassName;

// Within each group, here are the functions we use to sort the entries.
entry_sort_functions = {
        'author_asc': by_date_asc,
        'author_desc': by_date_asc,
        'date_posted_asc': by_date_asc,
        'date_posted_desc': by_date_desc
}

// Here are the functions we use to sort the groups themselves.
group_sort_functions = {
        'author_asc': by_group_name_asc,
        'author_desc': by_group_name_desc,
        'date_posted_asc': by_group_date_asc,
        'date_posted_desc': by_group_date_desc
}


// ["participant_id", "entry_date", "entry_content", "entry_id", "display", "group"]

// helper functions:
function d(entry) {
    //logDebug (serializeJSON(keys(entry)));
    return  isoTimestamp(entry['entry_date']);
}

function entry_author_name(entry) {
    return names[entry['participant_id']];
}

// FUNCTIONS FOR SORTING ENTRIES
function by_date_desc(a, b) {
    return d(b) - d(a);
}
function by_date_asc(a, b) {
    return d(a) - d(b);
}
function by_name_desc(a, b) {
    return entry_author_name(a) > entry_author_name(b);
}
function by_name_asc(a, b) {
    return entry_author_name(b) < entry_author_name(a);
}


// FUNCTIONS FOR SORTING GROUPS:
function by_group_date_desc(a, b) {
    return midnight[b] - midnight[a];
}
function by_group_date_asc(a, b) {
    return midnight[a] - midnight[b];
}
function by_group_name_asc(a, b) {
    //logDebug ("comparing " + names[b] + " to " + names[a] );
    //logDebug ("Winner: " + ((names[b] < names[a]) ? names[a] : names[b] ));
    if (names[b] == names[a]) return 0;
    return (names[b] < names[a]) ? 1 : -1;
}
function by_group_name_desc(a, b) {
    //logDebug ("comparing " + names[a] + " to " + names[b] );
    //logDebug ("Winner: " + ((names[a] < names[b]) ? names[b] : names[a]));
    if (names[b] == names[a]) return 0;
    return (names[a] < names[b]) ? 1 : -1;
}


function is_today (entry) {
    now = new Date();
    then = d(entry);
    return now.getMonth() == then.getMonth() &&
    now.getDate() ==   then.getDate()
    now.getFullYear() == then.getFullYear();
}

function date_group_string(entry) {
    then = d(entry);
    date_string = (then.getMonth() + 1) + "/" + then.getDate() + "/" + then.getFullYear();
    midnight_of_that_day = new Date (then.getFullYear(), then.getMonth(), then.getDate());
    midnight [date_string] = midnight_of_that_day.getTime();
    return date_string;
}

function author_group_string(entry) {
    return entry.participant_id
}

function is (entry) {
    return true;
}

function is_by (author_participant_id, entry) {
    return entry.participant_id == author_participant_id;
}

// for each entry, apply a chooser function to it and give the entry a new key 'display' with the result of the chooser function. // chooser functions might include: is(), is_today(), is_by(), and so on.
function set_display (entries, chooser_function) {
    //logDebug ("****  chooser function is " + chooser_function);
    map (function (e) {
        e['display'] = chooser_function(e);
    }, entries);
}

function build_show_function_table () {
    show_functions = {
        'all': is,
        'today': is_today
    }
    forEach (keys(names), function (a) {show_functions[a] = partial(is_by, a);});   
}

// if we're sorting by author, put each entry in its author's group. otherwise put each entry
// in its date group.
function build_group_function_table() {
    group_functions = {
        'author_asc': author_group_string,
        'author_desc': author_group_string,
        'date_posted_asc': date_group_string,
        'date_posted_desc': date_group_string
    }
    //  logDebug ("Group functions is now " + serializeJSON(keys(group_functions )));
}

// for each entry, assign it the group in which it will be displayed.
function set_group(entries, chooser_function) {
     map (function (e) {
        e['group'] = chooser_function(e);
    }, entries);
}

function init() {
    //createLoggingPane(true);
    //logDebug (serializeJSON (names));
    //logDebug (names['126790']);
    
    updateNoteList('noteDisplay');
    
    
    // basic form stuff:
    connect('submit_entry', 'onclick', submit_discussion_entry);
    
    if ( typeof( assignment_response_id )== "undefined" || assignment_response_id  == ""  ) {
        removeElement('response_id_field');
    }
    // setup group by and sort:
    group_by_code = default_group_by;
    author_show_categories = ['all'].concat(keys(names));
    build_show_function_table ();
    build_group_function_table();
    set_display (all_discussion_entries, show_functions[default_show]);
    set_group   (all_discussion_entries, group_functions[default_group_by]);
    // display entries:
    repaint_entries();
    // show statistics:
    if (all_discussion_entries.length > 0) {
        $('number_of_entries').innerHTML = all_discussion_entries.length
        $('most_recent_responder').innerHTML = names[all_discussion_entries.sort(by_date_desc)[0]['participant_id']]
    }
    else {
        $('number_of_entries').innerHTML = "None yet.";
        $('most_recent_responder').innerHTML = "n/a.";
    }
    add_names_to_show_menu();
    connect ('responses_to_show_select', 'onchange', show_menu_changed);
    connect ('group_by_select', 'onchange', group_by_menu_changed);
    hideElement($('fake_group_div'))
}

function makeOption(id, value, text) {
	return OPTION({"value": value, "id": id}, text);
}

function add_names_to_show_menu() {     
	map ( function(p) {
		appendChildNodes ($('responses_to_show_select'), makeOption (p, p, "Responses by " + names[p]));
	},
    keys(names));
}

function group_by_menu_changed(e) {
    group_by_code = e.target().options[e.target().selectedIndex].id
    //logDebug ("Group by code is now " + group_by_code);
    set_group (all_discussion_entries, group_functions[group_by_code])
    repaint_entries();
}

function show_menu_changed(e) {
    show_code = e.target().options[e.target().selectedIndex].id
    set_display(all_discussion_entries, show_functions[show_code]);
    repaint_entries();
}

function repaint_entries() {
	the_options =  $('group_by_select').options;
	for (i=0; i< the_options.length; i++) {
		 if ( the_options[i].id == group_by_code ) {
			the_options[i].selected = true;
	     }	
	}
    // this didn't work in i.e., arrgh:
    // $('group_by_select').options[group_by_code].selected = true;
    $('discussion_entries').innerHTML = "";
    all_groups = map(itemgetter('group'),all_discussion_entries);
    unique_groups = []
    forEach (all_groups, function (d) { 
            if (findValue(unique_groups, d) == -1) {unique_groups.push(d)}
        }
    );
    //logDebug ("Sort function for groups is:");
    //logDebug (group_sort_functions[group_by_code]);
    //logDebug ("before sort: " + serializeJSON(unique_groups));
    unique_groups.sort(group_sort_functions[group_by_code]);
    //logDebug ("after sort:  " + serializeJSON(unique_groups));
    showElement($('fake_group_div'));
    $('discussion_entries').innerHTML = '';
    forEach (unique_groups, function (e) {        
            entries_for_this_group = filter ( 
                function(f) { return f.group == e && f.display; },             
                all_discussion_entries
            );
            if (entries_for_this_group.length != 0) {
            
                //logDebug ("Sort function for entries within each group is:");
                //logDebug (entry_sort_functions[group_by_code]);
                entries_for_this_group.sort(entry_sort_functions[group_by_code]);
                paint_group (e, entries_for_this_group);
            }
        }
    )
    hideElement($('fake_group_div'));
}


function replace_inner_span (parent, child_class, content) {
    gebtac('span', child_class, parent)[0].innerHTML = content;
}
function replace_inner_input (parent, child_class, content) {
    gebtac('input', child_class, parent)[0].value = content;
}

function paint_group (group_code, entries) {
    sort_is_author =  (group_by_code == 'author_asc' || group_by_code == 'author_desc');
    new_group_div = $('fake_group_div').cloneNode(true);
    $('discussion_entries').appendChild(new_group_div);
    group_label = sort_is_author ? names[group_code] : group_code;
    replace_inner_span (new_group_div, 'fake_group_title', group_label);
    new_group_div.id = 'group_div_' + group_code;
    model_entry_div = gebtac('div', 'fake_entry_div', new_group_div)[0];
    forEach (entries,
        function (entry) {
            new_entry_div = model_entry_div.cloneNode(true);
            new_group_div.appendChild(new_entry_div);
            
            // You can only delete your own entries:
            // if the entry's author is not the CLIU, hide the trash icon.
            trash_icon = gebtac('input', 'delete_entry_class', new_entry_div)[0]
            if (participant_id != entry.participant_id) {
                hideElement (trash_icon);
            }
            
            
            fill_span = partial (replace_inner_span, new_entry_div);
            if (sort_is_author) {
                // don't fill in the author and suppress the word "on"
                fill_span ('fake_entry_name', '');
                fill_span ('on_span', '');
            }
            else {
                fill_span ('fake_entry_name', names[entry['participant_id']]);
            }
            fill_span ('fake_entry_date', print_date(isoTimestamp(entry['entry_date'])));
            fill_span ('fake_entry_text',  unescape(entry['entry_content']));     
            fill_input = partial (replace_inner_input, new_entry_div);          
            fill_input('commentId', entry['entry_id']);
            fill_input('participantId', entry['participant_id']);
            fill_input('assignmentId',  assignment_id);
            fill_input('assignmentResponseId',  assignment_response_id);
        }
    );
    hideElement(model_entry_div);
}

function print_date (d) {
    return DAY_NAMES[d.getDay()] + ", " +
    (d.getMonth() + 1)  + '/' +
    d.getDate() + '/' +
    d.getFullYear() +
    ", " + d.getHours()+ 
    ":" +  padFront(d.getMinutes(), 2);
}

function padFront(s,n){
    s = "" + s; // convert to string
    while(s.length < n) { s = '0' + s; }
    return s;
}

function submit_discussion_entry() {
    $('text').value = tinyMCE.getContent();
    $('enter_discussion_entry').submit();
}

addLoadEvent(init);
