package digital_table.elements;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Area;
import java.awt.geom.Point2D;

import digital_table.server.MapCanvas.Order;

public class ScreenBounds extends MapElement {
	private static final long serialVersionUID = 1L;

	static final int[] xOffsets = {65, 1421, 64, 1425, 63, 1421};	// relative x location of each screen
	//static final int[] yOffsets = {0, 3, 1101, 1106, 2202, 2207};	// relative y location of each screen
	static final int[] yOffsets = {250, 3, 1101, 1106, 2202, 2207};	// relative y location of each screen

	public final static String PROPERTY_COLOR = "color";	// Color
	public final static String PROPERTY_ALPHA = "alpha";	// float

	Color color = Color.LIGHT_GRAY;
	float alpha = 0.25f;
	
	public Order getDefaultOrder() {
		return Order.Top;
	}

	public void paint(Graphics2D g) {
		if (canvas == null || !visible) return;

		Composite c = g.getComposite();
		g.setColor(color);
		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
		Area area = new Area(g.getClip());
		for (int i = 0; i < xOffsets.length; i++) {
			// TODO these conversions should not be hard-coded
			Point2D.Double topLeft = new Point2D.Double((double)xOffsets[i] * 294 / 25400, (double)yOffsets[i] * 294 / 25400);
			Point2D.Double bottomRight = new Point2D.Double(((double)xOffsets[i] + 1280) * 294 / 25400, 
					((double)yOffsets[i] + 1024) * 294 / 25400);
			Point tl = canvas.getDisplayCoordinates(topLeft);
			Point br = canvas.getDisplayCoordinates(bottomRight);
			Area a = new Area(new Rectangle(tl.x, tl.y, br.x - tl.x, br.y - tl.y));
			area.subtract(a);
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
