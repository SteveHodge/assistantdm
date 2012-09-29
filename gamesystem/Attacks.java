package gamesystem;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

// TODO ranged attack
// TODO damage
// TODO multiple attacks

public class Attacks extends Statistic {
	int BAB = 0;

	public Attacks(AbilityScore str) {
		super("Attacks");

		Modifier strMod = str.getModifier();
		strMod.addPropertyChangeListener(new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				pcs.firePropertyChange("value", null, getValue());
			}
		});
		addModifier(strMod);
	}

	public int getBAB() {
		return BAB;
	}

	public void setBAB(int bab) {
		BAB = bab;
		pcs.firePropertyChange("value", null, getValue());
	}

	public int getValue() {
		return BAB + getModifiersTotal();
	}
}
