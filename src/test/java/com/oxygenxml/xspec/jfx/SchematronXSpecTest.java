package com.oxygenxml.xspec.jfx;

import java.io.File;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.xml.sax.InputSource;

import com.oxygenxml.xspec.XSpecUtil;

import net.sf.saxon.TransformerFactoryImpl;
import ro.sync.util.URLUtil;

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
    Source xmlSource = new SAXSource(new InputSource(new File("frameworks/xspec/src/schematron/schut-to-xspec.xsl").toURI().toURL().toString()));
    StringWriter writer = new StringWriter();
    Result outputTarget = new StreamResult(writer);
    transformer.transform(xmlSource, outputTarget);
    
    assertEquals(
        "label generation Xpath changed. Please update it inside com.oxygenxml.xspec.jfx.bridge.Bridge.showTestAWT(String, String, String)",
        "string-join((@label, tokenize(local-name(),'-')[.=('report','assert','not','rule')], @id, @role, @location, @context, current()[@count]/string('count:'), @count), ' ')", 
        writer.toString());
  }
  
  /**
   * Tests that a Schematron XSpec is run and that the result file contains all the meta data we need
   * to localize the scenarios.
   * 
   * @throws Exception If it fails.
   */
  public void testMetaData() throws Exception {
    URL xspecURL = getClass().getClassLoader().getResource("schematron/demo.xspec");
    URL xspecModuleURL = getClass().getClassLoader().getResource("schematron/demo-module.xspec");
    
    URL xmlURL = getClass().getClassLoader().getResource("schematron/demo.xml");
    URL schURL = new File(URLUtil.getCanonicalFileFromFileUrl(xmlURL).getParentFile(), "demo.sch").toURI().toURL();
    URL schCompiledURL = new File(URLUtil.getCanonicalFileFromFileUrl(xmlURL).getParentFile(), "demo.sch-compiled.xsl").toURI().toURL();
    File xspecFile = URLUtil.getCanonicalFileFromFileUrl(xspecURL);
    File outputFile = new File(xspecFile.getParentFile(), "demo-report.html");
    
    executeANT(xspecFile, outputFile,"", true);
    
    File xmlFormatOutput = new File(xspecFile.getParentFile(), "xspec/demo-result.xml");
    String replaceAll = read(xmlFormatOutput.toURI().toURL()).toString().replaceAll("date=\".*\"", "date=\"XXX\"").replaceAll("<\\?xml-stylesheet.*\\?>", "");
    replaceAll = replaceAll.replace('\u0009', ' ').replace('\u00A0', ' ');
    
    // XSpecUtil.generateId("No escaping bad(0)")
    
    // demo-02
    
    System.out.println("+++++ " + XSpecUtil.generateId("demo-02(0)"));
    
    assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
        "\n" + 
        "<x:report xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\"\n" + 
        "          xmlns:xs=\"http://www.w3.org/2001/XMLSchema\"\n" + 
        "          xmlns:test=\"http://www.jenitennison.com/xslt/unit-test\"\n" + 
        "          xmlns:x=\"http://www.jenitennison.com/xslt/xspec\"\n" + 
        "          xmlns:svrl=\"http://purl.oclc.org/dsdl/svrl\"\n" + 
        "          stylesheet=\"" + schCompiledURL.toString() + "\"\n" + 
        "          date=\"XXX\"\n" +
        "          xspec=\"" + xspecURL.toExternalForm() + "\"\n" + 
        "          schematron=\"" + schURL.toExternalForm() + "\">\n" + 
        "   <x:scenario source=\"" + xspecModuleURL.toString() + "\"\n" + 
        "               template-id=\"" + XSpecUtil.generateId("demo-02(0)") + "\">\n" + 
        "      <x:label>demo-02</x:label>\n" + 
        "      <x:context href=\"" + xmlURL.toString() + "\"/>\n" + 
        "      <x:scenario source=\"" + xspecModuleURL.toString() + "\"\n" + 
        "                  template-id=\"" + XSpecUtil.generateId("demo-02(0) / article should have a title(0)")
        + "\">\n" + 
        "         <x:label>article should have a title</x:label>\n" + 
        "         <x:result>\n" + 
        "            <svrl:schematron-output xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\"\n" + 
        "                                    xmlns:saxon=\"http://saxon.sf.net/\"\n" + 
        "                                    xmlns:schold=\"http://www.ascc.net/xml/schematron\"\n" + 
        "                                    xmlns:iso=\"http://purl.oclc.org/dsdl/schematron\"\n" + 
        "                                    xmlns:xhtml=\"http://www.w3.org/1999/xhtml\"\n" + 
        "                                    title=\"\"\n" + 
        "                                    schemaVersion=\"\"><!--   \n" + 
        "     \n" + 
        "     \n" + 
        "   -->\n" + 
        "               <svrl:active-pattern document=\"" + xmlURL.toString() + "\"/>\n" + 
        "               <svrl:fired-rule context=\"article\"/>\n" + 
        "               <svrl:fired-rule context=\"section\"/>\n" + 
        "               <svrl:fired-rule context=\"section\"/>\n" + 
        "               <svrl:failed-assert test=\"title\" id=\"a002\" location=\"/article[1]/section[2]\">\n" + 
        "                  <svrl:text>\n" + 
        "                section should have a title\n" + 
        "            </svrl:text>\n" + 
        "               </svrl:failed-assert>\n" + 
        "            </svrl:schematron-output>\n" + 
        "         </x:result>\n" + 
        "         <x:test successful=\"true\">\n" + 
        "            <x:label>Some other thing not assert a001</x:label>\n" + 
        "            <x:expect select=\"()\"/>\n" + 
        "         </x:test>\n" + 
        "      </x:scenario>\n" + 
        "      <x:scenario source=\"" + xspecModuleURL.toString() + "\"\n" + 
        "                  template-id=\"" + XSpecUtil.generateId("demo-02(0) / section should have a title(1)")
        + "\">\n" + 
        "         <x:label>section should have a title</x:label>\n" + 
        "         <x:result>\n" + 
        "            <svrl:schematron-output xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\"\n" + 
        "                                    xmlns:saxon=\"http://saxon.sf.net/\"\n" + 
        "                                    xmlns:schold=\"http://www.ascc.net/xml/schematron\"\n" + 
        "                                    xmlns:iso=\"http://purl.oclc.org/dsdl/schematron\"\n" + 
        "                                    xmlns:xhtml=\"http://www.w3.org/1999/xhtml\"\n" + 
        "                                    title=\"\"\n" + 
        "                                    schemaVersion=\"\"><!--   \n" + 
        "     \n" + 
        "     \n" + 
        "   -->\n" + 
        "               <svrl:active-pattern document=\"" + xmlURL.toString() + "\"/>\n" + 
        "               <svrl:fired-rule context=\"article\"/>\n" + 
        "               <svrl:fired-rule context=\"section\"/>\n" + 
        "               <svrl:fired-rule context=\"section\"/>\n" + 
        "               <svrl:failed-assert test=\"title\" id=\"a002\" location=\"/article[1]/section[2]\">\n" + 
        "                  <svrl:text>\n" + 
        "                section should have a title\n" + 
        "            </svrl:text>\n" + 
        "               </svrl:failed-assert>\n" + 
        "            </svrl:schematron-output>\n" + 
        "         </x:result>\n" + 
        "         <x:test successful=\"true\">\n" + 
        "            <x:label>Do a thing not assert a002 /article[1]/section[1]</x:label>\n" + 
        "            <x:expect select=\"()\"/>\n" + 
        "         </x:test>\n" + 
        "         <x:test successful=\"true\">\n" + 
        "            <x:label>Do something assert a002 /article[1]/section[2]</x:label>\n" + 
        "            <x:expect select=\"()\"/>\n" + 
        "         </x:test>\n" + 
        "      </x:scenario>\n" + 
        "   </x:scenario>\n" + 
        "   <x:scenario source=\"" + xspecURL.toString() + "\"\n" + 
        "               template-id=\"" + XSpecUtil.generateId("demo-01(1)") + "\">\n" + 
        "      <x:label>demo-01</x:label>\n" + 
        "      <x:context href=\"" + xmlURL.toString() + "\"/>\n" + 
        "      <x:scenario source=\"" + xspecURL.toString() + "\"\n" + 
        "                  template-id=\"" + XSpecUtil.generateId("demo-01(1) / article should have a title(0)")
        + "\">\n" + 
        "         <x:label>article should have a title</x:label>\n" + 
        "         <x:result>\n" + 
        "            <svrl:schematron-output xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\"\n" + 
        "                                    xmlns:saxon=\"http://saxon.sf.net/\"\n" + 
        "                                    xmlns:schold=\"http://www.ascc.net/xml/schematron\"\n" + 
        "                                    xmlns:iso=\"http://purl.oclc.org/dsdl/schematron\"\n" + 
        "                                    xmlns:xhtml=\"http://www.w3.org/1999/xhtml\"\n" + 
        "                                    title=\"\"\n" + 
        "                                    schemaVersion=\"\"><!--   \n" + 
        "     \n" + 
        "     \n" + 
        "   -->\n" + 
        "               <svrl:active-pattern document=\"" + xmlURL.toString() + "\"/>\n" + 
        "               <svrl:fired-rule context=\"article\"/>\n" + 
        "               <svrl:fired-rule context=\"section\"/>\n" + 
        "               <svrl:fired-rule context=\"section\"/>\n" + 
        "               <svrl:failed-assert test=\"title\" id=\"a002\" location=\"/article[1]/section[2]\">\n" + 
        "                  <svrl:text>\n" + 
        "                section should have a title\n" + 
        "            </svrl:text>\n" + 
        "               </svrl:failed-assert>\n" + 
        "            </svrl:schematron-output>\n" + 
        "         </x:result>\n" + 
        "         <x:test successful=\"true\">\n" + 
        "            <x:label>Some other thing not assert a001</x:label>\n" + 
        "            <x:expect select=\"()\"/>\n" + 
        "         </x:test>\n" + 
        "      </x:scenario>\n" + 
        "   </x:scenario>\n" + 
        "</x:report>", 
        replaceAll);
    
    
    assertTrue(outputFile.exists());
    
    
    File css = new File("frameworks/xspec/oxygen-results-view/test-report.css");
    File js = new File("frameworks/xspec/oxygen-results-view/test-report.js");
    
    
    String expected = read(outputFile.toURI().toURL()).toString();
    expected = expected.replaceAll("template-id=\".*?\"", "template-id=\"\"");
    assertEquals(
        "<html xmlns:test=\"http://www.jenitennison.com/xslt/unit-test\">\n" + 
        "   <head>\n" + 
        "      <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\"><link rel=\"stylesheet\" type=\"text/css\" href=\"" + css.toURI().toURL().toString()
        + "\"><script type=\"text/javascript\" src=\""
        + js.toURI().toURL().toString() + "\"></script></head>\n" + 
        "   <body>\n" + 
        "      <div class=\"testsuite\" data-name=\"demo-02\" template-id=\"\" data-source=\"" + xspecModuleURL.toString()  + "\" data-tests=\"3\" data-failures=\"0\">\n" + 
        "         <p style=\"margin:0px;\"><span>demo-02</span><span>&nbsp;</span><a class=\"button\" onclick=\"runScenario(this)\">Run</a></p>\n" + 
        "         <div class=\"testsuite\" data-name=\"article should have a title\" template-id=\"\" data-source=\"" + xspecModuleURL.toString() + "\" data-tests=\"1\" data-failures=\"0\">\n" + 
        "            <p style=\"margin:0px;\"><span>article should have a title</span><span>&nbsp;</span><a class=\"button\" onclick=\"runScenario(this)\">Run</a></p>\n" + 
        "            <div class=\"testcase\" data-name=\"Some other thing not assert a001\">\n" + 
        "               <p class=\"passed\"><span class=\"test-passed\" onclick=\"toggleResult(this)\">Some other thing not assert a001</span><span>&nbsp;</span><a class=\"button\" onclick=\"showTest(this)\">Show</a></p>\n" + 
        "            </div>\n" + 
        "         </div>\n" + 
        "         <div class=\"testsuite\" data-name=\"section should have a title\" template-id=\"\" data-source=\"" + xspecModuleURL.toString() + "\" data-tests=\"2\" data-failures=\"0\">\n" + 
        "            <p style=\"margin:0px;\"><span>section should have a title</span><span>&nbsp;</span><a class=\"button\" onclick=\"runScenario(this)\">Run</a></p>\n" + 
        "            <div class=\"testcase\" data-name=\"Do a thing not assert a002 /article[1]/section[1]\">\n" + 
        "               <p class=\"passed\"><span class=\"test-passed\" onclick=\"toggleResult(this)\">Do a thing not assert a002 /article[1]/section[1]</span><span>&nbsp;</span><a class=\"button\" onclick=\"showTest(this)\">Show</a></p>\n" + 
        "            </div>\n" + 
        "            <div class=\"testcase\" data-name=\"Do something assert a002 /article[1]/section[2]\">\n" + 
        "               <p class=\"passed\"><span class=\"test-passed\" onclick=\"toggleResult(this)\">Do something assert a002 /article[1]/section[2]</span><span>&nbsp;</span><a class=\"button\" onclick=\"showTest(this)\">Show</a></p>\n" + 
        "            </div>\n" + 
        "         </div>\n" + 
        "      </div>\n" + 
        "      <div class=\"testsuite\" data-name=\"demo-01\" template-id=\"\" data-source=\"" + xspecURL.toString() + "\" data-tests=\"1\" data-failures=\"0\">\n" + 
        "         <p style=\"margin:0px;\"><span>demo-01</span><span>&nbsp;</span><a class=\"button\" onclick=\"runScenario(this)\">Run</a></p>\n" + 
        "         <div class=\"testsuite\" data-name=\"article should have a title\" template-id=\"\" data-source=\"" + xspecURL.toString() + "\" data-tests=\"1\" data-failures=\"0\">\n" + 
        "            <p style=\"margin:0px;\"><span>article should have a title</span><span>&nbsp;</span><a class=\"button\" onclick=\"runScenario(this)\">Run</a></p>\n" + 
        "            <div class=\"testcase\" data-name=\"Some other thing not assert a001\">\n" + 
        "               <p class=\"passed\"><span class=\"test-passed\" onclick=\"toggleResult(this)\">Some other thing not assert a001</span><span>&nbsp;</span><a class=\"button\" onclick=\"showTest(this)\">Show</a></p>\n" + 
        "            </div>\n" + 
        "         </div>\n" + 
        "      </div>\n" + 
        "   </body>\n" + 
        "</html>", expected);
    
  }
  
  /**
   * The scenario contains inline XML. Schematron pre-parsing stages extracts it as an external XML.
   * Our build file accidently removed these generated files. 
   * 
   * <pre>
   * <x:scenario label="valid">
   *             <x:context>
   *                 <document>
   *                     <title>Introduction</title>
   *                     <p>some paragraph text</p>
   *                 </document>
   *             </x:context>
   *             <x:expect-not-assert id="a0001"/>
   *             <x:expect-valid/>
   *  </x:scenario>
   *  </pre>
   * @throws Exception
   */
  public void testInlineContext() throws Exception {
    URL xspecURL = getClass().getClassLoader().getResource("sch-inline-context/demo-03.xspec");
    
    File xspecFile = URLUtil.getCanonicalFileFromFileUrl(xspecURL);
    File outputFile = new File(xspecFile.getParentFile(), "demo-03-report.html");
    
    executeANT(xspecFile, outputFile,"", true);
    
    File xmlFormatOutput = new File(xspecFile.getParentFile(), "xspec/demo-03-result.xml");
    String replaceAll = read(xmlFormatOutput.toURI().toURL()).toString().replaceAll("date=\".*\"", "date=\"XXX\"").replaceAll("<\\?xml-stylesheet.*\\?>", "");
    replaceAll = replaceAll.replace('\u0009', ' ').replace('\u00A0', ' ');
    
    String getContext = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
        "<xsl:stylesheet xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\"\n" + 
        "    xmlns:xs=\"http://www.w3.org/2001/XMLSchema\"\n" + 
        "    exclude-result-prefixes=\"xs\"\n" + 
        "    version=\"2.0\">\n" + 
        "    <xsl:output omit-xml-declaration=\"yes\"/>\n" + 
        "    <xsl:template match=\"*:context\">\n" + 
        "        <xsl:value-of select=\"@href\"/>\n" + 
        "    </xsl:template>\n" + 
        "    <xsl:template match=\"text()\"/>\n" + 
        "</xsl:stylesheet>";
    Transformer newTransformer = new TransformerFactoryImpl().newTransformer(new StreamSource(new StringReader(getContext)));
    StringWriter writer = new StringWriter();
    Result outputTarget = new StreamResult(writer);
    newTransformer.transform(new SAXSource(new InputSource(xmlFormatOutput.toURI().toURL().toString())), outputTarget);
    
    File contextFile = URLUtil.getCanonicalFileFromFileUrl(new URL(writer.toString()));
    assertTrue("The generated file must exist", contextFile.exists());
  }
}
