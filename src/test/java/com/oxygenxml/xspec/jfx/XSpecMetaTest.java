package com.oxygenxml.xspec.jfx;

import java.io.File;
import java.net.URL;

import com.oxygenxml.xspec.XSpecUtil;

import ro.sync.util.URLUtil;

/**
 * Asserts the XSpec transformation meta data and the special report.
 *  
 * @author alex_jitianu
 */
public class XSpecMetaTest extends XSpecViewTestBase {

  /**
   * Run an XSpec file that has no failed asserts.
   * 
   * 1. To ensure back mapping we need to remember in which file a scenario was defined.
   * 
   * @throws Exception
   */
  public void testRunScenario_Passed() throws Exception {
    URL xspecURL = getClass().getClassLoader().getResource("meta/escape-for-regex.xspec");
    URL xslURL = getClass().getClassLoader().getResource("meta/escape-for-regex.xsl");
    File xspecFile = URLUtil.getCanonicalFileFromFileUrl(xspecURL);
    File outputFile = new File(xspecFile.getParentFile(), "escape-for-regex-report.html");
    
    executeANT(xspecFile, outputFile);
    
    String firstID = XSpecUtil.generateId("No escaping(0)");
    
    File xmlFormatOutput = new File(xspecFile.getParentFile(), "xspec/escape-for-regex-result.xml");
    String actualContent = read(xmlFormatOutput.toURI().toURL()).toString();
    assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
        "<report xmlns=\"http://www.jenitennison.com/xslt/xspec\"\n" + 
        "        xspec=\"" + xspecURL.toExternalForm() + "\"\n" + 
        "        stylesheet=\"" + xslURL.toString() + "\"\n" + 
        "        date=\"XXX\">\n" + 
        // The source attribute is present.
        // To ensure back mapping we need to remember in which file a scenario was defined.
        "   <scenario id=\"" + firstID + "\"\n" + 
        "             xspec=\"" + xspecURL.toString() + "\">\n" + 
        "      <label>No escaping</label>\n" + 
        "      <input-wrap xmlns=\"\">\n" + 
        "         <x:call xmlns:functx=\"http://www.functx.com\"\n" + 
        "                 xmlns:x=\"http://www.jenitennison.com/xslt/xspec\"\n" + 
        "                 function=\"functx:escape-for-regex\">\n" + 
        "            <x:param select=\"'Hello'\"/>\n" + 
        "         </x:call>\n" + 
        "      </input-wrap>\n" + 
        "      <result select=\"'Hello'\"/>\n" + 
        "      <test id=\"" + firstID + "-expect1\" successful=\"true\">\n" + 
        "         <label>Must not be escaped at all</label>\n" + 
        "         <expect select=\"'Hello'\"/>\n" + 
        "      </test>\n" + 
        "   </scenario>\n" + 
        "</report>", 
        filterAll(actualContent));
    
    
    assertTrue(outputFile.exists());
    
    
    File css = new File("frameworks/xspec/oxygen-results-view/test-report.css");
    File js = new File("frameworks/xspec/oxygen-results-view/test-report.js");
    
    
    assertEquals(
        "<!DOCTYPE HTML>\n"
        + "<html>\n" + 
        "   <head>\n" + 
        "      <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">\n" + 
        "      <link rel=\"stylesheet\" type=\"text/css\" href=\"" + css.toURI().toURL().toString()
        + "\"><script type=\"text/javascript\" src=\""
        + js.toURI().toURL().toString() + "\"></script></head>\n" + 
        "   <body>\n" + 
        "      <div class=\"testsuite\" data-name=\"No escaping\" id=\"" + firstID + "\" "
        
        + "data-xspec=\"" + xspecURL.toString() + "\" "
        + "data-tests=\"1\" "
        + "data-failures=\"0\">\n" + 
        "         <p style=\"margin:0px;\"><span>No escaping</span><span>&nbsp;</span><a class=\"button\" onclick=\"runScenario(this)\">Run</a></p>\n" + 
        "         <div class=\"testcase\" data-name=\"Must not be escaped at all\">\n" + 
        "            <p class=\"passed\"><span class=\"test-passed\" onclick=\"toggleResult(this)\">Must not be escaped at all</span><span>&nbsp;</span><a class=\"button\" onclick=\"showTest(this)\">Show</a></p>\n" + 
        "         </div>\n" + 
        "      </div>\n" + 
        "   </body>\n" + 
        "</html>", simplify(read(outputFile.toURI().toURL()).toString()));
  }

  /**
   * Run an XSpec file that has failed asserts.
   * 
   * 1. To ensure back mapping we need to remember in which file a scenario was defined.
   * 
   * @throws Exception
   */
  public void testRunScenario_Failed() throws Exception {
    URL xspecURL = getClass().getClassLoader().getResource("meta/test.xspec");
    URL xslURL = getClass().getClassLoader().getResource("meta/escape-for-regex.xsl");
    File xspecFile = URLUtil.getCanonicalFileFromFileUrl(xspecURL);
    File outputFile = new File(xspecFile.getParentFile(), "test.html");
    File xmlFormatOutput = new File(xspecFile.getParentFile(), "xspec/test-result.xml");
    
    URL importedXSpecURL = getClass().getClassLoader().getResource("meta/escape-for-regex.xspec");
    
    executeANT(xspecFile, outputFile);
    
    String firstID = XSpecUtil.generateId("When processing a list of phrases(0)");
    String secondID = XSpecUtil.generateId("No escaping(1)");
    
    
    assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
        "<report xmlns=\"http://www.jenitennison.com/xslt/xspec\"\n" + 
        "        xspec=\"" + xspecURL.toExternalForm() + "\"\n" + 
        "        stylesheet=\"" + xslURL.toString() + "\"\n" + 
        "        date=\"XXX\">\n" +
        "   <scenario id=\"" + firstID + "\"\n" + 
        "             xspec=\"" + xspecFile.toURI().toURL().toString() + "\">\n" + 
        "      <label>When processing a list of phrases</label>\n" + 
        "      <input-wrap xmlns=\"\">\n" + 
        "         <x:context xmlns:functx=\"http://www.functx.com\"\n" + 
        "                    xmlns:x=\"http://www.jenitennison.com/xslt/xspec\">\n" + 
        "            <phrases>\n" + 
        "               <phrase>Hello!</phrase>\n" + 
        "               <phrase>Goodbye!</phrase>\n" + 
        "               <phrase>(So long!)</phrase>\n" + 
        "            </phrases>\n" + 
        "         </x:context>\n" + 
        "      </input-wrap>\n" + 
        "      <result select=\"/element()\">\n" + 
        "         <content-wrap xmlns=\"\">\n" + 
        "            <phrases xmlns:functx=\"http://www.functx.com\">\n" + 
        "               <phrase status=\"changed\">Hello!</phrase>\n" + 
        "               <phrase status=\"changed\">Goodbye!</phrase>\n" + 
        "               <phrase status=\"same\">\\(So long!\\)</phrase>\n" + 
        "            </phrases>\n" + 
        "         </content-wrap>\n" + 
        "      </result>\n" + 
        "      <test id=\"" + firstID + "-expect1\" successful=\"true\">\n" + 
        "         <label>All phrase elements should remain</label>\n" + 
        "         <expect-test-wrap xmlns=\"\">\n" + 
        "            <x:expect xmlns:functx=\"http://www.functx.com\"\n" + 
        "                      xmlns:x=\"http://www.jenitennison.com/xslt/xspec\"\n" + 
        "                      test=\"count(phrases/phrase) = 3\"/>\n" + 
        "         </expect-test-wrap>\n" + 
        "         <expect select=\"()\"/>\n" + 
        "      </test>\n" + 
        "      <test id=\"" + firstID + "-expect2\" successful=\"false\">\n" + 
        "         <label>Strings should be escaped and status attributes should be added. The 'status' attribute are not as expected, indicating a problem in the tested template.</label>\n" + 
        "         <expect select=\"/element()\">\n" + 
        "            <content-wrap xmlns=\"\">\n" + 
        "               <phrases xmlns:functx=\"http://www.functx.com\"\n" + 
        "                        xmlns:x=\"http://www.jenitennison.com/xslt/xspec\">\n" + 
        "                  <phrase status=\"same\">Hello!</phrase>\n" + 
        "                  <phrase status=\"same\">Goodbye!</phrase>\n" + 
        "                  <phrase status=\"changed\">\\(So long!\\)</phrase>\n" + 
        "               </phrases>\n" + 
        "            </content-wrap>\n" + 
        "         </expect>\n" + 
        "      </test>\n" + 
        "   </scenario>\n" + 
        // The source attribute is present.
        // To ensure back mapping we need to remember in which file a scenario was defined.
        "   <scenario id=\"" + secondID + "\"\n" + 
        "             xspec=\"" + importedXSpecURL.toString() + "\">\n" + 
        "      <label>No escaping</label>\n" + 
        "      <input-wrap xmlns=\"\">\n" + 
        "         <x:call xmlns:functx=\"http://www.functx.com\"\n" + 
        "                 xmlns:x=\"http://www.jenitennison.com/xslt/xspec\"\n" + 
        "                 function=\"functx:escape-for-regex\">\n" + 
        "            <x:param select=\"'Hello'\"/>\n" + 
        "         </x:call>\n" + 
        "      </input-wrap>\n" + 
        "      <result select=\"'Hello'\"/>\n" + 
        "      <test id=\"" + secondID + "-expect1\" successful=\"true\">\n" + 
        "         <label>Must not be escaped at all</label>\n" + 
        "         <expect select=\"'Hello'\"/>\n" + 
        "      </test>\n" + 
        "   </scenario>\n" + 
        "</report>", 
        filterAll(read(xmlFormatOutput.toURI().toURL()).toString()));
    
    
    assertTrue(outputFile.exists());
    
    
    File css = new File("frameworks/xspec/oxygen-results-view/test-report.css");
    File js = new File("frameworks/xspec/oxygen-results-view/test-report.js");
    
    
    assertEquals(
        "<!DOCTYPE HTML>\n"
        + "<html>\n" + 
        "   <head>\n" + 
        "      <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">\n" + 
        "      <link rel=\"stylesheet\" type=\"text/css\" href=\"" + css.toURI().toURL().toString() + "\">"
            + "<script type=\"text/javascript\" src=\"" + js.toURI().toURL().toString() + "\"></script></head>\n" + 
        "   <body>\n" + 
        "      <div class=\"testsuite\" "
        // Scenario name
        + "data-name=\"When processing a list of phrases\" id=\"" + firstID + "\" "
        // Scenario source
        + "data-xspec=\"" + xspecFile.toURI().toURL().toString() + "\" data-tests=\"2\" data-failures=\"1\">\n" + 
        "         <p style=\"margin:0px;\"><span>When processing a list of phrases</span><span>&nbsp;</span><a class=\"button\" onclick=\"runScenario(this)\">Run</a></p>\n" +
        // x:expect
        "         <div class=\"testcase\" data-name=\"All phrase elements should remain\">\n" + 
        "            <p class=\"passed\"><span class=\"test-passed\" onclick=\"toggleResult(this)\">All phrase elements should remain</span><span>&nbsp;</span><a class=\"button\" onclick=\"showTest(this)\">Show</a></p>\n" + 
        "         </div>\n" + 
        // x:expect
        "         <div class=\"testcase\" data-name=\"Strings should be escaped and status attributes should be added. The 'status' attribute are not as expected, indicating a problem in the tested template.\">\n" + 
        "            <p class=\"failed\">"
        // Toggle result on click
        + "<span class=\"test-failed\" onclick=\"toggleResult(this)\">Strings should be escaped and status attributes should be added. The 'status' attribute\n" + 
        "                  are not as expected, indicating a problem in the tested template.</span><span>&nbsp;</span>"
        // A button to locate the scenario inside the XSPEC source file.
        + "<a class=\"button\" onclick=\"showTest(this)\">Show</a><span>&nbsp;</span>"
        // A button to show the Q-DIFF.
        + "<a class=\"button\" onclick=\"toggleResult(this)\">Q-Diff</a><span>&nbsp;</span>"
        // A button to show the DIFF inside Oxygen's DIFF.
        + "<a class=\"button\" onclick=\"showDiff(this)\">Diff</a></p>\n" +
        // Q-DIFF data in HTML format.
        "            <div class=\"failure\" id=\"" + firstID + "-expect2\" style=\"display:none;\">\n" + 
        "               <table class=\"xspecResult\">\n" + 
        "                  <thead>\n" + 
        "                     <tr>\n" + 
        "                        <th style=\"font-size:14px;\">Result</th>\n" + 
        "                        <th style=\"font-size:14px;\">Expected</th>\n" + 
        "                     </tr>\n" + 
        "                  </thead>\n" + 
        "                  <tbody>\n" + 
        "                     <tr>\n" + 
        "                        <td>\n" + 
        "                           <pre>&lt;<span class=\"inner-diff\">phrases</span> <span class=\"xmlns\">xmlns:functx=\"http://www.functx.com\"</span>&gt;\n" + 
        "   &lt;<span class=\"inner-diff\">phrase</span> <span class=\"inner-diff\">status</span>=<span class=\"diff\">\"changed\"</span>&gt;<span class=\"same\">Hello!</span>&lt;/phrase&gt;\n" + 
        "   &lt;<span class=\"inner-diff\">phrase</span> <span class=\"inner-diff\">status</span>=<span class=\"diff\">\"changed\"</span>&gt;<span class=\"same\">Goodbye!</span>&lt;/phrase&gt;\n" + 
        "   &lt;<span class=\"inner-diff\">phrase</span> <span class=\"inner-diff\">status</span>=<span class=\"diff\">\"same\"</span>&gt;<span class=\"same\">\\(So long!\\)</span>&lt;/phrase&gt;\n" + 
        "&lt;/phrases&gt;</pre>\n" + 
        "                        </td>\n" + 
        "                        <td>\n" + 
        "                           <pre>&lt;<span class=\"inner-diff\">phrases</span> <span class=\"xmlns\">xmlns:functx=\"http://www.functx.com\"</span>\n" + 
        "         <span class=\"xmlns trivial\">xmlns:x=\"http://www.jenitennison.com/xslt/xspec\"</span>&gt;\n" + 
        "   &lt;<span class=\"inner-diff\">phrase</span> <span class=\"inner-diff\">status</span>=<span class=\"diff\">\"same\"</span>&gt;<span class=\"same\">Hello!</span>&lt;/phrase&gt;\n" + 
        "   &lt;<span class=\"inner-diff\">phrase</span> <span class=\"inner-diff\">status</span>=<span class=\"diff\">\"same\"</span>&gt;<span class=\"same\">Goodbye!</span>&lt;/phrase&gt;\n" + 
        "   &lt;<span class=\"inner-diff\">phrase</span> <span class=\"inner-diff\">status</span>=<span class=\"diff\">\"changed\"</span>&gt;<span class=\"same\">\\(So long!\\)</span>&lt;/phrase&gt;\n" + 
        "&lt;/phrases&gt;</pre>\n" + 
        "                        </td>\n" + 
        "                     </tr>\n" + 
        "                  </tbody>\n" + 
        "               </table>\n" + 
        "            </div>\n" + 
        // DATA for the Oxygen DIFF.
        "            <pre class=\"embeded.diff.data\" style=\"display:none;\"><div class=\"embeded.diff.result\" style=\"white-space:pre;\">&lt;phrases&gt;\n" + 
        "   &lt;phrase status=\"changed\"&gt;Hello!&lt;/phrase&gt;\n" + 
        "   &lt;phrase status=\"changed\"&gt;Goodbye!&lt;/phrase&gt;\n" + 
        "   &lt;phrase status=\"same\"&gt;\\(So long!\\)&lt;/phrase&gt;\n" + 
        "&lt;/phrases&gt;</div><div class=\"embeded.diff.expected\" style=\"white-space:pre;\">&lt;phrases&gt;\n" + 
        "   &lt;phrase status=\"same\"&gt;Hello!&lt;/phrase&gt;\n" + 
        "   &lt;phrase status=\"same\"&gt;Goodbye!&lt;/phrase&gt;\n" + 
        "   &lt;phrase status=\"changed\"&gt;\\(So long!\\)&lt;/phrase&gt;\n" + 
        "&lt;/phrases&gt;</div></pre>\n" + 
        "         </div>\n" + 
        "      </div>\n" + 
        "      <div class=\"testsuite\" data-name=\"No escaping\" id=\"" + secondID +  "\" data-xspec=\""
        + importedXSpecURL.toString()
        + "\" data-tests=\"1\" data-failures=\"0\">\n" + 
        "         <p style=\"margin:0px;\"><span>No escaping</span><span>&nbsp;</span><a class=\"button\" onclick=\"runScenario(this)\">Run</a></p>\n" + 
        "         <div class=\"testcase\" data-name=\"Must not be escaped at all\">\n" + 
        "            <p class=\"passed\"><span class=\"test-passed\" onclick=\"toggleResult(this)\">Must not be escaped at all</span><span>&nbsp;</span><a class=\"button\" onclick=\"showTest(this)\">Show</a></p>\n" + 
        "         </div>\n" + 
        "      </div>\n" + 
        "   </body>\n" + 
        "</html>", simplify(read(outputFile.toURI().toURL()).toString()));
  }

  private String simplify(String string) {
    return string.replaceAll("<span xmlns=\"http://www.w3.org/1999/xhtml\"", "<span");
  }

  /**
   * We can specify which scenarios to be run.
   * 
   * @throws Exception
   */
  public void testRunSpecificScenarios_1() throws Exception {
    URL xspecURL = getClass().getClassLoader().getResource("meta/driverTest.xspec");
    URL xslURL = getClass().getClassLoader().getResource("meta/escape-for-regex.xsl");
    File xspecFile = URLUtil.getCanonicalFileFromFileUrl(xspecURL);
    File outputFile = new File(xspecFile.getParentFile(), "driverTest-report.html");
    
    executeANT(xspecFile, outputFile);
    
    File xmlFormatOutput = new File(xspecFile.getParentFile(), "xspec/driverTest-result.xml");
    
    String firstID = XSpecUtil.generateId("Test no.1(0)");
    String secondID = XSpecUtil.generateId("Test no.2(1)");
    
    assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
        "<report xmlns=\"http://www.jenitennison.com/xslt/xspec\"\n" + 
        "        xspec=\"" + xspecURL.toExternalForm() + "\"\n" + 
        "        stylesheet=\"" + xslURL.toString() + "\"\n" + 
        "        date=\"XXX\">\n" +
        "   <scenario id=\"" + firstID + "\"\n" + 
        "             xspec=\"" + xspecURL.toString() + "\">\n" + 
        "      <label>Test no.1</label>\n" + 
        "      <input-wrap xmlns=\"\">\n" + 
        "         <x:call xmlns:functx=\"http://www.functx.com\"\n" + 
        "                 xmlns:x=\"http://www.jenitennison.com/xslt/xspec\"\n" + 
        "                 function=\"functx:escape-for-regex\">\n" + 
        "            <x:param select=\"'Hello'\"/>\n" + 
        "         </x:call>\n" + 
        "      </input-wrap>\n" + 
        "      <result select=\"'Hello'\"/>\n" + 
        "      <test id=\"" + firstID + "-expect1\" successful=\"true\">\n" + 
        "         <label>Must not be escaped at all</label>\n" + 
        "         <expect select=\"'Hello'\"/>\n" + 
        "      </test>\n" + 
        "   </scenario>\n" + 
        "   <scenario id=\"" + secondID + "\"\n" + 
        "             xspec=\"" + xspecURL.toString() + "\">\n" + 
        "      <label>Test no.2</label>\n" + 
        "      <input-wrap xmlns=\"\">\n" + 
        "         <x:call xmlns:functx=\"http://www.functx.com\"\n" + 
        "                 xmlns:x=\"http://www.jenitennison.com/xslt/xspec\"\n" + 
        "                 function=\"functx:escape-for-regex\">\n" + 
        "            <x:param select=\"'(Hello)'\"/>\n" + 
        "         </x:call>\n" + 
        "      </input-wrap>\n" + 
        "      <result select=\"'\\(Hello\\)'\"/>\n" + 
        "      <test id=\"" + secondID + "-expect1\" successful=\"true\">\n" + 
        "         <label>Must not be escaped at all</label>\n" + 
        "         <expect select=\"'\\(Hello\\)'\"/>\n" + 
        "      </test>\n" + 
        "   </scenario>\n" + 
        "</report>", 
        filterAll(read(xmlFormatOutput.toURI().toURL()).toString()));
    
    assertTrue(outputFile.exists());
    
    
    File css = new File("frameworks/xspec/oxygen-results-view/test-report.css");
    File js = new File("frameworks/xspec/oxygen-results-view/test-report.js");
    
    assertEquals(
        "<!DOCTYPE HTML>\n<html>\n" + 
        "   <head>\n" + 
        "      <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">\n" + 
        "      <link rel=\"stylesheet\" type=\"text/css\" href=\"" + css.toURI().toURL().toString() + "\"><script type=\"text/javascript\" src=\"" + js.toURI().toURL().toString()
            + "\"></script></head>\n" + 
        "   <body>\n" + 
        "      <div class=\"testsuite\" data-name=\"Test no.1\" id=\"" + firstID + "\" data-xspec=\"" + xspecURL.toString()
        + "\" data-tests=\"1\" data-failures=\"0\">\n" + 
        "         <p style=\"margin:0px;\"><span>Test no.1</span><span>&nbsp;</span><a class=\"button\" onclick=\"runScenario(this)\">Run</a></p>\n" + 
        "         <div class=\"testcase\" data-name=\"Must not be escaped at all\">\n" + 
        "            <p class=\"passed\"><span class=\"test-passed\" onclick=\"toggleResult(this)\">Must not be escaped at all</span><span>&nbsp;</span><a class=\"button\" onclick=\"showTest(this)\">Show</a></p>\n" + 
        "         </div>\n" + 
        "      </div>\n" + 
        "      <div class=\"testsuite\" data-name=\"Test no.2\" id=\"" + secondID + "\" data-xspec=\"" + xspecURL.toString()
        + "\" data-tests=\"1\" data-failures=\"0\">\n" + 
        "         <p style=\"margin:0px;\"><span>Test no.2</span><span>&nbsp;</span><a class=\"button\" onclick=\"runScenario(this)\">Run</a></p>\n" + 
        "         <div class=\"testcase\" data-name=\"Must not be escaped at all\">\n" + 
        "            <p class=\"passed\"><span class=\"test-passed\" onclick=\"toggleResult(this)\">Must not be escaped at all</span><span>&nbsp;</span><a class=\"button\" onclick=\"showTest(this)\">Show</a></p>\n" + 
        "         </div>\n" + 
        "      </div>\n" + 
        "   </body>\n" + 
        "</html>", read(outputFile.toURI().toURL()).toString());
    
    // Assert the driver.
    /*
    File compiledXSL = new File(xspecFile.getParentFile(), "xspec/driverTest-compiled-original.xsl");
    File driverXSL = new File(xspecFile.getParentFile(), "xspec/driverTest-compiled.xsl");
    
    assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
        "<xsl:stylesheet xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\"\n" + 
        "                xmlns:xs=\"http://www.w3.org/2001/XMLSchema\"\n" + 
        "                xmlns:x=\"http://www.jenitennison.com/xslt/xspec\"\n" + 
        "                version=\"2.0\">\n" + 
        "   <xsl:import href=\"" + compiledXSL.toURI().toURL().toString() + "\"/>\n" + 
        "</xsl:stylesheet>", 
        read(driverXSL.toURI().toURL()).toString().replaceAll("\\\\", "/").replaceAll("<\\?xml-stylesheet.*\\?>", ""));
    */
  }

  /**
   * We can specify which scenarios to be run.
   * 
   * @throws Exception
   */
  public void testRunSpecificScenarios_2() throws Exception {
    URL xspecURL = getClass().getClassLoader().getResource("meta/driverTest.xspec");
    URL xslURL = getClass().getClassLoader().getResource("meta/escape-for-regex.xsl");
    File xspecFile = URLUtil.getCanonicalFileFromFileUrl(xspecURL);
    File outputFile = new File(xspecFile.getParentFile(), "driverTest-report.html");
    
//    / Math first group(0) / Test sum(0)
    String firstID = XSpecUtil.generateId("Test no.1(0)");
    String secondID = XSpecUtil.generateId("Test no.2(1)");
    String entryPoints = firstID + " " + secondID;
    executeANT(xspecFile, outputFile, entryPoints, false);
    
    File xmlFormatOutput = new File(xspecFile.getParentFile(), "xspec/driverTest-result.xml");
    
    assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
        "<report xmlns=\"http://www.jenitennison.com/xslt/xspec\"\n" + 
        "        xspec=\"" + xspecURL.toExternalForm() + "\"\n" + 
        "        stylesheet=\"" + xslURL.toString() + "\"\n" + 
        "        date=\"XXX\">\n" +
        "   <scenario id=\"" + firstID + "\"\n" + 
        "             xspec=\"" + xspecURL.toString() + "\">\n" + 
        "      <label>Test no.1</label>\n" + 
        "      <input-wrap xmlns=\"\">\n" + 
        "         <x:call xmlns:functx=\"http://www.functx.com\"\n" + 
        "                 xmlns:x=\"http://www.jenitennison.com/xslt/xspec\"\n" + 
        "                 function=\"functx:escape-for-regex\">\n" + 
        "            <x:param select=\"'Hello'\"/>\n" + 
        "         </x:call>\n" + 
        "      </input-wrap>\n" + 
        "      <result select=\"'Hello'\"/>\n" + 
        "      <test id=\"" + firstID + "-expect1\" successful=\"true\">\n" + 
        "         <label>Must not be escaped at all</label>\n" + 
        "         <expect select=\"'Hello'\"/>\n" + 
        "      </test>\n" + 
        "   </scenario>\n" + 
        "   <scenario id=\"" + secondID + "\"\n" + 
        "             xspec=\"" + xspecURL.toString() + "\">\n" + 
        "      <label>Test no.2</label>\n" + 
        "      <input-wrap xmlns=\"\">\n" + 
        "         <x:call xmlns:functx=\"http://www.functx.com\"\n" + 
        "                 xmlns:x=\"http://www.jenitennison.com/xslt/xspec\"\n" + 
        "                 function=\"functx:escape-for-regex\">\n" + 
        "            <x:param select=\"'(Hello)'\"/>\n" + 
        "         </x:call>\n" + 
        "      </input-wrap>\n" + 
        "      <result select=\"'\\(Hello\\)'\"/>\n" + 
        "      <test id=\"" + secondID + "-expect1\" successful=\"true\">\n" + 
        "         <label>Must not be escaped at all</label>\n" + 
        "         <expect select=\"'\\(Hello\\)'\"/>\n" + 
        "      </test>\n" + 
        "   </scenario>\n" + 
        "</report>", 
        filterAll(read(xmlFormatOutput.toURI().toURL()).toString()));
    
        
    // Assert the driver.
    /*
    File compiledXSL = new File(xspecFile.getParentFile(), "xspec/driverTest-compiled-original.xsl");
    File driverXSL = new File(xspecFile.getParentFile(), "xspec/driverTest-compiled.xsl");
    
    assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
        "<xsl:stylesheet xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\"\n" + 
        "                xmlns:xs=\"http://www.w3.org/2001/XMLSchema\"\n" + 
        "                xmlns:x=\"http://www.jenitennison.com/xslt/xspec\"\n" + 
        "                version=\"2.0\">\n" + 
        "   <xsl:import href=\"" + compiledXSL.toURI().toURL().toString() + "\"/>\n" + 
        "   <xsl:template name=\"x:main\">\n" + 
        "      <xsl:result-document format=\"x:report\">\n" + 
        "         <x:report>\n" + 
        // 
        "            <xsl:call-template name=\"x:" + firstID + "\"/>\n" + 
        "            <xsl:call-template name=\"x:" + secondID + "\"/>\n" + 
        "         </x:report>\n" + 
        "      </xsl:result-document>\n" + 
        "   </xsl:template>\n" + 
        "</xsl:stylesheet>", 
        read(driverXSL.toURI().toURL()).toString().replaceAll("\\\\", "/").replaceAll("<\\?xml-stylesheet.*\\?>", ""));
    */
  }
}
