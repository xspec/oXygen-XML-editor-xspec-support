package com.oxygenxml.xspec.jfx;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.junit.Ignore;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.w3c.dom.Node;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.concurrent.Worker.State;
import javafx.scene.web.WebEngine;
import junit.extensions.jfcunit.JFCTestCase;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.XPathCompiler;
import net.sf.saxon.s9api.XPathExecutable;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmNode;
import ro.sync.exml.workspace.api.editor.WSEditor;
import ro.sync.exml.workspace.api.editor.documenttype.DocumentTypeInformation;
import ro.sync.exml.workspace.api.standalone.StandalonePluginWorkspace;
import ro.sync.exml.workspace.api.util.UtilAccess;
import ro.sync.util.URLUtil;

@Ignore
public class XSpecViewTestBase extends JFCTestCase {
  
  /**
   * The plugin workspace used by the XSpec view.
   */
  protected StandalonePluginWorkspace pluginWorkspace;
  
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    
    
    pluginWorkspace = createPluginWorkspace();
  }
  
  protected StandalonePluginWorkspace createPluginWorkspace() {
    StandalonePluginWorkspace mock = Mockito.mock(StandalonePluginWorkspace.class);
    UtilAccess value = Mockito.mock(UtilAccess.class);
    Mockito.when(value.getFileName(Mockito.anyString())).then(new Answer<String>() {
      @Override
      public String answer(InvocationOnMock invocation) throws Throwable {
        return URLUtil.extractFileName((URL) invocation.getArguments()[0]);
      }
    });
    
    Mockito.when(mock.getUtilAccess()).thenReturn(value);
    
    Mockito.when(mock.getEditorAccess(Mockito.any(URL.class), Mockito.anyInt())).then(new Answer<WSEditor>() {
      @Override
      public WSEditor answer(InvocationOnMock invocation) throws Throwable {
        URL url = (URL) invocation.getArguments()[0];
        return createEditorMock(url);
      }
    });
    

    return mock;
  }
  
  protected void initXSpec(URL xspec) {
    // We simulate that the current opened editor is the XSPec itself.
    WSEditor editorMock = createEditorMock(xspec);
    Mockito.when(pluginWorkspace.getCurrentEditorAccess(Mockito.anyInt())).thenReturn(editorMock);
  }

  protected WSEditor createEditorMock(URL url) {
    WSEditor mock = Mockito.mock(WSEditor.class);
    Mockito.when(mock.getEditorLocation()).thenReturn(url);
    
    Mockito.when(mock.getDocumentTypeInformation()).thenReturn(Mockito.mock(DocumentTypeInformation.class));
    
//    try {
//      Mockito.doAnswer(new Answer<Object>() {
//        @Override
//        public Object answer(InvocationOnMock invocation) throws Throwable {
//          
//          TransformationFeedback feedback = (TransformationFeedback) invocation.getArguments()[1];
//          
//          // Execute the transformation.
//          
//          return null;
//        }
//      }).when(mock).runTransformationScenarios(
//          new String[] {com.oxygenxml.xspec.XSpecUtil.SCENARIO_NAME}, 
//          Mockito.any(TransformationFeedback.class));
//    } catch (TransformationScenarioNotFoundException e) {
//      e.printStackTrace();
//    }
    
    return mock;
  }

  protected String execute(final WebEngine webEngine, final String script) throws InterruptedException {
    final String[] toReturn = new String[1];
    
    final Semaphore s = new Semaphore(0);
    Platform.runLater(new Runnable() {
      @Override
      public void run() {
        try {
          toReturn[0] = webEngine.executeScript(script).toString();
        } catch (Exception e) {
          e.printStackTrace();
        }
        // It means that all other events scheduled on the FX thread are done.
        s.release();
      }
    });
    s.acquire();
    
    return toReturn[0];
  }

  /**
   * Loads a utilities Javascript library into the XSpec view.
   * 
   * @throws InterruptedException If it fails.
   */
  protected void loadUtilitiesLibrary(final WebEngine webEngine) throws InterruptedException {
    final URL utilitiesJS = getClass().getClassLoader().getResource("utilities.js");
    
    invokeAndWaitOnFX(new Runnable() {
      @Override
      public void run() {
        try {
          webEngine.executeScript(read(utilitiesJS).toString());
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    });
  }

  /**
   * Waits until the FX thread has consumed all scheduled events.
   * 
   * @throws InterruptedException
   */
  protected void waitForFX() throws InterruptedException {
    // Invoke and wait to ensure all other events are executed.
    invokeAndWaitOnFX(new Runnable() {
      @Override
      public void run() {}
    });
  }

  /**
   * Executes the given runnable on the FX thread and waits untilthe code was executed.
   * 
   * @param r Code to execute.
   * 
   * @throws InterruptedException If it fails.
   */
  protected void invokeAndWaitOnFX(final Runnable r) throws InterruptedException {
    final Semaphore s = new Semaphore(0);
    Platform.runLater(new Runnable() {
      @Override
      public void run() {
        r.run();
        
        s.release();
      }
    });
    
    s.acquire();
  }

  /**
   * Waits until the Web engine has successfully loaded the page.
   * 
   * @param engine Web engine.
   * @throws InterruptedException
   */
  protected void waitForEngine(final WebEngine engine) throws InterruptedException {
    final Semaphore s = new Semaphore(0);
    // Make the test on the FX thread, the same thread on which the state is updated.
    Platform.runLater(new Runnable() {
      @Override
      public void run() {
        // It means that all other events scheduled on the FX thread are done.
        if (engine.getLoadWorker().getState() == Worker.State.SUCCEEDED) {
          // Already loaded. Release the semaphore.
          s.release();
        } else {
          // Not yet loaded. Wait and release the semaphore when the state changes.
          engine.getLoadWorker().stateProperty().addListener(
              new ChangeListener<State>() {
                @Override public void changed(ObservableValue ov, State oldState, State newState) {
                  if (newState == Worker.State.SUCCEEDED) {
                    s.release();
                  }
                }
              });
        }
      }
    });
    s.acquire();
  }

  /**
   * Reads the content of a file.
   * 
   * @param url File to read.
   * 
   * @return The content of the file.
   * 
   * @throws Exception If it fails.
   */
  protected StringBuilder read(URL url) throws Exception {
    StringBuilder b = new StringBuilder();
  
    BufferedReader r = new BufferedReader(new InputStreamReader(url.openStream(), "UTF-8"));
    try {
      String l = null;
      while ((l = r.readLine()) != null) {
        if (b.length() > 0) {
          b.append("\n");
        }
        
        b.append(l);
      }
    } finally {
      r.close();
    }
  
    return b;
  }

  
  protected static String getInnerHTML(Node node) throws TransformerConfigurationException, TransformerException {
      StringWriter sw = new StringWriter();
      Result result = new StreamResult(sw);
      TransformerFactory factory = new net.sf.saxon.TransformerFactoryImpl();
      Transformer proc = factory.newTransformer();
      proc.setOutputProperty(OutputKeys.METHOD, "html");
      for (int i = 0; i < node.getChildNodes().getLength(); i++) {
          proc.transform(new DOMSource(node.getChildNodes().item(i)), result);
      }
      return sw.toString();
  }
  
  protected void executeANT(File xspecFile, File outputFile) throws IOException, InterruptedException {
    executeANT(xspecFile, outputFile, "", false);
  }
  
  /**
   * Execute an XSpec script using ANT. 
   * 
   * @param xspecFile XSpec file to execute.
   * @param outputFile Output file.
   * @param entryPoints Template entry points.
   * @param schematron <code>true</code> if the script represents a Schematron test.
   * 
   * @throws IOException If it fails.
   * @throws InterruptedException If it fails.
   */
  protected void executeANT(File xspecFile, File outputFile, String entryPoints, boolean schematron)
      throws IOException, InterruptedException {
    URL saxonConfig = getClass().getClassLoader().getResource("config/saxon-config.xml");

    // Build the classpath needed to launch ANT.
    File antL = URLUtil.getCanonicalFileFromFileUrl(getClass().getClassLoader().getResource("cmd/apache-ant-1.10.1/lib/ant-launcher.jar"));
    File ant = URLUtil.getCanonicalFileFromFileUrl(getClass().getClassLoader().getResource("cmd/apache-ant-1.10.1/lib/ant.jar"));

    String separator = ":";
    String osName = System.getProperty("os.name");
    if (osName.toUpperCase().startsWith("WIN")) {
      separator = ";";
    }
    
    StringBuilder antlauncherJar = new StringBuilder();
    antlauncherJar.append(antL.getAbsolutePath()).append(separator)
    .append(ant.getAbsolutePath());

    // Build the classpath needed by the build_report.xml script.
    StringBuilder cl = new StringBuilder();
    File saxon = URLUtil.getCanonicalFileFromFileUrl(getClass().getClassLoader().getResource("cmd/saxon9ee.jar"));
    cl.append("-lib ").append(saxon.getAbsolutePath()).append(" ");
    File extSaxon = new File("frameworks/xspec/oxygen-results-view/saxon-extension.jar");
    cl.append("-lib ").append(extSaxon.getAbsolutePath()).append(" ");
    File xerces = URLUtil.getCanonicalFileFromFileUrl(getClass().getClassLoader().getResource("cmd/xercesImpl.jar"));
    cl.append("-lib ").append(xerces.getAbsolutePath()).append(" ");
    File resolver = URLUtil.getCanonicalFileFromFileUrl(getClass().getClassLoader().getResource("cmd/resolver.jar"));
    cl.append("-lib ").append(resolver.getAbsolutePath()).append(" ");
    File xmlApis = URLUtil.getCanonicalFileFromFileUrl(getClass().getClassLoader().getResource("cmd/xml-apis.jar"));
    cl.append("-lib ").append(xmlApis.getAbsolutePath()).append(" ");


    File compilerXSL = new File("frameworks/xspec/oxygen-results-view/generate-xspec-tests-oxygen.xsl");
    File compilerXSLDriver = new File("frameworks/xspec/oxygen-results-view/compile-driver.xsl");
    File reportXSL = new File("frameworks/xspec/oxygen-results-view/unit-report-oxygen.xsl");
    File xspecProjectDir = new File("frameworks/xspec/");

    File buildFile = new File("frameworks/xspec/oxygen-results-view/build_report.xml");

    //    System.out.println("CLASSPATH: " + cl);
    //    System.out.println("ANT: " + antlauncherJar);

    List<String> lines = new ArrayList<String>();

    lines.add("java");
    lines.add("-Xmx256m");
    lines.add("-classpath");
    lines.add(antlauncherJar.toString());
    lines.add("org.apache.tools.ant.launch.Launcher");

    // Classpath
    String[] split = cl.toString().split(" ");
    for (int i = 0; i < split.length; i++) {
      lines.add(split[i]);
    }

    lines.add("-f");
    // Build file.
    lines.add(buildFile.getAbsolutePath());
    lines.add("-Dclean.output.dir=false");

    // Compile XSL.
    lines.add("-Dext.xspec.compiler.xsl=" + compilerXSL.getAbsolutePath() );
    // Driver XSL that is applied over the the compiled XSL.
    lines.add("-Dcompile.xspec.xsl.driver=" + compilerXSLDriver.getAbsolutePath() );
    // Report formatter.
    lines.add("-Dxspec.html.reporter.xsl=" + reportXSL.getAbsolutePath() );
    // XSpec project dir.
    lines.add("-Dxspec.project.dir=" + xspecProjectDir.getAbsolutePath() );
    // Output file name.
    lines.add("-Dxspec.result.html=" + outputFile.getAbsolutePath() );
    lines.add("-Dxspec.template.name.entrypoint=" + entryPoints);
    // XSpec to process.
    lines.add("-Dxspec.xml=" + xspecFile.getAbsolutePath() );
    // From tests we Run Saxon in HE mode so we need a config file that creates a HE configuration. 
    lines.add("-Dxspec.saxon.config=" + URLUtil.getCanonicalFileFromFileUrl(saxonConfig).getAbsolutePath());
    
    if (schematron) {
      lines.add("-Dtest.type=s");  
    }


    System.out.println("-----LINES----");
    for (String p : lines) {
      System.out.println(p);
    }
    System.out.println("-----------");

    final Process p = Runtime.getRuntime().exec(lines.toArray(new String[0]));

    new Thread(new Runnable() {
      public void run() {
        BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String line = null; 

        try {
          while ((line = input.readLine()) != null)
            System.out.println(line);
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }).start();

    new Thread(new Runnable() {
      public void run() {
        BufferedReader input = new BufferedReader(new InputStreamReader(p.getErrorStream()));
        String line = null; 

        try {
          while ((line = input.readLine()) != null)
            System.out.println("error: " + line);
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }).start();

    p.waitFor();

  }
  
  /**
   * Filter "x:test" ids.
   * 
   * @param content Content to filter.
   * 
   * @return The content without the ids.
   */
  protected String filterTestElementId(String content) {
    return content.replaceAll("x:test id=\"[^\"]*\"", "x:test");
  }
  
  /**
   * Filters various things that are not important for an assert or generated content
   * that changes on every run.
   * 
   * @param content Content to filter.
   * 
   * @return The filtered content.
   */
  protected String filterAll(String content) {
    return content
        .replaceAll("date=\".*\"", "date=\"XXX\"")
        .replaceAll("<\\?xml-stylesheet.*\\?>", "")
        .replaceAll("x:test id=\"[^\"]*\"", "x:test");
  }
  
  /**
   * Executes XPath.
   * 
   * @param resource Input.
   * @param xpath XPath expression.
   * 
   * @return Result.
   * 
   * @throws Exception if it fails.
   */
  protected String executeXPath(File resource, String xpath) throws Exception {
    Processor processor = new Processor(false);
    XdmNode build = processor.newDocumentBuilder().build(resource);
    XPathCompiler newXPathCompiler = processor.newXPathCompiler();
    
    XdmItem evaluateSingle = newXPathCompiler.evaluateSingle(xpath, build);
    
    return evaluateSingle.toString();
  }

}
