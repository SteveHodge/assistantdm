package gamesystem.core;

public interface PropertyCollection {
	public void addPropertyListener(String propName, PropertyListener<?> l);

	public <T> void addPropertyListener(SimpleProperty<T> property, PropertyListener<T> l);

	public void removePropertyListener(String propName, PropertyListener<?> l);

	public <T> void removePropertyListener(SimpleProperty<T> property, PropertyListener<T> l);

	public SimpleProperty<?> getProperty(String name);

	// XXX move this stuff to another interface as it should only be called by Property objects?

	public <T> void addProperty(SimpleProperty<T> property);

	public <T> void fireEvent(SimpleProperty<T> source, T oldValue);
}
