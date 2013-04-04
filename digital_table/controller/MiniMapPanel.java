package digital_table.controller;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Point2D;

import javax.swing.JPanel;
import javax.swing.ListModel;

import digital_table.elements.MapElement;

import digital_table.server.MapCanvas;
import digital_table.server.RepaintListener;

@SuppressWarnings("serial")
public class MiniMapPanel extends JPanel implements RepaintListener {
	public final static int DEFAULT_CELL_SIZE = 20;

	int rows = 38;
	int columns = 32;

	private MiniMapCanvas canvas;

	protected class MiniMapCanvas extends MapCanvas {
		public int getColumnWidth() {
			return getWidth() / columns;
		}

		public int getRowHeight() {
			return getHeight() / rows;
		}
		
		// get the grid coordinates of the grid cell containing (x,y) 
		public Point getGridCellCoordinates(int x, int y) {
			int col = x * columns / getWidth();
			int row = y * rows / getHeight();
			return new Point(col,row);
		}
		
		// get the pixel location of the top left corner of the grid cell at (col, row)
		// if p is not null the the location is stored in p and returned
		public Point getDisplayCoordinates(int col, int row, Point p) {
			if (p == null) p = new Point();
			p.x = col * getWidth() / columns;
			p.y = row * getHeight() / rows;
			return p;
		}

		public Point getDisplayCoordinates(int col, int row) {
			return getDisplayCoordinates(col, row, null);
		}

		public Point2D getGridCoordinates(int x, int y) {
			return new Point2D.Double((double)x * columns / getWidth(), (double)y * rows / getHeight());
		}

		public Point getDisplayCoordinates(Point2D p) {
			Point p2 = new Point();
			p2.x = (int)(p.getX() * getWidth() / columns);
			p2.y = (int)(p.getY() * getHeight() / rows);
			return p2;
		}
	};

	public MiniMapPanel() {
		canvas = new MiniMapCanvas();
		canvas.addRepaintListener(this);
	}
	
	public ListModel getModel() {
		return canvas.getModel();
	}
	
	public Dimension getPreferredSize() {
		return new Dimension(columns * DEFAULT_CELL_SIZE, rows * DEFAULT_CELL_SIZE);
	}

	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		canvas.paint((Graphics2D)g);
	}
	
	public void addElement(MapElement e) {
		canvas.addElement(e);
		repaint();
	}
	
	public void removeElement(int id) {
		if (canvas.removeElement(id)) repaint();
	}

	public Point2D getGridCoordinates(int x, int y) {
		return canvas.getGridCoordinates(x, y);
	}

	public Point getDisplayCoordinates(Point2D position) {
		return canvas.getDisplayCoordinates(position);
	}
}