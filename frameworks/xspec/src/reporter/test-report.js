
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


function showDiff(test) {
	
  var diffData = null;
  for (var i = 0; i < test.childNodes.length; i++) {
    
    if (test.childNodes[i].className == "embeded.diff.data") {
      diffData = test.childNodes[i];
      break;
    }
  }
    
  
  var left = null;
  var right = null;
  for (var i = 0; i < diffData.childNodes.length; i++) {
    if (diffData.childNodes[i].className == "embeded.diff.result") {
      left = diffData.childNodes[i];
    } else if (diffData.childNodes[i].className == "embeded.diff.expected") {
      right = diffData.childNodes[i];
    }
  }
    
  xspecBridge.showDiff(left.innerHTML, right.innerHTML);
    
}
