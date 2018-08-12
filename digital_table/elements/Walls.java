package digital_table.elements;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
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

// FIXME processing the walls happens for each display - it would be better to parse once and pass the resulting WallLayout to each display

public class Walls extends MapElement {
	private static final long serialVersionUID = 1L;

	public final static String PROPERTY_COLOR = "color";	// Color
	public final static String PROPERTY_ALPHA = "alpha";	// float
	public final static String PROPERTY_WALL_LAYOUT = "wall_layout";	// WallLayout
	public final static String PROPERTY_X = "x";	// double
	public final static String PROPERTY_Y = "y";	// double
	public final static String PROPERTY_ROTATIONS = "rotations";	// int - number of quadrants rotated clockwise
	public final static String PROPERTY_MIRRORED = "mirrored";	// boolean - image is flipped horizontally
	public final static String PROPERTY_WIDTH = "width";	// double
	public final static String PROPERTY_HEIGHT = "height";	// double

	Property<Color> color = new Property<Color>(PROPERTY_COLOR, Color.BLACK, Color.class);
	Property<Float> alpha = new Property<Float>(PROPERTY_ALPHA, 1.0f, Float.class);
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

	List<Point> cleared = new ArrayList<>();

	public static class WallLayout implements Serializable {
		private static final long serialVersionUID = 1L;
		List<Line2D.Double> walls = new ArrayList<Line2D.Double>();
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
				// expecting svg->g->path, but we'll just get all the paths
				NodeList svgList = document.getElementsByTagName("svg");
				boolean svg = svgList.getLength() > 0;
				if (!svg) {
					svgList = document.getElementsByTagName("walls");
				}

				Element root = (Element) svgList.item(0);
				if (svg) {
					System.out.println("  Format = svg");
					String viewBox[] = root.getAttribute("viewBox").split(" ");
					wallLayout.width = Double.parseDouble(viewBox[2]);
					wallLayout.height = Double.parseDouble(viewBox[3]);
				} else {
					System.out.println("  Format = walls xml");
					wallLayout.width = Integer.parseInt(root.getAttribute("width"));
					wallLayout.height = Integer.parseInt(root.getAttribute("height"));
				}

//				double millis = (System.nanoTime() - startTime) / 1000000d;
//				System.out.printf("Loading DOM took %.3fms\n", millis);
//				startTime = System.nanoTime();

				NodeList paths;
				if (svg)
					paths = document.getElementsByTagName("path");
				else
					paths = document.getElementsByTagName("wall");
				for (int i = 0; i < paths.getLength(); i++) {
					Element p = (Element) paths.item(i);
					String d = p.getAttribute("d");
					String[] parts = d.split(" ");
					double x1 = 0, y1 = 0;
//					System.out.println("Path has " + parts.length / 2 + " segments");
					for (int j = 0; j < parts.length;) {
						String op = parts[j++];
						String coords = parts[j++];
						if (coords.indexOf(',') == -1) {
							System.err.println("Error parsing walls: no comma in '" + coords + "' path #" + i + ", part #" + (j - 1) + ", op = " + op);
						}
						double x = Double.parseDouble(coords.substring(0, coords.indexOf(',')));
						double y = Double.parseDouble(coords.substring(coords.indexOf(',') + 1));
						if (op.equals("L")) {
							wallLayout.walls.add(new Line2D.Double(x1, y1, x, y));
						} else if (!op.equals("M")) {
							System.err.println("Unknown path operator: " + op);
						}
						x1 = x;
						y1 = y;
					}
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

	transient WallLayout wallLayout = null;
	transient List<Line2D.Double> walls = null;

	// TODO should be able to do this with an affine transformation
	private List<Line2D.Double> getTransformed() {
		if (wallLayout == null) return null;
		List<Line2D.Double> transformed = new ArrayList<>();
		AffineTransform t = getTransform(wallLayout.width, wallLayout.height);
		for (Line2D.Double line : wallLayout.walls) {
			Point2D.Double a = new Point2D.Double(line.x1, line.y1);
			t.transform(a, a);
			Point2D.Double b = new Point2D.Double(line.x2, line.y2);
			t.transform(b, b);
			transformed.add(new Line2D.Double(a, b));
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

	void setWallLayout(String xml) {
		if (width.getValue() == 0 || height.getValue() == 0) {
			System.out.println("Wall layout has no width or height");
		}
		System.out.println("Reading wall layout");
		System.out.println("  Width = " + width.getValue());
		System.out.println("  Height = " + height.getValue());
		System.out.println("  X = " + x.getValue());
		System.out.println("  Y = " + y.getValue());
		System.out.println("  Rotations = " + rotations.getValue());
		System.out.println("  Mirrored = " + mirrored.getValue());
		wallLayout = WallLayout.parseXML(xml);
		walls = getTransformed();
		canvas.repaint();
	}

	@Override
	public Order getDefaultOrder() {
		return Order.ABOVEGRID;
	}

	@Override
	public void paint(Graphics2D g) {
		if (canvas == null || getVisibility() != Visibility.VISIBLE) return;
		if (walls == null) return;

		g.setColor(color.getValue());
		Composite c = g.getComposite();
		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha.getValue()));

//			long startTime = System.nanoTime();
		for (Line2D.Double l : walls) {
			Point p1 = canvas.convertCanvasCoordsToDisplay(l.getP1());
			Point p2 = canvas.convertCanvasCoordsToDisplay(l.getP2());
			g.drawLine(p1.x, p1.y, p2.x, p2.y);
		}
//			double millis = (System.nanoTime() - startTime) / 1000000d;
//			//logger.info("Painting complete for " + this + " in " + micros + "ms");
//			System.out.printf("Wall painting took %.3fms\n", millis);


		// highlight border for selected element
		if (selected) {
			Point offset = canvas.convertCanvasCoordsToDisplay(new Point2D.Double(x.getValue(), y.getValue()));
			Dimension size = canvas.getDisplayDimension(width.getValue(), height.getValue());
			g.setColor(Color.BLUE);
			g.drawRect(offset.x, offset.y, size.width, size.height);
			g.drawRect(offset.x + 1, offset.y + 1, size.width - 2, size.height - 2);
		}

		g.setComposite(c);
	}

	@Override
	public String toString() {
		return "Walls (" + getID() + ")";
	}

	@Override
	public void setProperty(String property, Object value) {
		if (property.equals(PROPERTY_WALL_LAYOUT)) {
			setWallLayout((String) value);
		} else {
			super.setProperty(property, value);
			if (property.equals(PROPERTY_X)
					|| property.equals(PROPERTY_Y)
					|| property.equals(PROPERTY_ROTATIONS)
					|| property.equals(PROPERTY_MIRRORED)
					|| property.equals(PROPERTY_WIDTH)
					|| property.equals(PROPERTY_HEIGHT)) {
				walls = getTransformed();
				canvas.repaint();
			}
		}
	}
}
