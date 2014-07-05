package digital_table.controller;


import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import util.ModuleRegistry;
import util.XMLUtils;
import digital_table.server.TableDisplay;

/*
 * LTM190EX-L31 has pixel pitch of 0.294mm. 1280x1024 pixels in 376.32 x 301.056 mm display.
 * One inch grid should be 25.4/0.294 pixels (86.395)
 */

public class DigitalTableController implements DigitalTableModule {
	TableDisplay display;
	ControllerFrame controller = null;

	public DigitalTableController() {
		openRemote("corto");
		//		this("wintermute");
		ModuleRegistry.register(DigitalTableModule.class, this);
	}

	public DigitalTableController(String server) {
		openRemote(server);
		ModuleRegistry.register(DigitalTableModule.class, this);
	}

	public boolean isOpen() {
		return controller != null;
	}

	public void openRemote(String server) {
		controller = new ControllerFrame();
		controller.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosed(WindowEvent e) {
				quit();
			}
		});

		RemoteConnection.attemptConnection(server, controller::setRemote);
	}

	public void close() {
		saveDisplay();
		if (controller != null) {
			controller.quit();
			controller = null;
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
		close();
		System.exit(0);
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

	@Override
	public void moduleExit() {
		close();
	}

	@Override
	public void setCalibrateDisplay(boolean show) {
		if (controller != null) controller.setCalibrateDisplay(show);
	}
}
