package com.oxygenxml.xspec.saxon;

import net.sf.saxon.serialize.MessageEmitter;

public class SaxonMessageEmitter extends MessageEmitter {
  
  public SaxonMessageEmitter() {
    System.out.println("!!!!");
    writer = SaxonMessageWriter.getInstance();
  }
  

}
