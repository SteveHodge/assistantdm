package gamesystem;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import party.Creature;

// TODO consider refactoring to not make temp hps Modifiers
// TODO there are difference in how damage and healing are handled (particularly with temporary hitpoints). going to need undo

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
 * If an effect has no hps left it should notify
 */

public class HPs extends Statistic {
	protected Level level;
	protected Modifier conMod;
	protected int oldMod;	// TODO tracking the old modifier here is fragile. really need accurate reporting of changes in the event
	protected int hps, wounds, nonLethal;
	protected List<TempHPs> tempHPs = new ArrayList<TempHPs>();	// this list should contain exactly one active TempHPs from each source. all members should have hps > 0
	protected Map<Modifier,TempHPs> modMap = new HashMap<Modifier,TempHPs>();

	// interested parties can register listeners as with other Modifier subclasses. if damage reduces a hps to 0, the TempHPs will be removed from the HPs instance
	protected class TempHPs implements Modifier {
		int hps;
		String source;
		boolean active = true;
		int id;

		final PropertyChangeSupport modpcs = new PropertyChangeSupport(this);

		public TempHPs(String source, int hps) {
			this.hps = hps;
			this.source = source;
		}

		public int getModifier() {
			return hps;
		}

		public String getType() {
			return null;
		}

		public String getSource() {
			return source;
		}

		public String getCondition() {
			return null;
		}

		public void addPropertyChangeListener(PropertyChangeListener listener) {
			modpcs.addPropertyChangeListener(listener);
		}

		public void removePropertyChangeListener(PropertyChangeListener listener) {
			modpcs.removePropertyChangeListener(listener);
		}

		public int getID() {
			return id;
		}
	}

	public HPs(AbilityScore con, Level lvl) {
		super("Hit Points");

		level = lvl;
		// TODO could add a listener

		conMod = con.getModifier();
		oldMod = conMod.getModifier();
		conMod.addPropertyChangeListener(new PropertyChangeListener() {

			public void propertyChange(PropertyChangeEvent e) {
        		int oldhps = getMaximumHitPoints();
        		int newhps = oldhps + (level.getLevel() * (conMod.getModifier() - oldMod));
        		if (newhps < level.getLevel()) newhps = level.getLevel();	// FIXME if we need to use this then it won't be reversable. probably need a max hp override
        		//System.out.println("changing max hps from "+oldhps+" to "+newhps);
        		setMaximumHitPoints(newhps);
        		oldMod = conMod.getModifier();
			}
		});
	}

	public int getMaximumHitPoints() {
		return hps;
	}

	public void setMaximumHitPoints(int hp) {
		int old = hps;
		hps = hp;
        pcs.firePropertyChange(Creature.PROPERTY_MAXHPS, old, hp);
	}

	public int getWounds() {
		return wounds;
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
						iter.remove();
					}
					t.modpcs.firePropertyChange(Creature.PROPERTY_HPS, old, t.hps);
				}
			}

		}
		// notify of change to temphps
		pcs.firePropertyChange("value", oldHPs, getHPs());

		// apply any remaining damage as wounds
		if (dmg > 0) setWounds(wounds+dmg);
	}

	// this directly sets the number of wounds (it bypasses temporary hitpoints)
	public void setWounds(int i) {
		int old = wounds;
		wounds = i;
		pcs.firePropertyChange(Creature.PROPERTY_WOUNDS, old, i);
	}

	public int getNonLethal() {
		return nonLethal;
	}

	public void setNonLethal(int i) {
		int old = nonLethal;
		nonLethal = i;
		pcs.firePropertyChange(Creature.PROPERTY_NONLETHAL, old, i);
	}

	protected Modifier addTemporaryHPs(TempHPs temps) {
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
		pcs.firePropertyChange(Creature.PROPERTY_HPS, old, getHPs());
		return temps;
	}

	protected void removeTemporaryHPs(Modifier hps) {
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
				pcs.firePropertyChange(Creature.PROPERTY_HPS, old, getHPs());
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

	public int getHPs() {
		return hps + getTemporaryHPs() - wounds - nonLethal;
	}

	// Statistics methods
	// TODO this should search the temphps we already have in case the temphps were already loaded by parseDOM 
	public void addModifier(Modifier m) {
		TempHPs hps = new TempHPs(m.getSource(),m.getModifier());
		hps.id = m.getID();
		addTemporaryHPs(hps);
		modMap.put(m, hps);
	}

	public void removeModifier(Modifier m) {
		TempHPs hps = modMap.remove(m);
		if (hps != null) {
			removeTemporaryHPs(hps);
		}
	}

	public int getValue() {
		return getHPs();
	}

	public boolean hasConditionalModifier() {
		return false;
	}

	public int getModifiersTotal() {
		return getTemporaryHPs();
	}

	public int getModifiersTotal(String type) {
		return getTemporaryHPs();
	}

	public Map<Modifier, Boolean> getModifiers() {
		HashMap<Modifier, Boolean> map = new HashMap<Modifier, Boolean>();
		for (TempHPs t : tempHPs) {
			map.put(t, t.active);
		}
		return map;
	}

	
	protected static void printTempHPs(List<TempHPs> tempHPs) {
		for (TempHPs t : tempHPs) {
			if (t.active) {
				System.out.println("   "+t.source+": "+t.hps);
			} else {
				System.out.println("   ("+t.source+": "+t.hps+")");
			}
		}
	}

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
		oldMod = conMod.getModifier();	// we need to set the oldMod so that any future con changes are correctly calculated
		// TODO this means that HPs must be parsed after ability scores. we really need accurate reporting of old con mod in the event

		NodeList temps = e.getChildNodes();
		if (temps != null) {
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
						System.out.println("Unimplemented: parsing temporary hitpoints before buff");
					}
					TempHPs temp = new TempHPs(source, hps);
					temp.id = id;
					addTemporaryHPs(temp);
				}
			}
		}
	}
}
