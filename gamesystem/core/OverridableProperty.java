package gamesystem.core;

import java.util.List;

/* An OverridableProperty represents a single value that is overridable and that notifies interested parties of changes.
 * An OverridableProperty can have one of more override values, the last override value applied is reported as the OverridableProperty's
 * current value.
 * Two types of event are reported: valueChanged when the current value of the Property has changed and
 * compositionChanged when the list of overrides has changed. A single change may trigger both events.
 *
 * Overridable properties:
 * BAB (base value calculated based on class levels)
 * Race/Type
 * Size
 * HPs (override for things like polymorph, temp hitpoints need extra system)
 * Ability scores (these will be statistics and at least physical abilites can be overridden via polymorph)
 */

public interface OverridableProperty<T> extends Property<T> {
	// ProeprtyValue is used as a key for overrides. it could expose a method to return the associated value
	class PropertyValue<U> {
		public final U value;

		public PropertyValue(U val) {
			value = val;
		}
	}

	public T getBaseValue();	// the non-overriden value of the property

	public PropertyValue<T> addOverride(T overrideVal);

	public void removeOverride(PropertyValue<T> key);

	public boolean hasOverride();

	// returns all values in order (base value then each override in the order they were added)
	public List<T> getValues();
}
