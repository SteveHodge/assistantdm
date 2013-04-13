package digital_table.elements;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.event.MouseEvent;
import java.awt.geom.Area;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import digital_table.server.MapCanvas.Order;

public class ShapeableTemplate extends MapElement {
	private static final long serialVersionUID = 1L;

	public final static String PROPERTY_COLOR = "color";	// Color
	public final static String PROPERTY_ALPHA = "alpha";	// float
	public final static String PROPERTY_LABEL = "label";	// String
	public final static String PROPERTY_MAXIMUM = "maximum";	// int - number of 10ft cubes (0 means unlimited)
	public final static String PROPERTY_PLACED = "placed";		// readonly int - number of 10ft cubes used
	//public final static String PROPERTY_CONTIGUOUS = "contiguous";	// bool - all cubes after the first must be adjacent to at least one other cube
	//public final static String PROPERTY_5FOOT = "5foot";	// bool - shape using 5 foot cubes

	Color color = Color.RED;
	float alpha = 1.0f;
	int maximum = 0;
//	boolean contiguous = true;
	String label;
	
	List<Point> squares = new ArrayList<Point>();
	
	public Order getDefaultOrder() {
		return Order.BelowGrid;
	}

	public void paint(Graphics2D g) {
		if (canvas == null || !visible) return;

		Stroke oldStroke = g.getStroke();
		g.setColor(color);
		Composite c = g.getComposite();
		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));

		// build the shape
		Area area = new Area();
		for (Point p : squares) {
			Point tl = canvas.getDisplayCoordinates(p.x-1, p.y-1);
			Point br = canvas.getDisplayCoordinates(p.x+1, p.y+1);
			area.add(new Area(new Rectangle(tl.x, tl.y, br.x - tl.x, br.y - tl.y)));
		}

		g.fill(area);
		g.setColor(darken(color));
		g.setStroke(getThickStroke());
		g.draw(area);

		g.setStroke(oldStroke);
		g.setComposite(c);
	}

	protected Stroke getThickStroke() {
		if (canvas.getColumnWidth() < 40) return new BasicStroke(3);
		return new BasicStroke(5);
	}

	public void elementClicked(Point2D mouse, MouseEvent e, boolean dragging) {
		if (e.getButton() != MouseEvent.BUTTON1) return;
		if (e.getClickCount() != 1) return;

		// get nearest grid intersection
		int x = (int)(mouse.getX() + 0.5d);
		int y = (int)(mouse.getY() + 0.5d);
		Point p = new Point(x,y);
		int old = squares.size();
		if (squares.contains(p)) {
			squares.remove(p);
		} else {
			squares.add(p);
		}
		pcs.firePropertyChange(PROPERTY_PLACED, old, squares.size());
		canvas.repaint();
	}

	public String toString() {
		if (label == null || label.length() == 0) return "Shapeable ("+getID()+")";
		return "Shapeable ("+label+")";
	}
	
	public String getLabel() {
		return label == null ? "" : label;
	}
	
	public void setLabel(String l) {
		String old = label;
		label = l;
		pcs.firePropertyChange(PROPERTY_LABEL, old, label);
	}

	public Color getColor() {
		return color;
	}
	
	public void setColor(Color c) {
		if (color.equals(c)) return;
		Color old = color;
		color = c;
		pcs.firePropertyChange(PROPERTY_COLOR, old, color);
		if (canvas != null) canvas.repaint();
	}
	
	public float getAlpha() {
		return alpha;
	}
	
	public void setAlpha(float a) {
		if (alpha == a) return;
		float old = alpha;
		alpha = a;
		pcs.firePropertyChange(PROPERTY_ALPHA, old, alpha);
		if (canvas != null) canvas.repaint();
	}

	public int getMaximum() {
		return maximum;
	}
	
	// TODO if maximum will be less than the number of defined cubes then need to truncate the list of defined cubes
	public void setMaximum(int m) {
		if (maximum == m) return;
		int old = maximum;
		maximum = m;
		pcs.firePropertyChange(PROPERTY_MAXIMUM, old, maximum);
		if (canvas != null) canvas.repaint();
	}

	public int getPlaced() {
		return squares.size();
	}
	
//	public boolean getContiguous() {
//		return contiguous;
//	}
//	
//	public void setContiguous(boolean c) {
//		if (contiguous == c) return;
//		boolean old = contiguous;
//		contiguous = c;
//		pcs.firePropertyChange(PROPERTY_ALPHA, old, contiguous);
//		if (canvas != null) canvas.repaint();
//	}

	public Object getProperty(String property) {
		if (property.equals(PROPERTY_COLOR)) {
			return getColor();
		} else if (property.equals(PROPERTY_ALPHA)) {
			return getAlpha();
		} else if (property.equals(PROPERTY_LABEL)) {
			return getLabel();
		} else if (property.equals(PROPERTY_MAXIMUM)) {
			return getMaximum();
		} else if (property.equals(PROPERTY_PLACED)) {
			return getPlaced();
//		} else if (property.equals(PROPERTY_CONTIGUOUS)) {
//			return getContiguous();
		} else {
			// throw exception?
			return null;
		}
	}

	public void setProperty(String property, Object value) {
		if (property.equals(PROPERTY_COLOR)) {
			setColor((Color)value);
		} else if (property.equals(PROPERTY_ALPHA)) {
			setAlpha((Float)value);
		} else if (property.equals(PROPERTY_LABEL)) {
			setLabel((String)value);
		} else if (property.equals(PROPERTY_MAXIMUM)) {
			setMaximum((Integer)value);
//		} else if (property.equals(PROPERTY_CONTIGUOUS)) {
//			setContiguous((Boolean)value);
		} else {
			// throw exception?
		}
	}
}
