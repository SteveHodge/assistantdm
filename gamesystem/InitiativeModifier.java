package gamesystem;

import gamesystem.core.PropertyCollection;
import gamesystem.core.PropertyEvent;
import gamesystem.core.SettableProperty;

// TODO will probably want to switch to overrides rather than this being a SettableProperty once overrides on statistics are implemented
public class InitiativeModifier extends Statistic implements SettableProperty<Integer> {
	protected int baseValue = 0;

	public InitiativeModifier(AbilityScore dex, PropertyCollection parent) {
		super("initiative", "Initiative", parent);
		if (dex != null) addModifier(dex.getModifier());
	}

	@Override
	public Integer getRegularValue() {
		return baseValue + getModifiersTotal();
	}

	@Override
	public int getBaseValue() {
		return baseValue;
	}

	@Override
	public String getSummary() {
		StringBuilder text = new StringBuilder();
		text.append(getBaseValue()).append(" base<br/>");
		text.append(super.getSummary());
		return text.toString();
	}

	@Override
	public void setValue(Integer val) {
		if (baseValue == val.intValue()) return;
		int old = getValue();
		baseValue = val;
		fireEvent(createEvent(PropertyEvent.VALUE_CHANGED, old));
	}
}
