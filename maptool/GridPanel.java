package maptool;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;

import javax.swing.JPanel;

public class GridPanel extends JPanel {
	Point2D ref1;
	Point2D ref2;
	int colsSep, rowsSep;

	public GridPanel() {
		setOpaque(false);
	}

	void setRef1(int x, int y) {
		ref1 = new Point2D.Double((double) x / getWidth(), (double) y / getHeight());
		repaint();
	}

	void setRef2(int x, int y) {
		ref2 = new Point2D.Double((double) x / getWidth(), (double) y / getHeight());
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
		return Math.abs((ref2.getX() - ref1.getX()) * getWidth() / colsSep);
	}

	double getGridCellHeight() {
		if (ref1 == null || ref2 == null || rowsSep <= 0) return 0d;
		return Math.abs((ref2.getY() - ref1.getY()) * getHeight() / rowsSep);
	}

	@Override
	protected void paintComponent(Graphics graphics) {
		//System.out.println("Grid: " + getSize());
		Graphics2D g = (Graphics2D) graphics;

		double w = getGridCellWidth();
		double h = getGridCellHeight();
		if (w > 0 && h > 0) {
			g.setColor(Color.BLACK);
			double offsetX = (getWidth() * ref1.getX()) % w;
			double offsetY = (getHeight() * ref1.getY()) % h;
			System.out.println("Ref1 = " + ref1 + ", cell size = " + w + " x " + h);
			System.out.println("Offset = " + offsetX + " x " + offsetY);
			for (int i = 0; i < getWidth() / w; i++) {
				g.drawLine((int) (offsetX + i * w), 0, (int) (offsetX + i * w), getHeight());
			}
			for (int j = 0; j < getHeight() / h; j++) {
				g.drawLine(0, (int) (offsetY + j * h), getWidth(), (int) (offsetY + j * h));
			}
		}

		if (ref1 != null) {
			g.setColor(Color.RED);
			g.drawLine((int) (ref1.getX() * getWidth()), 0, (int) (ref1.getX() * getWidth()), getHeight());
			g.drawLine(0, (int) (ref1.getY() * getHeight()), getWidth(), (int) (ref1.getY() * getHeight()));
		}

		if (ref2 != null) {
			g.setColor(Color.BLUE);
			g.drawLine((int) (ref2.getX() * getWidth()), 0, (int) (ref2.getX() * getWidth()), getHeight());
			g.drawLine(0, (int) (ref2.getY() * getHeight()), getWidth(), (int) (ref2.getY() * getHeight()));
		}

	}
}
