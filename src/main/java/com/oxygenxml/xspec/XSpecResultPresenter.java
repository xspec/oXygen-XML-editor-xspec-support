package com.oxygenxml.xspec;

import java.net.URL;

/**
 * Presents the HTML resulted from executing an XSpec scenario.
 * 
 * @author alex_jitianu
 */
public interface XSpecResultPresenter {
  /**
   * If it already presents the results of an XSpec execution
   * then it will return the URL of the executed XSpec.
   * 
   * @return The XSpec that was executed and its results are currently presented.
   */
  URL getXspec();

  /**
   * Loads the results from executing an XSpec file.
   * 
   * @param resultHTML The result.
   */
  void loadContent(String resultHTML);

  /**
   * Loads the results from executing an XSpec file.
   * 
   * @param xspec The executed XSpec.
   * @param results The results in a HTML format.
   */
  public void load(URL xspecURL, URL resultHTML);
}
