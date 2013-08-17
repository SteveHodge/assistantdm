package digital_table.elements;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Area;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ListModel;

import digital_table.server.MapCanvas.Order;

public class DarknessMask extends MapElement {
	private static final long serialVersionUID = 1L;

	public final static String PROPERTY_COLOR = "color";	// Color
	public final static String PROPERTY_ALPHA = "alpha";	// float
	public final static String PROPERTY_LOW_LIGHT = "low_light";	// boolean
	public final static String PROPERTY_MASKCELL = "mask";	// Point - when this property is set the specified point will be masked
	public final static String PROPERTY_UNMASKCELL = "unmask";	// Point - when this property is set the specified point will be cleared

	Property<Color> color = new Property<Color>(PROPERTY_COLOR, Color.BLACK, Color.class);
	Property<Float> alpha = new Property<Float>(PROPERTY_ALPHA, 1.0f, Float.class);
	Property<Boolean> lowLight = new Property<Boolean>(PROPERTY_LOW_LIGHT, false, Boolean.class);

	List<Point> cleared = new ArrayList<Point>();

	@Override
	public Order getDefaultOrder() {
		return Order.ABOVEGRID;
	}

	@Override
	public void paint(Graphics2D g, Point2D offset) {
		if (canvas == null || getVisibility() == Visibility.HIDDEN) return;

		// build the shape
		Area dark = new Area(g.getClip());
		Area shadow = new Area();
		Area lowLight = new Area();

		// using indexed loop instead of iterator to avoid concurrency issues
		for (int i = 0; i < cleared.size(); i++) {
			Point p = cleared.get(i);
			Point tl = canvas.getDisplayCoordinates(p.x, p.y);
			Point br = canvas.getDisplayCoordinates(p.x+1, p.y+1);
			dark.subtract(new Area(new Rectangle(tl.x, tl.y, br.x - tl.x, br.y - tl.y)));
		}

		ListModel m = canvas.getModel();
		// go through and remove any illuminated areas from the mask
		// also create a mask of all shadowed area
		for (int i = 0; i < m.getSize(); i++) {
			Object e = m.getElementAt(i);
			if (e instanceof LightSource) {
				LightSource l = (LightSource) e;
				if (l.visible.getValue() != Visibility.HIDDEN) {
					dark.subtract(l.getBrightArea());
					dark.subtract(l.getShadowArea());
					shadow.add(l.getShadowArea());
					if (this.lowLight.getValue()) {
						dark.subtract(l.getLowLightArea());
						lowLight.add(l.getLowLightArea());
					}
				}
			}
		}
		// remove brightly lit area from the shadow mask
		for (int i = 0; i < m.getSize(); i++) {
			Object e = m.getElementAt(i);
			if (e instanceof LightSource) {
				LightSource l = (LightSource) e;
				if (l.visible.getValue() != Visibility.HIDDEN) {
					shadow.subtract(l.getBrightArea());
					if (this.lowLight.getValue()) {
						lowLight.subtract(l.getBrightArea());
						lowLight.subtract(l.getShadowArea());
					}
				}
			}
		}

		g.setColor(color.getValue());
		Composite c = g.getComposite();
		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha.getValue()));
		g.fill(dark);

		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha.getValue() * 0.5f));
		g.fill(shadow);

		if (this.lowLight.getValue()) {
			g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha.getValue() * 0.75f));
			g.fill(lowLight);
		}

		g.setComposite(c);
	}

	@Override
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

	@Override
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
