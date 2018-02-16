package gamesystem;

import gamesystem.core.PropertyCollection;

// FIXME implement overrides so the monster combat entry works properly
public class InitiativeModifier extends Statistic {
	protected int baseValue = 0;

	public InitiativeModifier(AbilityScore dex, PropertyCollection parent) {
		super("initiative", "Initiative", parent);
		if (dex != null) addModifier(dex.getModifier());
	}

	@Override
	public Integer getRegularValue() {
		return baseValue + super.getValue();
	}

	public Integer getBaseValue() {
		return baseValue;
	}

	public void setBaseValue(int v) {
		//int oldValue = getValue();
		baseValue = v;
		//System.out.println(name+".setBaseValue("+v+"). Old = "+oldValue+", new = "+newValue);
		fireEvent();	// TODO oldvalue
	}

	@Override
	public String getSummary() {
		StringBuilder text = new StringBuilder();
		text.append(getBaseValue()).append(" base<br/>");
		text.append(super.getSummary());
		return text.toString();
	}
}
