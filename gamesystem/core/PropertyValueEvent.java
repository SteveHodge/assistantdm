package gamesystem.core;

// A PropertyEvent that also provides the previous value of the Property

public class PropertyValueEvent<T> extends PropertyEvent {
	T old;

	public PropertyValueEvent(Property<T> src, T old) {
		super(src);
		this.old = old;
	}

	public T getOldValue() {
		return old;
	}
}
