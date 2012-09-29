package gamesystem;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import party.Creature;

// TODO this is statistic for the listener stuff but it doesn't really need modifiers so perhaps refactor
// TODO temporary hitpoints - implement as Modifiers? they are similar but not quite the same

public class HPs extends Statistic {
	protected Level level;
	protected Modifier conMod;
	protected int oldMod;
	protected int hps, wounds, nonLethal;

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
        		System.out.println("changing max hps from "+oldhps+" to "+newhps);
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

	public int getHPs() {
		return hps - wounds - nonLethal;
	}
}
