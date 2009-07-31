package party;

import java.util.HashMap;
import java.util.Map;

public class Skill implements Comparable<Skill> {
	protected static Map<String,Skill> skills;

	protected String name;
	protected int ability;

	private Skill(String name, int ability) {
		this.name = name;
		this.ability = ability;
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
		return addSkill(name,-1);
	}

	public static Skill addSkill(String name, int ability) {
		Skill s = new Skill(name, ability);
		skills.put(s.name.toLowerCase(), s);
		return s;
	}

	public int compareTo(Skill arg0) {
		return name.compareToIgnoreCase(arg0.name);
	}

	static {
		skills = new HashMap<String,Skill>();
		addSkill("Appraise",Creature.ABILITY_INTELLIGENCE);
		addSkill("Balance",Creature.ABILITY_DEXTERITY);
		addSkill("Bluff",Creature.ABILITY_CHARISMA);
		addSkill("Climb",Creature.ABILITY_STRENGTH);
		addSkill("Concentration",Creature.ABILITY_CONSTITUTION);
		addSkill("Craft",Creature.ABILITY_INTELLIGENCE);
		addSkill("Decipher Script",Creature.ABILITY_INTELLIGENCE);
		addSkill("Diplomacy",Creature.ABILITY_CHARISMA);
		addSkill("Disable Device",Creature.ABILITY_INTELLIGENCE);
		addSkill("Disguise",Creature.ABILITY_CHARISMA);
		addSkill("Escape Artist",Creature.ABILITY_DEXTERITY);
		addSkill("Forgery",Creature.ABILITY_INTELLIGENCE);
		addSkill("Gather Information",Creature.ABILITY_CHARISMA);
		addSkill("Handle Animal",Creature.ABILITY_CHARISMA);
		addSkill("Heal",Creature.ABILITY_WISDOM);
		addSkill("Hide",Creature.ABILITY_DEXTERITY);
		addSkill("Intimidate",Creature.ABILITY_CHARISMA);
		addSkill("Jump",Creature.ABILITY_STRENGTH);
		addSkill("Knowledge (Arcana)",Creature.ABILITY_INTELLIGENCE);
		addSkill("Knowledge (Arch and Eng)",Creature.ABILITY_INTELLIGENCE);
		addSkill("Knowledge (Dungeoneering)",Creature.ABILITY_INTELLIGENCE);
		addSkill("Knowledge (Geography)",Creature.ABILITY_INTELLIGENCE);
		addSkill("Knowledge (History)",Creature.ABILITY_INTELLIGENCE);
		addSkill("Knowledge (Local)",Creature.ABILITY_INTELLIGENCE);
		addSkill("Knowledge (Nature)",Creature.ABILITY_INTELLIGENCE);
		addSkill("Knowledge (Nobility)",Creature.ABILITY_INTELLIGENCE);
		addSkill("Knowledge (Machinery)",Creature.ABILITY_INTELLIGENCE);
		addSkill("Knowledge (Religion)",Creature.ABILITY_INTELLIGENCE);
		addSkill("Knowledge (The Planes)",Creature.ABILITY_INTELLIGENCE);
		addSkill("Listen",Creature.ABILITY_WISDOM);
		addSkill("Move Silently",Creature.ABILITY_DEXTERITY);
		addSkill("Open Lock",Creature.ABILITY_DEXTERITY);
		addSkill("Perform",Creature.ABILITY_CHARISMA);
		addSkill("Profession",Creature.ABILITY_WISDOM);
		addSkill("Ride",Creature.ABILITY_DEXTERITY);
		addSkill("Search",Creature.ABILITY_INTELLIGENCE);
		addSkill("Sense Motive",Creature.ABILITY_WISDOM);
		addSkill("Sleight of Hand",Creature.ABILITY_DEXTERITY);
		addSkill("Spellcraft",Creature.ABILITY_INTELLIGENCE);
		addSkill("Spot",Creature.ABILITY_WISDOM);
		addSkill("Survival",Creature.ABILITY_WISDOM);
		addSkill("Swim",Creature.ABILITY_STRENGTH);
		addSkill("Tumble",Creature.ABILITY_DEXTERITY);
		addSkill("Use Magic Device",Creature.ABILITY_CHARISMA);
		addSkill("Use Rope",Creature.ABILITY_DEXTERITY);
	}
}
