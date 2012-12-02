package gamesystem;

// TODO should this be inner class of Skills?
// TODO reimplement misc as modifiers
public class Skill extends Statistic {
	protected SkillType skillType;
	protected float ranks = 0;
	protected int misc;

//	public Skill(SkillType type, AbilityScore ability, Modifier acp) {
//		this(type, ability.getModifier(), acp);
//	}

	protected Skill(SkillType type, Modifier abilityMod, Modifier acp) {
		super(type.getName());
		skillType = type;
		addModifier(abilityMod);
		if (type.armorCheckPenaltyApplies) {
			if (type.doubleACP) acp = new DoubleModifier(acp);
			addModifier(acp);
		}
	}

	public float getRanks() {
		return ranks;
	}

	public int getValue() {
		int v = (int)ranks;
		return v + super.getValue() + misc;
	}

	public void setRanks(float r) {
		//int oldValue = getValue();
		ranks = r;
		int newValue = getValue();
		//System.out.println(name+".setBaseValue("+v+"). Old = "+oldValue+", new = "+newValue);
		pcs.firePropertyChange("value", null, newValue);	// total might not change, but listeners might still want to know
	}

	public void setMisc(int m) {
		misc = m;
		int newValue = getValue();
		pcs.firePropertyChange("value", null, newValue);	// total might not change, but listeners might still want to know
	}

	public int getMisc() {
		return misc;
	}
}
