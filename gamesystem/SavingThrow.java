package gamesystem;

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
	private Levels level;
	private int baseValue = -1;		// base value override (-1 means no override)

	// TODO verify that the ability is the correct one. alternatively pass all ability scores (that would allow the rules for non-abilities to be applied)
	public SavingThrow(Type type, AbilityScore ability, Levels lvl) {
		super(type.toString());
		this.type = type;
		if (ability != null) addModifier(ability.getModifier());
		level = lvl;
		if (level != null) {
			level.addPropertyChangeListener((e) -> {
				pcs.firePropertyChange("value", null, getValue());
			});
		}
	}

	public Type getType() {
		return type;
	}

	@Override
	public int getValue() {
		return getBaseValue() + super.getValue();
	}

	// gets the current base value - either the level derived value or the override if any
	public int getBaseValue() {
		if (level != null && baseValue == -1) return level.getBaseSave(type);
		return baseValue;
	}

	// gets the base save for the character level
	public int getCalculatedBase() {
		if (level == null) return 0;
		return level.getBaseSave(type);
	}

	public void setBaseOverride(int v) {
		if (baseValue != v) {
			baseValue = v;
			pcs.firePropertyChange("value", null, getValue());	// total maybe unchanged, but some listeners will be interested in any change to the modifiers
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
