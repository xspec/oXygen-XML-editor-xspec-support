package com.oxygenxml.xspec.jfx;

import java.io.File;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.oxygenxml.xspec.XSpecUtil;

import ro.sync.util.URLUtil;

/**
 * Asserts the XSpec transformation meta data and the special report.
 *  
 * @author alex_jitianu
 */
public class XSpecMeta2Test extends XSpecViewTestBase {

  /**
   * An assert with match on attributes.
   * 
   * https://github.com/xspec/oXygen-XML-editor-xspec-support/issues/41
   * 
   * @throws Exception If it fails.
   */
  public void testRunXSpec() throws Exception {
    URL xspecURL = getClass().getClassLoader().getResource("#41/to-test.xspec");
    URL xslURL = getClass().getClassLoader().getResource("#41/to-test.xsl");
    File xspecFile = URLUtil.getCanonicalFileFromFileUrl(xspecURL);
    File outputFile = new File(xspecFile.getParentFile(), "to-test.html");
    
    executeANT(xspecFile, outputFile);
    
    String firstID = XSpecUtil.generateId("template name code-to-code");
    
    File xmlFormatOutput = new File(xspecFile.getParentFile(), "xspec/to-test-result.xml");
    
    assertEquals("<result xmlns=\"http://www.jenitennison.com/xslt/xspec\" select=\"/*/@*\">\n" + 
        "   <content-wrap xmlns=\"\">\n" + 
        "      <pseudo-attribute xmlns=\"http://www.jenitennison.com/xslt/xspec\" code=\"F\"/>\n" + 
        "      <pseudo-attribute xmlns=\"http://www.jenitennison.com/xslt/xspec\" codeSystem=\"null\"/>\n" + 
        "      <pseudo-attribute xmlns=\"http://www.jenitennison.com/xslt/xspec\" displayName=\"Vrouw\"/>\n" + 
        "   </content-wrap>\n" + 
        "</result>", executeXPath(xmlFormatOutput, "//*:result"));
    
    assertEquals("<expect xmlns=\"http://www.jenitennison.com/xslt/xspec\" select=\"/*/@*\">\n" + 
        "   <content-wrap xmlns=\"\">\n" + 
        "      <pseudo-attribute xmlns=\"http://www.jenitennison.com/xslt/xspec\" code=\"false\"/>\n" + 
        "      <pseudo-attribute xmlns=\"http://www.jenitennison.com/xslt/xspec\"\n" + 
        "                        codeSystem=\"2.16.840.1.113883.5.1\"/>\n" + 
        "      <pseudo-attribute xmlns=\"http://www.jenitennison.com/xslt/xspec\" displayName=\"Vrouw\"/>\n" + 
        "   </content-wrap>\n" + 
        "</expect>", executeXPath(xmlFormatOutput, "//*:expect"));
    
    
    
    assertTrue(outputFile.exists());
    
    File css = new File("frameworks/xspec/oxygen-results-view/test-report.css");
    File js = new File("frameworks/xspec/oxygen-results-view/test-report.js");
    
    
    String htmlContent = simplify(read(outputFile.toURI().toURL()).toString());
    Pattern compile = Pattern.compile("<pre class=\"embeded\\.diff\\.data\" style=\"display:none;\">.*</pre>");
    Matcher matcher = compile.matcher(htmlContent);
    matcher.find();
    String actual = matcher.group();
    
    assertEquals("<pre class=\"embeded.diff.data\" style=\"display:none;\"><div class=\"embeded.diff.result\" style=\"white-space:pre;\">&lt;wrapper code=\"F\" codeSystem=\"null\" displayName=\"Vrouw\"/&gt;</div><div class=\"embeded.diff.expected\" style=\"white-space:pre;\">&lt;wrapper code=\"false\" codeSystem=\"2.16.840.1.113883.5.1\" displayName=\"Vrouw\"/&gt;</div></pre>", actual);
  }

  private String simplify(String string) {
    return string.replaceAll("<span xmlns=\"http://www.w3.org/1999/xhtml\"", "<span");
  }
}
