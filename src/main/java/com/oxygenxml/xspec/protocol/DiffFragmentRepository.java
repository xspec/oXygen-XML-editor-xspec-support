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
    int startTagCounter = 0;
    boolean isXML = false;
    try {
      while ((line = r.readLine()) != null) {
        lineCounter ++;
        
        int indexOf = line.indexOf("<");
        while (indexOf != -1) {
          startTagCounter ++;
          indexOf = line.indexOf("<", indexOf + 1);
        }
      }
      
      if (lineCounter < 2) {
        // If the XML is inline consider a 'well studied' ratio.
        int aproxLines = trim.length() / 50; 
        if (aproxLines != 0) {
          if (startTagCounter >= aproxLines && aproxLines > 5) {
            isXML = true;
          }
        }
      } else {
        if (startTagCounter >= lineCounter - 1 && lineCounter >= 3) {
          isXML = true;
        }
      }
      
      
    } catch (IOException e) {
      logger.error(e, e);
    }
    
    return isXML ? ContentTypes.XML_CONTENT_TYPE : ContentTypes.PLAIN_TEXT_CONTENT_TYPE;
  }
  
  /**
   * Gets the fragment associated with the given key.
   * 
   * @param key The key that identifies a fragment.
   * 
   * @return The fragment or null if the key is not mapped to any fragment.
   */
  public DiffFragment get(int key) {
    return cache.get(key);
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
    
    return DiffURLStreamHandler.build(key, host);
  }
}
