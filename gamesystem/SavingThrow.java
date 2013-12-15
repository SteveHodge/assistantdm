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
	private int baseValue = 0;

	// TODO verify that the ability is the correct one. alternatively pass all ability scores
	public SavingThrow(Type type, AbilityScore ability) {
		super(type.toString());
		this.type = type;
		if (ability != null) addModifier(ability.getModifier());
	}

	public Type getType() {
		return type;
	}

	@Override
	public int getValue() {
		return baseValue + super.getValue();
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

	@Override
	public String getSummary() {
		StringBuilder text = new StringBuilder();
		text.append(getBaseValue()).append(" base<br/>");
		text.append(super.getSummary());
		return text.toString();
	}
}
