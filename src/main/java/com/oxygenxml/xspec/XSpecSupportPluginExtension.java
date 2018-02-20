package com.oxygenxml.xspec;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JComponent;

import com.oxygenxml.xspec.ui.Icons;

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
  private XSpecResultsView resultsPresenter;

  @Override
  public void applicationStarted(final StandalonePluginWorkspace pluginWorkspaceAccess) {
    resultsPresenter = new XSpecResultsView(pluginWorkspaceAccess);
    
    // Intercept the view creation.
    pluginWorkspaceAccess.addViewComponentCustomizer(new ViewComponentCustomizer() {
      @Override
      public void customizeView(ViewInfo viewInfo) {
        if (viewInfo.getViewID().equals(XSpecResultsView.RESULTS)) {
          viewInfo.setComponent(resultsPresenter);
          viewInfo.setTitle("XSpec Test Results");
          Icon ic = Icons.loadIcon(Icons.XSPEC_VIEW);
          viewInfo.setIcon(ic);
        }
      }
    });
    
    // Contribute a toolbar action that executes our scenario.
    pluginWorkspaceAccess.addToolbarComponentsCustomizer(new ToolbarComponentsCustomizer() {
      @Override
      public void customizeToolbar(ToolbarInfo toolbarInfo) {
        if (toolbarInfo.getToolbarID().equals("com.oxygenxml.xspec")) {
          toolbarInfo.setTitle("XSpec");
          Action action = new AbstractAction("XSpec Run") {
            @Override
            public void actionPerformed(ActionEvent e) {
              resultsPresenter.runXSpec();
            }
          };
          ToolbarButton b = new ToolbarButton(action, false);
          b.setToolTipText("Run XSpec test scenarios");
          b.setIcon(Icons.loadIcon(Icons.RUN_TESTS));
          
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
