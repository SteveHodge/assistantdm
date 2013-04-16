package digital_table.elements;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Stroke;
import java.awt.geom.Point2D;

import digital_table.server.MapCanvas.Order;

public class LineTemplate extends MapElement {
	private static final long serialVersionUID = 1L;

	public final static String PROPERTY_X = "x";	// int
	public final static String PROPERTY_Y = "y";	// int
	public final static String PROPERTY_ORIGIN_X = "origin_x";	// int
	public final static String PROPERTY_ORIGIN_Y = "origin_y";	// int
	public final static String PROPERTY_COLOR = "color";	// Color
	public final static String PROPERTY_ALPHA = "alpha";	// float
	public final static String PROPERTY_RANGE = "range";	// int
	public final static String PROPERTY_LABEL = "label";
	public static final String PROPERTY_TARGET_LOCATION = "target";	// Point
	public static final String PROPERTY_ORIGIN_LOCATION = "origin";	// Point

	int originX, originY, targetX, targetY;
	int range = 12;
	Color color = Color.RED;
	float alpha = 1.0f;
	String label;
	
	public LineTemplate(int ox, int oy, int tx, int ty) {
		originX = ox;
		originY = oy;
		targetX = tx;
		targetY = ty;
	}
	
	public Order getDefaultOrder() {
		return Order.BELOWGRID;
	}

	public void paint(Graphics2D g) {
		if (canvas == null || !visible) return;

		Composite c = g.getComposite();
		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));

		// calculate the end point of the line based on range
		double dist = Math.sqrt((targetX-originX)*(targetX-originX)+(targetY-originY)*(targetY-originY));
		double endX = originX + range*(targetX - originX)/dist;
		double endY = originY + range*(targetY - originY)/dist;
		
		// test for affected cells
		g.setColor(color);
// this version uses intersections of the line with the rectangle formed by each cell
// it doesn't work consistently for cells that have a corner on the line. could simply special-case those
// cells but the below implementation *should* be more efficient
//		int minX, maxX, minY, maxY;
//		minX = (int)(originX < endX ? originX : endX-1);
//		minY = (int)(originY < endY ? originY : endY-1);
//		maxX = (int)(endX < originX ? originX : endX+1);
//		maxY = (int)(endY < originY ? originY : endY+1);
//		for (int x = minX; x < maxX; x++) {
//			boolean seen = false;
//			for (int y = minY; y < maxY; y++) {
//				Rectangle2D r = new Rectangle2D.Double(x,y,1,1);
//				if (r.intersectsLine(originX, originY, endX, endY)) {
//					Point tl = canvas.getDisplayCoordinates(x, y);
//					Point br = canvas.getDisplayCoordinates(x+1, y+1);
//					g.fillRect(tl.x, tl.y, br.x - tl.x, br.y - tl.y);
//					seen = true;
//				} else if (seen) {
//					break;	// this cell is not included so no other cells in this column can be included either
//				}
//			}
//		}
//		System.out.println();
		if (targetX == originX) {
			int minY = originY;
			int maxY = 1 + (int)endY;
			if (maxY < minY) {
				int t = minY;
				minY = maxY - 2;
				maxY = t ;
			}
			Point tl = canvas.getDisplayCoordinates(originX-1, minY);
			Point br = canvas.getDisplayCoordinates(originX+1, maxY);
			g.fillRect(tl.x, tl.y, br.x - tl.x, br.y - tl.y);
		} else if (targetX > originX) {
			for (int x = 0; x <= endX-originX; x++) {
				Point range = getVerticalRange(x, targetX, endX, endY);
				//System.out.println("x = "+x+", miny = "+range.x+", maxy = "+range.y+", endX = "+endX);
				for (int y = range.x; y <= range.y; y++) {
					Point tl = canvas.getDisplayCoordinates(x+originX, y);
					Point br = canvas.getDisplayCoordinates(x+originX+1, y+1);
					g.fillRect(tl.x, tl.y, br.x - tl.x, br.y - tl.y);
				}
			}
		} else {
			// targetX < originX
			for (int x = 0; x >= endX-originX; x--) {
				// we mirror the relevant x coordinates to calculate the range as if it were the other case
				Point range = getVerticalRange(-x, originX*2-targetX, originX*2-endX, endY);
				for (int y = range.x; y <= range.y; y++) {
					Point tl = canvas.getDisplayCoordinates(x+originX-1, y);
					Point br = canvas.getDisplayCoordinates(x+originX, y+1);
					g.fillRect(tl.x, tl.y, br.x - tl.x, br.y - tl.y);
				}
			}
		}
		g.setColor(darken(color));
		Stroke oldStroke = g.getStroke();
		g.setStroke(getThickStroke());
		Point s = canvas.getDisplayCoordinates(originX, originY);
		Point e = canvas.getDisplayCoordinates(new Point2D.Double(endX, endY));
		g.drawLine(s.x, s.y, e.x, e.y);
		Point t = canvas.getDisplayCoordinates(targetX, targetY); 
		g.fillOval(t.x-5, t.y-5, 10, 10);
		g.setStroke(oldStroke);

		g.setComposite(c);
	}

	protected Point getVerticalRange(int x, int targetX, double endX, double endY) {
		int minY, maxY;	// the range of cells to paint for this column. note that for lines with negative gradiant minY will be larger than maxY
		int deltaX = targetX-originX;
		int deltaY = targetY-originY;
		
		if (originY > targetY) {
			maxY = x*(deltaY)/(deltaX);
			if ((x > 0 || targetY == originY) && maxY * deltaX == x * deltaY) {
				// the smallest y position is an integer then include the previous cell
				// we don't do this for the first column unless the line is horizontal
				maxY++;
			}
			maxY += originY - 1;
			if (x+1+originX > endX) {
				//System.out.println("Using endY = "+endY);
				minY = (int)endY;	// if the next column is past endX then use endY as the end of the range to fill
			}
			else minY = (x+1)*deltaY/deltaX + originY - 1;
		} else {
			minY = x*deltaY/deltaX;
			if ((x > 0 || targetY == originY) && minY * deltaX == x*deltaY) {
				// the smallest y position is an integer then include the previous cell
				// we don't do this for the first column unless the line is horizontal
				minY--;
			}
			minY += originY;
			if (x+1+originX > endX) {
				//System.out.println("Using endY = "+endY);
				maxY = (int)endY;	// if the next column is past endX then use endY as the end of the range to fill
			}
			else maxY = (x+1)*deltaY/deltaX + originY;
		}
		return new Point(minY, maxY);
	}
	
	protected Stroke getThickStroke() {
		if (canvas.getColumnWidth() < 40) return new BasicStroke(3);
		return new BasicStroke(5);
	}

	public String toString() {
		if (label == null || label.length() == 0) return "Line ("+getID()+")";
		return "Line ("+label+")";
	}

	public Object getProperty(String property) {
		if (property.equals(PROPERTY_ORIGIN_X)) {
			return getOriginX();
		} else if (property.equals(PROPERTY_ORIGIN_Y)) {
			return getOriginY();
		} else if (property.equals(PROPERTY_X)) {
			return getX();
		} else if (property.equals(PROPERTY_Y)) {
			return getY();
		} else if (property.equals(PROPERTY_RANGE)) {
			return getRange();
		} else if (property.equals(PROPERTY_COLOR)) {
			return getColor();
		} else if (property.equals(PROPERTY_ALPHA)) {
			return getAlpha();
		} else if (property.equals(PROPERTY_LABEL)) {
			return getLabel();
		} else if (property.equals(PROPERTY_ORIGIN_LOCATION)) {
			return new Point(getOriginX(), getOriginY());
		} else if (property.equals(PROPERTY_TARGET_LOCATION)) {
			return new Point(getX(), getY());
		} else {
			// throw exception?
			return null;
		}
	}

	public void setProperty(String property, Object value) {
		if (property.equals(PROPERTY_ORIGIN_X)) {
			setOriginX((Integer)value);
		} else if (property.equals(PROPERTY_ORIGIN_Y)) {
			setOriginY((Integer)value);
		} else if (property.equals(PROPERTY_X)) {
			setX((Integer)value);
		} else if (property.equals(PROPERTY_Y)) {
			setY((Integer)value);
		} else if (property.equals(PROPERTY_RANGE)) {
			setRange((Integer)value);
		} else if (property.equals(PROPERTY_COLOR)) {
			setColor((Color)value);
		} else if (property.equals(PROPERTY_ALPHA)) {
			setAlpha((Float)value);
		} else if (property.equals(PROPERTY_LABEL)) {
			setLabel((String)value);
		} else if (property.equals(PROPERTY_ORIGIN_LOCATION)) {
			Point2D p = (Point2D)value;
			setOriginX((int)p.getX());
			setOriginY((int)p.getY());
		} else if (property.equals(PROPERTY_TARGET_LOCATION)) {
			Point2D p = (Point2D)value;
			setX((int)p.getX());
			setY((int)p.getY());
		} else {
			// throw exception?
		}
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

	public String getLabel() {
		return label == null ? "" : label;
	}
	
	public void setLabel(String l) {
		String old = label;
		label = l;
		pcs.firePropertyChange(PROPERTY_LABEL, old, label);
	}

	public int getX() {
		return targetX;
	}
	
	public void setX(int newX) {
		if (targetX == newX) return;
		int old = targetX;
		targetX = newX;
		pcs.firePropertyChange(PROPERTY_X, old, targetX);
		if (canvas != null) canvas.repaint();
	}

	public int getY() {
		return targetY;
	}
	
	public void setY(int newY) {
		if (targetY == newY) return;
		int old = targetY;
		targetY = newY;
		pcs.firePropertyChange(PROPERTY_Y, old, targetY);
		if (canvas != null) canvas.repaint();
	}

	public int getOriginY() {
		return originY;
	}
	
	public void setOriginY(int newY) {
		if (originY == newY) return;
		int old = originY;
		originY = newY;
		pcs.firePropertyChange(PROPERTY_ORIGIN_Y, old, originY);
		if (canvas != null) canvas.repaint();
	}

	public int getOriginX() {
		return originX;
	}
	
	public void setOriginX(int newX) {
		if (originX == newX) return;
		int old = originX;
		originX = newX;
		pcs.firePropertyChange(PROPERTY_ORIGIN_X, old, originX);
		if (canvas != null) canvas.repaint();
	}

	public int getRange() {
		return range;
	}
	
	public void setRange(int r) {
		if (range == r) return;
		int old = range;
		range = r;
		pcs.firePropertyChange(PROPERTY_RANGE, old, range);
		if (canvas != null) canvas.repaint();
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
}
