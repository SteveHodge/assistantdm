package gamesystem;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ItemDefinition {
	static Set<ItemDefinition> items = new HashSet<>();

	String slot;
	String cost;
	String weight;
	List<Attack> attacks;
	Armor armor;
	Shield shield;

	class Attack {
		String proficiency;
		String type;
		String damage;
		String critical;
		String damageType;
	}

	class Armor {
		String type;
		String bonus;
		String maxDex;
		String armorCheckPenalty;
		String spellFailure;
		String speed;
		boolean slowRun;
	}

	class Shield {
		String bonus;
		String maxDex;
		String armorCheckPenalty;
		String spellFailure;
	}
}
