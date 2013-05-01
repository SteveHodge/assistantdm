package digital_table.server;

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

	public enum Order {
		TOP,		// use for popups, informational elements
		ABOVEGRID,	// use for creatures
		BELOWGRID,	// use for templates
		BOTTOM;		// use for backgrounds images
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

	// TODO decide on standard argument - id or MapElement
	public boolean removeElement(int id) {
		MapElement e = getElement(id);
		if (e != null) {
			Group parent = e.getParent();
			int index = treeModel.getIndexOfChild(parent, e);
			boolean removed = model.removeElement(e);
			if (removed) {
				if (parent != null) {
					parent.removeChild(e);
				}
				boolean reparented = false;
				if (e instanceof Group) {
					for (int i = 0; i < model.getSize(); i++) {
						MapElement el = (MapElement) model.get(i);
						if (el.getParent() == e) {
							reparented = true;
							parent.addChild(el);
							// TODO should also adjust offsets
						}
					}
				}
				if (reparented) {
					treeModel.fireTreeStructureChanged(parent);
				} else {
					treeModel.fireTreeNodeRemoved(e, parent, index);
				}
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
				//System.out.println("Painting "+r);
				// get offset
				Point2D offset = new Point2D.Double();
				Group parent = r.getParent();
				while (parent != null) {
					offset = parent.translate(offset);
					parent = parent.getParent();
				}
				r.paint(g, offset);
			}
		}
	}

	public void repaint() {
		for (RepaintListener l : listeners) {
			l.repaint();
		}
	}

	public int getColumnWidth() {
		return 25400 / 294;
	}

	public int getRowHeight() {
		return 25400 / 294;
	}

	/**
	 * Get the precise (potentially fractional) grid coordinates of the pixel (x,y) where the pixel coordinates
	 * are in the coordinate system of the remote display
	 * 
	 * @param x
	 *            the pixel's x coordinate
	 * @param y
	 *            the pixel's y coordinate
	 * @return a Point2D.Double containing the grid coordinates
	 */
	public Point2D getRemoteGridCellCoords(int x, int y) {
		double col = (double) x * 294 / 25400;
		double row = (double) y * 294 / 25400;
		return new Point2D.Double(col, row);
	}

	/**
	 * Get the integer grid coordinates of the grid cell containing (x,y)
	 * 
	 * @param x
	 *            the pixel's x coordinate
	 * @param y
	 *            the pixel's y coordinate
	 * @return a Point containing the grid coordinates
	 */
	public Point getGridCellCoordinates(int x, int y) {
		int col = x * 294 / 25400;
		int row = y * 294 / 25400;
		return new Point(col, row);
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
		return getRemoteGridCellCoords(x, y);
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
		p.x = col * 25400 / 294;
		p.y = row * 25400 / 294;
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
		int x = (int) (p.getX() * 25400 / 294);
		int y = (int) (p.getY() * 25400 / 294);
		return new Point(x, y);
	}

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

		@Override
		public void valueForPathChanged(TreePath path, Object newValue) {
			// TODO not sure if i need implementation
		}
	};
}
