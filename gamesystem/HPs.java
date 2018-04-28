package gamesystem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import gamesystem.core.OverridableProperty;
import gamesystem.core.PropertyCollection;
import gamesystem.core.PropertyEvent;
import gamesystem.core.SimpleValueProperty;


// XXX change temp hitpoints to a property
// TODO there are difference in how damage and healing are handled (particularly with temporary hitpoints). going to need undo
// XXX i think hps should be a property rather than a statistic

/* Architecture:
 * HPs is a statistic representing current hitpoints. Modifiers on HPs apply temporary hitpoints. Not sure overrides make sense though there are effects that set current hps to a particular value. Perhaps could be a property.
 * MaxHPs is a statistic representing maximum hitpoints. Modifiers affect the maximum, e.g. from feats, negative levels. Maybe should be merged with HitDice.
 * Wounds will be a SimpleProperty as I don't think overrides make sense. Note the value is dependent on HPs and MaxHPs.
 * Non-lethal will be a SimplePoperty as I don't think overrides make sense.
 *
 * Rules for changes:
 * If MaxHPs changes then HPs changes as well. Wounds remains constant.
 * If HPs changes then Wounds changes as well. MaxHPs remains constant.
 * If Wounds changes then HPs changes as well. MaxHPs remains constant.
 */

/* Temporary hit points rules
 * 1. If temporary hit points are granted more than once then only the best one applies (FAQ)
 * 2. If temporary hit points are granted from different sources then they stack (FAQ)
 * 3. Damages comes off the oldest source first (FAQ) - this is interpreted to mean the oldest active source
 *
 * There is still a question as to what happens when there are multiples instances from a single source and
 * then some of the instances are removed (e.g. dispelled). Consider:
 * 1. Character gets 15 temporary hit points from Aid
 * 2. Character takes 8 damage (7 temp hps remain)
 * 3. Character gets 12 temporary hit points from Aid. This replaces the first Aid, leaving 12 temporary hit points
 * 4. Character takes 6 damage (6 temp hps remain)
 * 5. The second Aid is dispelled
 * How many temporary hit points remain?
 * Option A. The first spell's remaining 7 temporary hit points come back into effect
 * Option B. The damage is applied to all instances of the source that is currently taking damage. So when the first
 *           Aid comes back into effect only 1 temporary hp remain
 * Option A is exploitable and would cause unintuitive results (a successful dispel should not result in higher hps). It
 * would effectively mean that effects from the same source did stack in certain situations. For those reasons option B
 * is preferred.
 *
 * Variant rule: temporary hit points don't stack regardless of source.
 *
 * Implementation:
 * Need to track source and current value in order
 * When damage is sustained, selected the first source. remove the damage from all temp hps of that source
 * If an effect has no hps left it should be be notified
 */

// TODO fix up encapsulation since parsing has moved to XMLCreatureParser
public class HPs extends Statistic {
	List<TempHPs> tempHPs = new ArrayList<>();	// this list should contain exactly one active TempHPs from each source. all members should have hps > 0
	Map<Modifier, TempHPs> modMap = new HashMap<>();
	MaxHPs maxHPs;
	SimpleValueProperty<Integer> wounds;
	SimpleValueProperty<Integer> nonLethal;

	// interested parties can register listeners as with other Modifier subclasses. if damage reduces a hps to 0, the TempHPs will be removed from the HPs instance
	class TempHPs extends AbstractModifier {
		int hps;
		String source;
		int id;	// the id of the modifier that generated this instance

		// 'active' marks this instance as the one with the highest hps remaining for the particular source.
		// when damage is removed from an instance it is also removed from any other instance with the same source, but
		// only active instances are considered when deciding which instance to remove hps from. the effective total temporary
		// hps is the sum of all active instances. there should only be one active instance for each source
		boolean active = true;

		TempHPs(String source, int hps) {
			this.hps = hps;
			this.source = source;
		}

		@Override
		public int getModifier() {
			return hps;
		}

		@Override
		public String getSource() {
			return source;
		}

		@Override
		public int getID() {
			return id;
		}
	}

	// FIXME the max hps field in the ui should set a override on the total of this (so that modifiers are preserved), currently it effectively sets the base value
	// TODO should probably move this to HitDiceProperty
	// FIXME temporarily only supports a single override value
	public class MaxHPs extends Statistic implements OverridableProperty<Integer> {
		HitDiceProperty hitdice;
		Integer override = null;

		public MaxHPs(HitDiceProperty hd, PropertyCollection parent) {
			super("hit_points.max_hps", "Max Hit Points", parent);

			hitdice = hd;
			hitdice.addPropertyListener(e -> {
				if (override != null && override.equals(getRegularValue())) {
					override = null;	// XXX not sure if it's best to remove override if it now matches the correct value
				}
				fireEvent(createEvent(PropertyEvent.VALUE_CHANGED));
			});
		}

		@Override
		public OverridableProperty.PropertyValue<Integer> addOverride(Integer val) {
//			Integer old = getValue();
			if (val == null || val.equals(getRegularValue())) {
				override = null;	// TODO removing override should be done through removeOverride method - remove this hack when overrides are properly suported in the ui and implemented in statistic
			} else {
				override = val;
			}
			fireEvent(createEvent(PropertyEvent.OVERRIDE_ADDED));
			return new PropertyValue<Integer>(val);
		}

		@Override
		public int getBaseValue() {
			return hitdice.getMaxHPs();
		}

		@Override
		public Integer getValue() {
			if (override == null) return super.getRegularValue();
			return override;
		}

		@Override
		public boolean hasOverride() {
			return override != null;
		}
	}

	// TODO should probably move con monitoring to HitDiceProperty
	public HPs(HitDiceProperty hd, PropertyCollection parent) {
		super("hit_points", "Hit Points", parent);

		maxHPs = new MaxHPs(hd, parent);

		wounds = new SimpleValueProperty<Integer>("hit_points.wounds", parent, 0);

		nonLethal = new SimpleValueProperty<Integer>("hit_points.non-lethal", parent, 0);
	}

	public MaxHPs getMaxHPStat() {
		return maxHPs;
	}

	public SimpleValueProperty<Integer> getWoundsProperty() {
		return wounds;
	}

	public SimpleValueProperty<Integer> getNonLethalProperty() {
		return nonLethal;
	}

	public int getWounds() {
		return wounds.getValue();
	}

	public int getNonLethal() {
		return nonLethal.getValue();
	}

	public List<Modifier> getTemporaryHPsModifiers() {
		List<Modifier> m = Collections.<Modifier>unmodifiableList(tempHPs);
		return m;
	}

	public void applyHealing(int heal) {
//		int oldHPs = getHPs();
//		int oldWounds = wounds;
//		int oldNL = nonLethal;

		if (nonLethal.getValue() > heal) {
			nonLethal.setValue(nonLethal.getValue() - heal);
		} else {
			nonLethal.setValue(0);
		}

		if (wounds.getValue() > heal) {
			wounds.setValue(wounds.getValue() - heal);
		} else {
			wounds.setValue(0);
		}

		fireEvent(createEvent(PropertyEvent.VALUE_CHANGED));
	}

	// dmg should be > 0
	// TODO argument checking
	public void applyDamage(int dmg) {
		// remove damage from temporary hitpoint first:
		// while there is damage remaining to be allocated and temphps to allocate them to:
//		int oldHPs = getHPs();
		while (dmg > 0 && tempHPs.size() > 0) {
			// find the first active temphps
			TempHPs selected = null;
			for (TempHPs t : tempHPs) {
				if (t.active) {
					selected = t;
					break;
				}
			}
			// assertion: selected should not be null

			// determine how much damage can be allocated to temphps of this source:
			int d = dmg;
			if (d > selected.hps) d = selected.hps;
			dmg -= d;			// reduce damage by the amount removed from this source

			// remove damage from all temphps with the same source
			ListIterator<TempHPs> iter = tempHPs.listIterator();
			while (iter.hasNext()) {
				TempHPs t = iter.next();
				if (t.source.equals(selected.source)) {
					int old = t.hps;
					t.hps -= d;
					if (t.hps <= 0) {
						t.hps = 0;
						// if any temphps are reduced to 0 remove them
						// TODO need to notify source / remove source if it has no other effect
						iter.remove();
					}
					t.pcs.firePropertyChange(Creature.PROPERTY_HPS, old, t.hps);
				}
			}

		}
		// notify of change to temphps
		fireEvent(createEvent(PropertyEvent.VALUE_CHANGED));

		// apply any remaining damage as wounds
		if (dmg > 0) wounds.setValue(wounds.getValue() + dmg);
	}

	// apply non-lethal damage
	public void applyNonLethal(int d) {
		nonLethal.setValue(nonLethal.getValue() + d);
	}

	Modifier addTemporaryHPs(TempHPs temps) {
//		int old = getHPs();

		// find the highest instance of this source - that instance is active, all others with the same source are inactive
		TempHPs best = temps;
		temps.active = false;
		for (TempHPs t : tempHPs) {
			if (t.source.equals(temps.source)) {
				t.active = false;
				if (t.hps >= best.hps) {
					best = t;
				}
			}
		}
		best.active = true;

		tempHPs.add(temps);
		fireEvent(createEvent(PropertyEvent.VALUE_CHANGED));
		return temps;
	}

	// assumes the source of the modifier knows it is being removed
	private void removeTemporaryHPs(Modifier hps) {
//		int old = getHPs();
		if (tempHPs.remove(hps)) {
			TempHPs removed = (TempHPs)hps;
			if (removed.active) {
				// need to find new best instance of the same source (if any) and mark it active
				TempHPs best = null;
				for (TempHPs t : tempHPs) {
					if (t.source.equals(removed.source)) {
						if (best == null || t.hps >= best.hps) {
							best = t;
						}
					}
				}
				if (best != null) best.active = true;
				fireEvent(createEvent(PropertyEvent.VALUE_CHANGED));
			}
		}
	}

	// returns the current effective value of temporary hitpoints
	public int getTemporaryHPs() {
		int total = 0;
		for (TempHPs t : tempHPs) {
			if (t.active) total += t.hps;
		}
		return total;
	}

	// returns the current hitpoints (not including any non-lethal damage)
	public int getHPs() {
		return maxHPs.getValue() + getTemporaryHPs() - wounds.getValue();
	}

	// Statistics methods
	// TODO this should search the temphps we already have in case the temphps were already loaded by parseDOM
	@Override
	public void addModifier(Modifier m) {
		TempHPs hps = new TempHPs(m.getSource(),m.getModifier());
		hps.id = m.getID();
		addTemporaryHPs(hps);
		modMap.put(m, hps);
	}

	@Override
	public void removeModifier(Modifier m) {
		TempHPs hps = modMap.remove(m);
		if (hps != null) {
			removeTemporaryHPs(hps);
		}
	}

	@Override
	public Integer getValue() {
		return getHPs();
	}

	@Override
	public boolean hasConditionalModifier() {
		return false;
	}

	@Override
	public int getModifiersTotal() {
		return getTemporaryHPs();
	}

	@Override
	public int getModifiersTotal(String type) {
		return getTemporaryHPs();
	}

	@Override
	public Map<Modifier, Boolean> getModifiers() {
		HashMap<Modifier, Boolean> map = new HashMap<>();
		for (TempHPs t : tempHPs) {
			map.put(t, t.active);
		}
		return map;
	}

	@Override
	public String getSummary() {
		StringBuilder text = new StringBuilder();
		text.append(maxHPs.getValue()).append(" maximum<br/>");
		if (wounds.getValue() != 0) text.append(-wounds.getValue()).append(" wounds<br/>");

		Map<Modifier, Boolean> mods = getModifiers();
		for (Modifier m : mods.keySet()) {
			if (m.getCondition() == null) {
				if (!mods.get(m)) text.append("<s>");
				if (m.getModifier() >= 0) text.append("+");
				text.append(m.getModifier()).append(" temporary");
				if (m.getSource() != null) text.append(" (from ").append(m.getSource()).append(")");
				if (!mods.get(m)) text.append("</s>");
				text.append("<br/>");
			}
		}

		text.append("=").append(getHPs()).append(" current total<br/><br/>");
		if (nonLethal.getValue() != 0) text.append("Non-lethal damage taken: " + nonLethal.getValue()).append("<br/><br/>");
		return text.toString();
	}

	public String getShortSummary() {
		StringBuilder text = new StringBuilder();
		text.append(getHPs());
		if (nonLethal.getValue() != 0) text.append(" (" + nonLethal.getValue()).append(" NL)");
		text.append(" / ").append(maxHPs.getValue());
		return text.toString();
	}

	public void printTempHPs() {
		for (TempHPs t : tempHPs) {
			if (t.active) {
				System.out.println("   " + t.source + ": " + t.hps);
			} else {
				System.out.println("   (" + t.source + ": " + t.hps + ")");
			}
		}
	}
}
