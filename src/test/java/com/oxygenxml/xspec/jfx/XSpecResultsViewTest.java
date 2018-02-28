package com.oxygenxml.xspec.jfx;

import java.awt.BorderLayout;
import java.io.File;
import java.net.URL;

import javax.swing.JFrame;

import com.oxygenxml.xspec.XSpecResultsView;
import com.oxygenxml.xspec.XSpecUtil;

import javafx.application.Platform;

/**
 * Some test cases for the JavaFx renderer.
 *  
 * @author alex_jitianu
 */
public class XSpecResultsViewTest extends XSpecViewTestBase {
  /**
   * XSpec view.
   */
  private XSpecResultsView presenter;
  /**
   * A frame that presents the view.
   */
  private JFrame frame;
  
  @Override
  protected void setUp() throws Exception {
    System.setProperty("prism.order", "sw");
    
    super.setUp();
    
    presenter = new XSpecResultsView(pluginWorkspace);
    
    frame = new JFrame("Test frame");
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    
    frame.setSize(400, 400);
    frame.getContentPane().add(presenter, BorderLayout.CENTER);
    
    frame.setVisible(true);
    
    flushAWT();
  }

  
  @Override
  protected void tearDown() throws Exception {
    super.tearDown();
    
    frame.setVisible(false);
  }

  /**
   * Tests the filtering: all tests are presented or just the failed ones.
   * 
   * Issue https://github.com/xspec/oXygen-XML-editor-xspec-support/issues/5
   * 
   * @throws Exception If it fails.
   */
  public void testFiltering() throws Exception {
    URL xspecURL = getClass().getClassLoader().getResource("escape-for-regex.xspec");
    URL resultsURL = getClass().getClassLoader().getResource("escape-for-regex-report.html");
    
    executeANT(new File(xspecURL.getPath()), new File(resultsURL.getPath()));
    
    initXSpec(xspecURL);
    
    presenter.load(xspecURL, resultsURL);
    flushAWT();
    waitForFX();
    
    loadUtilitiesLibrary(presenter.getEngineForTests());
    
    String execute = execute(presenter.getEngineForTests(), "logScenarios()");
    assertEquals(
        "Scenario: No escaping, display: block\n" + 
        "Scenario: Test simple patterns, display: block\n" + 
        "Scenario: When encountering parentheses, display: block\n" + 
        "Scenario: When encountering a whitespace character class, display: block\n" + 
        "Scenario: When processing a list of phrases, display: block\n" + 
        "", execute);
    
    execute = execute(presenter.getEngineForTests(), "logTests()");
    assertEquals(
        "Test: Must not be escaped at all, display: block\n" + 
        "Test: escape them., display: block\n" + 
        "Test: escape the backslash, display: block\n" + 
        "Test: result should have one more character than source, display: block\n" + 
        "Test: All phrase elements should remain, display: block\n" + 
        "Test: Strings should be escaped and status attributes should be added. The 'status' attribute are not as expected, indicating a problem in the tested template., display: block\n" + 
        "", execute);
    
    
    // Present just the tests that have failed.
    presenter.setFilterTests(true);
    flushAWT();
    waitForFX();    
    execute = execute(presenter.getEngineForTests(), "logScenarios()");
    
    assertEquals(
        "Scenario: No escaping, display: none\n" + 
        "Scenario: Test simple patterns, display: none\n" + 
        "Scenario: When encountering parentheses, display: none\n" + 
        "Scenario: When encountering a whitespace character class, display: none\n" + 
        "Scenario: When processing a list of phrases, display: block\n" + 
        "", execute);
    
    execute = execute(presenter.getEngineForTests(), "logTests()");
    assertEquals(
        "Test: Must not be escaped at all, display: none\n" + 
        "Test: escape them., display: none\n" + 
        "Test: escape the backslash, display: none\n" + 
        "Test: result should have one more character than source, display: none\n" + 
        "Test: All phrase elements should remain, display: none\n" + 
        "Test: Strings should be escaped and status attributes should be added. The 'status' attribute are not as expected, indicating a problem in the tested template., display: block\n" + 
        "", execute);
    
  }


  /**
   * Tests the method that collects the names of the templates that correspond to the failed scenarios.
   * 
   * Issue https://github.com/xspec/oXygen-XML-editor-xspec-support/issues/17
   * 
   * @throws Exception If it fails.
   */
  public void testGetFailedTemplates() throws Exception {
    
    URL xspecURL = getClass().getClassLoader().getResource("runFailed/escape-for-regex.xspec");
    URL resultsURL = getClass().getClassLoader().getResource("runFailed/escape-for-regex-report.html");
    
    executeANT(new File(xspecURL.getPath()), new File(resultsURL.getPath()));
    
    initXSpec(xspecURL);
    
    presenter.load(xspecURL, resultsURL);
    flushAWT();
    waitForFX();
    
    final StringBuilder b = new StringBuilder(); 
    final Exception[] ex = new Exception[1];
    Platform.runLater(new Runnable() {
      @Override
      public void run() {
        try {
          StringBuilder failedTemplateNames = XSpecUtil.getFailedTemplateNames(presenter.getEngineForTests());
          b.append(failedTemplateNames);
        } catch (Exception e) {
          ex[0] = e;
        }
      }
    });
    waitForFX();
    
    if (ex[0] != null) {
      ex[0].printStackTrace(System.out);
    }
    
    assertNull("Unexpected exception:"  + (ex[0] != null ? ex[0].getMessage() : ""), ex[0]);
    
    StringBuilder expected = new StringBuilder();
    expected.append(XSpecUtil.generateId("No escaping bad(0)")).append(" ")
    .append(XSpecUtil.generateId("When processing a list of phrases(2)"));
    assertEquals(expected.toString(), b.toString());
  }

}
