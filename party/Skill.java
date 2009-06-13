package party;

import java.util.HashMap;
import java.util.Map;

public class Skill {
	protected static Map<String,Integer> skill_ability_map;

	static {
		skill_ability_map = new HashMap<String,Integer>();
		skill_ability_map.put("Appraise",Creature.ABILITY_INTELLIGENCE);
		skill_ability_map.put("Balance",Creature.ABILITY_DEXTERITY);
		skill_ability_map.put("Bluff",Creature.ABILITY_CHARISMA);
		skill_ability_map.put("Climb",Creature.ABILITY_STRENGTH);
		skill_ability_map.put("Concentration",Creature.ABILITY_CONSTITUTION);
		skill_ability_map.put("Craft",Creature.ABILITY_INTELLIGENCE);
		skill_ability_map.put("Decipher Script",Creature.ABILITY_INTELLIGENCE);
		skill_ability_map.put("Diplomacy",Creature.ABILITY_CHARISMA);
		skill_ability_map.put("Disable Device",Creature.ABILITY_INTELLIGENCE);
		skill_ability_map.put("Disguise",Creature.ABILITY_CHARISMA);
		skill_ability_map.put("Escape Artist",Creature.ABILITY_DEXTERITY);
		skill_ability_map.put("Forgery",Creature.ABILITY_INTELLIGENCE);
		skill_ability_map.put("Gather Information",Creature.ABILITY_CHARISMA);
		skill_ability_map.put("Handle Animal",Creature.ABILITY_CHARISMA);
		skill_ability_map.put("Heal",Creature.ABILITY_WISDOM);
		skill_ability_map.put("Hide",Creature.ABILITY_DEXTERITY);
		skill_ability_map.put("Intimidate",Creature.ABILITY_CHARISMA);
		skill_ability_map.put("Jump",Creature.ABILITY_STRENGTH);
		skill_ability_map.put("Knowledge (Arcana)",Creature.ABILITY_INTELLIGENCE);
		skill_ability_map.put("Knowledge (Arch and Eng)",Creature.ABILITY_INTELLIGENCE);
		skill_ability_map.put("Knowledge (Dungeoneering)",Creature.ABILITY_INTELLIGENCE);
		skill_ability_map.put("Knowledge (Geography)",Creature.ABILITY_INTELLIGENCE);
		skill_ability_map.put("Knowledge (History)",Creature.ABILITY_INTELLIGENCE);
		skill_ability_map.put("Knowledge (Local)",Creature.ABILITY_INTELLIGENCE);
		skill_ability_map.put("Knowledge (Nature)",Creature.ABILITY_INTELLIGENCE);
		skill_ability_map.put("Knowledge (Nobility)",Creature.ABILITY_INTELLIGENCE);
		skill_ability_map.put("Knowledge (Machinery)",Creature.ABILITY_INTELLIGENCE);
		skill_ability_map.put("Knowledge (Religion)",Creature.ABILITY_INTELLIGENCE);
		skill_ability_map.put("Knowledge (The Planes)",Creature.ABILITY_INTELLIGENCE);
		skill_ability_map.put("Listen",Creature.ABILITY_WISDOM);
		skill_ability_map.put("Move Silently",Creature.ABILITY_DEXTERITY);
		skill_ability_map.put("Open Lock",Creature.ABILITY_DEXTERITY);
		skill_ability_map.put("Perform",Creature.ABILITY_CHARISMA);
		skill_ability_map.put("Profession",Creature.ABILITY_WISDOM);
		skill_ability_map.put("Ride",Creature.ABILITY_DEXTERITY);
		skill_ability_map.put("Search",Creature.ABILITY_INTELLIGENCE);
		skill_ability_map.put("Sense Motive",Creature.ABILITY_WISDOM);
		skill_ability_map.put("Sleight of Hand",Creature.ABILITY_DEXTERITY);
		skill_ability_map.put("Spellcraft",Creature.ABILITY_INTELLIGENCE);
		skill_ability_map.put("Spot",Creature.ABILITY_WISDOM);
		skill_ability_map.put("Survival",Creature.ABILITY_WISDOM);
		skill_ability_map.put("Swim",Creature.ABILITY_STRENGTH);
		skill_ability_map.put("Tumble",Creature.ABILITY_DEXTERITY);
		skill_ability_map.put("Use Magic Device",Creature.ABILITY_CHARISMA);
		skill_ability_map.put("Use Rope",Creature.ABILITY_DEXTERITY);
	}

	public static int getAbilityForSkill(String skill) {
		if (!skill_ability_map.containsKey(skill)) return -1;
		return skill_ability_map.get(skill);
	}
}
