package digital_table.elements;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Point2D;

import digital_table.controller.DisplayConfig;

// TODO delay jfxPanel creation until the user sets a URL or makes the remote visible

public class BrowserLocal extends Browser {
	private static final long serialVersionUID = 1L;

	protected Color color = Color.LIGHT_GRAY;
	protected float alpha = 0.5f;

	public BrowserLocal(BrowserRemote remote) {
		super(remote.getID());
		screen = new Property<Integer>(PROPERTY_SCREEN, 0, Integer.class);
		getComponent();	// we need a browser to preload the page
	}

	@Override
	public void paint(Graphics2D g, Point2D offset) {
		if (isVisible() && canvas != null) {
			// show bounds of the browser on the map based on the screen - this should only run on the client
			Composite c = g.getComposite();
			g.setColor(color);
			g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
			DisplayConfig.Screen s = DisplayConfig.screens.get(screen.getValue());
			int browserSize = Math.min(s.size.width, s.size.height);
			int x = s.location.x + (s.size.width - browserSize) / 2;
			int y = s.location.y + (s.size.height - browserSize) / 2;
			Point2D topLeft = canvas.getRemoteGridCellCoords(x, y);
			Point2D bottomRight = canvas.getRemoteGridCellCoords(x + browserSize, y + browserSize);
			Point tl = canvas.getDisplayCoordinates(topLeft);
			Point br = canvas.getDisplayCoordinates(bottomRight);
			g.fillRect(tl.x, tl.y, br.x - tl.x, br.y - tl.y);
			g.setComposite(c);
			g.setColor(Color.BLACK);
			g.drawRect(tl.x, tl.y, br.x - tl.x, br.y - tl.y);
			// TODO should show the title in the area
		}
	}
}
