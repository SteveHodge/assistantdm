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
	public AbilityScore.Type ability;
	protected boolean trainedOnly;
	protected boolean armorCheckPenaltyApplies;
	protected boolean doubleACP;

	private SkillType(String name, AbilityScore.Type ability, boolean trained, boolean acp, boolean doubleACP) {
		this.name = name;
		this.ability = ability;
		trainedOnly = trained;
		armorCheckPenaltyApplies = acp;
		this.doubleACP = doubleACP;
	}

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

	public static AbilityScore.Type getAbilityForSkill(String skill) {
		SkillType s = getSkill(skill);
		if (s != null) return s.ability;
		return null;	// TODO should probably throw exception
	}

	public static SkillType getSkill(String n) {
		SkillType s = skills.get(n.toLowerCase());
		if (s == null) s = addSkill(n);
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
		addSkill("Appraise",AbilityScore.Type.INTELLIGENCE,false);
		addSkill("Balance",AbilityScore.Type.DEXTERITY,false,true);
		addSkill("Bluff",AbilityScore.Type.CHARISMA,false);
		addSkill("Climb",AbilityScore.Type.STRENGTH,false,true);
		addSkill("Concentration",AbilityScore.Type.CONSTITUTION,false);
		addSkill("Craft",AbilityScore.Type.INTELLIGENCE,false);
		addSkill("Craft (Armorsmithing)",AbilityScore.Type.INTELLIGENCE,false);
		addSkill("Craft (Weaponsmithing)",AbilityScore.Type.INTELLIGENCE,false);
		addSkill("Decipher Script",AbilityScore.Type.INTELLIGENCE,true);
		addSkill("Diplomacy",AbilityScore.Type.CHARISMA,false);
		addSkill("Disable Device",AbilityScore.Type.INTELLIGENCE,true);
		addSkill("Disguise",AbilityScore.Type.CHARISMA,false);
		addSkill("Escape Artist",AbilityScore.Type.DEXTERITY,false,true);
		addSkill("Forgery",AbilityScore.Type.INTELLIGENCE,false);
		addSkill("Gather Information",AbilityScore.Type.CHARISMA,false);
		addSkill("Handle Animal",AbilityScore.Type.CHARISMA,true);
		addSkill("Heal",AbilityScore.Type.WISDOM,false);
		addSkill("Hide",AbilityScore.Type.DEXTERITY,false,true);
		addSkill("Intimidate",AbilityScore.Type.CHARISMA,false);
		addSkill("Jump",AbilityScore.Type.STRENGTH,false,true);
		addSkill("Knowledge (Arcana)",AbilityScore.Type.INTELLIGENCE,true);
		addSkill("Knowledge (Arch and Eng)",AbilityScore.Type.INTELLIGENCE,true);
		addSkill("Knowledge (Dungeoneering)",AbilityScore.Type.INTELLIGENCE,true);
		addSkill("Knowledge (Geography)",AbilityScore.Type.INTELLIGENCE,true);
		addSkill("Knowledge (History)",AbilityScore.Type.INTELLIGENCE,true);
		addSkill("Knowledge (Local)",AbilityScore.Type.INTELLIGENCE,true);
		addSkill("Knowledge (Nature)",AbilityScore.Type.INTELLIGENCE,true);
		addSkill("Knowledge (Nobility)",AbilityScore.Type.INTELLIGENCE,true);
		addSkill("Knowledge (Machinery)",AbilityScore.Type.INTELLIGENCE,true);
		addSkill("Knowledge (Religion)",AbilityScore.Type.INTELLIGENCE,true);
		addSkill("Knowledge (The Planes)",AbilityScore.Type.INTELLIGENCE,true);
		addSkill("Listen",AbilityScore.Type.WISDOM,false);
		addSkill("Move Silently",AbilityScore.Type.DEXTERITY,false,true);
		addSkill("Open Lock",AbilityScore.Type.DEXTERITY,true);
		addSkill("Perform",AbilityScore.Type.CHARISMA,false);
		addSkill("Profession",AbilityScore.Type.WISDOM,true);
		addSkill("Ride",AbilityScore.Type.DEXTERITY,false);
		addSkill("Search",AbilityScore.Type.INTELLIGENCE,false);
		addSkill("Sense Motive",AbilityScore.Type.WISDOM,false);
		addSkill("Sleight of Hand",AbilityScore.Type.DEXTERITY,true,true);
		addSkill("Spellcraft",AbilityScore.Type.INTELLIGENCE,true);
		addSkill("Spot",AbilityScore.Type.WISDOM,false);
		addSkill("Survival",AbilityScore.Type.WISDOM,false);
		addSkill("Swim",AbilityScore.Type.STRENGTH,false, true, true);
		addSkill("Tumble",AbilityScore.Type.DEXTERITY,true,true);
		addSkill("Use Magic Device",AbilityScore.Type.CHARISMA,true);
		addSkill("Use Rope",AbilityScore.Type.DEXTERITY,false);
	}
}
