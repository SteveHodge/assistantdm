package gamesystem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SkillType implements Comparable<SkillType> {
	public static Map<String,SkillType> skills;

	protected String name;
	public int ability;
	protected boolean trainedOnly;

	private SkillType(String name, int ability, boolean trained) {
		this.name = name;
		this.ability = ability;
		trainedOnly = trained;
	}

	public String toString() {
		return name;
	}

	public String getName() {
		return name;
	}

	public int getAbility() {
		return ability;
	}

	public boolean isTrainedOnly() {
		return trainedOnly;
	}

	public static int getAbilityForSkill(String skill) {
		SkillType s = getSkill(skill);
		if (s != null) return s.ability;
		return -1;
	}

	public static SkillType getSkill(String n) {
		SkillType s = skills.get(n.toLowerCase());
		if (s == null) s = addSkill(n);
		return s;
	}

	public static SkillType addSkill(String name) {
		int ability = -1;
		if (name.startsWith("Profession")) ability = AbilityScore.ABILITY_WISDOM;
		else if (name.startsWith("Craft")) ability = AbilityScore.ABILITY_INTELLIGENCE;
		else if (name.startsWith("Knowledge")) ability = AbilityScore.ABILITY_INTELLIGENCE;
		return addSkill(name,ability,true);
	}

	public static SkillType addSkill(String name, int ability, boolean trained) {
		SkillType s = new SkillType(name, ability, trained);
		skills.put(s.name.toLowerCase(), s);
		return s;
	}

	public int compareTo(SkillType arg0) {
		return name.compareToIgnoreCase(arg0.name);
	}

	public static Iterator<SkillType> iterator() {
		List<SkillType> s = new ArrayList<SkillType>(skills.values());
		Collections.sort(s);
		return s.iterator();
	}

	public static Set<SkillType> getUntrainedSkills() {
		Set<SkillType> u = new HashSet<SkillType>();
		for (SkillType s : skills.values()) {
			if (!s.trainedOnly) u.add(s);
		}
		return u;
	}

	static {
		skills = new HashMap<String,SkillType>();
		addSkill("Appraise",AbilityScore.ABILITY_INTELLIGENCE,false);
		addSkill("Balance",AbilityScore.ABILITY_DEXTERITY,false);
		addSkill("Bluff",AbilityScore.ABILITY_CHARISMA,false);
		addSkill("Climb",AbilityScore.ABILITY_STRENGTH,false);
		addSkill("Concentration",AbilityScore.ABILITY_CONSTITUTION,false);
		addSkill("Craft",AbilityScore.ABILITY_INTELLIGENCE,false);
		addSkill("Decipher Script",AbilityScore.ABILITY_INTELLIGENCE,true);
		addSkill("Diplomacy",AbilityScore.ABILITY_CHARISMA,false);
		addSkill("Disable Device",AbilityScore.ABILITY_INTELLIGENCE,true);
		addSkill("Disguise",AbilityScore.ABILITY_CHARISMA,false);
		addSkill("Escape Artist",AbilityScore.ABILITY_DEXTERITY,false);
		addSkill("Forgery",AbilityScore.ABILITY_INTELLIGENCE,false);
		addSkill("Gather Information",AbilityScore.ABILITY_CHARISMA,false);
		addSkill("Handle Animal",AbilityScore.ABILITY_CHARISMA,true);
		addSkill("Heal",AbilityScore.ABILITY_WISDOM,false);
		addSkill("Hide",AbilityScore.ABILITY_DEXTERITY,false);
		addSkill("Intimidate",AbilityScore.ABILITY_CHARISMA,false);
		addSkill("Jump",AbilityScore.ABILITY_STRENGTH,false);
		addSkill("Knowledge (Arcana)",AbilityScore.ABILITY_INTELLIGENCE,true);
		addSkill("Knowledge (Arch and Eng)",AbilityScore.ABILITY_INTELLIGENCE,true);
		addSkill("Knowledge (Dungeoneering)",AbilityScore.ABILITY_INTELLIGENCE,true);
		addSkill("Knowledge (Geography)",AbilityScore.ABILITY_INTELLIGENCE,true);
		addSkill("Knowledge (History)",AbilityScore.ABILITY_INTELLIGENCE,true);
		addSkill("Knowledge (Local)",AbilityScore.ABILITY_INTELLIGENCE,true);
		addSkill("Knowledge (Nature)",AbilityScore.ABILITY_INTELLIGENCE,true);
		addSkill("Knowledge (Nobility)",AbilityScore.ABILITY_INTELLIGENCE,true);
		addSkill("Knowledge (Machinery)",AbilityScore.ABILITY_INTELLIGENCE,true);
		addSkill("Knowledge (Religion)",AbilityScore.ABILITY_INTELLIGENCE,true);
		addSkill("Knowledge (The Planes)",AbilityScore.ABILITY_INTELLIGENCE,true);
		addSkill("Listen",AbilityScore.ABILITY_WISDOM,false);
		addSkill("Move Silently",AbilityScore.ABILITY_DEXTERITY,false);
		addSkill("Open Lock",AbilityScore.ABILITY_DEXTERITY,true);
		addSkill("Perform",AbilityScore.ABILITY_CHARISMA,false);
		addSkill("Profession",AbilityScore.ABILITY_WISDOM,true);
		addSkill("Ride",AbilityScore.ABILITY_DEXTERITY,false);
		addSkill("Search",AbilityScore.ABILITY_INTELLIGENCE,false);
		addSkill("Sense Motive",AbilityScore.ABILITY_WISDOM,false);
		addSkill("Sleight of Hand",AbilityScore.ABILITY_DEXTERITY,true);
		addSkill("Spellcraft",AbilityScore.ABILITY_INTELLIGENCE,true);
		addSkill("Spot",AbilityScore.ABILITY_WISDOM,false);
		addSkill("Survival",AbilityScore.ABILITY_WISDOM,false);
		addSkill("Swim",AbilityScore.ABILITY_STRENGTH,false);
		addSkill("Tumble",AbilityScore.ABILITY_DEXTERITY,true);
		addSkill("Use Magic Device",AbilityScore.ABILITY_CHARISMA,true);
		addSkill("Use Rope",AbilityScore.ABILITY_DEXTERITY,false);
	}
}
