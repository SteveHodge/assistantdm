package gamesystem;

import java.util.Map;

import gamesystem.core.PropertyCollection;

// TODO should this be inner class of Skills?
// TODO reimplement misc as modifiers
public class Skill extends Statistic {
	private SkillType skillType;
	float ranks = 0;
	int misc;

//	public Skill(SkillType type, AbilityScore ability, Modifier acp) {
//		this(type, ability.getModifier(), acp);
//	}

	protected Skill(SkillType type, Modifier abilityMod, Modifier acp, PropertyCollection parent) {
		super(type.getName(), parent);
		skillType = type;
		addModifier(abilityMod);
		if (type.armorCheckPenaltyApplies) {
			if (type.doubleACP) acp = new DoubleModifier(acp);
			addModifier(acp);
		}
	}

	public SkillType getSkillType() {
		return skillType;
	}

	public float getRanks() {
		return ranks;
	}

	@Override
	public Integer getValue() {
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

	@Override
	public String getSummary() {
		StringBuffer text = new StringBuffer();
		text.append(getRanks()).append(" base<br/>");
		Map<Modifier, Boolean> mods = getModifiers();
		text.append(Statistic.getModifiersHTML(mods));
		text.append(getValue()).append(" total ").append(getName()).append("<br/>");
		String conds = Statistic.getModifiersHTML(mods, true);
		if (conds.length() > 0) text.append("<br/>").append(conds);
		return text.toString();
	}
}
