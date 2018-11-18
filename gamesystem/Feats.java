package gamesystem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import gamesystem.core.AbstractProperty;
import gamesystem.core.Property;
import gamesystem.core.PropertyCollection;
import gamesystem.core.PropertyEvent;

/* each Feat should be a Property who's value is a boolean indicating if the feat is active or not
 * repeatable feats can be added multiple times, and some cases can be bonus feats
 * a listener could listen to a feat that hasn't been added yet
 * losing prereqs (when implemented) would set active state to off
 * class features like ranger combat styles would add (virtual) feats that could have the active state overridden when conditions warrant
 * the Feats class would be a collection property that notifies changes to the collection
 * will want to abstract out the collection aspect and use it for other cases (skills and attacks, at least. perhaps hitdice though not sure that's really a collection)
 *
 * Currently the value of this property is the number of feats. Not sure this is best
 */

public class Feats extends AbstractProperty<Integer> {
	Creature creature;
	Map<Feat, FeatProperty> feats = new HashMap<>();
	List<Feat> featList = new ArrayList<>();	// order list of feats
	Map<String, Integer> repeatedOrdinals = new HashMap<>();	// maps feats to the

	public Feats(Creature c, PropertyCollection parent) {
		super("feats", parent);
		creature = c;
	}

	// adds a feat and returns the feat's property
	public Property<Boolean> addFeat(Feat f) {
		Set<Integer> used = new HashSet<>();
		for (Feat feat : featList) {
			if (feat.getName().equals(f.getName())	// feat has same name as existing and
					&& ((feat.target == null && f.target == null) || feat.target.equals(f.target))) {	// either both feats have no targets or both feats have the same target
				if (!f.definition.repeatable) return null;	// XXX throw exception?
				used.add(feats.get(feat).ordinal);
			}
		}
		FeatProperty prop = new FeatProperty(f);
		for (int i = 1; i <= used.size() + 1; i++) {
			if (!used.contains(i)) {
				prop.ordinal = i;
				break;
			}
		}
		if (prop.ordinal > 1) {
			// need to adjust the source of the modifiers in the feat. this is a bit of hack but seems the best option
			f.addOrdinal(prop.ordinal);
		}
		feats.put(f, prop);
		featList.add(f);
		f.apply(creature);
		fireEvent(createEvent(PropertyEvent.VALUE_CHANGED));
		//System.out.println("Added feat " + f.getName() + (f.bonus ? " (bonus feat)" : "") + (f.definition.hasTarget() ? " (" + f.target + ")" : ""));
		return prop;
	}

// removes a feat
	public void removeFeat(Feat f) {
		if (feats.containsKey(f)) {
			Property<Boolean> prop = feats.remove(f);
			featList.remove(f);
			parent.removeProperty(prop);
			fireEvent(createEvent(PropertyEvent.VALUE_CHANGED));
		}
	}

	public boolean hasFeat(String name) {
		if (name == null || name.length() == 0) return false;
		String lowerName = name.toLowerCase();
		for (Feat f : feats.keySet()) {
			Property<Boolean> prop = feats.get(f);
			if (f.getName().toLowerCase().equals(lowerName) && prop.getValue()) return true;
		}
		return false;
	}

	public int getSize() {
		return featList.size();
	}

// XXX maybe the collection interface should return the property for the feat
	public Feat get(int idx) {
		return featList.get(idx);
	}

// XXX if get returns the property then this is unneeded
	public Property<Boolean> getFeatProperty(Feat f) {
		return feats.get(f);
	}

	@Override
	public Integer getValue() {
		return feats.size();
	}

	class FeatProperty extends AbstractProperty<Boolean> {
		Feat feat;
		boolean active = true;
		int ordinal = 1;	// if this is the second or subsequent taking of a repeatable feat then this indicates the taking number

		public FeatProperty(Feat feat) {
			super(feat.getName(), Feats.this.parent);
			this.feat = feat;
		}

		@Override
		public Boolean getValue() {
			return active;
		}

	}
}
