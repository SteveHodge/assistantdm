package gamesystem.core;

public interface PropertyCollection {
	public void addPropertyListener(String propName, PropertyListener<?> l);

	public <T> void addPropertyListener(Property<T> property, PropertyListener<T> l);

	public void removePropertyListener(String propName, PropertyListener<?> l);

	public <T> void removePropertyListener(Property<T> property, PropertyListener<T> l);

	public Property<?> getProperty(String name);

	// TODO move this stuff to another interface as it should only be called by Property objects:

	public <T> void addProperty(Property<T> property);

	public <T> void fireEvent(Property<T> source, PropertyEventType type, T oldValue);
}
