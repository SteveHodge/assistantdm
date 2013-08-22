package digital_table.elements;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import digital_table.server.MapCanvas;
import digital_table.server.MapCanvas.Order;
import digital_table.server.ScreenManager;

// TODO should all MapElement's have a position? if so then it should be implemented here. otherwise could move the dragging stuff to a subclass
// TODO move screenmanger stuff out of here. should be an interface to indicate an element can do popups - also allows more coordination between DisplayTable and element (such as popup sizing etc)
public abstract class MapElement implements Serializable {
	private static final long serialVersionUID = 1L;

	public static final String PROPERTY_VISIBLE = "visible";

	private static int nextID = 1;
	protected final int id;
	protected MapCanvas canvas = null;
	protected ScreenManager screenManager = null;
	protected Map<String, Property<?>> properties = new HashMap<String, Property<?>>();
	protected Property<Visibility> visible = new Property<Visibility>(PROPERTY_VISIBLE, true, Visibility.HIDDEN, Visibility.class);
	public Group parent = null;	// TODO should be private

	protected boolean selected = false;

	protected final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

	public enum Visibility {
		HIDDEN, FADED, VISIBLE;	// order is significant for the minimumVisibility method

		public static Visibility minimumVisibility(Visibility v1, Visibility v2) {
			int ord = Math.min(v1.ordinal(), v2.ordinal());
			return Visibility.values()[ord];
		}
	}

	protected class Property<T> implements Serializable {
		private static final long serialVersionUID = 1L;

		protected String name;
		protected T value;
		protected Class<T> typeToken;
		protected boolean repaint;

		public Property(String name, T defaultValue, Class<T> token) {
			this(name, true, defaultValue, token);
		}

		/**
		 * 
		 * @param name
		 *            Name of property
		 * @param repaint
		 *            if true then this element will be repainted when this property is set
		 * @param defaultValue
		 *            initial value
		 * @param token
		 *            class token
		 */
		public Property(String name, boolean repaint, T defaultValue, Class<T> token) {
			this.name = name;
			this.repaint = repaint;
			typeToken = token;
			value = defaultValue;
			properties.put(name, this);
		}

		public void setValue(T v) {
			if (value != null && value.equals(v)) return;
			T old = value;
			value = v;
			pcs.firePropertyChange(name, old, value);
			if (repaint && canvas != null) canvas.repaint();
		}

		public void setValueUntyped(Object v) {
			setValue(typeToken.cast(v));
		}

		public T getValue() {
			return value;
		}

		@Override
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

	public MapElement getElement(int id) {
		if (this.id == id) return this;
		return null;
	}

	public Group getParent() {
		return parent;
	}

	// returns true if this element and all ancestor elements are visible
	public Visibility getVisibility() {
		if (parent == null) return visible.getValue();
		return Visibility.minimumVisibility(visible.getValue(), parent.getVisibility());
	}

	public void setSelected(boolean sel) {
		if (sel != selected) {
			selected = sel;
			if (canvas != null) canvas.repaint();
		}
	}

	public abstract void paint(Graphics2D g, Point2D offset);

	@Override
	public String toString() {
		return super.toString() + " (ID " + id + ")";
	}

	public void setProperty(String property, Object value) {
		Property<?> prop = properties.get(property);
		if (prop != null) {
			prop.setValueUntyped(value);
		} else {
			//new Exception().printStackTrace();
			System.out.println("Unknown property (set) " + property);
		}
	}

	public Object getProperty(String property) {
		Property<?> prop = properties.get(property);
		if (prop != null) {
			return prop.getValue();
		} else {
			System.out.println("Unknown property (get) " + property);
			//new Exception().printStackTrace();
			return null;
		}
	}

	public Set<String> getProperties() {
		return properties.keySet();
	}

	// TODO perhaps this should be a static field
	public Order getDefaultOrder() {
		return Order.BOTTOM;
	}

	public static Color darken(Color c) {
		return (new Color(c.getRed() * 3 / 5, c.getGreen() * 3 / 5, c.getBlue() * 3 / 5));
	}

	public static Color lighten(Color c) {
		return (new Color(c.getRed() / 2 + 127, c.getGreen() / 2 + 127, c.getBlue() / 2 + 127));
	}
}