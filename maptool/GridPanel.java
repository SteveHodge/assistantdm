package maptool;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;

import javax.swing.JPanel;

@SuppressWarnings("serial")
public class GridPanel extends JPanel {
	Point2D ref1;	// stored as fraction of image width/height
	Point2D ref2;
	int colsSep, rowsSep;
	ScalableImagePanel image;

	public GridPanel(ScalableImagePanel i) {
		setOpaque(false);
		image = i;
	}

	void setRef1(int x, int y) {
		ref1 = new Point2D.Double((double) x / image.getImageWidth(), (double) y / image.getImageHeight());
		repaint();
	}

	void setRef2(int x, int y) {
		ref2 = new Point2D.Double((double) x / image.getImageWidth(), (double) y / image.getImageHeight());
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

	double getGridCellWidth() {
		if (ref1 == null || ref2 == null || colsSep <= 0) return 0d;
		return Math.abs((ref2.getX() - ref1.getX()) * image.getImageWidth() / colsSep);
	}

	double getGridCellHeight() {
		if (ref1 == null || ref2 == null || rowsSep <= 0) return 0d;
		return Math.abs((ref2.getY() - ref1.getY()) * image.getImageHeight() / rowsSep);
	}

	double getGridWidth() {
		if (ref1 == null || ref2 == null || colsSep <= 0) return 0d;
		return colsSep / ((Math.abs((ref2.getX() - ref1.getX()))));
	}

	double getGridHeight() {
		if (ref1 == null || ref2 == null || rowsSep <= 0) return 0d;
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
			double offsetX = (image.getImageWidth() * ref1.getX()) % w;
			double offsetY = (image.getImageHeight() * ref1.getY()) % h;
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

	}
}
