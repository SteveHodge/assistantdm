package gamesystem.core;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

/*
 * Abstract property implementation
 */

abstract public class AbstractProperty<T> implements Property<T> {
	protected List<PropertyValue<T>> overrides;
	protected String name;
	protected PropertyCollection parent;

	public AbstractProperty(String name, PropertyCollection parent) {
		this.name = name;
		this.parent = parent;
		parent.addProperty(this);
	}

	@Override
	abstract public T getBaseValue();

	@Override
	public T getValue() {
		if (hasOverride()) return overrides.get(overrides.size() - 1).value;
		return getBaseValue();
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public PropertyCollection getParent() {
		return parent;
	}

	@Override
	public PropertyValue<T> addOverride(T overrideVal) {
		T old = getValue();
		PropertyValue<T> v = new PropertyValue<>(overrideVal);
		if (overrides == null) overrides = new ArrayList<>();
		overrides.add(v);
		fireEvent(old);
		return v;
	}

	@Override
	public void removeOverride(PropertyValue<T> key) {
		T old = getValue();
		if (overrides.remove(key)) {
			fireEvent(old);
		} else {
			throw new NoSuchElementException();
		}
	}

	@Override
	public boolean hasOverride() {
		return overrides != null && overrides.size() > 0;
	}

	// returns all values in order (base value then each override in the order they were added)
	@Override
	public List<T> getValues() {
		List<T> values = new ArrayList<>();
		values.add(getBaseValue());
		if (overrides != null) {
			for (PropertyValue<T> v : overrides) {
				values.add(v.value);
			}
		}
		return values;
	}

	@Override
	public void addPropertyListener(PropertyListener<T> l) {
		parent.addPropertyListener(this, l);
	}

	@Override
	public void removePropertyListener(PropertyListener<T> l) {
		parent.removePropertyListener(this, l);
	}

	protected void fireEvent() {
		// XXX perhaps shouldn't allow update events with no old value
		parent.fireEvent(this, null);
	}

	protected void fireEvent(T old) {
		parent.fireEvent(this, old);
	}

//	public static int listenerCount = 0;
//
//	public static int listenerLists = 0;
//	public static int propertyCount = 0;
//	{
//		propertyCount++;
//	}
//
//	@Override
//	public void addPropertyListener(PropertyListener<T> l) {
//		if (listenerList == null) {
//			listenerList = new EventListenerList();
//			listenerLists++;
//		}
//		listenerList.add(PropertyListener.class, l);
//		listenerCount++;
//	}
//
//	@Override
//	public void removePropertyListener(PropertyListener<T> l) {
//		if (listenerList == null) listenerList = new EventListenerList();
//		listenerList.remove(PropertyListener.class, l);
//	}
//
//	@SuppressWarnings("unchecked")
//	protected void firePropertyChanged(T oldVal, boolean compChange) {
//		if (listenerList == null) return;
//		Object[] listeners = listenerList.getListenerList();
//		PropertyEvent<T> event = null;
//		for (int i = listeners.length - 2; i >= 0; i -= 2) {
//			if (listeners[i] == PropertyListener.class) {
//				if (event == null) event = new PropertyEvent<>(this, oldVal);
//				if (compChange) {
//					((PropertyListener<T>) listeners[i + 1]).compositionChanged(event);
//				} else if (getValue() != oldVal) {
//					((PropertyListener<T>) listeners[i + 1]).valueChanged(event);
//				}
//			}
//		}
//	}
}
