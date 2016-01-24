package gamesystem;

import gamesystem.Feat.FeatDefinition;



// Feat is an instance of a FeatDefinition that can be applied to a Creature
public class Feat extends Feature<Feat, FeatDefinition> {
	public boolean bonus = false;	// true if this feat was added due to a class feature. eventually will want to expand to list of sources

	Feat(FeatDefinition def) {
		super(def);
	}

	public static class FeatDefinition extends FeatureDefinition<FeatDefinition> {
		public boolean repeatable = false;
		public String summary;
		public String ref;

		protected FeatDefinition(String name) {
			super(name);
		}

		protected FeatDefinition(String name, boolean repeat) {
			super(name);
			repeatable = repeat;
		}

		public Feat getFeat() {
			Feat b = new Feat(this);

			for (Effect e : effects) {
				b.modifiers.put(e.getModifier(name), e.target);
			}
			return b;
		}

		protected FeatDefinition addSkillBonuses(String skill1, String skill2) {
			addFixedEffect(Creature.STATISTIC_SKILLS + "." + skill1, null, 2, null);
			addFixedEffect(Creature.STATISTIC_SKILLS + "." + skill2, null, 2, null);
			return this;
		}

		FeatDefinition summary(String summary) {
			this.summary = summary;
			return this;
		}

		FeatDefinition ref(String ref) {
			this.ref = ref;
			return this;
		}
	}

	public static FeatDefinition getFeatDefinition(String name) {
		for (FeatDefinition f : Feat.FEATS) {
			if (f.name.equals(name)) return f;
		}
		return null;
	}

	public static final String FEAT_MULTI_WEAPON_FIGHTING = "Multiweapon Fighting";
	public static final String FEAT_MULTIATTACK = "Multiattack";
	public static final String FEAT_POWER_ATTACK = "Power Attack";
	public static final String FEAT_COMBAT_EXPERTISE = "Combat Expertise";
	public static final String FEAT_WEAPON_FINESSE = "Weapon Finesse";
	public static final String FEAT_GREATER_TWO_WEAPON_FIGHTING = "Greater Two-Weapon Fighting";
	public static final String FEAT_IMPROVED_TWO_WEAPON_FIGHTING = "Improved Two-Weapon Fighting";
	public static final String FEAT_TWO_WEAPON_FIGHTING = "Two-Weapon Fighting";

	public static FeatDefinition[] FEATS = {
		/*
		 * Buff-like feats:
		 */
		new FeatDefinition("Acrobatic").addSkillBonuses("Jump", "Tumble").summary("+2 bonus on Jump and Tumble checks.").ref("PH 89"),
		new FeatDefinition("Agile").addSkillBonuses("Balance", "Escape Artist").summary("+2 bonus on Balance and Escape Artist checks.").ref("PH 89"),
		new FeatDefinition("Alertness").addSkillBonuses("Listen", "Spot").summary("+2 bonus on Listen and Spot checks.").ref("PH 89"),
		new FeatDefinition("Animal Affinity").addSkillBonuses("Handle Animal", "Ride").summary("+2 bonus on Handle Animal and Ride checks.").ref("PH 89"),
		new FeatDefinition("Athletic").addSkillBonuses("Climb", "Swim").summary("+2 bonus on Climb and Swim checks.").ref("PH 89"),
		new FeatDefinition("Combat Casting").summary("+4 bonus on Concentration checks for defensive casting.").ref("PH 92"),	// TODO needs ability checks
		new FeatDefinition("Deceitful").addSkillBonuses("Disguise", "Forgery").summary("+2 bonus on Disguise and Forgery checks.").ref("PH 93"),
		new FeatDefinition("Deft Hands").addSkillBonuses("Slight of Hand", "Use Rope").summary("+2 bonus on Sleight of Hand and Use Rope checks.").ref("PH 93"),
		new FeatDefinition("Diligent").addSkillBonuses("Appraise", "Decipher Script").summary("+2 bonus on Appraise and Decipher Script checks.").ref("PH 93"),
		new FeatDefinition("Great Fortitude").addBonus(Creature.STATISTIC_FORTITUDE_SAVE, 2).summary("+2 bonus on Fortitude saves.").ref("PH 94"),
		new FeatDefinition("Improved Initiative").addBonus(Creature.STATISTIC_INITIATIVE, 4).summary("+4 bonus on initiative checks.").ref("PH 96"),
		new FeatDefinition("Improved Grapple").addBonus(Creature.STATISTIC_GRAPPLE, 4).summary("+4 bonus on grapple checks; no attack of opportunity.").ref("PH 95"),		// +4 grapple check
		new FeatDefinition("Investigator").addSkillBonuses("Gather Information", "Search").summary("+2 bonus on Gather Information and Search checks.").ref("PH 96"),
		new FeatDefinition("Iron Will").addBonus(Creature.STATISTIC_WILL_SAVE, 2).summary("+2 bonus on Will saves.").ref("PH 97"),
		new FeatDefinition("Lightning Reflexes").addBonus(Creature.STATISTIC_REFLEX_SAVE, 2).summary("+2 bonus on Reflex saves.").ref("PH 97"),
		new FeatDefinition("Magical Aptitude").addSkillBonuses("Spellcraft", "Use Magic Device").summary("+2 bonus on Spellcraft and Use Magic Device checks.").ref("PH 97"),
		new FeatDefinition("Negotiator").addSkillBonuses("Diplomacy", "Sense Motive").summary("+2 bonus on Diplomacy and Sense Motive checks.").ref("PH 98"),
		new FeatDefinition("Nimble Fingers").addSkillBonuses("Disable Device", "Open Locks").summary("+2 bonus on Disable Device and Open Lock checks.").ref("PH 98"),
		new FeatDefinition("Persuasive").addSkillBonuses("Bluff", "Intimidate").summary("+2 bonus on Bluff and Intimidate checks.").ref("PH 98"),
		new FeatDefinition("Self-Sufficient").addSkillBonuses("Heal", "Survival").summary("+2 bonus on Heal and Survival checks.").ref("PH 100"),
		new FeatDefinition("Skill Focus", true).summary("+3 bonus on checks with selected skill.").ref("PH 100"),
		new FeatDefinition("Stealthy").addSkillBonuses("Hide", "Move Silently").summary("+2 bonus on Hide and Move Silently checks.").ref("PH 101"),
		new FeatDefinition("Toughness", true).summary("The character gains +3 hit points.").ref("PH 101"),	// TODO modifier to HPs, but non-temporary
		new FeatDefinition("Run").addBonus(Creature.STATISTIC_SKILLS + ".Jump", 4, "with running start").summary("Run at 5 times normal speed, +4 bonus on Jump checks made after running start.")
		.ref("PH 99"),	// TODO also affects run speed multiplier
		/*
		 * combat ralated feats:
		 */
		new FeatDefinition("Armor Proficiency (light)").summary("No armor check penalty on attack rolls.").ref("PH 89"),
		new FeatDefinition("Armor Proficiency (medium)").summary("No armor check penalty on attack rolls.").ref("PH 89"),
		new FeatDefinition("Armor Proficiency (heavy)").summary("No armor check penalty on attack rolls.").ref("PH 89"),
		new FeatDefinition("Shield Proficiency").summary("No armor check penalty on attack rolls.").ref("PH 100"),
		new FeatDefinition("Tower Shield Proficiency").summary("No armor check penalty on attack rolls.").ref("PH 101"),
		new FeatDefinition("Simple Weapon Proficiency").summary("No -4 penalty on attack rolls with simple weapons.").ref("PH 100"),
		new FeatDefinition("Martial Weapon Proficiency", true).summary("No penalty on attacks with specific martial weapon.").ref("PH 97"),
		new FeatDefinition("Exotic Weapon Proficiency", true).summary("No penalty on attacks with specific exotic weapon.").ref("PH 94"),		// specific weapon type

		// attack
		new FeatDefinition(Feat.FEAT_COMBAT_EXPERTISE).summary("Trade attack bonus for AC (max 5 points).").ref("PH 92"),	// choose 1-5 // also affects ac
		new FeatDefinition("Rapid Shot").summary("One extra ranged attack each round.").ref("PH 99"),	// extra atack at top bonus, all attacks -2
		new FeatDefinition("Manyshot").summary("Shoot two or more arrows simultaneously.").ref("PH 97"),	// choose 2-4, though note BAB requirements
		new FeatDefinition(Feat.FEAT_POWER_ATTACK).summary("Trade attack bonus for damage (up to base attack bonus).").ref("PH 98"),		// choose 1-BAB
		new FeatDefinition(Feat.FEAT_TWO_WEAPON_FIGHTING).summary("Reduce two-weapon fighting penalties by 2.").ref("PH 102"),
		new FeatDefinition(Feat.FEAT_IMPROVED_TWO_WEAPON_FIGHTING).summary("Gain second off-hand attack.").ref("PH 96"),
		new FeatDefinition(Feat.FEAT_GREATER_TWO_WEAPON_FIGHTING).summary("Gain third off-hand attack.").ref("PH 95"),
		new FeatDefinition(Feat.FEAT_WEAPON_FINESSE).summary("Use Dex modifier instead of Str modifier on attack rolls with light melee weapons.").ref("PH 102"),
		new FeatDefinition("Weapon Focus", true).summary("+1 bonus on attack rolls with selected weapon.").ref("PH 102"),	// specific weapon type
		new FeatDefinition("Greater Weapon Focus", true).summary("+1 bonus on attack rolls with selected weapon.").ref("PH 95"),	// specific weapon type

		// ac
		new FeatDefinition("Dodge").summary("+1 dodge bonus to AC against selected target.").ref("PH 93"),
		new FeatDefinition("Improved Shield Bash").summary("Retain shield bonus to AC when shield bashing.").ref("PH 96"),		// ac unaffected by shield bash
		new FeatDefinition("Two-Weapon Defense").summary("Off-hand weapon grants +1 shield bonus to AC.").ref("PH 102"),

		// damage
		new FeatDefinition("Improved Critical", true).summary("Double threat range of weapon.").ref("PH 95"),		// specific weapon type
		new FeatDefinition("Weapon Specialization", true).summary("+2 bonus on damage rolls with selected weapon.").ref("PH 102"),		// specific weapon type
		new FeatDefinition("Greater Weapon Specialization", true).summary("+2 bonus on damage rolls with selected weapon.").ref("PH 95"),		// specific weapon type

		// other:
		new FeatDefinition("Improved Unarmed Strike").summary("Considered armed even when unarmed.").ref("PH 96"),		// deals lethal or non-lethal damage

		// but with no effect in AssistantDM:
		new FeatDefinition("Blind-Fight").summary("Reroll miss chance for concealment").ref("PH 89"),
		new FeatDefinition("Improved Disarm").summary("+4 bonus on disarm attempts; no attack of opportunity.").ref("PH 95"),
		new FeatDefinition("Improved Feint").summary("Feint in combat as move action.").ref("PH 95"),
		new FeatDefinition("Improved Trip").summary("+4 bonus on trip attempts; no attack of opportunity.").ref("PH 96"),
		new FeatDefinition("Whirlwind Attack").summary("One melee attack against each opponent within reach.").ref("PH 102"),
		new FeatDefinition("Combat Reflexes").summary("Additional attacks of opportunity.").ref("PH 92"),
		new FeatDefinition("Mobility").summary("+4 dodge bonus to AC against some attacks of opportunity.").ref("PH 98"),
		new FeatDefinition("Spring Attack").summary("Move before and after melee attack.").ref("PH 100"),
		new FeatDefinition("Deflect Arrows").summary("Deflect one ranged attack per round.").ref("PH 93"),
		new FeatDefinition("Snatch Arrows").summary("Catch a deflected range attack.").ref("PH 100"),
		new FeatDefinition("Stunning Fist").summary("Stun opponent with unarmed strike.").ref("PH 101"),
		new FeatDefinition("Mounted Combat").summary("Negate hits on mount with Ride check.").ref("PH 98"),
		new FeatDefinition("Mounted Archery").summary("Half penalty for ranged attacks while mounted.").ref("PH 98"),
		new FeatDefinition("Ride-By Attack").summary("Move before and after a mounted charge.").ref("PH 99"),
		new FeatDefinition("Trample").summary("Target cannot avoid mounted overrun.").ref("PH 101"),
		new FeatDefinition("Spirited Charge").summary("Double damage with mounted charge.").ref("PH 100"),
		new FeatDefinition("Point Blank Shot").summary("+1 bonus on ranged attack and damage within 30 ft.").ref("PH 98"),
		new FeatDefinition("Far Shot").summary("Increase range increment by 50% or 100%.").ref("PH 94"),
		new FeatDefinition("Precise Shot").summary("You can shoot or throw at opponents engaged in melee without the –4 penalty.").ref("PH 98"),
		new FeatDefinition("Improved Precise Shot").summary("Ignore less than total cover/concealment on ranged attacks.").ref("PH 96"),
		new FeatDefinition("Shot on the Run").summary("Move before and after ranged attack.").ref("PH 100"),
		new FeatDefinition("Cleave").summary("Extra melee attack after dropping target.").ref("PH 92"),
		new FeatDefinition("Great Cleave").summary("No limit to cleave attacks each round.").ref("PH 94"),
		new FeatDefinition("Improved Bull Rush").summary("+4 bonus on bull rush attempts; no attack of opportunity.").ref("PH 95"),
		new FeatDefinition("Improved Overrun").summary("+4 bonus on overrun attempts").ref("PH 96"),
		new FeatDefinition("Improved Sunder").summary("+4 bonus on sunder attempts; no attack of opportunity.").ref("PH 96"),
		new FeatDefinition("Rapid Reload").summary("Reload crossbows more quickly.").ref("PH 99"),
		/*
		 * others:
		 */
		new FeatDefinition("Augment Summoning").summary("Summoned creatures gain +4 Str, +4 Con.").ref("PH 89"),
		new FeatDefinition("Endurance").summary("+4 bonus on checks or saves to resist nonlethal damage.").ref("PH 93"),
		new FeatDefinition("Diehard").summary("Remain conscious at -1 to -9 hp.").ref("PH 93"),
		new FeatDefinition("Eschew Materials").summary("Cast spells without material components.").ref("PH 94"),
		new FeatDefinition("Extra Turning").summary("Can turn or rebuke 4 more times per day.").ref("PH 94"),
		new FeatDefinition("Improved Counterspell").summary("Counterspell with spell of same school.").ref("PH 95"),
		new FeatDefinition("Improved Familiar").summary("As long as you are able to acquire a new familiar, you may choose your new familiar from a nonstandard list.").ref("DMG 200"),
		new FeatDefinition("Improved Turning").summary("+1 level for turning checks.").ref("PH 96"),
		new FeatDefinition("Leadership").summary("Attract cohort and followers.").ref("PH 97"),
		new FeatDefinition("Natural Spell").summary("Cast spells while in wild shape.").ref("PH 98"),
		new FeatDefinition("Quick Draw").summary("Draw weapon as free action.").ref("PH 98"),
		new FeatDefinition("Spell Focus", true).summary("+1 bonus on save DCs against specific school of magic.").ref("PH 100"),
		new FeatDefinition("Greater Spell Focus", true).summary("+2 bonus on save DCs against specific school of magic.").ref("PH 94"),
		new FeatDefinition("Spell Mastery", true).summary("Can prepare some spells without spellbook.").ref("PH 100"),
		new FeatDefinition("Spell Penetration").summary("+2 bonus on caster level checks to defeat spell resistance.").ref("PH 100"),
		new FeatDefinition("Greater Spell Penetration").summary("+4 bonus on caster level checks to defeat spell resistance.").ref("PH 94"),
		new FeatDefinition("Track").summary("Use Survival skill to track.").ref("PH 101"),
		/*
		 * metamagic
		 */
		new FeatDefinition("Scribe Scroll").summary("Create magic scrolls.").ref("PH 99"),
		new FeatDefinition("Empower Spell").summary("Increase spell's variable, numeric effects by 50%.").ref("PH 93"),
		new FeatDefinition("Enlarge Spell").summary("Double spell's range.").ref("PH 94"),
		new FeatDefinition("Extend Spell").summary("Double spell's duration.").ref("PH 94"),
		new FeatDefinition("Heighten Spell").summary("Cast spells as higher level.").ref("PH 95"),
		new FeatDefinition("Maximize Spell").summary("Maximize spell's variable, numeric effects.").ref("PH 97"),
		new FeatDefinition("Quicken Spell").summary("Cast spells as free action.").ref("PH 98"),
		new FeatDefinition("Silent Spell").summary("Cast spells without verbal components.").ref("PH 100"),
		new FeatDefinition("Still Spell").summary("Cast spells without somatic components.").ref("PH 101"),
		new FeatDefinition("Widen Spell").summary("Double spell's area.").ref("PH 102"),
		/*
		 * crafting
		 */
		new FeatDefinition("Brew Potion").summary("Create magic potions.").ref("PH 89"),
		new FeatDefinition("Craft Magic Arms and Armor").summary("Create magic weapons, armor, and shields.").ref("PH 92"),
		new FeatDefinition("Craft Rod").summary("Create magic rods.").ref("PH 92"),
		new FeatDefinition("Craft Staff").summary("Create magic staves.").ref("PH 92"),
		new FeatDefinition("Craft Wand").summary("Create magic wands.").ref("PH 92"),
		new FeatDefinition("Craft Wondrous Item").summary("Create magic wondrous items.").ref("PH 92"),
		new FeatDefinition("Forge Ring").summary("Create magic rings.").ref("PH 94")
	};
}
