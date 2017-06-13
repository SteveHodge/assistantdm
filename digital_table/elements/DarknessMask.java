package digital_table.elements;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Area;
import java.awt.geom.Line2D;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ListModel;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import digital_table.server.MapCanvas.Order;

public class DarknessMask extends MapElement {
	private static final long serialVersionUID = 1L;

	public final static String PROPERTY_COLOR = "color";	// Color
	public final static String PROPERTY_ALPHA = "alpha";	// float
	public final static String PROPERTY_LOW_LIGHT = "low_light";	// boolean
	public final static String PROPERTY_MASKCELL = "mask";	// Point - when this property is set the specified point will be masked
	public final static String PROPERTY_UNMASKCELL = "unmask";	// Point - when this property is set the specified point will be cleared
	public final static String PROPERTY_SHOW_WALLS = "show_walls";	// boolean

	Property<Color> color = new Property<Color>(PROPERTY_COLOR, Color.BLACK, Color.class);
	Property<Float> alpha = new Property<Float>(PROPERTY_ALPHA, 1.0f, Float.class);
	Property<Boolean> lowLight = new Property<Boolean>(PROPERTY_LOW_LIGHT, false, Boolean.class);
	Property<Boolean> showWalls = new Property<Boolean>(PROPERTY_SHOW_WALLS, false, Boolean.class);

	List<Point> cleared = new ArrayList<>();

	class WallLayout {
		List<Line2D.Double> walls = new ArrayList<Line2D.Double>();
	}

	transient WallLayout wallLayout = null;

	void initializeWallLayout() {
		wallLayout = new WallLayout();
//		wallLayout.walls.add(new Line2D.Double(6.0d, 3.0d, 6.0d, 7.0d));
//		wallLayout.walls.add(new Line2D.Double(19.0d, 25.0d, 33.0d, 25.0d));
//		wallLayout.walls.add(new Line2D.Double(10.0d, 7.0d, 13.0d, 8.5d));
//		wallLayout.walls.add(new Line2D.Double(13.0d, 8.5d, 13.0d, 10d));
//		wallLayout.walls.add(new Line2D.Double(13.0d, 10d, 8.0d, 10d));

		try {
			DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document document = builder.parse("D:\\DnDBooks\\_Campaigns\\Ptolus Madness Rising\\Ratling Nest\\ShadowmaskLinear.svg");
			// expecting svg->g->path, but we'll just get all the paths
			NodeList paths = document.getElementsByTagName("path");
			for (int i = 0; i < paths.getLength(); i++) {
				Element p = (Element) paths.item(i);
				String d = p.getAttribute("d");
				String[] parts = d.split(" ");
				double x1 = 0, y1 = 0;
				System.out.println("Path has " + parts.length / 2 + " segments");
				for (int j = 0; j < parts.length;) {
					String op = parts[j++];
					String coords = parts[j++];
					double x = Double.parseDouble(coords.substring(0, coords.indexOf(','))) / 160d;
					double y = Double.parseDouble(coords.substring(coords.indexOf(',') + 1)) / 160d;
					if (op.equals("L")) {
						wallLayout.walls.add(new Line2D.Double(x1, y1, x, y));
					} else if (!op.equals("M")) {
						System.err.println("Unknown path operator: " + op);
					}
					x1 = x;
					y1 = y;
				}
			}
			System.out.println("Wall segments: " + wallLayout.walls.size());

		} catch (ParserConfigurationException | SAXException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public Order getDefaultOrder() {
		return Order.ABOVEGRID;
	}

	@Override
	public void paint(Graphics2D g) {
		if (canvas == null || getVisibility() == Visibility.HIDDEN) return;

		// build the shape
		Area dark = new Area(g.getClip());
		Area shadow = new Area();
		Area lowLight = new Area();

		// using indexed loop instead of iterator to avoid concurrency issues
		for (int i = 0; i < cleared.size(); i++) {
			Point p = cleared.get(i);
			Point tl = canvas.convertCanvasCoordsToDisplay(p.x, p.y);
			Point br = canvas.convertCanvasCoordsToDisplay(p.x+1, p.y+1);
			dark.subtract(new Area(new Rectangle(tl.x, tl.y, br.x - tl.x, br.y - tl.y)));
		}

		ListModel<MapElement> m = canvas.getModel();
		// go through and remove any illuminated areas from the mask
		// also create a mask of all shadowed area
		for (int i = 0; i < m.getSize(); i++) {
			Object e = m.getElementAt(i);
			if (e instanceof LightSource) {
				LightSource l = (LightSource) e;
				if (l.visible.getValue() != Visibility.HIDDEN) {
					dark.subtract(l.getBrightArea(wallLayout));
					dark.subtract(l.getShadowArea(wallLayout));
					shadow.add(l.getShadowArea(wallLayout));
					if (this.lowLight.getValue()) {
						dark.subtract(l.getLowLightArea(wallLayout));
						lowLight.add(l.getLowLightArea(wallLayout));
					}
				}
			}
		}
		// remove brightly lit area from the shadow mask
		for (int i = 0; i < m.getSize(); i++) {
			Object e = m.getElementAt(i);
			if (e instanceof LightSource) {
				LightSource l = (LightSource) e;
				if (l.visible.getValue() != Visibility.HIDDEN) {
					shadow.subtract(l.getBrightArea(wallLayout));
					if (this.lowLight.getValue()) {
						lowLight.subtract(l.getBrightArea(wallLayout));
						lowLight.subtract(l.getShadowArea(wallLayout));
					}
				}
			}
		}

		g.setColor(color.getValue());
		Composite c = g.getComposite();
		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha.getValue()));
		g.fill(dark);

		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha.getValue() * 0.5f));
		g.fill(shadow);

		if (this.lowLight.getValue()) {
			g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha.getValue() * 0.75f));
			g.fill(lowLight);
		}

		g.setComposite(c);

		if (wallLayout == null) {
			initializeWallLayout();
		}
		if (showWalls.getValue()) {
//			long startTime = System.nanoTime();
			g.setColor(Color.RED);
			if (wallLayout != null && wallLayout.walls != null) {
				for (Line2D.Double l : wallLayout.walls) {
					Point p1 = canvas.convertCanvasCoordsToDisplay(l.getP1());
					Point p2 = canvas.convertCanvasCoordsToDisplay(l.getP2());
					g.drawLine(p1.x, p1.y, p2.x, p2.y);
				}
			}
//			double millis = (System.nanoTime() - startTime) / 1000000d;
//			//logger.info("Painting complete for " + this + " in " + micros + "ms");
//			System.out.printf("Wall painting took %.3fms\n", millis);
		}
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

	@Override
	public void setProperty(String property, Object value) {
		if (property.equals(PROPERTY_MASKCELL)) {
			setMasked((Point)value, true);
		} else if (property.equals(PROPERTY_UNMASKCELL)) {
			setMasked((Point)value, false);
		} else {
			super.setProperty(property, value);
		}
	}
}
