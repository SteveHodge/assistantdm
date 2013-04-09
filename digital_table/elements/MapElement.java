package digital_table.elements;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;

import digital_table.server.MapCanvas;
import digital_table.server.MapCanvas.Order;
import digital_table.server.ScreenManager;

/*
 * Ideas for elements:
 * initiative order display (for DM as table or players as strip)
 * webpage
 * darkness mask
 * make existing elements editable? or have some sort of punch through element?
 * remote screen bounds - use for configuration
 * dice roller?
 * thrown object scatter?
 */

// TODO should all MapElement's have a position? if so then it should be implemented here. otherwise could move the dragging stuff to a subclass
// TODO move screenmanger stuff out of here. should be an interface to indicate an element can do popups - also allows more coordination between DisplayTable and element (such as popup sizing etc)
public abstract class MapElement implements Serializable {
	private static final long serialVersionUID = 1L;

	private static int nextID = 1;
	protected final int id;
	protected MapCanvas canvas = null;
	protected ScreenManager screenManager = null;
	protected boolean visible = false;

	protected final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

	protected MapElement() {
		id = nextID++;
	}

	// this should only be used by elements that implement separate remote and local class. it should be used
	// to synchroise the ids of the remote and local instances
	protected MapElement(int id) {
		this.id = id;
	}
	
	public void addPropertyChangeListener(PropertyChangeListener listener) {
		pcs.addPropertyChangeListener(listener);
	}

//	public void addPropertyChangeListener(String property, PropertyChangeListener listener) {
//		pcs.addPropertyChangeListener(property, listener);
//	}

	public void removePropertyChangeListener(PropertyChangeListener listener) {
		pcs.removePropertyChangeListener(listener);
	}

	public void setMapCanvas(MapCanvas m) {
		canvas = m;
	}

	public void setScreenMananger(ScreenManager m) {
		screenManager = m;
	}

	public int getID() {
		return id;
	}

	public abstract void paint(Graphics2D g);

	public String toString() {
		return super.toString() + " (ID "+id+")";
	}
	
	public void setProperty(String property, Object value) {
		System.out.println("Unknown property "+property);
	}

	public Object getProperty(String property) {
		return null;
	}

	public void setVisible(boolean visible) {
		if (this.visible != visible) {
			this.visible = visible;
			if (canvas != null) canvas.repaint();
		}
	}
	
	/**
	 * If this MapElement is draggable then this will return an object that identifies the aspect that is being dragged
	 * @param gridLocation location of the mouse in the grid when dragging commenced. Should be serialisable
	 * @return an Object that identifies what is being dragged
	 */
	public Object getDragTarget(Point2D gridLocation) {
		return null;
	}

	/**
	 * Get the location of the element in grid coordinates.
	 * Currently this is only used by the minimap dragging code. If a subclass returns true for isDraggable()
	 * then it must override and implement this method otherwise it does not need to.
	 * @param target the Object identifying the aspect of the element to get the location of
	 * @return a Point2D containing the location of the specified target. null if the target is not recognised
	 */
	public Point2D getLocation(Object target) {
		return null;
	}

	/**
	 * Set the location of the element in grid coordinates. Some elements may support integer positions - these
	 * element may round the coordinates in p however they choose.
	 * Currently this is only used by the minimap dragging code. If a subclass returns true for isDraggable()
	 * then it must override and implement this method otherwise it does not need to.
	 * @param target the Object identifying the aspect of the element to be moved
	 * @param p a Point2D specifying the new location for this element
	 */
	public void setLocation(Object target, Point2D p) {
	}

	public Order getDefaultOrder() {
		return Order.Bottom;
	}
	
	public static Color darken(Color c) {
		return (new Color(c.getRed()*3/5,c.getGreen()*3/5,c.getBlue()*3/5));
	}
	
	public static Color lighten(Color c) {
		return (new Color(c.getRed()/2+127,c.getGreen()/2+127,c.getBlue()/2+127));
	}
}