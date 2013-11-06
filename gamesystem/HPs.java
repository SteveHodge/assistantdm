package gamesystem;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import party.Creature;

// TODO change temp hitpoints to a property
// TODO there are difference in how damage and healing are handled (particularly with temporary hitpoints). going to need undo
// TODO i think hps should be a property rather than a statistic

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

public class HPs extends Statistic {
	private Modifier conMod = null;
	private int oldMod;	// TODO tracking the old modifier here is fragile. really need accurate reporting of changes in the event
	private int hps, wounds, nonLethal;
	private List<TempHPs> tempHPs = new ArrayList<TempHPs>();	// this list should contain exactly one active TempHPs from each source. all members should have hps > 0
	private Map<Modifier, TempHPs> modMap = new HashMap<Modifier, TempHPs>();

	// interested parties can register listeners as with other Modifier subclasses. if damage reduces a hps to 0, the TempHPs will be removed from the HPs instance
	private class TempHPs extends AbstractModifier {
		private int hps;
		private String source;
		private int id;	// the id of the modifier that generated this instance

		// 'active' marks this instance as the one with the highest hps remaining for the particular source.
		// when damage is removed from an instance it is also removed from any other instance with the same source, but
		// only active instances are considered when deciding which instance to remove hps from. the effective total temporary
		// hps is the sum of all active instances. there should only be one active instance for each source
		private boolean active = true;

		private TempHPs(String source, int hps) {
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

	// TODO should add a listener to level
	public HPs(AbilityScore con, final HitDice level) {
		super("Hit Points");

		if (con != null) {
			conMod = con.getModifier();
			oldMod = conMod.getModifier();
			conMod.addPropertyChangeListener(new PropertyChangeListener() {
				@Override
				public void propertyChange(PropertyChangeEvent e) {
					int oldhps = getMaximumHitPoints();
					int newhps = oldhps + (level.getHitDiceCount() * (conMod.getModifier() - oldMod));
					System.out.println("hitdice = " + level.getHitDiceCount() + " oldMod = " + oldMod + ", newMod = " + conMod.getModifier());
					if (newhps < level.getHitDiceCount()) newhps = level.getHitDiceCount();	// FIXME if we need to use this then it won't be reversable. probably need a max hp override
					System.out.println("changing max hps from " + oldhps + " to " + newhps);
					setMaximumHitPoints(newhps);
					oldMod = conMod.getModifier();
				}
			});
		}
	}

	public int getMaximumHitPoints() {
		return hps;
	}

	public void setMaximumHitPoints(int hp) {
		int old = hps;
		hps = hp;
		firePropertyChange(Creature.PROPERTY_MAXHPS, old, hp);
	}

	public List<Modifier> getTemporaryHPsModifiers() {
		List<Modifier> m = Collections.<Modifier>unmodifiableList(tempHPs);
		return m;
	}

	public int getWounds() {
		return wounds;
	}

	public void applyHealing(int heal) {
		int oldHPs = getHPs();
		int oldWounds = wounds;
		int oldNL = nonLethal;

		if (nonLethal > heal) {
			nonLethal -= heal;
		} else {
			nonLethal = 0;
		}

		if (wounds > heal) {
			wounds -= heal;
		} else {
			wounds = 0;
		}

		// TODO clean this up. Shouldn't be firing so many events
		firePropertyChange(Creature.PROPERTY_WOUNDS, oldWounds, wounds);
		firePropertyChange(Creature.PROPERTY_NONLETHAL, oldNL, nonLethal);
		firePropertyChange(Creature.PROPERTY_HPS, oldHPs, getHPs());
	}

	// dmg should be > 0
	// TODO argument checking
	public void applyDamage(int dmg) {
		// remove damage from temporary hitpoint first:
		// while there is damage remaining to be allocated and temphps to allocate them to:
		int oldHPs = getHPs();
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
		firePropertyChange(Creature.PROPERTY_HPS, oldHPs, getHPs());

		// apply any remaining damage as wounds
		if (dmg > 0) setWounds(wounds+dmg);
	}

	// apply non-lethal damage
	public void applyNonLethal(int d) {
		int old = nonLethal;
		nonLethal += d;
		firePropertyChange(Creature.PROPERTY_NONLETHAL, old, nonLethal);
	}

	// this directly sets the number of wounds (it bypasses temporary hitpoints)
	public void setWounds(int i) {
		int old = wounds;
		wounds = i;
		firePropertyChange(Creature.PROPERTY_WOUNDS, old, i);
	}

	public int getNonLethal() {
		return nonLethal;
	}

	public void setNonLethal(int i) {
		int old = nonLethal;
		nonLethal = i;
		firePropertyChange(Creature.PROPERTY_NONLETHAL, old, i);
	}

	private Modifier addTemporaryHPs(TempHPs temps) {
		int old = getHPs();

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
		firePropertyChange(Creature.PROPERTY_HPS, old, getHPs());
		return temps;
	}

	// assumes the source of the modifier knows it is being removed
	private void removeTemporaryHPs(Modifier hps) {
		int old = getHPs();
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
				firePropertyChange(Creature.PROPERTY_HPS, old, getHPs());
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

	// TODO not sure this should include non-lethal
	public int getHPs() {
		return hps + getTemporaryHPs() - wounds - nonLethal;
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
	public int getValue() {
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
		HashMap<Modifier, Boolean> map = new HashMap<Modifier, Boolean>();
		for (TempHPs t : tempHPs) {
			map.put(t, t.active);
		}
		return map;
	}

	@Override
	public String getSummary() {
		StringBuilder text = new StringBuilder();
		text.append(getMaximumHitPoints()).append(" maximum<br/>");
		if (getWounds() != 0) text.append(-getWounds()).append(" wounds<br/>");
		if (getNonLethal() != 0) text.append(-getNonLethal()).append(" non-lethal damage taken<br/>");

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
		return text.toString();
	}

//	private static void printTempHPs(List<TempHPs> tempHPs) {
//		for (TempHPs t : tempHPs) {
//			if (t.active) {
//				System.out.println("   " + t.source + ": " + t.hps);
//			} else {
//				System.out.println("   (" + t.source + ": " + t.hps + ")");
//			}
//		}
//	}

	@Override
	public Element getElement(Document doc) {
		Element e = doc.createElement("HitPoints");
		e.setAttribute("maximum", ""+hps);
		if (wounds != 0) e.setAttribute("wounds", ""+wounds);
		if (nonLethal != 0) e.setAttribute("non-lethal" ,""+nonLethal);

		for (TempHPs t : tempHPs) {
			Element me = doc.createElement("TempHPs");
			me.setAttribute("hps", ""+t.hps);
			me.setAttribute("source", t.source);
			if (t.id > 0) me.setAttribute("id", ""+t.id);
			e.appendChild(me);
		}
		return e;
	}

	// TODO notify listeners?
	public void parseDOM(Element e) {
		if (!e.getTagName().equals("HitPoints")) return;
		hps = Integer.parseInt(e.getAttribute("maximum"));
		if (e.hasAttribute("wounds")) wounds = Integer.parseInt(e.getAttribute("wounds"));
		if (e.hasAttribute("non-lethal")) nonLethal = Integer.parseInt(e.getAttribute("non-lethal"));
		if (conMod != null) {
			oldMod = conMod.getModifier();	// we need to set the oldMod so that any future con changes are correctly calculated
		}
		// TODO this means that HPs must be parsed after ability scores. we really need accurate reporting of old con mod in the event

		// set any existing temporary hps to 0. this prevents temporary hitpoints that have been used for a particular
		// buff being reset. after we've parsed this element we'll remove any remaining temporary hitpoints.
		for (TempHPs temp : tempHPs) {
			//System.out.println("TempHPs: " + temp.id + " = " + temp.hps);
			temp.hps = 0;
		}

		NodeList temps = e.getChildNodes();
		for (int k=0; k<temps.getLength(); k++) {
			if (!temps.item(k).getNodeName().equals("TempHPs")) continue;
			Element m = (Element)temps.item(k);
			String source = m.getAttribute("source");
			int hps = Integer.parseInt(m.getAttribute("hps"));
			int id = Integer.parseInt(m.getAttribute("id"));

			boolean found = false;
			if (id > 0) {
				// see if we have a modifier to map this to. if we do then adjust the existing temp hps
				for (TempHPs temp : tempHPs) {
					if (temp.id == id) {
						if (!temp.source.equals(source)) System.out.println("Temp HPs: conflicting source for ID "+id);
						temp.hps = hps;
						found = true;
					}
				}
			}
			if (!found) {
				if (id > 0) {
					// if we don't have a modifier then we set up a temp hps but we'll need to wait for the Buff to link it up
					System.err.println("Unimplemented: parsing temporary hitpoints before buff");
				}
				TempHPs temp = new TempHPs(source, hps);
				temp.id = id;
				addTemporaryHPs(temp);
			}
		}

		// clean up: check active flags are correctly set and remove any tempHPs set to 0
		List<TempHPs> toDelete = new ArrayList<TempHPs>();
		Map<String, TempHPs> best = new HashMap<String, TempHPs>();
		for (TempHPs temp : tempHPs) {
			if (temp.hps == 0) {
				toDelete.add(temp);
			} else {
				TempHPs currBest = best.get(temp.source);
				if (currBest == null || currBest.hps < temp.hps) {
					if (currBest != null) currBest.active = false;
					best.put(temp.source, temp);
					temp.active = true;
				} else {
					temp.active = false;
				}
			}
		}
		for (TempHPs temp : toDelete) {
			//System.out.println("Deleting " + temp.id);
			tempHPs.remove(temp);
			// TODO need to notify source / remove source if it has no other effect
		}
		//printTempHPs(tempHPs);
	}
}
