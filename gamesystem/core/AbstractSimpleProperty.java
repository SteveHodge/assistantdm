package gamesystem.core;

abstract public class AbstractSimpleProperty<T> implements SimpleProperty<T> {
	protected String name;
	protected PropertyCollection parent;

	public AbstractSimpleProperty(String name, PropertyCollection parent) {
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
	public PropertyCollection getParent() {
		return parent;
	}

	@Override
	public void addPropertyListener(PropertyListener<T> l) {
		parent.addPropertyListener(this, l);
	}

	@Override
	public void removePropertyListener(PropertyListener<T> l) {
		parent.removePropertyListener(this, l);
	}

	protected void fireEvent() {
		// XXX perhaps shouldn't allow update events with no old value
		parent.fireEvent(this, null);
	}

	protected void fireEvent(T old) {
		parent.fireEvent(this, old);
	}
}
