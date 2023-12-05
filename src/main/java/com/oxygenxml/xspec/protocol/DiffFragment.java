package com.oxygenxml.xspec.protocol;

import ro.sync.diff.api.DiffContentTypes;

/**
 * Stores a fragment together with its content type.
 *  
 * @author alex_jitianu
 */
public class DiffFragment {
  /**
   * The content.
   */
  private String content;
  /**
   * The content type. One of {@link ContentTypes}
   */
  private String contentType;
  
  /**
   * Constructor.
   * 
   * @param content The content.
   * @param contentType The content type. One of {@link ContentTypes}
   */
  public DiffFragment(String content, String contentType) {
    this.content = content;
    this.contentType = contentType;
  }
  
  public String getContent() {
    return content;
  }
  
  public String getContentType() {
    return contentType;
  }
}
