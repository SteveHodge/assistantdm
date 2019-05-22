package digital_table.elements;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Area;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ListModel;

import digital_table.server.MapCanvas.Order;

// TODO processing the walls takes too long and happens for each display - it should be threaded and perhaps done before being passed to the MapElement

public class DarknessMask extends MapElement {
	private static final long serialVersionUID = 1L;

	public final static String PROPERTY_COLOR = "color";	// Color
	public final static String PROPERTY_ALPHA = "alpha";	// float
	public final static String PROPERTY_LOW_LIGHT = "low_light";	// boolean
	public final static String PROPERTY_MASKCELL = "mask";	// Point - when this property is set the specified point will be masked
	public final static String PROPERTY_UNMASKCELL = "unmask";	// Point - when this property is set the specified point will be cleared
	public final static String PROPERTY_TRACK_HISTORY = "track_history";	// boolean - show areas that have ever been illuminated
	public final static String PROPERTY_HISTORY_COLOR = "history_color";	// Color
	public final static String PROPERTY_HISTORY = "history";	// String - the serialised version of the area that has ever been illuminated

	Property<Color> color = new Property<Color>(PROPERTY_COLOR, Color.BLACK, Color.class);
	Property<Float> alpha = new Property<Float>(PROPERTY_ALPHA, 1.0f, Float.class);
	Property<Boolean> lowLight = new Property<Boolean>(PROPERTY_LOW_LIGHT, false, Boolean.class);
	Property<Boolean> trackHistory = new Property<Boolean>(PROPERTY_TRACK_HISTORY, false, Boolean.class);
	Property<Color> historyColor = new Property<Color>(PROPERTY_HISTORY_COLOR, Color.BLUE.darker().darker(), Color.class);

	List<Point> cleared = new ArrayList<>();

	Area seen = null;

	@Override
	public Order getDefaultOrder() {
		return Order.ABOVEGRID;
	}

	@Override
	public void paint(Graphics2D g) {
		if (canvas == null || getVisibility() == Visibility.HIDDEN) return;

		// build the shape
		Area dark = new Area(g.getClip());

		// using indexed loop instead of iterator to avoid concurrency issues
		for (int i = 0; i < cleared.size(); i++) {
			Point p = cleared.get(i);
			Point tl = canvas.convertCanvasCoordsToDisplay(p.x, p.y);
			Point br = canvas.convertCanvasCoordsToDisplay(p.x+1, p.y+1);
			dark.subtract(new Area(new Rectangle(tl.x, tl.y, br.x - tl.x, br.y - tl.y)));
		}

		ListModel<MapElement> m = canvas.getModel();
		List<Line2D.Double> wallLayout = new ArrayList<>();	// FIXME should cache this as then LightSource can use its cache more often

		// find the wall layouts
		for (int i = 0; i < m.getSize(); i++) {
			Object e = m.getElementAt(i);
			if (e instanceof Walls) {
				List<Line2D.Double> points = ((Walls) e).getWalls();
				if (points != null) wallLayout.addAll(points);
			}
		}

		// go through and combine the areas lit at each level
		Area shadow = new Area();
		Area lowLight = new Area();
		Area bright = new Area();

		for (int i = 0; i < m.getSize(); i++) {
			Object e = m.getElementAt(i);
			if (e instanceof LightSource) {
				LightSource l = (LightSource) e;
				if (l.visible.getValue() != Visibility.HIDDEN) {
					bright.add(l.getBrightArea(wallLayout));
					shadow.add(l.getShadowArea(wallLayout));
					if (this.lowLight.getValue()) {
						lowLight.add(l.getLowLightArea(wallLayout));
					}
				}
			}
		}

		if (seen == null || !trackHistory.getValue())
			seen = new Area();

		// remove brighter areas from the dimmer areas so each Area is just the area lit at that level, subtract all from the dark area
		// also update the historically seen area to include the currently visible area
		Area fogOfWar = new Area(seen);	// all areas previous but not currently seen
		if (this.lowLight.getValue()) {
			seen.add(lowLight);
			fogOfWar.subtract(lowLight);
//			dark.subtract(lowLight);
			lowLight.subtract(shadow);
		} else {
			seen.add(shadow);
			fogOfWar.subtract(shadow);
//			dark.subtract(shadow);
		}
		shadow.subtract(bright);
		dark.subtract(seen);	// dark is now the area never seen

		g.setColor(color.getValue());
		Composite c = g.getComposite();
		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha.getValue()));
		g.fill(dark);

		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha.getValue() * 0.5f));
		g.fill(shadow);

		if (this.lowLight.getValue()) {
			fogOfWar.subtract(lowLight);
			g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha.getValue() * 0.75f));
			g.fill(lowLight);
		}

		if (trackHistory.getValue()) {
			g.setColor(historyColor.getValue());
			g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha.getValue() * 0.5f));
			g.fill(fogOfWar);
		}

		g.setComposite(c);
	}

	@Override
	public String toString() {
		return "Darkness ("+getID()+")";
	}

	/**
	 *
	 * @return array of the points defining the cells that have been removed from the mask
	 */
	public Point[] getCells() {
		Point[] ps = new Point[cleared.size()];
		for (int i = 0; i < ps.length; i++) {
			ps[i] = new Point(cleared.get(i));
		}
		return ps;
	}

	public boolean isMasked(Point p) {
		if (cleared.contains(p)) return false;
		return true;
	}

	public void setMasked(Point p, boolean mask) {
		if (mask) {
			cleared.remove(p);
			canvas.repaint();
		} else if (!cleared.contains(p)) {
			cleared.add(p);
			canvas.repaint();
		}
	}

	String serialisePath(PathIterator p) {
		StringBuilder s = new StringBuilder();
		s.append("Path(");
		if (p.getWindingRule() == PathIterator.WIND_EVEN_ODD)
			s.append("EO");
		else if (p.getWindingRule() == PathIterator.WIND_NON_ZERO)
			s.append("NZ");
		else {
			System.err.println("serialisePath: unknown PathIterator winding rule: " + p.getWindingRule());
			s.append("??");
		}

		DecimalFormat fmt = new DecimalFormat();
		fmt.setRoundingMode(RoundingMode.HALF_UP);
		fmt.setMaximumFractionDigits(2);

		while (!p.isDone()) {
			double[] coords = new double[6];
			s.append(" ");
			int type = p.currentSegment(coords);
			if (type == PathIterator.SEG_MOVETO) {
				s.append("M");
				appendCoords(s, coords, 0, fmt);
			} else if (type == PathIterator.SEG_LINETO) {
				s.append("L");
				appendCoords(s, coords, 0, fmt);
			} else if (type == PathIterator.SEG_CUBICTO) {
				s.append("C");
				appendCoords(s, coords, 0, fmt).append(",");
				appendCoords(s, coords, 2, fmt).append(",");
				appendCoords(s, coords, 4, fmt);
			} else if (type == PathIterator.SEG_QUADTO) {
				s.append("Q");
				appendCoords(s, coords, 0, fmt).append(",");
				appendCoords(s, coords, 2, fmt);
			} else if (type == PathIterator.SEG_CLOSE) {
				s.append("X");
			} else {
				System.err.println("serialisePath: unknown PathIterator segment type: " + type);
				s.append("?");
			}
			p.next();
		}
		s.append(")");
		return s.toString();
	}

	StringBuilder appendCoords(StringBuilder s, double[] coords, int idx, DecimalFormat fmt) {
		s.append(fmt.format(coords[idx])).append(",").append(fmt.format(coords[idx + 1]));
		return s;
	}

	Path2D.Double parsePath(String s) {
		if (s == null || s.length() == 0) return null;
		if (!s.startsWith("Path(")) {
			System.err.println("parsePath: not a valid path: '" + s + "'");
			return null;
		}
		String[] pieces = s.substring(5).split(" ");

		Path2D.Double path = new Path2D.Double();

		// first piece is the winding rule
		if (pieces[0].equals("EO"))
			path.setWindingRule(Path2D.WIND_EVEN_ODD);
		else if (pieces[0].equals("NZ"))
			path.setWindingRule(Path2D.WIND_NON_ZERO);
		else {
			System.err.println("parsePath: unknown winding rule: '" + pieces[0] + "'");
			return null;
		}

		double[] coord = new double[2];
		double[] coord2 = new double[2];
		double[] coord3 = new double[2];
		for (int i = 1; i < pieces.length; i++) {
			String piece = pieces[i];
			if (piece.endsWith(")"))
				piece = piece.substring(0, piece.length()-1);
			char cmd = piece.charAt(0);
			String[] coords = piece.substring(1).split(",");

			if (cmd == 'X') {
				path.closePath();
			} else if (cmd == 'M') {
				parseCoord(coords, 0, coord);
				path.moveTo(coord[0], coord[1]);
			} else if (cmd == 'L') {
				parseCoord(coords, 0, coord);
				path.lineTo(coord[0], coord[1]);
			} else if (cmd == 'Q') {
				parseCoord(coords, 0, coord);
				parseCoord(coords, 2, coord2);
				path.quadTo(coord[0], coord[1], coord2[0], coord2[1]);
			} else if (cmd == 'C') {
				parseCoord(coords, 0, coord);
				parseCoord(coords, 2, coord2);
				parseCoord(coords, 4, coord3);
				path.curveTo(coord[0], coord[1], coord2[0], coord2[1], coord3[0], coord3[1]);
			}
		}
		return path;
	}

	void parseCoord(String[] coords, int idx, double[] coord) {
		if (coords.length < idx + 2) {
			System.err.println("parsePath: can't parse coords from: '" + String.join(",", coords) + "'");
			coord[0] = 0;
			coord[1] = 0;
			return;
		}
		try {
			coord[0] = Double.parseDouble(coords[idx]);
		} catch (NumberFormatException e) {
			System.err.println("parsePath: can't parse double from: '" + coords[idx] + "'");
		}
		try {
			coord[1] = Double.parseDouble(coords[idx + 1]);
		} catch (NumberFormatException e) {
			System.err.println("parsePath: can't parse double from: '" + coords[idx + 1] + "'");
		}
	}

	@Override
	public void setProperty(String property, Object value) {
		if (property.equals(PROPERTY_MASKCELL)) {
			setMasked((Point)value, true);
		} else if (property.equals(PROPERTY_UNMASKCELL)) {
			setMasked((Point)value, false);
		} else if (property.equals(PROPERTY_HISTORY)) {
			Path2D.Double p = parsePath(value.toString());
			if (p == null)
				seen = new Area();
			else
				seen = new Area(p);
			canvas.repaint();
		} else {
			super.setProperty(property, value);
		}
	}

	@Override
	public Object getProperty(String property) {
		if (property.equals(PROPERTY_HISTORY)) {
			if (seen == null || seen.isEmpty())
				return "";
			return serialisePath(seen.getPathIterator(null));
		}
		return super.getProperty(property);
	}
}
