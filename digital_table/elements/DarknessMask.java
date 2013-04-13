package digital_table.elements;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.geom.Area;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import digital_table.server.MapCanvas.Order;

public class DarknessMask extends MapElement {
	private static final long serialVersionUID = 1L;

	public final static String PROPERTY_COLOR = "color";	// Color
	public final static String PROPERTY_ALPHA = "alpha";	// float

	Color color = Color.BLACK;
	float alpha = 1.0f;
	
	boolean dragClear = true;	// when dragging if true then we clear cells, otherwise we reset cells 
	List<Point> cleared = new ArrayList<Point>();
	
	public Order getDefaultOrder() {
		return Order.AboveGrid;
	}

	public void paint(Graphics2D g) {
		if (canvas == null || !visible) return;

		g.setColor(color);
		Composite c = g.getComposite();
		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));

		// build the shape
		Area area = new Area(g.getClip());
		for (Point p : cleared) {
			Point tl = canvas.getDisplayCoordinates(p.x, p.y);
			Point br = canvas.getDisplayCoordinates(p.x+1, p.y+1);
			area.subtract(new Area(new Rectangle(tl.x, tl.y, br.x - tl.x, br.y - tl.y)));
		}

		g.fill(area);
		g.setComposite(c);
	}

	public void elementClicked(Point2D location, MouseEvent e, boolean dragging) {
		if (!dragging) {
			if (e.getButton() != MouseEvent.BUTTON1) return;
			if (e.getClickCount() != 1) return;
		}

		// get nearest grid intersection
		int x = (int)location.getX();
		int y = (int)location.getY();
		Point p = new Point(x,y);
		// TODO cleanup logic:
		if (!dragging) {
			if (cleared.contains(p)) {
				cleared.remove(p);
			} else {
				cleared.add(p);
			}
		} else if (dragging && dragClear) {
			if (!cleared.contains(p)) cleared.add(p);
		} else if (dragging && !dragClear) {
			if (cleared.contains(p)) cleared.remove(p);
		}
		canvas.repaint();
	}

	public DragMode getDragMode() {
		return DragMode.PAINT;
	}
	
	public Object getDragTarget(Point2D location) {
		// get nearest grid intersection
		int x = (int)location.getX();
		int y = (int)location.getY();
		Point p = new Point(x,y);
		dragClear = !cleared.contains(p);	// if the cell is already cleared then we are reseting, otherwise clearing 
		return "MASK";
	}

	public String toString() {
		return "Darkness ("+getID()+")";
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
		} else {
			// throw exception?
		}
	}
}
