package digital_table.server;


import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.application.Platform;

import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import digital_table.elements.MapElement;


//TODO JavaFX platform stuff should only be called if necessary (once Browser is added)

public class DigitalTable implements TableDisplay, ScreenManager {
	static final String imageFile = "D:\\Ptolus\\Maps\\DeluxeCityMap\\Ptolus_1px_to_5ft.png";
	//static final String imageFile = "c:\\Ptolus\\Maps\\DeluxeCityMap\\Ptolus_1px_to_5ft.png";

	static final int[] xOffsets = {65, 1421, 64, 1425, 63, 1421};	// relative x location of each screen
	//static final int[] yOffsets = {0, 3, 1101, 1106, 2202, 2207};	// relative y location of each screen
	static final int[] yOffsets = {250, 3, 1101, 1106, 2202, 2207};	// relative y location of each screen

	JFrame[] idFrames = null;
	JFrame[] screens = new JFrame[6];
	
	Map<Integer,List<JComponent>> components = new HashMap<Integer,List<JComponent>>();
	MapCanvas canvas = new MapCanvas();

    public DigitalTable() {
        super();
    }

    public static void main(String[] args) {
        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager());
        }
        try {
            String name = "TableDisplay";
            TableDisplay engine = new DigitalTable();
            TableDisplay stub = (TableDisplay) UnicastRemoteObject.exportObject(engine, 0);
            Registry registry = LocateRegistry.getRegistry();
            registry.rebind(name, stub);
            System.out.println("DigitalTable bound");
            Platform.setImplicitExit(false);
        } catch (Exception e) {
            System.err.println("DigitalTable exception:");
            e.printStackTrace();
        }
    }

	public Object[] getScreenList() {
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice[] devs = ge.getScreenDevices();
		Object[] list = new Object[devs.length+1];
		list[0] = "not allocated";
		for (int i = 1; i < list.length; i++) {
			list[i] = "" + i;
		}
		return list;
	}

	public void showScreens(int[] screenNums) {
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
				screens[i] = makeFrame(devs[screenNums[i]].getDefaultConfiguration(), xOffsets[i], yOffsets[i]);
				screens[i].setVisible(true);
			}
		}
	}

	/*
	 * x, y define the position of the top left of the screen in the virtual space
	 */
	JFrame makeFrame(GraphicsConfiguration config, int x, int y) {
		JFrame frame = new JFrame(config);
		frame.setBounds(config.getBounds());
		frame.setUndecorated(true);
		frame.setResizable(false);
		//JPanel p = new GridPanel(x, y);
		//JLayeredPane layeredPane = new JLayeredPane();
		JPanel p = new MapViewport(canvas, x, y);
		//layeredPane.add(p);
		frame.add(p);
		frame.validate();
		return frame;
	}

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
			JLabel label = new JLabel(""+(i+1), SwingConstants.CENTER);
			label.setFont(label.getFont().deriveFont(300.0f));
			idFrames[i].add(label, BorderLayout.CENTER);
			idFrames[i].pack();
			Rectangle bounds = config.getBounds();
			bounds.width -= idFrames[i].getWidth();
			bounds.height -= idFrames[i].getHeight();
			idFrames[i].setLocation(bounds.x+bounds.width/2, bounds.y+bounds.height/2);
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

	public void quit() throws RemoteException {
		// need to use invokeLater so this method has a chance to return (otherwise we'll get an exception on the client)
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				Platform.exit();
				System.exit(0);
			}
		});
	}

	public void addElement(MapElement element) {
		element.setScreenMananger(this);
		canvas.addElement(element);
	}

	public void removeElement(int id) {
		List<JComponent> comps = components.get(id);
		for (JComponent component : comps) {
			Container parent =  component.getParent();
			if (parent != null) {
				parent.remove(component);
				parent.repaint();
			}
		}
		components.remove(id);
		canvas.removeElement(id);
	}
	
	public void reorderElement(int id, int index) throws RemoteException {
		MapElement e = canvas.getElement(id);
		if (e != null) {
			DefaultListModel model = (DefaultListModel)canvas.getModel();
			model.removeElement(e);
			model.add(index, e);
		}
	}

	public void setElementProperty(int id, String property, Object value) {
		MapElement e = canvas.getElement(id);
		if (e != null) {
			e.setProperty(property, value);
		}
	}

	public void setElementVisible(int id, boolean visible) {
		MapElement e = canvas.getElement(id);
		if (e != null) {
			e.setVisible(visible);
		}
	}

	public void setElementPosition(int id, Point2D point) throws RemoteException {
		MapElement e = canvas.getElement(id);
		if (e != null) {
			e.setLocation(point);
		}
	}

	public void addComponent(int id, JComponent component, int screen) {
		JFrame frame = screens[screen];
		if (frame != null) {
			//JLayeredPane l = (JLayeredPane)frame.getContentPane().getComponent(0);
			JLayeredPane l = frame.getLayeredPane();
			l.add(component,1);	// TODO should set the number according to the element's position
			List<JComponent> comps = components.get(id);
			if (comps == null) {
				comps = new ArrayList<JComponent>();
				components.put(id, comps);
			}
			comps.add(component);
			Dimension size = frame.getContentPane().getSize();
			Dimension popupSize = new Dimension(Math.min(size.width, size.height), Math.min(size.width, size.height));	// make largest possible square
			component.setSize(popupSize);
			component.setLocation((size.width-popupSize.width)/2, (size.height-popupSize.height)/2);	// centre on screen
			component.repaint();
		}
	}

	public void removeComponent(int id, JComponent component) {
		List<JComponent> comps = components.get(id);
		Container parent =  component.getParent();
		if (parent != null && comps.remove(component)) {
			parent.remove(component);
			parent.repaint();
		}
	}
}
