package com.oxygenxml.xspec;

import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;

import ro.sync.exml.options.APIAccessibleOptionTags;
import ro.sync.exml.plugin.workspace.WorkspaceAccessPluginExtension;
import ro.sync.exml.workspace.api.standalone.StandalonePluginWorkspace;
import ro.sync.exml.workspace.api.standalone.ToolbarComponentsCustomizer;
import ro.sync.exml.workspace.api.standalone.ToolbarInfo;
import ro.sync.exml.workspace.api.standalone.ViewComponentCustomizer;
import ro.sync.exml.workspace.api.standalone.ViewInfo;
import ro.sync.exml.workspace.api.standalone.ui.ToolbarButton;

/**
 * Contributes a view for presenting XSpec results.
 * 
 * @author alex_jitianu
 */
public class XSpecSupportPluginExtension implements WorkspaceAccessPluginExtension {
  /**
   * Results presenter.
   */
  private XSpecResultsPresenter resultsPresenter;

  @Override
  public void applicationStarted(final StandalonePluginWorkspace pluginWorkspaceAccess) {
    resultsPresenter = new XSpecResultsPresenter(pluginWorkspaceAccess);
    
    // Intercept the view creation.
    pluginWorkspaceAccess.addViewComponentCustomizer(new ViewComponentCustomizer() {
      @Override
      public void customizeView(ViewInfo viewInfo) {
        if (viewInfo.getViewID().equals(XSpecResultsPresenter.RESULTS)) {
          viewInfo.setComponent(resultsPresenter);
          viewInfo.setTitle("XSpec Test Results");
        }
      }
    });
    
    // Contribute a toolbar action that executes our scenario.
    pluginWorkspaceAccess.addToolbarComponentsCustomizer(new ToolbarComponentsCustomizer() {
      @Override
      public void customizeToolbar(ToolbarInfo toolbarInfo) {
        if (toolbarInfo.getToolbarID().equals("com.oxygenxml.xspec")) {
          Action action = new AbstractAction("XSpec Run") {
            @Override
            public void actionPerformed(ActionEvent e) {
              XSpecUtil.runScenario(pluginWorkspaceAccess, resultsPresenter, null);
            }
          };
          ToolbarButton b = new ToolbarButton(action, true);
          
          toolbarInfo.setComponents(new JComponent[] {b});
        }
      }
    });
  }
  
  @Override
  public boolean applicationClosing() {
    resultsPresenter.dispose();
    return true;
  }
}
