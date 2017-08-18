package com.oxygenxml.xspec.jfx;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.URL;
import java.util.concurrent.Semaphore;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

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
import ro.sync.exml.workspace.api.editor.WSEditor;
import ro.sync.exml.workspace.api.editor.documenttype.DocumentTypeInformation;
import ro.sync.exml.workspace.api.standalone.StandalonePluginWorkspace;
import ro.sync.exml.workspace.api.util.UtilAccess;
import ro.sync.util.URLUtil;

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
    executeANT(xspecFile, outputFile, "");
  }
  
  protected void executeANT(File xspecFile, File outputFile, String entryPoints) throws IOException, InterruptedException {
    URL saxonConfig = getClass().getClassLoader().getResource("config/saxon-config.xml");
    
    // Build the classpath needed to launch ANT.
    File antL = URLUtil.getCanonicalFileFromFileUrl(getClass().getClassLoader().getResource("cmd/apache-ant-1.10.1/lib/ant-launcher.jar"));
    File ant = URLUtil.getCanonicalFileFromFileUrl(getClass().getClassLoader().getResource("cmd/apache-ant-1.10.1/lib/ant.jar"));

    StringBuilder antlauncherJar = new StringBuilder();
    antlauncherJar.append(antL.getAbsolutePath()).append(";").append(ant.getAbsolutePath());
    
    // Build the classpath needed by the build_report.xml script.
    StringBuilder cl = new StringBuilder();
    File saxon = URLUtil.getCanonicalFileFromFileUrl(getClass().getClassLoader().getResource("cmd/saxon9ee.jar"));
    cl.append("-lib \"").append(saxon.getAbsolutePath()).append("\" ");
    File extSaxon = new File("frameworks/xspec/oxygen-results-view/saxon-extension.jar");
    cl.append("-lib \"").append(extSaxon.getAbsolutePath()).append("\" ");
    File xerces = URLUtil.getCanonicalFileFromFileUrl(getClass().getClassLoader().getResource("cmd/xercesImpl.jar"));
    cl.append("-lib \"").append(xerces.getAbsolutePath()).append("\" ");
    File resolver = URLUtil.getCanonicalFileFromFileUrl(getClass().getClassLoader().getResource("cmd/resolver.jar"));
    cl.append("-lib \"").append(resolver.getAbsolutePath()).append("\" ");
    

    File compilerXSL = new File("frameworks/xspec/oxygen-results-view/generate-xspec-tests-oxygen.xsl");
    File compilerXSLDriver = new File("frameworks/xspec/oxygen-results-view/compile-driver.xsl");
    File reportXSL = new File("frameworks/xspec/oxygen-results-view/unit-report-oxygen.xsl");
    File xspecProjectDir = new File("frameworks/xspec/");
    
    File buildFile = new File("frameworks/xspec/oxygen-results-view/build_report.xml");
    
//    System.out.println("CLASSPATH: " + cl);
//    System.out.println("ANT: " + antlauncherJar);
    
    String cmd = 
        "java -Xmx256m "
        + "-classpath \"" + antlauncherJar.toString() + "\" "
        + "org.apache.tools.ant.launch.Launcher "
        // Classpath
        + cl.toString()
        // Build file.
        + "-f \"" + buildFile.getAbsolutePath() + "\" "
        + "\"-Dclean.output.dir=false\" "
        // Compile XSL.
        + "\"-Dcompile.xspec.xsl=" + compilerXSL.getAbsolutePath() + "\" "
        // Driver XSL that is applied over the the compiled XSL.
        + "\"-Dcompile.xspec.xsl.driver=" + compilerXSLDriver.getAbsolutePath() + "\" "
        // Report formatter.
        + "\"-Dformat.xspec.report=" + reportXSL.getAbsolutePath() + "\" "
        // XSpec project dir.
        + "\"-Dxspec.project.dir=" + xspecProjectDir.getAbsolutePath() + "\" "
        // Output file name.
        + "\"-Dxspec.result.html=" + outputFile.getAbsolutePath() + "\" "
        + "\"-Dxspec.template.name.entrypoint=" + entryPoints+ "\" "
        // XSpec to process.
        + "\"-Dxspec.xml=" + xspecFile.getAbsolutePath() +  "\" "
        // From tests we Run Saxon in HE mode so we need a config file that creates a HE configuration. 
        + "\"-Dxspec.saxon.config=" + URLUtil.getCanonicalFileFromFileUrl(saxonConfig).getAbsolutePath() + "\"";
    
    System.out.println(cmd);
    
    final Process p = Runtime.getRuntime().exec(cmd);

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
}
