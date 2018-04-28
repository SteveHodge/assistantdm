package gamesystem.core;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

/*
 * Abstract property implementation. Generates OverriddenValueEvents by default but can generate events of a different class by overriding fireOverrideAddedEvent and fireOverrideRemovedEvent
 */

abstract public class AbstractOverridableProperty<T> extends AbstractProperty<T> implements OverridableProperty<T> {
	protected List<PropertyValue<T>> overrides;

	public AbstractOverridableProperty(String name, PropertyCollection parent) {
		super(name, parent);
	}

	@Override
	abstract public T getRegularValue();

	@Override
	public T getValue() {
		if (hasOverride()) return overrides.get(overrides.size() - 1).value;
		return getRegularValue();
	}

	@Override
	public PropertyValue<T> addOverride(T overrideVal) {
		T old = getValue();
		PropertyValue<T> v = new PropertyValue<>(overrideVal);
		if (overrides == null) overrides = new ArrayList<>();
		overrides.add(v);
		fireEvent(createEvent(PropertyEvent.OVERRIDE_ADDED, old));
		return v;
	}

	@Override
	public void removeOverride(PropertyValue<T> key) {
		T old = getValue();
		if (overrides.remove(key)) {
			fireEvent(createEvent(PropertyEvent.OVERRIDE_REMOVED, old));
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
		values.add(getRegularValue());
		if (overrides != null) {
			for (PropertyValue<T> v : overrides) {
				values.add(v.value);
			}
		}
		return values;
	}
}
