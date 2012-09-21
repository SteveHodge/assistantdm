package gamesystem;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public class AbilityScore extends Statistic {
	public static final int ABILITY_STRENGTH = 0;
	public static final int ABILITY_DEXTERITY = 1;
	public static final int ABILITY_CONSTITUTION = 2;
	public static final int ABILITY_INTELLIGENCE = 3;
	public static final int ABILITY_WISDOM = 4;
	public static final int ABILITY_CHARISMA = 5;
	protected static final String[] ability_names = {"Strength", "Dexterity", "Constitution", "Intelligence", "Wisdom", "Charisma"};

	protected final Modifier modifier;

	public static int getModifier(int score) {
		return score/2-5;
	}

	public static String getAbilityName(int type) {
		return ability_names[type];
	}

	protected class AbilityModifier implements Modifier {
		final PropertyChangeSupport modpcs = new PropertyChangeSupport(this);

		public AbilityModifier(AbilityScore score) {
			score.addPropertyChangeListener(new PropertyChangeListener() {
				public void propertyChange(PropertyChangeEvent evt) {
					int oldValue = ((Integer)evt.getOldValue())/2-5;
					int newValue = ((Integer)evt.getNewValue())/2-5;
					if (oldValue != newValue) {
						modpcs.firePropertyChange("value", oldValue, newValue);
					}
				}
			});
		}

		public int getModifier() {
			return getValue()/2-5;
		}

		public String getType() {
			return name;
		}

		public String getSource() {
			return null;
		}

		public void addPropertyChangeListener(PropertyChangeListener listener) {
			modpcs.addPropertyChangeListener(listener);
		}

		public void removePropertyChangeListener(PropertyChangeListener listener) {
			modpcs.removePropertyChangeListener(listener);
		}
	};

	// TODO bounds checking
	public AbilityScore(int type) {
		super(ability_names[type]);
		modifier = new AbilityModifier(this); 
	}

	public Modifier getModifier() {
		return modifier;
	}
}
