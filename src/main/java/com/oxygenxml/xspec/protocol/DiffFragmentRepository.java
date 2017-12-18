package com.oxygenxml.xspec.protocol;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import ro.sync.exml.editor.ContentTypes;
import ro.sync.exml.workspace.api.PluginWorkspaceProvider;
import ro.sync.util.URLUtil;

/**
 * A repository for the XML fragments that can be accessed through the DIFF protocol.
 *  
 * @author alex_jitianu
 */
public class DiffFragmentRepository {
  /**
   * Logger for logging.
   */
  private static final Logger logger = Logger.getLogger(DiffFragmentRepository.class.getName());
  
  /**
   * Protocol name.
   */
  public static final String DIFF_PROTOCOL = "diff";
  
  /**
   * Singleton instance.
   */
  private static DiffFragmentRepository instance;
  /**
   * Maps an ID to an XML fragment.
   */
  private Map<Integer, DiffFragment> cache = new HashMap<Integer, DiffFragment>();
  /**
   * Private contructor.
   */
  private DiffFragmentRepository() {}
  
  /**
   * @return The repository instance.
   */
  public static DiffFragmentRepository getInstance() {
    if (instance == null) {
      instance = new DiffFragmentRepository();
    }
    
    return instance;
  }
  
  /**
   * Records a new fragment in the repository.
   * 
   * @param key Unique key to identify this fragment.
   * @param fragment Fragment to store.
   */
  private void put(int key, String fragment) {
    String contentType = getContentType(fragment);
    cache.put(key, new DiffFragment(fragment, contentType));
  }
  
  /**
   * Analyzes the fragment and returns the content type.
   * 
   * @param fragment The framgent to analyze.
   * 
   * @return One of {@link ContentTypes}
   */
  private String getContentType(String fragment) {
    String trim = fragment.trim();
    BufferedReader r = new BufferedReader(new StringReader(trim));
    String line = null;
    int lineCounter = 0;
    int tagCharCounter = 0;
    boolean isXML = false;
    try {
      while ((line = r.readLine()) != null) {
        if (line.length() > 0) {
          lineCounter ++;

          int indexOf = line.indexOf("<");
          while (indexOf != -1) {
            tagCharCounter ++;
            indexOf = line.indexOf("<", indexOf + 1);
          }
          
          indexOf = line.indexOf(">");
          while (indexOf != -1) {
            tagCharCounter ++;
            indexOf = line.indexOf(">", indexOf + 1);
          }
        }
      }
      
      if (logger.isDebugEnabled()) {
        logger.debug("lineCounter " + lineCounter);
        logger.debug("startTagCounter " + tagCharCounter);
        logger.debug("trim.length() " + trim.length());
      }
      
      
      if (lineCounter < 2) {
        // If the XML is inline consider a 'well studied' ratio.
        int aproxLines = Math.max(trim.length() / 50, 1); 
        if (tagCharCounter / aproxLines >=2) {
          isXML = true;
        }
      } else {
        if (tagCharCounter >= lineCounter - 1) {
          isXML = true;
        }
      }
      
    } catch (IOException e) {
      logger.error(e, e);
    }
    
    return isXML ? ContentTypes.XML_CONTENT_TYPE : ContentTypes.PLAIN_TEXT_CONTENT_TYPE;
  }
  
  /**
   * Checks of the fragment is present in the cache.
   * 
   * @param key The key that identifies a fragment.
   * 
   * @return <code>true</code> if the key is present in the map.
   */
  public boolean contains(int key) {
    return cache.containsKey(key);
  }
  
  /**
   * Clears all recorded fragments.
   */
  public void dispose() {
    cache.clear();
  }
  
  /**
   * Builds an URL to identify the given key.
   * 
   * @param key The key.
   * @param host Something to put as host name.
   * 
   * @return Teh URL form.
   * 
   * @throws MalformedURLException Problems building the URL.
   */
  public URL cache(String fragment, String host) throws MalformedURLException {
    int key = fragment.hashCode();
    if (!contains(key)) {
      
      put(key, fragment);
    }
    
    return build(DIFF_PROTOCOL, key, host);
  }
  
  /**
   * Gets the fragment identified by this URL.
   * 
   * @param u The URL.
   * 
   * @return The fragment or <code>null</code>.
   */
  public String getFragment(URL u) {
    String fragment = null;
    
    String fileName = URLUtil.extractFileName(u);
    String extension = URLUtil.getExtension(fileName);

    String idAsString = fileName.substring(0, fileName.length() - extension.length() - 1);

    try {
      int key = Integer.parseInt(idAsString);

      DiffFragment diffFragment = cache.get(key);
      if (diffFragment != null) {
        fragment = diffFragment.getContent();
      }

    } catch (Throwable t) {
      logger.error(t, t);
    }
    
    return fragment;
  }
  
  /**
   * Builds an URL to identify a fragment with teh given key.
   * 
   * @param key The key.
   * @param host Something to put as host name.
   * 
   * @return The URL form.
   * 
   * @throws MalformedURLException Problems building the URL.
   */
  public URL build(String protocol, int key, String host) throws MalformedURLException {
    DiffFragment diffFragment = cache.get(key);
    StringBuilder b = new StringBuilder(protocol);
    b.append(":/").append(host).append("/").append(key).append(".");
    if (diffFragment.getContentType().equals(ContentTypes.XML_CONTENT_TYPE)) {
      b.append("xml");
    } else {
      b.append("txt");
    }
    return new URL(b.toString());
  }
}
