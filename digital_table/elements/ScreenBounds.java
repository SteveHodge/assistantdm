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

	Color color = Color.LIGHT_GRAY;
	float alpha = 0.5f;
	
	public Order getDefaultOrder() {
		return Order.TOP;
	}

	public void paint(Graphics2D g) {
		if (canvas == null || !visible) return;

		Composite c = g.getComposite();
		g.setColor(color);
		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
		Area area = new Area(g.getClip());
		for (DisplayConfig.Screen screen : DisplayConfig.screens) {
			if (screen.open) {
				Point2D topLeft = canvas.getRemoteGridCellCoords(screen.location.x, screen.location.y);
				Point2D bottomRight = canvas.getRemoteGridCellCoords(screen.location.x + screen.size.width, screen.location.y + screen.size.height);
				Point tl = canvas.getDisplayCoordinates(topLeft);
				Point br = canvas.getDisplayCoordinates(bottomRight);
				Area a = new Area(new Rectangle(tl.x, tl.y, br.x - tl.x, br.y - tl.y));
				area.subtract(a);
			}
		}
		g.fill(area);
		g.setComposite(c);
	}

	public String toString() {
		return "Screen Bounds";
	}

	public Color getColor() {
		return color;
	}
	
	public void setColor(Color c) {
		if (color.equals(c)) return;
		Color old = color;
		color = c;
		pcs.firePropertyChange(PROPERTY_COLOR, old, color);
		if (canvas != null) canvas.repaint();
	}

	public float getAlpha() {
		return alpha;
	}
	
	public void setAlpha(float a) {
		if (alpha == a) return;
		float old = alpha;
		alpha = a;
		pcs.firePropertyChange(PROPERTY_ALPHA, old, alpha);
		if (canvas != null) canvas.repaint();
	}

	public Object getProperty(String property) {
		if (property.equals(PROPERTY_COLOR)) {
			return getColor();
		} else if (property.equals(PROPERTY_ALPHA)) {
			return getAlpha();
		} else {
			// throw exception?
			return null;
		}
	}

	public void setProperty(String property, Object value) {
		if (property.equals(PROPERTY_COLOR)) {
			setColor((Color)value);
		} else if (property.equals(PROPERTY_ALPHA)) {
			setAlpha((Float)value);
		} else {
			// throw exception?
		}
	}
}
