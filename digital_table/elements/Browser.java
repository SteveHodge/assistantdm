package digital_table.elements;

import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

@SuppressWarnings("restriction")
public abstract class Browser extends MapElement {
	private static final long serialVersionUID = 1L;

	public static final String PROPERTY_URL = "url";		// String
	public static final String PROPERTY_TITLE = "title";	// String - read only
	public static final String PROPERTY_LABEL = "label";	// String - read only - fired when title changes
	public static final String PROPERTY_ROLLOVER = "rollover";	// String - read only
	public final static String PROPERTY_ROTATIONS = "rotations";	// int - number of quadrants rotated clockwise
	public final static String PROPERTY_SCREEN = "screen";	// String

	Property<Integer> screen;
	Property<String> title = new Property<String>(PROPERTY_TITLE, false, "", String.class) {
		private static final long serialVersionUID = 1L;

		@Override
		public void setValue(String v) {
			if (value.equals(v)) return;
			if (v == null) v = "";
			String old = value;
			super.setValue(v);
			pcs.firePropertyChange(PROPERTY_LABEL, old, title);
		}
	};

	Property<String> rollover = new Property<String>(PROPERTY_ROLLOVER, false, "", String.class) {
		private static final long serialVersionUID = 1L;

		@Override
		public void setValue(String s) {
			if (s == null) s = "";
			super.setValue(s);
		}
	};

	Property<String> urlString = new Property<String>(PROPERTY_URL, false, "", String.class) {
		private static final long serialVersionUID = 1L;

		@Override
		public void setValue(String v) {
			setURL(v, true);
		}
	};

	Property<Integer> rotations = new Property<Integer>(PROPERTY_ROTATIONS, false, 0, Integer.class) {
		private static final long serialVersionUID = 1L;

		@Override
		public void setValue(Integer r) {
			r = r % 4;
			if (value == r) return;
			int old = value;
			value = r;
			if (view != null) {
				view.rotateProperty().set(90.0d * value);
			}
			pcs.firePropertyChange(PROPERTY_ROTATIONS, old, rotations);
			//if (canvas != null) canvas.repaint();
		}
	};


	transient protected JFXPanel jfxPanel = null;
	transient protected WebEngine engine;
	transient protected WebView view;

	protected Browser() {
	}

	protected Browser(int id) {
		super(id);
	}

	@Override
	public Layer getDefaultLayer() {
		return Layer.INFORMATION;
	}

	public JComponent getComponent() {
		if (jfxPanel == null) {
			jfxPanel = new JFXPanel();
			createScene();

			if (urlString.getValue() == null || urlString.getValue().length() == 0) {
				loadURL("http://oracle.com");
			} else {
				loadURL(urlString.getValue());
			}
		}
		return jfxPanel;
	}

	private void createScene() {
		Platform.runLater(() -> {
			view = new WebView();
			view.rotateProperty().set(90.0d * rotations.getValue());
			engine = view.getEngine();

			engine.titleProperty().addListener((observable, oldValue, newValue) -> SwingUtilities.invokeLater(() -> title.setValue(newValue)));

			engine.setOnStatusChanged(event -> SwingUtilities.invokeLater(() -> rollover.setValue(event.getData())));

			engine.locationProperty().addListener((ov, oldValue, newValue) -> SwingUtilities.invokeLater(() -> setURL(newValue, false)));

			engine.getLoadWorker().workDoneProperty().addListener((observableValue, oldValue, newValue) -> SwingUtilities.invokeLater(() -> {
				//progressBar.setValue(newValue.intValue());
			}));

			engine.getLoadWorker().exceptionProperty().addListener((o, old, value) -> {
				if (engine.getLoadWorker().getState() == javafx.concurrent.Worker.State.FAILED) {
					SwingUtilities.invokeLater(() -> {
//                                    JOptionPane.showMessageDialog(
//                                            panel,
//                                            (value != null) ?
//                                            engine.getLocation() + "\n" + value.getMessage() :
//                                            engine.getLocation() + "\nUnexpected error.",
//                                            "Loading error...",
//                                            JOptionPane.ERROR_MESSAGE);
					});
				}
			});

			jfxPanel.setScene(new Scene(view));
		});
	}

	@Override
	public String toString() {
		if (title.getValue() == null || title.getValue().length() == 0) return "Browser ("+getID()+")";
		return "Browser ("+title+")";
	}

	public void loadURL(final String url) {
		if (jfxPanel == null) return;
		Platform.runLater(() -> {
			String tmp = toURL(url);
			if (tmp == null) {
				tmp = toURL("http://" + url);
			}
			engine.load(tmp);
		});
	}

	private static String toURL(String str) {
		try {
			return new URL(str).toExternalForm();
		} catch (MalformedURLException exception) {
			return null;
		}
	}

	@Override
	public void setProperty(String property, Object value) {
		if (property.equals(PROPERTY_TITLE)) {
			// read only
		} else if (property.equals(PROPERTY_ROLLOVER)) {
			// read only
		} else {
			super.setProperty(property, value);
		}
	}

	protected void setURL(String url, boolean load) {
		if (urlString.getValue().equals(url) && !load) return;
		String old = urlString.getValue();
		urlString.value = url;
		if (load) loadURL(url);
		pcs.firePropertyChange(PROPERTY_URL, old, urlString.getValue());
	}
}