package gamesystem.core;

import java.util.List;

/* A Property represents a single value that is overridable and that notifies interested parties of changes.
 * A Property can have one of more override values, the last override value applied is reported as the Property's
 * current value.
 * Two types of event are reported: valueChanged when the current value of the Property has changed and
 * compositionChanged when the list of overrides has changed. A single change may trigger both events.
 *
 * May extract a simple property that is not overridable (this class would become OverrideableProperty)
 * Overridable properties:
 * BAB (base value calculated based on class levels)
 * Race/Type
 * Size
 * HPs (override for things like polymorph, temp hitpoints need extra system)
 * Ability scores (these will be statistics and at least physical abilites can be overridden via polymorph)
 *
 * May defer the listener stuff to a parent object (Statistic or Creature) to make Properties more light-weight
 * by reducing the number of listener lists created. Higher level object will want to trigger higher level events
 * when a property changes so it may make sense to integrate event generation. Initially the higher level objects
 * will probably just add a proxy listener to the Property.
 */

public interface Property<T> {
	// ProeprtyValue is used as a key for overrides. it could expose a method to return the associated value
	class PropertyValue<U> {
		final U value;

		PropertyValue(U val) {
			value = val;
		}
	}

	public String getName();

	public PropertyCollection getParent();

	public void addPropertyListener(PropertyListener<T> l);

	public void removePropertyListener(PropertyListener<T> l);

	public T getValue();	// the current (possibly overriden) value of the property

	public T getBaseValue();	// the non-overriden value of the property

	public PropertyValue<T> addOverride(T overrideVal);

	public void removeOverride(PropertyValue<T> key);

	public boolean hasOverride();

	// returns all values in order (base value then each override in the order they were added)
	public List<T> getValues();
}
