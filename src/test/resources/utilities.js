logScenarios = function logScenarios() {
    
    var testsuites = document.getElementsByClassName("testsuite");
    
    var log = "";
    for (var i = 0; i < testsuites.length; i++) {
        var display = getComputedStyle(testsuites[i]).display;
        
        var scenarioName = testsuites[i].getAttribute('data-name');
        
        log += "Scenario: " + scenarioName + ", display: " + display + "\n";
    }
    
    return log;
};

logTests = function logTests() {
    
    var testsuites = document.getElementsByClassName("testcase");
    
    var log = "";
    for (var i = 0; i < testsuites.length; i++) {
        var display = getComputedStyle(testsuites[i]).display;
        
        var scenarioName = testsuites[i].getAttribute('data-name');
        
        log += "Test: " + scenarioName + ", display: " + display + "\n";
    }
    
    return log;
};

