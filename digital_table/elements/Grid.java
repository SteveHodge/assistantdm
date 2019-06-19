package digital_table.elements;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Point2D;

public class Grid extends MapElement {
	private static final long serialVersionUID = 1L;

	public final static String PROPERTY_COLOR = "color";	// Color
	public final static String PROPERTY_ALPHA = "alpha";	// float

	Property<Color> color = new Property<Color>(PROPERTY_COLOR, Color.BLACK, Color.class);
	Property<Float> alpha = new Property<Float>(PROPERTY_ALPHA, 1.0f, Float.class);

	public Grid() {
		visible.setValue(Visibility.VISIBLE);
	}

	@Override
	public void paint(Graphics2D g) {
		if (canvas == null || getVisibility() == Visibility.HIDDEN) return;

		g.setColor(color.getValue());

		Composite c = g.getComposite();
		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha.getValue()));

		Rectangle bounds = g.getClipBounds();
		Point2D tl = canvas.convertDisplayCoordsToCanvas(bounds.x, bounds.y);
		Point tlCell = new Point();
		tlCell.setLocation(tl.getX(), tl.getY());
		Point2D br = canvas.convertDisplayCoordsToCanvas(bounds.x + bounds.width, bounds.y + bounds.height);
		Point brCell = new Point();
		brCell.setLocation(br.getX(), br.getY());

		Point p = new Point();

		for (int col = tlCell.x; col <= brCell.x; col++) {
			canvas.convertCanvasCoordsToDisplay(col, 0, p);
			g.drawLine(p.x, bounds.y, p.x, bounds.y + bounds.height);
		}
		for (int row = tlCell.y; row <= brCell.y; row++) {
			canvas.convertCanvasCoordsToDisplay(0, row, p);
			g.drawLine(bounds.x, p.y, bounds.x + bounds.width, p.y);
		}

		g.setComposite(c);
	}

	long lastPaintTime = 0;

	@Override
	public String getIDString() {
		return "Grid";
	}

	@Override
	public String toString() {
		return "Grid";
	}
}