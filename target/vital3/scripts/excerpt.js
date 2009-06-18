function excerpt(a) {
	var temp = new Array();
	temp = a.href.split("#")[1];
	var ex_box = document.getElementById(temp);
	var ex_box_height = ex_box.style.height;
	var ex_box_flow = ex_box.style.overflow;
	if (!ex_box_height && !ex_box_flow)
	{
		document.getElementById(temp).style.height = 'auto';
		document.getElementById(temp).style.overflow = 'hidden';
		var ex_box = document.getElementById(temp);
		var ex_box_height = ex_box.style.height;
		var ex_box_flow = ex_box.style.overflow;
	}
	
	if ((ex_box_height != 'auto'))
	{
		ex_box.style.height = 'auto';
		ex_box.style.overflow = 'hidden';
		a.className = null;
		a.className = "ex_show";
		if (document.getElementById('ellipsis'+temp)) {
			document.getElementById('ellipsis'+temp).style.display = 'none';
			document.getElementById('ellipsis'+temp).style.visibility = 'hidden';
		}
	}
	
	else
	{
		ex_box.style.height = '1.5em';
		ex_box.style.overflow = 'hidden';
		a.className = null;
		a.className = "ex_hide";
		if (document.getElementById('ellipsis'+temp)) {
			document.getElementById('ellipsis'+temp).style.display = 'inline';
			document.getElementById('ellipsis'+temp).style.visibility = 'visible';
		}
	}
}

function findheight()
{
	var numdiv = getElementsByTagAndClassName("DIV", "noteclip").length;
	for (i=0; i < numdiv; i++) {
		getElementsByTagAndClassName("DIV", "noteclip")[i].style.height = "1.5em";
	}
}

addLoadEvent(findheight);
