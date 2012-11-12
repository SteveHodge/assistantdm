package gamesystem;

public class Feat {
	public String name;

	public Feat(String name) {
		this.name = name;
	}

	public boolean equals(Object obj) {
		if (!(obj instanceof Feat)) return false;
		return name.equals(((Feat)obj).name);
	}

	public int hashCode() {
		return name.hashCode();
	}

	public String toString() {
		return name;
	}

	public static final String FEAT_TWO_WEAPON_FIGHTING = "Two-Weapon Fighting";
	public static final String FEAT_IMPROVED_TWO_WEAPON_FIGHTING = "Improved Two-Weapon Fighting";
	public static final String FEAT_GREATER_TWO_WEAPON_FIGHTING = "Greater Two-Weapon Fighting";
	public static final String FEAT_WEAPON_FINESSE = "Weapon Finesse";
	public static final String FEAT_COMBAT_EXPERTISE = "Combat Expertise";
	public static final String FEAT_POWER_ATTACK = "Power Attack";

	public static Feat[] FEATS = { 
		/*
		 * Buff-like feats:
		 */
		new Feat("Acrobatic"),
		new Feat("Agile"),
		new Feat("Alertness"),
		new Feat("Animal Affinity"),
		new Feat("Athletic"),
		new Feat("Combat Casting"),
		new Feat("Deceitful"),
		new Feat("Deft Hands"),
		new Feat("Diligent"),
		new Feat("Great Fortitude"),
		new Feat("Improved Initiative"),
		new Feat("Investigator"),
		new Feat("Iron Will"),
		new Feat("Lightning Reflexes"),
		new Feat("Magical Aptitude"),
		new Feat("Negotiator"),
		new Feat("Nimble Fingers"),
		new Feat("Persuasive"),
		new Feat("Self-Sufficient"),
		new Feat("Skill Focus"),
		new Feat("Stealthy"),
		new Feat("Toughness"),
		new Feat("Run"),
		/*
		 * combat ralated feats: 
		 */
		new Feat("Armor Proficiency (light)"),
		new Feat("Armor Proficiency (medium)"),
		new Feat("Armor Proficiency (heavy)"),
		new Feat("Shield Proficiency"),
		new Feat("Tower Shield Proficiency"),
		new Feat("Simple Weapon Proficiency"),
		new Feat("Martial Weapon Proficiency"),
		new Feat("Exotic Weapon Proficiency"),	// specific weapon type

		// attack
		new Feat(FEAT_COMBAT_EXPERTISE),	// choose 1-5 // also affects ac
		new Feat("Rapid Shot"),			// extra atack at top bonus, all attacks -2
		new Feat("Manyshot"),			// choose 2-4, though note BAB requirements
		new Feat(FEAT_POWER_ATTACK),		// choose 1-BAB
		new Feat(FEAT_TWO_WEAPON_FIGHTING),
		new Feat(FEAT_IMPROVED_TWO_WEAPON_FIGHTING),
		new Feat(FEAT_GREATER_TWO_WEAPON_FIGHTING),
		new Feat(FEAT_WEAPON_FINESSE),
		new Feat("Weapon Focus"),		// specific weapon type
		new Feat("Greater Weapon Focus"),	// specific weapon type

		// ac
		new Feat("Dodge"),
		new Feat("Improved Shield Bash"),	// ac unaffected by shield bash
		new Feat("Two-Weapon Defense"),

		// damage
		new Feat("Improved Critical"),		// specific weapon type
		new Feat("Weapon Specialization"),	// specific weapon type
		new Feat("Greater Weapon Specialization"),	// specific weapon type

		// other:
		new Feat("Improved Unarmed Strike"),	// deals lethal or non-lethal damage
		new Feat("Improved Grapple"),			// +4 grapple check
		// but with no effect in AssistantDM:
		new Feat("Blind-Fight"),
		new Feat("Improved Disarm"),
		new Feat("Improved Feint"),
		new Feat("Improved Trip"),
		new Feat("Whirlwind Attack"),
		new Feat("Combat Reflexes"),
		new Feat("Mobility"),
		new Feat("Spring Attack"),
		new Feat("Deflect Arrows"),
		new Feat("Snatch Arrows"),
		new Feat("Improved Unarmed Strike"),
		new Feat("Stunning Fist"),
		new Feat("Mounted Combat"),
		new Feat("Mounted Archery"),
		new Feat("Ride-By Attack"),
		new Feat("Spirited Charge"),
		new Feat("Trample"),
		new Feat("Point Blank Shot"),
		new Feat("Far Shot"),
		new Feat("Precise Shot"),
		new Feat("Shot on the Run"),
		new Feat("Improved Precise Shot"),
		new Feat("Cleave"),
		new Feat("Great Cleave"),
		new Feat("Improved Bull Rush"),
		new Feat("Improved Overrun"),
		new Feat("Improved Sunder"),
		new Feat("Rapid Reload"),
		/*
		 * others:
		 */
		new Feat("Augment Summoning"),
		new Feat("Endurance"),
		new Feat("Diehard Endurance"),
		new Feat("Eschew Materials"),
		new Feat("Extra Turning"),
		new Feat("Improved Counterspell"),
		new Feat("Improved Turning"),
		new Feat("Leadership"),
		new Feat("Natural Spell"),
		new Feat("Quick Draw"),
		new Feat("Spell Focus"),
		new Feat("Greater Spell Focus"),
		new Feat("Spell Mastery"),
		new Feat("Spell Penetration"),
		new Feat("Greater Spell Penetration"),
		new Feat("Track"),
		/*
		 * metamagic
		 */
		new Feat("Scribe Scroll"),
		new Feat("Empower Spell"),
		new Feat("Enlarge Spell"),
		new Feat("Extend Spell"),
		new Feat("Heighten Spell"),
		new Feat("Maximize Spell"),
		new Feat("Quicken Spell"),
		new Feat("Silent Spell"),
		new Feat("Still Spell"),
		new Feat("Widen Spell"),
		/*
		 * crafting
		 */
		new Feat("Brew Potion"),
		new Feat("Craft Magic Arms and Armor"),
		new Feat("Craft Rod"),
		new Feat("Craft Staff"),
		new Feat("Craft Wand"),
		new Feat("Craft Wondrous Item"),
		new Feat("Forge Ring"),
	};
}
