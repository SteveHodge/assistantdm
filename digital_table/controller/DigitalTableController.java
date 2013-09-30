package digital_table.controller;


import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import javafx.application.Platform;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import util.XMLUtils;
import camera.CameraPanel;
import digital_table.server.TableDisplay;

/*
 * LTM190EX-L31 has pixel pitch of 0.294mm. 1280x1024 pixels in 376.32 x 301.056 mm display.
 * One inch grid should be 25.4/0.294 pixels (86.395)
 */

public class DigitalTableController {
	TableDisplay display;
	CameraPanel camera;
	ControllerFrame controller = null;

	public DigitalTableController() {
		this("corto");
		//		this("wintermute");
	}

	public DigitalTableController(String server) {
		this(server, null);
	}

	public DigitalTableController(String server, CameraPanel camera) {
		this.camera = camera;

		try {
			String name = "TableDisplay";
			Registry registry = LocateRegistry.getRegistry(server);
			display = (TableDisplay)registry.lookup(name);
		} catch (Exception e) {
			System.err.println("TableDisplay exception:" + e.getMessage());
			//e.printStackTrace();
		}

		if (display != null) {
			Platform.setImplicitExit(false);
			final MonitorConfigFrame f = new MonitorConfigFrame(display);
			f.addWindowListener(new WindowListener() {
				@Override
				public void windowClosed(WindowEvent arg0) {
					if (f.openScreens) openScreens(f);
				}
				@Override
				public void windowActivated(WindowEvent arg0) {}
				@Override
				public void windowClosing(WindowEvent arg0) {}
				@Override
				public void windowDeactivated(WindowEvent arg0) {}
				@Override
				public void windowDeiconified(WindowEvent arg0) {}
				@Override
				public void windowIconified(WindowEvent arg0) {}
				@Override
				public void windowOpened(WindowEvent arg0) {}
			});
		}
	}

	protected void saveDisplay() {
		if (controller != null) {
			try {
				Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
				doc.appendChild(controller.getElement(doc));
				XMLUtils.writeDOM(doc, new File("display.xml"));
			} catch (ParserConfigurationException e) {
				e.printStackTrace();
			}
		}
	}

	protected void quit() {
		saveDisplay();
		System.exit(0);
	}

	private void openScreens(MonitorConfigFrame f) {
		try {
			for (int i = 0; i < f.screenNums.length; i++) {
				if (f.screenNums[i] >= 0) {
					DisplayConfig.Screen s = DisplayConfig.screens.get(f.screenNums[i]);
					s.location = DisplayConfig.defaultLocations[i];
					s.open = true;
				}
			}
			display.showScreens(f.screenNums,DisplayConfig.defaultLocations);
			File xmlFile = new File("display.xml");
			Document dom = null;
			if (xmlFile.exists()) {
				DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
				//InputStream is = p.getClass().getClassLoader().getResourceAsStream("party.xsd");
				//factory.setSchema(SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI).newSchema(new StreamSource(is)));
				try {
					dom = factory.newDocumentBuilder().parse(xmlFile);
				} catch (SAXException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (ParserConfigurationException e) {
					e.printStackTrace();
				}
			}
			controller = new ControllerFrame(display, camera);
			controller.addWindowListener(new WindowListener() {
				@Override
				public void windowClosed(WindowEvent arg0) {
					quit();
				}
				@Override
				public void windowActivated(WindowEvent arg0) {}
				@Override
				public void windowClosing(WindowEvent arg0) {}
				@Override
				public void windowDeactivated(WindowEvent arg0) {}
				@Override
				public void windowDeiconified(WindowEvent arg0) {}
				@Override
				public void windowIconified(WindowEvent arg0) {}
				@Override
				public void windowOpened(WindowEvent arg0) {}
			});
			if (dom != null) controller.parseDOM(dom.getDocumentElement());
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	protected Node getElement(Document doc) {
		if (controller == null) return null;
		return controller.getElement(doc);
	}

	public static void main(String[] args) {
		if (System.getSecurityManager() == null) {
			System.setSecurityManager(new SecurityManager());
		}

		new DigitalTableController();
	}
}
