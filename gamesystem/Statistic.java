package gamesystem;


import java.beans.PropertyChangeListener;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import gamesystem.core.AbstractOverridableProperty;
import gamesystem.core.PropertyCollection;
import gamesystem.core.StatisticEvent;
import gamesystem.core.StatisticEvent.EventType;

/*
 * Statistic - a game mechanic that can have modifiers applied. Understands how to combine modifiers to correctly obtain a total.
 * In many cases there is a base value that the modifiers are applied to. But the base value may not be editable so its implementation
 * is left to the subclasses (by overriding getBaseValue(), which returns 0 in this class).
 *
 * Reports a single property "value" which is the total of the base value and the applicable modifiers.
 *
 * Subclasses may implement additional properties, e.g. Attacks implements an extra_attacks property. Generally properties
 * are values that are changeable but are not subject to typed modifiers.
 */
// TODO need to implement overrides. overrides should apply to the total, but need to handle modifiers added after the override
// XXX perhaps have a subclass that contains the base value stuff
public class Statistic extends AbstractOverridableProperty<Integer> {
	protected Set<Modifier> modifiers = new HashSet<>();
	protected String description;

	public Statistic(String name, String desc, PropertyCollection parent) {
		super(name, parent);
		description = desc;
	}

	//----------------------------- Property Interface -----------------------------
	@Override
	public Integer getRegularValue() {
		return getBaseValue() + getModifiersTotal();
	}

	@Override
	public PropertyValue<Integer> addOverride(Integer overrideVal) {
		// TODO implement
		return null;
	}

	@Override
	public void removeOverride(PropertyValue<Integer> key) {
		// TODO implement
	}
	//------------------------------------------------------------------------------

	//--------------------------- Event Related Methods ----------------------------
	@Override
	protected void fireOverrideAddedEvent(Integer old) {
		fireEvent(new StatisticEvent(this, EventType.OVERRIDE_ADDED));
	}

	@Override
	protected void fireOverrideRemovedEvent(Integer old) {
		fireEvent(new StatisticEvent(this, EventType.OVERRIDE_REMOVED));
	}
	//------------------------------------------------------------------------------

	public String getDescription() {
		return description;
	}

	// problem here is that we know what the old value of the modifier is (from the event),
	// but we can't easily use that old value to calculate the old total
	// TODO could store old values locally
	protected final PropertyChangeListener listener = evt -> fireEvent(new StatisticEvent(this, EventType.TOTAL_CHANGED));

	public void addModifier(Modifier m) {
		if (m == null) {
			System.err.println("Attempted to add null modifier to " + this);
			return;
		}
		//int oldValue = getValue();
		m.addPropertyChangeListener(listener);
		modifiers.add(m);
		fireEvent(new StatisticEvent(this, EventType.MODIFIER_ADDED));
	}

	public void removeModifier(Modifier m) {
		if (m == null) return;
		//int oldValue = getValue();
		modifiers.remove(m);
		m.removePropertyChangeListener(listener);
		fireEvent(new StatisticEvent(this, EventType.MODIFIER_REMOVED));
	}

	// set a specific property on the statistic. this default implementation doesn't implement any properties
	// it is intended that generally when a property is set the old value is remembered and restored when the
	// property is reset. for the tracking of set values "key" should be a unique object/value. this key is used
	// to remove the set value via resetProperty. "source" can be used in cases where multiple values are combined
	// - usually in these cases only one value for each source is included
	public void setProperty(String property, Object value, String source, Object key) {
	}

	public void resetProperty(String property, Object key) {
	}

	public int getBaseValue() {
		return 0;
	}

	// FIXME need to account for overrides
	@Override
	public Integer getValue() {
		return getBaseValue() + getModifiersTotal();
	}

	// returns true if this has an active conditional modifier
	public boolean hasConditionalModifier() {
		Map<Modifier,Boolean> mods = getModifiers();
		for (Modifier m : mods.keySet()) {
			if (mods.get(m) && m.getCondition() != null) return true;
		}
		return false;
	}

	// getModifiers() and the getModifiersTotal() methods should all be overridden if a subclasses
	// does any manipulation or filtering of modifiers
	// TODO change this back to protected
	public Set<Modifier> getModifierSet() {
		return modifiers;
	}

	// returns the map of active modifiers
	public Map<Modifier,Boolean> getModifiers() {
		return getModifiers(getModifierSet());
	}

	// returns the total of all active modifiers
	public int getModifiersTotal() {
		return getModifiersTotal(getModifierSet(), null);
	}

	// returns the total of all active modifiers excluding types in the specified set
	// conditional modifiers are not included
	public int getModifiersTotal(String... excludeTypes) {
		return getModifiersTotalExcluding(getModifierSet(), excludeTypes);
	}

	// returns the total active modifiers of the type specified. if type is null then the total of all modifiers is returned
	// conditional modifiers are not included
	public int getModifiersTotal(String type) {
		return getModifiersTotal(getModifierSet(), type);
	}

	// conditional modifiers are not included
	protected static int getModifiersTotal(Set<Modifier> mods, String type) {
		int total = 0;
		Map<Modifier,Boolean> map = getModifiers(mods);
		for (Modifier m : map.keySet()) {
			if (map.get(m) && (type == null || type.equals(m.getType())) && m.getCondition() == null) {
				total += m.getModifier();
			}
		}
		return total;
	}

	// conditional modifiers are not included
	protected static int getModifiersTotalExcluding(Set<Modifier> mods, String... excluding) {
		int total = 0;
		List<String> exTypes = Arrays.asList(excluding);
		Map<Modifier, Boolean> map = getModifiers(mods);
		for (Modifier m : map.keySet()) {
			if (map.get(m) && !exTypes.contains(m.getType()) && m.getCondition() == null) {
				total += m.getModifier();
			}
		}
		return total;
	}

	/* returns all current modifiers mapped to a boolean indicating whether or not each modifier is counted in the total
	 * never returns null, though may return an empty map
	 */
	// TODO inefficient implementation
	public static Map<Modifier,Boolean> getModifiers(Set<Modifier> modifiers) {
		// go through all the modifiers
		// non-conditional modifiers should be active unless
		// 1. there is a modifier from the same source that is better
		// 2. there is a modifier with the same type that is better (and the type is not dodge, circumstance, or untyped)
		// conditional modifiers should be active unless:
		// 1. there is a modifier with no condition or the same condition that is from the same source and is better
		// 2. there is a modifier with no condition or the same condition of the same type that is better (and the type is not dodge, circumstance, or untyped)

		HashMap<Modifier, Boolean> map = new HashMap<>();
		for (Modifier m : modifiers) {
			boolean include = true;

			// if the modifier is 0 then exclude it
			if (m.getModifier() == 0) include = false;

			// check if there are modifiers with the same source, type and sign
			if (include && m.getSource() != null) {
				for (Modifier a : map.keySet()) {
					if (map.get(a) && m.getSource().equals(a.getSource()) && Integer.signum(m.getModifier()) == Integer.signum(a.getModifier())
							&& (m.getType() == null && a.getType() == null || m.getType() != null && m.getType().equals(a.getType()))
							) {
						// 'a' is an existing active modifier with the same source and sign
						if (Math.abs(m.getModifier()) < Math.abs(a.getModifier())) {
							// m is a smaller modifier so exclude it unless 'a' has a different condition
							if (a.getCondition() == null || a.getCondition().equals(m.getCondition())) {
								include = false;
								break;
							}
						} else {
							// m is equal or larger modifier
							// we should exclude 'a' if m has no condition or m and a have the same condition
							// note this could cause problem if a later modifier in the set invalidates this modifier but that shouldn't happen
							if (m.getCondition() == null || m.getCondition().equals(a.getCondition())) {
								map.put(a, false);
							}
						}
					}
				}
			}

			// check if there are modifiers with the same type and sign (but not dodge, circumstance of untyped modifiers as they always count)
			if (include && m.getType() != null && !m.getType().equals("Dodge") && !m.getType().equals("Circumstance")) {
				for (Modifier a : map.keySet()) {
					if (map.get(a) && m.getType().equals(a.getType()) && Integer.signum(m.getModifier()) == Integer.signum(a.getModifier())) {
						if (Math.abs(m.getModifier()) < Math.abs(a.getModifier())) {
							// m is a smaller modifier so exclude it unless 'a' has a different condition
							if (a.getCondition() == null || a.getCondition().equals(m.getCondition())) {
								include = false;
								break;
							}
						} else {
							// m is equal or larger modifier
							// we should exclude 'a' if m has no condition or m and a have the same condition
							// note this could cause problem if a later modifier in the set invalidates this modifier but that shouldn't happen
							if (m.getCondition() == null || m.getCondition().equals(a.getCondition())) {
								map.put(a, false);
							}
						}
					}
				}
			}

			map.put(m, include);
		}

		return map;
	}

	// returns a html formatted list of non-conditional modifiers from the specified map. inactive modifiers (where the
	// matching boolean is false) are stuck through.
	public static String getModifiersHTML(Map<Modifier,Boolean> mods) {
		return getModifiersHTML(mods, false);
	}

	// returns a html formatted list of modifiers from the specified map. inactive modifiers (where the
	// matching boolean is false) are stuck through.
	// if conditionals is true then returns html for conditional modifiers only, otherwise returns
	// html for non-conditionals only
	public static String getModifiersHTML(Map<Modifier,Boolean> mods, boolean conditionals) {
		StringBuilder text = new StringBuilder();
		for (Modifier m : mods.keySet()) {
			if ((m.getCondition() == null) != conditionals) {
				if (!mods.get(m)) text.append("<s>");
				text.append(m);
				if (!mods.get(m)) text.append("</s>");
				text.append("<br/>");
			}
		}
		return text.toString();
	}

	public String getSummary() {
		StringBuilder text = new StringBuilder();
		Map<Modifier, Boolean> mods = getModifiers();
		text.append(Statistic.getModifiersHTML(mods));
		text.append(getValue()).append(" total ").append(getDescription()).append("<br/>");
		String conds = Statistic.getModifiersHTML(mods, true);
		if (conds.length() > 0) text.append("<br/>").append(conds);
		return text.toString();
	}

	@Override
	public String toString() {
		String s = new String();
		Map<Modifier,Boolean> map = getModifiers();
		for (Modifier m : map.keySet()) {
			if (map.get(m)) {
				s += m + "\n";
			} else {
				s += "(" + m + ")\n";
			}
		}
		s += "Total " + name + " = " + getValue();
		return s;
	}

//	public static void main(String[] args) {
//		Statistic mods = new Statistic("Test Statistic");
//		mods.addModifier(new ImmutableModifier(2,"dodge"));
//		mods.addModifier(new ImmutableModifier(1,"luck"));
//		mods.addModifier(new ImmutableModifier(1,"morale"));
//		System.out.println(mods + "\n");
//
//		mods.addModifier(new ImmutableModifier(2,"luck"));	// should replace luck +1
//		System.out.println(mods + "\n");
//
//		mods.addModifier(new ImmutableModifier(-1,"luck"));	// should be added
//		System.out.println(mods + "\n");
//
//		mods.addModifier(new ImmutableModifier(-2,"luck"));	// should replace luck -2
//		System.out.println(mods + "\n");
//
//		mods.addModifier(new ImmutableModifier(1,"dodge"));	// should be added
//		System.out.println(mods + "\n");
//
//		mods.addModifier(new ImmutableModifier(0,"morale"));	// should not be included
//		System.out.println(mods + "\n");
//
//		mods.addModifier(new ImmutableModifier(2,null,"spell"));	// should be added
//		System.out.println(mods + "\n");
//
//		mods.addModifier(new ImmutableModifier(1));			// should be added
//		System.out.println(mods + "\n");
//
//		mods.addModifier(new ImmutableModifier(3,null,"spell"));	// should replace previous "spell" bonus
//		System.out.println(mods + "\n");
//	}
}
