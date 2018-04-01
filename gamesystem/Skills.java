package gamesystem;

import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
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

	static class SynergyModifier extends ImmutableModifier {
		SkillType from;
		SkillType target;

		// doesn't check that the synergy actually applies to fromSkill and targetSkill
		public SynergyModifier(SkillType from, SkillType target, SkillType.Synergy synergy) {
			super(2, null, from.getName(), synergy.condition);
			this.from = from;
			this.target = target;
		}

		@Override
		public int hashCode() {
			int result = super.hashCode();
			result ^= from.getName().hashCode();
			result ^= target.getName().hashCode();
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) return true;
			if (!super.equals(obj)) return false;
			if (getClass() != obj.getClass()) return false;
			SynergyModifier other = (SynergyModifier) obj;
			return from == other.from && target == other.target;
		}
	}

	Set<SynergyModifier> synergyMods = new HashSet<>();	// the synergy modifiers that have been applied

	final protected PropertyChangeListener modifierListener = evt -> fireEvent();	// for ability modifier changes. sends event to indicate all skills need updating

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
			updateSynergies(skill);
			parent.fireEvent(skill, null);
		}
	}

	void updateSynergies(Skill changed) {
		// check synergies from this skill
		if (changed.ranks >= 5 && changed.skillType.hasSynergies()) {
			// add any synergies that should be applied if they aren't already applied
			applySynergies(changed.skillType);

		} else if (changed.skillType.hasSynergies()) {
			// less than 5 ranks. remove any applied synergies
			for (SkillType.Synergy synergy : changed.skillType.synergies) {
				SkillType toType = SkillType.getSkill(synergy.target);
				Skill toSkill = skills.get(toType);
				SynergyModifier m = new SynergyModifier(changed.skillType, toType, synergy);
				if (synergyMods.contains(m)) {
					toSkill.removeModifier(m);
					synergyMods.remove(m);
				}
			}

		}

		// if this is a trained skill with 0 ranks then remove any synergies that have been applied
		if (changed.skillType.isTrainedOnly() && changed.ranks == 0) {
			List<Modifier> toRemove = new ArrayList<>();
			for (Modifier m : changed.getModifierSet()) {
				if (m instanceof SynergyModifier) toRemove.add(m);
			}
			for (Modifier m : toRemove) {
				changed.removeModifier(m);
				synergyMods.remove(m);
			}
		}

		else if (changed.skillType.isTrainedOnly() && changed.ranks > 0) {
			// trained skill with at least one rank: check all synergies that should apply have been applied
			for (SkillType t : skills.keySet()) {
				if (t.hasSynergies() && skills.get(t).ranks >= 5) {
					applySynergies(t);	// this will apply synergies even for other skills but that should be ok
				}
			}
		}
	}

	private void applySynergies(SkillType from) {
		for (SkillType.Synergy synergy : from.synergies) {
			SkillType toType = SkillType.getSkill(synergy.target);
			Skill toSkill = skills.get(toType);
			if (!toType.isTrainedOnly() || (toSkill != null && toSkill.ranks > 0)) {
				// target is untrained or there is a rank so add the modifier if it doesn't already exist
				SynergyModifier m = new SynergyModifier(from, toType, synergy);
				if (!synergyMods.contains(m)) {
					Skill s = getSkill(toType);	// we use getSkill in case the target is an untrained skill we don't have ranks in
					s.addModifier(m);
					synergyMods.add(m);
				}
			}
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
			return (int) skill.ranks + getModifiersTotal(mods, null);
		}
	}

	public int getModifiersTotal(SkillType s, String... excl) {
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

	// TODO reimplement misc as modifiers
	public class Skill extends Statistic {
		private SkillType skillType;
		float ranks = 0;

//		public Skill(SkillType type, AbilityScore ability, Modifier acp) {
//			this(type, ability.getModifier(), acp);
//		}

		protected Skill(SkillType type, Modifier abilityMod, Modifier acp, PropertyCollection parent) {
			super("skills." + type.getName().toLowerCase(), type.getName(), parent);
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
			int v = (int) ranks;
			return v + super.getValue();
		}

		public void setRanks(float r) {
			ranks = r;
			updateSynergies(this);
			fireEvent();
		}

		@Override
		public String getSummary() {
			StringBuffer text = new StringBuffer();
			text.append(getRanks()).append(" base<br/>");
			Map<Modifier, Boolean> mods = getModifiers();
			text.append(Statistic.getModifiersHTML(mods));
			text.append(getValue()).append(" total ").append(getDescription()).append("<br/>");
			String conds = Statistic.getModifiersHTML(mods, true);
			if (conds.length() > 0) text.append("<br/>").append(conds);
			return text.toString();
		}
	}
}
