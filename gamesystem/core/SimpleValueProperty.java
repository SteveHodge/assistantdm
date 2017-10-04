package gamesystem.core;

public class SimpleValueProperty<T> extends AbstractSimpleProperty<T> {
	T value;

	public SimpleValueProperty(String name, PropertyCollection parent, T initial) {
		super(name, parent);
		value = initial;
	}

	@Override
	public T getValue() {
		return value;
	}

	public void setValue(T val) {
		T old = value;
		value = val;
		fireEvent(old);
	}
}
