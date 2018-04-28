package gamesystem.core;

/*
 * A concrete property implementation that uses a user-set value as the regular value
 */

public class ValueProperty<T> extends AbstractOverridableProperty<T> implements SettableProperty<T> {
	T value;

	public ValueProperty(String name, PropertyCollection parent, T initialVal) {
		super(name, parent);
		value = initialVal;
	}

	@Override
	public void setValue(T newVal) {
		if (value == newVal) return;	// no change
		T old = getValue();
		value = newVal;
		fireEvent(createEvent(PropertyEvent.REGULAR_VALUE_CHANGED, old));
	}

	@Override
	public T getRegularValue() {
		return value;
	}
}
