package gamesystem;

import gamesystem.core.PropertyCollection;

// TODO shouldn't be able to set the baseValue, instead use overrides
public class InitiativeModifier extends Statistic {
	protected int baseValue = 0;

	public InitiativeModifier(AbilityScore dex, PropertyCollection parent) {
		super("initiative", "Initiative", parent);
		if (dex != null) addModifier(dex.getModifier());
	}

	@Override
	public Integer getValue() {
		return baseValue + super.getValue();
	}

	@Override
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
