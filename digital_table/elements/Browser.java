package digital_table.elements;

import static javafx.concurrent.Worker.State.FAILED;

import java.awt.BorderLayout;
import java.awt.Graphics2D;
import java.net.MalformedURLException;
import java.net.URL;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.JFXPanel;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebEvent;
import javafx.scene.web.WebView;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public class Browser extends MapElement {
	private static final long serialVersionUID = 1L;

	public static final String PROPERTY_URL = "url";		// String
	public static final String PROPERTY_TITLE = "title";	// String - read only 
	public static final String PROPERTY_LABEL = "label";	// String - read only - fired when title changes 
	public static final String PROPERTY_ROLLOVER = "rollover";	// String - read only
	public final static String PROPERTY_ROTATIONS = "rotations";	// int - number of quadrants rotated clockwise
	public final static String PROPERTY_SCREEN = "screen";	// String
	
	String urlString = "";
	String title;
	String rollover;
	int rotations = 0;
	int screen = 0;
	
	transient protected JFXPanel jfxPanel = null;
    transient protected WebEngine engine;
    transient protected WebView view;
    transient protected JPanel panel = null;
    transient protected int onScreen;
    
    public Browser() {
    }
    
	public void paint(Graphics2D g) {
		if (panel != null) {
			panel.repaint();	// without this the underlying map will often get painted ontop of the panel. with this there can still be flickering
		}
	}

	public JComponent getComponent() {
		if (jfxPanel == null) {
			jfxPanel = new JFXPanel();
			createScene();

	        if (urlString == null || urlString.length() == 0) {
	        	loadURL("http://oracle.com");
	        } else {
	        	loadURL(urlString);
	        }
		}
		return jfxPanel;
    }

    private void createScene() {
        Platform.runLater(new Runnable() {
        	public void run() {
                view = new WebView();
                view.rotateProperty().set(90.0d * rotations);
                engine = view.getEngine();

                engine.titleProperty().addListener(new ChangeListener<String>() {
                    public void changed(ObservableValue<? extends String> observable, String oldValue, final String newValue) {
                        SwingUtilities.invokeLater(new Runnable() {
                            public void run() {
                            	setTitle(newValue);
                            }
                        });
                    }
                });

                engine.setOnStatusChanged(new EventHandler<WebEvent<String>>() {
                    public void handle(final WebEvent<String> event) {
                        SwingUtilities.invokeLater(new Runnable() {
                            public void run() {
                            	setRollover(event.getData());
                            }
                        });
                    }
                });

                engine.locationProperty().addListener(new ChangeListener<String>() {
                    public void changed(ObservableValue<? extends String> ov, String oldValue, final String newValue) {
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override public void run() {
                            	setURL(newValue, false);
                            }
                        });
                    }
                });

                engine.getLoadWorker().workDoneProperty().addListener(new ChangeListener<Number>() {
                    public void changed(ObservableValue<? extends Number> observableValue, Number oldValue, final Number newValue) {
                        SwingUtilities.invokeLater(new Runnable() {
                            public void run() {
                                //progressBar.setValue(newValue.intValue());
                            }
                        });
                    }
                });

                engine.getLoadWorker().exceptionProperty().addListener(new ChangeListener<Throwable>() {
                    public void changed(ObservableValue<? extends Throwable> o, Throwable old, final Throwable value) {
                        if (engine.getLoadWorker().getState() == FAILED) {
                            SwingUtilities.invokeLater(new Runnable() {
                                public void run() {
//                                    JOptionPane.showMessageDialog(
//                                            panel,
//                                            (value != null) ?
//                                            engine.getLocation() + "\n" + value.getMessage() :
//                                            engine.getLocation() + "\nUnexpected error.",
//                                            "Loading error...",
//                                            JOptionPane.ERROR_MESSAGE);
                                }
                            });
                        }
                    }
                });

                jfxPanel.setScene(new Scene(view));
            }
        });
    }

	public String toString() {
		if (title == null || title.length() == 0) return "Browser ("+getID()+")";
		return "Browser ("+title+")";
	}

	public void loadURL(final String url) {
    	if (jfxPanel == null) return;
        Platform.runLater(new Runnable() {
            @Override public void run() {
                String tmp = toURL(url);

                if (tmp == null) {
                    tmp = toURL("http://" + url);
                }

                engine.load(tmp);
            }
        });
    }

    private static String toURL(String str) {
        try {
            return new URL(str).toExternalForm();
        } catch (MalformedURLException exception) {
                return null;
        }
    }

	public Object getProperty(String property) {
		if (property.equals(PROPERTY_URL)) {
			return getURL();
		} else if (property.equals(PROPERTY_TITLE)) {
			return getTitle();
		} else if (property.equals(PROPERTY_ROLLOVER)) {
			return getRollover();
		} else if (property.equals(PROPERTY_ROTATIONS)) {
			return getRotations();
		} else if (property.equals(PROPERTY_SCREEN)) {
			return getScreen();
		} else {
			// throw exception?
			return null;
		}
	}

	public void setProperty(String property, Object value) {
		if (property.equals(PROPERTY_URL)) {
			setURL((String)value);
		} else if (property.equals(PROPERTY_TITLE)) {
			// read only
		} else if (property.equals(PROPERTY_ROLLOVER)) {
			// read only
		} else if (property.equals(PROPERTY_ROTATIONS)) {
			setRotations((Integer)value);
		} else if (property.equals(PROPERTY_SCREEN)) {
			setScreen((Integer)value);
		} else {
			// throw exception?
		}
	}
	
	public String getURL() {
		return urlString;
	}
	
	public void setURL(String url) {
		setURL(url,true);
	}

	protected void setURL(String url, boolean load) {
		String old = urlString;
		urlString = url;
		if (load) loadURL(url);
		pcs.firePropertyChange(PROPERTY_URL, old, urlString);
		//if (canvas != null) canvas.repaint();
	}

	public String getTitle() {
		return title == null ? "" : title;
	}
	
	protected void setTitle(String newTitle) {
		String old = title;
		title = newTitle;
		pcs.firePropertyChange(PROPERTY_TITLE, old, title);
		pcs.firePropertyChange(PROPERTY_LABEL, old, title);
		//if (canvas != null) canvas.repaint();
	}

	public String getRollover() {
		return rollover == null ? "" : rollover;
	}

	protected void setRollover(String newRollover) {
		String old = rollover;
		rollover = newRollover;
		pcs.firePropertyChange(PROPERTY_ROLLOVER, old, rollover);
		//if (canvas != null) canvas.repaint();
	}

	public int getRotations() {
		return rotations;
	}
	
	public void setRotations(int r) {
		r = r % 4;
		if (rotations == r) return;
		int old = rotations;
		rotations = r;
		if (view != null) {
			view.rotateProperty().set(90.0d * rotations);
		}
		pcs.firePropertyChange(PROPERTY_ROTATIONS, old, rotations);
		//if (canvas != null) canvas.repaint();
	}

	public int getScreen() {
		return screen;
	}
	
	public void setScreen(int s) {
		if (screen == s) return;
		int old = screen;
		screen = s;
		checkScreenSetup();
		pcs.firePropertyChange(PROPERTY_SCREEN, old, screen);
		//if (canvas != null) canvas.repaint();
	}

	public void setVisible(boolean visible) {
		super.setVisible(visible);
		checkScreenSetup();
	}
	
	public void checkScreenSetup() {
		if (!visible) {
			// make the panel not visible if it exists
			if (panel != null) {
				panel.setVisible(false);
			}
			return;
		}

		if (panel == null) {
			// create a new panel and add it to the screen 
			panel = new JPanel();
			panel.setLayout(new BorderLayout());
	        panel.add(getComponent(), BorderLayout.CENTER);
	        if (screenManager != null) {
	        	screenManager.addComponent(getID(), panel, screen);
		        onScreen = screen;
	        }
		} else {
			if (onScreen != screen && screenManager != null) {
				// if the panel has changed screens then move it
				screenManager.removeComponent(getID(), panel);
				screenManager.addComponent(getID(), panel, screen);
		        onScreen = screen;
			}
			// set it visible incase it is not visible
			panel.setVisible(true);
		}
	}
}