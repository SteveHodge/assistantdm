package digital_table.elements;

import java.awt.Color;
import java.awt.Graphics2D;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

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

	public static final String PROPERTY_VISIBLE = "visible";
	
	private static int nextID = 1;
	protected final int id;
	protected MapCanvas canvas = null;
	protected ScreenManager screenManager = null;
	protected Map<String,Property<?>> properties = new HashMap<String,Property<?>>();
	protected Property<Boolean> visible = new Property<Boolean>(PROPERTY_VISIBLE,true,false);

	protected final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

	protected class Property<T> implements Serializable {
		private static final long serialVersionUID = 1L;

		String name;
		T value;
		boolean visible;
		
		public Property(String name, T defaultValue) {
			this(name, true, defaultValue);
		}

		public Property(String name, boolean visible, T defaultValue) {
			this.name = name;
			this.visible = visible;
			value = defaultValue;
			properties.put(name, this);
		}
		
		public void setValue(T v) {
			if (value.equals(v)) return;
			T old = value;
			value = v;
			pcs.firePropertyChange(name, old, value);
			if (visible && canvas != null) canvas.repaint();
		}

		public T getValue() {
			return value;
		}
		
		public String toString() {
			return value.toString();
		}
	};
	
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
	
	public <T> void setProperty(String property, T value) {
		@SuppressWarnings("unchecked")
		Property<T> prop = (Property<T>)properties.get(property);
		if (prop != null) {
			prop.setValue(value);
		} else {
			System.out.println("Unknown property "+property);
		}
	}

	public Object getProperty(String property) {
		Property<?> prop = properties.get(property);
		if (prop != null) {
			return prop.getValue();
		} else {
			System.out.println("Unknown property "+property);
			return null;
		}
	}

	public Order getDefaultOrder() {
		return Order.BOTTOM;
	}
	
	public static Color darken(Color c) {
		return (new Color(c.getRed()*3/5,c.getGreen()*3/5,c.getBlue()*3/5));
	}
	
	public static Color lighten(Color c) {
		return (new Color(c.getRed()/2+127,c.getGreen()/2+127,c.getBlue()/2+127));
	}
}