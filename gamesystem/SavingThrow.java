package gamesystem;

import gamesystem.core.PropertyCollection;
import gamesystem.core.StatisticEvent;

public class SavingThrow extends Statistic {
	public enum Type {
		FORTITUDE("Fortitude", "Fort", AbilityScore.Type.CONSTITUTION),
		REFLEX("Reflex", "Ref", AbilityScore.Type.DEXTERITY),
		WILL("Will", "Will", AbilityScore.Type.WISDOM);

		private Type(String d, String ab, AbilityScore.Type a) {
			description = d;
			abbreviation = ab;
			ability = a;
		}

		@Override
		public String toString() {return description;}

		public String getAbbreviation() {
			return abbreviation;
		}

		public AbilityScore.Type getAbilityType() {return ability;}

		public static Type getSavingThrowType(String d) {
			for (Type t : values()) {
				if (t.description.equals(d)) return t;
			}
			return null;	// TODO probably better to throw an exception
		}

		private final String description;
		private final String abbreviation;
		private final AbilityScore.Type ability;
	}

	final private Type type;
	private HitDiceProperty hitdice;
	private int baseValue = -1;		// base value override (-1 means no override)

	// TODO verify that the ability is the correct one. alternatively pass all ability scores (that would allow the rules for non-abilities to be applied)
	public SavingThrow(Type type, AbilityScore ability, HitDiceProperty hd, PropertyCollection parent) {
		super("saving_throws." + type.toString().toLowerCase(), type.toString(), parent);
		this.type = type;
		if (ability != null) addModifier(ability.getModifier());
		setHitDice(hd);
	}

	public void setHitDice(HitDiceProperty hd) {
		hitdice = hd;
		if (hitdice != null) {
			hitdice.addPropertyListener(e -> {
				fireEvent(new StatisticEvent(this, StatisticEvent.EventType.TOTAL_CHANGED));
			});
		}
	}

	public int getNonOverrideValue() {
		int value = super.getValue();
		if (hitdice != null) value += hitdice.getBaseSave(type);
		return value;
	}

	public Type getType() {
		return type;
	}

	// gets the current base value - either the level derived value or the override if any
	@Override
	public int getBaseValue() {
		if (hitdice != null && baseValue == -1) return hitdice.getBaseSave(type);
		return baseValue;
	}

	// gets the base save for the character level
	public int getCalculatedBase() {
		if (hitdice == null) return 0;
		return hitdice.getBaseSave(type);
	}

	public void setBaseOverride(int v) {
		if (baseValue != v) {
			baseValue = v;
			fireEvent(new StatisticEvent(this, StatisticEvent.EventType.TOTAL_CHANGED));	// TODO fix this, it should be a more appropriate type
		}
	}

	public void clearBaseOverride() {
		setBaseOverride(-1);
	}

	public int getBaseOverride() {
		return baseValue;
	}

	@Override
	public String getSummary() {
		StringBuilder text = new StringBuilder();
		text.append(getBaseValue()).append(" base<br/>");
		text.append(super.getSummary());
		return text.toString();
	}
}
