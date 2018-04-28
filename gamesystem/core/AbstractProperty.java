package gamesystem.core;

/* Implementation of the listener and hierarchy related methods of Property
 *
 */

abstract public class AbstractProperty<T> implements Property<T> {
	protected String name;
	protected PropertyCollection parent;

	public AbstractProperty(String name, PropertyCollection parent) {
		this.name = name;
		this.parent = parent;
		parent.addProperty(this);
	}

	@Override
	abstract public T getValue();

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return getName();
	}

	@Override
	public PropertyCollection getParent() {
		return parent;
	}

	@Override
	public void addPropertyListener(PropertyListener l) {
		parent.addPropertyListener(this, l);
	}

	@Override
	public void removePropertyListener(PropertyListener l) {
		parent.removePropertyListener(this, l);
	}

	public PropertyEvent createEvent(String type) {
		return new PropertyEvent(this, type);
	}

	public PropertyEvent createEvent(String type, T old) {
		PropertyEvent e = new PropertyEvent(this, type);
		e.set(PropertyEvent.PREVIOUS_VALUE, old);
		return e;
	}

	protected void fireEvent(PropertyEvent e) {
		parent.fireEvent(e);
	}
}
