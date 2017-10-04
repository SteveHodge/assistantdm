package gamesystem.core;

// Provides notification of changes to a value

public interface SimpleProperty<T> {
	public String getName();

	public PropertyCollection getParent();

	public void addPropertyListener(PropertyListener<T> l);

	public void removePropertyListener(PropertyListener<T> l);

	public T getValue();	// the current (possibly overriden) value of the property
}
