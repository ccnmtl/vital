function excerpt(a) {
	var temp = new Array();
	temp = a.href.split("#")[1];
	var ex_box = document.getElementById(temp);
	var ex_box_height = ex_box.style.height;
	var ex_box_flow = ex_box.style.overflow;
	if ((ex_box_height != 'auto'))
	{
		ex_box.style.height = 'auto';
		ex_box.style.overflow = 'hidden';
		a.className = null;
		a.className = "ex_show";
	}
	
	else
	{
		ex_box.style.height = '1.4em';
		ex_box.style.overflow = 'hidden';
		a.className = null;
		a.className = "ex_hide";
	}
}
