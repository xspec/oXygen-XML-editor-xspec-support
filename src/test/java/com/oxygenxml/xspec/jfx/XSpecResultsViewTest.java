package com.oxygenxml.xspec.jfx;

import java.awt.BorderLayout;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.URL;
import java.util.concurrent.Semaphore;

import javax.swing.JFrame;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.mockito.Mockito;
import org.w3c.dom.Node;

import com.oxygenxml.xspec.XSpecResultsView;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.concurrent.Worker.State;
import javafx.scene.web.WebEngine;
import junit.extensions.jfcunit.JFCTestCase;
import ro.sync.exml.workspace.api.standalone.StandalonePluginWorkspace;

/**
 * Some test cases for the JavaFx renderer.
 *  
 * @author alex_jitianu
 */
public class XSpecResultsViewTest extends JFCTestCase {
  /**
   * The plugin workspace used by the XSpec view.
   */
  private StandalonePluginWorkspace pluginWorkspace;
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
    super.setUp();
    
    pluginWorkspace = Mockito.mock(StandalonePluginWorkspace.class);
    
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
    URL resultsURL = getClass().getClassLoader().getResource("escape-for-regex-result.html");
    
    presenter.load(xspecURL, resultsURL);
    flushAWT();
    waitForFX();
    
    loadUtilitiesLibrary();
    
    String execute = execute("logScenarios()");
    assertEquals(
        "Scenario: No escaping, display: block\n" + 
        "Scenario: Test simple patterns, display: block\n" + 
        "Scenario: When encountering parentheses, display: block\n" + 
        "Scenario: When encountering a whitespace character class, display: block\n" + 
        "Scenario: When processing a list of phrases, display: block\n" + 
        "", execute);
    
    execute = execute("logTests()");
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
    
    execute = execute("logScenarios()");
    
    assertEquals(
        "Scenario: No escaping, display: none\n" + 
        "Scenario: Test simple patterns, display: none\n" + 
        "Scenario: When encountering parentheses, display: none\n" + 
        "Scenario: When encountering a whitespace character class, display: none\n" + 
        "Scenario: When processing a list of phrases, display: block\n" + 
        "", execute);
    
    execute = execute("logTests()");
    assertEquals(
        "Test: Must not be escaped at all, display: none\n" + 
        "Test: escape them., display: none\n" + 
        "Test: escape the backslash, display: none\n" + 
        "Test: result should have one more character than source, display: none\n" + 
        "Test: All phrase elements should remain, display: none\n" + 
        "Test: Strings should be escaped and status attributes should be added. The 'status' attribute are not as expected, indicating a problem in the tested template., display: block\n" + 
        "", execute);
    
  }
  
  private String execute(final String script) throws InterruptedException {
    final WebEngine webEngine = presenter.getEngineForTests();
    final String[] toReturn = new String[1];
    
    final Semaphore s = new Semaphore(0);
    Platform.runLater(new Runnable() {
      @Override
      public void run() {
        try {
          toReturn[0] = webEngine.executeScript(script).toString();
        } catch (Exception e) {
          e.printStackTrace();
        }
        // It means that all other events scheduled on the FX thread are done.
        s.release();
      }
    });
    s.acquire();
    
    return toReturn[0];
  }
  
  /**
   * Loads a utilities Javascript library into the XSpec view.
   * 
   * @throws InterruptedException If it fails.
   */
  private void loadUtilitiesLibrary() throws InterruptedException {
    final URL utilitiesJS = getClass().getClassLoader().getResource("utilities.js");
    
    final WebEngine webEngine = presenter.getEngineForTests();
    
    invokeAndWaitOnFX(new Runnable() {
      @Override
      public void run() {
        try {
          webEngine.executeScript(read(utilitiesJS).toString());
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    });
  }

  /**
   * Waits until the FX thread has consumed all scheduled events.
   * 
   * @throws InterruptedException
   */
  private void waitForFX() throws InterruptedException {
    // Invoke and wait to ensure all other events are executed.
    invokeAndWaitOnFX(new Runnable() {
      @Override
      public void run() {}
    });
  }
  
  /**
   * Executes the given runnable on the FX thread and waits untilthe code was executed.
   * 
   * @param r Code to execute.
   * 
   * @throws InterruptedException If it fails.
   */
  private void invokeAndWaitOnFX(final Runnable r) throws InterruptedException {
    final Semaphore s = new Semaphore(0);
    Platform.runLater(new Runnable() {
      @Override
      public void run() {
        r.run();
        
        s.release();
      }
    });
    
    s.acquire();
  }
  
  /**
   * Waits until the Web engine has successfully loaded the page.
   * 
   * @param engine Web engine.
   * @throws InterruptedException
   */
  private void waitForEngine(final WebEngine engine) throws InterruptedException {
    final Semaphore s = new Semaphore(0);
    // Make the test on the FX thread, the same thread on which the state is updated.
    Platform.runLater(new Runnable() {
      @Override
      public void run() {
        // It means that all other events scheduled on the FX thread are done.
        if (engine.getLoadWorker().getState() == Worker.State.SUCCEEDED) {
          // Already loaded. Release the semaphore.
          s.release();
        } else {
          // Not yet loaded. Wait and release the semaphore when the state changes.
          engine.getLoadWorker().stateProperty().addListener(
              new ChangeListener<State>() {
                @Override public void changed(ObservableValue ov, State oldState, State newState) {
                  if (newState == Worker.State.SUCCEEDED) {
                    s.release();
                  }
                }
              });
        }
      }
    });
    s.acquire();
  }
  
  public static String getInnerHTML(Node node) throws TransformerConfigurationException, TransformerException {
      StringWriter sw = new StringWriter();
      Result result = new StreamResult(sw);
      TransformerFactory factory = new net.sf.saxon.TransformerFactoryImpl();
      Transformer proc = factory.newTransformer();
      proc.setOutputProperty(OutputKeys.METHOD, "html");
      for (int i = 0; i < node.getChildNodes().getLength(); i++)
      {
          proc.transform(new DOMSource(node.getChildNodes().item(i)), result);
      }
      return sw.toString();
  }
  
  /**
   * Reads the content of a file.
   * 
   * @param url File to read.
   * 
   * @return The content of the file.
   * 
   * @throws Exception If it fails.
   */
  private StringBuilder read(URL url) throws Exception {
    StringBuilder b = new StringBuilder();

    BufferedReader r = new BufferedReader(new InputStreamReader(url.openStream(), "UTF-8"));
    try {
      String l = null;
      while ((l = r.readLine()) != null) {
        if (b.length() > 0) {
          b.append("\n");
        }
        
        b.append(l);
      }
    } finally {
      r.close();
    }

    return b;
  }
}
