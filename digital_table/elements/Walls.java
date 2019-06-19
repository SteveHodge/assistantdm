package digital_table.elements;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import digital_table.server.MapCanvas.Order;

public class Walls extends MapElement {
	private static final long serialVersionUID = 1L;

	public final static String PROPERTY_LABEL = "label";	// String
	public final static String PROPERTY_WALL_LAYOUT = "wall_layout";	// WallLayout
	public final static String PROPERTY_SHOW_WALLS = "draw_walls";	// boolean - draw walls

	Property<String> label = new Property<String>(PROPERTY_LABEL, "", String.class);
	private Property<Boolean> showWalls = new Property<Boolean>(PROPERTY_SHOW_WALLS, false, Boolean.class);

	public static class WallLayout implements Serializable {
		private static final long serialVersionUID = 1L;
		List<Point2D.Double[]> walls = new ArrayList<>();
		double width, height;

		public static WallLayout parseXML(String xml) {
			WallLayout wallLayout = new WallLayout();

			try {
				long startTime = System.nanoTime();

				DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
				factory.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);	// disabling these features improves parsing time by at least 3 orders of magnitude
				factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
				DocumentBuilder builder = factory.newDocumentBuilder();
				InputStream is = new ByteArrayInputStream(xml.getBytes());
				Document document = builder.parse(is);
				NodeList svgList = document.getElementsByTagName("walls");

				Element root = (Element) svgList.item(0);
				System.out.println("  Format = walls xml");
				wallLayout.width = Integer.parseInt(root.getAttribute("width"));
				wallLayout.height = Integer.parseInt(root.getAttribute("height"));

//				double millis = (System.nanoTime() - startTime) / 1000000d;
//				System.out.printf("Loading DOM took %.3fms\n", millis);
//				startTime = System.nanoTime();

				NodeList walls = document.getElementsByTagName("wall");
				for (int i = 0; i < walls.getLength(); i++) {
					Element wall = (Element) walls.item(i);
					String wallPoints = wall.getAttribute("points");
					double x1 = 0, y1 = 0;
//					System.out.println("Path has " + parts.length / 2 + " segments");
					List<Point2D.Double> points = new ArrayList<>();
					for (String point : wallPoints.split(" ")) {
						if (point.indexOf(',') == -1) {
							System.err.println("Error parsing walls: no comma in '" + point + "' path #" + i + ", part #" + points.size());
						}
						double x = Double.parseDouble(point.substring(0, point.indexOf(',')));
						double y = Double.parseDouble(point.substring(point.indexOf(',') + 1));
						points.add(new Point2D.Double(x + x1, y + y1));
						x1 += x;
						y1 += y;
					}
					wallLayout.walls.add(points.toArray(new Point2D.Double[0]));
				}
				double millis = (System.nanoTime() - startTime) / 1000000d;
				System.out.printf("Wall segment generation took %.3fms\n", millis);
				System.out.println("Wall segments: " + wallLayout.walls.size());

			} catch (ParserConfigurationException | SAXException | IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return wallLayout;
		}
	}

	private transient WallLayout wallLayout = null;
	private transient List<Line2D.Double> walls = null;

	public List<Line2D.Double> getWalls() {
		if (visible.getValue() == Visibility.VISIBLE)
			return walls;
		else
			return new ArrayList<Line2D.Double>();
	}

	private List<Line2D.Double> getTransformed() {
		if (wallLayout == null) return null;
		List<Line2D.Double> transformed = new ArrayList<>();
		AffineTransform t = getTransform(wallLayout.width, wallLayout.height);
		for (Point2D.Double[] wall : wallLayout.walls) {
			Point2D.Double[] newpts = new Point2D.Double[wall.length];
			t.transform(wall, 0, newpts, 0, wall.length);
			for (int i = 1; i < wall.length; i++) {
				transformed.add(new Line2D.Double(newpts[i - 1], newpts[i]));
			}
		}
		return transformed;
	}

	// returns the AffineTransform that would transform an image of the specified width and height to the
	// dimensions of this element. the AffineTransform includes and rotations and mirroring set on this element
	AffineTransform getTransform(double srcWidth, double srcHeight) {
		if (parent == null || !(parent instanceof MapImage))
			return null;
		AffineTransform transform;
		MapImage img = (MapImage) parent;
		double x = img.getX();
		double y = img.getY();
		double width = img.width.getValue();
		double height = img.height.getValue();
		int rotations = img.rotations.getValue();
		boolean mirrored = img.mirrored.getValue();
//		System.out.printf("Walls from image: (%.1f, %.1f) - (%.1f, %.1f) rot = %d, mirror = %b\n", x, y, width, height, rotations, mirrored);

		if (mirrored) {
			transform = AffineTransform.getScaleInstance(-1, 1);
			transform.quadrantRotate(-rotations);
			if (rotations == 0)
				transform.translate(-x - width, y);
			else if (rotations == 1)
				transform.translate(-y - height, -x - width);
			else if (rotations == 2)
				transform.translate(x, -y - height);
			else if (rotations == 3)
				transform.translate(y, x);
		} else {
			transform = AffineTransform.getQuadrantRotateInstance(rotations);
			if (rotations == 0) transform.translate(x, y);
			if (rotations == 1) transform.translate(y, -x - width);
			if (rotations == 2) transform.translate(-x - width, -y - height);
			if (rotations == 3) transform.translate(-y - height, x);
		}
		if (rotations % 2 == 0) {
			transform.scale(width / srcWidth, height / srcHeight);
		} else {
			transform.scale(height / srcWidth, width / srcHeight);
		}
		return transform;
	}

	void imageUpdated() {
		walls = getTransformed();
	}

	@Override
	public Order getDefaultOrder() {
		return Order.ABOVEGRID;
	}

	@Override
	public void paint(Graphics2D g) {
		if (canvas == null || walls == null || !showWalls.getValue()) return;

		Stroke s = g.getStroke();
		g.setStroke(new BasicStroke(3.0f));
		g.setColor(selected ? Color.RED : Color.BLUE);

//			long startTime = System.nanoTime();
		for (Line2D.Double l : walls) {
			Point p1 = canvas.convertCanvasCoordsToDisplay(l.getP1());
			Point p2 = canvas.convertCanvasCoordsToDisplay(l.getP2());
			g.drawLine(p1.x, p1.y, p2.x, p2.y);
		}

		g.setStroke(s);
	}

	@Override
	public String getIDString() {
		return "Walls" + (label == null || label.getValue().length() == 0 ? "" : " (" + label + ")");
	}

	@Override
	public String toString() {
		String text = label.getValue();
		if (text == null || text.equals("")) text += getID();
		return "Walls (" + text + ")";
	}

	@Override
	public void setProperty(String property, Object value) {
		if (property.equals(PROPERTY_WALL_LAYOUT)) {
			wallLayout = (WallLayout) value;
			walls = getTransformed();
			canvas.repaint();

		} else {
			super.setProperty(property, value);
		}
	}
}
