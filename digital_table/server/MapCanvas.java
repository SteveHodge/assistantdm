package digital_table.server;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.ListModel;
import javax.swing.event.EventListenerList;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import digital_table.elements.Grid;
import digital_table.elements.Group;
import digital_table.elements.MapElement;

/*
 * Represents the whole map area - an infinite plane which can contain elements arranged in layers
 * 
 * note: this class implements coordinate conversion appropriate for 0.294 dot pitch monitors. subclass with
 * different scaling need to override getColumnWidth(), getColumnHeight(), and all get..Coordinate() methods
 */

// TODO reimplement models and clean up
// TODO probably should have root MapElement to avoid all the special cases

public class MapCanvas implements ListDataListener {
	private DefaultListModel model;
	private List<RepaintListener> listeners = new ArrayList<RepaintListener>();
	private Grid grid;	// used to position other element above or below the grid

	// xoffset and yoffset specify the number of grid squares that are added to grid coordinates during conversions
	// the grid square at grid coordinates (xoffset,yoffset) will be the top left square of the "standard" display
	protected int xoffset = 0;
	protected int yoffset = 0;

	public enum Order {
		TOP,		// use for popups, informational elements
		ABOVEGRID,	// use for creatures
		BELOWGRID,	// use for templates
		BOTTOM;		// use for background images
	}

	public MapCanvas() {
		setModel(new DefaultListModel());
	}

	public ListModel getModel() {
		return model;
	}

	public TreeModel getTreeModel() {
		return treeModel;
	}

	public void setModel(DefaultListModel m) {
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

	public void addElement(MapElement element, MapElement parent) {
		element.setMapCanvas(this);
		if (element instanceof Grid) grid = (Grid) element;
		int pos = 0;
		switch (element.getDefaultOrder()) {
		case TOP:
			pos = 0;
			break;
		case ABOVEGRID:
			pos = getIndexOf(grid);
			break;
		case BELOWGRID:
			pos = getIndexOf(grid) + 1;
			break;
		case BOTTOM:
			pos = model.getSize();
		}
		model.add(pos, element);
		if (parent != null && parent instanceof Group) {
			((Group) parent).addChild(element);
		}
		treeModel.fireTreeNodeInserted(element);
	}

	public void changeParent(MapElement element, MapElement parent) {
		// TODO finer grained events would be better
		if (element.parent != null) {
			Group p = element.parent;
			p.removeChild(element);
			treeModel.fireTreeStructureChanged();
		}
		if (parent != null && parent instanceof Group) {
			((Group) parent).addChild(element);
			treeModel.fireTreeStructureChanged();
		}
		repaint();	// need this at the moment because changing the hierarchy can move the nodes
	}

	private void removeChildren(Group parent) {
		for (int i = 0; i < model.getSize(); i++) {
			MapElement el = (MapElement) model.get(i);
			if (el.getParent() == parent) {
				if (el instanceof Group) removeChildren((Group) el);
				parent.removeChild(el);
				model.removeElement(el);
			}
		}
	}

	public boolean removeElement(int id) {
		MapElement e = getElement(id);
		if (e != null) {
			Group parent = e.getParent();
			int index = treeModel.getIndexOfChild(parent, e);
			boolean removed = model.removeElement(e);
			if (removed) {
				if (e instanceof Group) removeChildren((Group) e);
				if (parent != null) {
					parent.removeChild(e);
				}
				treeModel.fireTreeNodeRemoved(e, parent, index);
			}
			return removed;
		}
		return false;
	}

	public void promoteElement(MapElement e) {
		int index = model.indexOf(e);
		if (index < 1) return;
		model.removeElement(e);
		model.add(index - 1, e);
		treeModel.fireTreeStructureChanged(null);
	}

	public void demoteElement(MapElement e) {
		int index = model.indexOf(e);
		if (index < 0 || index == model.getSize() - 1) return;
		model.removeElement(e);
		model.add(index + 1, e);
		treeModel.fireTreeStructureChanged(null);
	}

	public MapElement getElement(int id) {
		for (int i = 0; i < model.getSize(); i++) {
			MapElement e = (MapElement) model.getElementAt(i);
			if (e.getID() == id) return e;
		}
		return null;
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
		for (int i = model.getSize() - 1; i >= 0; i--) {
			MapElement r = (MapElement) model.getElementAt(i);
			if (r != null) {
				// get ancestor's relative position
				Point2D offset = new Point2D.Double();
				Group parent = r.getParent();
				while (parent != null) {
					offset = parent.translate(offset);
					parent = parent.getParent();
				}
				// we need to adjust the offset because this element's paint will add the parent's position
				// to it's own which will result in the offset being applied twice.
				offset.setLocation(offset.getX() + xoffset, offset.getY() + yoffset);	// TODO this is hacky - elements should not sum pixel positions - they should sum grid position and then convert
				r.paint(g, offset);
			}
		}
	}

	public void repaint() {
		for (RepaintListener l : listeners) {
			l.repaint();
		}
	}

	// ----------------------- coordinate conversion related methods -----------------------
	public void setOffset(int offx, int offy) {
		xoffset = offx;
		yoffset = offy;
		repaint();
	}

	public int getXOffset() {
		return xoffset;
	}

	public int getYOffset() {
		return yoffset;
	}

	// the getResolution... methods are used in coordinate conversions. subclasses can override these
	// methods rather than reimplementing the entire set of coordinate conversion methods.
	// the default implementations give a resolution of one inch per grid cell on a 0.294 mm dot pitch monitor:
	// (25400 mm per 100 inches / 294 mm per 100 dots = 86.39 dots per inch)
	private final static int RESOLUTION_NUMERATOR = 25400;
	private final static int RESOLUTION_DENOMINATOR = 294;

	protected int getResolutionNumeratorX() {
		return RESOLUTION_NUMERATOR;
	}

	protected int getResolutionDenominatorX() {
		return RESOLUTION_DENOMINATOR;
	}

	protected int getResolutionNumeratorY() {
		return RESOLUTION_NUMERATOR;
	}

	protected int getResolutionDenominatorY() {
		return RESOLUTION_DENOMINATOR;
	}

	// this should be used for calculating the size of element features that are relative to the grid size (e.g. font
	// sizes, line thicknesses, etc). it should not be used for calculating sizes of features that need to align to the
	// grid (as it returns an integer approximation of the true grid cell width)
	public int getColumnWidth() {
		return getResolutionNumeratorX() / getResolutionDenominatorX();
	}

	// this should be used for calculating the size of element features that are relative to the grid size (e.g. font
	// sizes, line thicknesses, etc). it should not be used for calculating sizes of features that need to align to the
	// grid (as it returns an integer approximation of the true grid cell width)
	public int getRowHeight() {
		return getResolutionNumeratorY() / getResolutionDenominatorY();
	}

	// TODO implement top-left-bottom-right versions of the get...Dimension() methods. could convert the existing methods to top-left-width-height

	// returns the precise size in grid units of the rectangle defined by the supplied coordinates (which are in the
	// coordinate system of the remote display)
//	public Dimension2D getRemoteGridDimension(int left, int top, int right, int bottom) {
//	}

	public Dimension getDisplayDimension(double width, double height) {
		int w = (int) (width * getResolutionNumeratorX() / getResolutionDenominatorX());
		int h = (int) (height * getResolutionNumeratorY() / getResolutionDenominatorY());
		return new Dimension(w, h);
	}

	// returns the precise size in grid units of the rectangle located at (0,0) with the specified width and height
	// (which are in the coordinate system of the remote display). note that this may give imprecise results due to
	// round in the coordinate conversions
	// TODO should probably return a Dimension2D. and it should be a method on the remote display
	public Point2D getRemoteGridDimension(int width, int height) {
		double col = (double) width * RESOLUTION_DENOMINATOR / RESOLUTION_NUMERATOR;
		double row = (double) height * RESOLUTION_DENOMINATOR / RESOLUTION_NUMERATOR;
		return new Point2D.Double(col, row);
	}

//	private static class Dimension2DDouble extends Dimension2D {
//		private double width, height;
//
//		Dimension2DDouble(double w, double h) {
//			setSize(w, h);
//		}
//
//		@Override
//		public double getHeight() {
//			return height;
//		}
//
//		@Override
//		public double getWidth() {
//			return width;
//		}
//
//		@Override
//		public void setSize(double w, double h) {
//			width = w;
//			height = h;
//		}
//
//	}

	// TODO this should probably accessed through the DisplayManager
	/**
	 * Get the precise (potentially fractional) grid coordinates of the pixel (x,y) where the pixel coordinates
	 * are in the coordinate system of the remote display.
	 * 
	 * @param x
	 *            the pixel's x coordinate
	 * @param y
	 *            the pixel's y coordinate
	 * @return a Point2D.Double containing the grid coordinates
	 */
	public Point2D getRemoteGridCoordinates(int x, int y) {
		double col = (double) x * RESOLUTION_DENOMINATOR / RESOLUTION_NUMERATOR;
		double row = (double) y * RESOLUTION_DENOMINATOR / RESOLUTION_NUMERATOR;
		return new Point2D.Double(col + xoffset, row + yoffset);
	}

	/**
	 * Get the precise (potentially fractional) grid coordinates of the pixel (x,y)
	 * 
	 * @param x
	 *            the pixel's x coordinate
	 * @param y
	 *            the pixel's y coordinate
	 * @return a Point2D.Double containing the grid coordinates
	 */
	public Point2D getGridCoordinates(int x, int y) {
		double col = (double) x * getResolutionDenominatorX() / getResolutionNumeratorX();
		double row = (double) y * getResolutionDenominatorY() / getResolutionNumeratorY();
		return new Point2D.Double(col + xoffset, row + yoffset);
	}

	/**
	 * Update the supplied Point with the pixel coordinates of the top left corner of the grid cell at (col,row)
	 * 
	 * @param col
	 *            the column number of the cell
	 * @param row
	 *            the row number of the cell
	 * @param p
	 *            the Point to store the coordinates in. If this is null then a new Point will be allocated and returned
	 * @return a Point containing the coordinates. If p is not null then this will be p
	 */
	public Point getDisplayCoordinates(int col, int row, Point p) {
		if (p == null) p = new Point();
		p.x = (col - xoffset) * getResolutionNumeratorX() / getResolutionDenominatorX();
		p.y = (row - yoffset) * getResolutionNumeratorY() / getResolutionDenominatorY();
		return p;
	}

	/**
	 * Get the pixel coordinates of the top left corner of the grid cell at (col,row)
	 * 
	 * @param col
	 *            the column number of the cell
	 * @param row
	 *            the row number of the cell
	 * @return a Point containing the pixel coordinates of the top left corner of the specified cell
	 */
	public Point getDisplayCoordinates(int col, int row) {
		return getDisplayCoordinates(col, row, null);
	}

	/**
	 * Gets the pixel coordinates of the grid point p
	 * 
	 * @param p
	 *            the point
	 * @return a new Point containing the pixel coordinates corresponding the grid point p
	 */
	public Point getDisplayCoordinates(Point2D p) {
		int x = (int) ((p.getX() - xoffset) * getResolutionNumeratorX() / getResolutionDenominatorX());
		int y = (int) ((p.getY() - yoffset) * getResolutionNumeratorY() / getResolutionDenominatorY());
		return new Point(x, y);
	}

	// ----------------------- tree model related methods -----------------------

	@Override
	public void contentsChanged(ListDataEvent arg0) {
		repaint();
	}

	@Override
	public void intervalAdded(ListDataEvent arg0) {
		repaint();
	}

	@Override
	public void intervalRemoved(ListDataEvent arg0) {
		repaint();
	}

	// TODO probably should make ElementTree top level class and expose these methods in the model
	public void updateTreeNode(MapElement element) {
		treeModel.fireTreeNodeChanged(element);
	}

	public TreePath getTreePath(MapElement node) {
		return treeModel.getPathTo(node);
	}

	private ElementTree treeModel = new ElementTree();

	private class ElementTree implements TreeModel {
		private EventListenerList listeners = new EventListenerList();

		@Override
		public Object getRoot() {
			return MapCanvas.this;
		}

		@Override
		public int getChildCount(Object parent) {
			if (parent == MapCanvas.this) parent = null;
			int count = 0;
			for (int i = 0; i < model.getSize(); i++) {
				MapElement e = (MapElement) model.get(i);
				if (e.getParent() == parent) count++;
			}
			return count;
		}

		@Override
		public MapElement getChild(Object parent, int index) {
			if (parent == MapCanvas.this) parent = null;
			int count = 0;
			for (int i = 0; i < model.getSize(); i++) {
				MapElement e = (MapElement) model.get(i);
				if (e.getParent() == parent) {
					if (count++ == index) return e;
				}
			}
			return null;
		}

		@Override
		public int getIndexOfChild(Object parent, Object child) {
			if (parent == MapCanvas.this) parent = null;
			int count = 0;
			for (int i = 0; i < model.getSize(); i++) {
				MapElement e = (MapElement) model.get(i);
				if (e.getParent() == parent) {
					if (e == child) return count;
					count++;
				}
			}
			return -1;
		}

		@Override
		public boolean isLeaf(Object node) {
			if (node == MapCanvas.this) return false;
			for (int i = 0; i < model.getSize(); i++) {
				MapElement e = (MapElement) model.get(i);
				if (e.getParent() == node) return false;
			}
			return true;
		}

		public TreePath getPathTo(Object node) {
			List<Object> ancestry = new ArrayList<Object>();
			if (node == MapCanvas.this) node = null;
			while (node != null) {
				ancestry.add(0, node);
				node = ((MapElement) node).getParent();
			}
			ancestry.add(0, MapCanvas.this);
			Object[] path = ancestry.toArray();
			return new TreePath(path);
		}

		@Override
		public void addTreeModelListener(TreeModelListener e) {
			listeners.add(TreeModelListener.class, e);
		}

		@Override
		public void removeTreeModelListener(TreeModelListener e) {
			listeners.remove(TreeModelListener.class, e);
		}

		void fireTreeNodeInserted(MapElement node) {
			Object[] list = listeners.getListenerList();
			for (int i = list.length - 2; i >= 0; i -= 2) {
				if (list[i] == TreeModelListener.class) {
					TreePath path = getPathTo(node.getParent());
					int[] indices = new int[1];
					indices[0] = getIndexOfChild(node.getParent() == null ? MapCanvas.this : node.getParent(), node);
					Object[] children = new Object[1];
					children[0] = node;
					TreeModelEvent e = new TreeModelEvent(this, path, indices, children);
					((TreeModelListener) list[i + 1]).treeNodesInserted(e);
				}
			}
		}

		void fireTreeNodeRemoved(MapElement node, MapElement parent, int index) {
			Object[] list = listeners.getListenerList();
			for (int i = list.length - 2; i >= 0; i -= 2) {
				if (list[i] == TreeModelListener.class) {
					TreePath path = getPathTo(parent);
					int[] indices = new int[1];
					indices[0] = index;
					Object[] children = new Object[1];
					children[0] = node;
					TreeModelEvent e = new TreeModelEvent(this, path, indices, children);
					((TreeModelListener) list[i + 1]).treeNodesRemoved(e);
				}
			}
		}

		void fireTreeNodeChanged(MapElement node) {
			Object[] list = listeners.getListenerList();
			for (int i = list.length - 2; i >= 0; i -= 2) {
				if (list[i] == TreeModelListener.class) {
					TreePath path = getPathTo(node.getParent());
					int[] indices = new int[1];
					indices[0] = getIndexOfChild(node.getParent() == null ? MapCanvas.this : node.getParent(), node);
					Object[] children = new Object[1];
					children[0] = node;
					TreeModelEvent e = new TreeModelEvent(this, path, indices, children);
					((TreeModelListener) list[i + 1]).treeNodesChanged(e);
				}
			}
		}

		void fireTreeStructureChanged(MapElement node) {
			Object[] list = listeners.getListenerList();
			for (int i = list.length - 2; i >= 0; i -= 2) {
				if (list[i] == TreeModelListener.class) {
					TreePath path = getPathTo(node);
					TreeModelEvent e = new TreeModelEvent(this, path);
					((TreeModelListener) list[i + 1]).treeStructureChanged(e);
				}
			}
		}

		void fireTreeStructureChanged() {
			Object[] list = listeners.getListenerList();
			for (int i = list.length - 2; i >= 0; i -= 2) {
				if (list[i] == TreeModelListener.class) {
					TreePath path = new TreePath(getRoot());
					TreeModelEvent e = new TreeModelEvent(this, path);
					((TreeModelListener) list[i + 1]).treeStructureChanged(e);
				}
			}
		}

		@Override
		public void valueForPathChanged(TreePath path, Object newValue) {
			// TODO not sure if i need implementation
		}

//		void printTree() {
//			for (int i = 0; i < model.size(); i++) {
//				MapElement e = (MapElement) model.get(i);
//				if (e.parent == null) {
//					printSubtree(e, "+-");
//				}
//			}
//		}
//
//		void printSubtree(Object node, String prefix) {
//			System.out.println(prefix + node.toString() + " (" + getChildCount(node) + ")");
//			for (int i = 0; i < getChildCount(node); i++) {
//				printSubtree(getChild(node, i), "  " + prefix);
//			}
//		}
	};
}
