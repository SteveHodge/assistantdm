package digital_table.controller;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.font.GlyphVector;
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

import digital_table.elements.Group;
import digital_table.elements.MapElement;
import digital_table.elements.MapElement.Layer;
import digital_table.elements.MapElement.Visibility;
import digital_table.elements.Token;
import digital_table.server.MapCanvas;
import util.Updater;

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

	protected int rotateAngle = 90;	// angle in degrees to rotate the image. currently better be 90 or -90.

	protected CameraOverlayCanvas canvas;
	protected Map<Integer, MapElement> elements = new HashMap<>();

	protected class CameraOverlayCanvas extends MapCanvas {
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

		@Override
		public void debug(String title) {
		}

//		@Override
//		public void paint(Graphics2D g) {
//			if (TokenOverlay.this instanceof RemoteImageDisplay) {
//				System.err.println("Remote Image Paint");
//				Thread.dumpStack();
//			}
//			super.paint(g);
//		}
	};

	private class MaskToken extends Group {
		private static final long serialVersionUID = 1L;

		private Property<Integer> space = new Property<>(Token.PROPERTY_SPACE, 10, Integer.class);
		private Property<Color> color = new Property<Color>(Token.PROPERTY_COLOR, Color.BLACK, Color.class);

		private String webLabel = null;
		private boolean hasWebLabel = false;
		Token realToken;

		public MaskToken(Token t) {
			super(t.getID());
			realToken = t;
		}

		@Override
		public Layer getDefaultLayer() {
			return Layer.INFORMATION;
		}

		@Override
		public void paint(Graphics2D g) {
			if (canvas == null || getVisibility() == Visibility.HIDDEN) return;

			Point2D o = canvas.convertGridCoordsToDisplay(canvas.getElementOrigin(this));
			g.translate(o.getX(), o.getY());

			int space = this.space.getValue();
			float offset = 0.1f;
			if (space < 10) {
				// draw tiny and smaller creatures slightly smaller
				space = 10;
				offset = 0.2f;
			}

			float arcWidth = canvas.getColumnWidth() * space / (space > 10 ? 30 : 45);
			float arcHeight = canvas.getRowHeight() * space / (space > 10 ? 30 : 45);
			float cells = space / 10f - offset * 2;
			Point tl = canvas.convertGridCoordsToDisplay(new Point2D.Double(getX() + offset, getY() + offset));
			Point br = canvas.convertGridCoordsToDisplay(new Point2D.Double(getX() + offset + cells, getY() + offset + cells));
			BasicStroke stroke = getBorderStroke(space);
			float inset = stroke.getLineWidth() / 2;

			// paint background:
			Shape s = new RoundRectangle2D.Float(tl.x + inset, tl.y + inset, br.x - tl.x - inset * 2, br.y - tl.y - inset * 2, arcWidth, arcHeight);
			g.setColor(Color.WHITE);
			g.fill(s);

			// paint border:
			g.setColor(color.getValue());
			Stroke oldStroke = g.getStroke();
			g.setStroke(stroke);
			g.draw(s);
			g.setStroke(oldStroke);

			float l = stroke.getLineWidth();
			Shape oldClip = g.getClip();
			Shape clip = new RoundRectangle2D.Float(tl.x + l, tl.y + l, br.x - tl.x - l * 2, br.y - tl.y - l * 2,
					arcWidth,
					arcHeight);
			g.setClip(clip);

			if (webLabel != null && webLabel.length() > 0) {
				Rectangle2D clipBounds = clip.getBounds2D();
				Rectangle2D labelBounds = new Rectangle2D.Double(clipBounds.getX() + l / 2, clipBounds.getY() + l / 2, clipBounds.getWidth() - l, clipBounds.getHeight() - l);
				AffineTransform t = g.getTransform();
				g.rotate(Math.toRadians(-rotateAngle), labelBounds.getCenterX(), labelBounds.getCenterY());

//				System.out.println(webLabel + ": labelBounds: " + labelBounds);
				g.setColor(Color.BLACK);
				Font f = g.getFont().deriveFont((float) labelBounds.getHeight());
				g.setFont(f);

				GlyphVector gv = g.getFont().createGlyphVector(g.getFontRenderContext(), webLabel);
				Rectangle bounds = gv.getPixelBounds(null, 0f, 0f);
//				System.out.println(webLabel + ": Bounds = " + bounds + ", fontsize = " + g.getFont().getSize());

				if (Math.abs(bounds.getHeight() - labelBounds.getHeight()) > 1) {
					// if height doesn't match then rescale the font to match height
					float newSize = (float) (labelBounds.getHeight() * labelBounds.getHeight() / bounds.getHeight());
					if (newSize < 7) newSize = 7;
					f = f.deriveFont(newSize);
					g.setFont(f);

					gv = g.getFont().createGlyphVector(g.getFontRenderContext(), webLabel);
					bounds = gv.getPixelBounds(null, 0f, 0f);
//					System.out.println(webLabel + ": New bounds = " + bounds + ", fontsize = " + g.getFont().getSize());
				}

				if (bounds.getWidth() > labelBounds.getWidth()) {
					// if label is too long then shrink to fit
					float newSize = (float) (f.getSize2D() * labelBounds.getWidth() / bounds.getWidth());
					if (newSize < 7) newSize = 7;
					f = f.deriveFont(newSize);
					g.setFont(f);
				}

				double yPos = labelBounds.getMaxY();
				gv = g.getFont().createGlyphVector(g.getFontRenderContext(), webLabel);
				bounds = gv.getPixelBounds(null, 0f, (float) yPos);
//				System.out.println(webLabel + ": Final bounds = " + bounds + ", fontsize = " + g.getFont().getSize());
				double xPos = (labelBounds.getCenterX() - bounds.getWidth() / 2);
				if (xPos < labelBounds.getX()) xPos = labelBounds.getX();

				g.drawString(webLabel, (float) xPos, (float) yPos);
				//g.fill(gv.getOutline((float) xPos, (float) yPos));
				g.setTransform(t);
			}

			g.setClip(oldClip);

			g.translate(-o.getX(), -o.getY());
		}

		private BasicStroke getBorderStroke(int space) {
			int stroke = canvas.getColumnWidth() < 40 ? 4 : 6;
			if (space <= 10) stroke = stroke / 2;
			return new BasicStroke(stroke);
		}

		@Override
		public String toString() {
			if (label == null || label.getValue().length() == 0) return "Token (" + getID() + ")";
			return "Token (" + label + ")";
		}

		@Override
		public void setProperty(String property, Object value) {
			if (property.equals(Group.PROPERTY_X)) {
				if (value instanceof Integer) {
					setX(((Integer) value).doubleValue());
				} else {
					setX((Double) value);
				}
			} else if (property.equals(Group.PROPERTY_Y)) {
				if (value instanceof Integer) {
					setY(((Integer) value).doubleValue());
				} else {
					setY((Double) value);
				}
			} else if (property.equals(PROPERTY_WEB_LABEL)) {
				if (value != null && ((String) value).length() > 0) {
					hasWebLabel = true;
					if (!value.equals(webLabel)) {
						webLabel = (String) value;
						canvas.repaint();
					}
				} else {
					hasWebLabel = false;
					webLabel = null;
					canvas.repaint();
				}
			} else if (property.equals(Token.PROPERTY_COLOR)) {
				// don't want to use white as that's the background colour...
				if (Color.WHITE.equals(value)) value = Color.BLACK;
				super.setProperty(property, value);
			} else if (property.equals(Token.PROPERTY_LABEL)
					|| property.equals(MapElement.PROPERTY_VISIBLE)
					|| property.equals(Group.PROPERTY_LOCATION)
					|| property.equals(MapElement.PROPERTY_LAYER)
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

			SortedMap<String, MapElement> descriptions = new TreeMap<>();

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
				((Graphics2D) g).rotate(Math.toRadians(rotateAngle), getWidth() / 2, getHeight() / 2);
				canvas.width = getWidth();
				canvas.height = getHeight();
				g.translate((getWidth() - getHeight()) / 2, canvas.getRowHeight() + (getHeight() - getWidth()) / 2);
				canvas.paint((Graphics2D) g);
			}
		};
	}

	void updateOverlay(int width, int height) {
		SortedMap<String, MapElement> descriptions = new TreeMap<>();
		updateOverlay(getImage(width, height, descriptions), descriptions);
	}

	// this version used by the RemoteImageDisplay repaint thread
	void updateOverlay(BufferedImage img, SortedMap<String, MapElement> descriptions) {
		try {
//			System.out.println("Updating token overlay");
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			ImageIO.write(img, "png", stream);
			StringBuilder json = new StringBuilder("[\n");
			boolean first = true;
			for (String k : descriptions.keySet()) {
				MapElement t = descriptions.get(k);
				if (t.getVisibility() == Visibility.HIDDEN) continue;
				if (first) {
					first = false;
				} else {
					json.append(",\n");
				}
				json.append("\t{\"token\": \"").append(k).append("\", ");
				json.append("\"name\": \"").append(t.getProperty(MaskToken.PROPERTY_LABEL)).append("\"");
				Color c = (Color) t.getProperty(Token.PROPERTY_COLOR);
				if (!Color.BLACK.equals(c)) {
					String r = Integer.toHexString(c.getRed());
					if (r.length() == 1) r = "0" + r;
					String g = Integer.toHexString(c.getGreen());
					if (g.length() == 1) g = "0" + g;
					String b = Integer.toHexString(c.getBlue());
					if (b.length() == 1) b = "0" + b;
					json.append(", \"color\": \"#").append(r).append(g).append(b).append("\"");
				}
				json.append("}");
			}
			json.append("\n]\n");

			// order of the following is important because of the workaround in the webpage for mobile safari:
			// if the order is reversed then when the image file is updated the website will close the
			// server-sent event connection and the update to the legend will get missed
			Updater.updateURL(Updater.TOKEN_BASE_URL + ".json", json.toString().getBytes());
			Updater.updateURL(Updater.TOKEN_BASE_URL + ".png", stream.toByteArray());

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	BufferedImage getImage(int width, int height, SortedMap<String, MapElement> descriptions) {
		assignLabels(descriptions);
		return getImage(width, height, BufferedImage.TYPE_INT_ARGB);
	}

	protected BufferedImage getImage(int width, int height, int type) {
		canvas.width = width;
		canvas.height = height;
		BufferedImage output = new BufferedImage(width, height, type);
		Graphics2D imgG = output.createGraphics();
		imgG.setBackground(new Color(255, 255, 255, 0));
		imgG.setClip(0, 0, output.getWidth(), output.getHeight());
		imgG.rotate(Math.toRadians(rotateAngle), output.getWidth() / 2, output.getHeight() / 2);
		imgG.translate((output.getWidth() - height) / 2, canvas.getRowHeight() + (output.getHeight() - width) / 2);
		canvas.paint(imgG);
		return output;
	}

	void addElement(MapElement e, MapElement parent) {
		if (parent != null) parent = elements.get(parent.getID());

		if (e instanceof Token) {
			MaskToken t = new MaskToken((Token) e);
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
			if (property.equals(MapElement.PROPERTY_LAYER)) {
				canvas.changeLayer(e, (Layer) value);
			} else {
				e.setProperty(property, value);
			}
		}
	}

	// Assigns labels to all visible tokens that don't have a web_label set either by the user or previously by the system.
	// hidden tokens have system assigned labels removed. Also generates a key. If a web_label is used for more than one
	// token then the description used in the key will be taken from one of the associated tokens randomly.
	private SortedMap<String, MapElement> assignLabels(SortedMap<String, MapElement> descriptions) {
		descriptions.clear();

		// gather any used labels
		for (int id : elements.keySet()) {
			MapElement e = elements.get(id);
			if (e instanceof MaskToken) {
				MaskToken t = (MaskToken) e;
				if (t.hasWebLabel || (t.getProperty(MapElement.PROPERTY_VISIBLE) == Visibility.VISIBLE && t.webLabel != null)) {
					descriptions.put(t.webLabel, t);
					t.realToken.setProperty(Token.PROPERTY_WEB_LABEL, "");	// user set label, we don't feed this back to the ui
				} else {
					t.webLabel = null;
					t.realToken.setProperty(Token.PROPERTY_WEB_LABEL, "");
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
					t.realToken.setProperty(Token.PROPERTY_WEB_LABEL, t.webLabel);
					descriptions.put(t.webLabel, t);
				}
			}
		}

		return descriptions;
	}
}
