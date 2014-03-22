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
import java.awt.geom.Point2D;

import digital_table.server.MapCanvas.Order;

public class PersonalEmanation extends MapElement {
	private static final long serialVersionUID = 1L;

	public final static String PROPERTY_RADIUS = "radius";	// int
	public final static String PROPERTY_X = "x";	// int
	public final static String PROPERTY_Y = "y";	// int
	public final static String PROPERTY_COLOR = "color";	// Color
	public final static String PROPERTY_ALPHA = "alpha";	// float
	public final static String PROPERTY_LABEL = "label";	// String
	public final static String PROPERTY_SPACE = "space";	// int

	Property<Integer> x, y;
	Property<Integer> radius;
	Property<Integer> space = new Property<Integer>(PROPERTY_SPACE, 1, Integer.class);
	Property<Float> alpha = new Property<Float>(PROPERTY_ALPHA, 0.5f, Float.class);
	Property<Color> color = new Property<Color>(PROPERTY_COLOR, Color.RED, Color.class);
	Property<String> label = new Property<String>(PROPERTY_LABEL, false, "", String.class);

	transient boolean affected[][];

	public PersonalEmanation(int radius, int x, int y) {
		this.x = new Property<Integer>(PROPERTY_X, true, x, Integer.class);
		this.y = new Property<Integer>(PROPERTY_Y, true, y, Integer.class);
		this.radius = new Property<Integer>(PROPERTY_RADIUS, true, radius, Integer.class);
	}

	@Override
	public Order getDefaultOrder() {
		return Order.TOP;
	}

	@Override
	public void paint(Graphics2D g) {
		if (canvas == null || getVisibility() == Visibility.HIDDEN) return;

		Point2D o = canvas.convertGridCoordsToDisplay(canvas.getElementOrigin(this));
		g.translate(o.getX(), o.getY());

		Stroke oldStroke = g.getStroke();
		g.setColor(color.getValue());
		Composite c = g.getComposite();
		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha.getValue() * (getVisibility() == Visibility.FADED ? 0.5f : 1f)));

		// build the shape
		Area area = new Area();
		int r = radius.getValue();
		int x = this.x.getValue();
		int y = this.y.getValue();
		int space = this.space.getValue();
		if (parent != null && parent instanceof Token) {
			space = ((Token) parent).getSpace();
		}

		area.add(getRectangularArea(x - r, y, x + r + space, y + space));
		area.add(getRectangularArea(x, y - r, x + space, y + space + r));
		area.add(getQuadrant(x, y, -1, -1, r));
		area.add(getQuadrant(x + space, y, 1, -1, r));
		area.add(getQuadrant(x, y + space, -1, 1, r));
		area.add(getQuadrant(x + space, y + space, 1, 1, r));

		g.fill(area);
		g.setColor(darken(color.getValue()));
		g.setStroke(getThickStroke());
		g.draw(area);
		g.setStroke(oldStroke);
		g.setComposite(c);
		g.translate(-o.getX(), -o.getY());
	}

	private Area getQuadrant(int x, int y, int xdir, int ydir, int radius) {
		Area area = new Area();

		for (int i = 0; i < radius; i++) {
			for (int j = 0; j < radius; j++) {
				if (isAffected(i, j, radius)) {
					int gridx = xdir * i + x;
					int gridy = ydir * j + y;
					area.add(getRectangularArea(gridx, gridy, gridx + xdir, gridy + ydir));
				}
			}
		}
		return area;
	}

	// TODO better not to cache this - then radius need not be an subclass
	private boolean isAffected(int i, int j, int r) {
		// measure distance from (0, 0) to each corner of this cell
		// if all four corners are within the radius then the cell is affected
		// note: only need to test the bottom right corner - if that is in the radius then the other corners must be
		int dist = i + 1 + j + 1 - (Math.min(i + 1, j + 1) - 1) / 2;	// the subtracted term is half the number of diagonals
		if (dist <= r + 1) return true;
		return false;
	}

	// if the rectangle has any negative dimensions it will be modified to make those dimensions positive
	private Area getRectangularArea(int x1, int y1, int x2, int y2) {
		Point p1 = canvas.convertGridCoordsToDisplay(x1, y1, null);
		Point p2 = canvas.convertGridCoordsToDisplay(x2, y2, null);
		Rectangle rect = new Rectangle(p1.x, p1.y, p2.x - p1.x, p2.y - p1.y);
		if (rect.width < 0) {
			rect.width = -rect.width;
			rect.x -= rect.width;
		}
		if (rect.height < 0) {
			rect.height = -rect.height;
			rect.y -= rect.height;
		}
		return new Area(rect);
	}

	private Stroke getThickStroke() {
		if (canvas.getColumnWidth() < 40) return new BasicStroke(3);
		return new BasicStroke(5);
	}

	@Override
	public String toString() {
		if (label == null || label.getValue().length() == 0) return "Template (" + getID() + ")";
		return "Template (" + label + ")";
	}
}
