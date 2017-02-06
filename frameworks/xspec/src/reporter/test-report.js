
function toggleResult(test) {
	
    var failure = null;
for (var i = 0; i < test.childNodes.length; i++) {
    if (test.childNodes[i].className == "failure") {
      failure = test.childNodes[i];
      break;
    }
}

if (failure != null) {
    if (failure.style.display == "none") {
    	
        failure.style.display = "block";
    } else {
        failure.style.display = "none";
    }
}
    
    
}


function showTest(test) {
    xspecBridge.showTest(test);
    
}


function runScenario(test) {
    
    xspecBridge.runScenario(test);
    
}