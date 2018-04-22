package gamesystem.core;

// A PropertyEvent that also provides the previous value of the Property

public class OverridablePropertyEvent<T> extends PropertyEvent {
	public enum EventType {
		REGULAR_VALUE_CHANGED,	// the current value (from getValue()) will not have changed if there is an override in place
		OVERRIDE_ADDED, OVERRIDE_REMOVED
	}

	EventType type;
	T old;

	public OverridablePropertyEvent(Property<T> src, EventType type, T old) {
		super(src);
		this.type = type;
		this.old = old;
	}

	public T getOldValue() {
		return old;
	}

	public EventType getEventType() {
		return type;
	}
}
