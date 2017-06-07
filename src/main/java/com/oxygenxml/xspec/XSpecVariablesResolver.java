package com.oxygenxml.xspec;

import ro.sync.exml.workspace.api.util.EditorVariablesResolver;

/**
 * Contributes a number of special XSpec variables.
 *  
 * @author alex_jitianu
 */
public class XSpecVariablesResolver extends EditorVariablesResolver {
  /**
   * The name of the entry point template.
   */
  private static final String TEMPLATE_NAME_VAR = "${xspec.template.name.entrypoint}";
  /**
   * <code>true</code> to skip recompiling the XSpec file.
   */
  private static final String SKIP_COMPILE_VAR = "${skipCompile}";
  /**
   * The name of the entry point template.
   */
  private String templateNames = null;
  /**
   * <code>true</code> to skip recompiling the XSpec file.
   */
  private boolean skipCompilation = false;
  
  
  public void setTemplateNames(String templateName) {
    this.templateNames = templateName;
  }

  public void setSkipCompilation(boolean skipCompilation) {
    this.skipCompilation = skipCompilation;
  }
  
  @Override
  public String resolveEditorVariables(String contentWithEditorVariables,
      String currentEditedFileURL) {
    String tpl = "";
    if (templateNames != null) {
      tpl = templateNames;
    }
    
    String expr = contentWithEditorVariables.replace(TEMPLATE_NAME_VAR, tpl);
    
    expr = expr.replace(SKIP_COMPILE_VAR, String.valueOf(skipCompilation));
    
    return expr;
  }
}
