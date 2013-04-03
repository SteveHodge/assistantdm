package digital_table.elements;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Stroke;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

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
	
	public void paint(Graphics2D g) {
		if (canvas == null || !visible) return;

		Composite c = g.getComposite();
		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));

		// calculate the end point of the line based on range
		double dist = Math.sqrt((targetX-originX)*(targetX-originX)+(targetY-originY)*(targetY-originY));
		double endX = originX + range*(targetX - originX)/dist;
		double endY = originY + range*(targetY - originY)/dist;
		
		// test for affected cells
		g.setColor(new Color(color.getRed()/2+127,color.getBlue()/2+127,color.getGreen()/2+127));
		int minX, maxX, minY, maxY;
		minX = (int)(originX < endX ? originX : endX-1);
		minY = (int)(originY < endY ? originY : endY-1);
		maxX = (int)(endX < originX ? originX : endX+1);
		maxY = (int)(endY < originY ? originY : endY+1);
		for (int x = minX; x < maxX; x++) {
			boolean seen = false;
			for (int y = minY; y < maxY; y++) {
				Rectangle2D r = new Rectangle2D.Double(x,y,1,1);
				if (r.intersectsLine(originX, originY, endX, endY)) {
					Point tl = canvas.getDisplayCoordinates(x, y);
					Point br = canvas.getDisplayCoordinates(x+1, y+1);
					g.fillRect(tl.x, tl.y, br.x - tl.x, br.y - tl.y);
					seen = true;
				} else if (seen) {
					break;	// this cell is not included so no other cells in this column can be included either
				}
			}
		}
		
		g.setColor(color);
		Stroke oldStroke = g.getStroke();
		g.setStroke(new BasicStroke(3));
		Point s = canvas.getDisplayCoordinates(originX, originY);
		Point e = canvas.getDisplayCoordinates(new Point2D.Double(endX, endY));
		g.drawLine(s.x, s.y, e.x, e.y);
		Point t = canvas.getDisplayCoordinates(targetX, targetY); 
		g.fillOval(t.x-5, t.y-5, 10, 10);
		g.setStroke(oldStroke);

		g.setComposite(c);
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

	public boolean isDraggable() {
		return true;
	}

	public Point2D getLocation() {
		return new Point(targetX,targetY);
	}
	
	public void setLocation(Point2D p) {
		setX((int)p.getX());
		setY((int)p.getY());
	}
}
