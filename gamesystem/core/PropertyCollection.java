package gamesystem.core;

public interface PropertyCollection {
	public void addPropertyListener(String propName, PropertyListener l);

	public void addPropertyListener(Property<?> property, PropertyListener l);

	public void removePropertyListener(String propName, PropertyListener l);

	public void removePropertyListener(Property<?> property, PropertyListener l);

	public Property<?> getProperty(String name);

	// XXX move this stuff to another interface as it should only be called by Property objects?

	public void addProperty(Property<?> property);

	public void fireEvent(PropertyEvent event);
}
