package digital_table.controller;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import digital_table.server.TableDisplay;

public class DisplayConfig {
	public static Point[] defaultLocations = {
		new Point(65,250),	// 0 for the real table
		new Point(1421,3),
		new Point(64,1101),
		new Point(1425,1106),
		new Point(63,2202),
		new Point(1421,2207)
	};
	
	public static List<Screen> screens = new ArrayList<Screen>();

	static public class Screen {
		public int index;
		public Dimension size; 
		public Point location;	// location (in remote-native pixels) of the screen on the canvas
		public boolean open = false;
	}

	static void getScreens(TableDisplay display) {
		try {
			Rectangle[] bounds = display.getScreenBounds();
			screens.clear();
			for (int i = 0; i < bounds.length; i++) {
				System.out.println("Screen "+i+": "+bounds[i]);
				Screen s = new Screen();
				s.index = i;
				s.size = bounds[i].getSize();
				s.location = bounds[i].getLocation();
				screens.add(s);
			}
		} catch (RemoteException ex) {
			ex.printStackTrace();
		}
	}
}
