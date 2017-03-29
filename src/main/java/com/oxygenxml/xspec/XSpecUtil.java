package com.oxygenxml.xspec;

import java.net.MalformedURLException;
import java.net.URL;

import org.apache.log4j.Logger;

import ro.sync.exml.workspace.api.PluginWorkspace;
import ro.sync.exml.workspace.api.editor.WSEditor;
import ro.sync.exml.workspace.api.editor.transformation.TransformationFeedback;
import ro.sync.exml.workspace.api.editor.transformation.TransformationScenarioNotFoundException;
import ro.sync.exml.workspace.api.standalone.StandalonePluginWorkspace;
import ro.sync.util.URLUtil;

/**
 * Utilities.
 *  
 * @author alex_jitianu
 */
public class XSpecUtil {
  
  /**
   * Logger for logging.
   */
  private static final Logger logger = Logger.getLogger(XSpecUtil.class.getName());
  /**
   * The name of the XSpec running scenario.
   */
  private static final String SCENARIO_NAME = "XSpec Report";
  /**
   * The output file.
   */
  private static final String SCENARIO_OUTPUT_FILE = "${cfdu}/${cfn}-report.html";
  private static final String PENDING = "<!DOCTYPE html>\n" + 
      "<html>\n" + 
      "<body>\n" + 
      "\n" + 
      "<p id=\"demo\"></p>\n" + 
      "\n" + 
      "<b>Running<span class=\"pending\"></span></b>\n" + 
      "\n" + 
      "<script>\n" + 
      "function change() {\n" + 
      " var elem = document.getElementsByClassName(\"pending\")[0];\n" + 
      "    \n" + 
      "    var ct = elem.innerHTML + \".\";\n" + 
      "    \n" + 
      "    if (10 == ct.length) {\n" + 
      "      ct = \".\";\n" + 
      "    }  \n" + 
      "    \n" + 
      "    elem.innerHTML = ct;\n" + 
      "    \n" + 
      "    console.log(elem.innerHTML);\n" + 
      "    \n" + 
      "    \n" + 
      "    setTimeout(function() {change()}, 300);\n" + 
      "    \n" + 
      "    return;\n" + 
      "}\n" + 
      "\n" + 
      "\n" + 
      "change();\n" + 
      "</script>\n" + 
      "\n" + 
      "</body>\n" + 
      "</html>";
  
  /**
   * Runs a scenario named "XSpec Report" and intercepts its result.
   * 
   * @param pluginWorkspaceAccess Workspace access.
   * @param resultsPresenter XSpec results presenter.
   * @param feedback Receives notifications when the transformation has finished.
   */
  public static void runScenario(
      StandalonePluginWorkspace pluginWorkspaceAccess, 
      XSpecResultsView resultsPresenter,
      TransformationFeedback feedback) {
    
    runScenario(null, pluginWorkspaceAccess, resultsPresenter, feedback);
  }

  /**
   * Runs a scenario named "XSpec Report" and intercepts its result.
   * 
   * @param wsEditor XSpec editor to execute.
   * @param pluginWorkspaceAccess Workspace access.
   * @param resultsPresenter XSpec results presenter.
   * @param feedback Receives notifications when the transformation has finished.
   */
  public static void runScenario(
      WSEditor wsEditor,
      final StandalonePluginWorkspace pluginWorkspaceAccess, 
      final XSpecResultsView resultsPresenter,
      final TransformationFeedback feedback) {
    
    if (wsEditor == null) {
      wsEditor = getXSpecEditor(pluginWorkspaceAccess, resultsPresenter);
    }
    
    resultsPresenter.loadContent(PENDING);
    
    if (wsEditor != null) {
      final URL editorLocation = wsEditor.getEditorLocation();
      try {
        wsEditor.runTransformationScenarios(new String[] {SCENARIO_NAME}, new TransformationFeedback() {
          @Override
          public void transformationStopped() {
            // Not sure if we should do something...
          }

          @Override
          public void transformationFinished(boolean success) {
            if (success) {
              String toOpen = pluginWorkspaceAccess.getUtilAccess().expandEditorVariables(SCENARIO_OUTPUT_FILE, editorLocation);
              try {
                resultsPresenter.load(editorLocation, new URL(toOpen));
              } catch (MalformedURLException e) {
                e.printStackTrace();
              }
            } else {
              logger.warn("Transformation ended with error");
            }

            if (feedback != null) {
              feedback.transformationFinished(success);
            }
          }
        });
      } catch (TransformationScenarioNotFoundException e1) {
        logger.error(e1, e1);
        pluginWorkspaceAccess.showErrorMessage("Required \"XSpec Report\" scenario not found. Please install the XSpec framework as well.");
      }
    }
  }

  /**
   * Gets the XSpec to execute. This is either the current selected editor, or if the current selected 
   * editor doesn't appear to be an XSpec, the last executed XSpec. 
   * 
   * @param pluginWorkspaceAccess Plugin workspace access.
   * @param resultsPresenter The XSpec results presenter.
   * 
   * @return The XSpec editor to execute.
   */
  private static WSEditor getXSpecEditor(
      final StandalonePluginWorkspace pluginWorkspaceAccess, XSpecResultsView resultsPresenter) {
    WSEditor currentEditorAccess = pluginWorkspaceAccess.getCurrentEditorAccess(PluginWorkspace.MAIN_EDITING_AREA);
    
    boolean isXspecOn = false;
    if (currentEditorAccess != null) {
      String extension = URLUtil.getExtension(currentEditorAccess.getEditorLocation().toString());
      if ("xspec".equals(extension)) {
        isXspecOn = true;
      } else if ("xsl".equals(extension) || "xslt".equals(extension)) {
        isXspecOn = false;
      } else {
        // TODO Perhaps a detection of the namespace of the root element (XPath).
      }
    }
    
    if (!isXspecOn) {
      URL xspecURL = resultsPresenter.getXspec();
      if (xspecURL != null) {
        WSEditor candidate = pluginWorkspaceAccess.getEditorAccess(xspecURL, PluginWorkspace.MAIN_EDITING_AREA);
        if (candidate == null) {
          pluginWorkspaceAccess.open(xspecURL);
          
          candidate = pluginWorkspaceAccess.getEditorAccess(xspecURL, PluginWorkspace.MAIN_EDITING_AREA);
        }
        
        if (candidate != null) {
          currentEditorAccess = candidate;
        }
      }
    }
    
    return currentEditorAccess;
  }

}
