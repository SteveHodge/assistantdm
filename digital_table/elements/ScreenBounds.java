package digital_table.elements;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Area;
import java.awt.geom.Point2D;

import digital_table.controller.DisplayConfig;
import digital_table.server.MapCanvas.Order;

public class ScreenBounds extends MapElement {
	private static final long serialVersionUID = 1L;

	public final static String PROPERTY_COLOR = "color";	// Color
	public final static String PROPERTY_ALPHA = "alpha";	// float

	Property<Color> color = new Property<Color>(PROPERTY_COLOR, Color.LIGHT_GRAY, Color.class);
	Property<Float> alpha = new Property<Float>(PROPERTY_ALPHA, 0.5f, Float.class);

	@Override
	public Order getDefaultOrder() {
		return Order.TOP;
	}

	@Override
	public void paint(Graphics2D g, Point2D offset) {
		if (canvas == null || getVisibility() == Visibility.HIDDEN) return;

		Composite c = g.getComposite();
		g.setColor(color.getValue());
		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha.getValue()));
		Area area = new Area(g.getClip());
		for (DisplayConfig.Screen screen : DisplayConfig.screens) {
			if (screen.open) {
				Point2D topLeft = canvas.getRemote().getGridCoordinates(screen.location.x, screen.location.y);
				Point2D bottomRight = canvas.getRemote().getGridCoordinates(screen.location.x + screen.size.width, screen.location.y + screen.size.height);
				Point tl = canvas.getDisplayCoordinates(topLeft);
				Point br = canvas.getDisplayCoordinates(bottomRight);
				Area a = new Area(new Rectangle(tl.x, tl.y, br.x - tl.x, br.y - tl.y));
				area.subtract(a);
			}
		}
		g.fill(area);
		g.setComposite(c);
	}

	@Override
	public String toString() {
		return "Screen Bounds";
	}
}
