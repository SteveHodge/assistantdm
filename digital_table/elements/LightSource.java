package digital_table.elements;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.geom.Area;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.List;
import java.util.function.BiConsumer;

import digital_table.server.MapCanvas.Order;

public class LightSource extends MapElement {
	private static final long serialVersionUID = 1L;

	public final static String PROPERTY_RADIUS = "radius";	// int
	public final static String PROPERTY_X = "x";	// int
	public final static String PROPERTY_Y = "y";	// int
	public final static String PROPERTY_LABEL = "label";	// String
	public final static String PROPERTY_TYPE = "type";	// Type
	public final static String PROPERTY_ALL_CORNERS = "all_corners";	// boolean
	public final static String PROPERTY_COLOR = "color";	// Color
	public final static String PROPERTY_ALWAYS_SHOW_ORIGIN = "always_show";	// boolean. if true then always draws an indicator of the origin, if false then this occurs only if the element is selected or dragging
	public final static String PROPERTY_PAINT_LIGHT = "paint_light";	// boolean. if true then will fill the area lit (out to the shadowy radius)

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
	Property<Boolean> allCorners = new Property<Boolean>(PROPERTY_ALL_CORNERS, false, Boolean.class);
	Property<Color> color = new Property<Color>(PROPERTY_COLOR, Color.YELLOW, Color.class);
	Property<Boolean> showOrigin = new Property<Boolean>(PROPERTY_ALWAYS_SHOW_ORIGIN, false, Boolean.class);
	Property<Boolean> paintLight = new Property<Boolean>(PROPERTY_PAINT_LIGHT, false, Boolean.class);

	public LightSource(int radius, int x, int y) {
		this.x = new Property<Integer>(PROPERTY_X, true, x, Integer.class);
		this.y = new Property<Integer>(PROPERTY_Y, true, y, Integer.class);
		this.radius = new Property<Integer>(PROPERTY_RADIUS, true, radius, Integer.class);
	}

	@Override
	public Order getDefaultOrder() {
		return Order.ABOVEGRID;
	}

	@Override
	public void paint(Graphics2D g) {
		g.setColor(color.getValue());

		if (isDragging() || selected || (showOrigin.getValue() && getVisibility() != Visibility.HIDDEN)) {
			Point2D o = canvas.convertGridCoordsToDisplay(canvas.getElementOrigin(this));
			g.translate(o.getX(), o.getY());
			Stroke stroke = g.getStroke();
			g.setStroke(new BasicStroke(3.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
			if (allCorners.getValue() && parent != null && parent instanceof Token) {
				// if radiating from a token, ignore this element's position offset, but draw a light in each corner
				int tokenSize = ((Token) parent).getSpace();
				Point p1 = canvas.convertGridCoordsToDisplay(tokenSize, tokenSize, null);
				g.drawLine(-10, 0, 0, 0);
				g.drawLine(0, -10, 0, 0);
				g.drawLine(-5, -5, 0, 0);
				g.drawLine(5, -5, -5, 5);

				g.drawLine(p1.x, 0, p1.x + 10, 0);
				g.drawLine(p1.x, -10, p1.x, 0);
				g.drawLine(p1.x - 5, -5, p1.x + 5, 5);
				g.drawLine(p1.x + 5, -5, p1.x, 0);

				g.drawLine(p1.x, p1.y, p1.x + 10, p1.y);
				g.drawLine(p1.x, p1.y, p1.x, p1.y + 10);
				g.drawLine(p1.x, p1.y, p1.x + 5, p1.y + 5);
				g.drawLine(p1.x + 5, p1.y - 5, p1.x - 5, p1.y + 5);

				g.drawLine(-10, p1.y, 0, p1.y);
				g.drawLine(0, p1.y, 0, p1.y + 10);
				g.drawLine(-5, p1.y - 5, +5, p1.y + 5);
				g.drawLine(0, p1.y, -5, p1.y + 5);

			} else {
				Point p1 = canvas.convertGridCoordsToDisplay(x.getValue(), y.getValue(), null);
				g.drawLine(p1.x - 10, p1.y, p1.x + 10, p1.y);
				g.drawLine(p1.x, p1.y - 10, p1.x, p1.y + 10);
				g.drawLine(p1.x - 5, p1.y - 5, p1.x + 5, p1.y + 5);
				g.drawLine(p1.x + 5, p1.y - 5, p1.x - 5, p1.y + 5);
			}
			g.setStroke(stroke);
			g.translate(-o.getX(), -o.getY());
		}

		if (paintLight.getValue() && getVisibility() != Visibility.HIDDEN) {
			Composite c = g.getComposite();
			g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
			int brightRadius = type.getValue().getBrightRadius(radius.getValue());
			int shadowRadius = type.getValue().getShadowRadius(radius.getValue());
			int radius = brightRadius > shadowRadius ? brightRadius : shadowRadius;
			Area area = new Area();
			int tokenSize = 0;
			Point2D o = canvas.getElementOrigin(this);	// grid coordinates - converted to display coords by getRectangle
			Point oo = new Point((int) o.getX() + x.getValue(), (int) o.getY() + y.getValue());
			if (allCorners.getValue() && parent != null && parent instanceof Token) {
				oo = new Point((int) o.getX(), (int) o.getY());			// if radiate from all corners, and there is a token then ignore the x, y values for this element
				tokenSize = ((Token) parent).getSpace();
				area.add(new Area(getRectangle(oo.x, oo.y, oo.x + tokenSize, oo.y + tokenSize + radius)));
				area.add(new Area(getRectangle(oo.x, oo.y, oo.x + tokenSize + radius, oo.y + tokenSize)));
				area.add(new Area(getRectangle(oo.x, oo.y, oo.x + tokenSize, oo.y + -radius)));
				area.add(new Area(getRectangle(oo.x, oo.y, oo.x - radius, oo.y + tokenSize)));
			}
			addQuadrant(area, oo.x + tokenSize, oo.y + tokenSize, 1, 1, radius);
			addQuadrant(area, oo.x + tokenSize, oo.y, 1, -1, radius);
			addQuadrant(area, oo.x, oo.y + tokenSize, -1, 1, radius);
			addQuadrant(area, oo.x, oo.y, -1, -1, radius);
			g.fill(area);
			g.setComposite(c);
		}
	}

	Area getBrightArea(List<Line2D.Double> walls) {
		int r = type.getValue().getBrightRadius(radius.getValue());
		return getArea(r, walls);
	}

	Area getShadowArea(List<Line2D.Double> walls) {
		int r = type.getValue().getShadowRadius(radius.getValue());
		return getArea(r, walls);
	}

	Area getLowLightArea(List<Line2D.Double> walls) {
		int r = 2 * type.getValue().getShadowRadius(radius.getValue());
		return getArea(r, walls);
	}

	// shadow mask cache:
	List<Line2D.Double> cachedWalls = null;
	Point cachedLocation = null;
	double cachedRadius = 0;
	Area cachedShadow = null;
	int cachedTokenSize = 0;

	private Area getArea(int radius, List<Line2D.Double> walls) {
		Area area = new Area();
		if (radius == 0) return area;
		Point o = getAbsLocation();
		int tokenSize = 0;
		if (allCorners.getValue() && parent != null && parent instanceof Token) {
			tokenSize = ((Token) parent).getSpace();
			area.add(new Area(getRectangle(o.x, o.y, o.x + tokenSize, o.y + tokenSize + radius)));
			area.add(new Area(getRectangle(o.x, o.y, o.x + tokenSize + radius, o.y + tokenSize)));
			area.add(new Area(getRectangle(o.x, o.y, o.x + tokenSize, o.y + -radius)));
			area.add(new Area(getRectangle(o.x, o.y, o.x - radius, o.y + tokenSize)));
		}
		addQuadrant(area, o.x + tokenSize, o.y + tokenSize, 1, 1, radius);
		addQuadrant(area, o.x + tokenSize, o.y, 1, -1, radius);
		addQuadrant(area, o.x, o.y + tokenSize, -1, 1, radius);
		addQuadrant(area, o.x, o.y, -1, -1, radius);

		// FIXME type/lowlight flag probably don't change often. we don't need to make this so large, we can predict the likely max based on type and just recalc if radius > r.
		Double r = canvas.getDisplayDimension(4 * this.radius.getValue() + 1, 0).getWidth();			// the factor of 4 is the maximum shadowy radius with low light vision. the +1 helps make sure no bits that should be shadowed are seen
		Double s = canvas.getDisplayDimension(tokenSize, 0).getWidth();

		if (walls != null) {
			// create shadow mask and remove from area
			Point origin = canvas.convertCanvasCoordsToDisplay(getAbsLocation());

			if (!origin.equals(cachedLocation) || walls != cachedWalls || cachedRadius != r || cachedTokenSize != tokenSize) {	// FIXME radius check should be less than I think
//				System.out.println("Cache out of date, updating shadow: location? " + (!origin.equals(cachedLocation)) + ", walls? " + (walls != cachedWalls) + ", radius? " + (cachedRadius != r)
//						+ ", tokenSize? " + (cachedTokenSize != tokenSize));
//				long startTime = System.nanoTime();
				Rectangle2D shadowBounds = new Rectangle2D.Double(origin.x - r, origin.y - r, r * 2 + s, r * 2 + s);	// performance of Area.add sucks, so instead we subtract the shadows from a rectangle and then intersect the result with the lit area
				Area shadowArea = null;
				double[] offsets = new double[tokenSize == 0 ? 1 : 2];
				if (offsets.length > 1) offsets[1] = s;
				for (double x : offsets) {
					for (double y : offsets) {
//				double x = 0;
//				double y = s;
						Point localOrigin = new Point((int) (origin.x + x), (int) (origin.y + y));
						Area shad = walls.parallelStream().collect(
								() -> {
									return new Area(shadowBounds);
								},
								new ShadowProjector(localOrigin, shadowBounds, r),
								new BiConsumer<Area, Area>() {
									@Override
									public void accept(Area shadowArea1, Area shadowArea2) {
										//System.out.println("combining shadows");
										shadowArea1.intersect(shadowArea2);
									}
								});
						if (shadowArea == null) {
							shadowArea = shad;
						} else {
							shadowArea.add(shad);
						}
					}
				}
				cachedShadow = shadowArea;
				cachedWalls = walls;
				cachedRadius = r;
				cachedLocation = origin;
				cachedTokenSize = tokenSize;
//				double millis = (System.nanoTime() - startTime) / 1000000d;
				//logger.info("Painting complete for " + this + " in " + micros + "ms");
//				System.out.printf("Shadow calculation took %.3fms\n", millis);
			} else {
				//System.out.println("Using cached shadow");
			}
			area.intersect(cachedShadow);
		}
		return area;
	}

	private class ShadowProjector implements BiConsumer<Area, Line2D.Double>
	{
		private Point origin;
		private Rectangle2D shadowBounds;
		private double radius;

		ShadowProjector(Point o, Rectangle2D b, double r) {
			origin = o;
			shadowBounds = b;
			radius = r;
		}

		@Override
		public void accept(Area shadowArea, java.awt.geom.Line2D.Double l) {
			// if the wall intersects the area then project lines from the centre through the end points to create a shadow mask to remove from the area
			Point p1 = canvas.convertCanvasCoordsToDisplay(l.getP1());
			Point p2 = canvas.convertCanvasCoordsToDisplay(l.getP2());
			if (!shadowBounds.intersectsLine(p1.x, p1.y, p2.x, p2.y)) return;
			Point2D.Double p1e = extendLine(p1.x, p1.y, origin, radius);
			Point2D.Double p2e = extendLine(p2.x, p2.y, origin, radius);
			Point2D.Double a = extendLine(p1e.x + (p2e.x - p1e.x) / 2, p1e.y + (p2e.y - p1e.y) / 2, origin, radius);	// FIXME this still doesn't work in degenerate cases
//			System.out.println("origin = " + origin);
//			System.out.println("p1 = " + p1);
//			System.out.println("p2 = " + p2);
//			System.out.println("p1e = " + p1e);
//			System.out.println("p2e = " + p2e);
			if (p1.equals(origin) || p2.equals(origin)) return;		// no shadow if the light is on one of the endpoints
			Path2D.Double shadow = new Path2D.Double(Path2D.WIND_EVEN_ODD);
			shadow.moveTo(p1.x, p1.y);
			shadow.lineTo(p1e.x, p1e.y);
			shadow.lineTo(a.x, a.y);
			shadow.lineTo(p2e.x, p2e.y);
			shadow.lineTo(p2.x, p2.y);
			shadow.closePath();
			try {
				shadowArea.subtract(new Area(shadow));
			} catch (InternalError e) {
				// shouldn't occur
				System.err.println(e);
				System.err.println("Light at " + origin);
				System.err.println("Wall: " + p1 + " to " + p2);
			}
		}
	};

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

	Point lastLoc;
	private Point getAbsLocation() {
		if (isDragging()) return lastLoc;
		Point2D p;
		if (allCorners.getValue() && parent != null && parent instanceof Token) {
			// if we're radiating from all corners of a token, then lock the origin to the token's position (ignore this element's position offset)
			p = new Point2D.Double(0, 0);
		} else {
			p = new Point2D.Double(x.getValue(), y.getValue());
		}
		Group parent = getParent();
		while (parent != null) {
			p = parent.translate(p);
			parent = parent.getParent();
		}
		lastLoc = new Point((int) p.getX(), (int) p.getY());
		return lastLoc;
	}

	private void addQuadrant(Area area, int x, int y, int xdir, int ydir, int r) {
		for (int i = 0; i < r; i++) {
			for (int j = 0; j < r; j++) {
				if (isAffected(r, i, j)) {
					int gridx = xdir * i + x;
					int gridy = ydir * j + y;
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
