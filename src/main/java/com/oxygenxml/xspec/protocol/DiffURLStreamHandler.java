package com.oxygenxml.xspec.protocol;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

import org.apache.log4j.Logger;

import ro.sync.exml.editor.ContentTypes;
import ro.sync.util.URLUtil;

public class DiffURLStreamHandler extends URLStreamHandler {
  /**
   * Logger for logging.
   */
  private static final Logger logger = Logger.getLogger(DiffURLStreamHandler.class.getName());

  /**
   * Protocol name.
   */
  public static final String DIFF_PROTOCOL = "diff";

  @Override
  protected URLConnection openConnection(URL u) throws IOException {
    return new DiffURLConnection(u);
  }
  
  /**
   * Builds an URL to identify the given key.
   * 
   * @param key The key.
   * @param host Something to put as host name.
   * 
   * @return The URL form.
   * 
   * @throws MalformedURLException Problems building the URL.
   */
  public static URL build(int key, String host) throws MalformedURLException {
    DiffFragment diffFragment = DiffFragmentRepository.getInstance().get(key);
    StringBuilder b = new StringBuilder(DIFF_PROTOCOL);
    b.append(":/").append(host).append("/").append(key).append(".");
    if (diffFragment.getContentType().equals(ContentTypes.XML_CONTENT_TYPE)) {
      b.append("xml");
    } else {
      b.append("txt");
    }
    return new URL(b.toString());
  }

  /**
   * Gets the fragment identified by this URL.
   * 
   * @param u The URL.
   * 
   * @return The fragment or <code>null</code>.
   */
  private static String getFragment(URL u) {
    String fragment = null;
    
    String fileName = URLUtil.extractFileName(u);
    String extension = URLUtil.getExtension(fileName);

    String idAsString = fileName.substring(0, fileName.length() - extension.length() - 1);

    try {
      int key = Integer.parseInt(idAsString);

      DiffFragment diffFragment = DiffFragmentRepository.getInstance().get(key);
      if (diffFragment != null) {
        fragment = diffFragment.getContent();
      }

    } catch (Throwable t) {
      logger.error(t, t);
    }
    
    return fragment;
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
      String fragment = getFragment(url);
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
