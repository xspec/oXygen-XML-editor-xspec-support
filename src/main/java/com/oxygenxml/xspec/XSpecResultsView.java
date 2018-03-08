package com.oxygenxml.xspec;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.net.URL;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.apache.log4j.Logger;

import com.oxygenxml.xspec.jfx.BrowserInteractor;
import com.oxygenxml.xspec.jfx.SwingBrowserPanel;
import com.oxygenxml.xspec.jfx.bridge.Bridge;
import com.oxygenxml.xspec.ui.Icons;

import javafx.application.Platform;
import javafx.scene.web.WebEngine;
import ro.sync.exml.editor.EditorPageConstants;
import ro.sync.exml.workspace.api.PluginWorkspace;
import ro.sync.exml.workspace.api.editor.WSEditor;
import ro.sync.exml.workspace.api.editor.page.text.TextPopupMenuCustomizer;
import ro.sync.exml.workspace.api.editor.page.text.WSTextEditorPage;
import ro.sync.exml.workspace.api.editor.page.text.xml.WSXMLTextEditorPage;
import ro.sync.exml.workspace.api.editor.page.text.xml.XPathException;
import ro.sync.exml.workspace.api.editor.transformation.TransformationFeedback;
import ro.sync.exml.workspace.api.listeners.WSEditorChangeListener;
import ro.sync.exml.workspace.api.standalone.StandalonePluginWorkspace;
import ro.sync.exml.workspace.api.standalone.ui.ToolbarButton;
import ro.sync.exml.workspace.api.standalone.ui.ToolbarToggleButton;
import ro.sync.ui.hidpi.RetinaDetector;
import ro.sync.util.URLUtil;

/**
 * A view taht uses a JavaFX WebEngine to present the results of running an XSpec 
 * scenario.   
 * @author alex_jitianu
 */
public class XSpecResultsView extends JPanel implements XSpecResultPresenter {
  /**
   * View ID.
   */
  static final String RESULTS = "com.oxygenxml.xspec.results";
  
  /**
   * Logger for logging.
   */
  private static final Logger logger = Logger.getLogger(XSpecResultsView.class.getName());
  /**
   * A Javascript that will present just the failed tests.
   */
  protected static final String SHOW_ONLY_FAILED_TESTS = "showOnlyFailedTests();";
  /**
   * A Javascript that will present all the tests.
   */
  protected static final String SHOW_ALL_TESTS = "showAllTests();";
  
  static {
    Platform.setImplicitExit(false);
  }
  /**
   * The JavaFX browser used to present the results.
   */
  private SwingBrowserPanel panel;
  /**
   * A bridge between Javascript and Java. XSpec Javascript will call on these methods.
   * 
   * We keep this reference because of:
   * 
   * https://bugs.openjdk.java.net/browse/JDK-8170085
   */
  private Bridge xspecBridge;
  /**
   * Our workspace access.
   */
  private StandalonePluginWorkspace pluginWorkspace;
  /**
   * Currently loaded XSpec.
   */
  private URL xspec;
  
  /**
   * Show just the tests that failed.
   */
  private JButton showFailuresOnly;

  private XSpecVariablesResolver resolver = new XSpecVariablesResolver();
  /**
   * The action that runs the transformation.
   */
  private AbstractAction runAction;
  /**
   * Action to run just the failures.
   */
  private AbstractAction runFailuresAction;
  
  /**
   * Constructor.
   * 
   * @param pluginWorkspace Oxygen workspace access.
   */
  public XSpecResultsView(
      final StandalonePluginWorkspace pluginWorkspace) {
    this.pluginWorkspace = pluginWorkspace;
    pluginWorkspace.getUtilAccess().addCustomEditorVariablesResolver(resolver);
    panel = new SwingBrowserPanel(new BrowserInteractor() {
      @Override
      public void pageLoaded() {
        if (xspec != null) {
          if (xspecBridge != null) {
            // A little house keeping.
            xspecBridge.dispose();
          }
          // The XSpec results were loaded by the WebEngine.
          // Install the Javascript->Java bridge.
          xspecBridge = Bridge.install(
              panel.getWebEngine(), 
              XSpecResultsView.this,
              getVariableResolver(),
              pluginWorkspace, 
              xspec);
          
          applyTestFilter();
        }
      }
      
      @Override
      public void alert(String message) {
        // A debugging method for the javascript. Redirect Javascript alerts to the console.
        logger.info(message);
      }
    });
    
    Object object = UIManager.get("SidePane.lineColor");
    if (object instanceof Color) {
      Color c = (Color) object;
      
      panel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, c));
    }
    
    if (RetinaDetector.getInstance().isRetinaNoImplicitSupport()) {
      float scalingFactor = RetinaDetector.getInstance().getScalingFactor();
      panel.setScaleFactor(scalingFactor);
    }
    
    panel.loadContent("");
    
    setLayout(new BorderLayout());
    
    JToolBar toolbar = new JToolBar();
    toolbar.setFloatable(false);
    
    // Run scenario
    runAction = new AbstractAction() {
      @Override
      public void actionPerformed(ActionEvent arg0) {
        // Run all scenarios.
        resolver.setTemplateNames("");
        enableButtons(false);
        try {
          XSpecUtil.runScenario(pluginWorkspace, XSpecResultsView.this, createTransformationFeedback());
        } catch (OperationCanceledException e) {
          // canceled by user.
          enableButtons(true);
        }
      }
    };
    
    final Icon runIcon = Icons.loadIcon(Icons.RUN_TESTS);
    runAction.putValue(Action.SMALL_ICON, runIcon);
    runAction.putValue(Action.SHORT_DESCRIPTION, "Run XSpec");
    
    // Show failures.
    Action showFailuresAction = new AbstractAction() {
      @Override
      public void actionPerformed(ActionEvent arg0) {
        applyTestFilter();
      }
    };
    
    Icon ic = Icons.loadIcon(Icons.SHOW_ONLY_FAILED_TESTS);
    showFailuresAction.putValue(Action.SMALL_ICON, ic);
    showFailuresAction.putValue(Action.SHORT_DESCRIPTION, "Show only failures");
    
    // Run scenario
    runFailuresAction = new AbstractAction() {
      @Override
      public void actionPerformed(ActionEvent arg0) {
        enableButtons(false);
        Platform.runLater(new Runnable() {
          @Override
          public void run() {
            try {
              final StringBuilder b = XSpecUtil.getFailedTemplateNames(panel.getWebEngine());
              
              if (b.length() > 0) {
                SwingUtilities.invokeLater(new Runnable() {
                  @Override
                  public void run() {
                    resolver.setTemplateNames(b.toString());
                    try {
                      XSpecUtil.runScenario(
                          pluginWorkspace, 
                          XSpecResultsView.this, 
                          createTransformationFeedback());
                    } catch (OperationCanceledException e) {
                      // canceled by user.
                      enableButtons(true);
                    }
                  }
                });
              } else {
                pluginWorkspace.showInformationMessage("No failed messages!");
                enableButtons(true);  
              }
            } catch (Exception e) {
              logger.error(e, e);
              enableButtons(true);
            }
          }
        });
      }
    };
    
    Icon runFailureIcon = Icons.loadIcon(Icons.RUN_FAILED_TESTS);
    runFailuresAction.putValue(Action.SMALL_ICON, runFailureIcon);
    runFailuresAction.putValue(Action.SHORT_DESCRIPTION, "Run Only Failures");

    ToolbarButton runFailuresButton = new ToolbarButton(runFailuresAction, false);
    showFailuresOnly = new ToolbarToggleButton(showFailuresAction);
    ToolbarButton runButton = new ToolbarButton(runAction, false);
    
    toolbar.add(runButton);
    toolbar.add(runFailuresButton);
    toolbar.addSeparator();
    toolbar.add(showFailuresOnly);
    
    add(toolbar, BorderLayout.NORTH);
    
    add(panel, BorderLayout.CENTER);
    
    enableButtons(true);
    
    // Contextual menu.
    pluginWorkspace.addEditorChangeListener(new WSEditorChangeListener() {
      @Override
      public void editorOpened(URL editorLocation) {
        final WSEditor editorAccess = pluginWorkspace.getEditorAccess(editorLocation, PluginWorkspace.MAIN_EDITING_AREA);
        if ("xspec".equals(URLUtil.getExtension(URLUtil.extractFileName(editorLocation.toString()))) &&
            EditorPageConstants.PAGE_TEXT.equals(editorAccess.getCurrentPageID())) {
          WSXMLTextEditorPage textPage = ((WSXMLTextEditorPage) editorAccess.getCurrentPage());
          
          textPage.addPopUpMenuCustomizer(new TextPopupMenuCustomizer() {
            @Override
            public void customizePopUpMenu(Object popUp, WSTextEditorPage textPage) {
              JPopupMenu jPopup = (JPopupMenu) popUp;
              
              AbstractAction runCurrentAction = createRunScenarioAction(pluginWorkspace, editorAccess, textPage);
              
              runCurrentAction.putValue(Action.SMALL_ICON, runIcon);
              
              runCurrentAction.putValue(Action.NAME, "Run test scenario(s)");
              
              jPopup.add(runCurrentAction);
            }
          });
        }
      }
    }, PluginWorkspace.MAIN_EDITING_AREA);
  }
  
  /**
   * Applies the filtering criteria on the test results.
   */
  private void applyTestFilter() {
    boolean selected = showFailuresOnly.isSelected();
    if (selected) {
      panel.executeScript(SHOW_ONLY_FAILED_TESTS);
    } else {
      panel.executeScript(SHOW_ALL_TESTS);
    }
  }
  
  public void setFilterTests(boolean filter) {
    showFailuresOnly.setSelected(filter);
    
    applyTestFilter();
  }
  
  /**
   * Loads the results of an XSpec script.
   * 
   * @param xspec The executed XSpec.
   * @param results The results.
   */
  public void load(URL xspec, URL results) {
    this.xspec = xspec;
    
    pluginWorkspace.showView(RESULTS, false);
    
    panel.loadURL(results);
  }

  /**
   * Loads the results of an XSpec script.
   * 
   * @param xspec The executed XSpec.
   * @param results The results.
   */
  public void loadContent(String content) {
    this.xspec = null;
    
    panel.loadContent(content);
    
    pluginWorkspace.showView(RESULTS, false);
  }  
  
  /**
   * Dispose.
   */
  public void dispose() {
    if (xspecBridge != null) {
      xspecBridge.dispose();
    }
  }
  
  public URL getXspec() {
    return xspec;
  }
  
  public WebEngine getEngineForTests() {
    return panel.getWebEngine();
  }
  
  private void enableButtons(boolean enable) {
    runAction.setEnabled(enable);
    runFailuresAction.setEnabled(enable && xspec != null);
    showFailuresOnly.setEnabled(enable && xspec != null);
    if (enable) {
      resolver.setTemplateNames("");
    }
  }

  /**
   * @return Special variables resolver.
   */
  public XSpecVariablesResolver getVariableResolver() {
    return resolver;
  }
  
  /**
   * Creates an action that executes the scenario at caret position.
   * 
   * @param pluginWorkspace Plugin workspace.
   * @param editorAccess Editor acccess.
   * @param textPage Text page.
   * 
   * @return An action that executes the scenario at caret position.
   */
  private AbstractAction createRunScenarioAction(
      final StandalonePluginWorkspace pluginWorkspace,
      final WSEditor editorAccess, 
      final WSTextEditorPage textPage) {
    return new AbstractAction() {
      @Override
      public void actionPerformed(ActionEvent e) {
        String scenarioName = "";
        String xpath = "string-join(\n" + 
            "for $s in  ancestor-or-self::*:scenario\n" + 
            "return  \n" + 
            "    concat(\n" + 
            "            if ($s/@label) then $s/@label else $s/*:label,  '(',\n" + 
            "            count($s/preceding-sibling::x:scenario), ')')\n" + 
            "       , ' / ' )\n" + 
            "      ";
        try {
          Object[] evaluateXPath = ((WSXMLTextEditorPage) textPage).evaluateXPath(xpath);
          if (evaluateXPath != null && String.valueOf(evaluateXPath[0]).length() > 0) {
            if (logger.isDebugEnabled()) {
              logger.debug("Xpath result:" + evaluateXPath[0]);
            }
            scenarioName = String.valueOf(evaluateXPath[0]);
            scenarioName = XSpecUtil.generateId(scenarioName);
          }
        } catch (XPathException ex) {
          logger.error(ex, ex);
        }
        
        if (logger.isDebugEnabled()) {
          logger.debug("scenarioName  |" + scenarioName + "|");
        }
        
        // We only need to execute this scenario.
        resolver.setTemplateNames(scenarioName);
        
        XSpecResultPresenter resultsPresenter = XSpecResultsView.this;
        // Step 3. Run the scenario
        try {
          XSpecUtil.runScenario(
              editorAccess,
              (StandalonePluginWorkspace) pluginWorkspace, 
              resultsPresenter,
              new TransformationFeedback() {
                @Override
                public void transformationStopped() {}
                @Override
                public void transformationFinished(boolean success) {}
              });
        } catch (OperationCanceledException ex) {
          logger.error(ex, ex);
        }
      }
    };
  }
  
  /**
   * @return An implementation that will enable the buttons when the transformation is finished.
   */
  private TransformationFeedback createTransformationFeedback() {
    return new TransformationFeedback() {
      @Override
      public void transformationStopped() {
        enableButtons(true);
      }
      @Override
      public void transformationFinished(boolean success) {
        enableButtons(true);
      }
    };
  }
  
  /**
   * Detects and executes an XSpec file.
   */
  public void runXSpec() {
    runAction.actionPerformed(null);
  }
}
