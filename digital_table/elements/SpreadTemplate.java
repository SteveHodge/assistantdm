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

import digital_table.server.MapCanvas.Order;

// TODO convert PROPERTY constants to enum?
// TODO cache area when drawing?

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
	
	Property<Integer> x, y;
	Property<Integer> radius;
	Property<Float> alpha = new Property<Float>(PROPERTY_ALPHA, 1.0f, Float.class);
	Property<Color> color = new Property<Color>(PROPERTY_COLOR, Color.RED, Color.class);
	Property<Type> type = new Property<Type>(PROPERTY_TYPE, Type.CIRCLE, Type.class);
	Property<Direction> direction = new Property<Direction>(PROPERTY_DIRECTION, Direction.SE, Direction.class);
	Property<String> label = new Property<String>(PROPERTY_LABEL, false, "", String.class);

	transient boolean affected[][];

	public SpreadTemplate(int radius, int x, int y) {
		this.x = new Property<Integer>(PROPERTY_X, true, x, Integer.class);
		this.y = new Property<Integer>(PROPERTY_Y, true, y, Integer.class);
		this.radius = new Property<Integer>(PROPERTY_RADIUS, true, radius, Integer.class) {
			private static final long serialVersionUID = 1L;

			public void setValue(Integer v) {
				if (value.equals(v)) return;
				Integer old = value;
				value = v;
				affected = null;
				pcs.firePropertyChange(name, old, value);
				if (visible && canvas != null) canvas.repaint();
			}
		};
		calculateSpread();
	}
	
	public Order getDefaultOrder() {
		return Order.BELOWGRID;
	}

	public void paint(Graphics2D g) {
		if (canvas == null || !visible.getValue()) return;

		Stroke oldStroke = g.getStroke();
		g.setColor(color.getValue());
		Composite c = g.getComposite();
		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha.getValue()));

		// build the shape
		Area area = new Area();
		if (type.getValue() == Type.CIRCLE) {
			area.add(getQuadrant(1, 1));
			area.add(getQuadrant(1, -1));
			area.add(getQuadrant(-1, 1));
			area.add(getQuadrant(-1, -1));
		} else if (type.getValue() == Type.QUADRANT) {
			if (direction.getValue().getXDirection() == 0) {
				area.add(getVerticalQuadrant(direction.getValue().getYDirection()));
			} else if (direction.getValue().getYDirection() == 0) {
				area.add(getHorizontalQuadrant(direction.getValue().getXDirection()));
			} else {
				// diagonal cones - just draw the quadrant
				area.add(getQuadrant(direction.getValue().getXDirection(), direction.getValue().getYDirection()));
			}
		}
		g.fill(area);
		g.setColor(darken(color.getValue()));
		g.setStroke(getThickStroke());
		g.draw(area);
		if (type.getValue() == Type.CIRCLE) {
			Point t = canvas.getDisplayCoordinates(x.getValue(), y.getValue()); 
			g.fillOval(t.x-5, t.y-5, 10, 10);
		}
		g.setStroke(oldStroke);
		g.setComposite(c);
	}

	protected Area getQuadrant(int xdir, int ydir) {
		Area area = new Area();

		if (affected == null) calculateSpread();
	
		for (int i = 0; i < radius.getValue(); i++) {
			for (int j = 0; j < radius.getValue(); j++) {
				if (affected[i][j]) {
					int gridx = xdir*i+x.getValue();
					int gridy = ydir*j+y.getValue();
					area.add(new Area(getRectangle(gridx, gridy, gridx+xdir, gridy+ydir)));
				}
			}
		}
		return area;
	}

	// if the rectangle has any negative dimensions it will be modified to make those dimensions positive
	protected Rectangle getRectangle(int x1, int y1, int x2, int y2) {
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
	
	protected Area getVerticalQuadrant(int ydir) {
		Area area = new Area();
		if (affected == null) calculateSpread();
		for (int i = 0; i < radius.getValue(); i++) {
			for (int j = 0; j < radius.getValue(); j++) {
				if (affected[i][j] &&  Math.abs(i) <= Math.abs(j)) {
					int gridy = ydir*j+y.getValue();
					area.add(new Area(getRectangle(x.getValue()+i, gridy, x.getValue()+i+1, gridy+ydir)));
					area.add(new Area(getRectangle(x.getValue()-i-1, gridy, x.getValue()-i, gridy+ydir)));
				}
			}
		}
		return area;
	}
	protected Area getHorizontalQuadrant(int xdir) {
		Area area = new Area();
		if (affected == null) calculateSpread();
		for (int i = 0; i < radius.getValue(); i++) {
			for (int j = 0; j < radius.getValue(); j++) {
				if (affected[i][j] && Math.abs(i) >= Math.abs(j)) {
					int gridx = xdir*i+x.getValue();
					area.add(new Area(getRectangle(gridx, y.getValue()-j-1, gridx+xdir, y.getValue()-j)));
					area.add(new Area(getRectangle(gridx, y.getValue()+j, gridx+xdir, y.getValue()+j+1)));
				}
			}
		}
		return area;
	}

	// TODO better not to cache this - then radius need not be an subclass
	protected void calculateSpread() {
		affected = new boolean[radius.getValue()][radius.getValue()];

		// calculate the affected cells
		for (int i = 0; i < radius.getValue(); i++) {
			for (int j = 0; j < radius.getValue(); j++) {
				// measure distance from (0, 0) to each corner of this cell
				// if all four corners are within the radius then the cell is affected
				// note: only need to test the bottom right corner - if that is in the radius then the other corners must be 
				int dist = i+1 + j+1 - (Math.min(i+1, j+1)-1)/2;	// the subtracted term is half the number of diagonals
				if (dist <= radius.getValue()+1) affected[i][j] = true;
			}
		}
	}

	protected Stroke getThickStroke() {
		if (canvas.getColumnWidth() < 40) return new BasicStroke(3);
		return new BasicStroke(5);
	}

/*	public void paint(Graphics2D g) {
		if (canvas == null || !visible) return;

		Stroke oldStroke = g.getStroke();
		g.setColor(color);
		Composite c = g.getComposite();
		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
		if (type == Type.CIRCLE) {
			paintQuadrant(g, 1, 1);
			paintQuadrant(g, 1, -1);
			paintQuadrant(g, -1, 1);
			paintQuadrant(g, -1, -1);
			g.setColor(darken(color));
			Point t = canvas.getDisplayCoordinates(x, y); 
			g.fillOval(t.x-5, t.y-5, 10, 10);
		} else if (type == Type.QUADRANT) {
			if (direction.getXDirection() == 0) {
				paintQuadrantVert(g, direction.getYDirection());
			} else if (direction.getYDirection() == 0) {
				paintQuadrantHoriz(g, direction.getXDirection());
			} else {
				// diagonal cones - just draw the quadrant
				paintQuadrant(g, direction.getXDirection(), direction.getYDirection());
				// draw the edges from the origin
				g.setColor(darken(color));
				g.setStroke(getThickStroke());
				Point s = canvas.getDisplayCoordinates(x, y);
				Point e = canvas.getDisplayCoordinates(x+direction.getXDirection()*radius, y);
				g.drawLine(s.x, s.y, e.x, e.y);
				canvas.getDisplayCoordinates(x, y+direction.getYDirection()*radius, e);
				g.drawLine(s.x, s.y, e.x, e.y);
			}
		}
		g.setStroke(oldStroke);
		g.setComposite(c);
	}

	protected void paintQuadrantVert(Graphics2D g, int ydir) {
		if (affected == null) calculateSpread();
		Point p1 = new Point();
		Point p2 = new Point();
		Stroke oldStroke = g.getStroke();
		Stroke edgeStroke = getThickStroke();	
		for (int i = 0; i < radius; i++) {
			for (int j = 0; j < radius; j++) {
				if (affected[i][j] &&  Math.abs(i) <= Math.abs(j)) {
					int gridy = ydir*j+y;

					g.setColor(color);
					g.setStroke(oldStroke);
					canvas.getDisplayCoordinates(x+i, gridy, p1);
					canvas.getDisplayCoordinates(x+i+1, gridy+ydir, p2);
					g.fillRect(p1.x, p1.y, p2.x-p1.x, p2.y-p1.y);
					canvas.getDisplayCoordinates(x-i-1, gridy, p1);
					canvas.getDisplayCoordinates(x-i, gridy+ydir, p2);
					g.fillRect(p1.x, p1.y, p2.x-p1.x, p2.y-p1.y);

					g.setColor(darken(color));
					g.setStroke(edgeStroke);
					if (j-ydir < 0 || j-ydir==radius || !affected[i][j-ydir] || Math.abs(i) > Math.abs(j-ydir)) {
						int yadj = ydir < 0 ? ydir : 0;
						canvas.getDisplayCoordinates(x-i-1, gridy+yadj, p1);
						canvas.getDisplayCoordinates(x-i, gridy+yadj, p2);
						g.drawLine(p1.x, p1.y, p2.x, p2.y);
						canvas.getDisplayCoordinates(x+i+1, gridy+yadj, p1);
						canvas.getDisplayCoordinates(x+i, gridy+yadj, p2);
						g.drawLine(p1.x, p1.y, p2.x, p2.y);
					}
					if (j+ydir < 0 || j+ydir==radius || !affected[i][j+ydir] || Math.abs(i) > Math.abs(j+ydir)) {
						int yadj = ydir > 0 ? ydir : 0;
						canvas.getDisplayCoordinates(x-i-1, gridy+yadj, p1);
						canvas.getDisplayCoordinates(x-i, gridy+yadj, p2);
						g.drawLine(p1.x, p1.y, p2.x, p2.y);
						canvas.getDisplayCoordinates(x+i+1, gridy+yadj, p1);
						canvas.getDisplayCoordinates(x+i, gridy+yadj, p2);
						g.drawLine(p1.x, p1.y, p2.x, p2.y);
					}
					if (i+1 < 0 || i+1==radius || !affected[i+1][j] || Math.abs(i+1) > Math.abs(j)) {
						canvas.getDisplayCoordinates(x-i-1, gridy, p1);
						canvas.getDisplayCoordinates(x-i-1, gridy+ydir, p2);
						g.drawLine(p1.x, p1.y, p2.x, p2.y);
						canvas.getDisplayCoordinates(x+i+1, gridy, p1);
						canvas.getDisplayCoordinates(x+i+1, gridy+ydir, p2);
						g.drawLine(p1.x, p1.y, p2.x, p2.y);
					}
				}
			}
		}
	}

	protected void paintQuadrantHoriz(Graphics2D g, int xdir) {
		if (affected == null) calculateSpread();
		Point p1 = new Point();
		Point p2 = new Point();
		Stroke oldStroke = g.getStroke();
		Stroke edgeStroke = getThickStroke();
		for (int i = 0; i < radius; i++) {
			for (int j = 0; j < radius; j++) {
				if (affected[i][j] && Math.abs(i) >= Math.abs(j)) {
					g.setStroke(oldStroke);
					g.setColor(color);
					int gridx = xdir*i+x;
					canvas.getDisplayCoordinates(gridx, y-j-1, p1);
					canvas.getDisplayCoordinates(gridx+xdir, y-j, p2);
					g.fillRect(p1.x, p1.y, p2.x - p1.x, p2.y - p1.y);
					canvas.getDisplayCoordinates(gridx, y+j, p1);
					canvas.getDisplayCoordinates(gridx+xdir, y+j+1, p2);
					g.fillRect(p1.x, p1.y, p2.x - p1.x, p2.y - p1.y);

					g.setColor(darken(color));
					g.setStroke(edgeStroke);
					// check the edge closest to x - if the cell on the other side (with x = i+xdir) is unaffected the draw the edge
					// it's unaffected if it is not set in the array, if (i+xdir) is out of range for the array, or
					// if it's on the wrong side of the diagonal i = j
					if (i-xdir < 0 || i-xdir==radius || !affected[i-xdir][j] || Math.abs(i-xdir) < Math.abs(j)) {
						int xadj = xdir < 0 ? xdir : 0;
						canvas.getDisplayCoordinates(gridx+xadj, y-j-1, p1);
						canvas.getDisplayCoordinates(gridx+xadj, y-j, p2);
						g.drawLine(p1.x, p1.y, p2.x, p2.y);
						canvas.getDisplayCoordinates(gridx+xadj, y+j+1, p1);
						canvas.getDisplayCoordinates(gridx+xadj, y+j, p2);
						g.drawLine(p1.x, p1.y, p2.x, p2.y);
					}
					// test the outer vertical edge. don't draw if the next cell in xdir is unaffected:
					if (i+xdir < 0 || i+xdir==radius || !affected[i+xdir][j] || Math.abs(i+xdir) < Math.abs(j)) {
						int xadj = xdir > 0 ? xdir : 0;
						canvas.getDisplayCoordinates(gridx+xadj, y-j-1, p1);
						canvas.getDisplayCoordinates(gridx+xadj, y-j, p2);
						g.drawLine(p1.x, p1.y, p2.x, p2.y);
						canvas.getDisplayCoordinates(gridx+xadj, y+j+1, p1);
						canvas.getDisplayCoordinates(gridx+xadj, y+j, p2);
						g.drawLine(p1.x, p1.y, p2.x, p2.y);
					}
					// test the outer horizontal edges. don't draw if the next cell in ydir is unaffected:
					if (j+1 < 0 || j+1==radius || !affected[i][j+1] || Math.abs(i) < Math.abs(j+1)) {
						canvas.getDisplayCoordinates(gridx, y-j-1, p1);
						canvas.getDisplayCoordinates(gridx+xdir, y-j-1, p2);
						g.drawLine(p1.x, p1.y, p2.x, p2.y);
						canvas.getDisplayCoordinates(gridx, y+j+1, p1);
						canvas.getDisplayCoordinates(gridx+xdir, y+j+1, p2);
						g.drawLine(p1.x, p1.y, p2.x, p2.y);
					}
				}
			}
		}
	}

	protected void paintQuadrant(Graphics2D g, int xdir, int ydir) {
		if (affected == null) calculateSpread();
		Point p1 = new Point();
		Point p2 = new Point();

		// paint the affected cells
		g.setColor(color);
		for (int i = 0; i < radius; i++) {
			for (int j = 0; j < radius; j++) {
				if (affected[i][j]) {
					int gridx = xdir*i+x;
					int gridy = ydir*j+y;
					canvas.getDisplayCoordinates(gridx, gridy, p1);
					canvas.getDisplayCoordinates(gridx+xdir, gridy+ydir, p2);
					g.fillRect(p1.x, p1.y, p2.x - p1.x, p2.y - p1.y);
				}
			}
		}

		// draw the edge
		Stroke oldStroke = g.getStroke();
		g.setColor(darken(color));
		g.setStroke(getThickStroke());
		for (int i = 0; i < radius; i++) {
			for (int j = 0; j < radius; j++) {
				if (affected[i][j]) {
					int gridx = xdir*i+x;
					int gridy = ydir*j+y;
					canvas.getDisplayCoordinates(gridx+xdir, gridy+ydir, p2);
					// test the two outer edges (the edges that join at p2)
					// if the cells on the other side of those edges aren't affected then draw a border
					if (i == radius-1 || !affected[i+1][j] || j == radius-1 || !affected[i][j+1]) {
						if (i == radius-1 || !affected[i+1][j]) {
							canvas.getDisplayCoordinates(gridx+xdir, gridy, p1);
							g.drawLine(p1.x, p1.y, p2.x, p2.y);
						}
						if (j == radius-1 || !affected[i][j+1]) {
							canvas.getDisplayCoordinates(gridx, gridy+ydir, p1);
							g.drawLine(p1.x, p1.y, p2.x, p2.y);
						}
					}
				}
			}
		}
		g.setStroke(oldStroke);
	}
*/
	
	public String toString() {
		if (label == null || label.getValue().length() == 0) return "Template ("+getID()+")";
		return "Template ("+label+")";
	}
}
