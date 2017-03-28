
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


function showTest(element) {


    var test = element.getAttribute('data-label');
    
    var scenario = getAncestor(element, "testsuite");
    
    var scenarioName = scenario.getAttribute('data-name');
    var scenarioLocation = scenario.getAttribute('data-source');
    
    
    xspecBridge.showTest(test, scenarioName, scenarioLocation);
    
}


function runScenario(currentNode) {

    var scenario = getAncestor(currentNode, "testsuite");
    
    var scenarioName = scenario.getAttribute('data-name');
    var scenarioLocation = scenario.getAttribute('data-source');
    
    xspecBridge.runScenario(scenarioName, scenarioLocation);
    
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


function getAncestor(element, ancestorClass) {
    var node = element;
    while (node != null && node.className != ancestorClass) {
        node = node.parentElement;
    }

    return node;
}
