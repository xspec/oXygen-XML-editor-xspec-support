package com.oxygenxml.xspec.diff;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;

import org.apache.log4j.Logger;

import com.oxygenxml.xspec.protocol.DiffFragmentRepository;
import com.oxygenxml.xspec.protocol.DiffURLStreamHandler;

import junit.framework.TestCase;

public class DiffURLStreamHandlerTest extends TestCase {
  
  /**
   * Logger for logging.
   */
  private static final Logger logger = Logger.getLogger(DiffURLStreamHandlerTest.class.getName());


  /**
   * Installs the GIT protocol that we use to identify certain file versions.
   */
  protected void installDiffProtocol() {
    // Install protocol.
    try {
    URL.setURLStreamHandlerFactory(new URLStreamHandlerFactory() {
      @Override
      public URLStreamHandler createURLStreamHandler(String protocol) {
        if (protocol.equals(DiffURLStreamHandler.DIFF_PROTOCOL)) {
          URLStreamHandler handler = new DiffURLStreamHandler();
          return handler;
        }
        
        return null;
      }
    });
    } catch (Throwable t) {
      if (!t.getMessage().contains("factory already defined")) {
        logger.info(t, t);
      }
    } 
  }
  
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    
    installDiffProtocol();
  }
  
  
  /**
   * Fragments are added inside a cache and can be accessed through a special protocol.
   * 
   * @throws Exception If it fails.
   */
  public void testCache() throws Exception {
    DiffFragmentRepository instance = DiffFragmentRepository.getInstance();
    
    String fragment = "<fragment></fragment>";
    URL cache = instance.cache(fragment, "something.com");
    
    assertEquals("diff:/something.com/369136489.xml", cache.toString());
    
    assertEquals(fragment, read(cache));
    
    fragment = "1 + 1 = 2";
    cache = instance.cache(fragment, "something.com");
    
    assertEquals("diff:/something.com/2034751356.txt", cache.toString());
    
    assertEquals(fragment, read(cache));
  }
  
  private String read(URL toRead) throws Exception {
    StringBuilder b = new StringBuilder();
    InputStream openStream = null;
    try {
      openStream = toRead.openStream();
      InputStreamReader r = new InputStreamReader(openStream);
      
      char[] c = new char[1024];
      int l = -1;
      while ((l = r.read(c)) != -1) {
        b.append(c, 0, l);
      }
    } finally {
      if (openStream != null) {
        openStream.close();
      }
    }
    
    return b.toString();
  }
  
}
