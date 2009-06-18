var roundedCornersOnLoad = function () {
    swapDOM("visual_version", SPAN(null, MochiKit.Visual.VERSION));
    roundClass("p", 'message');
};
addLoadEvent(roundedCornersOnLoad);


