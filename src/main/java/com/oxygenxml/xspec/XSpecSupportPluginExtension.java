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
    
    String[] additional = (String[]) pluginWorkspaceAccess.getGlobalObjectProperty(
    		APIAccessibleOptionTags.ADDITIONAL_FRAMEWORKS_DIRECTORIES);
    String absolutePath = new File(XSpecSupportPlugin.getInstance().getDescriptor().getBaseDir(), "frameworks").getAbsolutePath();
    if (additional != null && additional.length > 0) {
    	boolean found = false;
    	for (int i = 0; i < additional.length; i++) {
			if (absolutePath.equals(additional[i])) {
				found = true;
				break;
			}
		}
    	
    	if (!found) {
    		String[] additionalFrameworks = new String[additional.length + 1];
    		System.arraycopy(additional, 0, additionalFrameworks, 0, additional.length);
    	}
    } else {
    	additional = new String[1];
    }
    
	additional[additional.length - 1] = 
    		absolutePath;
    
    pluginWorkspaceAccess.setGlobalObjectProperty(APIAccessibleOptionTags.ADDITIONAL_FRAMEWORKS_DIRECTORIES, additional);
    
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
    return true;
  }
}
