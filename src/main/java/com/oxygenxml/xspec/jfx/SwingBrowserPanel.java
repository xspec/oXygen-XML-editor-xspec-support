package com.oxygenxml.xspec.jfx;

import java.awt.BorderLayout;
import java.awt.Color;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.UIManager;

import org.apache.log4j.Logger;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.concurrent.Worker.State;
import javafx.embed.swing.JFXPanel;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebErrorEvent;
import javafx.scene.web.WebEvent;
import javafx.scene.web.WebView;

/**
 * A Web browser based on JFX.
 * 
 * @author alex_jitianu
 */
public class SwingBrowserPanel extends JPanel {

  /**
   * Default progress indicator height.
   */
  private int PROGRESS_HEIGHT= 80;
  /**
   * Default progress indicator width.
   */
  private int PROGRESS_WIDTH = 80;
  /**
   * Minimum page loading time.
   */
  private static final int PAGE_LOADING_TIME = 200;
  /**
   * Logger for logging.
   */
  private static final Logger logger = Logger.getLogger(SwingBrowserPanel.class.getName());
  /**
   * The Web engine used to render the pages.
   */
  private WebEngine engine;
  /**
   * The panel holding the JFX components.
   */
  private JFXPanel jfxPanel;
  /**
   * The loaded URL.
   */
  private URL url;
  /**
   * Handler to use links from jfx browser from outside.
   */
  private BrowserInteractor handler;
  /**
   * JavaFx stack pane..allows components to have layers. 
   * In our case, the stack pane contains {@link JFXPanel} which contains the {@link WebView} that renders the html
   * pages and the {@link ProgressIndicator}. 
   * The {@link ProgressIndicator} disappears when the {@link WebView} successfully loads the html page.
   */
  private StackPane stackPane;
  /**
   * This progress is active while the {@link WebView} loads the html page. 
   */
  private ProgressIndicator indicator;
  /**
   * Timer to decide when the {@link ProgressIndicator} should be shown.
   */
  private Timer timer = new Timer(false);
  /**
   * Web view containing the web engine.
   */
  private WebView view;
  /**
   * Load listener.
   */
  private ChangeListener<State> changeListener = null;
  
  /**
   * Constructor.
   */
  public SwingBrowserPanel(BrowserInteractor handler) {
    this.handler = handler;
    this.jfxPanel = new JFXPanel();

    initComponents();

    if (logger.isDebugEnabled()) {
      logger.debug("Create browser");
    }
    
  }

  /**
   * Initialize the components.
   */
  private void initComponents() {
    createScene();
    
    setLayout(new BorderLayout());
    add(jfxPanel, BorderLayout.CENTER);
    
  }

  /**
   * Gets some colors from the UIManager and installs them on the web engine.
   */
  private void installColors() {
    // Take these colors from the UIManager. Useful if we are on a dark theme.
    try {
      Color color = (Color) UIManager.get("TextArea.background");
      Color fg = (Color) UIManager.get("TextArea.foreground");
      
      String s = "body{" + 
          "background-color:" + "rgb(" + color.getRed() + "," + color.getGreen() + "," + color.getBlue() + ");" +
          "}div{color:" + "rgb(" + fg.getRed() + "," + fg.getGreen() + "," + fg.getBlue() + ");" +
          "}";
      String encoded = URLEncoder.encode(s, "UTF-8");
      String dataURL = "data:text/plain;charset=utf-8," + encoded;
      engine.setUserStyleSheetLocation(dataURL);
    } catch (UnsupportedEncodingException e) {
      logger.error(e, e);
    }
  }

  /**
   * Create scene for javafx components.
   */
  private void createScene() {
    // Run from FX thread.
    Platform.runLater(new Runnable() {
      /**
       * Timer task to set visible the loading page progress indicator.
       */
      private TimerTask progressPresentTask;

      @Override
      public void run() {
        // On HiDPI screens running on windows, the youtube video have problems.
        // If we scale the font size, videos will not fit to boundaries.
        // The font will be a bit smaller, but the embedded videos will behave proper.
        view = new WebView();
        engine = view.getEngine();
        
        
        engine.setOnAlert(new EventHandler<WebEvent<String>>() {
          @Override
          public void handle(WebEvent<String> event) {
            // Catch every href link.
            handler.alert(event.getData());
          }
        });

        
        stackPane = new StackPane();
        indicator = new ProgressIndicator();
        // Make it the progress smaller.
        indicator.setPrefSize(PROGRESS_WIDTH, PROGRESS_HEIGHT);
        indicator.setMaxWidth(PROGRESS_WIDTH);
        indicator.setMaxHeight(PROGRESS_HEIGHT);
        // Process JavaScript that intercepts link navigation when the page is fully loaded.
        changeListener = new ChangeListener<State>() {
          @Override public void changed(ObservableValue ov, State oldState, State newState) {
            if (newState == Worker.State.SUCCEEDED) {
              // Page is loaded, so the progress indicator is not needed anymore.
              // Cancel the task.
              progressPresentTask.cancel();
              // Kill the timer.
              timer.cancel();
              // Remove the progress indicator from the stack pane.
              stackPane.getChildren().remove(indicator);

              handler.pageLoaded();
            }
          }


        };
        engine.getLoadWorker().stateProperty().addListener(changeListener);

        engine.setOnError(new EventHandler<WebErrorEvent>()
            {
          @Override
          public void handle(WebErrorEvent event)
          {
            System.err.println("error: " + event);
          }
            });
        
        engine.getLoadWorker().exceptionProperty().addListener(new ChangeListener<Throwable>() {
          @Override
          public void changed(ObservableValue<? extends Throwable> ov, Throwable t, Throwable t1) {
              System.out.println("Received exception: "+t1.getMessage());
          }
      });

        progressPresentTask = new TimerTask() {
          @Override
          public void run() {
            Platform.runLater(new Runnable() {
              @Override
              public void run() {
                if (engine.getLoadWorker().getState() != State.SUCCEEDED) {
                  indicator.setVisible(true);
                }
              }
            });
          }
        };

        timer.schedule(progressPresentTask, PAGE_LOADING_TIME);
        // We present it only if the loading takes too long.
        indicator.setVisible(false);
        stackPane.getChildren().addAll(view, indicator);
        
        // create root
        BorderPane root = new BorderPane();
        root.setCenter(stackPane);
        
        jfxPanel.setScene(new Scene(root));

        installColors();
      }
    });
  }

  /**
   * @return Returns the URL of the current loaded page.
   */
  public URL getLoadedUrl() {
    return url;
  }

  /**
   * Load the given URL into the browser.
   * 
   * @param url Url to load.
   */
  public void loadURL(final URL url) {
    if (logger.isDebugEnabled()) {
      logger.debug("Load URL " + url);
    }
    this.url = url;
    Platform.runLater(new Runnable() {
      @Override
      public void run() {
        String tmp = toURL(url.toExternalForm());

        if (tmp == null) {
          tmp = toURL("http://" + url.toExternalForm());
        }

        engine.load(tmp);
      }
    });

  }
  
  /**
   * Load the given URL into the browser.
   * 
   * @param url Url to load.
   */
  public void loadContent(final String content) {
    this.url = null;
    Platform.runLater(new Runnable() {
      @Override
      public void run() {
        engine.loadContent(content);
      }
    });
  }

  /**
   * Constructs an URL from a string.
   * 
   * @param str the String url.
   * @return The URL in external form or <code>null</code> if it's not an URL.
   */
  private static String toURL(String str) {
    try {
      return new URL(str).toExternalForm();
    } catch (MalformedURLException exception) {
      return null;
    }
  }

  /**
   * Reset the loaded URL. This will stop any streaming that might take place.
   */
  public void dispose() {
    Platform.runLater(new Runnable() {
      @Override
      public void run() {
        engine.getLoadWorker().stateProperty().removeListener(changeListener);
        engine.load(null);
        // Reset cookies to make sure the memory is released.
        java.net.CookieManager manager = new java.net.CookieManager();
        java.net.CookieHandler.setDefault(manager);
      }
    });
  }

  /**
   * Get the webEngine for tests.
   * @return the webEngine.
   */
  public WebEngine getWebEngine() {
    return engine;
  }
  
  
  public static void main(String[] args) {
    JFrame frame = new JFrame();
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    
    SwingBrowserPanel p = new SwingBrowserPanel(new BrowserInteractor() {
      @Override
      public void alert(String message) {
        System.out.println(message);
      }
      public void pageLoaded() {
        System.out.println("page loaded");
      }
    });
    p.loadContent("<!DOCTYPE html>\n" + 
        "<html xmlns=\"http://www.w3.org/1999/xhtml\">\n" + 
        "    <head>\n" + 
        "        <title></title>\n" + 
        "    </head>\n" + 
        "    <body><p><b>BOLD</b><button onclick=\"jsbridge.showMessage('go')\">Go</button></p></body>\n" + 
        "</html>");
    frame.add(p, BorderLayout.CENTER);
    
    frame.setSize(400, 400);
    frame.setVisible(true);
  }

  public void executeScript(final String script) {
    Platform.runLater(new Runnable() {
      @Override
      public void run() {
        try {
        engine.executeScript(script);
        } catch (Throwable t) {
          t.printStackTrace();
        }
      }
    });
  }
  
  /**
   * Sets the scale factor.
   * 
   * @param scale The scale factor.
   */
  public void setScaleFactor(float scale) {
    Platform.runLater(() -> {
      view.setFontScale(scale);
    });
  }
}
