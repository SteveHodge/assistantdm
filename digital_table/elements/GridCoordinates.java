package digital_table.elements;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

public class GridCoordinates extends MapElement {
	private static final long serialVersionUID = 1L;

	public final static String PROPERTY_COLOR = "color";	// Color
	public final static String PROPERTY_ALPHA = "alpha";	// float
	public static final String PROPERTY_RULER_ROW = "ruler_row";	// int
	public static final String PROPERTY_RULER_COLUMN = "ruler_column";	// int
	public static final String PROPERTY_INVERT_ROW = "invert_row";	// boolean
	public static final String PROPERTY_INVERT_COLUMN = "invert_column";	// boolean
	public static final String PROPERTY_BACKGROUND_COLOR = "background_color";	// Color

	public boolean showCoordinates = false;	// show coordinates of every cell
	Property<Integer> rulerRow = new Property<Integer>(PROPERTY_RULER_ROW, 0, Integer.class);
	Property<Integer> rulerColumn = new Property<Integer>(PROPERTY_RULER_COLUMN, 27, Integer.class);
	Property<Boolean> invertRow = new Property<Boolean>(PROPERTY_INVERT_ROW, false, Boolean.class);
	Property<Boolean> invertColumn = new Property<Boolean>(PROPERTY_INVERT_COLUMN, false, Boolean.class);
	Property<Color> color = new Property<Color>(PROPERTY_COLOR, Color.BLACK, Color.class);
	Property<Color> backgroundColor = new Property<Color>(PROPERTY_BACKGROUND_COLOR, Color.WHITE, Color.class);
	Property<Float> alpha = new Property<Float>(PROPERTY_ALPHA, 1.0f, Float.class);

	public GridCoordinates() {
		visible.setValue(Visibility.VISIBLE);
	}

	public String getLetterIndex(int index) {
		String s = new String();
		if (index > 25) {
			s += (char) (64 + index / 26);
		}
		s += (char) (65 + index % 26);
		return s;
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
		int cellWidth = canvas.getColumnWidth();
		int cellHeight = canvas.getRowHeight();

		if (showCoordinates) {
			for (int row = tlCell.y; row <= brCell.y; row++) {
				for (int col = tlCell.x; col <= brCell.x; col++) {
					canvas.convertCanvasCoordsToDisplay(col, row, p);
					String s = "" + (col - canvas.getXOffset()) + "," + (row - canvas.getYOffset());
					Rectangle2D strBounds = g.getFontMetrics().getStringBounds(s, g);
					g.drawString(s,
							(int) (p.x + (cellWidth - strBounds.getWidth()) / 2 - strBounds.getX()),
							(int) (p.y + (cellHeight - strBounds.getHeight()) / 2 - strBounds.getY())
							);
				}
			}
		}

		if (rulerRow.getValue() != null) {
			Font f = g.getFont();
			float newSize = canvas.getRowHeight() / 2 - 4;
			if (newSize < 12.0) newSize = 12.0f;
			int row = rulerRow.getValue();

			if (invertRow.getValue()) {
				AffineTransform rot = AffineTransform.getQuadrantRotateInstance(2);
				g.setFont(f.deriveFont(newSize).deriveFont(rot));
			} else {
				g.setFont(f.deriveFont(newSize));
			}
			for (int col = tlCell.x; col <= brCell.x; col++) {
				g.setColor(backgroundColor.getValue());
				String s = getLetterIndex(col - canvas.getRemote().getXOffset());
				Rectangle2D strBounds = g.getFontMetrics().getStringBounds(s, g);
				canvas.convertCanvasCoordsToDisplay(col, row + canvas.getYOffset(), p);

				int bgy = invertRow.getValue() ? (int) (p.y + cellHeight - strBounds.getHeight() - 1) : p.y;
				g.fillRect(p.x, bgy, cellWidth + 1, (int) (-strBounds.getY() + 4));
				g.setColor(color.getValue());
				if (invertRow.getValue()) {
					g.drawString(s,
							(int) (p.x + (cellWidth + strBounds.getWidth()) / 2 - strBounds.getX()),
							(int) (p.y + cellHeight + strBounds.getY())
							);
				} else {
					g.drawString(s,
							(int) (p.x + (cellWidth - strBounds.getWidth()) / 2 - strBounds.getX()),
							(int) (p.y - strBounds.getY() + 2)
							);
				}
				g.drawLine(p.x, bgy + 1, p.x, bgy + (int) (-strBounds.getY() + 4));
			}
			g.setFont(f);
		}

		if (rulerColumn.getValue() != null) {
			Font f = g.getFont();
			float newSize = canvas.getColumnWidth() / 2 - 4;
			if (newSize < 12.0f) newSize = 12.0f;
			if (newSize > canvas.getColumnWidth() - 4) newSize = canvas.getColumnWidth() - 4;
			int col = rulerColumn.getValue();

			AffineTransform rot;
			if (invertColumn.getValue()) {
				rot = AffineTransform.getQuadrantRotateInstance(3);
			} else {
				rot = AffineTransform.getQuadrantRotateInstance(1);
			}
			g.setFont(f.deriveFont(newSize).deriveFont(rot));

			for (int row = tlCell.y; row <= brCell.y; row++) {
				g.setColor(backgroundColor.getValue());
				String s = "" + (row + 1 - canvas.getRemote().getYOffset());
				Rectangle2D strBounds = g.getFontMetrics().getStringBounds(s, g);
				canvas.convertCanvasCoordsToDisplay(col + canvas.getXOffset(), row, p);

				int bgx = invertColumn.getValue() ? p.x+1 : (int) (p.x + cellWidth + strBounds.getY() - 2);
				g.fillRect(bgx, p.y, (int) (-strBounds.getY() + 4), cellHeight + 1);
				g.setColor(color.getValue());

				if (invertColumn.getValue()) {
					g.drawString(s,
							(int) (p.x - strBounds.getY() + 2),
							(int) (p.y + (cellHeight + strBounds.getWidth()) / 2 - strBounds.getX())
							);
				} else {
					g.drawString(s,
							(int) (p.x + cellWidth + strBounds.getY()),
							(int) (p.y + (cellHeight - strBounds.getWidth()) / 2 - strBounds.getX())
							);
				}
				g.drawLine(bgx, p.y, bgx + (int) (-strBounds.getY() + 3), p.y);
			}
			g.setFont(f);
		}

		g.setComposite(c);
	}

	long lastPaintTime = 0;

	@Override
	public String getIDString() {
		return "GridCoordinates";
	}

	@Override
	public Layer getDefaultLayer() {
		return Layer.INFORMATION;
	}

	@Override
	public String toString() {
		return "GridCoordinates";
	}
}
