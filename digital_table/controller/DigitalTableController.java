package digital_table.controller;


import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import javafx.application.Platform;

import digital_table.server.TableDisplay;
import digital_table.controller.MonitorConfigFrame;

/*
 * LTM190EX-L31 has pixel pitch of 0.294mm. 1280x1024 pixels in 376.32 x 301.056 mm display.
 * One inch grid should be 25.4/0.294 pixels (86.395)
 */

public class DigitalTableController {
	TableDisplay display;

	public DigitalTableController() {
		try {
			String name = "TableDisplay";
			Registry registry = LocateRegistry.getRegistry("corto");
			display = (TableDisplay) registry.lookup(name);
		} catch (Exception e) {
			System.err.println("TableDisplay exception:" + e.getMessage());
			e.printStackTrace();
		}

		if (display != null) {
            Platform.setImplicitExit(false);
			new MonitorConfigFrame(display);
		}
	}

	public static void main(String[] args) {
		if (System.getSecurityManager() == null) {
			System.setSecurityManager(new SecurityManager());
		}

		new DigitalTableController();
	}
}
