package com.oxygenxml.xspec.ui;

import java.net.URL;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import ro.sync.exml.workspace.api.PluginWorkspace;
import ro.sync.exml.workspace.api.PluginWorkspaceProvider;

/**
 * Icons used by the plugins.
 *  
 * @author alex_jitianu
 */
public class Icons {
  /**
   * The icon for the view.
   */
  public static final String XSPEC_VIEW = "icons/DockableFrameXSpecView16.png";
  /**
   * Run all Xspec tests from a file.
   */
  public static final String RUN_TESTS = "icons/XSpecRun16.png";
  /**
   * Run just the failed tests.
   */
  public static final String RUN_FAILED_TESTS = "icons/XSpecRunFailures16.png";
  /**
   * Shows only the failed tests.
   */
  public static final String SHOW_ONLY_FAILED_TESTS = "icons/XSpecShowFailures16.png";

  /**
   * Creates an icon for the given resource.
   * 
   * @param path Icon path.
   * 
   * @return The icon or null if it was not found.
   */
  public static final Icon loadIcon(String path) {
    Icon icon = null;
    URL resource = Icons.class.getClassLoader().getResource(path);
    if (resource != null) {
      PluginWorkspace pluginWorkspace = PluginWorkspaceProvider.getPluginWorkspace();
      if (pluginWorkspace != null) {
        icon = (Icon) pluginWorkspace.getImageUtilities().loadIcon(resource);
      }
      
      icon = new ImageIcon(resource);
    }
    
    return icon;
  }
}
