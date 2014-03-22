package digital_table.elements;

import java.awt.Graphics2D;
import java.awt.geom.Point2D;

public class Group extends MapElement {
	private static final long serialVersionUID = 1L;

	public final static String PROPERTY_LABEL = "label";	// String
	public final static String PROPERTY_LOCATION = "location";
	public final static String PROPERTY_X = "x";	// double
	public final static String PROPERTY_Y = "y";	// double

	protected Property<String> label = new Property<String>(PROPERTY_LABEL, "", String.class);
	protected Property<Point2D> location = new Property<Point2D>(PROPERTY_LOCATION, new Point2D.Double(), Point2D.class);

	protected Group(int id) {
		super(id);
	}

	public Group() {
		super();
	}

	@Override
	public void paint(Graphics2D g) {
	}

	public void addChild(MapElement e) {
		// TODO should adjust the child's location if any
		e.parent = this;
	}

	public void removeChild(MapElement e) {
		// TODO should adjust the child's location if any
		if (e.parent == this) e.parent = null;
	}

	public Point2D translate(Point2D p) {
		Point2D loc = location.getValue();
		return new Point2D.Double(loc.getX() + p.getX(), loc.getY() + p.getY());
	}

	protected double getX() {
		return location.getValue().getX();
	}

	protected double getY() {
		return location.getValue().getY();
	}

	protected void setX(double x) {
		Point2D p = new Point2D.Double(x, getY());
		location.setValue(p);
	}

	protected void setY(double y) {
		Point2D p = new Point2D.Double(getX(), y);
		location.setValue(p);
	}

	@Override
	public String toString() {
		if (label == null || label.getValue().length() == 0) return "Group (" + getID() + ")";
		return "Group (" + label + ")";
	}

	@Override
	public Object getProperty(String property) {
		if (property.equals(PROPERTY_X)) {
			return getX();
		} else if (property.equals(PROPERTY_Y)) {
			return getY();
		} else {
			return super.getProperty(property);
		}
	}

	@Override
	public void setProperty(String property, Object value) {
		if (property.equals(PROPERTY_X)) {
			setX((Double) value);
		} else if (property.equals(PROPERTY_Y)) {
			setY((Double) value);
		} else {
			super.setProperty(property, value);
		}
	}
}
