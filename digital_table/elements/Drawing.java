package digital_table.elements;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Stroke;
import java.awt.geom.Point2D;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

//TODO optimize by grouping fills (and lines) by color. test if caching an Area for each color is faster

public class Drawing extends MapElement {
	private static final long serialVersionUID = 1L;

	public static class Fill implements Serializable {
		private static final long serialVersionUID = 1L;
		public int gridx;
		public int gridy;
		public Color color;

		public Fill() {
			this(0, 0, Color.BLACK);
		}

		public Fill(Fill f) {
			this(f.gridx, f.gridy, f.color);
		}

		public Fill(int x, int y, Color c) {
			gridx = x;
			gridy = y;
			color = c;
		}

		// returns true if f is in the same location as this Fill
		public boolean isCoincident(Fill f) {
			return gridx == f.gridx && gridy == f.gridy;
		}

		public boolean isCoincident(Point p) {
			return gridx == p.x && gridy == p.y;
		}
	}

	public static class Line implements Serializable {
		private static final long serialVersionUID = 1L;
		public int x1, y1, x2, y2;
		public Color color;

		public Line(int x1, int y1, int x2, int y2, Color c) {
			this.x1 = x1;
			this.y1 = y1;
			this.x2 = x2;
			this.y2 = y2;
			color = c;
		}

		public Line(Line l) {
			this(l.x1, l.y1, l.x2, l.y2, l.color);
		}

		public Line() {
			this(0, 0, 0, 0, Color.BLACK);
		}

		// returns true if l is in the same location as this Line
		public boolean isCoincident(Line l) {
			if (x1 == l.x1 && y1 == l.y1 && x2 == l.x2 && y2 == l.y2) return true;
			return x1 == l.x2 && y1 == l.y2 && x2 == l.x1 && y2 == l.y1;
		}
	}

	public final static String PROPERTY_LABEL = "label";	// String
	public final static String PROPERTY_ADD_FILL = "add_fill";	// Fill - when this property is set the grid square specified will be filled with the specified color
	public final static String PROPERTY_REMOVE_FILL = "remove_fill";	// Point - when this property is set the grid square specified by the point will be removed
	public final static String PROPERTY_ADD_LINE = "add_line";	// Line - when this property is set a line is drawn between the specified coordinates
	public final static String PROPERTY_REMOVE_LINE = "remove_line";	// Line - remove any line matching the coordinates of the supplied line

	Property<String> label = new Property<String>(PROPERTY_LABEL, false, "", String.class);

	List<Fill> fills = new ArrayList<>();
	List<Line> lines = new ArrayList<>();

	private Line tempLine;

	@Override
	public Layer getDefaultLayer() {
		return Layer.MAP_FOREGROUND;
	}

	@Override
	public void paint(Graphics2D g) {
		if (canvas == null || getVisibility() == Visibility.HIDDEN) return;

		Point2D o = canvas.convertGridCoordsToDisplay(canvas.getElementOrigin(this));
		g.translate(o.getX(), o.getY());

		Composite c = g.getComposite();

		// build the shape
//		Area area = new Area();
		for (Fill f : fills) {
			g.setColor(f.color);
			Point tl = canvas.convertGridCoordsToDisplay(f.gridx, f.gridy);
			Point br = canvas.convertGridCoordsToDisplay(f.gridx + 1, f.gridy + 1);
//			area.add(new Area(new Rectangle(tl.x, tl.y, br.x - tl.x, br.y - tl.y)));
			g.fillRect(tl.x, tl.y, br.x - tl.x, br.y - tl.y);
		}

		Stroke oldStroke = g.getStroke();
		g.setStroke(getThickStroke());

		for (Line l : lines) {
			g.setColor(l.color);
			Point p1 = canvas.convertGridCoordsToDisplay(l.x1, l.y1);
			Point p2 = canvas.convertGridCoordsToDisplay(l.x2, l.y2);
			g.drawLine(p1.x, p1.y, p2.x, p2.y);
		}

		if (tempLine != null) {
			g.setColor(tempLine.color);
			Point p1 = canvas.convertGridCoordsToDisplay(tempLine.x1, tempLine.y1);
			Point p2 = canvas.convertGridCoordsToDisplay(tempLine.x2, tempLine.y2);
			g.drawLine(p1.x, p1.y, p2.x, p2.y);
		}

		g.setStroke(oldStroke);

		g.setComposite(c);
		g.translate(-o.getX(), -o.getY());
	}

	@Override
	public String getIDString() {
		return "Drawing" + (label == null || label.getValue().length() == 0 ? "" : " (" + label + ")");
	}

	protected Stroke getThickStroke() {
		if (canvas.getColumnWidth() < 40) return new BasicStroke(3);
		return new BasicStroke(5);
	}

	@Override
	public String toString() {
		if (label == null || label.getValue().length() == 0) return "Drawing (" + getID() + ")";
		return "Drawing (" + label + ")";
	}

	public boolean containsFill(Point p) {
		for (Fill f : fills) {
			if (f.isCoincident(p)) {
				return true;
			}
		}
		return false;
	}

	public boolean containsLine(Line l) {
		for (Line line : lines) {
			if (line.isCoincident(l)) {
				return true;
			}
		}
		return false;
	}

	// Add a temporary line. This is used by the ui while making a line
	public void setTempLine(Point start, int endx, int endy, Color c) {
		tempLine = new Line(start.x, start.y, endx, endy, c);
		canvas.repaint();
	}

	public void clearTempLine() {
		tempLine = null;
		canvas.repaint();
	}

	public void addFill(Fill f) {
		removeFill(new Point(f.gridx, f.gridy));
		fills.add(f);
		canvas.repaint();
	}

	public void removeFill(Point p) {
		for (int i = fills.size() - 1; i >= 0; i--) {
			Fill f = fills.get(i);
			if (f.isCoincident(p)) {
				fills.remove(f);
				canvas.repaint();
			}
		}
	}

	public void addLine(Line l) {
		removeLine(l);
		lines.add(l);
		canvas.repaint();
	}

	public void removeLine(Line l) {
		for (int i = lines.size() - 1; i >= 0; i--) {
			Line line = lines.get(i);
			if (line.isCoincident(l)) {
				lines.remove(line);
				canvas.repaint();
			}
		}
	}

	/**
	 *
	 * @return array of the filled grid squares
	 */
	public Fill[] getFills() {
		Fill[] fs = new Fill[fills.size()];
		int i = 0;
		for (Fill f : fills) {
			fs[i++] = new Fill(f);
		}
		return fs;
	}

	public Line[] getLines() {
		Line[] ls = new Line[lines.size()];
		int i = 0;
		for (Line l : lines) {
			ls[i++] = new Line(l);
		}
		return ls;
	}

	@Override
	public void setProperty(String property, Object value) {
		if (property.equals(PROPERTY_ADD_FILL)) {
			addFill((Fill) value);
		} else if (property.equals(PROPERTY_REMOVE_FILL)) {
			removeFill((Point) value);
		} else if (property.equals(PROPERTY_ADD_LINE)) {
			addLine((Line) value);
		} else if (property.equals(PROPERTY_REMOVE_LINE)) {
			removeLine((Line) value);
		} else {
			super.setProperty(property, value);
		}
	}
}
