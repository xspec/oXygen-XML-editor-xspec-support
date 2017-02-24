package com.oxygenxml.xspec;

import java.awt.BorderLayout;
import java.net.URL;

import javafx.application.Platform;

import javax.swing.JPanel;

import org.apache.log4j.Logger;

import ro.sync.exml.workspace.api.standalone.StandalonePluginWorkspace;

import com.oxygenxml.xspec.jfx.BrowserInteractor;
import com.oxygenxml.xspec.jfx.SwingBrowserPanel;
import com.oxygenxml.xspec.jfx.bridge.Bridge;

/**
 * A view taht uses a JavaFX WebEngine to present the results of running an XSpec 
 * scenario.   
 * @author alex_jitianu
 */
public class XSpecResultsPresenter extends JPanel {
  /**
   * View ID.
   */
  static final String RESULTS = "com.oxygenxml.xspec.results";
  
  /**
   * Logger for logging.
   */
  private static final Logger logger = Logger.getLogger(XSpecResultsPresenter.class.getName());
  
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
   * Constructor.
   * 
   * @param pluginWorkspace Oxygen workspace access.
   */
  public XSpecResultsPresenter(final StandalonePluginWorkspace pluginWorkspace) {
    this.pluginWorkspace = pluginWorkspace;
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
          xspecBridge = Bridge.install(panel.getWebEngine(), XSpecResultsPresenter.this, pluginWorkspace, xspec);
        }
      }
      
      @Override
      public void alert(String message) {
        // A debugging method for the javascript. Redirect Javascript alerts to the console.
        logger.info(message);
      }
    });
    
    panel.loadContent("");
    
    setLayout(new BorderLayout());
    add(panel, BorderLayout.CENTER);
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
}
