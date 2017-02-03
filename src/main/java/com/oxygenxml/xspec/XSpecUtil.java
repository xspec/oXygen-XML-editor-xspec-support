package com.oxygenxml.xspec;

import java.net.MalformedURLException;
import java.net.URL;

import org.apache.log4j.Logger;

import ro.sync.exml.workspace.api.PluginWorkspace;
import ro.sync.exml.workspace.api.editor.WSEditor;
import ro.sync.exml.workspace.api.editor.transformation.TransformationFeedback;
import ro.sync.exml.workspace.api.editor.transformation.TransformationScenarioNotFoundException;
import ro.sync.exml.workspace.api.standalone.StandalonePluginWorkspace;

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

  /**
   * Runs a scenario named "XSpec Report" and intercepts its result.
   * 
   * @param pluginWorkspaceAccess Workspace access.
   * @param resultsPresenter XSpec results presenter.
   * @param feedback Receives notifications when the transformation has finished.
   */
  public static void runScenario(
      final StandalonePluginWorkspace pluginWorkspaceAccess, 
      final XSpecResultsPresenter resultsPresenter,
      final TransformationFeedback feedback) {
    final WSEditor wsEditor = pluginWorkspaceAccess.getCurrentEditorAccess(PluginWorkspace.MAIN_EDITING_AREA);
    if (wsEditor != null) {
      try {
        wsEditor.runTransformationScenarios(new String[] {SCENARIO_NAME}, new TransformationFeedback() {
          @Override
          public void transformationStopped() {
            // Not sure if we should do something...
          }
          
          @Override
          public void transformationFinished(boolean success) {
            if (success) {
              String toOpen = pluginWorkspaceAccess.getUtilAccess().expandEditorVariables(SCENARIO_OUTPUT_FILE, wsEditor.getEditorLocation());
              try {
                resultsPresenter.load(wsEditor.getEditorLocation(), new URL(toOpen));
              } catch (MalformedURLException e) {
                e.printStackTrace();
              }
            }
            
            if (feedback != null) {
              feedback.transformationFinished(success);
            }
          }
        });
      } catch (TransformationScenarioNotFoundException e1) {
        logger.error(e1, e1);
      }
    }
  }
}
