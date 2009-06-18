function expand_all() {
    hs_expand_all();
    document.getElementById("menustat").value = "Collapse all";
    document.getElementById("menustat").onclick = collapse_all;
}

function collapse_all() {
    hs_collapse_all();
    document.getElementById("menustat").value = "Expand all";
    document.getElementById("menustat").onclick = expand_all;
}

function update_menustat_onclick(expander) {
    var func = (expander) ? expand_all : collapse_all;
    document.getElementById("menustat").onclick = func;
}
function menustat_init() {
    var i = 0;  //'*all' should be the only one
    for (a in hs_ids) {
	i++;
    }
    if (i == 1 && hs_ids['*all'] == 'expand') {
	expand_all();
    }
}

addLoadEvent(menustat_init);
