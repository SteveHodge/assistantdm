package party;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Skill implements Comparable<Skill> {
	protected static Map<String,Skill> skills;

	protected String name;
	protected int ability;
	protected boolean trainedOnly;

	private Skill(String name, int ability, boolean trained) {
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
		Skill s = getSkill(skill);
		if (s != null) return s.ability;
		return -1;
	}

	public static Skill getSkill(String n) {
		Skill s = skills.get(n.toLowerCase());
		if (s == null) s = addSkill(n);
		return s;
	}

	public static Skill addSkill(String name) {
		int ability = -1;
		if (name.startsWith("Profession")) ability = Creature.ABILITY_WISDOM;
		else if (name.startsWith("Craft")) ability = Creature.ABILITY_INTELLIGENCE;
		else if (name.startsWith("Knowledge")) ability = Creature.ABILITY_INTELLIGENCE;
		return addSkill(name,ability,true);
	}

	public static Skill addSkill(String name, int ability, boolean trained) {
		Skill s = new Skill(name, ability, trained);
		skills.put(s.name.toLowerCase(), s);
		return s;
	}

	public int compareTo(Skill arg0) {
		return name.compareToIgnoreCase(arg0.name);
	}

	public static Iterator<Skill> iterator() {
		List<Skill> s = new ArrayList<Skill>(skills.values());
		Collections.sort(s);
		return s.iterator();
	}

	public static Set<Skill> getUntrainedSkills() {
		Set<Skill> u = new HashSet<Skill>();
		for (Skill s : skills.values()) {
			if (!s.trainedOnly) u.add(s);
		}
		return u;
	}

	static {
		skills = new HashMap<String,Skill>();
		addSkill("Appraise",Creature.ABILITY_INTELLIGENCE,false);
		addSkill("Balance",Creature.ABILITY_DEXTERITY,false);
		addSkill("Bluff",Creature.ABILITY_CHARISMA,false);
		addSkill("Climb",Creature.ABILITY_STRENGTH,false);
		addSkill("Concentration",Creature.ABILITY_CONSTITUTION,false);
		addSkill("Craft",Creature.ABILITY_INTELLIGENCE,false);
		addSkill("Decipher Script",Creature.ABILITY_INTELLIGENCE,true);
		addSkill("Diplomacy",Creature.ABILITY_CHARISMA,false);
		addSkill("Disable Device",Creature.ABILITY_INTELLIGENCE,true);
		addSkill("Disguise",Creature.ABILITY_CHARISMA,false);
		addSkill("Escape Artist",Creature.ABILITY_DEXTERITY,false);
		addSkill("Forgery",Creature.ABILITY_INTELLIGENCE,false);
		addSkill("Gather Information",Creature.ABILITY_CHARISMA,false);
		addSkill("Handle Animal",Creature.ABILITY_CHARISMA,true);
		addSkill("Heal",Creature.ABILITY_WISDOM,false);
		addSkill("Hide",Creature.ABILITY_DEXTERITY,false);
		addSkill("Intimidate",Creature.ABILITY_CHARISMA,false);
		addSkill("Jump",Creature.ABILITY_STRENGTH,false);
		addSkill("Knowledge (Arcana)",Creature.ABILITY_INTELLIGENCE,true);
		addSkill("Knowledge (Arch and Eng)",Creature.ABILITY_INTELLIGENCE,true);
		addSkill("Knowledge (Dungeoneering)",Creature.ABILITY_INTELLIGENCE,true);
		addSkill("Knowledge (Geography)",Creature.ABILITY_INTELLIGENCE,true);
		addSkill("Knowledge (History)",Creature.ABILITY_INTELLIGENCE,true);
		addSkill("Knowledge (Local)",Creature.ABILITY_INTELLIGENCE,true);
		addSkill("Knowledge (Nature)",Creature.ABILITY_INTELLIGENCE,true);
		addSkill("Knowledge (Nobility)",Creature.ABILITY_INTELLIGENCE,true);
		addSkill("Knowledge (Machinery)",Creature.ABILITY_INTELLIGENCE,true);
		addSkill("Knowledge (Religion)",Creature.ABILITY_INTELLIGENCE,true);
		addSkill("Knowledge (The Planes)",Creature.ABILITY_INTELLIGENCE,true);
		addSkill("Listen",Creature.ABILITY_WISDOM,false);
		addSkill("Move Silently",Creature.ABILITY_DEXTERITY,false);
		addSkill("Open Lock",Creature.ABILITY_DEXTERITY,true);
		addSkill("Perform",Creature.ABILITY_CHARISMA,false);
		addSkill("Profession",Creature.ABILITY_WISDOM,true);
		addSkill("Ride",Creature.ABILITY_DEXTERITY,false);
		addSkill("Search",Creature.ABILITY_INTELLIGENCE,false);
		addSkill("Sense Motive",Creature.ABILITY_WISDOM,false);
		addSkill("Sleight of Hand",Creature.ABILITY_DEXTERITY,true);
		addSkill("Spellcraft",Creature.ABILITY_INTELLIGENCE,true);
		addSkill("Spot",Creature.ABILITY_WISDOM,false);
		addSkill("Survival",Creature.ABILITY_WISDOM,false);
		addSkill("Swim",Creature.ABILITY_STRENGTH,false);
		addSkill("Tumble",Creature.ABILITY_DEXTERITY,true);
		addSkill("Use Magic Device",Creature.ABILITY_CHARISMA,true);
		addSkill("Use Rope",Creature.ABILITY_DEXTERITY,false);
	}
}
