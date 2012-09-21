package gamesystem;


import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/*
 * Statistic - a value that can have modifiers applied. Understands how to combine modifiers to correctly obtain a total.
 * 
 * Contains a single property "value" which is the total of the base value and the applicable modifiers.
 */
public class Statistic {
	protected String name;
	protected int baseValue = 0;
	protected Set<Modifier> modifiers = new HashSet<Modifier>();
	protected final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
	protected final PropertyChangeListener listener = new PropertyChangeListener() {
		public void propertyChange(PropertyChangeEvent evt) {
			System.out.println("Modifier to "+name+" changed");
			// problem here is that we know what the old value of the modifier is (from the event),
			// but we can't easily use that old value to calculate the old total
			// TODO could store old values locally
			pcs.firePropertyChange("value", null, getValue());
		}
	};

	public Statistic(String name) {
		this.name = name;
	}

	public Statistic(String name, int base) {
		this.name = name;
		baseValue = base;
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
		int oldValue = getValue();
		m.addPropertyChangeListener(listener);
		modifiers.add(m);
		int newValue = getValue();
		if (oldValue != newValue) pcs.firePropertyChange("value", oldValue, newValue);
	}

	public void removeModifier(Modifier m) {
		int oldValue = getValue();
		m.removePropertyChangeListener(listener);
		modifiers.remove(m);
		int newValue = getValue();
		if (oldValue != newValue) pcs.firePropertyChange("value", oldValue, newValue);
	}

	public int getBaseValue() {
		return baseValue;
	}

	public void setBaseValue(int v) {
		int oldValue = getValue();
		baseValue = v;
		int newValue = getValue();
		//System.out.println(name+".setBaseValue("+v+"). Old = "+oldValue+", new = "+newValue);
		if (oldValue != newValue) pcs.firePropertyChange("value", oldValue, newValue);
	}

	public int getValue() {
		return baseValue + getModifiersTotal();
	}

	public int getModifiersTotal() {
		int total = 0;
		Map<Modifier,Boolean> map = getModifiers();
		for (Modifier m : map.keySet()) {
			if (map.get(m)) {
				total += m.getModifier();
			}
		}
		return total;
	}

	/* returns all current modifiers mapped to a boolean indicating whether or not each modifier is counted in the total
	 * never returns null, though may return an empty map
	 */
	// TODO inefficient implementation
	public Map<Modifier,Boolean> getModifiers() {
		// go through all the modifiers
		// if the modifier source is the same as another modifier source then only the highest bonus or lowest penalty counts
		// else {
		//	if the modifier type is dodge or circumstance then it counts
		//	if the modifier type is null then it counts
		//	if the modifier type is the same as another modifier type then only the highest bonus or lowest penalty count
		// }
		HashMap<Modifier,Boolean> map = new HashMap<Modifier,Boolean>();
		for (Modifier m : modifiers) {
			boolean include = true;
			// if the modifier is 0 then it doesn't count
			if (m.getModifier() == 0) {
				include = false;
			}

			// check if there are modifiers with the same source and sign
			if (include && m.getSource() != null) {
				for (Modifier a : map.keySet()) {
					if (m.getSource().equals(a.getSource()) && Integer.signum(m.getModifier()) == Integer.signum(a.getModifier())) {
						if (Math.abs(m.getModifier()) <= Math.abs(a.getModifier())) {
							// m is a smaller modifer so exclude it
							include = false;
							break;
						} else {
							// m is a larger modifier so exclude the old modifier
							// note this could cause problem if a later modifier in the set invalidates this modifier but that shouldn't happen 
							map.put(a, false);
						}
					}
				}
			}

			// check if there are modifiers with the same type and sign (but not dodge, circumstance of untypes modifiers as they always count)
			if (include && m.getType() != null && !m.getType().equals("dodge") && !m.getType().equals("circumstance")) {
				for (Modifier a : map.keySet()) {
					if (m.getType().equals(a.getType()) && Integer.signum(m.getModifier()) == Integer.signum(a.getModifier())) {
						if (Math.abs(m.getModifier()) <= Math.abs(a.getModifier())) {
							// m is a smaller modifer so exclude it
							include = false;
							break;
						} else {
							// m is a larger modifier so exclude the old modifier
							// note this could cause problem if a later modifier in the set invalidates this modifier
							map.put(a, false);
						}
					}
				}
			}

			map.put(m, include);
		}
		return map;
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
