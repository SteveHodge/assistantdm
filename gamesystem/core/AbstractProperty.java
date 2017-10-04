package gamesystem.core;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

/*
 * Abstract property implementation
 */

abstract public class AbstractProperty<T> extends AbstractSimpleProperty<T> implements Property<T> {
	protected List<PropertyValue<T>> overrides;

	public AbstractProperty(String name, PropertyCollection parent) {
		super(name, parent);
	}

	@Override
	abstract public T getBaseValue();

	@Override
	public T getValue() {
		if (hasOverride()) return overrides.get(overrides.size() - 1).value;
		return getBaseValue();
	}

	@Override
	public PropertyValue<T> addOverride(T overrideVal) {
		T old = getValue();
		PropertyValue<T> v = new PropertyValue<>(overrideVal);
		if (overrides == null) overrides = new ArrayList<>();
		overrides.add(v);
		fireEvent(old);
		return v;
	}

	@Override
	public void removeOverride(PropertyValue<T> key) {
		T old = getValue();
		if (overrides.remove(key)) {
			fireEvent(old);
		} else {
			throw new NoSuchElementException();
		}
	}

	@Override
	public boolean hasOverride() {
		return overrides != null && overrides.size() > 0;
	}

	// returns all values in order (base value then each override in the order they were added)
	@Override
	public List<T> getValues() {
		List<T> values = new ArrayList<>();
		values.add(getBaseValue());
		if (overrides != null) {
			for (PropertyValue<T> v : overrides) {
				values.add(v.value);
			}
		}
		return values;
	}
}
