package maptool;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JPanel;

@SuppressWarnings("serial")
public class GridPanel extends JPanel {
	Point2D ref1;	// stored as fraction of image width/height
	Point2D ref2;
	int colsSep, rowsSep;
	ScalableImagePanel image;
	Set<POI> pois = new HashSet<POI>();

	public GridPanel(ScalableImagePanel i) {
		setOpaque(false);
		image = i;
	}

	Point2D convertPoint(Point2D p) {
		return new Point2D.Double(p.getX() / image.getImageWidth(), p.getY() / image.getImageHeight());
	}

	void setRef1(Point2D p) {
		ref1 = convertPoint(p);
		repaint();
	}

	void setRef2(Point2D p) {
		ref2 = convertPoint(p);
		repaint();
	}

	void setRefSeparationColumns(int c) {
		colsSep = c;
		repaint();
	}

	void setRefSeparationRows(int r) {
		rowsSep = r;
		repaint();
	}

	void setPOI(maptool.POI poi) {
		pois.add(poi);
		repaint();
	}

	public final static double DEFAULT_GRIDSIZE = 25400.0d / 294;	// The grid size used if none has been set. This is resolution of the digital table top.

	// grid cell dimensions in pixels
	double getGridCellWidth() {
		if (ref1 == null || ref2 == null || colsSep <= 0) return DEFAULT_GRIDSIZE * image.getScale();
		return Math.abs((ref2.getX() - ref1.getX()) * image.getImageWidth() / colsSep);
	}

	double getGridCellHeight() {
		if (ref1 == null || ref2 == null || rowsSep <= 0) return DEFAULT_GRIDSIZE * image.getScale();
		return Math.abs((ref2.getY() - ref1.getY()) * image.getImageHeight() / rowsSep);
	}

	// image dimensions in grid cells
	double getGridWidth() {
		if (ref1 == null || ref2 == null || colsSep <= 0) return image.getImageWidth() * image.getScale() / DEFAULT_GRIDSIZE;
		return colsSep / ((Math.abs((ref2.getX() - ref1.getX()))));
	}

	double getGridHeight() {
		if (ref1 == null || ref2 == null || rowsSep <= 0) return image.getImageHeight() * image.getScale() / DEFAULT_GRIDSIZE;
		return rowsSep / ((Math.abs((ref2.getY() - ref1.getY()))));
	}

	double getXOffset() {
		if (ref1 == null || ref2 == null || colsSep <= 0) return 0d;
		double x = ref1.getX() * image.getImageWidth() / getGridCellWidth();
		return 1.0d + (-x % 1);
	}

	double getYOffset() {
		if (ref1 == null || ref2 == null || rowsSep <= 0) return 0d;
		double y = ref1.getY() * image.getImageHeight() / getGridCellHeight();
		return 1.0d + (-y % 1);
	}

	@Override
	protected void paintComponent(Graphics graphics) {
		//System.out.println("Painting grid: " + getSize());
		Graphics2D g = (Graphics2D) graphics;

		double w = getGridCellWidth();
		double h = getGridCellHeight();
		if (w > 0 && h > 0) {
			g.setColor(Color.BLACK);
			double offsetX = 0;
			double offsetY = 0;
			if (ref1 != null) {
				offsetX = (image.getImageWidth() * ref1.getX()) % w;
				offsetY = (image.getImageHeight() * ref1.getY()) % h;
			}
			//System.out.println("Ref1 = " + ref1 + ", cell size = " + w + " x " + h);
			//System.out.println("Offset = " + offsetX + " x " + offsetY);
			for (int i = 0; i < getWidth() / w; i++) {
				g.drawLine((int) (offsetX + i * w), 0, (int) (offsetX + i * w), getHeight());
			}
			for (int j = 0; j < getHeight() / h; j++) {
				g.drawLine(0, (int) (offsetY + j * h), getWidth(), (int) (offsetY + j * h));
			}
		}

		if (ref1 != null) {
			g.setColor(Color.RED);
			g.drawLine((int) (ref1.getX() * image.getImageWidth()), 0, (int) (ref1.getX() * image.getImageWidth()), getHeight());
			g.drawLine(0, (int) (ref1.getY() * image.getImageHeight()), getWidth(), (int) (ref1.getY() * image.getImageHeight()));
		}

		if (ref2 != null) {
			g.setColor(Color.BLUE);
			g.drawLine((int) (ref2.getX() * image.getImageWidth()), 0, (int) (ref2.getX() * image.getImageWidth()), getHeight());
			g.drawLine(0, (int) (ref2.getY() * image.getImageHeight()), getWidth(), (int) (ref2.getY() * image.getImageHeight()));
		}

		Font f = g.getFont();
		Composite c = g.getComposite();
		g.setFont(f.deriveFont(Font.BOLD, 15));
		FontMetrics metrics = g.getFontMetrics();
		for (POI poi : pois) {
			int textw = metrics.stringWidth(poi.id) + 10;

			if (textw > 10) {
				Point p = new Point((int) (poi.relX * image.displayWidth), (int) (poi.relY * image.displayHeight));
				p.x += 7;
				p.y -= 10;

				int texth = metrics.getHeight();

				// paint background
				g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
				g.setColor(Color.WHITE);
				g.fillRect(p.x, p.y, textw, texth);
				g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));

				// draw text
				int y = p.y + metrics.getAscent();
				g.setColor(Color.RED);
				g.drawString(poi.id, p.x + 5, y);
			}

			// paint background
			Point p = new Point((int) (poi.relX * image.displayWidth), (int) (poi.relY * image.displayHeight));
			g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
			g.setColor(Color.WHITE);
			g.fillOval(p.x - 7, p.y - 7, 15, 15);
			g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
			g.setColor(Color.RED);
			g.fillOval(p.x - 5, p.y - 5, 11, 11);
			g.setComposite(c);
		}
		g.setComposite(c);
		g.setFont(f);
	}
}
