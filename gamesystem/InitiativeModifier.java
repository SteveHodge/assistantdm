package gamesystem;


public class InitiativeModifier extends Statistic {
	protected int baseValue = 0;

	public InitiativeModifier(AbilityScore dex) {
		super("Initiative");
		addModifier(dex.getModifier());
	}

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
		pcs.firePropertyChange("value", null, newValue);
	}
}
