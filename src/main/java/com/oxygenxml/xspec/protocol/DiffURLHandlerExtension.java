package com.oxygenxml.xspec.protocol;

import java.net.URL;
import java.net.URLStreamHandler;

import ro.sync.exml.plugin.urlstreamhandler.URLHandlerReadOnlyCheckerExtension;
import ro.sync.exml.plugin.urlstreamhandler.URLStreamHandlerPluginExtension;

/**
 * Extension point that builds URL that are used in Oxygen's DIFF tool.
 *  
 * @author alex_jitianu
 */
public class DiffURLHandlerExtension
    implements URLStreamHandlerPluginExtension, URLHandlerReadOnlyCheckerExtension {
  
  @Override
  public URLStreamHandler getURLStreamHandler(String protocol) {
    if (DiffURLStreamHandler.DIFF_PROTOCOL.equals(protocol)) {
      return new DiffURLStreamHandler();
    }
    
    return null;
  }

  @Override
  public boolean isReadOnly(URL url) {
    if (DiffURLStreamHandler.DIFF_PROTOCOL.equals(url.getProtocol())) {
      // All of our resources are read only.
      return true;
    }
    
    return false;
  }

  @Override
  public boolean canCheckReadOnly(String protocol) {
    if (DiffURLStreamHandler.DIFF_PROTOCOL.equals(protocol)) {
      // All of our resources are read only.
      return true;
    }
    
    return false;

  }
}
