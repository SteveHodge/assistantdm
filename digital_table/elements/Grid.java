package digital_table.elements;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;

public class Grid extends MapElement {
	private static final long serialVersionUID = 1L;

	public final static String PROPERTY_COLOR = "color";	// Color
	public final static String PROPERTY_ALPHA = "alpha";	// float
	public static final String PROPERTY_RULER_ROW = "ruler_row";	// int
	public static final String PROPERTY_RULER_COLUMN = "ruler_column";	// int
	public static final String PROPERTY_BACKGROUND_COLOR = "background_color";	// Color

	public boolean showCoordinates = false;	// show coordinates of every cell
	Property<Integer> rulerRow = new Property<Integer>(PROPERTY_RULER_ROW, 0, Integer.class);
	Property<Integer> rulerColumn = new Property<Integer>(PROPERTY_RULER_COLUMN, 27, Integer.class);
	Property<Color> color = new Property<Color>(PROPERTY_COLOR, Color.BLACK, Color.class);
	Property<Color> backgroundColor = new Property<Color>(PROPERTY_BACKGROUND_COLOR, Color.WHITE, Color.class);
	Property<Float> alpha = new Property<Float>(PROPERTY_ALPHA, 1.0f, Float.class);

	public Grid() {
		visible.value = true;
	}

	public String getLetterIndex(int index) {
		String s = new String();
		if (index > 25) {
			s += (char)(64 + index/26);
		}
		s += (char)(65 + index % 26);
		return s;
	}

	@Override
	public void paint(Graphics2D g) {
		if (canvas == null || !isVisible()) return;

		g.setColor(color.getValue());

		Composite c = g.getComposite();
		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha.getValue()));

		Rectangle bounds = g.getClipBounds();
		Point tlCell = canvas.getGridCellCoordinates(bounds.x, bounds.y);
		Point brCell = canvas.getGridCellCoordinates(bounds.x + bounds.width, bounds.y + bounds.height);

		Point p = new Point();
		int cellWidth = canvas.getColumnWidth();
		int cellHeight = canvas.getRowHeight();

		if (showCoordinates) {
			for (int row = tlCell.y; row <= brCell.y; row++) {
				for (int col = tlCell.x; col <= brCell.x; col++) {
					canvas.getDisplayCoordinates(col, row, p);
					String s = ""+col+","+row;
					Rectangle2D strBounds = g.getFontMetrics().getStringBounds(s,g);
					g.drawString(s,
							(int)(p.x + (cellWidth - strBounds.getWidth())/2 - strBounds.getX()),
							(int)(p.y + (cellHeight - strBounds.getHeight())/2 - strBounds.getY())
							);
				}
			}
		}

		if (rulerRow.getValue() != null) {
			Font f = g.getFont();
			float newSize = canvas.getRowHeight()/2-4;
			if (newSize < 12.0) newSize = 12.0f;
			g.setFont(f.deriveFont(newSize));
			int row = rulerRow.getValue();
			for (int col = tlCell.x; col <= brCell.x; col++) {
				canvas.getDisplayCoordinates(col, row, p);
				String s = getLetterIndex(col);
				Rectangle2D strBounds = g.getFontMetrics().getStringBounds(s,g);
				g.setColor(backgroundColor.getValue());
				g.fillRect(p.x, p.y, cellWidth+1, (int)(-strBounds.getY() + 4));
				g.setColor(color.getValue());
				g.drawString(s,
						(int)(p.x + (cellWidth - strBounds.getWidth())/2 - strBounds.getX()),
						(int)(p.y - strBounds.getY() + 2)
						);
			}
			g.setFont(f);
		}

		if (rulerColumn.getValue() != null) {
			Font f = g.getFont();
			float newSize = canvas.getColumnWidth()/2-4;
			if (newSize < 12.0f) newSize = 12.0f;
			if (newSize > canvas.getColumnWidth()-4) newSize = canvas.getColumnWidth()-4;
			AffineTransform rot = AffineTransform.getQuadrantRotateInstance(1);
			g.setFont(f.deriveFont(newSize).deriveFont(rot));
			int col = rulerColumn.getValue();
			for (int row = tlCell.y; row <= brCell.y; row++) {
				canvas.getDisplayCoordinates(col, row, p);
				String s = ""+(row+1);
				Rectangle2D strBounds = g.getFontMetrics().getStringBounds(s,g);
				g.setColor(backgroundColor.getValue());
				g.fillRect((int)(p.x + cellWidth + strBounds.getY()-2), p.y,
						(int)(-strBounds.getY()+4), cellHeight+1);
				g.setColor(color.getValue());
				g.drawString(s,
						(int)(p.x + cellWidth + strBounds.getY()),
						(int)(p.y + (cellHeight - strBounds.getWidth())/2 - strBounds.getX())
						);
			}
			g.setFont(f);
		}

		for (int col = tlCell.x; col <= brCell.x; col++) {
			canvas.getDisplayCoordinates(col, 0, p);
			g.drawLine(p.x, bounds.y, p.x, bounds.y + bounds.height);
		}
		for (int row = tlCell.y; row <= brCell.y; row++) {
			canvas.getDisplayCoordinates(0, row, p);
			g.drawLine(bounds.x, p.y, bounds.x + bounds.width, p.y);
		}

		g.setComposite(c);
	}

	@Override
	public String toString() {
		return "Grid";
	}
}