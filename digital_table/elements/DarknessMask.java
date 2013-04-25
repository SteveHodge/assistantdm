package digital_table.elements;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Area;
import java.util.ArrayList;
import java.util.List;

import digital_table.server.MapCanvas.Order;

public class DarknessMask extends MapElement {
	private static final long serialVersionUID = 1L;

	public final static String PROPERTY_COLOR = "color";	// Color
	public final static String PROPERTY_ALPHA = "alpha";	// float
	public final static String PROPERTY_MASKCELL = "mask";	// Point - when this property is set the specified point will be masked
	public final static String PROPERTY_UNMASKCELL = "unmask";	// Point - when this property is set the specified point will be cleared

	Property<Color> color = new Property<Color>(PROPERTY_COLOR, Color.BLACK, Color.class);
	Property<Float> alpha = new Property<Float>(PROPERTY_ALPHA, 1.0f, Float.class);
	
	List<Point> cleared = new ArrayList<Point>();
	
	public Order getDefaultOrder() {
		return Order.ABOVEGRID;
	}

	public void paint(Graphics2D g) {
		if (canvas == null || !visible.getValue()) return;

		g.setColor(color.getValue());
		Composite c = g.getComposite();
		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha.getValue()));

		// build the shape
		Area area = new Area(g.getClip());
		// using indexed loop instead of iterator to avoid concurrency issues
		for (int i = 0; i < cleared.size(); i++) {
			Point p = cleared.get(i);
			Point tl = canvas.getDisplayCoordinates(p.x, p.y);
			Point br = canvas.getDisplayCoordinates(p.x+1, p.y+1);
			area.subtract(new Area(new Rectangle(tl.x, tl.y, br.x - tl.x, br.y - tl.y)));
		}

		g.fill(area);
		g.setComposite(c);
	}

	public String toString() {
		return "Darkness ("+getID()+")";
	}
	
	public boolean isMasked(Point p) {
		if (cleared.contains(p)) return false;
		return true;
	}
	
	public void setMasked(Point p, boolean mask) {
		if (mask) {
			cleared.remove(p);
			canvas.repaint();
		} else if (!cleared.contains(p)) {
			cleared.add(p);
			canvas.repaint();
		}
	}

	public void setProperty(String property, Object value) {
		if (property.equals(PROPERTY_MASKCELL)) {
			setMasked((Point)value, true);
		} else if (property.equals(PROPERTY_UNMASKCELL)) {
			setMasked((Point)value, false);
		} else {
			super.setProperty(property, value);
		}
	}
}
