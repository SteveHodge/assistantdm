package digital_table.server;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Rectangle;
import java.net.URI;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import digital_table.elements.MapElement;
import digital_table.elements.MapElement.Layer;
import javafx.application.Platform;

//TODO JavaFX platform stuff should only be called if necessary (once Browser is added)

@SuppressWarnings("restriction")
public class DigitalTable implements TableDisplay, ScreenManager {
	private static final String SERVICE_NAME = "TableDisplay";

	private static final Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

	private JFrame[] idFrames = null;
	private JFrame[] screens = new JFrame[6];

	private Map<Integer, List<JComponent>> components = new HashMap<>();
	private MapCanvas canvas = new MapCanvas();

	public DigitalTable() {
		super();
	}

	public static void main(String[] args) {
		if (System.getSecurityManager() == null) {
			System.setSecurityManager(new SecurityManager());
		}

		logger.setLevel(Level.OFF);

		try {
			TableDisplay engine = new DigitalTable();
			TableDisplay stub = (TableDisplay) UnicastRemoteObject.exportObject(engine, 0);
			//Registry registry = LocateRegistry.getRegistry();
			Registry registry = LocateRegistry.createRegistry(1099);
			registry.rebind(SERVICE_NAME, stub);
			System.out.println("DigitalTable bound");
			logger.info("DigitalTable bound");
			Platform.setImplicitExit(false);
		} catch (Exception e) {
			System.err.println("DigitalTable exception:");
			e.printStackTrace();
			logger.log(Level.SEVERE, e.getMessage(), e);
			System.exit(0);
		}
	}

	@Override
	public Rectangle[] getScreenBounds() {
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice[] devs = ge.getScreenDevices();
		Rectangle[] list = new Rectangle[devs.length];
		for (int i = 0; i < list.length; i++) {
			GraphicsConfiguration config = devs[i].getDefaultConfiguration();
			list[i] = config.getBounds();
		}
		return list;
	}

	@Override
	public void showScreens(int[] screenNums, Point[] offsets) {
		hideIDs();

		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice[] devs = ge.getScreenDevices();

		for (int i = 0; i < screens.length; i++) {
			if (screens[i] != null) {
				screens[i].setVisible(false);
				screens[i].dispose();
				screens[i] = null;
			}
			if (screenNums[i] >= 0) {
				screens[i] = makeFrame(devs[screenNums[i]].getDefaultConfiguration(), offsets[i]);
				screens[i].setVisible(true);
			}
		}
	}

	/*
	 * viewOffset defines the position of the top left of the screen in the virtual space
	 */
	private JFrame makeFrame(GraphicsConfiguration config, Point viewOffset) {
		JFrame frame = new JFrame(config);
		frame.setBounds(config.getBounds());
		frame.setUndecorated(true);
		frame.setResizable(false);
		//JPanel p = new GridPanel(x, y);
		//JLayeredPane layeredPane = new JLayeredPane();
		JPanel p = new MapViewport(canvas, viewOffset.x, viewOffset.y);
		//layeredPane.add(p);
		frame.add(p);
		frame.validate();
		return frame;
	}

	@Override
	public void setScreenIDsVisible(boolean visible) {
		hideIDs();
		if (visible) showIDs();
	}

	private void showIDs() {
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice[] devs = ge.getScreenDevices();

		idFrames = new JFrame[devs.length];
		for (int i = 0; i < devs.length; i++) {
			GraphicsConfiguration config = devs[i].getDefaultConfiguration();
			idFrames[i] = new JFrame(config);
			idFrames[i].setUndecorated(true);
			JLabel label = new JLabel("" + (i + 1), SwingConstants.CENTER);
			label.setFont(label.getFont().deriveFont(300.0f));
			idFrames[i].add(label, BorderLayout.CENTER);
			idFrames[i].pack();
			Rectangle bounds = config.getBounds();
			bounds.width -= idFrames[i].getWidth();
			bounds.height -= idFrames[i].getHeight();
			idFrames[i].setLocation(bounds.x + bounds.width / 2, bounds.y + bounds.height / 2);
			idFrames[i].setVisible(true);
		}
	}

	private void hideIDs() {
		if (idFrames != null) {
			for (int i = 0; i < idFrames.length; i++) {
				if (idFrames[i] != null) {
					idFrames[i].dispose();
				}
			}
			idFrames = null;
		}
	}

	private void closeScreens() {
		for (int i = 0; i < screens.length; i++) {
			if (screens[i] != null) {
				screens[i].setVisible(false);
				screens[i].dispose();
				screens[i] = null;
			}
		}
	}

	@Override
	public void requestExit() throws RemoteException {
		logger.info("Quit requested");

		hideIDs();
		closeScreens();
		Platform.exit();

		new Thread() {
			@Override
			public void run() {
				try {
					sleep(2000);
					System.out.println("Shutting down");
					logger.info("Shutting down");
					Registry registry = LocateRegistry.getRegistry();
					try {
						registry.unbind(SERVICE_NAME);
					} catch (NotBoundException e) {
						System.out.println(e.getMessage());
					}
					UnicastRemoteObject.unexportObject(DigitalTable.this, false);
				} catch (Exception e) {
				}
				System.exit(0);
			}
		}.start();
	}

	@Override
	public void setOffset(int offx, int offy) {
		canvas.setOffset(offx, offy);
	}

	@Override
	public void addElement(MapElement element) {
		element.setScreenMananger(this);
		canvas.addElement(element, null);
	}

	@Override
	public void addElement(MapElement element, int parent) {
		MapElement p = canvas.getElement(parent);
		element.setScreenMananger(this);
		canvas.addElement(element, p);
	}

	@Override
	public void changeParent(int id, int parent) throws RemoteException {
		MapElement e = canvas.getElement(id);
		MapElement p = canvas.getElement(parent);
		if (e != null) {
			canvas.changeParent(e, p);
		}
	}

	@Override
	public void removeElement(int id) {
		List<JComponent> comps = components.get(id);
		if (comps != null) {
			for (JComponent component : comps) {
				Container parent = component.getParent();
				if (parent != null) {
					parent.remove(component);
					parent.repaint();
				}
			}
		}
		components.remove(id);
		canvas.removeElement(id);
	}

	@Override
	public void promoteElement(int id) {
		MapElement e = canvas.getElement(id);
		if (e != null) canvas.promoteElement(e);
	}

	@Override
	public void demoteElement(int id) {
		MapElement e = canvas.getElement(id);
		if (e != null) canvas.demoteElement(e);
	}

	@Override
	public void setElementProperty(int id, String property, Object value) {
		MapElement e = canvas.getElement(id);
		if (e == null) return;
		if (property.equals(MapElement.PROPERTY_LAYER)) {
			canvas.changeLayer(e, (Layer) value);
		} else {
			e.setProperty(property, value);
		}
	}

	@Override
	public void addComponent(int id, JComponent component, int screen) {
		JFrame frame = screens[screen];
		if (frame != null) {
			//JLayeredPane l = (JLayeredPane)frame.getContentPane().getComponent(0);
			JLayeredPane l = frame.getLayeredPane();
			l.add(component, 1);	// TODO should set the number according to the element's position
			List<JComponent> comps = components.get(id);
			if (comps == null) {
				comps = new ArrayList<>();
				components.put(id, comps);
			}
			comps.add(component);
			Dimension size = frame.getContentPane().getSize();
			Dimension popupSize = new Dimension(Math.min(size.width, size.height), Math.min(size.width, size.height));	// make largest possible square
			component.setSize(popupSize);
			component.setLocation((size.width - popupSize.width) / 2, (size.height - popupSize.height) / 2);	// centre on screen
			component.repaint();
		}
	}

	@Override
	public void removeComponent(int id, JComponent component) {
		List<JComponent> comps = components.get(id);
		Container parent = component.getParent();
		if (parent != null && comps.remove(component)) {
			parent.remove(component);
			parent.repaint();
		}
	}

	@Override
	public boolean hasMedia(URI uri) throws RemoteException {
		return MediaManager.INSTANCE.hasMedia(uri);
	}

	@Override
	public void addMedia(URI uri, byte[] bytes) throws RemoteException {
		MediaManager.INSTANCE.addMedia(uri, bytes);
	}

	@Override
	public MeasurementLog getMemoryUsage() throws RemoteException {
		return canvas.getMemoryUsage();
	}

	@Override
	public MeasurementLog getPaintTiming() throws RemoteException {
		return canvas.getPaintTiming();
	}
}
