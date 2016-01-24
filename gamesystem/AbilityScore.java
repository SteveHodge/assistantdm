package gamesystem;


// XXX might have to manage ability scores together otherwise some of the other statistics that use ability
// modifiers will need special case handling for non-abilities (e.g. should use dex modifier for all attack if
// str is missing, int modifier should be used for initiative if dex is missing - and that means supplying extra
// abilities to those statistics which will almost never be used)

// TODO overrides are a bit unintuituve - they ignore modifiers, which is fine, but the modifiers are reapplied once the override is removed (which at the moment happens if the override is set to the baseValue in the ui)
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

	public static int getModifier(int score) {
		return score/2-5;
	}

	protected class AbilityModifier extends AbstractModifier {
		public AbilityModifier() {
			AbilityScore.this.addPropertyChangeListener(evt ->
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
			return name;
		}

		@Override
		public String toString() {
			StringBuilder s = new StringBuilder();
			if (getModifier() >= 0) s.append("+");
			s.append(getModifier()).append(" ").append(type).append(" modifier ");
			return s.toString();
		}
	};

	public AbilityScore(Type type) {
		super(type.toString());
		this.type = type;
		modifier = new AbilityModifier();
	}

	public Type getType() {
		return type;
	}

	@Override
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

	// returns the "normal" value of the ability score (the base value + any modifiers that apply)
	// if no override is set then this will be equal to getValue()
	public int getRegularValue() {
		return baseValue + super.getValue();
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

	public Modifier getModifier() {
		return modifier;
	}

	public int getModifierValue() {
		return modifier.getModifier();
	}

	@Override
	public String getSummary() {
		StringBuilder text = new StringBuilder();
		if (getOverride() > 0) text.append("<s>");
		text.append(getBaseValue()).append(" base<br/>");
		text.append(super.getSummary());

		if (getOverride() > 0) {
			text.append("</s><br/>").append(getOverride()).append(" current ").append(getName()).append(" (override)");
		}

		text.append("<br/>");
		if (getModifierValue() >= 0) text.append("+");
		text.append(getModifierValue()).append(" ").append(getName()).append(" modifier");
		return text.toString();
	}
}
