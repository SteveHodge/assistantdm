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
import java.util.ArrayList;
import java.util.List;

import digital_table.server.MapCanvas.Order;

// TODO this is very similar to Label. consider combining

public class Initiative extends MapElement {
	private static final long serialVersionUID = 1L;

	public final static String PROPERTY_ALPHA = "alpha";	// float
	public final static String PROPERTY_ROTATIONS = "rotations";	// int - number of quadrants rotated clockwise
	public final static String PROPERTY_X = "x";	// double
	public final static String PROPERTY_Y = "y";	// double
	public final static String PROPERTY_TEXT = "text";	// String
	public final static String PROPERTY_COLOR = "color";	// Color
	public static final String PROPERTY_BACKGROUND_COLOR = "background_color";	// Color

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

	float fontSize = 0.333f;	// font size in grid coordinate-space

	@Override
	public Order getDefaultOrder() {
		return Order.TOP;
	}

	@Override
	public void paint(Graphics2D g) {
		if (getVisibility() == Visibility.HIDDEN || canvas == null) return;

//		Point2D o = canvas.getDisplayCoordinates(offset);
//		g.translate(o.getX(), o.getY());

		Font f = g.getFont();
		float newSize = canvas.getDisplayDimension(0, fontSize).height;
		g.setFont(f.deriveFont(newSize));

		List<String[]> text = getTable();

		// get the maximum width of the first cell in each line
		FontMetrics metrics = g.getFontMetrics();
		int col1Width = 0, col2Width = 0;
		for (String[] line : text) {
			if (metrics.stringWidth(line[0]) > col1Width) col1Width = metrics.stringWidth(line[0]);
			if (metrics.stringWidth(line[1]) > col2Width) col2Width = metrics.stringWidth(line[1]);
		}

		if (col1Width + col2Width > 0) {
			Composite c = g.getComposite();
			g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha.getValue()));

			Point p = canvas.convertCanvasCoordsToDisplay(new Point2D.Double(x.getValue() + canvas.getRemote().getXOffset(), y.getValue() + canvas.getRemote().getYOffset()));
			int x0 = p.x + 5;
			int x1 = p.x + 5 + col1Width + 10;
			int y = p.y + 5 + metrics.getAscent();

			int w = col1Width + col2Width + 20;
			int h = metrics.getHeight() * text.size() + 10;

			AffineTransform t = AffineTransform.getQuadrantRotateInstance(rotations.getValue());
			Point size = new Point(w, h);
			t.transform(size, size);	// transform to get new dimensions
			int newWidth = Math.abs(size.x);
			int newHeight = Math.abs(size.y);
			t = g.getTransform();
			g.rotate(Math.toRadians(rotations.getValue() * 90), p.x + newWidth / 2, p.y + newHeight / 2);
			g.translate((newWidth - w) / 2, (newHeight - h) / 2);

			g.setColor(backgroundColor.getValue());
			g.fillRect(p.x, p.y, w, h);
			g.setColor(color.getValue());
			for (String[] line : text) {
				g.drawString(line[0], x0, y);
				g.drawString(line[1], x1, y);
				y += metrics.getHeight();
			}

			g.setTransform(t);
			g.setComposite(c);
			g.setFont(f);
		}
//		g.translate(-o.getX(), -o.getY());
	}

	protected List<String[]> getTable() {
		String[] lines = text.getValue().split("\\r?\\n|\\r");
		List<String[]> output = new ArrayList<>();

		String[] outLine = new String[2];
		for (String line : lines) {
			String[] parts = line.split("=");
			if (parts.length < 2) continue;

			if (parts[0].equals("round")) {
				outLine[0] = "Round";
				outLine[1] = parts[1];
				output.add(outLine);
				outLine = new String[2];

			} else if (parts[0].equals("lastindex")) {

			} else if (parts[0].startsWith("fixedname")) {
				outLine[0] = parts[1];

			} else if (parts[0].startsWith("init")) {
				outLine[1] = parts[1];
				output.add(outLine);
				outLine = new String[2];
			}
		}
		return output;
	}

	@Override
	public String toString() {
		return "Initiative (" + getID() + ")";
	}
}
