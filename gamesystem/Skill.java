package gamesystem;

public class Skill extends Statistic {
	protected SkillType skillType;
	protected float ranks = 0;

	public Skill(SkillType type, AbilityScore ability) {
		super(type.getName());
		skillType = type;
		addModifier(ability.getModifier());
	}

	public float getRanks() {
		return ranks;
	}

	public void setRanks(float r) {
		int oldValue = getValue();
		ranks = r;
		baseValue = (int)r;
		int newValue = getValue();
		//System.out.println(name+".setBaseValue("+v+"). Old = "+oldValue+", new = "+newValue);
		if (oldValue != newValue) pcs.firePropertyChange("value", oldValue, newValue);
	}
}
