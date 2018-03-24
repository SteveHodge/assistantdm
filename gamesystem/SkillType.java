package gamesystem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import gamesystem.StatisticsCollection.StatisticDescription;

public class SkillType implements Comparable<SkillType> {
	public static Map<String, SkillType> skills = new HashMap<>();

	protected String name;
	public AbilityScore.Type ability;
	protected boolean trainedOnly;
	protected boolean armorCheckPenaltyApplies;
	protected boolean doubleACP;

	SkillType(String name) {
		this.name = name;
		skills.put(name.toLowerCase(), this);
	}

	private SkillType(String name, AbilityScore.Type ability, boolean trained, boolean acp, boolean doubleACP) {
		this.name = name;
		this.ability = ability;
		trainedOnly = trained;
		armorCheckPenaltyApplies = acp;
		this.doubleACP = doubleACP;
	}

	@Override
	public String toString() {
		return name;
	}

	public String getName() {
		return name;
	}

	public AbilityScore.Type getAbility() {
		return ability;
	}

	public boolean isTrainedOnly() {
		return trainedOnly;
	}

	public static StatisticDescription[] getStatistics() {
		StatisticDescription[] targets = new StatisticDescription[skills.size()];
		int i = 0;
		for (String t : skills.keySet()) {
			SkillType s = skills.get(t);
			targets[i++] = new StatisticDescription(s.name, t);
		}
		return targets;
	}

	public static AbilityScore.Type getAbilityForSkill(String skill) {
		SkillType s = getSkill(skill);
		if (s != null) return s.ability;
		return null;	// TODO should probably throw exception
	}

	public static SkillType getSkill(String n) {
		SkillType s = skills.get(n.toLowerCase());
		if (s == null) {
			System.err.println("Unknown skill " + n + " added");
			s = addSkill(n);
		}
		return s;
	}

	public static SkillType addSkill(String name) {
		AbilityScore.Type ability = null;
		if (name.startsWith("Profession")) ability = AbilityScore.Type.WISDOM;
		else if (name.startsWith("Craft")) ability = AbilityScore.Type.INTELLIGENCE;
		else if (name.startsWith("Knowledge")) ability = AbilityScore.Type.INTELLIGENCE;
		return addSkill(name,ability,true);
	}

	public static SkillType addSkill(String name, AbilityScore.Type ability, boolean trained) {
		return addSkill(name, ability, trained, false, false);
	}

	public static SkillType addSkill(String name, AbilityScore.Type ability, boolean trained, boolean acp) {
		return addSkill(name, ability, trained, acp, false);
	}

	public static SkillType addSkill(String name, AbilityScore.Type ability, boolean trained, boolean acp, boolean doubleACP) {
		SkillType s = new SkillType(name, ability, trained, acp, doubleACP);
		skills.put(s.name.toLowerCase(), s);
		return s;
	}

	@Override
	public int compareTo(SkillType arg0) {
		return name.compareToIgnoreCase(arg0.name);
	}

	public static Iterator<SkillType> iterator() {
		List<SkillType> s = new ArrayList<>(skills.values());
		Collections.sort(s);
		return s.iterator();
	}

	public static Set<SkillType> getUntrainedSkills() {
		Set<SkillType> u = new HashSet<>();
		for (SkillType s : skills.values()) {
			if (!s.trainedOnly) u.add(s);
		}
		return u;
	}
}
