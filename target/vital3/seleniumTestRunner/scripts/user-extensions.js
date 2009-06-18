
// // define a var to be used to ensure unique names for concurrent selenium tests

//uniqueIdString = (new Date()).getTime();

globalStoredVars = new Array();

Selenium.prototype.doSaveStoredVars = function() {
    
    globalStoredVars = storedVars;
    
}

Selenium.prototype.doLoadStoredVars = function() {
    
    storedVars = globalStoredVars;
    
}


// All do* methods on the Selenium prototype are added as actions.
// Eg add a typeRepeated action to Selenium, which types the text twice into a text box.
// The typeTwiceAndWait command will be available automatically
/*Selenium.prototype.doSetUniqueString = function(prefix) {
    
    uniqueIdString = text + (new Date()).getTime();
    
};

Selenium.prototype.doTypeUniqueString = function(locator) {
    // All locator-strategies are automatically handled by "findElement"
    var element = this.page().findElement(locator);
    
    // Create the text to type
    var valueToType = uniqueIdString;
    
    // Replace the element text with the new text
    this.page().replaceText(element, valueToType);
};

*/