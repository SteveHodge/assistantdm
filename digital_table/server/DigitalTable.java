package digital_table.server;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Rectangle;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.application.Platform;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import digital_table.elements.MapElement;

//TODO JavaFX platform stuff should only be called if necessary (once Browser is added)

public class DigitalTable implements TableDisplay, ScreenManager {
	static final String SERVICE_NAME = "TableDisplay";

	//static final int[] xOffsets = {65, 1421, 64, 1425, 63, 1421};	// relative x location of each screen
	//static final int[] yOffsets = {0, 3, 1101, 1106, 2202, 2207};	// relative y location of each screen
	//static final int[] yOffsets = {250, 3, 1101, 1106, 2202, 2207};	// relative y location of each screen

	JFrame[] idFrames = null;
	JFrame[] screens = new JFrame[6];

	Map<Integer, List<JComponent>> components = new HashMap<Integer, List<JComponent>>();
	MapCanvas canvas = new MapCanvas();

	Registry registry = null;

	public DigitalTable() {
		super();
	}

	public static void main(String[] args) {
		if (System.getSecurityManager() == null) {
			System.setSecurityManager(new SecurityManager());
		}
		try {
			TableDisplay engine = new DigitalTable();
			TableDisplay stub = (TableDisplay) UnicastRemoteObject.exportObject(engine, 0);
			//Registry registry = LocateRegistry.getRegistry();
			Registry registry = LocateRegistry.createRegistry(1099);
			registry.rebind(SERVICE_NAME, stub);
			System.out.println("DigitalTable bound");
			Platform.setImplicitExit(false);
		} catch (Exception e) {
			System.err.println("DigitalTable exception:");
			e.printStackTrace();
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
	 * x, y define the position of the top left of the screen in the virtual space
	 */
	JFrame makeFrame(GraphicsConfiguration config, Point viewOffset) {
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

	protected void showIDs() {
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

	protected void hideIDs() {
		if (idFrames != null) {
			for (int i = 0; i < idFrames.length; i++) {
				if (idFrames[i] != null) {
					idFrames[i].dispose();
				}
			}
			idFrames = null;
		}
	}

	@Override
	public void requestExit() throws RemoteException {
		hideIDs();
		for (int i = 0; i < screens.length; i++) {
			if (screens[i] != null) {
				screens[i].setVisible(false);
				screens[i].dispose();
				screens[i] = null;
			}
		}
		Platform.exit();

		new Thread() {
			@Override
			public void run() {
				try {
					sleep(2000);
					System.out.println("Shutting down");
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

		System.out.println("Quit complete");
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
		if (e != null) {
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
				comps = new ArrayList<JComponent>();
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
}
