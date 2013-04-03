package digital_table.elements;

import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;

import digital_table.server.MapCanvas;
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
	
	public boolean isDraggable() {
		return false;
	}

	/**
	 * Get the location of the element in grid coordinates.
	 * Currently this is only used by the minimap dragging code. If a subclass returns true for isDraggable()
	 * then it must override and implement this method otherwise it does not need to.
	 * @return a Point2D containing the location of the element
	 */
	public Point2D getLocation() {
		return null;
	}

	/**
	 * Set the location of the element in grid coordinates. Some elements may support integer positions - these
	 * element may round the coordinates in p however they choose.
	 * Currently this is only used by the minimap dragging code. If a subclass returns true for isDraggable()
	 * then it must override and implement this method otherwise it does not need to.
	 * @param p a Point2D specifying the new location for this element
	 */
	public void setLocation(Point2D p) {
	}
}