package gamesystem;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashSet;
import java.util.Set;

// TODO damage
// TODO multiple attacks

public class Attacks extends Statistic {
	int BAB = 0;
	Modifier strMod;
	Modifier dexMod;

	final PropertyChangeListener listener = new PropertyChangeListener() {
		public void propertyChange(PropertyChangeEvent evt) {
			pcs.firePropertyChange("value", null, getValue());
		}
	};

	public Attacks(AbilityScore str, AbilityScore dex) {
		super("Attacks");

		strMod = str.getModifier();
		strMod.addPropertyChangeListener(listener);

		dexMod = dex.getModifier();
		dexMod.addPropertyChangeListener(listener);
	}

	public int getBAB() {
		return BAB;
	}

	public void setBAB(int bab) {
		BAB = bab;
		pcs.firePropertyChange("value", null, getValue());
	}

	// returns the str-modified ("melee") statistic
	public int getValue() {
		Set<Modifier> mods = new HashSet<Modifier>();
		mods.addAll(modifiers);
		mods.add(strMod);
		return BAB + getModifiersTotal(mods,null);
	}

	public int getRangedValue() {
		Set<Modifier> mods = new HashSet<Modifier>();
		mods.addAll(modifiers);
		mods.add(dexMod);
		return BAB + getModifiersTotal(mods,null);
	}
}
