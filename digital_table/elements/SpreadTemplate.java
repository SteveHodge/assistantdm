package digital_table.elements;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Point2D;

// TODO convert PROPERTY constants to enum?

public class SpreadTemplate extends MapElement {
	private static final long serialVersionUID = 1L;

	public final static String PROPERTY_RADIUS = "radius";	// int
	public final static String PROPERTY_X = "x";	// int
	public final static String PROPERTY_Y = "y";	// int
	public final static String PROPERTY_COLOR = "color";	// Color
	public final static String PROPERTY_TYPE = "type";	// int, using constants below
	public final static String PROPERTY_DIRECTION = "direction";	// int, using constants below
	public final static String PROPERTY_ALPHA = "alpha";	// float
	public final static String PROPERTY_LABEL = "label";
	
	public enum Type {
		CIRCLE("Circle"),
		QUADRANT("Quadrant");

		public String toString() {return description;}

		private Type(String d) {description = d;}

		private final String description;
	};
	
	public enum Direction {
		N(0,-1),
		NE(1,-1),
		E(1,0),
		SE(1,1),
		S(0,1),
		SW(-1,1),
		W(-1,0),
		NW(-1,-1);

		public int getXDirection() {return xDir;}
		
		public int getYDirection() {return yDir;}
		
		private Direction(int xdir, int ydir) {xDir = xdir; yDir = ydir;}
		
		private final int xDir, yDir;
	}
	
	int radius;
	int x, y;
	Color color = Color.RED;
	float alpha = 1.0f;
	Type type = Type.CIRCLE;
	Direction direction = Direction.SE;
	String label = null;

	transient boolean affected[][];

	public SpreadTemplate(int radius, int x, int y) {
		this.radius = radius;
		this.x = x;
		this.y = y;
		calculateSpread();
	}
	
	protected void calculateSpread() {
		affected = new boolean[radius][radius];

		// calculate the affected cells
		for (int i = 0; i < radius; i++) {
			for (int j = 0; j < radius; j++) {
				// measure distance from (0, 0) to each corner of this cell
				// if all four corners are within the radius then the cell is affected
				// note: only need to test the bottom right corner - if that is in the radius then the other corners must be 
				int dist = i+1 + j+1 - (Math.min(i+1, j+1)-1)/2;	// the subtracted term is half the number of diagonals
				if (dist <= radius+1) affected[i][j] = true;
			}
		}
	}

	public void paint(Graphics2D g) {
		if (canvas == null || !visible) return;

		g.setColor(color);
		Composite c = g.getComposite();
		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
		if (type == Type.CIRCLE) {
			paintQuadrant(g, 1, 1);
			paintQuadrant(g, 1, -1);
			paintQuadrant(g, -1, 1);
			paintQuadrant(g, -1, -1);
		} else if (type == Type.QUADRANT) {
			if (direction.getXDirection() == 0) {
				paintQuadrantVert(g, direction.getYDirection());
			} else if (direction.getYDirection() == 0) {
				paintQuadrantHoriz(g, direction.getXDirection());
			} else {
				// diagonal cones - just draw the quadrant
				paintQuadrant(g, direction.getXDirection(), direction.getYDirection());
			}
		}
		g.setComposite(c);
	}

	protected void paintQuadrantVert(Graphics2D g, int ydir) {
		if (affected == null) calculateSpread();
		Point p = new Point();
		for (int i = 0; i < radius; i++) {
			for (int j = 0; j < radius; j++) {
				if (affected[i][j] &&  Math.abs(i) <= Math.abs(j)) {
					canvas.getDisplayCoordinates(i+x, ydir*j+y+(ydir-1)/2, p);	// the (_dir-1)/2 part subtracts one from the coordinates if that direction is negative
					g.fillRect(p.x, p.y, canvas.getColumnWidth()+1, canvas.getRowHeight()+1);
					canvas.getDisplayCoordinates(-i+x-1, ydir*j+y+(ydir-1)/2, p);	// the (_dir-1)/2 part subtracts one from the coordinates if that direction is negative
					g.fillRect(p.x, p.y, canvas.getColumnWidth()+1, canvas.getRowHeight()+1);
				}
			}
		}
	}

	protected void paintQuadrantHoriz(Graphics2D g, int xdir) {
		if (affected == null) calculateSpread();
		Point p = new Point();
		for (int i = 0; i < radius; i++) {
			for (int j = 0; j < radius; j++) {
				if (affected[i][j] && Math.abs(i) >= Math.abs(j)) {
					canvas.getDisplayCoordinates(xdir*i+x+(xdir-1)/2, -j+y+-1, p);	// the (_dir-1)/2 part subtracts one from the coordinates if that direction is negative
					g.fillRect(p.x, p.y, canvas.getColumnWidth()+1, canvas.getRowHeight()+1);
					canvas.getDisplayCoordinates(xdir*i+x+(xdir-1)/2, j+y, p);	// the (_dir-1)/2 part subtracts one from the coordinates if that direction is negative
					g.fillRect(p.x, p.y, canvas.getColumnWidth()+1, canvas.getRowHeight()+1);
				}
			}
		}
	}

	protected void paintQuadrant(Graphics2D g, int xdir, int ydir) {
		if (affected == null) calculateSpread();
		Point p = new Point();
		for (int i = 0; i < radius; i++) {
			for (int j = 0; j < radius; j++) {
				if (affected[i][j]) {
					canvas.getDisplayCoordinates(xdir*i+x+(xdir-1)/2, ydir*j+y+(ydir-1)/2, p);	// the (_dir-1)/2 part subtracts one from the coordinates if that direction is negative
					g.fillRect(p.x, p.y, canvas.getColumnWidth()+1, canvas.getRowHeight()+1);
				}
			}
		}
	}
	
	public String toString() {
		if (label == null || label.length() == 0) return "Template ("+getID()+")";
		return "Template ("+label+")";
	}
	
	public String getLabel() {
		return label == null ? "" : label;
	}
	
	public void setLabel(String l) {
		String old = label;
		label = l;
		pcs.firePropertyChange(PROPERTY_LABEL, old, label);
	}

	public int getRadius() {
		return radius;
	}
	
	public void setRadius(int r) {
		if (r == radius) return;
		int old = radius;
		radius = r;
		affected = null;
		pcs.firePropertyChange(PROPERTY_RADIUS, old, radius);
		if (canvas != null) canvas.repaint();
	}

	public int getX() {
		return x;
	}
	
	public void setX(int newX) {
		if (x == newX) return;
		int old = x;
		x = newX;
		pcs.firePropertyChange(PROPERTY_X, old, x);
		if (canvas != null) canvas.repaint();
	}

	public int getY() {
		return y;
	}
	
	public void setY(int newY) {
		if (y == newY) return;
		int old = y;
		y = newY;
		pcs.firePropertyChange(PROPERTY_Y, old, y);
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
	
	public Type getType() {
		return type;
	}
	
	public void setType(Type t) {
		if (type == t) return;
		Type old = type;
		type = t;
		pcs.firePropertyChange(PROPERTY_TYPE, old, type);
		if (canvas != null) canvas.repaint();
	}

	public Direction getDirection() {
		return direction;
	}
	
	public void setDirection(Direction d) {
		if (direction == d) return;
		Direction old = direction;
		direction = d;
		pcs.firePropertyChange(PROPERTY_DIRECTION, old, direction);
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

	public Object getProperty(String property) {
		if (property.equals(PROPERTY_RADIUS)) {
			return getRadius();
		} else if (property.equals(PROPERTY_X)) {
			return getX();
		} else if (property.equals(PROPERTY_Y)) {
			return getY();
		} else if (property.equals(PROPERTY_COLOR)) {
			return getColor();
		} else if (property.equals(PROPERTY_TYPE)) {
			return getType();
		} else if (property.equals(PROPERTY_DIRECTION)) {
			return getDirection();
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
		if (property.equals(PROPERTY_RADIUS)) {
			setRadius((Integer)value);
		} else if (property.equals(PROPERTY_X)) {
			setX((Integer)value);
		} else if (property.equals(PROPERTY_Y)) {
			setY((Integer)value);
		} else if (property.equals(PROPERTY_COLOR)) {
			setColor((Color)value);
		} else if (property.equals(PROPERTY_TYPE)) {
			setType((Type)value);
		} else if (property.equals(PROPERTY_DIRECTION)) {
			setDirection((Direction)value);
		} else if (property.equals(PROPERTY_ALPHA)) {
			setAlpha((Float)value);
		} else if (property.equals(PROPERTY_LABEL)) {
			setLabel((String)value);
		} else {
			// throw exception?
		}
	}

	public boolean isDraggable() {
		return true;
	}

	public Point2D getLocation() {
		return new Point(x,y);
	}
	
	public void setLocation(Point2D p) {
		setX((int)p.getX());
		setY((int)p.getY());
	}
}
