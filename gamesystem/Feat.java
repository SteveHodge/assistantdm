package gamesystem;


// TODO should the Buff produced be the instance of the Feat that is applied to a character? probably...
public class Feat extends BuffFactory {
	public boolean repeatable = false;

	protected Feat(String name) {
		super(name);
	}

	protected Feat(String name, boolean repeat) {
		super(name);
		repeatable = repeat;
	}

	protected Feat addSkillBonuses(String skill1, String skill2) {
		addEffect(Creature.STATISTIC_SKILLS+"."+skill1, null, 2);
		addEffect(Creature.STATISTIC_SKILLS+"."+skill2, null, 2);
		return this;
	}

	protected Feat addBonus(String target, int modifier, String condition) {
		addEffect(target, null, modifier, condition);
		return this;
	}

	protected Feat addBonus(String target, int modifier) {
		addEffect(target, null, modifier);
		return this;
	}

	public static final String FEAT_TWO_WEAPON_FIGHTING = "Two-Weapon Fighting";
	public static final String FEAT_IMPROVED_TWO_WEAPON_FIGHTING = "Improved Two-Weapon Fighting";
	public static final String FEAT_GREATER_TWO_WEAPON_FIGHTING = "Greater Two-Weapon Fighting";
	public static final String FEAT_WEAPON_FINESSE = "Weapon Finesse";
	public static final String FEAT_COMBAT_EXPERTISE = "Combat Expertise";
	public static final String FEAT_POWER_ATTACK = "Power Attack";
	public static final String FEAT_MULTIATTACK = "Multiattack";
	public static final String FEAT_MULTI_WEAPON_FIGHTING = "Multiweapon Fighting";
	public static final String FEAT_IMPROVED_GRAPPLE = "Improved Grapple";

	public static Feat[] FEATS = {
		/*
		 * Buff-like feats:
		 */
		(new Feat("Acrobatic")).addSkillBonuses("Jump", "Tumble"),
		(new Feat("Agile")).addSkillBonuses("Balance", "Escape Artist"),
		(new Feat("Alertness")).addSkillBonuses("Listen", "Spot"),
		(new Feat("Animal Affinity")).addSkillBonuses("Handle Animal", "Ride"),
		(new Feat("Athletic")).addSkillBonuses("Climb", "Swim"),
		new Feat("Combat Casting"),	// TODO needs ability checks
		(new Feat("Deceitful")).addSkillBonuses("Disguise", "Forgery"),
		(new Feat("Deft Hands")).addSkillBonuses("Slight of Hand", "Use Rope"),
		(new Feat("Diligent")).addSkillBonuses("Appraise", "Decipher Script"),
		(new Feat("Great Fortitude")).addBonus(Creature.STATISTIC_FORTITUDE_SAVE, 2),
		(new Feat("Improved Initiative")).addBonus(Creature.STATISTIC_INITIATIVE, 4),
		(new Feat("Investigator")).addSkillBonuses("Gather Information", "Search"),
		(new Feat("Iron Will")).addBonus(Creature.STATISTIC_WILL_SAVE, 2),
		(new Feat("Lightning Reflexes")).addBonus(Creature.STATISTIC_REFLEX_SAVE, 2),
		(new Feat("Magical Aptitude")).addSkillBonuses("Spellcraft", "Use Magic Device"),
		(new Feat("Negotiator")).addSkillBonuses("Diplomacy", "Sense Motive"),
		(new Feat("Nimble Fingers")).addSkillBonuses("Disable Device", "Open Locks"),
		(new Feat("Persuasive")).addSkillBonuses("Bluff", "Intimidate"),
		(new Feat("Self-Sufficient")).addSkillBonuses("Heal", "Survival"),
		new Feat("Skill Focus",true),
		(new Feat("Stealthy")).addSkillBonuses("Hide", "Move Silently"),
		new Feat("Toughness",true),	// TODO modifier to HPs, but non-temporary
		(new Feat("Run")).addBonus(Creature.STATISTIC_SKILLS+".Jump", 4, "with running start"),	// TODO also affects run speed multiplier
		/*
		 * combat ralated feats:
		 */
		new Feat("Armor Proficiency (light)"),
		new Feat("Armor Proficiency (medium)"),
		new Feat("Armor Proficiency (heavy)"),
		new Feat("Shield Proficiency"),
		new Feat("Tower Shield Proficiency"),
		new Feat("Simple Weapon Proficiency"),
		new Feat("Martial Weapon Proficiency",true),
		new Feat("Exotic Weapon Proficiency",true),	// specific weapon type

		// attack
		new Feat(FEAT_COMBAT_EXPERTISE),	// choose 1-5 // also affects ac
		new Feat("Rapid Shot"),			// extra atack at top bonus, all attacks -2
		new Feat("Manyshot"),			// choose 2-4, though note BAB requirements
		new Feat(FEAT_POWER_ATTACK),		// choose 1-BAB
		new Feat(FEAT_TWO_WEAPON_FIGHTING),
		new Feat(FEAT_IMPROVED_TWO_WEAPON_FIGHTING),
		new Feat(FEAT_GREATER_TWO_WEAPON_FIGHTING),
		new Feat(FEAT_WEAPON_FINESSE),
		new Feat("Weapon Focus",true),		// specific weapon type
		new Feat("Greater Weapon Focus",true),	// specific weapon type

		// ac
		new Feat("Dodge"),
		new Feat("Improved Shield Bash"),	// ac unaffected by shield bash
		new Feat("Two-Weapon Defense"),

		// damage
		new Feat("Improved Critical",true),		// specific weapon type
		new Feat("Weapon Specialization",true),	// specific weapon type
		new Feat("Greater Weapon Specialization",true),	// specific weapon type

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
		new Feat("Extra Turning",true),
		new Feat("Improved Counterspell"),
		new Feat("Improved Turning"),
		new Feat("Leadership"),
		new Feat("Natural Spell"),
		new Feat("Quick Draw"),
		new Feat("Spell Focus",true),
		new Feat("Greater Spell Focus",true),
		new Feat("Spell Mastery",true),
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
