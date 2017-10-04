package gamesystem.core;

import java.util.EventListener;

public interface PropertyListener<T> extends EventListener {
	public void propertyChanged(Property<T> source, T oldValue);		// TODO probably should ditch oldValue
}
