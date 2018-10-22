package digital_table.elements;

import java.awt.AlphaComposite;
import java.awt.Composite;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

import digital_table.server.MapCanvas.Order;

// TODO fix layout when rotated
// TODO fix hardcoded sizes, change font-size property to something useful (small/med/large enum, for example)

public class PointOfInterest extends MapElement {
	private static final long serialVersionUID = 1L;

	public final static String PROPERTY_X = "x";	// double
	public final static String PROPERTY_Y = "y";	// double
	public static final String PROPERTY_ID = "id";	// String
	public final static String PROPERTY_TEXT = "text";	// String

	// position in grid coordinate-space
	Property<Double> x = new Property<Double>(PROPERTY_X, 0d, Double.class);
	Property<Double> y = new Property<Double>(PROPERTY_Y, 0d, Double.class);
	Property<String> text = new Property<String>(PROPERTY_TEXT, "", String.class);
	Property<String> id = new Property<String>(PROPERTY_ID, "", String.class);

	@Override
	public Order getDefaultOrder() {
		return Order.ABOVEGRID;
	}

	@Override
	public void paint(Graphics2D g) {
		if (getVisibility() == Visibility.HIDDEN || canvas == null) return;

		if (!(parent instanceof POIGroup)) return;
		if (parent.getVisibility() == Visibility.HIDDEN) return;
		POIGroup group = (POIGroup)parent;

		Point2D o = canvas.convertGridCoordsToDisplay(canvas.getElementOrigin(this));
		g.translate(o.getX(), o.getY());

		Font f = g.getFont();
		float newSize;
		if (group.rotations.getValue() % 2 == 0) {
			//newSize = canvas.getDisplayDimension(0, fontSize.getValue()).height;
			newSize = 15;
		} else {
			//newSize = canvas.getDisplayDimension(fontSize.getValue(), 0).width;
			newSize = 15;
		}
		g.setFont(f.deriveFont(Font.BOLD, newSize));

		// get the maximum width of each line
		FontMetrics metrics = g.getFontMetrics();
		int width = metrics.stringWidth(id.getValue());

		if (width > 0) {
			Composite c = g.getComposite();

			Point p = canvas.convertGridCoordsToDisplay(new Point2D.Double(x.getValue(), y.getValue()));
			p.x += 7;
			p.y -= 10;

			int w = width + 10;
			int h = metrics.getHeight();

			AffineTransform t = AffineTransform.getQuadrantRotateInstance(group.rotations.getValue());
			Point size = new Point(w, h);
			t.transform(size, size);	// transform to get new dimensions
			int newWidth = Math.abs(size.x);
			int newHeight = Math.abs(size.y);
			t = g.getTransform();
			g.rotate(Math.toRadians(group.rotations.getValue() * 90), p.x + newWidth / 2, p.y + newHeight / 2);
			g.translate((newWidth - w) / 2, (newHeight - h) / 2);

			// paint background
			if (!group.solidBackground.getValue()) {
				g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
			}
			g.setColor(group.backgroundColor.getValue());
			g.fillRect(p.x, p.y, w, h);
			g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, group.alpha.getValue() * (getVisibility() == Visibility.FADED ? 0.5f : 1f)));

			// draw text
			int y = p.y + metrics.getAscent();
			g.setColor(group.color.getValue());
			g.drawString(id.getValue(), p.x + 5, y);

			g.setTransform(t);
			g.setComposite(c);
			g.setFont(f);
		}

		Point p = canvas.convertGridCoordsToDisplay(new Point2D.Double(x.getValue(), y.getValue()));
		// paint background
		Composite c = g.getComposite();
		if (!group.solidBackground.getValue()) {
			g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
		}
		g.setColor(group.backgroundColor.getValue());
		g.fillOval(p.x - 7, p.y - 7, 15, 15);
		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, group.alpha.getValue() * (getVisibility() == Visibility.FADED ? 0.5f : 1f)));
		g.setColor(group.color.getValue());
		g.fillOval(p.x - 5, p.y - 5, 11, 11);
		g.setComposite(c);

		g.translate(-o.getX(), -o.getY());
	}

	@Override
	public String toString() {
		String t = id.getValue();
		if (t.length() > 0 && text.getValue().length() > 0)
			t += ": " + text.getValue();
		else if (text.getValue().length() > 0)
			t = text.getValue();
		else
			t = Integer.toString(getID());
		return "Point of Interest (" + t + ")";
	}
}
