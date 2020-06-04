package digital_table.elements;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;

import digital_table.server.MapCanvas.Order;

// TODO this is very similar to Label. consider combining

public class WebMessage extends MapElement {
	private static final long serialVersionUID = 1L;

	public final static String PROPERTY_ALPHA = "alpha";	// float
	public final static String PROPERTY_ROTATIONS = "rotations";	// int - number of quadrants rotated clockwise
	public final static String PROPERTY_X = "x";	// double
	public final static String PROPERTY_Y = "y";	// double
	public final static String PROPERTY_WIDTH = "width";	// double
	public final static String PROPERTY_HEIGHT = "height";	// double
	public final static String PROPERTY_COLOR = "color";	// Color
	public final static String PROPERTY_BACKGROUND_COLOR = "background_color";	// Color
	public final static String PROPERTY_MESSAGE = "message";	// String - add a new message to the list
	public final static String PROPERTY_CLEAR = "clear";	// boolean - clear the message list
	public final static String PROPERTY_FONT_SIZE = "font_size";	// double

	public enum ColorMode {
		GLOBAL("Single Colour"),	// use the colour set by PROPERTY_COLOR for all text
		COLOR_PREFIXES("Coloured Names"),	// use per-prefix colours for the prefix text, PROPERTY_COLOR colour for remaining text
		COLOR_ALL("Coloured Messages");	// use per-prefix colours for whole messages

		@Override
		public String toString() {
			return description;
		}

		private ColorMode(String d) {
			description = d;
		}

		private final String description;
	}

	public final static String PROPERTY_COLOR_MODE = "color_mode";	// ColorMode
	public final static String PROPERTY_PREFIX_PREFIX = "prefix_color_";	// Color - note remaining text of property defines the prefix the colour applies to

	// position in grid coordinate-space
	Property<Double> x = new Property<Double>(PROPERTY_X, 0d, Double.class);
	Property<Double> y = new Property<Double>(PROPERTY_Y, 0d, Double.class);
	Property<Double> width = new Property<Double>(PROPERTY_WIDTH, 6d, Double.class);
	Property<Double> height = new Property<Double>(PROPERTY_HEIGHT, 2d, Double.class);
	Property<Float> alpha = new Property<Float>(PROPERTY_ALPHA, 1.0f, Float.class);
	Property<Integer> rotations = new Property<Integer>(PROPERTY_ROTATIONS, 0, Integer.class) {
		private static final long serialVersionUID = 1L;

		@Override
		public void setValue(Integer r) {
			super.setValue(r % 4);
		}
	};
	Property<Color> color = new Property<Color>(PROPERTY_COLOR, Color.BLACK, Color.class);
	Property<Color> backgroundColor = new Property<Color>(PROPERTY_BACKGROUND_COLOR, Color.WHITE, Color.class);
	Property<Double> fontSize = new Property<Double>(PROPERTY_FONT_SIZE, 0.333d, Double.class);
	Property<ColorMode> colorMode = new Property<ColorMode>(PROPERTY_COLOR_MODE, ColorMode.GLOBAL, ColorMode.class);

	Map<String, Color> prefixColors = new HashMap<>();
	ArrayDeque<String> messages = new ArrayDeque<String>();		// stores all messages from last to first (most efficient order for drawing)

	@Override
	public Order getDefaultOrder() {
		return Order.TOP;
	}

	@Override
	public void paint(Graphics2D g) {
		if (getVisibility() == Visibility.HIDDEN || canvas == null) return;

		Point2D o = canvas.convertGridCoordsToDisplay(canvas.getElementOrigin(this));
		g.translate(o.getX(), o.getY());

		Font f = g.getFont();
		float newSize;
		if (rotations.getValue() % 2 == 0) {
			newSize = canvas.getDisplayDimension(0, fontSize.getValue()).height;
		} else {
			newSize = canvas.getDisplayDimension(fontSize.getValue(), 0).width;
		}
		g.setFont(f.deriveFont(newSize));

		// get the maximum width of the first cell in each line
		FontMetrics metrics = g.getFontMetrics();
		Composite c = g.getComposite();
		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha.getValue() * (getVisibility() == Visibility.FADED ? 0.5f : 1f)));

		Point p = canvas.convertGridCoordsToDisplay(new Point2D.Double(x.getValue(), y.getValue()));

		Dimension s = canvas.getDisplayDimension(width.getValue(), height.getValue());
		int w = s.width;
		int h = s.height;

		AffineTransform t = AffineTransform.getQuadrantRotateInstance(rotations.getValue());
		Point size = new Point(w, h);
		t.transform(size, size);	// transform to get new dimensions
		int newWidth = Math.abs(size.x);
		int newHeight = Math.abs(size.y);
		t = g.getTransform();
		g.rotate(Math.toRadians(rotations.getValue() * 90), p.x + newWidth / 2, p.y + newHeight / 2);
		g.translate((newWidth - w) / 2, (newHeight - h) / 2);

		Shape oldClip = g.getClip();
		g.clip(new Rectangle(p.x, p.y, w, h));

		// paint background
		g.setColor(backgroundColor.getValue());
		g.fillRect(p.x, p.y, w, h);

		// draw text
		int y = p.y + h - 5 - metrics.getHeight() + metrics.getAscent();
		for (String line : messages) {
			g.setColor(color.getValue());
			String prefix = "";
			if (colorMode.getValue() == ColorMode.COLOR_ALL || colorMode.getValue() == ColorMode.COLOR_PREFIXES) {
				for (String pre : prefixColors.keySet()) {
					if (line.startsWith(pre)) {
						prefix = pre;
						g.setColor(prefixColors.get(pre));
					}
				}
				if (prefix.length() > 0) {
					Font normalFont = g.getFont();
					g.setFont(normalFont.deriveFont(Font.BOLD));
					g.drawString(prefix, p.x + 5, y);
					double boldWidth = g.getFontMetrics().getStringBounds(prefix, g).getWidth();
					g.setColor(color.getValue());
					g.setFont(normalFont);
					g.drawString(line.substring(prefix.length()), p.x + (int) boldWidth + 5, y);
				}
			}
			if (prefix.length() == 0) {
				g.drawString(line, p.x + 5, y);
			}
			y -= metrics.getHeight();
		}

		g.setClip(oldClip);

		g.setTransform(t);
		g.setComposite(c);
		g.setFont(f);
		g.translate(-o.getX(), -o.getY());
	}

	@Override
	public String getIDString() {
		return "WebMessage";
	}

	@Override
	public String toString() {
		return "Web Messages (" + getID() + ")";
	}

	@Override
	public void setProperty(String property, Object value) {
		if (property.equals(PROPERTY_CLEAR)) {
			messages.clear();
			if (canvas != null) canvas.repaint();

		} else if (property.equals(PROPERTY_MESSAGE)) {
			messages.addFirst(value.toString());
			if (canvas != null) canvas.repaint();

		} else if (property.startsWith(PROPERTY_PREFIX_PREFIX)) {
			String prefix = property.substring(PROPERTY_PREFIX_PREFIX.length());
			prefixColors.put(prefix, (Color) value);
			if (canvas != null) canvas.repaint();

		} else {
			super.setProperty(property, value);
		}
	}

	@Override
	public Object getProperty(String property) {
		if (property.startsWith(PROPERTY_PREFIX_PREFIX)) {
			String prefix = property.substring(PROPERTY_PREFIX_PREFIX.length());
			return prefixColors.get(prefix);

		} else {
			return super.getProperty(property);
		}
	}
}
