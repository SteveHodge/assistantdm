package digital_table.elements;

import java.awt.AlphaComposite;
import java.awt.Composite;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;

import digital_table.server.MapCanvas.Order;

// TODO fix hardcoded sizes, change font-size property to something useful (small/med/large enum, for example)

public class PointOfInterest extends MapElement {
	private static final long serialVersionUID = 1L;

	public final static String PROPERTY_REL_X = "rel-x";	// double - the x-coordinate as a proportion of the base image's native size and rotation
	public final static String PROPERTY_REL_Y = "rel-y";	// double
	public final static String PROPERTY_LOCATION = "location";	// Point2D - (write only) coordinate in grid units relative to parent. if this is set then rel-x and rel-y will be calculated from this using the base image's current size and rotation
	public static final String PROPERTY_ID = "id";	// String
	public final static String PROPERTY_TEXT = "text";	// String

	// position as fraction of base image in native rotation
	Property<Double> relX = new Property<Double>(PROPERTY_REL_X, 0d, Double.class);
	Property<Double> relY = new Property<Double>(PROPERTY_REL_Y, 0d, Double.class);
	Property<String> text = new Property<String>(PROPERTY_TEXT, "", String.class);
	Property<String> id = new Property<String>(PROPERTY_ID, "", String.class);

	@Override
	public Order getDefaultOrder() {
		return Order.ABOVEGRID;
	}

	AffineTransform getTransform(MapImage map) {
		int rotations = map.rotations.getValue();
		AffineTransform transform;
		if (map.mirrored.getValue()) {
			transform = AffineTransform.getScaleInstance(-1, 1);
			transform.quadrantRotate(-rotations);
			rotations = 3 - rotations;
		} else {
			transform = AffineTransform.getQuadrantRotateInstance(rotations);
		}

		// translation to adjust origin for rotation
		if (rotations % 4 == 1)
			transform.translate(0, -1);
		else if (rotations % 4 == 2)
			transform.translate(-1, -1);
		else if (rotations % 4 == 3)
			transform.translate(-1, 0);

		return transform;
	}

	@Override
	public void paint(Graphics2D g) {
		if (getVisibility() == Visibility.HIDDEN || canvas == null) return;

		if (!(parent instanceof POIGroup)) return;
		if (parent.getVisibility() == Visibility.HIDDEN) return;
		POIGroup group = (POIGroup)parent;
		if (!(group.parent instanceof MapImage)) return;
		MapImage map = (MapImage) group.parent;

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
		int labelWidth = metrics.stringWidth(id.getValue());

		Point2D pos = getTransform(map).transform(new Point2D.Double(relX.getValue(), relY.getValue()), null);
		Point position = canvas.convertGridCoordsToDisplay(new Point2D.Double(pos.getX() * map.width.getValue(), pos.getY() * map.height.getValue()));
		//Point position = canvas.convertGridCoordsToDisplay(new Point2D.Double(relX.getValue() * map.width.getValue(), relY.getValue() * map.height.getValue()));

		if (labelWidth > 0) {
			Composite c = g.getComposite();

			Point p = new Point((int) position.getX() + 7, (int) position.getY() - 10);

			int w = labelWidth + 10;
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

		// paint background
		Composite c = g.getComposite();
		if (!group.solidBackground.getValue()) {
			g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
		}
		g.setColor(group.backgroundColor.getValue());
		g.fillOval((int) position.getX() - 7, (int) position.getY() - 7, 15, 15);
		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, group.alpha.getValue() * (getVisibility() == Visibility.FADED ? 0.5f : 1f)));
		g.setColor(group.color.getValue());
		g.fillOval((int) position.getX() - 5, (int) position.getY() - 5, 11, 11);
		g.setComposite(c);

		g.translate(-o.getX(), -o.getY());
	}

	@Override
	public void setProperty(String property, Object value) {
		if (property.equals(PROPERTY_LOCATION)) {
			if (!(parent instanceof POIGroup)) return;
			POIGroup group = (POIGroup) parent;
			if (!(group.parent instanceof MapImage)) return;
			MapImage map = (MapImage) group.parent;
			Point2D p = (Point2D) value;
			try {
				Point2D rel = new Point2D.Double(p.getX() / map.width.getValue(), p.getY() / map.height.getValue());
				getTransform(map).inverseTransform(rel, rel);	// reverse any rotation or mirroring of the map
				relX.setValue(rel.getX());
				relY.setValue(rel.getY());
			} catch (NoninvertibleTransformException e) {
			}
//		} else if (property.equals(PROPERTY_REL_X)) {
//			if (!(parent instanceof POIGroup)) return;
//			POIGroup group = (POIGroup) parent;
//			if (!(group.parent instanceof MapImage)) return;
//			MapImage map = (MapImage) group.parent;
//			double d = (Double) value;
//			relX.setValue(d / (map.width.getValue() * 2));
//		} else if (property.equals(PROPERTY_REL_Y)) {
//			if (!(parent instanceof POIGroup)) return;
//			POIGroup group = (POIGroup) parent;
//			if (!(group.parent instanceof MapImage)) return;
//			MapImage map = (MapImage) group.parent;
//			double d = (Double) value;
//			relY.setValue(d / (map.height.getValue() * 2));
		} else {
			super.setProperty(property, value);
		}
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
