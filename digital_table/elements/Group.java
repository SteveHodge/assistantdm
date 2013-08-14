package digital_table.elements;

import java.awt.Graphics2D;
import java.awt.geom.Point2D;

public class Group extends MapElement {
	private static final long serialVersionUID = 1L;

	public final static String PROPERTY_LABEL = "label";	// String
	public final static String PROPERTY_LOCATION = "location";

	protected Property<String> label = new Property<String>(PROPERTY_LABEL, "", String.class);
	protected Property<Point2D> location = new Property<Point2D>(PROPERTY_LOCATION, new Point2D.Double(), Point2D.class);

	protected Group(int id) {
		super(id);
	}

	public Group() {
		super();
	}

	@Override
	public void paint(Graphics2D g, Point2D offset) {
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

	@Override
	public String toString() {
		if (label == null || label.getValue().length() == 0) return "Group (" + getID() + ")";
		return "Group (" + label + ")";
	}
}
