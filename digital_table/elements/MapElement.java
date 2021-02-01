package digital_table.elements;

import java.awt.Color;
import java.awt.Graphics2D;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import digital_table.server.MapCanvas;
import digital_table.server.MeasurementLog;
import digital_table.server.ScreenManager;

// TODO should all MapElement's have a position? if so then it should be implemented here. otherwise could move the dragging stuff to a subclass
// TODO move screenmanger stuff out of here. should be an interface to indicate an element can do popups - also allows more coordination between DisplayTable and element (such as popup sizing etc)
public abstract class MapElement implements Serializable {
	static final Logger logger = Logger.getLogger(MapElement.class.getName());
//	{
//		logger.setLevel(Level.FINEST);
//	}

	private static final long serialVersionUID = 1L;

	public static final String PROPERTY_VISIBLE = "visible";
	public static final String PROPERTY_DRAGGING = "dragging";
	public static final String PROPERTY_LAYER = "layer";

	private static int nextID = 1;
	protected final int id;
	protected MapCanvas canvas = null;
	protected ScreenManager screenManager = null;
	protected Map<String, Property<?>> properties = new HashMap<>();
	protected Property<Visibility> visible = new Property<Visibility>(PROPERTY_VISIBLE, true, Visibility.HIDDEN, Visibility.class);
	protected Property<Boolean> dragging = new Property<>(PROPERTY_DRAGGING, true, false, Boolean.class);
	protected Property<Layer> layer = new Property<>(PROPERTY_LAYER, true, getDefaultLayer(), Layer.class);
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

	public enum Layer {
		INFORMATION("Information"), TEMPLATE("Template"), CHARACTER_TOKENS("Character Tokens"), OBFUSCATION("Map Obfuscation"), MONSTER_TOKENS("Monster Tokens"), GRID("Grid"), MAP_FOREGROUND(
				"Map Foreground"), MAP_BACKGROUND("Map Background"), BACKGROUND(
				"Background");

		@Override
		public String toString() {
			return name;
		}

		Layer(String name) {
			this.name = name;
		}

		private String name;
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

	public boolean isDragging() {
		if (parent == null) return dragging.getValue();
		return dragging.getValue() || parent.isDragging();
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

	public abstract void paint(Graphics2D g);

	public void paintElement(Graphics2D g) {
		lastPaintTime = 0;
		long startTime = System.nanoTime();
		paint(g);
		lastPaintTime = (System.nanoTime() - startTime) / 1000;
		if (lastPaintTime > worstPaintTime) worstPaintTime = lastPaintTime;
		totalPaintTime += lastPaintTime;
		numFrames++;
	}

	long lastPaintTime = 0;
	long totalPaintTime = 0;
	long worstPaintTime = 0;
	int numFrames = 0;

	@Override
	public String toString() {
		return super.toString() + " (ID " + id + ")";
	}

	public void setProperty(String property, Object value) {
		Property<?> prop = properties.get(property);
		if (prop != null) {
			prop.setValueUntyped(value);
		} else {
			System.out.println("Unknown property (set) " + property);
			//new Exception().printStackTrace();
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

	public Layer getDefaultLayer() {
		return Layer.BACKGROUND;
	}

	// returns the memory usage of the object. typically will be an approximation only including rasters allocated for images.
	// elements that use negligible memory can return 0.
	public MeasurementLog getMemoryUsage() {
		return null;
	}

	// return a string that identifies the element by type and by any user-supplied identifier (e.g. a label). Should not include the id.
	public abstract String getIDString();

	// returns the last paint timing of the object
	// elements that do not painting can return 0.
	public MeasurementLog getPaintTiming() {
		MeasurementLog m = new MeasurementLog(getIDString(), getID());
		m.last = lastPaintTime;
		m.average = totalPaintTime / numFrames;
		m.worst = worstPaintTime;
		return m;
	}

	public static Color darken(Color c) {
		if (c == null) return null;
		return (new Color(c.getRed() * 3 / 5, c.getGreen() * 3 / 5, c.getBlue() * 3 / 5));
	}

	public static Color lighten(Color c) {
		if (c == null) return null;
		return (new Color(c.getRed() / 2 + 127, c.getGreen() / 2 + 127, c.getBlue() / 2 + 127));
	}
}