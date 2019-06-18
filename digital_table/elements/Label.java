package digital_table.elements;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

import digital_table.server.MapCanvas.Order;
import digital_table.server.MeasurementLog;

public class Label extends MapElement {
	private static final long serialVersionUID = 1L;

	public final static String PROPERTY_ALPHA = "alpha";	// float
	public final static String PROPERTY_ROTATIONS = "rotations";	// int - number of quadrants rotated clockwise
	public final static String PROPERTY_X = "x";	// double
	public final static String PROPERTY_Y = "y";	// double
	public final static String PROPERTY_TEXT = "text";	// String
	public final static String PROPERTY_COLOR = "color";	// Color
	public static final String PROPERTY_BACKGROUND_COLOR = "background_color";	// Color
	public static final String PROPERTY_SOLID_BACKGROUND = "solid_background";	// boolean
	public static final String PROPERTY_FONT_SIZE = "font_size";	// double

	// position in grid coordinate-space
	Property<Double> x = new Property<Double>(PROPERTY_X, 0d, Double.class);
	Property<Double> y = new Property<Double>(PROPERTY_Y, 0d, Double.class);
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
	Property<String> text = new Property<String>(PROPERTY_TEXT, "", String.class);
	Property<Boolean> solidBackground = new Property<Boolean>(PROPERTY_SOLID_BACKGROUND, true, Boolean.class);
	Property<Double> fontSize = new Property<Double>(PROPERTY_FONT_SIZE, 0.333d, Double.class);

	@Override
	public Order getDefaultOrder() {
		return Order.ABOVEGRID;
	}

	@Override
	public void paint(Graphics2D g) {
		lastPaintTime = 0;
		long startTime = System.nanoTime();

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

		String[] text = getText();

		// get the maximum width of each line
		FontMetrics metrics = g.getFontMetrics();
		int width = 0;
		for (String line : text) {
			if (metrics.stringWidth(line) > width) width = metrics.stringWidth(line);
		}

		if (width > 0) {
			Composite c = g.getComposite();
			g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha.getValue() * (getVisibility() == Visibility.FADED ? 0.5f : 1f)));

			Point p = canvas.convertGridCoordsToDisplay(new Point2D.Double(x.getValue(), y.getValue()));

			int w = width + 10;
			int h = metrics.getHeight() * text.length;

			AffineTransform t = AffineTransform.getQuadrantRotateInstance(rotations.getValue());
			Point size = new Point(w, h);
			t.transform(size, size);	// transform to get new dimensions
			int newWidth = Math.abs(size.x);
			int newHeight = Math.abs(size.y);
			t = g.getTransform();
			g.rotate(Math.toRadians(rotations.getValue() * 90), p.x + newWidth / 2, p.y + newHeight / 2);
			g.translate((newWidth - w) / 2, (newHeight - h) / 2);

			// paint background
			if (solidBackground.getValue()) {
				g.setColor(backgroundColor.getValue());
				g.fillRect(p.x, p.y, w, h);
			}

			// draw text
			int y = p.y + metrics.getAscent();
			g.setColor(color.getValue());
			for (String line : text) {
				g.drawString(line, p.x + 5, y);
				y += metrics.getHeight();
			}

			g.setTransform(t);
			g.setComposite(c);
			g.setFont(f);
		}
		g.translate(-o.getX(), -o.getY());
		lastPaintTime = (System.nanoTime() - startTime) / 1000;
	}

	long lastPaintTime = 0;

	@Override
	public MeasurementLog getPaintTiming() {
		MeasurementLog m = new MeasurementLog("Label (" + getText()[0] + ")", id);
		m.total = lastPaintTime;
		return m;
	}

	protected String[] getText() {
		String[] lines;
		if (text.getValue() == null) {
			lines = new String[1];
			lines[0] = "";
		} else {
			lines = text.getValue().split("\\r?\\n|\\r");
		}
		return lines;
	}

	@Override
	public String toString() {
		String text = getText()[0];
		if (text.equals("")) text += getID();
		return "Label (" + text + ")";
	}
}
