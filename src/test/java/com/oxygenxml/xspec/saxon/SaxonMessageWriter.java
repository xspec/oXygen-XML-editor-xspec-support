package com.oxygenxml.xspec.saxon;

/*
 *  The Syncro Soft SRL License
 *
 *  Copyright (c) 1998-2007 Syncro Soft SRL, Romania.  All rights
 *  reserved.
 *
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions
 *  are met:
 *
 *  1. Redistribution of source or in binary form is allowed only with
 *  the prior written permission of Syncro Soft SRL.
 *
 *  2. Redistributions of source code must retain the above copyright
 *  notice, this list of conditions and the following disclaimer.
 *
 *  3. Redistributions in binary form must reproduce the above copyright
 *  notice, this list of conditions and the following disclaimer in
 *  the documentation and/or other materials provided with the
 *  distribution.
 *
 *  4. The end-user documentation included with the redistribution,
 *  if any, must include the following acknowledgment:
 *  "This product includes software developed by the
 *  Syncro Soft SRL (http://www.sync.ro/)."
 *  Alternately, this acknowledgment may appear in the software itself,
 *  if and wherever such third-party acknowledgments normally appear.
 *
 *  5. The names "Oxygen" and "Syncro Soft SRL" must
 *  not be used to endorse or promote products derived from this
 *  software without prior written permission. For written
 *  permission, please contact support@oxygenxml.com.
 *
 *  6. Products derived from this software may not be called "Oxygen",
 *  nor may "Oxygen" appear in their name, without prior written
 *  permission of the Syncro Soft SRL.
 *
 *  THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 *  WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 *  OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED.  IN NO EVENT SHALL THE SYNCRO SOFT SRL OR
 *  ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 *  SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 *  LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 *  USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 *  OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 *  OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 *  SUCH DAMAGE.
 */

import java.io.IOException;
import java.io.Writer;

import ro.sync.document.DocumentPositionedInfo;
import ro.sync.exml.plugin.transform.XSLMessageListener;

/**
 * Collects every message as it arrives from the transformation.
 */
public class SaxonMessageWriter extends Writer {
  /**
   * Collects characters send to it.
   */
  private StringBuilder buf = new StringBuilder();
  /**
   * The name of the engine.
   */
  private String engineName;
  /**
   * Listener to delegate the messages to.
   */
  private XSLMessageListener messageListener;
  /**
   * The singleton instance;
   */
  private static SaxonMessageWriter instance;
  /**
   * Private constructor to ensure the singleton pattern.
   */
  private SaxonMessageWriter() {}
  
  /**
   * @return The singleton writer.
   */
  public static SaxonMessageWriter getInstance() {
  	if (instance == null) {
  		instance = new SaxonMessageWriter();
  	}
	  return instance;
  }
  
  /**
   * @param engineName Name of the engine that will be presented in the messages forwarded to the 
  */
  public void setEngineName(String engineName) {
  	this.engineName = engineName;
  }

  /**
   * @param messageListener The message listener to delegate the messages to.
   */
  public void setMessageListener(XSLMessageListener messageListener) {
  	this.messageListener = messageListener;
  }
  
  /**
   * Implement the write method of the abstract writer class. 
   * Actually just put the chars in a StringBuffer, and flush them on enter. 
   * 
   * @see java.io.Writer#write(char[], int, int)
   */
  @Override
  public void write(char[] cbuf, int off, int len) throws IOException {
    for (int i = off; i < off + len; i++) {
      char c = cbuf[i];
      // Append the char to the buffer.
      if (c == '\n') {
        // Flush on enter.
        flush();
      } else {
        buf.append(c);
      }
    }
  }
  
  /**
   * Flush the existing buffers.
   * 
   * @see java.io.Writer#flush()
   */
  @Override
  public void flush() throws IOException {
    if (buf.length() > 0) {
      // Create a document position info.
      DocumentPositionedInfo dpi = new DocumentPositionedInfo(
          DocumentPositionedInfo.NOT_KNOWN, 
          '[' + engineName + "] " + buf.toString(), 
          null, 
          DocumentPositionedInfo.NOT_KNOWN, 
          DocumentPositionedInfo.NOT_KNOWN);
      // Add the existing buffer content to the "messages" TEXT tab.
      if (messageListener != null) {
      	messageListener.message(dpi);
      }
      
      // Clear the buffer.
      buf.setLength(0);
    }
  }

  /**
   * On close we perform a supplementary flush.
   * 
   * @see java.io.Writer#close()
   */
  @Override
  public void close() throws IOException {
    flush();
  }
}