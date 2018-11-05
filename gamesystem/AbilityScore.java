package gamesystem;

import gamesystem.core.PropertyCollection;
import gamesystem.core.PropertyEvent;
import gamesystem.core.SimpleValueProperty;

// XXX might have to manage ability scores together otherwise some of the other statistics that use ability
// modifiers will need special case handling for non-abilities (e.g. should use dex modifier for all attack if
// str is missing, int modifier should be used for initiative if dex is missing - and that means supplying extra
// abilities to those statistics which will almost never be used)

// TODO overrides are a bit unintuituve - they ignore modifiers, which is fine, but the modifiers are reapplied once the override is removed (which at the moment happens if the override is set to the baseValue in the ui)
// FIXME base value and overrides should not be reimplemented here
// TODO for level/hd progression we need to know the score with permanent modifiers (such as from race, levelling up, magic books that grant inherent bonuses etc) but not with temporary modifiers (e.g. from spells or magic items)
public class AbilityScore extends Statistic {
	public enum Type {
		STRENGTH("Strength"),
		DEXTERITY("Dexterity"),
		CONSTITUTION("Constitution"),
		INTELLIGENCE("Intelligence"),
		WISDOM("Wisdom"),
		CHARISMA("Charisma");

		private Type(String d) {
			description = d;
		}

		@Override
		public String toString() {return description;}

		public String getAbbreviation() {
			return description.substring(0, 3).toUpperCase();
		}

		// XXX brute force implementation - could keep a map
		public static Type getAbilityType(String d) {
			for (Type t : values()) {
				if (t.description.equals(d)) return t;
			}
			return null;	// TODO probably better to throw an exception
		}

		private final String description;
	}

	protected final Modifier modifier;
	protected Type type;
	protected int baseValue = 0;
	protected int override = -1;
	SimpleValueProperty<Integer> damage;
	SimpleValueProperty<Integer> drain;

	public static int getModifier(int score) {
		return score/2-5;
	}

	protected class AbilityModifier extends AbstractModifier {
		public AbilityModifier() {
			AbilityScore.this.addPropertyListener(e ->
			//int oldValue = ((Integer)evt.getOldValue())/2-5;
			//int newValue = ((Integer)evt.getNewValue())/2-5;
			//modpcs.firePropertyChange("value", null, newValue);
			pcs.firePropertyChange("value", null, getModifier()));
		}

		@Override
		public int getModifier() {
			return AbilityScore.getModifier(getValue());
		}

		@Override
		public String getType() {
			return type.toString();
		}

		@Override
		public String toString() {
			StringBuilder s = new StringBuilder();
			if (getModifier() >= 0) s.append("+");
			s.append(getModifier()).append(" ").append(type).append(" modifier ");
			return s.toString();
		}
	};

	public AbilityScore(Type type, PropertyCollection parent) {
		super("ability_scores." + type.toString().toLowerCase(), type.toString(), parent);
		this.type = type;
		modifier = new AbilityModifier();
		damage = new SimpleValueProperty<Integer>(getName() + ".damage", parent, 0);
		drain = new SimpleValueProperty<Integer>(getName() + ".drain", parent, 0);
	}

	public Type getType() {
		return type;
	}

	// returns the regular value (see getRegularValue()) if no override is set or else the override value
	@Override
	public Integer getValue() {
		if (override == -1) {
			return getRegularValue();
		} else {
			return override;
		}
	}

	@Override
	public int getBaseValue() {
		return baseValue;
	}

	// returns the "normal" value of the ability score (the base value + any modifiers that apply less any drain or damage)
	@Override
	public Integer getRegularValue() {
		return baseValue + getModifiersTotal() - damage.getValue() - drain.getValue();
	}

	public void setBaseValue(int v) {
		int oldValue = getValue();
		baseValue = v;
//		System.out.println(name + ".setBaseValue(" + v + "). Old = " + oldValue + ", new = " + getValue() + ", getBaseValue = " + getBaseValue());
		fireEvent(createEvent(PropertyEvent.VALUE_CHANGED, oldValue));
	}

	public void setOverride(int v) {
		if (override != v) {
			override = v;
			PropertyEvent e = createEvent(PropertyEvent.OVERRIDE_ADDED);
			fireEvent(e);
		}
	}

	public int getOverride() {
		return override;
	}

	public void clearOverride() {
		if (override != -1) {
			override = -1;
			fireEvent(createEvent(PropertyEvent.OVERRIDE_REMOVED));
		}
	}

	public Modifier getModifier() {
		return modifier;
	}

	public int getModifierValue() {
		return modifier.getModifier();
	}

	public SimpleValueProperty<Integer> getDrain() {
		return drain;
	}

	public SimpleValueProperty<Integer> getDamage() {
		return damage;
	}

	public void healDamage(int h) {
		if (h > damage.getValue()) h = damage.getValue();
		if (h <= 0) return;
		damage.setValue(damage.getValue() - h);
	}

	@Override
	public String getSummary() {
		StringBuilder text = new StringBuilder();
		if (getOverride() > 0) text.append("<s>");
		text.append(getBaseValue()).append(" base<br/>");
		text.append(super.getSummary());
		if (drain.getValue() != 0)
			text.append(-drain.getValue()).append(" drained<br/>");
		if (damage.getValue() != 0)
			text.append(-damage.getValue()).append(" damage<br/>");

		if (getOverride() > 0) {
			text.append("</s><br/>").append(getOverride()).append(" current ").append(getDescription()).append(" (override)");
		}

		text.append("<br/>");
		if (getModifierValue() >= 0) text.append("+");
		text.append(getModifierValue()).append(" ").append(getDescription()).append(" modifier");
		return text.toString();
	}
}
