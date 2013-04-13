package digital_table.controller;


import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.rmi.RemoteException;
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
			final MonitorConfigFrame f = new MonitorConfigFrame(display);
			f.addWindowListener(new WindowListener() {
				public void windowClosed(WindowEvent arg0) {
					if (f.openScreens) openScreens(f);
				}
				public void windowActivated(WindowEvent arg0) {}
				public void windowClosing(WindowEvent arg0) {}
				public void windowDeactivated(WindowEvent arg0) {}
				public void windowDeiconified(WindowEvent arg0) {}
				public void windowIconified(WindowEvent arg0) {}
				public void windowOpened(WindowEvent arg0) {}
			});
		}
	}

	protected void openScreens(MonitorConfigFrame f) {
		try {
			for (int i = 0; i < f.screenNums.length; i++) {
				if (f.screenNums[i] >= 0) {
					DisplayConfig.Screen s = DisplayConfig.screens.get(f.screenNums[i]);
					s.location = DisplayConfig.defaultLocations[i];
					s.open = true;
				}
			}
			display.showScreens(f.screenNums,DisplayConfig.defaultLocations);
			ControllerFrame controller = new ControllerFrame(display);
			controller.addWindowListener(new WindowListener() {
				public void windowClosed(WindowEvent arg0) {
					System.exit(0);
				}
				public void windowActivated(WindowEvent arg0) {}
				public void windowClosing(WindowEvent arg0) {}
				public void windowDeactivated(WindowEvent arg0) {}
				public void windowDeiconified(WindowEvent arg0) {}
				public void windowIconified(WindowEvent arg0) {}
				public void windowOpened(WindowEvent arg0) {}
			});
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		if (System.getSecurityManager() == null) {
			System.setSecurityManager(new SecurityManager());
		}

		new DigitalTableController();
	}
}
