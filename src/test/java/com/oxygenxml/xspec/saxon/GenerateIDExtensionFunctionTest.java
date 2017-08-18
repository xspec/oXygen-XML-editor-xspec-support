package com.oxygenxml.xspec.saxon;

import java.io.File;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.xml.sax.InputSource;

import junit.framework.TestCase;
import net.sf.saxon.Configuration;
import net.sf.saxon.TransformerFactoryImpl;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.XPathCompiler;
import net.sf.saxon.s9api.XPathExecutable;
import net.sf.saxon.s9api.XPathSelector;
import net.sf.saxon.s9api.XdmItem;
import ro.sync.util.URLUtil;

/**
 * Tests the ID generation.
 *  
 * @author alex_jitianu
 */
public class GenerateIDExtensionFunctionTest extends TestCase {

  /**
   * Nested scenarios with the same label will receive different labels.
   * 
   * @throws Exception
   */
  public void testNesting() throws Exception {
    URL xspecURL = getClass().getClassLoader().getResource("#20/math.xspec");
    URL xslURL = new File("frameworks/xspec/oxygen-results-view/generate-xspec-tests-oxygen.xsl").toURI().toURL();
    File xspecFile = URLUtil.getCanonicalFileFromFileUrl(xspecURL);
    
    File compiledXSLFile = new File(xspecFile.getParentFile(), "math-compiled.xsl");
    
    Configuration userSpecifiedConfiguration = null;
    userSpecifiedConfiguration = Configuration.newConfiguration();
    userSpecifiedConfiguration.registerExtensionFunction(new GenerateIDExtensionFunction());
    
    TransformerFactoryImpl newInstance = new TransformerFactoryImpl(userSpecifiedConfiguration);
    
    // A way of intercepting xsl:messages
//    SaxonMessageWriter instance = SaxonMessageWriter.getInstance();
//    instance.setMessageListener(new XSLMessageListener() {
//      @Override
//      public void message(DocumentPositionedInfo message) {
//        System.out.println(message);
//      }
//    });
    
    newInstance.setAttribute(
        net.sf.saxon.lib.FeatureKeys.MESSAGE_EMITTER_CLASS, SaxonMessageEmitter.class.getName());
    
    Source source = new SAXSource(new InputSource(xslURL.toString()));
    Transformer transformer = newInstance.newTransformer(source);
    
    Source xmlSource = new StreamSource(xspecFile);
    Result outputTarget = new StreamResult(compiledXSLFile);
    
    transformer.transform(xmlSource, outputTarget);
    
    // Make sure there are no templates with the same name.
    Processor p = new Processor(false);
    XPathCompiler compiler = p.newXPathCompiler();
    XPathExecutable exec = compiler.compile("doc(\"" + compiledXSLFile.toURI().toURL().toString()
        + "\")//xsl:template/xs:string(@name)");
    XPathSelector load = exec.load();
    
    Set<String> s = new HashSet<String>();
    
    for (XdmItem xdmItem : load) {
      String templateName = xdmItem.getStringValue();
      assertFalse("Two templates have the same name detected: " + templateName, s.contains(templateName));
      s.add(templateName);
    }
  }
}
