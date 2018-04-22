package gamesystem.core;

// The simplest possible change event: it just indicates that specified property changed in some way

// FIXME can't seem use PropertyEvent directly in PropertySubclasses. The SimpleEvent class is a workaround. Try to find a proper solution
public class PropertyEvent {
	public final Property<?> source;

	public PropertyEvent(Property<?> src) {
		source = src;
	}
}
