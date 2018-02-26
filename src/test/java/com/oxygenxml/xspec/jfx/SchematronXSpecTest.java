package com.oxygenxml.xspec.jfx;

import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.xml.sax.InputSource;

import de.schlichtherle.io.File;
import net.sf.saxon.TransformerFactoryImpl;

/**
 * Some Schematron related tests.
 *  
 * @author alex_jitianu
 */
public class SchematronXSpecTest extends XSpecViewTestBase {

  /**
   * The same XPath used to create a label is used by us, afterwards, to pin-point the element.
   * If it changes, we should update it. 
   */
  public void testLocalizationXPath() throws Exception {
    Source xslSource = new StreamSource(new StringReader(
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
        "<xsl:stylesheet xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\"\n" + 
        "    xmlns:xs=\"http://www.w3.org/2001/XMLSchema\"\n" + 
        "    exclude-result-prefixes=\"xs\"\n" + 
        "    version=\"2.0\">\n" + 
        "    <xsl:output omit-xml-declaration=\"yes\"></xsl:output>\n" + 
        "    <!--\n" + 
        "         <xsl:template name=\"make-label\">\n" + 
        "        <xsl:attribute name=\"label\" select=\"string-join((@label, tokenize(local-name(),'-')[.=('report','assert','not','rule')], @id, @role, @location, @context, current()[@count]/string('count:'), @count), ' ')\"/>\n" + 
        "    </xsl:template>\n" + 
        "        -->\n" + 
        "    <xsl:template match=\"text()\"/>\n" + 
        "    <xsl:template match=\"*:template[@name='make-label']/*:attribute[@name='label']\">\n" + 
        "        <xsl:value-of select=\"@select\"/>\n" + 
        "    </xsl:template>\n" + 
        "</xsl:stylesheet>"));
    Transformer transformer = new TransformerFactoryImpl().newTransformer(xslSource);
    Source xmlSource = new SAXSource(new InputSource(new File("frameworks\\xspec\\src\\schematron\\schut-to-xspec.xsl").toURI().toURL().toString()));
    StringWriter writer = new StringWriter();
    Result outputTarget = new StreamResult(writer);
    transformer.transform(xmlSource, outputTarget);
    
    assertEquals(
        "label generation Xpath changed. Please update it inside com.oxygenxml.xspec.jfx.bridge.Bridge.showTestAWT(String, String, String)",
        "string-join((@label, tokenize(local-name(),'-')[.=('report','assert','not','rule')], @id, @role, @location, @context, current()[@count]/string('count:'), @count), ' ')", 
        writer.toString());
  }
}
