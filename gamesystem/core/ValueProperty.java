package gamesystem.core;

/*
 * A concrete property implementation that uses a user-set value as the base
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
		if (overrides != null && overrides.size() > 0) {
			// have override so the final value won't change but the composition will
			T old = getValue();
			value = newVal;
			fireEvent(old);
		} else {
			// no override
			T old = value;
			value = newVal;
			fireEvent(old);
		}
	}

	@Override
	public T getRegularValue() {
		return value;
	}
}
