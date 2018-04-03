package gamesystem;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ItemDefinition {
	static Set<ItemDefinition> items = new HashSet<>();

	String name;
	String slot;
	String cost;
	String weight;
	List<Attack> attacks;
	Armor armor;
	Shield shield;

	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		StringBuilder s = new StringBuilder();
		s.append(name).append(" (").append(slot).append(")");
		if (cost != null && cost.length() > 0) s.append(" ").append(cost);
		if (weight != null && weight.length() > 0) s.append(" ").append(weight);
		return s.toString();
	}

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

	public static Set<ItemDefinition> getShields() {
		Set<ItemDefinition> shields = new HashSet<>();
		for (ItemDefinition item : items) {
			if (item.shield != null) {
				shields.add(item);
			}
		}
		return shields;
	}

	public static Set<ItemDefinition> getArmor() {
		Set<ItemDefinition> armors = new HashSet<>();
		for (ItemDefinition item : items) {
			if (item.armor != null) {
				armors.add(item);
			}
		}
		return armors;
	}

	public static ItemDefinition getItem(String name) {
		for (ItemDefinition item : items) {
			if (item.name.compareToIgnoreCase(name) == 0) return item;
		}
		return null;
	}

	public String getShieldBonus() {
		if (shield == null) return "";
		return shield.bonus;
	}

	public String getShieldACP() {
		if (shield == null) return "";
		return shield.armorCheckPenalty;
	}

	public String getShieldSpellFailure() {
		if (shield == null) return "";
		return shield.spellFailure;
	}

	public String getWeight() {
		return weight;
	}

	public String getArmorType() {
		if (armor == null) return "";
		return armor.type;
	}

	public String getArmorBonus() {
		if (armor == null) return "";
		return armor.bonus;
	}

	public String getArmorMaxDex() {
		if (armor == null) return "";
		return armor.maxDex;
	}

	public String getArmorACP() {
		if (armor == null) return "";
		return armor.armorCheckPenalty;
	}

	public String getSpellFailure() {
		if (armor == null) return "";
		return armor.spellFailure;
	}

	public String getArmorSpeed() {
		if (armor == null) return "";
		return armor.speed;
	}

}
