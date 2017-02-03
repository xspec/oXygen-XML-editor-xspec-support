package com.oxygenxml.xspec.jfx;

/**
 * Receives notifications from the browser.
 */
public interface BrowserInteractor {

	/**
	 * A message intercepted on the JavaScript alert() method.
	 */
	void alert(String message);
	/**
	 * The page was loaded.
	 */
	void pageLoaded();
}
