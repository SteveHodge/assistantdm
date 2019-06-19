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
import digital_table.server.MeasurementLog;

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

		Composite c = g.getComposite();
		if (labelWidth > 0) {
			AffineTransform t = g.getTransform();
			g.rotate(Math.toRadians(group.rotations.getValue() * 90), position.x + 1, position.y + 1);
			g.translate(8, -9);

			// paint text background
			if (!group.solidBackground.getValue())
				g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
			g.setColor(group.backgroundColor.getValue());
			g.fillRect(position.x, position.y, labelWidth + 10, metrics.getHeight());

			// draw text
			g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, group.alpha.getValue() * (getVisibility() == Visibility.FADED ? 0.5f : 1f)));
			g.setColor(group.color.getValue());
			g.drawString(id.getValue(), position.x + 5, position.y + metrics.getAscent());

			g.setTransform(t);
		}

		// paint dot background
		if (!group.solidBackground.getValue())
			g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
		g.setColor(group.backgroundColor.getValue());
		g.fillOval(position.x - 7, position.y - 7, 15, 15);

		// paint dot
		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, group.alpha.getValue() * (getVisibility() == Visibility.FADED ? 0.5f : 1f)));
		g.setColor(group.color.getValue());
		g.fillOval(position.x - 5, position.y - 5, 11, 11);

		// restore previous settings
		g.setComposite(c);
		g.setFont(f);
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

	@Override
	public String getIDString() {
		return "PointOfInterest" + (text.getValue().length() > 0 ? " (" + text.getValue() + ")" : "");
	}

	public MeasurementLog getPaintTiming() {
		return null;
	}
}
