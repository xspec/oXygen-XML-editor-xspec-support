package com.oxygenxml.xspec.protocol;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

public class DiffURLStreamHandler extends URLStreamHandler {

  /**
   * Protocol name.
   */
  public static final String DIFF_PROTOCOL = DiffFragmentRepository.DIFF_PROTOCOL;

  @Override
  protected URLConnection openConnection(URL u) throws IOException {
    return new DiffURLConnection(u);
  }
  
  /**
   * Connection for a DIFF fragment identifying URL.
   * 
   * @author alex_jitianu
   */
  private class DiffURLConnection extends URLConnection {
    /**
     * Constructor.
     * 
     * @param url Fragment identifying URL.
     */
    protected DiffURLConnection(URL url) {
      super(url);
    }

    @Override
    public void connect() throws IOException {}
    
    @Override
    public InputStream getInputStream() throws IOException {
      String fragment = DiffFragmentRepository.getInstance().getFragment(url);
      if (fragment == null) {
        // Unable 
        throw new IOException("Unable to get the content");
      }
      
      return new ByteArrayInputStream(fragment.getBytes("UTF-8"));
    }

  }

  public static void main(String[] args) throws Exception {
    URL url = new URL("http://EXPECTED/12343.32.xml");

    System.out.println(url.getHost());
    System.out.println(url.getPath());
  }

}
