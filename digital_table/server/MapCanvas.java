package digital_table.server;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.event.EventListenerList;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import digital_table.elements.Group;
import digital_table.elements.MapElement;
import digital_table.elements.MapElement.Layer;

/*
 * Represents the whole map area - an infinite plane which can contain elements arranged in layers
 *
 * note: this class implements coordinate conversion appropriate for 0.294 dot pitch monitors. subclass with
 * different scaling need to override getColumnWidth(), getColumnHeight(), and all get..Coordinate() methods
 */

// TODO reimplement models and clean up
// TODO probably should have root MapElement to avoid all the special cases

public class MapCanvas implements CoordinateConverter, Iterable<MapElement> {
	private Map<Layer, List<MapElement>> layers = new HashMap<>();
	private List<RepaintListener> listeners = new ArrayList<>();

	// xoffset and yoffset specify the number of grid squares that are added to grid coordinates during conversions
	// the grid square at grid coordinates (xoffset,yoffset) will be the top left square of the "standard" display
	protected int xoffset = 0;
	protected int yoffset = 0;

	public MapCanvas() {
		for (int i = 0; i < Layer.values().length; i++) {
			layers.put(Layer.values()[i], new ArrayList<>());
		}
	}

	@Override
	public Iterator<MapElement> iterator() {
		return new CanvasIterator();
	}

	private class CanvasIterator implements Iterator<MapElement>
	{
		int currentLayer;
		Iterator<MapElement> currentIterator;

		CanvasIterator() {
			currentLayer = 0;
			currentIterator = layers.get(Layer.values()[currentLayer]).iterator();
		}

		@Override
		public boolean hasNext() {
			while (!currentIterator.hasNext()) {
				// current iterator empty: move to next layer
				currentLayer++;
				if (currentLayer > Layer.values().length - 1) return false;
				currentIterator = layers.get(Layer.values()[currentLayer]).iterator();
			}
			return true;
		}

		@Override
		public MapElement next() {
			return currentIterator.next();
		}
	}

	public TreeModel getTreeModel() {
		return treeModel;
	}

	public void addRepaintListener(RepaintListener l) {
		listeners.add(l);
	}

	public void removeRepaintListener(RepaintListener l) {
		listeners.remove(l);
	}

	private List<MapElement> getLayerList(MapElement e) {
		Layer layer = (Layer) e.getProperty(MapElement.PROPERTY_LAYER);
		List<MapElement> list = layers.get(layer);
		return list;
	}

	public void addElement(MapElement element, MapElement parent) {
		element.setMapCanvas(this);
		if (parent != null && parent instanceof Group) {
			((Group) parent).addChild(element);
		}
		getLayerList(element).add(element);
		treeModel.fireTreeNodeInserted(element);
		repaint();
	}

	// assumes the current value of the layer property of element matches the layer list that contains element. this
	// method will update the property after moving the element to the correct layer list. so this method should be
	// called instead of changing the property on the element itself.
	// the element will be placed at the top of the new layer.
	public void changeLayer(MapElement element, Layer layer) {
		List<MapElement> oldList = getLayerList(element);
		List<MapElement> newList = layers.get(layer);
		if (oldList.remove(element)) {
			newList.add(0, element);
			element.setProperty(MapElement.PROPERTY_LAYER, layer);
			treeModel.fireTreeStructureChanged();
			repaint();
		}
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
		for (List<MapElement> list : layers.values()) {
			for (int i = list.size() - 1; i >= 0; i--) {
				MapElement el = list.get(i);
				if (el.getParent() == parent) {
					if (el instanceof Group) {
						removeChildren((Group) el);
						if (i >= list.size()) i = list.size() - 1;	// the recursion above can remove enough elements that i becomes out of range
					}
					parent.removeChild(el);
					list.remove(el);
				}
			}
		}
	}

	// note this assumes the layer property of the element matches the layer list it is stored in
	public boolean removeElement(int id) {
		MapElement e = getElement(id);
		if (e != null) {
			Group parent = e.getParent();
			int index = treeModel.getIndexOfChild(parent, e);
			List<MapElement> list = getLayerList(e);
			boolean removed = list.remove(e);
			if (removed) {
				if (e instanceof Group) removeChildren((Group) e);
				if (parent != null) {
					parent.removeChild(e);
				}
				treeModel.fireTreeNodeRemoved(e, parent, index);
			}
			repaint();
			return removed;
		}
		return false;
	}

	public void promoteElement(MapElement e) {
		List<MapElement> list = getLayerList(e);
		int index = list.indexOf(e);
		if (index < 1) return;
		list.remove(e);
		list.add(index - 1, e);
		treeModel.fireTreeStructureChanged(null);
		repaint();
	}

	public void demoteElement(MapElement e) {
		List<MapElement> list = getLayerList(e);
		int index = list.indexOf(e);
		if (index < 0 || index == list.size() - 1) return;
		list.remove(e);
		list.add(index + 1, e);
		treeModel.fireTreeStructureChanged(null);
		repaint();
	}

	public MapElement getElement(int id) {
		for (MapElement e : this) {
			if (e.getID() == id) return e;
		}
		return null;
	}

	public void paint(Graphics2D g) {
		lastPaintTime = 0;
		long startTime = System.nanoTime();

		Rectangle bounds = g.getClipBounds();
		g.clearRect(bounds.x, bounds.y, bounds.width, bounds.height);

		for (int l = Layer.values().length - 1; l >= 0; l--) {
			List<MapElement> list = layers.get(Layer.values()[l]);
			for (int i = list.size() - 1; i >= 0; i--) {
				MapElement r = list.get(i);
				if (r != null) r.paintElement(g);
			}
		}

		lastPaintTime = (System.nanoTime() - startTime) / 1000;
		if (lastPaintTime > worstPaintTime) worstPaintTime = lastPaintTime;
		totalPaintTime += lastPaintTime;
		numFrames++;
	}

	public Point2D getElementOrigin(MapElement el) {
		Point2D offset = new Point2D.Double(-xoffset, -yoffset);
		Group parent = el.getParent();
		while (parent != null) {
			offset = parent.translate(offset);
			parent = parent.getParent();
		}
		return offset;
	}

	public void repaint() {
		for (RepaintListener l : listeners) {
			l.repaint();
		}
	}

	public MeasurementLog getMemoryUsage() {
		List<MeasurementLog> logs = new ArrayList<>();
		for (MapElement e : this) {
			MeasurementLog l = e.getMemoryUsage();
			if (l != null) logs.add(l);
		}
		MeasurementLog total = new MeasurementLog("MapCanvas", hashCode(), logs.size());
		logs.toArray(total.components);
		total.updateTotal();
		return total;
	}

	long lastPaintTime = 0;
	long worstPaintTime = 0;
	long totalPaintTime = 0;
	int numFrames = 0;

	public MeasurementLog getPaintTiming() {
		List<MeasurementLog> logs = new ArrayList<>();
		for (MapElement e : this) {
			MeasurementLog l = e.getPaintTiming();
			if (l != null) logs.add(l);
		}
		MeasurementLog total = new MeasurementLog("MapCanvas", hashCode(), logs.size());
		logs.toArray(total.components);
		total.last = lastPaintTime;
		total.average = totalPaintTime / numFrames;
		total.worst = worstPaintTime;
		return total;
	}

	// ----------------------- coordinate conversion related methods -----------------------
	// returns a CoordinateConverter that can convert values expressed in remote display units (pixels) into remote
	// grid units. the default implementation simply returns 'this' (i.e. this MapCanvas already operates in remote
	// display units).
	// this is a bit hackish - the local MapCanvas shouldn't really need to know about the remote MapCanvas. however
	// the local MapCanvas is already passed to all methods that are interested in coordinate conversions so it is
	// the most convenient place at the moment
	// TODO consider refactoring to move this elsewhere
	public CoordinateConverter getRemote() {
		return this;
	}

	public void setOffset(int offx, int offy) {
		xoffset = offx;
		yoffset = offy;
		repaint();
	}

	@Override
	public int getXOffset() {
		return xoffset;
	}

	@Override
	public int getYOffset() {
		return yoffset;
	}

	// the getResolution... methods are used in coordinate conversions. subclasses can override these
	// methods rather than reimplementing the entire set of coordinate conversion methods.
	// the default implementations give a resolution of one inch per grid cell on a 0.294 mm dot pitch monitor:
	// (25400 mm per 100 inches / 294 mm per 100 dots = 86.39 dots per inch)
	public final static int RESOLUTION_NUMERATOR = 25400;
	public final static int RESOLUTION_DENOMINATOR = 294;

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

	// returns a Dimension representing the size on pixel units of a rectangle with the width and height specified (in grid
	// units).
	public Dimension getDisplayDimension(double width, double height) {
		int w = (int) (width * getResolutionNumeratorX() / getResolutionDenominatorX());
		int h = (int) (height * getResolutionNumeratorY() / getResolutionDenominatorY());
		return new Dimension(w, h);
	}

	// returns the grid coordinates corresponding to the display coordinates (x,y).
	// note that this may give imprecise results due to rounding in the coordinate conversions
	@Override
	public Point2D convertDisplayCoordsToGrid(int x, int y) {
		double col = (double) x * getResolutionDenominatorX() / getResolutionNumeratorX();
		double row = (double) y * getResolutionDenominatorY() / getResolutionNumeratorY();
		return new Point2D.Double(col, row);
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
	@Override
	public Point2D convertDisplayCoordsToCanvas(int x, int y) {
		double col = (double) x * getResolutionDenominatorX() / getResolutionNumeratorX();
		double row = (double) y * getResolutionDenominatorY() / getResolutionNumeratorY();
		return new Point2D.Double(col + xoffset, row + yoffset);
	}

	/**
	 * Update the supplied Point with the pixel coordinates of the top left corner of the grid cell at (col,row), adjusted for the canvas origin
	 *
	 * @param col
	 *            the column number of the cell
	 * @param row
	 *            the row number of the cell
	 * @param p
	 *            the Point to store the coordinates in. If this is null then a new Point will be allocated and returned
	 * @return a Point containing the coordinates. If p is not null then this will be p
	 */
	public Point convertCanvasCoordsToDisplay(int col, int row, Point p) {
		if (p == null) p = new Point();
		p.x = (col - xoffset) * getResolutionNumeratorX() / getResolutionDenominatorX();
		p.y = (row - yoffset) * getResolutionNumeratorY() / getResolutionDenominatorY();
		return p;
	}

	/**
	 * Get the pixel coordinates of the top left corner of the grid cell at (col,row), adjusted for the canvas origin
	 *
	 * @param col
	 *            the column number of the cell
	 * @param row
	 *            the row number of the cell
	 * @return a Point containing the pixel coordinates of the top left corner of the specified cell
	 */
	public Point convertCanvasCoordsToDisplay(int col, int row) {
		return convertCanvasCoordsToDisplay(col, row, null);
	}

	/**
	 * Gets the pixel coordinates of the grid point p, adjusted for the canvas origin
	 *
	 * @param p
	 *            the point
	 * @return a new Point containing the pixel coordinates corresponding the grid point p
	 */
	public Point convertCanvasCoordsToDisplay(Point2D p) {
		int x = (int) ((p.getX() - xoffset) * getResolutionNumeratorX() / getResolutionDenominatorX());
		int y = (int) ((p.getY() - yoffset) * getResolutionNumeratorY() / getResolutionDenominatorY());
		return new Point(x, y);
	}

	/**
	 * Update the supplied Point with the pixel coordinates of the top left corner of the grid cell at (col,row). Does not apply the canvas origin
	 *
	 * @param col
	 *            the column number of the cell
	 * @param row
	 *            the row number of the cell
	 * @param p
	 *            the Point to store the coordinates in. If this is null then a new Point will be allocated and returned
	 * @return a Point containing the coordinates. If p is not null then this will be p
	 */
	public Point convertGridCoordsToDisplay(int col, int row, Point p) {
		if (p == null) p = new Point();
		p.x = col * getResolutionNumeratorX() / getResolutionDenominatorX();
		p.y = row * getResolutionNumeratorY() / getResolutionDenominatorY();
		return p;
	}

	/**
	 * Get the pixel coordinates of the top left corner of the grid cell at (col,row). Does not apply the canvas origin
	 *
	 * @param col
	 *            the column number of the cell
	 * @param row
	 *            the row number of the cell
	 * @return a Point containing the pixel coordinates of the top left corner of the specified cell
	 */
	public Point convertGridCoordsToDisplay(int col, int row) {
		return convertGridCoordsToDisplay(col, row, null);
	}

	/**
	 * Gets the pixel coordinates of the grid point p. Does not apply the canvas origin
	 *
	 * @param p
	 *            the point
	 * @return a new Point containing the pixel coordinates corresponding the grid point p
	 */
	public Point convertGridCoordsToDisplay(Point2D p) {
		int x = (int) (p.getX() * getResolutionNumeratorX() / getResolutionDenominatorX());
		int y = (int) (p.getY() * getResolutionNumeratorY() / getResolutionDenominatorY());
		return new Point(x, y);
	}

	// ----------------------- tree model related methods -----------------------

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
			for (MapElement e : MapCanvas.this) {
				if (e.getParent() == parent) count++;
			}
			return count;
		}

		@Override
		public MapElement getChild(Object parent, int index) {
			if (parent == MapCanvas.this) parent = null;
			int count = 0;
			for (MapElement e : MapCanvas.this) {
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
			for (MapElement e : MapCanvas.this) {
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
			for (MapElement e : MapCanvas.this) {
				if (e.getParent() == node) return false;
			}
			return true;
		}

		public TreePath getPathTo(Object node) {
			List<Object> ancestry = new ArrayList<>();
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
			debug("fireTreeNodeInserted");
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
			debug("fireTreeNodeRemoved");
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
			debug("fireTreeNodeChanged");
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
			debug("fireTreeStructureChanged(" + node + ")");
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
			debug("fireTreeStructureChanged");
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

		void printTree(String title) {
			System.out.println("------------------- " + title + " ---------------------");
			System.out.println("Canvas: " + MapCanvas.this);
			System.out.println("Info layer: " + MapCanvas.this.layers.get(Layer.INFORMATION));
			for (MapElement e : MapCanvas.this) {
				if (e.parent == null) {
					printSubtree(e, "+-");
				}
			}
		}

		void printSubtree(MapElement node, String prefix) {
			System.out.println(prefix + node.toString() + " layer: " + node.getProperty(MapElement.PROPERTY_LAYER) + (getChildCount(node) > 0 ? " children: " + getChildCount(node) : ""));
			for (int i = 0; i < getChildCount(node); i++) {
				printSubtree(getChild(node, i), "  " + prefix);
			}
		}
	};

	public void debug(String title) {
//		treeModel.printTree(title);
	}
}
