package com.oxygenxml.xspec.jfx.bridge;

import java.awt.Rectangle;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;

import org.apache.log4j.Logger;

import com.oxygenxml.xspec.OperationCanceledException;
import com.oxygenxml.xspec.XSpecResultPresenter;
import com.oxygenxml.xspec.XSpecUtil;
import com.oxygenxml.xspec.XSpecVariablesResolver;
import com.oxygenxml.xspec.protocol.DiffFragmentRepository;

import javafx.scene.web.WebEngine;
import netscape.javascript.JSObject;
import ro.sync.exml.workspace.api.PluginWorkspace;
import ro.sync.exml.workspace.api.PluginWorkspaceProvider;
import ro.sync.exml.workspace.api.editor.WSEditor;
import ro.sync.exml.workspace.api.editor.page.WSEditorPage;
import ro.sync.exml.workspace.api.editor.page.text.xml.WSXMLTextEditorPage;
import ro.sync.exml.workspace.api.editor.page.text.xml.WSXMLTextNodeRange;
import ro.sync.exml.workspace.api.editor.page.text.xml.XPathException;
import ro.sync.exml.workspace.api.editor.transformation.TransformationFeedback;
import ro.sync.exml.workspace.api.standalone.StandalonePluginWorkspace;
import ro.sync.exml.workspace.api.util.XMLUtilAccess;

/**
 * A bridge between JavaScript and Java. JavaScript code will be able to invoke 
 * these methods.
 *  
 * @author alex_jitianu
 */
public class Bridge {
  /**
   * Logger for logging.
   */
  private static final Logger logger = Logger.getLogger(Bridge.class.getName());

  /**
   * The executed XSpec file.
   */
  private URL xspec;
  /**
   * Interface to Oxygen's workspace.
   */
  private PluginWorkspace pluginWorkspace;
  
  /**
   * Extract an interface to pass to the bridge.
   */
  private XSpecResultPresenter resultsPresenter;
  /**
   * Contributes a number of special XSpec variables.
   */
  private XSpecVariablesResolver variablesResolver;

  /**
   * Constructor.
   * 
   * @param pluginWorkspace 
   * @param resultsPresenter 
   * @param variablesResolver 
   * @param xspec The executed XSpec file.
   */
  private Bridge(
      PluginWorkspace pluginWorkspace, 
      XSpecResultPresenter resultsPresenter, 
      XSpecVariablesResolver variablesResolver, 
      URL xspec) {
    this.pluginWorkspace = pluginWorkspace;
    this.resultsPresenter = resultsPresenter;
    this.variablesResolver = variablesResolver;
    this.xspec = xspec;
  }

  /**
   * Installs in the Web Engine the bridge between Javascript and the Java environment.
   * Javascript code will be able to call Java methods.
   * 
   * @param engine Web Engine.
   * @param resultsPresenter Presents the HTML resulted from an XSpec execution.
   * @param variablesResolver Resolves special XSpec variables, like which template to execute.
   * @param pluginWorkspace Oxygen workspace.
   * @param xspec The XSpec for which we present the results.
   * @return
   */
  public static Bridge install(
      WebEngine engine, 
      XSpecResultPresenter resultsPresenter,
      XSpecVariablesResolver variablesResolver,
      PluginWorkspace pluginWorkspace, 
      URL xspec) {
    JSObject window = (JSObject) engine.executeScript("window");
    Bridge value = new Bridge(
        pluginWorkspace, 
        resultsPresenter,
        variablesResolver,
        xspec);
    window.setMember("xspecBridge", value);

    return value;
  }

  /**
   * Invoked from Javascript. Shows an XSpect test (<expect> element).
   * 
   * @param testName Test name.
   * @param scenarioName Scenario name.
   * @param scenarioLocation System id of the file that contains the scenario.
   */
  public void showTest(final String testName, final String scenarioName, final String scenarioLocation) {
    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        showTestAWT(testName, scenarioName, scenarioLocation);
      }
    });
  }

  private void showTestAWT(String testName, String scenarioName, String scenarioLocation) {
    // Just in case the file is no longer opened.
    URL toOpen = xspec;
    try {
      toOpen = new URL(scenarioLocation);
    } catch (MalformedURLException e1) {
      e1.printStackTrace();
    }
    pluginWorkspace.open(toOpen);

    WSEditor editor = pluginWorkspace.getEditorAccess(toOpen, PluginWorkspace.MAIN_EDITING_AREA);

    WSEditorPage currentPage = editor.getCurrentPage();
    if (currentPage instanceof WSXMLTextEditorPage) {
      WSXMLTextEditorPage textpage = (WSXMLTextEditorPage) currentPage;
      String xpath = "//*:expect[@label=\"" + testName
          + "\" or *:label=\"" + testName
          + "\"][parent::*:scenario[@label=\"" + scenarioName
          + "\" or *:label/text()=\"" + scenarioName
          + "\"]]";

      if (logger.isDebugEnabled()) {
        logger.debug("Show test XPath: " + xpath);
      }

      try {
        WSXMLTextNodeRange[] ranges = textpage.findElementsByXPath(xpath);
        if (ranges != null && ranges.length > 0) {

          if (logger.isDebugEnabled()) {
            logger.debug("Got range: " + ranges[0]);
          }

          int start = textpage.getOffsetOfLineStart(ranges[0].getStartLine()) + ranges[0].getStartColumn() - 1;
          int end = textpage.getOffsetOfLineStart(ranges[0].getEndLine()) + ranges[0].getEndColumn() - 1;

          textpage.select(end, start);
          
          JTextArea textComponent = (JTextArea) textpage.getTextComponent();
          Rectangle rStart = textComponent.modelToView(start);
          Rectangle rEnd = textComponent.modelToView(end);
          textComponent.scrollRectToVisible(
              new Rectangle(
                  rStart.x, 
                  rStart.y, 
                  Math.abs(rEnd.x - rStart.x), 
                  Math.max(Math.abs(rEnd.y - rStart.y) + rEnd.height, 3 * rEnd.height)));


        } else {
          logger.warn("Unable to identify test");
        }
      } catch (XPathException e) {
        logger.error(e, e);
      } catch (BadLocationException e) {
        logger.error(e, e);
      }

    }
  }

  /**
   * Invoked from Javascript. Runs an XSpect scenario.
   * 
   * @param scenarioName The name of the scenario.
   * @param scenarioLocation System id of the file that contains the scenario.
   */
  public void runScenario(final String scenarioName, final String scenarioLocation) {
    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        runScenarioAWT(scenarioName, scenarioLocation);
      }
    });
  }
  
  private void runScenarioAWT(String scenarioName, String scenarioLocation) {
    try {
        WSEditor xspecToExecute = getEditorAccess(xspec);
        
        // We only need to execute this scenario.
        variablesResolver.setTemplateNames(scenarioName);
        
        // Step 3. Run the scenario
        XSpecUtil.runScenario(
            xspecToExecute,
            (StandalonePluginWorkspace) pluginWorkspace, 
            resultsPresenter,
            new TransformationFeedback() {
              @Override
              public void transformationStopped() {}
              @Override
              public void transformationFinished(boolean success) {}
            });
    } catch (OperationCanceledException e1) {
      // The user canceled the operation.
    }
  }

  private WSEditor getEditorAccess(URL toOpen) {
    WSEditor e = pluginWorkspace.getEditorAccess(toOpen, PluginWorkspace.MAIN_EDITING_AREA);
    if (e == null) {
      // Just in case the file is no longer opened.
      pluginWorkspace.open(toOpen);

      e = pluginWorkspace.getEditorAccess(toOpen, PluginWorkspace.MAIN_EDITING_AREA);
    }
    return e;
  }

  /**
   * Shows the Oxygen Diff files with the given content.
   *  
   * @param actual Left side content.
   * @param expected Right side content.
   */
  public void showDiff(String actual, String expected) {
    if (logger.isDebugEnabled()) {
      logger.debug("Show diff");

      logger.debug("Left "  + actual.getClass());
      logger.debug("Right "  + expected.getClass());

      logger.debug("Left content " + actual);
      logger.debug("Right content " + expected);
    }
    
    try {
      DiffFragmentRepository instance = DiffFragmentRepository.getInstance();
      
      
      XMLUtilAccess xmlUtilAccess = PluginWorkspaceProvider.getPluginWorkspace().getXMLUtilAccess();
      String lu = xmlUtilAccess.unescapeAttributeValue(actual);
      String lr = xmlUtilAccess.unescapeAttributeValue(expected);
      final URL actualURL = instance.cache(
          lu, 
          "ACTUAL");
      final URL expectedURL = instance.cache(
          lr, 
          "EXPECTED");

      if (logger.isDebugEnabled()) {
        logger.debug("Left URL " + actualURL);
        logger.debug("Right URL " + expectedURL);
      }

      SwingUtilities.invokeLater(new Runnable() {
        @Override
        public void run() {
          ((StandalonePluginWorkspace)pluginWorkspace).openDiffFilesApplication(actualURL, expectedURL);
        }
      });
    } catch (Exception e) {
      logger.error(e, e);
    }
  }

  /**
   * The bridge will not be used anymore. Dispose any resources kept internally.
   */
  public void dispose() {
    DiffFragmentRepository.getInstance().dispose();
  }
}
