package gamesystem;


import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/*
 * Statistic - a game mechanic that can have modifiers applied. Understands how to combine modifiers to correctly obtain a total.
 * In many cases there is a base value that the modifiers are applied to. But the base value may not be editable so it's implementation
 * is left to the subclasses. 
 * 
 * Reports a single property "value" which is the total of the base value and the applicable modifiers.
 */
// TODO might need more comprehensive reporting. specifically subclasses may wish to issue a property change for baseValue separately
// to the property change for value (i.e. total). property change for changing modifiers even when the total is unchanged are also desirable
// TODO if the value doesn't change the pcs.firePropertyChange won't actually send an event. So that changes to modifiers that don't affect the
// total are reported we always report the old value as null. Probably best to change to a customer Event/Listener implementation
public class Statistic {
	protected String name;
	protected Set<Modifier> modifiers = new HashSet<Modifier>();
	protected final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

	protected final PropertyChangeListener listener = new PropertyChangeListener() {
		public void propertyChange(PropertyChangeEvent evt) {
			//System.out.println("Modifier to "+name+" changed");
			// problem here is that we know what the old value of the modifier is (from the event),
			// but we can't easily use that old value to calculate the old total
			// TODO could store old values locally
			pcs.firePropertyChange("value", null, getValue());
		}
	};

	public Statistic(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void addPropertyChangeListener(PropertyChangeListener listener) {
		pcs.addPropertyChangeListener(listener);
	}

//	public void addPropertyChangeListener(String property, PropertyChangeListener listener) {
//		pcs.addPropertyChangeListener(property, listener);
//	}

	public void removePropertyChangeListener(PropertyChangeListener listener) {
		pcs.removePropertyChangeListener(listener);
	}

	public void addModifier(Modifier m) {
		//int oldValue = getValue();
		m.addPropertyChangeListener(listener);
		modifiers.add(m);
		int newValue = getValue();
		pcs.firePropertyChange("value", null, newValue); 
	}

	public void removeModifier(Modifier m) {
		//int oldValue = getValue();
		modifiers.remove(m);
		m.removePropertyChangeListener(listener);
		int newValue = getValue();
		pcs.firePropertyChange("value", null, newValue);
	}

	public int getValue() {
		return getModifiersTotal();
	}

	// returns true if this has an active conditional modifier
	public boolean hasConditionalModifier() {
		Map<Modifier,Boolean> mods = getModifiers();
		for (Modifier m : mods.keySet()) {
			if (mods.get(m) && m.getCondition() != null) return true;
		}
		return false;
	}

	// returns the total of all active modifiers
	public int getModifiersTotal() {
		return getModifiersTotal(modifiers , null);
	}

	// returns the total active modifiers of the type specified. if type is null then the total of all modifiers is returned
	// conditional modifiers are not included
	public int getModifiersTotal(String type) {
		return getModifiersTotal(modifiers, type);
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

	public Map<Modifier,Boolean> getModifiers() {
		return getModifiers(modifiers);
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

		HashMap<Modifier,Boolean> map = new HashMap<Modifier,Boolean>();
		for (Modifier m : modifiers) {
			boolean include = true;

			// if the modifier is 0 then exlcude it
			if (m.getModifier() == 0) include = false;

			// check if there are modifiers with the same source and sign (we assume that two modifiers from the same source on the same target will always have the same type)
			if (include && m.getSource() != null) {
				for (Modifier a : map.keySet()) {
					if (map.get(a) && m.getSource().equals(a.getSource()) && Integer.signum(m.getModifier()) == Integer.signum(a.getModifier())) {
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
			if (include && m.getType() != null && !m.getType().equals("dodge") && !m.getType().equals("circumstance")) {
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

	public static void main(String[] args) {
		Statistic mods = new Statistic("Test Statistic");
		mods.addModifier(new ImmutableModifier(2,"dodge"));
		mods.addModifier(new ImmutableModifier(1,"luck"));
		mods.addModifier(new ImmutableModifier(1,"morale"));
		System.out.println(mods + "\n");

		mods.addModifier(new ImmutableModifier(2,"luck"));	// should replace luck +1
		System.out.println(mods + "\n");

		mods.addModifier(new ImmutableModifier(-1,"luck"));	// should be added
		System.out.println(mods + "\n");

		mods.addModifier(new ImmutableModifier(-2,"luck"));	// should replace luck -2
		System.out.println(mods + "\n");

		mods.addModifier(new ImmutableModifier(1,"dodge"));	// should be added
		System.out.println(mods + "\n");

		mods.addModifier(new ImmutableModifier(0,"morale"));	// should not be included
		System.out.println(mods + "\n");

		mods.addModifier(new ImmutableModifier(2,null,"spell"));	// should be added
		System.out.println(mods + "\n");

		mods.addModifier(new ImmutableModifier(1));			// should be added
		System.out.println(mods + "\n");

		mods.addModifier(new ImmutableModifier(3,null,"spell"));	// should replace previous "spell" bonus
		System.out.println(mods + "\n");
	}
}
