package gamesystem;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/*
 * Skills is a compound Statistic - it represents all possible skills for a creature. Modifiers can be added to this class,
 * they will apply to all skills.
 * 
 * Unlike other modifiers, this class fires property change events for each skill name (not just "value").
 * "value" property changes denote changes to global skills modifiers (or ability modifiers). All skills should be considered updated
 */
public class Skills extends Statistic {
	Map<SkillType,Skill> skills = new HashMap<SkillType,Skill>();
	Modifier[] abilityMods;

	PropertyChangeListener abilityListener = new PropertyChangeListener() {
		public void propertyChange(PropertyChangeEvent evt) {
			// for ability modifier changes. sends event to indicate all skills need updating 
			pcs.firePropertyChange("value", null, null);
		}
	};

	public Skills(AbilityScore[] abilities) {
		super("Skills");
		abilityMods = new Modifier[abilities.length];
		for (int i = 0; i < abilities.length; i++) {
			abilityMods[i] = abilities[i].getModifier();
			abilityMods[i].addPropertyChangeListener(abilityListener);
		}
	}

	public void setRanks(SkillType s, float r) {
		Skill skill = skills.get(s);
		if (skill == null) {
			skill = new Skill(s,abilityMods[s.ability]);
			skills.put(s,skill);
		}
		if (skill.ranks != r) {
			//int oldValue = getValue(s);
			skill.ranks = r;
			int newValue = getValue(s);
			pcs.firePropertyChange(skill.name, null, newValue);
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

	public int getValue(SkillType s) {
		Skill skill = skills.get(s);
		Set<Modifier> mods = new HashSet<Modifier>(modifiers);
		if (skill == null) {
			mods.add(abilityMods[s.ability]);
			return super.getModifiersTotal(mods, null);
		} else {
			mods.addAll(skill.modifiers);
			return (int)skill.ranks + getModifiersTotal(mods, null);
		}
	}

	public Map<Modifier,Boolean> getModifiers(SkillType s) {
		Skill skill = skills.get(s);
		Set<Modifier> mods = new HashSet<Modifier>(modifiers);
		if (skill == null) {
			mods.add(abilityMods[s.ability]);
		} else {
			mods.addAll(skill.modifiers);
		}
		return getModifiers(mods);
	}

	public void addModifier(SkillType s, Modifier m) {
		Skill skill = skills.get(s);
		if (skill == null) {
			skill = new Skill(s,abilityMods[s.ability]);
			skills.put(s,skill);
		}

		//int oldValue = getValue(s);
		m.addPropertyChangeListener(listener);
		skill.modifiers.add(m);
		int newValue = getValue(s);
		pcs.firePropertyChange(s.name, null, newValue);
	}

	public void removeModifier(SkillType s, Modifier m) {
		Skill skill = skills.get(s);
		if (skill == null) {
			skill = new Skill(s,abilityMods[s.ability]);
			skills.put(s,skill);
		}

		//int oldValue = getValue(s);
		skill.modifiers.remove(m);
		m.removePropertyChangeListener(listener);
		int newValue = getValue(s);
		pcs.firePropertyChange(s.name, null, newValue);
	}

	// returns all skills with ranks > 0
	public Set<SkillType> getTrainedSkills() {
		Set<SkillType> trained = new HashSet<SkillType>();
		for (SkillType type : skills.keySet()) {
			Skill s = skills.get(type);
			if (s.ranks > 0f) trained.add(type);
		}
		return trained;
	}
}
