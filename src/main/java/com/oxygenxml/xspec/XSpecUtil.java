package com.oxygenxml.xspec;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;

import org.apache.log4j.Logger;

import javafx.scene.web.WebEngine;
import netscape.javascript.JSObject;
import ro.sync.exml.workspace.api.PluginWorkspace;
import ro.sync.exml.workspace.api.editor.WSEditor;
import ro.sync.exml.workspace.api.editor.documenttype.DocumentTypeInformation;
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
  public static final String SCENARIO_NAME = "XSpec Report";
  /**
   * The output file.
   */
  private static final String SCENARIO_OUTPUT_FILE = "${cfdu}/${cfn}-report.html";
  private static final String FILE_NAME_MARKER = "${MARKER}";
  private static final String PENDING = "<!DOCTYPE html>\n" + 
      "<html>\n" + 
      "<body>\n" + 
      "\n" + 
      "<p id=\"demo\"></p>\n" + 
      "\n" + 
      "Running <b>" + FILE_NAME_MARKER + " <span class=\"pending\"></span></b>\n" + 
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
   * 
   * @throws OperationCanceledException Operation stopped by user. 
   */
  public static void runScenario(
      StandalonePluginWorkspace pluginWorkspaceAccess, 
      XSpecResultsView resultsPresenter,
      TransformationFeedback feedback) throws OperationCanceledException {
    
    runScenario(null, pluginWorkspaceAccess, resultsPresenter, feedback);
  }

  /**
   * Runs a scenario named "XSpec Report" and intercepts its result.
   * 
   * @param wsEditor XSpec editor to execute.
   * @param pluginWorkspaceAccess Workspace access.
   * @param resultsPresenter XSpec results presenter.
   * @param feedback Receives notifications when the transformation has finished.
   * 
   * @throws OperationCanceledException Operation canceled. 
   */
  public static void runScenario(
      WSEditor wsEditor,
      final StandalonePluginWorkspace pluginWorkspaceAccess, 
      final XSpecResultsView resultsPresenter,
      final TransformationFeedback feedback) throws OperationCanceledException {
    
    if (wsEditor == null) {
      wsEditor = getXSpecEditor(pluginWorkspaceAccess, resultsPresenter);
    }
    
    if (wsEditor != null) {
      final URL editorLocation = wsEditor.getEditorLocation();
      
      resultsPresenter.loadContent(
          PENDING.replace(FILE_NAME_MARKER, pluginWorkspaceAccess.getUtilAccess().getFileName(editorLocation.toString())));
      
      DocumentTypeInformation dti = wsEditor.getDocumentTypeInformation();
      if (dti == null) {
        // We need the document type association. If it's not present we wait for it.
        final WSEditor fwsEditor = wsEditor;
        // Is better to use a thread that polls the wsEditor.getDocumentTypeInformation for a few
        // times with a small sleep. Because of threading issues we might miss a callback
        // ro.sync.exml.workspace.api.listeners.WSEditorListener.documentTypeExtensionsReconfigured()
        new Thread(new Runnable() {
          @Override
          public void run() {
            int counter = 0;
            DocumentTypeInformation documentTypeInformation = fwsEditor.getDocumentTypeInformation();
            try {
              while (documentTypeInformation == null || counter < 4) {
                Thread.sleep(200);
                documentTypeInformation = fwsEditor.getDocumentTypeInformation();
                counter ++;
              }
            } catch (InterruptedException e) {
              e.printStackTrace();
            }
            
            if (documentTypeInformation != null) {
              executeScenario(pluginWorkspaceAccess, resultsPresenter, feedback,
                  editorLocation, fwsEditor);
            }
          }
        }).start();
      } else {
        executeScenario(pluginWorkspaceAccess, resultsPresenter, feedback,
            editorLocation, wsEditor);
      }
    }
  }
  

  private static void executeScenario(
      final StandalonePluginWorkspace pluginWorkspaceAccess,
      final XSpecResultsView resultsPresenter,
      final TransformationFeedback feedback, final URL editorLocation,
      final WSEditor fwsEditor) {
    try {
      fwsEditor.runTransformationScenarios(new String[] {SCENARIO_NAME}, new TransformationFeedback() {
        @Override
        public void transformationStopped() {
          // Not sure if we should do something...
          resultsPresenter.loadContent("");
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
            resultsPresenter.loadContent("");
            logger.warn("Transformation ended with error");
          }

          if (feedback != null) {
            feedback.transformationFinished(success);
          }
        }
      });
    } catch (TransformationScenarioNotFoundException e1) {
      resultsPresenter.loadContent("");
      logger.error(e1, e1);
      pluginWorkspaceAccess.showErrorMessage("Required \"XSpec Report\" scenario not found. Please install the XSpec framework as well.");
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
   * 
   * @throws OperationCanceledException The operation was canceled. 
   */
  private static WSEditor getXSpecEditor(
      final StandalonePluginWorkspace pluginWorkspaceAccess, XSpecResultsView resultsPresenter) throws OperationCanceledException {
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
      if (xspecURL == null) {
        // The current editor is not an XSpec. Ask for one.
        xspecURL = pluginWorkspaceAccess.chooseURL("Open XSpec to execute", new String[] {"xspec", "*"}, "XSpec files");
        if (xspecURL == null) {
          throw new OperationCanceledException();
        }
      }
      
      if (xspecURL != null) {
        WSEditor candidate = pluginWorkspaceAccess.getEditorAccess(xspecURL, PluginWorkspace.MAIN_EDITING_AREA);
        if (candidate == null) {
          pluginWorkspaceAccess.open(xspecURL);
          
          candidate = pluginWorkspaceAccess.getEditorAccess(xspecURL, PluginWorkspace.MAIN_EDITING_AREA);
          
        }
        
        if (candidate != null) {
          isXspecOn = true;
          currentEditorAccess = candidate;
        }
      }
    }
    
    return currentEditorAccess;
  }

  /**
   * Operation canceled. 
   *
   */
  public static class OperationCanceledException extends Exception {
    
  }
  
  /**
   * Generates an unique ID based on the given seed.
   * 
   * @param seed The seed.
   * 
   * @return A unique ID.
   */
  public static String generateId(String seed) {
    String ID = "x" + UUID.nameUUIDFromBytes(seed.getBytes()).toString();
    
    return ID;
  }
  
  /**
   * Converts a JavaScript array to a Java array.
   * 
   * @param jsArray A JS array.
   * 
   * @return The Java array.
   * 
   * @throws Exception The conversion failed.
   */
  public static Object[] convertToArray(Object jsArray) throws Exception {
    Object[] parameters = null;
    if (jsArray != null) {
      if (logger.isDebugEnabled()) {
        logger.debug("Array received from JS");
      }

      JSObject jsObj = (JSObject) jsArray;

      Object length = jsObj.eval("this.length");
      if (logger.isDebugEnabled()) {
        logger.debug("Array length: " + length);
      }

      if (length != null) {
        int size = Integer.parseInt(length.toString());

        parameters = new Object[size];

        for (int i = 0; i < size; i++) {
          Object slot = jsObj.getSlot(i);
          if (logger.isDebugEnabled()) {
            logger.debug("Slot: " + slot + " class: " + slot.getClass());
          }
          parameters[i] = slot;
        }
      }
    } else {
      logger.warn("The received parameter is not an array.");
    }
    
    return parameters;
  }
  
  /**
   * Gets the template names of the failed scenarios from the report loaded by the given engine.
   * For each scenario:
   * <pre>
   *  &lt;x:scenario label="No escaping">....&lt;/x:scenario>
   * </pre>
   * a template is generated in the compiled XSLT:
   * <pre>
   *  &lt;xsl:template name="x:x65e49470-1cdf-3d10-ad6d-79d35fbe3962">....&lt;/xsl:template>
   * </pre>
   * 
   * @param webEngine Engine that loaded the HTML report.
   * 
   * @return The names of the templates that correspond to each failed scenario separated by a space:
   * <pre>x65e49470-1cdf-3d10-ad6d-79d35fbe3962 x:x32fb71e3-69ad-3c05-809b-fec2848053ab</pre>
   * @throws Exception
   */
  public static StringBuilder getFailedTemplateNames(WebEngine webEngine) throws Exception {
    final StringBuilder b = new StringBuilder();
    Object[] failed = XSpecUtil.convertToArray(webEngine.executeScript("getFailedScenarios()"));
    if (failed != null && failed.length > 0) {
      for (int i = 0; i < failed.length; i++) {
        if (b.length() > 0) {
          b.append(" ");
        }

        b.append(String.valueOf(failed[i]));
      }
    }
    return b;
  }

}
