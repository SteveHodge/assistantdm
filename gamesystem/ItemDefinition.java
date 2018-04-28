package gamesystem;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ItemDefinition {
	public enum SlotType {
		RING, HEAD, BODY, FACE, TORSO, HANDS, NECK, WAIST, ARMS, SHOULDERS, FEET;

		private SlotType() {
			description = super.toString().toLowerCase();
		}

		@Override
		public String toString() {return description;}

		// XXX brute force implementation - could keep a map
		public static SlotType getSlot(String d) {
			for (SlotType t : values()) {
				if (t.description.equals(d)) return t;
			}
			return null;	// TODO probably better to throw an exception
		}

		private final String description;
	}

	static Set<ItemDefinition> items = new HashSet<>();

	String name;
	String slot;
	String cost;
	String weight;
	List<Attack> attacks;
	Armor armor;
	Shield shield;
	BuffFactory buff;

	public String getName() {
		return name;
	}

	public BuffFactory getBuffFactory() {
		return buff;
	}

	@Override
	public String toString() {
		return name;
	}

	public String getSummary() {
		StringBuilder s = new StringBuilder();
		s.append(name).append(" (").append(slot).append(")");
		if (cost != null && cost.length() > 0) s.append(" ").append(cost);
		if (weight != null && weight.length() > 0) s.append(" ").append(weight);
		return s.toString();
	}

	public class Attack {
		String proficiency;
		String type;
		String damage;
		String critical;
		String damageType;
		String range;

		public ItemDefinition getItem() {
			return ItemDefinition.this;
		}

		public String getDamageType() {
			return damageType;
		}

		public String getCritical() {
			return critical;
		}

		public String getRange() {
			return range;
		}

		public String getDamage() {
			return damage;
		}

		public String getWeaponType() {
			return type;
		}
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

	public static Set<ItemDefinition> getAttacks() {
		Set<ItemDefinition> attacks = new HashSet<>();
		for (ItemDefinition item : items) {
			if (item.attacks != null && item.attacks.size() > 0) {
				attacks.add(item);
			}
		}
		return attacks;
	}

	public static Set<ItemDefinition> getItemsForSlot(SlotType slot) {
		Set<ItemDefinition> items = new HashSet<>();
		for (ItemDefinition item : ItemDefinition.items) {
			if (item.slot != null && slot.description.compareToIgnoreCase(item.slot) == 0) {
				items.add(item);
			}
		}
		return items;
	}

	public static ItemDefinition getItem(String name) {
		for (ItemDefinition item : items) {
			if (item.name.compareToIgnoreCase(name) == 0) return item;
		}
		return null;
	}

	public Attack getAttack(int i) {
		return attacks.get(i);
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
