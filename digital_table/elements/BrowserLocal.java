package digital_table.elements;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Point2D;

import digital_table.controller.DisplayConfig;
import digital_table.server.MeasurementLog;

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
	public void paint(Graphics2D g) {
		lastPaintTime = 0;
		long startTime = System.nanoTime();

		if (getVisibility() == Visibility.HIDDEN || canvas == null) return;
		// show bounds of the browser on the map based on the screen
		Composite c = g.getComposite();
		g.setColor(color);
		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
		DisplayConfig.Screen s = DisplayConfig.screens.get(screen.getValue());
		int browserSize = Math.min(s.size.width, s.size.height);
		int x = s.location.x + (s.size.width - browserSize) / 2;
		int y = s.location.y + (s.size.height - browserSize) / 2;
		Point2D topLeft = canvas.getRemote().convertDisplayCoordsToCanvas(x, y);
		Point2D bottomRight = canvas.getRemote().convertDisplayCoordsToCanvas(x + browserSize, y + browserSize);
		Point tl = canvas.convertCanvasCoordsToDisplay(topLeft);
		Point br = canvas.convertCanvasCoordsToDisplay(bottomRight);
		g.fillRect(tl.x, tl.y, br.x - tl.x, br.y - tl.y);
		g.setComposite(c);
		g.setColor(Color.BLACK);
		g.drawRect(tl.x, tl.y, br.x - tl.x, br.y - tl.y);
		// TODO should show the title in the area

		lastPaintTime = (System.nanoTime() - startTime) / 1000;
	}

	long lastPaintTime = 0;

	@Override
	public MeasurementLog getPaintTiming() {
		MeasurementLog m = new MeasurementLog("BrowserLocal (" + title.getValue() + ")", id);
		m.total = lastPaintTime;
		return m;
	}
}
