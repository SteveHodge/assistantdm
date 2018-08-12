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

	public final static String PROPERTY_WALL_LAYOUT = "wall_layout";	// WallLayout
	public final static String PROPERTY_X = "x";	// double
	public final static String PROPERTY_Y = "y";	// double
	public final static String PROPERTY_ROTATIONS = "rotations";	// int - number of quadrants rotated clockwise
	public final static String PROPERTY_MIRRORED = "mirrored";	// boolean - image is flipped horizontally
	public final static String PROPERTY_WIDTH = "width";	// double
	public final static String PROPERTY_HEIGHT = "height";	// double
	public final static String PROPERTY_SHOW_WALLS = "draw_walls";	// boolean - draw walls

	Property<Double> width = new Property<Double>(PROPERTY_WIDTH, 0d, Double.class);
	Property<Double> height = new Property<Double>(PROPERTY_HEIGHT, 0d, Double.class);
	Property<Double> x = new Property<Double>(PROPERTY_X, 0d, Double.class);
	Property<Double> y = new Property<Double>(PROPERTY_Y, 0d, Double.class);

	Property<Integer> rotations = new Property<Integer>(PROPERTY_ROTATIONS, 0, Integer.class) {
		private static final long serialVersionUID = 1L;

		@Override
		public void setValue(Integer r) {
			r = r % 4;
			int oldR = rotations.getValue();
			if (oldR == r) return;
			super.setValue(r);

			if ((r + oldR) % 2 == 1) {
				// change is an odd number of quadrants so we need to swap width and height
				double w = width.getValue();
				width.setValue(height.getValue());
				height.setValue(w);
			}
		}
	};
	private Property<Boolean> mirrored = new Property<Boolean>(PROPERTY_MIRRORED, false, Boolean.class);
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
		AffineTransform transform;
		if (mirrored.getValue()) {
			transform = AffineTransform.getScaleInstance(-1, 1);
			transform.quadrantRotate(-rotations.getValue());
			if (rotations.getValue() == 0)
				transform.translate(-x.getValue() - width.getValue(), y.getValue());
			else if (rotations.getValue() == 1)
				transform.translate(-y.getValue() - height.getValue(), -x.getValue() - width.getValue());
			else if (rotations.getValue() == 2)
				transform.translate(x.getValue(), -y.getValue() - height.getValue());
			else if (rotations.getValue() == 3)
				transform.translate(y.getValue(), x.getValue());
		} else {
			transform = AffineTransform.getQuadrantRotateInstance(rotations.getValue());
			if (rotations.getValue() == 0) transform.translate(x.getValue(), y.getValue());
			if (rotations.getValue() == 1) transform.translate(y.getValue(), -x.getValue() - width.getValue());
			if (rotations.getValue() == 2) transform.translate(-x.getValue() - width.getValue(), -y.getValue() - height.getValue());
			if (rotations.getValue() == 3) transform.translate(-y.getValue() - height.getValue(), x.getValue());
		}
		if (rotations.getValue() % 2 == 0) {
			transform.scale(width.getValue() / srcWidth, height.getValue() / srcHeight);
		} else {
			transform.scale(height.getValue() / srcWidth, width.getValue() / srcHeight);
		}
		return transform;
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
//			double millis = (System.nanoTime() - startTime) / 1000000d;
//			//logger.info("Painting complete for " + this + " in " + micros + "ms");
//			System.out.printf("Wall painting took %.3fms\n", millis);

		g.setStroke(s);
	}

	@Override
	public String toString() {
		return "Walls (" + getID() + ")";
	}

	@Override
	public void setProperty(String property, Object value) {
		if (property.equals(PROPERTY_WALL_LAYOUT)) {
			wallLayout = (WallLayout) value;
			walls = getTransformed();
			canvas.repaint();

		} else {
			super.setProperty(property, value);
			if (property.equals(PROPERTY_X)
					|| property.equals(PROPERTY_Y)
					|| property.equals(PROPERTY_ROTATIONS)
					|| property.equals(PROPERTY_MIRRORED)
					|| property.equals(PROPERTY_WIDTH)
					|| property.equals(PROPERTY_HEIGHT)) {
				walls = getTransformed();
			}
		}
	}
}
