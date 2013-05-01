package digital_table.elements;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Area;
import java.awt.geom.Point2D;

import digital_table.server.MapCanvas.Order;

public class LightSource extends MapElement {
	private static final long serialVersionUID = 1L;

	public final static String PROPERTY_RADIUS = "radius";	// int
	public final static String PROPERTY_X = "x";	// int
	public final static String PROPERTY_Y = "y";	// int
	public final static String PROPERTY_LABEL = "label";	// String
	public final static String PROPERTY_TYPE = "type";	// Type

	public enum Type {
		BRIGHT("Bright Light") {

			@Override
			int getBrightRadius(int r) {
				return r;
			}

			@Override
			int getShadowRadius(int r) {
				return r * 2;
			}

		},
		SHADOW("Shadowy Light") {
			@Override
			int getBrightRadius(int r) {
				return 0;
			}

			@Override
			int getShadowRadius(int r) {
				return r;
			}
		},
		DARKVISION("Darkvision") {
			@Override
			int getBrightRadius(int r) {
				return r;
			}

			@Override
			int getShadowRadius(int r) {
				return 0;
			}
		};

		abstract int getBrightRadius(int r);

		abstract int getShadowRadius(int r);

		@Override
		public String toString() {
			return description;
		}

		private Type(String d) {
			description = d;
		}

		private final String description;
	};

	Property<Integer> x, y;
	Property<Integer> radius;
	Property<String> label = new Property<String>(PROPERTY_LABEL, false, "", String.class);
	Property<Type> type = new Property<Type>(PROPERTY_TYPE, Type.BRIGHT, Type.class);

	public LightSource(int radius, int x, int y) {
		this.x = new Property<Integer>(PROPERTY_X, true, x, Integer.class);
		this.y = new Property<Integer>(PROPERTY_Y, true, y, Integer.class);
		this.radius = new Property<Integer>(PROPERTY_RADIUS, true, radius, Integer.class);
	}

	@Override
	public Order getDefaultOrder() {
		return Order.BELOWGRID;
	}

	@Override
	public void paint(Graphics2D g, Point2D offset) {
	}

	protected Area getBrightArea() {
		Area area = new Area();
		int r = type.getValue().getBrightRadius(radius.getValue());
		addQuadrant(area, 1, 1, r);
		addQuadrant(area, 1, -1, r);
		addQuadrant(area, -1, 1, r);
		addQuadrant(area, -1, -1, r);
		return area;
	}

	protected Area getShadowArea() {
		Area area = new Area();
		int r = type.getValue().getShadowRadius(radius.getValue());
		addQuadrant(area, 1, 1, r);
		addQuadrant(area, 1, -1, r);
		addQuadrant(area, -1, 1, r);
		addQuadrant(area, -1, -1, r);
		return area;
	}

	protected Area getLowLightArea() {
		Area area = new Area();
		int r = 2 * type.getValue().getShadowRadius(radius.getValue());
		addQuadrant(area, 1, 1, r);
		addQuadrant(area, 1, -1, r);
		addQuadrant(area, -1, 1, r);
		addQuadrant(area, -1, -1, r);
		return area;
	}

	private Point getAbsLocation() {
		Point2D p = new Point2D.Double(x.getValue(), y.getValue());
		Group parent = getParent();
		while (parent != null) {
			p = parent.translate(p);
			parent = parent.getParent();
		}
		return new Point((int) p.getX(), (int) p.getY());
	}

	private void addQuadrant(Area area, int xdir, int ydir, int r) {
		Point origin = getAbsLocation();

		for (int i = 0; i < r; i++) {
			for (int j = 0; j < r; j++) {
				if (isAffected(r, i, j)) {
					int gridx = xdir * i + origin.x;
					int gridy = ydir * j + origin.y;
					area.add(new Area(getRectangle(gridx, gridy, gridx + xdir, gridy + ydir)));
				}
			}
		}
	}

	private boolean isAffected(int radius, int i, int j) {
		// measure distance from (0, 0) to each corner of this cell
		// if all four corners are within the radius then the cell is affected
		// note: only need to test the bottom right corner - if that is in the radius then the other corners must be
		int dist = i + 1 + j + 1 - (Math.min(i + 1, j + 1) - 1) / 2;	// the subtracted term is half the number of diagonals
		if (dist <= radius + 1) return true;
		return false;
	}

	// if the rectangle has any negative dimensions it will be modified to make those dimensions positive
	private Rectangle getRectangle(int x1, int y1, int x2, int y2) {
		Point p1 = canvas.getDisplayCoordinates(x1, y1, null);
		Point p2 = canvas.getDisplayCoordinates(x2, y2, null);
		Rectangle rect = new Rectangle(p1.x, p1.y, p2.x - p1.x, p2.y - p1.y);
		if (rect.width < 0) {
			rect.width = -rect.width;
			rect.x -= rect.width;
		}
		if (rect.height < 0) {
			rect.height = -rect.height;
			rect.y -= rect.height;
		}
		return rect;
	}

	@Override
	public String toString() {
		if (label == null || label.getValue().length() == 0) return "Light Source (" + getID() + ")";
		return "Light Source (" + label + ")";
	}
}
