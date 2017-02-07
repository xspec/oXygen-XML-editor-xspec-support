package com.oxygenxml.xspec.jfx.bridge;

import java.lang.reflect.InvocationTargetException;
import java.net.URL;

import javafx.scene.web.WebEngine;

import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;

import netscape.javascript.JSObject;

import org.apache.log4j.Logger;

import ro.sync.exml.workspace.api.PluginWorkspace;
import ro.sync.exml.workspace.api.editor.WSEditor;
import ro.sync.exml.workspace.api.editor.page.WSEditorPage;
import ro.sync.exml.workspace.api.editor.page.text.actions.TextActionsProvider;
import ro.sync.exml.workspace.api.editor.page.text.xml.WSXMLTextEditorPage;
import ro.sync.exml.workspace.api.editor.page.text.xml.WSXMLTextNodeRange;
import ro.sync.exml.workspace.api.editor.page.text.xml.XPathException;
import ro.sync.exml.workspace.api.editor.transformation.TransformationFeedback;
import ro.sync.exml.workspace.api.standalone.StandalonePluginWorkspace;

import com.oxygenxml.xspec.XSpecResultsPresenter;
import com.oxygenxml.xspec.XSpecUtil;

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
   * Form control editing context.
   */
  private URL xspec;

  private PluginWorkspace pluginWorkspace;

  private XSpecResultsPresenter resultsPresenter;

  /**
   * Constructor.
   * @param pluginWorkspace 
   * @param resultsPresenter2 
   * 
   * @param context Form control editing context.
   */
  private Bridge(PluginWorkspace pluginWorkspace, XSpecResultsPresenter resultsPresenter, URL xspec) {
    this.pluginWorkspace = pluginWorkspace;
    this.resultsPresenter = resultsPresenter;
    this.xspec = xspec;
  }

  public static Bridge install(WebEngine engine, XSpecResultsPresenter resultsPresenter, PluginWorkspace pluginWorkspace, URL xspec) {
    JSObject window = (JSObject) engine.executeScript("window");
    Bridge value = new Bridge(pluginWorkspace, resultsPresenter, xspec);
    window.setMember("xspecBridge", value);

    return value;
  }

  /**
   * Invoked from Javascript. Shows an XSpect test (<expect> element).
   * 
   * @param testName Test name.
   */
  public void showTest(final String testName) {
    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        showTestAWT(testName);
      }
    });
  }

  private void showTestAWT(String testName) {
    // Just in case the file is no longer opened.
    pluginWorkspace.open(xspec);

    WSEditor editor = pluginWorkspace.getEditorAccess(xspec, PluginWorkspace.MAIN_EDITING_AREA);

    WSEditorPage currentPage = editor.getCurrentPage();
    if (currentPage instanceof WSXMLTextEditorPage) {
      WSXMLTextEditorPage textpage = (WSXMLTextEditorPage) currentPage;
      String xpath = "//*:expect[@label=\"" + testName
          + "\" or *:label=\"" + testName
          + "\"]";
      
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
          int end = textpage.getOffsetOfLineEnd(ranges[0].getStartLine()) - 1;

          textpage.select(start, end);


        }
      } catch (XPathException e) {
        e.printStackTrace();
      } catch (BadLocationException e) {
        e.printStackTrace();
      }

    }
  }

  /**
   * Invoked from Javascript. Runs an XSpect scenario.
   * 
   * @param scenarioName The name of the scenario.
   */
  public void runScenario(final String scenarioName) {
    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        runScenarioAWT(scenarioName);
      }
    });
  }

  private void runScenarioAWT(String testName) {
    // Just in case the file is no longer opened.
    pluginWorkspace.open(xspec);

    final WSEditor editor = pluginWorkspace.getEditorAccess(xspec, PluginWorkspace.MAIN_EDITING_AREA);

    WSEditorPage currentPage = editor.getCurrentPage();
    if (currentPage instanceof WSXMLTextEditorPage) {

      // Step 1. Locate the scenario.
      final WSXMLTextEditorPage textpage = (WSXMLTextEditorPage) currentPage;
      String xpath = "//*:scenario[@label=\"" + testName
          + "\" or *:label=\"" + testName
          + "\"]";

      if (logger.isDebugEnabled()) {
        logger.debug("Xpath " + xpath);
      }
      try {
        WSXMLTextNodeRange[] ranges = textpage.findElementsByXPath(xpath);
        if (logger.isDebugEnabled()) {
          logger.debug("ranges " + ranges);
        }
        if (ranges != null && ranges.length > 0) {
          int start = textpage.getOffsetOfLineStart(ranges[0].getStartLine()) + ranges[0].getStartColumn();

          // Step 2. Put a @focus on the scenario
          textpage.setCaretPosition(start);

          JTextArea textComponent = (JTextArea) textpage.getTextComponent();
          textpage.setCaretPosition(start);
          WSXMLTextNodeRange[] focusAttr = textpage.findElementsByXPath("@focus");
          if (focusAttr != null && focusAttr.length > 0) {
            // TODO The presence of the @focus will run the scenario. But we should identify and remove all
            // others @focus.

            //            int startAttr = textpage.getOffsetOfLineStart(focusAttr[0].getStartLine()) + focusAttr[0].getStartColumn() - 1;
            //            int endAttr = textpage.getOffsetOfLineStart(focusAttr[0].getEndLine()) + focusAttr[0].getEndColumn() - 1;
            //            textComponent.replaceRange("focus=\"true\"", startAttr, endAttr);

          } else {
            // Look for a place where to put @focus
            String text = textComponent.getText(start, Math.min(20, textComponent.getDocument().getLength() - start));
            if (logger.isDebugEnabled()) {
              logger.debug("Read: " + text);
            }

            // We've read something like: <x:scenario labe="... 
            // or something like: <x:scenario>
            int position = 0;
            char ch = 0;
            while (position < text.length() && ch != ' ' && ch != '>') {
              ch = text.charAt(position);
              position ++;
            };

            if (position < text.length()) {
              // we have an insert location.
              textComponent.insert(" focus=\"true\"", start + position - 1);
            }
          }

          // Save the change. We will start an ANT transformation so we need the 
          // file to be saved.
          // TODO Perhaps it would be best to run a temporary copy, one that
          // we can modify as we see fit!
          editor.save();

          // Step 3. Run the scenario
          XSpecUtil.runScenario(
              (StandalonePluginWorkspace) pluginWorkspace, 
              resultsPresenter,
              new TransformationFeedback() {
                @Override
                public void transformationStopped() {}
                @Override
                public void transformationFinished(boolean success) {
                  try {
                    SwingUtilities.invokeAndWait(new Runnable() {
                      @Override
                      public void run() {
                        // Step 4. Undo the change.

                        // TODO The transformation is performed on a thread. The user 
                        // can touch the editor during this time. An UNDO now can't guarantee 
                        // the fact that we will UNDO the @focus attribute set above.
                        TextActionsProvider actionsProvider = textpage.getActionsProvider();
                        Object object = actionsProvider.getTextActions().get("Edit/Edit_Undo");
                        if (object != null) {
                          actionsProvider.invokeAction(object);
                          editor.save();
                        }
                      }
                    });
                  } catch (InvocationTargetException e) {
                    logger.error(e, e);
                  } catch (InterruptedException e) {
                    logger.error(e, e);
                  }
                }
              });
        }
      } catch (XPathException e) {
        e.printStackTrace();
      } catch (BadLocationException e) {
        e.printStackTrace();
      }
    }
  }
}
