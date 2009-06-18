/* 
 *  Window resizer
 *  This script will resize a window, based on the dimensions of one of the images
 *  Variables allow you to determine which image, and any extra padding for other content 
 *  I added a loop so the script does not run before the image is loaded
 *  When calling this function, you should call windowResizer()
 */
function goResizerGoGoResizer(imgno,w,h) {
        if (window.document.images[imgno]) {  
                windowheight = window.document.images[imgno].height + h + 130;
                 if (window.document.images[imgno].width > w) { // This if clause sets the window at least as wide as the header image
                        windowwidth = window.document.images[imgno].width + 100;
                } else {
                        windowwidth = w + 100;
                }
                window.resizeTo(windowwidth,windowheight);		
				//window.moveTo((screen.width-500)/2, (screen.height-300)/2);		
                window.focus();
        } else if (i < 120)  { // Will loop for 1 minute, then fail
                i++;
                window.setTimeout("goResizerGoGoResizer()", 300);
        }
}
function windowResizer(imgno,w,h) {
        i = 0;  // Set loop break
        window.setTimeout("goResizerGoGoResizer(" + imgno + "," + w + "," + h + ")", 300);
}

