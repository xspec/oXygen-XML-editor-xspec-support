package com.oxygenxml.xspec.jfx;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;

import junit.framework.TestCase;

/**
 * Tests that we don't use certain methods.
 * 
 * @author alex_jitianu
 */
public class NotAllowedUsageTest extends TestCase {

  /**
   * Tests that we don't use certain methods.
   *  
   * @throws Exception If it fails.
   */
  public void testNotAllowed() throws Exception {
    File bridgeClass = new File("src/main/java/com/oxygenxml/xspec/jfx/bridge/Bridge.java");
    
    FileInputStream fis = new FileInputStream(bridgeClass);
    BufferedReader r = new BufferedReader(new InputStreamReader(fis));
    StringBuilder b = new StringBuilder();
    try {
      String line = null;
      int lineNumber = 0;
      while((line = r.readLine()) != null) {
        
        int indexOf = line.indexOf("SwingUtilities.invokeAndWait(");
        if (indexOf != -1) {
          b.append("Line ").append(lineNumber).append("\n");
        }
        
        lineNumber ++;
      }
    } finally {
      r.close();
    }
    
    // https://bugs.openjdk.java.net/browse/JDK-8087465
    assertEquals("Avoid using SwingUtilities.invokeAndWait() inside the JFX-AWT bridge. Found:\n", "", b.toString());
  }
}
