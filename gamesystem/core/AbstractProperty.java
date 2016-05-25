package gamesystem.core;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import javax.swing.event.EventListenerList;

/*
 * Abstract property implementation
 */

abstract public class AbstractProperty<T> implements Property<T> {
	protected List<PropertyValue<T>> overrides;
	EventListenerList listenerList;

	// TODO name
	// TODO hierarchy stuff

	@Override
	abstract public T getBaseValue();

	@Override
	public T getValue() {
		if (hasOverride()) return overrides.get(overrides.size() - 1).value;
		return getBaseValue();
	}

	@Override
	public PropertyValue<T> addOverride(T overrideVal) {
		T old = getValue();
		PropertyValue<T> v = new PropertyValue<>(overrideVal);
		if (overrides == null) overrides = new ArrayList<>();
		overrides.add(v);
		firePropertyChanged(old, true);
		firePropertyChanged(old, false);	// value also changed
		return v;
	}

	@Override
	public void removeOverride(PropertyValue<T> key) {
		T old = getValue();
		if (overrides.remove(key)) {
			firePropertyChanged(old, true);
			firePropertyChanged(old, false);	// value also changed
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
		if (listenerList == null) listenerList = new EventListenerList();
		listenerList.add(PropertyListener.class, l);
	}

	@Override
	public void removePropertyListener(PropertyListener<T> l) {
		if (listenerList == null) listenerList = new EventListenerList();
		listenerList.remove(PropertyListener.class, l);
	}

	@SuppressWarnings("unchecked")
	protected void firePropertyChanged(T oldVal, boolean compChange) {
		if (listenerList == null) return;
		Object[] listeners = listenerList.getListenerList();
		PropertyEvent<T> event = null;
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == PropertyListener.class) {
				if (event == null) event = new PropertyEvent<>(this, oldVal);
				if (compChange) {
					((PropertyListener<T>) listeners[i + 1]).compositionChanged(event);
				} else if (getValue() != oldVal) {
					((PropertyListener<T>) listeners[i + 1]).valueChanged(event);
				}
			}
		}
	}
}
