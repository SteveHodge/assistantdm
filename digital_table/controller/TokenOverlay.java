package digital_table.controller;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.imageio.ImageIO;

import util.Updater;
import digital_table.elements.Group;
import digital_table.elements.MapElement;
import digital_table.elements.Token;
import digital_table.server.MapCanvas;
import digital_table.server.MapCanvas.Order;

/**
 * Generates overlay images to be layered on top of the calibrated camera image to highlight the tokens. Token elements
 * added to this display are replaced with a custom MapElement.
 * 
 * @author Steve
 * 
 */

class TokenOverlay {
	static final String PROPERTY_WEB_LABEL = "web_label";

	private int rows = 39;
	private int columns = 32;

	private CameraOverlayCanvas canvas;
	private Map<Integer, MapElement> elements = new HashMap<Integer, MapElement>();

	private class CameraOverlayCanvas extends MapCanvas {
		int width, height;

		@Override
		public int getColumnWidth() {
			//return getWidth() / columns;
			return height / columns;
		}

		@Override
		public int getRowHeight() {
			//return getHeight() / rows;
			return width / rows;
		}

		// get the grid coordinates of the grid cell containing (x,y)
		@Override
		public Point getGridCellCoordinates(int x, int y) {
			int col = x * columns / height;
			int row = y * rows / width;
			return new Point(col, row);
		}

		// get the pixel location of the top left corner of the grid cell at (col, row)
		// if p is not null the the location is stored in p and returned
		@Override
		public Point getDisplayCoordinates(int col, int row, Point p) {
			if (p == null) p = new Point();
			p.x = col * height / columns;
			p.y = row * width / rows;
			return p;
		}

		@Override
		public Point getDisplayCoordinates(int col, int row) {
			return getDisplayCoordinates(col, row, null);
		}

		@Override
		public Point2D getGridCoordinates(int x, int y) {
			return new Point2D.Double((double) x * columns / height, (double) y * rows / width);
		}

		@Override
		public Point getDisplayCoordinates(Point2D p) {
			Point p2 = new Point();
			p2.x = (int) (p.getX() * height / columns);
			p2.y = (int) (p.getY() * width / rows);
			return p2;
		}
	};

	private class MaskToken extends Group {
		private static final long serialVersionUID = 1L;

//		public final static String PROPERTY_X = "x";	// int
//		public final static String PROPERTY_Y = "y";	// int
//		public final static String PROPERTY_COLOR = "color";	// Color - currently unimplemented
//		public final static String PROPERTY_LABEL = "label";	// String
//		public final static String PROPERTY_SPACE = "space";	// int - in 1/2 foot units

//		private Property<Color> color = new Property<Color>(Token.PROPERTY_COLOR, Color.WHITE, Color.class);
		private Property<Integer> space = new Property<Integer>(Token.PROPERTY_SPACE, 10, Integer.class);

		private String webLabel = null;
		private boolean hasWebLabel = false;

		public MaskToken(int id) {
			super(id);
		}

		@Override
		public Order getDefaultOrder() {
			return Order.ABOVEGRID;
		}

		private int getX() {
			return (int) location.getValue().getX();
		}

		private int getY() {
			return (int) location.getValue().getY();
		}

		private void setX(int x) {
			Point p = new Point(x, getY());
			location.setValue(p);
		}

		private void setY(int y) {
			Point p = new Point(getX(), y);
			location.setValue(p);
		}

		@Override
		public void paint(Graphics2D g, Point2D offset) {
			if (canvas == null || !isVisible()) return;
			Point2D o = canvas.getDisplayCoordinates((int) offset.getX(), (int) offset.getY());
			g.translate(o.getX(), o.getY());

			int space = this.space.getValue();
			if (space < 10) space = 10;	// TODO need to be able to draw sub-Small tokens slightly smaller

			float arcWidth = canvas.getColumnWidth() * space / 30;
			float arcHeight = canvas.getRowHeight() * space / 30;
			int cells = space / 10;
			Point tl = canvas.getDisplayCoordinates(getX(), getY());
			Point br = canvas.getDisplayCoordinates(getX() + cells, getY() + cells);
			BasicStroke stroke = getThickStroke();
			float inset = stroke.getLineWidth() / 2;

			// paint background:
			Shape s = new RoundRectangle2D.Float(tl.x + inset, tl.y + inset, br.x - tl.x - inset * 2, br.y - tl.y - inset * 2, arcWidth, arcHeight);
			g.setColor(Color.WHITE);
			g.fill(s);

			// paint border:
			g.setColor(Color.BLACK);
			Stroke oldStroke = g.getStroke();
			g.setStroke(getThickStroke());
			g.draw(s);
			g.setStroke(oldStroke);

			Shape oldClip = g.getClip();
			Shape clip = new RoundRectangle2D.Float(tl.x + stroke.getLineWidth(), tl.y + stroke.getLineWidth(), br.x - tl.x - stroke.getLineWidth() * 2, br.y - tl.y - stroke.getLineWidth() * 2,
					arcWidth,
					arcHeight);
			g.setClip(clip);
			Rectangle2D clipBounds = clip.getBounds2D();

			if (webLabel != null && webLabel.length() > 0) {
				Font f = g.getFont();
				float newSize = (float) clipBounds.getHeight();
				if (newSize < 8.0f) newSize = 8.0f;
				g.setFont(f.deriveFont(newSize));
				FontMetrics metrics = g.getFontMetrics();
				Rectangle2D bounds = metrics.getStringBounds(webLabel, g);
				double xPos = clipBounds.getCenterX() - bounds.getWidth() / 2;
				if (xPos < clipBounds.getX()) xPos = clipBounds.getX();
				double yPos = clipBounds.getMaxY() - metrics.getDescent() / 2;

				AffineTransform t = g.getTransform();
				g.rotate(Math.toRadians(90), clipBounds.getCenterX(), clipBounds.getCenterY());
				g.drawString(webLabel, (float) xPos, (float) yPos);
				g.setTransform(t);
			}

			g.setClip(oldClip);

			g.translate(-o.getX(), -o.getY());
		}

		private BasicStroke getThickStroke() {
			if (canvas.getColumnWidth() < 40) return new BasicStroke(4);
			return new BasicStroke(6);
		}

		@Override
		public String toString() {
			if (label == null || label.getValue().length() == 0) return "Token (" + getID() + ")";
			return "Token (" + label + ")";
		}

		@Override
		public Object getProperty(String property) {
			if (property.equals(Token.PROPERTY_X)) {
				return getX();
			} else if (property.equals(Token.PROPERTY_Y)) {
				return getY();
			} else {
				return super.getProperty(property);
			}
		}

		@Override
		public void setProperty(String property, Object value) {
			if (property.equals(Token.PROPERTY_X)) {
				setX((Integer) value);
			} else if (property.equals(Token.PROPERTY_Y)) {
				setY((Integer) value);
			} else if (property.equals(PROPERTY_WEB_LABEL)) {
				if (value != null && ((String) value).length() > 0) {
					hasWebLabel = true;
					webLabel = (String) value;
				} else {
					hasWebLabel = false;
				}
			} else if (property.equals(Token.PROPERTY_LABEL)
					|| property.equals(Token.PROPERTY_VISIBLE)
					|| property.equals(Token.PROPERTY_LOCATION)
					|| property.equals(Token.PROPERTY_SPACE)) {
				super.setProperty(property, value);
			}
		}
	}


	TokenOverlay() {
		canvas = new CameraOverlayCanvas();
	}

	void updateOverlay(int width, int height) {
		SortedMap<String, String> descriptions = new TreeMap<String, String>();
		try {
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			ImageIO.write(getImage(width, height, descriptions), "png", stream);
			StringBuilder output = new StringBuilder();
			for (String k : descriptions.keySet()) {
				output.append(k).append("\n");
				output.append(descriptions.get(k)).append("\n");
			}
			Updater.update("http://armitage/assistantdm/upload.php/tokens1.png", stream.toByteArray());
			Updater.update("http://armitage/assistantdm/upload.php/tokens1.txt", output.toString().getBytes());

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	BufferedImage getImage(int width, int height, SortedMap<String, String> descriptions) {
		assignLabels(descriptions);
		BufferedImage output = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D imgG = output.createGraphics();
		imgG.setBackground(new Color(255, 255, 255, 0));
		imgG.setClip(0, 0, output.getWidth(), output.getHeight());
		imgG.rotate(Math.toRadians(-90), output.getWidth() / 2, output.getHeight() / 2);
		imgG.translate((output.getWidth() - height) / 2, canvas.getRowHeight() + (output.getHeight() - width) / 2);
		canvas.width = width;
		canvas.height = height;
		canvas.paint(imgG);
		return output;
	}

	void addElement(MapElement e, MapElement parent) {
		if (parent != null) parent = elements.get(parent.getID());

		if (e instanceof Token) {
			MaskToken t = new MaskToken(e.getID());
			canvas.addElement(t, parent);
			elements.put(t.getID(), t);

		} else {
			// we serialize the element to a byte array and then deserialize it to a new MapElement
			// this produces a private copy of the element in it's current state
			try {
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				ObjectOutputStream os = new ObjectOutputStream(bos);
				os.writeObject(e);
				os.close();
				ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
				ObjectInputStream is = new ObjectInputStream(bis);
				MapElement copy = (MapElement) is.readObject();
				is.close();

				copy.setProperty(MapElement.PROPERTY_VISIBLE, false);

				canvas.addElement(copy, parent);
				elements.put(copy.getID(), copy);
			} catch (IOException e1) {
				e1.printStackTrace();
			} catch (ClassNotFoundException e1) {
				e1.printStackTrace();
			}
		}
	}

	void removeElement(int id) {
		canvas.removeElement(id);
	}

	void promoteElement(MapElement e) {
		e = elements.get(e.getID());
		canvas.promoteElement(e);
	}

	void demoteElement(MapElement e) {
		e = elements.get(e.getID());
		canvas.demoteElement(e);
	}

	void setProperty(int id, String property, Object value) {
		MapElement e = elements.get(id);
		if (e != null && e instanceof MaskToken) {
			e.setProperty(property, value);
		}
	}

	// Assigns labels to all visible tokens that don't have a web_label set either by the user or previously by the system.
	// hidden tokens have system assigned labels removed. Also generates a key. If a web_label is used for more than one
	// token then the description used in the key will be taken from one of the associated tokens randomly.
	private SortedMap<String, String> assignLabels(SortedMap<String, String> descriptions) {
		descriptions.clear();

		// gather any used labels
		for (int id : elements.keySet()) {
			MapElement e = elements.get(id);
			if (e instanceof MaskToken) {
				MaskToken t = (MaskToken) e;
				if (t.hasWebLabel || ((Boolean) t.getProperty(MapElement.PROPERTY_VISIBLE) && t.webLabel != null)) {
					descriptions.put(t.webLabel, (String) t.getProperty(Token.PROPERTY_LABEL));
				} else {
					t.webLabel = null;
				}
			}
		}

		// assign labels to visible tokens without labels
		int nextLabel = 0;
		for (int id : elements.keySet()) {
			MapElement e = elements.get(id);
			if (e instanceof MaskToken) {
				MaskToken t = (MaskToken) e;
				if ((Boolean) t.getProperty(MapElement.PROPERTY_VISIBLE) && t.webLabel == null) {
					// find the next available label
					do {
						nextLabel++;
					} while (descriptions.containsKey(Integer.toString(nextLabel)));

					t.webLabel = Integer.toString(nextLabel);
					descriptions.put(t.webLabel, (String) t.getProperty(Token.PROPERTY_LABEL));
				}
			}
		}

		return descriptions;
	}
}
