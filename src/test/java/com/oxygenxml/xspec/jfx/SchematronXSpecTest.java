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
        "    <xsl:output method=\"text\"></xsl:output>\n" + 
        "    <!--\n" + 
        "         <xsl:template name=\"make-label\">\n" + 
        "        <xsl:attribute name=\"label\" select=\"string-join((@label, tokenize(local-name(),'-')[.=('report','assert','not','rule')], @id, @role, @location, @context, current()[@count]/string('count:'), @count), ' ')\"/>\n" + 
        "    </xsl:template>\n" + 
        "        -->\n" + 
        "    <xsl:template match=\"text()\"/>\n" + 
        "    <xsl:template match=\"xsl:template[@name='create-expect']/xsl:param[@name='label']\">\n" + 
        "        <xsl:value-of select=\"normalize-space(@select)\"/>\n" + 
        "    </xsl:template>\n" + 
        "</xsl:stylesheet>"));
    Transformer transformer = new TransformerFactoryImpl().newTransformer(xslSource);
    Source xmlSource = new SAXSource(new InputSource(new File("frameworks/xspec/src/schematron/schut-to-xspec.xsl").toURI().toURL().toString()));
    StringWriter writer = new StringWriter();
    Result outputTarget = new StreamResult(writer);
    transformer.transform(xmlSource, outputTarget);
    
    assertEquals(
        "label generation Xpath changed. Please update it inside com.oxygenxml.xspec.jfx.bridge.Bridge.showTestAWT(String, String, String)",
        "( @label, tokenize(local-name(), '-')[. = ('report', 'assert', 'not', 'rule')], @id, @role, @location, @context, (@count ! ('count:', .)) ) => string-join(' ')", 
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
    URL schCompiledURL = new File(URLUtil.getCanonicalFileFromFileUrl(xmlURL).getParentFile(), "xspec/demo-sch-preprocessed.xsl").toURI().toURL();
    File xspecFile = URLUtil.getCanonicalFileFromFileUrl(xspecURL);
    File outputFile = new File(xspecFile.getParentFile(), "demo-report.html");
    
    executeANT(xspecFile, outputFile,"", true);
    
    File xmlFormatOutput = new File(xspecFile.getParentFile(), "xspec/demo-result.xml");
    String replaceAll = read(xmlFormatOutput.toURI().toURL()).toString().replaceAll("date=\".*\"", "date=\"XXX\"").replaceAll("<\\?xml-stylesheet.*\\?>", "");
    replaceAll = replaceAll.replace('\u0009', ' ').replace('\u00A0', ' ');
    
    // XSpecUtil.generateId("No escaping bad(0)")
    
    // demo-02
    
    System.out.println("+++++ " + XSpecUtil.generateId("demo-02(1)"));
    
    assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
        "<report xmlns=\"http://www.jenitennison.com/xslt/xspec\"\n" +
        "        xspec=\"" + xspecURL.toExternalForm() + "\"\n" + 
        "        stylesheet=\"" + schCompiledURL.toString() + "\"\n" + 
        "        schematron=\"" + schURL.toExternalForm() + "\"\n" + 
        "        date=\"XXX\">\n" +
        "   <scenario id=\"" + XSpecUtil.generateId("demo-01(0)") + "\"\n" + 
        "             xspec=\"" + xspecURL.toString() + "\">\n" + 
        "      <label>demo-01</label>\n" + 
        "      <input-wrap xmlns=\"\">\n" + 
        "         <x:context xmlns:x=\"http://www.jenitennison.com/xslt/xspec\"\n" + 
        "                    href=\"" + xmlURL.toString() + "\"/>\n" + 
        "      </input-wrap>\n" + 
        "      <scenario id=\"" + XSpecUtil.generateId("demo-01(0) / article should have a title(0)") + "\"\n" + 
        "                xspec=\"" + xspecURL.toString() + "\">\n" + 
        "         <label>article should have a title</label>\n" + 
        "         <result select=\"/element()\">\n" + 
        "            <content-wrap xmlns=\"\">\n" + 
        "               <svrl:schematron-output xmlns:svrl=\"http://purl.oclc.org/dsdl/svrl\"\n" + 
        "                                       xmlns:xs=\"http://www.w3.org/2001/XMLSchema\"\n" + 
        "                                       xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\"\n" + 
        "                                       xmlns:saxon=\"http://saxon.sf.net/\"\n" + 
        "                                       xmlns:schold=\"http://www.ascc.net/xml/schematron\"\n" + 
        "                                       xmlns:iso=\"http://purl.oclc.org/dsdl/schematron\"\n" + 
        "                                       xmlns:xhtml=\"http://www.w3.org/1999/xhtml\"\n" + 
        "                                       title=\"\"\n" + 
        "                                       schemaVersion=\"\"><!--   \n" + 
        "     \n" + 
        "     \n" + 
        "   -->\n" + 
        "                  <svrl:active-pattern document=\"" + xmlURL.toString() + "\"/>\n" + 
        "                  <svrl:fired-rule context=\"article\"/>\n" + 
        "                  <svrl:fired-rule context=\"section\"/>\n" + 
        "                  <svrl:fired-rule context=\"section\"/>\n" + 
        "                  <svrl:failed-assert test=\"title\" id=\"a002\" location=\"/article[1]/section[2]\">\n" + 
        "                     <svrl:text>\n" + 
        "                section should have a title\n" + 
        "            </svrl:text>\n" + 
        "                  </svrl:failed-assert>\n" + 
        "               </svrl:schematron-output>\n" + 
        "            </content-wrap>\n" + 
        "         </result>\n" + 
        "         <test id=\"" + XSpecUtil.generateId("demo-01(0) / article should have a title(0)") + "-expect1\" successful=\"true\">\n" + 
        "            <label>Some other thing not assert a001</label>\n" + 
        "            <expect-test-wrap xmlns=\"\">\n" + 
        "               <x:expect xmlns:x=\"http://www.jenitennison.com/xslt/xspec\"\n" + 
        "                         test=\"Q{http://purl.oclc.org/dsdl/svrl}schematron-output[Q{http://purl.oclc.org/dsdl/svrl}fired-rule] and empty(Q{http://purl.oclc.org/dsdl/svrl}schematron-output/Q{http://purl.oclc.org/dsdl/svrl}failed-assert[(@id, preceding-sibling::Q{http://purl.oclc.org/dsdl/svrl}fired-rule[1]/@id, preceding-sibling::Q{http://purl.oclc.org/dsdl/svrl}active-pattern[1]/@id)[1] = 'a001'])\"/>\n" + 
        "            </expect-test-wrap>\n" + 
        "            <expect select=\"()\"/>\n" + 
        "         </test>\n" + 
        "      </scenario>\n" + 
        "   </scenario>\n" + 
        "   <scenario id=\"" + XSpecUtil.generateId("demo-02(1)") + "\"\n" + 
        "             xspec=\"" + xspecModuleURL.toString() + "\">\n" + 
        "      <label>demo-02</label>\n" + 
        "      <input-wrap xmlns=\"\">\n" + 
        "         <x:context xmlns:x=\"http://www.jenitennison.com/xslt/xspec\"\n" + 
        "                    href=\"" + xmlURL.toString() + "\"/>\n" + 
        "      </input-wrap>\n" + 
        "      <scenario id=\"" + XSpecUtil.generateId("demo-02(1) / article should have a title(0)") + "\"\n" + 
        "                xspec=\"" + xspecModuleURL.toString() + "\">\n" + 
        "         <label>article should have a title</label>\n" + 
        "         <result select=\"/element()\">\n" + 
        "            <content-wrap xmlns=\"\">\n" + 
        "               <svrl:schematron-output xmlns:svrl=\"http://purl.oclc.org/dsdl/svrl\"\n" + 
        "                                       xmlns:xs=\"http://www.w3.org/2001/XMLSchema\"\n" + 
        "                                       xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\"\n" + 
        "                                       xmlns:saxon=\"http://saxon.sf.net/\"\n" + 
        "                                       xmlns:schold=\"http://www.ascc.net/xml/schematron\"\n" + 
        "                                       xmlns:iso=\"http://purl.oclc.org/dsdl/schematron\"\n" + 
        "                                       xmlns:xhtml=\"http://www.w3.org/1999/xhtml\"\n" + 
        "                                       title=\"\"\n" + 
        "                                       schemaVersion=\"\"><!--   \n" + 
        "     \n" + 
        "     \n" + 
        "   -->\n" + 
        "                  <svrl:active-pattern document=\"" + xmlURL.toString() + "\"/>\n" + 
        "                  <svrl:fired-rule context=\"article\"/>\n" + 
        "                  <svrl:fired-rule context=\"section\"/>\n" + 
        "                  <svrl:fired-rule context=\"section\"/>\n" + 
        "                  <svrl:failed-assert test=\"title\" id=\"a002\" location=\"/article[1]/section[2]\">\n" + 
        "                     <svrl:text>\n" + 
        "                section should have a title\n" + 
        "            </svrl:text>\n" + 
        "                  </svrl:failed-assert>\n" + 
        "               </svrl:schematron-output>\n" + 
        "            </content-wrap>\n" + 
        "         </result>\n" + 
        "         <test id=\"" + XSpecUtil.generateId("demo-02(1) / article should have a title(0)") + "-expect1\" successful=\"true\">\n" + 
        "            <label>Some other thing not assert a001</label>\n" + 
        "            <expect-test-wrap xmlns=\"\">\n" + 
        "               <x:expect xmlns:x=\"http://www.jenitennison.com/xslt/xspec\"\n" + 
        "                         test=\"Q{http://purl.oclc.org/dsdl/svrl}schematron-output[Q{http://purl.oclc.org/dsdl/svrl}fired-rule] and empty(Q{http://purl.oclc.org/dsdl/svrl}schematron-output/Q{http://purl.oclc.org/dsdl/svrl}failed-assert[(@id, preceding-sibling::Q{http://purl.oclc.org/dsdl/svrl}fired-rule[1]/@id, preceding-sibling::Q{http://purl.oclc.org/dsdl/svrl}active-pattern[1]/@id)[1] = 'a001'])\"/>\n" + 
        "            </expect-test-wrap>\n" + 
        "            <expect select=\"()\"/>\n" + 
        "         </test>\n" + 
        "      </scenario>\n" + 
        "      <scenario id=\"" + XSpecUtil.generateId("demo-02(1) / section should have a title(1)") + "\"\n" + 
        "                xspec=\"" + xspecModuleURL.toString() + "\">\n" + 
        "         <label>section should have a title</label>\n" + 
        "         <result select=\"/element()\">\n" + 
        "            <content-wrap xmlns=\"\">\n" + 
        "               <svrl:schematron-output xmlns:svrl=\"http://purl.oclc.org/dsdl/svrl\"\n" + 
        "                                       xmlns:xs=\"http://www.w3.org/2001/XMLSchema\"\n" + 
        "                                       xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\"\n" + 
        "                                       xmlns:saxon=\"http://saxon.sf.net/\"\n" + 
        "                                       xmlns:schold=\"http://www.ascc.net/xml/schematron\"\n" + 
        "                                       xmlns:iso=\"http://purl.oclc.org/dsdl/schematron\"\n" + 
        "                                       xmlns:xhtml=\"http://www.w3.org/1999/xhtml\"\n" + 
        "                                       title=\"\"\n" + 
        "                                       schemaVersion=\"\"><!--   \n" + 
        "     \n" + 
        "     \n" + 
        "   -->\n" + 
        "                  <svrl:active-pattern document=\"" + xmlURL.toString() + "\"/>\n" + 
        "                  <svrl:fired-rule context=\"article\"/>\n" + 
        "                  <svrl:fired-rule context=\"section\"/>\n" + 
        "                  <svrl:fired-rule context=\"section\"/>\n" + 
        "                  <svrl:failed-assert test=\"title\" id=\"a002\" location=\"/article[1]/section[2]\">\n" + 
        "                     <svrl:text>\n" + 
        "                section should have a title\n" + 
        "            </svrl:text>\n" + 
        "                  </svrl:failed-assert>\n" + 
        "               </svrl:schematron-output>\n" + 
        "            </content-wrap>\n" + 
        "         </result>\n" + 
        "         <test id=\"" + XSpecUtil.generateId("demo-02(1) / section should have a title(1)") + "-expect1\" successful=\"true\">\n" + 
        "            <label>Do a thing not assert a002 /article[1]/section[1]</label>\n" + 
        "            <expect-test-wrap xmlns=\"\">\n" + 
        "               <x:expect xmlns:x=\"http://www.jenitennison.com/xslt/xspec\"\n" + 
        "                         test=\"Q{http://purl.oclc.org/dsdl/svrl}schematron-output[Q{http://purl.oclc.org/dsdl/svrl}fired-rule] and empty(Q{http://purl.oclc.org/dsdl/svrl}schematron-output/Q{http://purl.oclc.org/dsdl/svrl}failed-assert[(@id, preceding-sibling::Q{http://purl.oclc.org/dsdl/svrl}fired-rule[1]/@id, preceding-sibling::Q{http://purl.oclc.org/dsdl/svrl}active-pattern[1]/@id)[1] = 'a002'][($Q{http://www.jenitennison.com/xslt/xspec}context/root()/(/article[1]/section[1]) treat as node()) is Q{http://www.jenitennison.com/xslt/xspec}select-node($Q{http://www.jenitennison.com/xslt/xspec}context/root(), @location, preceding-sibling::Q{http://purl.oclc.org/dsdl/svrl}ns-prefix-in-attribute-values, 3)])\"/>\n" + 
        "            </expect-test-wrap>\n" + 
        "            <expect select=\"()\"/>\n" + 
        "         </test>\n" + 
        "         <test id=\"" + XSpecUtil.generateId("demo-02(1) / section should have a title(1)") + "-expect2\" successful=\"true\">\n" + 
        "            <label>Do something assert a002 /article[1]/section[2]</label>\n" + 
        "            <expect-test-wrap xmlns=\"\">\n" + 
        "               <x:expect xmlns:x=\"http://www.jenitennison.com/xslt/xspec\"\n" + 
        "                         test=\"exists(Q{http://purl.oclc.org/dsdl/svrl}schematron-output/Q{http://purl.oclc.org/dsdl/svrl}failed-assert[(@id, preceding-sibling::Q{http://purl.oclc.org/dsdl/svrl}fired-rule[1]/@id, preceding-sibling::Q{http://purl.oclc.org/dsdl/svrl}active-pattern[1]/@id)[1] = 'a002'][($Q{http://www.jenitennison.com/xslt/xspec}context/root()/(/article[1]/section[2]) treat as node()) is Q{http://www.jenitennison.com/xslt/xspec}select-node($Q{http://www.jenitennison.com/xslt/xspec}context/root(), @location, preceding-sibling::Q{http://purl.oclc.org/dsdl/svrl}ns-prefix-in-attribute-values, 3)])\"/>\n" + 
        "            </expect-test-wrap>\n" +
        "            <expect select=\"()\"/>\n" + 
        "         </test>\n" + 
        "      </scenario>\n" + 
        "   </scenario>\n" + 
        "</report>",
        filterTestElementId(replaceAll));
    
    
    assertTrue(outputFile.exists());
    
    
    File css = new File("frameworks/xspec/oxygen-results-view/test-report.css");
    File js = new File("frameworks/xspec/oxygen-results-view/test-report.js");
    
    
    String expected = read(outputFile.toURI().toURL()).toString();
    expected = expected.replaceAll("id=\".*?\"", "id=\"\"");
    assertEquals(
        "<html>\n" + 
        "   <head>\n" + 
        "      <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\"><link rel=\"stylesheet\" type=\"text/css\" href=\"" + css.toURI().toURL().toString()
        + "\"><script type=\"text/javascript\" src=\""
        + js.toURI().toURL().toString() + "\"></script></head>\n" + 
        "   <body>\n" + 
        "      <div class=\"testsuite\" data-name=\"demo-01\" id=\"\" data-xspec=\"" + xspecURL.toString() + "\" data-tests=\"1\" data-failures=\"0\">\n" + 
        "         <p style=\"margin:0px;\"><span>demo-01</span><span>&nbsp;</span><a class=\"button\" onclick=\"runScenario(this)\">Run</a></p>\n" + 
        "         <div class=\"testsuite\" data-name=\"article should have a title\" id=\"\" data-xspec=\"" + xspecURL.toString() + "\" data-tests=\"1\" data-failures=\"0\">\n" + 
        "            <p style=\"margin:0px;\"><span>article should have a title</span><span>&nbsp;</span><a class=\"button\" onclick=\"runScenario(this)\">Run</a></p>\n" + 
        "            <div class=\"testcase\" data-name=\"Some other thing not assert a001\">\n" + 
        "               <p class=\"passed\"><span class=\"test-passed\" onclick=\"toggleResult(this)\">Some other thing not assert a001</span><span>&nbsp;</span><a class=\"button\" onclick=\"showTest(this)\">Show</a></p>\n" + 
        "            </div>\n" + 
        "         </div>\n" + 
        "      </div>\n" + 
        "      <div class=\"testsuite\" data-name=\"demo-02\" id=\"\" data-xspec=\"" + xspecModuleURL.toString()  + "\" data-tests=\"3\" data-failures=\"0\">\n" + 
        "         <p style=\"margin:0px;\"><span>demo-02</span><span>&nbsp;</span><a class=\"button\" onclick=\"runScenario(this)\">Run</a></p>\n" + 
        "         <div class=\"testsuite\" data-name=\"article should have a title\" id=\"\" data-xspec=\"" + xspecModuleURL.toString() + "\" data-tests=\"1\" data-failures=\"0\">\n" + 
        "            <p style=\"margin:0px;\"><span>article should have a title</span><span>&nbsp;</span><a class=\"button\" onclick=\"runScenario(this)\">Run</a></p>\n" + 
        "            <div class=\"testcase\" data-name=\"Some other thing not assert a001\">\n" + 
        "               <p class=\"passed\"><span class=\"test-passed\" onclick=\"toggleResult(this)\">Some other thing not assert a001</span><span>&nbsp;</span><a class=\"button\" onclick=\"showTest(this)\">Show</a></p>\n" + 
        "            </div>\n" + 
        "         </div>\n" + 
        "         <div class=\"testsuite\" data-name=\"section should have a title\" id=\"\" data-xspec=\"" + xspecModuleURL.toString() + "\" data-tests=\"2\" data-failures=\"0\">\n" + 
        "            <p style=\"margin:0px;\"><span>section should have a title</span><span>&nbsp;</span><a class=\"button\" onclick=\"runScenario(this)\">Run</a></p>\n" + 
        "            <div class=\"testcase\" data-name=\"Do a thing not assert a002 /article[1]/section[1]\">\n" + 
        "               <p class=\"passed\"><span class=\"test-passed\" onclick=\"toggleResult(this)\">Do a thing not assert a002 /article[1]/section[1]</span><span>&nbsp;</span><a class=\"button\" onclick=\"showTest(this)\">Show</a></p>\n" + 
        "            </div>\n" + 
        "            <div class=\"testcase\" data-name=\"Do something assert a002 /article[1]/section[2]\">\n" + 
        "               <p class=\"passed\"><span class=\"test-passed\" onclick=\"toggleResult(this)\">Do something assert a002 /article[1]/section[2]</span><span>&nbsp;</span><a class=\"button\" onclick=\"showTest(this)\">Show</a></p>\n" + 
        "            </div>\n" + 
        "         </div>\n" + 
        "      </div>\n" + 
        "   </body>\n" + 
        "</html>", expected);
    
  }
}
