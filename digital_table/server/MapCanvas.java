package digital_table.server;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.ListModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import digital_table.elements.Grid;
import digital_table.elements.MapElement;

/*
 * Represents the whole map area - an infinite plane which can contain elements arranged in layers
 * 
 * note: this class implements coordinate conversion appropriate for 0.294 dot pitch monitors. subclass with
 * different scaling need to override getColumnWidth(), getColumnHeight(), and all get..Coordinate() methods
 */

public class MapCanvas implements ListDataListener {
	ListModel model;
	protected Color bgColor;
	List<RepaintListener> listeners = new ArrayList<RepaintListener>();
	Grid grid;	// used to position other element above or below the grid

	public enum Order {
		Top,		// use for popups, informational elements
		AboveGrid,	// use for creatures
		BelowGrid,	// use for templates
		Bottom;		// use for backgrounds images 
	}
	
	public MapCanvas() {
		setModel(new DefaultListModel());
	}

	public ListModel getModel() {
		return model;
	}
	
	public void setModel(ListModel m) {
		if (model != null) {
			model.removeListDataListener(this);
		}
		model = m;
		model.addListDataListener(this);
	}

	public void addRepaintListener(RepaintListener l) {
		listeners.add(l);
	}
	
	public void removeRepaintListener(RepaintListener l) {
		listeners.remove(l);
	}

	public void addElement(MapElement element) {
		element.setMapCanvas(this);
		if (element instanceof Grid) grid = (Grid)element;
		int pos = 0;
		switch (element.getDefaultOrder()) {
		case Top:
			pos = 0;
			break;
		case AboveGrid:
			pos = getIndexOf(grid);
			break;
		case BelowGrid:
			pos = getIndexOf(grid)+1;
			break;
		case Bottom:
			pos = model.getSize();
		}
		((DefaultListModel)model).add(pos, element);
	}
	
	protected int getIndexOf(MapElement el) {
		for (int i = 0; i < model.getSize(); i++) {
			if (model.getElementAt(i) == grid) return i;
		}
		return 0;
	}
	
	public void paint(Graphics2D g) {
		Rectangle bounds = g.getClipBounds();
		g.clearRect(bounds.x, bounds.y, bounds.width, bounds.height);
		for (int i = model.getSize()-1; i >= 0; i--) {
			MapElement r = (MapElement)model.getElementAt(i);
			if (r != null) {
				//System.out.println("Painting "+r);
				r.paint(g);
			}
		}
	}

	public void repaint() {
		for(RepaintListener l : listeners) {
			l.repaint();
		}
	}
	
	public int getColumnWidth() {
		return 25400/294;
	}

	public int getRowHeight() {
		return 25400/294;
	}
	
	/**
	 * Get the integer grid coordinates of the grid cell containing (x,y) 
	 * @param x the pixel's x coordinate
	 * @param y the pixel's y coordinate
	 * @return a Point containing the grid coordinates
	 */
	public Point getGridCellCoordinates(int x, int y) {
		int col = x * 294 / 25400;
		int row = y * 294 / 25400;
		return new Point(col,row);
	}

	/**
	 * Get the precise (potentially fractional) grid coordinates of the pixel (x,y)
	 * @param x the pixel's x coordinate 
	 * @param y the pixel's y coordinate
	 * @return a Point2D.Double containing the grid coordinates
	 */
	public Point2D getGridCoordinates(int x, int y) {
		double col = (double)x * 294 / 25400;
		double row = (double)y * 294 / 25400;
		return new Point2D.Double(col,row);
	}
	
	/**
	 * Update the supplied Point with the pixel coordinates of the top left corner of the grid cell at (col,row)
	 * @param col the column number of the cell
	 * @param row the row number of the cell
	 * @param p the Point to store the coordinates in. If this is null then a new Point will be allocated and returned 
	 * @return a Point containing the coordinates. If p is not null then this will be p
	 */
	public Point getDisplayCoordinates(int col, int row, Point p) {
		if (p == null) p = new Point();
		p.x = col * 25400 / 294;
		p.y = row * 25400 / 294;
		return p;
	}

	/**
	 * Get the pixel coordinates of the top left corner of the grid cell at (col,row)
	 * @param col the column number of the cell
	 * @param row the row number of the cell 
	 * @return a Point containing the pixel coordinates of the top left corner of the specified cell
	 */
	public Point getDisplayCoordinates(int col, int row) {
		return getDisplayCoordinates(col, row, null);
	}

	/**
	 * Gets the pixel coordinates of the grid point p 
	 * @param p the point
	 * @return a new Point containing the pixel coordinates corresponding the grid point p
	 */
	public Point getDisplayCoordinates(Point2D p) {
		int x = (int)(p.getX() * 25400 / 294);
		int y = (int)(p.getY() * 25400 / 294);
		return new Point(x, y);
	}
	
	public boolean removeElement(int id) {
		MapElement e = getElement(id);
		if (e != null) {
			return ((DefaultListModel)model).removeElement(e);
		}
		return false;
	}

	public MapElement getElement(int id) {
		for (int i = 0; i < model.getSize(); i++) {
			MapElement e = (MapElement)model.getElementAt(i);
			if (e.getID() == id) {
				return e;
			}
		}
		return null;
	}

	public void contentsChanged(ListDataEvent arg0) {
		repaint();
	}

	public void intervalAdded(ListDataEvent arg0) {
		repaint();
	}

	public void intervalRemoved(ListDataEvent arg0) {
		repaint();
	}
}
