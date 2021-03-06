package gamesystem.core;

// Just a value that notifies changes via ChangedValueEvents

public class SimpleValueProperty<T> extends AbstractProperty<T> implements SettableProperty<T> {
	T value;

	public SimpleValueProperty(String name, PropertyCollection parent, T initial) {
		super(name, parent);
		value = initial;
	}

	@Override
	public T getValue() {
		return value;
	}

	@Override
	public void setValue(T val) {
		T old = value;
		value = val;
		fireEvent(createEvent(PropertyEvent.VALUE_CHANGED, old));
	}
}
