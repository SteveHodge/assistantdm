package gamesystem;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

// TODO overrides are a bit unintuituve - they ignore modifiers, which is fine, but the modifiers are reapplied once the override is removed (which at the moment happens if the override is set to the baseValue in the ui) 
public class AbilityScore extends Statistic {
	// TODO if these constants are really only applicable in relation to the array in Character then they should be defined there (and protected)
	public static final int ABILITY_STRENGTH = 0;
	public static final int ABILITY_DEXTERITY = 1;
	public static final int ABILITY_CONSTITUTION = 2;
	public static final int ABILITY_INTELLIGENCE = 3;
	public static final int ABILITY_WISDOM = 4;
	public static final int ABILITY_CHARISMA = 5;
	protected static final String[] ability_names = {"Strength", "Dexterity", "Constitution", "Intelligence", "Wisdom", "Charisma"};

	protected final Modifier modifier;
	protected int type;
	protected int baseValue = 0;
	protected int override = -1;

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
					//int oldValue = ((Integer)evt.getOldValue())/2-5;
					//int newValue = ((Integer)evt.getNewValue())/2-5;
					//modpcs.firePropertyChange("value", null, newValue);
					modpcs.firePropertyChange("value", null, getModifier());
				}
			});
		}

		public int getModifier() {
			return AbilityScore.getModifier(getValue());
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

		public String toString() {
			StringBuilder s = new StringBuilder();
			if (getModifier() >= 0) s.append("+");
			s.append(getModifier()).append(" ").append(getAbilityName(type)).append(" modifier ");
			return s.toString();
		}

		public String getCondition() {
			return null;
		}
	};

	// TODO bounds checking
	public AbilityScore(int type) {
		super(ability_names[type]);
		this.type = type;
		modifier = new AbilityModifier(this); 
	}

	public int getValue() {
		if (override == -1) {
			return baseValue + super.getValue();
		} else {
			return override;
		}
	}

	public int getBaseValue() {
		return baseValue;
	}

	public void setBaseValue(int v) {
		//int oldValue = getValue();
		baseValue = v;
		int newValue = getValue();
		//System.out.println(name+".setBaseValue("+v+"). Old = "+oldValue+", new = "+newValue);
		pcs.firePropertyChange("value", null, newValue);	// total maybe unchanged, but some listeners will be interested in any change to the modifiers
	}

	public void setOverride(int v) {
		if (override != v) {
			override = v;
			pcs.firePropertyChange("value", null, override);
		}
	}

	public int getOverride() {
		return override;
	}

	public void clearOverride() {
		if (override != -1) {
			override = -1;
			pcs.firePropertyChange("value", null, getValue());
		}
	}

	public int getType() {
		return type;
	}

	public Modifier getModifier() {
		return modifier;
	}

	public int getModifierValue() {
		return modifier.getModifier();
	}
}
