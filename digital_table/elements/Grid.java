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
	public Integer rulerRow = 0;
	public Integer rulerColumn = 27;

	Color color = Color.BLACK;
	Color backgroundColor = Color.WHITE;
	float alpha = 1.0f;

	public Grid() {
		visible = true;
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
		if (canvas == null || !visible) return;

		g.setColor(color);

		Composite c = g.getComposite();
		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));

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

		if (rulerRow != null) {
			Font f = g.getFont();
			float newSize = canvas.getRowHeight()/2-4;
			if (newSize < 12.0) newSize = 12.0f;
			g.setFont(f.deriveFont(newSize));
			int row = rulerRow.intValue();
			for (int col = tlCell.x; col <= brCell.x; col++) {
				canvas.getDisplayCoordinates(col, row, p); 
				String s = getLetterIndex(col);
				Rectangle2D strBounds = g.getFontMetrics().getStringBounds(s,g);
				g.setColor(backgroundColor);
				g.fillRect(p.x, p.y, cellWidth+1, (int)(-strBounds.getY() + 4));
				g.setColor(color);
				g.drawString(s, 
						(int)(p.x + (cellWidth - strBounds.getWidth())/2 - strBounds.getX()), 
						(int)(p.y - strBounds.getY() + 2)
						);
			}
			g.setFont(f);
		}
		
		if (rulerColumn != null) {
			Font f = g.getFont();
			float newSize = canvas.getColumnWidth()/2-4;
			if (newSize < 12.0f) newSize = 12.0f;
			if (newSize > canvas.getColumnWidth()-4) newSize = canvas.getColumnWidth()-4;
			AffineTransform rot = AffineTransform.getQuadrantRotateInstance(1);
			g.setFont(f.deriveFont(newSize).deriveFont(rot));
			int col = rulerColumn.intValue();
			for (int row = tlCell.y; row <= brCell.y; row++) {
				canvas.getDisplayCoordinates(col, row, p); 
				String s = ""+(row+1);
				Rectangle2D strBounds = g.getFontMetrics().getStringBounds(s,g);
				g.setColor(backgroundColor);
				g.fillRect((int)(p.x + cellWidth + strBounds.getY()-2), p.y,
						(int)(-strBounds.getY()+4), cellHeight+1);
				g.setColor(color);
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

	public String toString() {
		return "Grid";
	}
	
	public Object getProperty(String property) {
		if (property.equals(PROPERTY_COLOR)) {
			return getColor();
		} else if (property.equals(PROPERTY_ALPHA)) {
			return getAlpha();
		} else if (property.equals(PROPERTY_RULER_ROW)) {
			return getRulerRow();
		} else if (property.equals(PROPERTY_RULER_COLUMN)) {
			return getRulerColumn();
		} else if (property.equals(PROPERTY_BACKGROUND_COLOR)) {
			return getBackgroundColor();
		} else {
			// throw exception?
			return null;
		}
	}

	public void setProperty(String property, Object value) {
		if (property.equals(PROPERTY_COLOR)) {
			setColor((Color)value);
		} else if (property.equals(PROPERTY_ALPHA)) {
			setAlpha((Float)value);
		} else if (property.equals(PROPERTY_RULER_ROW)) {
			setRulerRow((Integer)value);
		} else if (property.equals(PROPERTY_RULER_COLUMN)) {
			setRulerColumn((Integer)value);
		} else if (property.equals(PROPERTY_BACKGROUND_COLOR)) {
			setBackgroundColor((Color)value);
		} else {
			// throw exception?
		}
	}

	public float getAlpha() {
		return alpha;
	}
	
	public void setAlpha(float a) {
		if (alpha == a) return;
		float old = alpha;
		alpha = a;
		pcs.firePropertyChange(PROPERTY_ALPHA, old, alpha);
		if (canvas != null) canvas.repaint();
	}

	public Color getColor() {
		return color;
	}
	
	public void setColor(Color c) {
		if (color.equals(c)) return;
		Color old = color;
		color = c;
		pcs.firePropertyChange(PROPERTY_COLOR, old, color);
		if (canvas != null) canvas.repaint();
	}
	
	public Color getBackgroundColor() {
		return backgroundColor;
	}
	
	public void setBackgroundColor(Color c) {
		if (backgroundColor.equals(c)) return;
		Color old = backgroundColor;
		backgroundColor = c;
		pcs.firePropertyChange(PROPERTY_BACKGROUND_COLOR, old, backgroundColor);
		if (canvas != null) canvas.repaint();
	}

	public Integer getRulerRow() {
		return rulerRow;
	}

	public void setRulerRow(Integer newRow) {
		if (rulerRow == newRow) return;
		Integer old = rulerRow;
		rulerRow = newRow;
		pcs.firePropertyChange(PROPERTY_RULER_ROW, old, rulerRow);
		if (canvas != null) canvas.repaint();
	}

	public Integer getRulerColumn() {
		return rulerColumn;
	}

	public void setRulerColumn(Integer newColumn) {
		if (rulerColumn == newColumn) return;
		Integer old = rulerColumn;
		rulerColumn = newColumn;
		pcs.firePropertyChange(PROPERTY_RULER_COLUMN, old, rulerColumn);
		if (canvas != null) canvas.repaint();
	}
}