package gamesystem.core;

// Provides notification of changes to a value. Properties are identified by a name and can be part of a hierarchy.

// Subinterfaces extend the functionality:
// OverridableProperty allows the normal value of a property to be temporarily changed
// SettableProperty provides a method to update the value of the property
// SettableBaseValue extends OverridableProperty to allow the base value to be set

public interface Property<T> {
	public String getName();

	public PropertyCollection getParent();

	public void addPropertyListener(PropertyListener l);

	public void removePropertyListener(PropertyListener l);

	public T getValue();	// the current (possibly overriden) value of the property
}
