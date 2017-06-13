package digital_table.elements;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Area;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
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
	public void paint(Graphics2D g) {
	}

	Area getBrightArea(DarknessMask.WallLayout walls) {
		int r = type.getValue().getBrightRadius(radius.getValue());
		return getArea(r, walls);
	}

	Area getShadowArea(DarknessMask.WallLayout walls) {
		int r = type.getValue().getShadowRadius(radius.getValue());
		return getArea(r, walls);
	}

	Area getLowLightArea(DarknessMask.WallLayout walls) {
		int r = 2 * type.getValue().getShadowRadius(radius.getValue());
		return getArea(r, walls);
	}

	private Area getArea(int radius, DarknessMask.WallLayout walls) {
		Area area = new Area();
		if (radius == 0) return area;
		addQuadrant(area, 1, 1, radius);
		addQuadrant(area, 1, -1, radius);
		addQuadrant(area, -1, 1, radius);
		addQuadrant(area, -1, -1, radius);

		Double r = 2 * canvas.convertGridCoordsToDisplay(new Point2D.Double(radius, 0d)).getX();			// the 2* helps to ensure that the shadow extends far enough to cover the lit area. see below

		if (walls != null && walls.walls != null) {
			// create shadow mask and remove from area
			Point origin = getAbsLocation();
			origin = canvas.convertCanvasCoordsToDisplay(origin);
			for (Line2D.Double l : walls.walls) {
				// if the wall intersects the area then project lines from the centre through the end points to create a quadrilateral to remove from the area
				// TODO optimise by checking if the line intersects the area
				Point p1 = canvas.convertCanvasCoordsToDisplay(l.getP1());
				Point p2 = canvas.convertCanvasCoordsToDisplay(l.getP2());
				Point2D.Double mp = new Point2D.Double((p2.x + p1.x) / 2, (p2.y + p1.y) / 2);
				if (p1.equals(origin) || p2.equals(origin) || mp.equals(origin)) continue;		// no shadow if the light is on one of the endpoints or the midpoint
				Path2D.Double shadow = new Path2D.Double(Path2D.WIND_EVEN_ODD);
				shadow.moveTo(p1.x, p1.y);
				Point2D.Double p = extendLine(p1.x, p1.y, origin, r);
				shadow.lineTo(p.x, p.y);
				p = extendLine(mp.x, mp.y, origin, r);	// extend a light from the midpoint - this helps ensure the shadow covers the lit area along with using double the actual radius.
				// the "correct" but lower performance alternative would be to use a curved path around the edge of the lit area
				shadow.lineTo(p.x, p.y);
				p = extendLine(p2.x, p2.y, origin, r);
				shadow.lineTo(p.x, p.y);
				shadow.lineTo(p2.x, p2.y);
				shadow.closePath();
				try {
					area.subtract(new Area(shadow));
//					area.add(new Area(shadow));
				} catch (InternalError e) {
					// shouldn't occur
					System.err.println(e);
					System.err.println("Light at " + origin);
					System.err.println("Wall: " + p1 + " to " + p2);
				}
			}
		}
		return area;
	}

	private Point2D.Double extendLine(double x1, double y1, Point2D origin, double radius) {
		double x = x1 - origin.getX();
		double y = y1 - origin.getY();
		double x2, y2;
		if (Math.abs(x) < Math.abs(y)) {
			y2 = (y < 0) ? -radius : radius;
			x2 = y2 * x / y;
		} else {
			x2 = (x < 0) ? -radius : radius;
			y2 = x2 * y / x;
		}
		return new Point2D.Double(x2 + x1, y2 + y1);
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
		Point p1 = canvas.convertCanvasCoordsToDisplay(x1, y1, null);
		Point p2 = canvas.convertCanvasCoordsToDisplay(x2, y2, null);
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
