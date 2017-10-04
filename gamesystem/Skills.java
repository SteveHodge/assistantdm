package gamesystem;

import java.beans.PropertyChangeListener;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import gamesystem.core.PropertyCollection;

/*
 * Skills is a compound Statistic - it represents all possible skills for a creature. Modifiers can be added to this class,
 * they will apply to all skills.
 *
 * Unlike other modifiers, this class fires property change events for each skill name (not just "value").
 * "value" property changes denote changes to global skills modifiers (or ability modifiers). All skills should be considered updated
 */
// TODO reimplement misc as Modifier
public class Skills extends Statistic implements StatisticsCollection {
	final public Map<SkillType, Skill> skills = new HashMap<>();	// TODO public for Character.getXML. change when no longer required
	final protected EnumMap<AbilityScore.Type, Modifier> abilityMods = new EnumMap<>(AbilityScore.Type.class);
	final protected Modifier acp;

	final protected PropertyChangeListener modifierListener = evt ->
	// for ability modifier changes. sends event to indicate all skills need updating
	fireEvent();

	public Skills(Collection<AbilityScore> abilities, Modifier acp, PropertyCollection parent) {
		super("skills", "Skills", parent);
		for (AbilityScore a : abilities) {
			abilityMods.put(a.type, a.getModifier());
			a.getModifier().addPropertyChangeListener(modifierListener);
		}
		this.acp = acp;
		acp.addPropertyChangeListener(modifierListener);
	}

	@Override
	public StatisticDescription[] getStatistics() {
		StatisticDescription[] targets = SkillType.getStatistics();
		for (StatisticDescription skill : targets) {
			skill.target = Creature.STATISTIC_SKILLS + "." + skill.target;
		}
		return targets;
	}

	public Skill getSkill(SkillType s) {
		Skill skill = skills.get(s);
		if (skill == null) {
			skill = new Skill(s, abilityMods.get(s.ability), acp, getParent());
			skills.put(s,skill);
		}
		return skill;
	}

	public void setRanks(SkillType s, float r) {
		Skill skill = getSkill(s);
		if (skill.ranks != r) {
			//int oldValue = getValue(s);
			skill.ranks = r;
			parent.fireEvent(skill, null);
		}
	}

	public float getRanks(SkillType s) {
		Skill skill = skills.get(s);
		if (skill == null) {
			return 0f;
		} else {
			return skill.ranks;
		}
	}

	public void setMisc(SkillType s, int m) {
		Skill skill = getSkill(s);
		if (skill.misc != m) {
			//int oldValue = getValue(s);
			skill.misc = m;
			parent.fireEvent(skill, null);
		}
	}

	public int getMisc(SkillType s) {
		Skill skill = skills.get(s);
		if (skill == null) {
			return 0;
		} else {
			return skill.misc;
		}
	}

	protected Set<Modifier> getModifiersSet(SkillType s) {
		Skill skill = skills.get(s);
		Set<Modifier> mods = new HashSet<>(modifiers);
		if (skill == null) {
			mods.add(abilityMods.get(s.ability));
			if (s.armorCheckPenaltyApplies) {
				if (s.doubleACP) {
					mods.add(new DoubleModifier(acp));
				} else {
					mods.add(acp);
				}
			}
			return mods;
		} else {
			mods.addAll(skill.modifiers);
			return mods;
		}
	}

	public int getValue(SkillType s) {
		Skill skill = skills.get(s);
		Set<Modifier> mods = getModifiersSet(s);
		if (skill == null) {
			return super.getModifiersTotal(mods, null);
		} else {
			return (int)skill.ranks + getModifiersTotal(mods, null) + skill.misc;
		}
	}

	public int getModifiersTotal(SkillType s, Set<String> excl) {
		return super.getModifiersTotalExcluding(getModifiersSet(s), excl);
	}

	// returns true if this has an active conditional modifier
	public boolean hasConditionalModifier(SkillType s) {
		Map<Modifier,Boolean> mods = getModifiers(s);
		for (Modifier m : mods.keySet()) {
			if (mods.get(m) && m.getCondition() != null) return true;
		}
		return false;
	}

	public Map<Modifier,Boolean> getModifiers(SkillType s) {
		Set<Modifier> mods = getModifiersSet(s);
		return getModifiers(mods);
	}

	public void addModifier(SkillType s, Modifier m) {
		Skill skill = getSkill(s);

		//int oldValue = getValue(s);
		m.addPropertyChangeListener(listener);
		skill.modifiers.add(m);
		parent.fireEvent(skill, null);
	}

	public void removeModifier(SkillType s, Modifier m) {
		Skill skill = skills.get(s);
		if (skill == null) return;

		//int oldValue = getValue(s);
		skill.modifiers.remove(m);
		m.removePropertyChangeListener(listener);
		parent.fireEvent(skill, null);
	}

	// returns all skills with ranks > 0
	public Set<SkillType> getTrainedSkills() {
		Set<SkillType> trained = new HashSet<>();
		for (SkillType type : skills.keySet()) {
			Skill s = skills.get(type);
			if (s.ranks > 0f) trained.add(type);
		}
		return trained;
	}

	public String getSummary(SkillType s) {
		StringBuffer text = new StringBuffer();
		text.append(getRanks(s)).append(" base<br/>");
		Map<Modifier, Boolean> mods = getModifiers(s);
		text.append(Statistic.getModifiersHTML(mods));
		text.append(getValue(s)).append(" total ").append(s.getName()).append("<br/>");
		String conds = Statistic.getModifiersHTML(mods, true);
		if (conds.length() > 0) text.append("<br/>").append(conds);
		return text.toString();
	}
}
