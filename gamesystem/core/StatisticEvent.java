package gamesystem.core;

public class StatisticEvent extends PropertyEvent {
	public enum EventType {
		TOTAL_CHANGED, MODIFIER_ADDED, MODIFIER_REMOVED, OVERRIDE_ADDED, OVERRIDE_REMOVED
	}

	private EventType type;

	public StatisticEvent(Property<Integer> src, EventType type) {
		super(src);
		this.type = type;
	}

	public EventType getEventType() {
		return type;
	}
}
