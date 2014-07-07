package digital_table.controller;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
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
import javax.swing.JPanel;

import util.Updater;
import digital_table.elements.Group;
import digital_table.elements.MapElement;
import digital_table.elements.MapElement.Visibility;
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

	final int rows = 39;
	final int columns = 32;

	private CameraOverlayCanvas canvas;
	private Map<Integer, MapElement> elements = new HashMap<>();

	private class CameraOverlayCanvas extends MapCanvas {
		int width, height;

		@Override
		protected int getResolutionNumeratorX() {
			return height;
		}

		@Override
		protected int getResolutionDenominatorX() {
			return columns;
		}

		@Override
		protected int getResolutionNumeratorY() {
			return width;
		}

		@Override
		protected int getResolutionDenominatorY() {
			return rows;
		}
	};

	private class MaskToken extends Group {
		private static final long serialVersionUID = 1L;

//		public final static String PROPERTY_X = "x";	// int
//		public final static String PROPERTY_Y = "y";	// int
//		public final static String PROPERTY_COLOR = "color";	// Color - currently unimplemented
//		public final static String PROPERTY_LABEL = "label";	// String
//		public final static String PROPERTY_SPACE = "space";	// int - in 1/2 foot units

//		private Property<Color> color = new Property<>(Token.PROPERTY_COLOR, Color.WHITE, Color.class);
		private Property<Integer> space = new Property<>(Token.PROPERTY_SPACE, 10, Integer.class);

		private String webLabel = null;
		private boolean hasWebLabel = false;

		public MaskToken(int id) {
			super(id);
		}

		@Override
		public Order getDefaultOrder() {
			return Order.ABOVEGRID;
		}

		@Override
		public void paint(Graphics2D g) {
			if (canvas == null || getVisibility() == Visibility.HIDDEN) return;

			Point2D o = canvas.convertGridCoordsToDisplay(canvas.getElementOrigin(this));
			g.translate(o.getX(), o.getY());

			int space = this.space.getValue();
			if (space < 10) space = 10;	// TODO need to be able to draw sub-Small tokens slightly smaller

			float arcWidth = canvas.getColumnWidth() * space / 30;
			float arcHeight = canvas.getRowHeight() * space / 30;
			int cells = space / 10;
			Point tl = canvas.convertGridCoordsToDisplay(location.getValue());
			Point br = canvas.convertGridCoordsToDisplay((int) getX() + cells, (int) getY() + cells);
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
		public void setProperty(String property, Object value) {
			if (property.equals(Group.PROPERTY_X)) {
				setX((Integer) value);
			} else if (property.equals(Group.PROPERTY_Y)) {
				setY((Integer) value);
			} else if (property.equals(PROPERTY_WEB_LABEL)) {
				if (value != null && ((String) value).length() > 0) {
					hasWebLabel = true;
					webLabel = (String) value;
				} else {
					hasWebLabel = false;
				}
			} else if (property.equals(Token.PROPERTY_LABEL)
					|| property.equals(MapElement.PROPERTY_VISIBLE)
					|| property.equals(Group.PROPERTY_LOCATION)
					|| property.equals(Token.PROPERTY_SPACE)) {
				super.setProperty(property, value);
			}
		}
	}


	TokenOverlay() {
		canvas = new CameraOverlayCanvas();
	}

	void setOffset(int offx, int offy) {
		canvas.setOffset(offx, offy);
	}

	// intended for debugging
	@SuppressWarnings("serial")
	JPanel getPanel() {
		return new JPanel() {
			{
				canvas.addRepaintListener(() -> repaintPanel());
			}

			SortedMap<String, String> descriptions = new TreeMap<>();

			private void repaintPanel() {
				repaint();
			}

			@Override
			protected void paintComponent(Graphics g) {
				super.paintComponent(g);
				assignLabels(descriptions);
				StringBuilder output = new StringBuilder();
				for (String k : descriptions.keySet()) {
					output.append(k).append("\t");
					output.append(descriptions.get(k)).append("\n");
				}
//				System.out.println("Tokens:\n" + output);
				((Graphics2D) g).rotate(Math.toRadians(-90), getWidth() / 2, getHeight() / 2);
				g.translate((getWidth() - getHeight()) / 2, canvas.getRowHeight() + (getHeight() - getWidth()) / 2);
				canvas.width = getWidth();
				canvas.height = getHeight();
				canvas.paint((Graphics2D) g);
			}
		};
	}

	void updateOverlay(int width, int height) {
		SortedMap<String, String> descriptions = new TreeMap<>();
		try {
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			ImageIO.write(getImage(width, height, descriptions), "png", stream);
			StringBuilder json = new StringBuilder();
			for (String k : descriptions.keySet()) {
				if (json.length() > 0) json.append(",\n");
				json.append("\t{\"token\": \"").append(k).append("\", \"name\": \"");
				json.append(descriptions.get(k)).append("\"}");
			}
			json.append("\n]\n");
			// order of these is important because of the workaround in the webpage for mobile safari:
			// if the order is reversed then when the image file is updated the website will close the
			// server-sent event connection and the update to the legend will get missed
			Updater.update("http://armitage/assistantdm/test/tokens1.json", ("[\n" + json.toString()).getBytes());
			Updater.update("http://armitage/assistantdm/test/tokens1.png", stream.toByteArray());

		} catch (IOException e) {
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
			// XXX would clone() work?
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

				//if (!(copy instanceof Grid))
				copy.setProperty(MapElement.PROPERTY_VISIBLE, Visibility.HIDDEN);

				canvas.addElement(copy, parent);
				elements.put(copy.getID(), copy);
			} catch (IOException | ClassNotFoundException e1) {
				e1.printStackTrace();
			}
		}
	}

	public void changeParent(MapElement element, MapElement parent) {
		MapElement e = elements.get(element.getID());
		MapElement p = elements.get(parent.getID());
		if (e != null) canvas.changeParent(e, p);
	}

	void removeElement(int id) {
		canvas.removeElement(id);
		elements.remove(id);
	}

	void promoteElement(MapElement e) {
		e = elements.get(e.getID());
		canvas.promoteElement(e);
	}

	void demoteElement(MapElement e) {
		e = elements.get(e.getID());
		canvas.demoteElement(e);
	}

	void setProperty(MapElement element, String property, Object value) {
		MapElement e = elements.get(element.getID());
		if (e != null && (e instanceof MaskToken || e instanceof Group)) {
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
				if (t.hasWebLabel || (t.getProperty(MapElement.PROPERTY_VISIBLE) == Visibility.VISIBLE && t.webLabel != null)) {
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
				if (t.getProperty(MapElement.PROPERTY_VISIBLE) == Visibility.VISIBLE && t.webLabel == null) {
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
