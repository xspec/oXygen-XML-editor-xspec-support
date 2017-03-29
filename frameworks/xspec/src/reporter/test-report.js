
function toggleResult(element) {
    
    var test = getAncestor(element, "testcase");
    
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
    
    var test = getAncestor(element, "testcase");
    
    var testName = test.getAttribute('data-name');
    
    var scenario = getAncestor(element, "testsuite");
    
    var scenarioName = scenario.getAttribute('data-name');
    var scenarioLocation = scenario.getAttribute('data-source');
    
    
    xspecBridge.showTest(testName, scenarioName, scenarioLocation);
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

/**
 * Keeps just the failed tests visible. This method is called from the JAVA code, from the action in the view's toolbar.
 */
function showOnlyFailedTests() {
    
    var tcs = document.querySelectorAll(".testcase > p.passed, .testcase > p.skipped");
    for (var i = 0; i < tcs.length; i++) {
        tcs[i].style.display = "none";
    }
    
    var empty = document.querySelectorAll(".testcase, .testsuite");
    for (var i = 0; i < empty.length; i++) {
        var f = empty[i].getElementsByClassName("failed");
        if (f == null || f.length == 0) {
            empty[i].style.display = "none";
        }
    }
}

/**
 * Makes all tests visible. This method is called from the JAVA code, from the action in the view's toolbar.
 */
function showAllTests() {
    var tcs = document.querySelectorAll(".testcase > p, .testcase, .testsuite");
    for (var i = 0; i < tcs.length; i++) {
        tcs[i].style.display = "block";
    }
}