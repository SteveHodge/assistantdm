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
import java.util.ArrayList;
import java.util.List;

import digital_table.server.MapCanvas.Order;

// TODO if maximum will be less than the number of defined cubes then need to truncate the list of defined cubes

public class ShapeableTemplate extends MapElement {
	private static final long serialVersionUID = 1L;

	public final static String PROPERTY_COLOR = "color";	// Color
	public final static String PROPERTY_ALPHA = "alpha";	// float
	public final static String PROPERTY_LABEL = "label";	// String
	public final static String PROPERTY_MAXIMUM = "maximum";	// int - number of 10ft cubes (0 means unlimited)
	public final static String PROPERTY_PLACED = "placed";		// readonly int - number of 10ft cubes used
	public final static String PROPERTY_ADDCUBE = "add";	// Point - when this property is set the cube centred on the specified point will be added
	public final static String PROPERTY_REMOVECUBE = "remove";	// Point - when this property is set the cube centred on the specified point will be removed
	//public final static String PROPERTY_CONTIGUOUS = "contiguous";	// bool - all cubes after the first must be adjacent to at least one other cube
	//public final static String PROPERTY_5FOOT = "5foot";	// bool - shape using 5 foot cubes

	Property<Integer> maximum = new Property<Integer>(PROPERTY_MAXIMUM, 0, Integer.class);
	Property<Color> color = new Property<Color>(PROPERTY_COLOR, Color.RED, Color.class);
	Property<Float> alpha = new Property<Float>(PROPERTY_ALPHA, 1.0f, Float.class);
	Property<String> label = new Property<String>(PROPERTY_LABEL, false, "", String.class);

	List<Point> squares = new ArrayList<Point>();

	@Override
	public Order getDefaultOrder() {
		return Order.BELOWGRID;
	}

	@Override
	public void paint(Graphics2D g, Point2D offset) {
		if (canvas == null || !isVisible()) return;
		Point2D o = canvas.getDisplayCoordinates((int) offset.getX(), (int) offset.getY());
		g.translate(o.getX(), o.getY());

		Stroke oldStroke = g.getStroke();
		g.setColor(color.getValue());
		Composite c = g.getComposite();
		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha.getValue()));

		// build the shape
		Area area = new Area();
		for (Point p : squares) {
			Point tl = canvas.getDisplayCoordinates(p.x-1, p.y-1);
			Point br = canvas.getDisplayCoordinates(p.x+1, p.y+1);
			area.add(new Area(new Rectangle(tl.x, tl.y, br.x - tl.x, br.y - tl.y)));
		}

		g.fill(area);
		g.setColor(darken(color.getValue()));
		g.setStroke(getThickStroke());
		g.draw(area);

		g.setStroke(oldStroke);
		g.setComposite(c);
		g.translate(-o.getX(), -o.getY());
	}

	protected Stroke getThickStroke() {
		if (canvas.getColumnWidth() < 40) return new BasicStroke(3);
		return new BasicStroke(5);
	}

	@Override
	public String toString() {
		if (label == null || label.getValue().length() == 0) return "Shapeable ("+getID()+")";
		return "Shapeable ("+label+")";
	}

	public int getPlaced() {
		return squares.size();
	}

	public boolean contains(Point p) {
		return squares.contains(p);
	}

	public void addCube(Point p) {
		if (!squares.contains(p)) {
			int old = squares.size();
			squares.add(p);
			pcs.firePropertyChange(PROPERTY_PLACED, old, squares.size());
			canvas.repaint();
		}
	}

	public void removeCube(Point p) {
		if (squares.contains(p)) {
			int old = squares.size();
			squares.remove(p);
			pcs.firePropertyChange(PROPERTY_PLACED, old, squares.size());
			canvas.repaint();
		}
	}

	/**
	 * 
	 * @return array of the points defining the centres of the cubes
	 */
	public Point[] getCubes() {
		Point[] ps = new Point[squares.size()];
		for (int i = 0; i < ps.length; i++) {
			ps[i] = new Point(squares.get(i));
		}
		return ps;
	}

	@Override
	public Object getProperty(String property) {
		if (property.equals(PROPERTY_PLACED)) {
			return getPlaced();
		} else {
			return super.getProperty(property);
		}
	}

	@Override
	public void setProperty(String property, Object value) {
		if (property.equals(PROPERTY_ADDCUBE)) {
			addCube((Point)value);
		} else if (property.equals(PROPERTY_REMOVECUBE)) {
			removeCube((Point)value);
		} else {
			super.setProperty(property, value);
		}
	}
}
