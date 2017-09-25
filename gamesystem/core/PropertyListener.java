package gamesystem.core;

import java.util.EventListener;

public interface PropertyListener<T> extends EventListener {
	public void propertyChanged(Property<T> source, PropertyEventType type, T oldValue, T newValue);		// TODO fix up type safety
}
