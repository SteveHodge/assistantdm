package gamesystem;

import gamesystem.ClassFeature.ClassFeatureDefinition;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/* Class Features/Special Abilities
 *
 * Classes:
 * ClassFeature - instantiated class feature that's attached to a character. may have parameters with associated current values
 * ClassFeatureDefinition - definition of a class feature, including defining parameters with default values
 * LevelUpAction - abstract base class for classes that represent actions that affect class features (and feats) that happen on level up
 *   AddBonusFeatAction - add a bonus feature for a specifed list of options
 *   AddFeatureAction - add a class feature
 *   RemoveFeatureAction - remove a class feature
 *   SetParameterAction - change a parameter on an existing class feature
 *
 * ToDo:
 * Other core classes
 * Calculated parameters in output
 * Calculation system for determining variable bonuses, DCs, etc
 * UI for selecting options
 * ClassFeatures might need to track what classes they came from. this might be important for things that stack across classes
 * Extend calculated parameters to FeatDefinition
 */

// a class feature as instantiated for a particular character. tracks the values of parameters
public class ClassFeature extends Feature<ClassFeature, ClassFeatureDefinition> {
	Map<String, Object> parameters;	// maps parameter name to value
	int template = 0;	// index of current template

	ClassFeature(ClassFeatureDefinition def) {
		super(def);
	}

	public void setParameter(String param, Object val) {
		if ("template".equals(param)) {
			template = (Integer) val;
		} else {
			if (parameters == null) parameters = new HashMap<>();
			parameters.put(param, val);
		}
	}

	public String getNameAndType() {
		if (definition.type == SpecialAbilityType.NATURAL) return definition.name;
		return String.format("%s (%s)", definition.name, definition.type.getAbbreviation());
	}

	public String getSummary() {
		String out = new String(definition.summaries.get(template));
		if (parameters != null) {
			for (String param : parameters.keySet()) {
				out = out.replace("&(" + param + ")", parameters.get(param).toString());
			}
		}
		return out;
	}

	@Override
	public String toString() {
		StringBuilder b = new StringBuilder();
		b.append(definition);
		if (template != 0 || parameters != null && parameters.size() > 0) {
			String[] pieces = new String[parameters.size() + (template == 0 ? 0 : 1)];
			int i = 0;
			if (template != 0) pieces[i++] = "template: "+template;
			if (parameters != null) {
				for (String p : parameters.keySet()) {
					pieces[i++] = p + ": " + parameters.get(p);
				}
			}
			b.append(" [").append(String.join(", ", pieces)).append("]");
		}
		return b.toString();
	}

	static enum SpecialAbilityType {
		NATURAL,
		EXTRAORDINARY,
		SPELL_LIKE,
		SUPERNATURAL;

		public String getAbbreviation() {
			if (this == EXTRAORDINARY) return "Ex";
			if (this == SUPERNATURAL) return "Su";
			if (this == SPELL_LIKE) return "Sp";
			return "";
		}
	}

	// represents the definition of a class feature. generates instances of ClassFeature as required
	public static class ClassFeatureDefinition extends FeatureDefinition<ClassFeatureDefinition> {
		public String id;
		SpecialAbilityType type;
		List<String> summaries = new ArrayList<>();
		Map<String, Object> parameters = new HashMap<>();	// maps parameter name to default value

		ClassFeatureDefinition(String id, String name, SpecialAbilityType type) {
			super(name);
			this.id = id;
			this.type = type;
		}

		ClassFeatureDefinition(String id, String name) {
			this(id, name, SpecialAbilityType.NATURAL);
		}

		ClassFeatureDefinition addSummary(String summary) {
			summaries.add(summary);
			return this;
		}

		ClassFeatureDefinition addParameter(String name, Object def) {
			parameters.put(name, def);
			return this;
		}

		ClassFeature getFeature() {
			ClassFeature f = new ClassFeature(this);

			// setup any parameters
			for (String param : parameters.keySet()) {
				if (f.parameters == null) f.parameters = new HashMap<>();
				f.parameters.put(param, parameters.get(param));
			}

			// add any effects:
			for (Effect e : effects) {
				f.modifiers.put(e.getModifier(name), e.target);
			}

			return f;
		}

		@Override
		public String toString() {
			if (type == SpecialAbilityType.NATURAL) return name;
			return String.format("%s (%s)", name, type.getAbbreviation());
		}

		static ClassFeatureDefinition findFeatureFactory(String feature) {
			for (ClassFeatureDefinition f : ClassFeature.featureDefinitions) {
				if (f.id.equals(feature)) return f;
			}
			return null;
		}
	}

	static ClassFeatureDefinition[] featureDefinitions = {
		new ClassFeatureDefinition("barbarian_fast_movement", "Fast Movement", SpecialAbilityType.EXTRAORDINARY)
		.addSummary("+10 to land speed when in medium armor or lighter and not carrying a heavy load."),

		new ClassFeatureDefinition("illiteracy", "Illiteracy")
		.addSummary("Cannot read or write. Must spend 2 skill points to gain literacy or gain levels in another class."),

		new ClassFeatureDefinition("rage", "Rage", SpecialAbilityType.EXTRAORDINARY)
		.addSummary(
				"You can fly into a screaming frenzy once per encounter, up to @(times) per day. This gives +@(ability_bonus) to Strength and Constitution, and a +@(save_bonus) morale bonus to Will saves, but gives a -2 penalty to AC. Cannot use any skills that require patience or concentration while enraged. Your rage lasts up to 3 + Con mod rounds.")	// calculated
				.addParameter("times", "once")
				.addParameter("ability_bonus", 4)
				.addParameter("save_bonus", 2),

				new ClassFeatureDefinition("uncanny_dodge", "Uncanny Dodge", SpecialAbilityType.EXTRAORDINARY)
		.addSummary("You retain your Dexterity bonus to AC even if flatfooted or struck by an invisible attacker."),

		new ClassFeatureDefinition("improved_uncanny_dodge", "Improved Uncanny Dodge", SpecialAbilityType.EXTRAORDINARY)
		.addSummary("Can no longer be flanked."),

		new ClassFeatureDefinition("trap_sense", "Trap Sense", SpecialAbilityType.EXTRAORDINARY)
		.addSummary("+@(bonus) to Reflex saves and AC against traps.")
		.addParameter("bonus", 1),

		new ClassFeatureDefinition("damage_reduction", "Damage Reduction", SpecialAbilityType.EXTRAORDINARY)
		.addSummary("@(dr)/–")
		.addParameter("dr", 1),

		new ClassFeatureDefinition("indomitable_will", "Indomitable Will", SpecialAbilityType.EXTRAORDINARY)
		.addSummary("While in a rage you gain a +4 bonus on Will saves to resist Enchantment spells."),

		new ClassFeatureDefinition("tireless_rage", "Tireless Rage", SpecialAbilityType.EXTRAORDINARY)
		.addSummary("You no longer become fatigued at the end of your rage"),

		// TODO should Bardic Knowledge be treated as a skill?
		new ClassFeatureDefinition("bardic_knowledge", "Bardic Knowledge", SpecialAbilityType.EXTRAORDINARY)
		.addSummary("You can make a special knowledge check for stray bits of trivia. This check is 1d20 + bard level + Int mod."),

		// TODO maybe have some way of making bardic music the parent of the sub-powers
		new ClassFeatureDefinition("bardic_music", "Bardic Music")
		.addSummary("Performances can create varied magical effects once per day per bard level."),	// TODO calculated value

		new ClassFeatureDefinition("countersong", "Countersong", SpecialAbilityType.SUPERNATURAL)
		.addSummary("You can counter any sonic or language-dependent magical effect. Anyone within 30 feet can use your Perform check in place of their saving throw. You can maintain a countersong for 10 rounds."),

		new ClassFeatureDefinition("fascinate", "Fascinate", SpecialAbilityType.SPELL_LIKE)
		.addSummary("You can fascinate 0 creature(s) within 90 feet. If you beat their Will save with a Perform check, they will listen quietly for up to 0 round(s)."),	// calculated values

		new ClassFeatureDefinition("inspire_courage", "Inspire Courage", SpecialAbilityType.SUPERNATURAL)
		.addSummary("While singing, all allies who can hear you gain a +1 morale bonus to saving throws against charm and fear effects, and a +@(bonus) morale bonus to attack and weapon damage rolls. The effect lasts as long as you sing plus 5 rounds.")
		.addParameter("bonus", 1),

		new ClassFeatureDefinition("inspire_competence", "Inspire Competence", SpecialAbilityType.SUPERNATURAL)
		.addSummary("You can help an ally succeed at a task. They get a +2 competence bonus to skill checks as long as they are able to see and hear you and are within 30 feet. This can be maintained for 2 minutes."),

		new ClassFeatureDefinition("suggestion", "Suggestion", SpecialAbilityType.SPELL_LIKE)
		.addSummary("You can make a suggestion (as the spell) to a creature you have already fascinated. Will save (DC 9 negates)."),	// calculated

		new ClassFeatureDefinition("inspire_greatness", "Inspire Greatness", SpecialAbilityType.SUPERNATURAL)
		.addSummary("You can inspire up to -2 creature(s). This gives them +2 bonus Hit Dice (d10s), +2 competence bonus on attacks, and +1 competence bonus on Fortitude saves. This lasts as long as you play, and for 5 rounds after you stop."),	// calculated

		new ClassFeatureDefinition("song_of_freedom", "Song of Freedom", SpecialAbilityType.SPELL_LIKE)
		.addSummary("With one minute of uninterrupted music and concentration you can affect a single target within 30 feet as though with a break enchantment spell."),

		new ClassFeatureDefinition("inspire_heroics", "Inspire Heroics", SpecialAbilityType.SUPERNATURAL)
		.addSummary("You can inspire tremendous heroism in -4 willing ally(including yourself) within 30 feet. A creature so inspired gains a +4 morale bonus on saving throws and a +4 dodge bonus to AC."),

		new ClassFeatureDefinition("mass_suggestion", "Mass Suggestion", SpecialAbilityType.SPELL_LIKE)
		.addSummary("You can make a suggestion (as the spell) to any creatures you have already fascinated. Will save (DC 9 negates)."),

		// TODO aura needs to be split into four version predicated on deity's alignment
		new ClassFeatureDefinition("aura", "Aura", SpecialAbilityType.EXTRAORDINARY)
		.addParameter("strength", "a faint")
		.addSummary("You have &(strength) aura corresponding to the alignment of your deity."),

		// TODO restricted spells
		new ClassFeatureDefinition("cleric_spells", "Spells")
		.addSummary("You can cast divine spells from the cleric spell list and the relevant domain spell lists."),

		// TODO split into cure and inflict versions. implement using choice of features
		new ClassFeatureDefinition("cleric_spontaneous", "Spontaneous Casting")
		.addSummary("Can spontaneously cast <i>cure</i> or <i>inflict</i> spells, by sacrificing a pre-prepared spell of equal or higher level."),

		// TODO split into turn and rebuke versions. implement using choice of features
		new ClassFeatureDefinition("turning", "Turn/Rebuke Undead", SpecialAbilityType.SUPERNATURAL)
		.addSummary("Can turn or rebuke undead (3 + Chr mod) times per day. A turning check is made on (1d20 + Chr mod); turning damage is equal to (2d6 + cleric level + Chr bonus) on a successful check."),	// TODO calculated values. turning check should be a statistic

		// TODO restricted spells
		new ClassFeatureDefinition("druid_spells", "Spells")
		.addSummary("You can cast divine spells from the druid spell list."),

		new ClassFeatureDefinition("druid_spontaneous", "Spontaneous Casting")
		.addSummary("Can spontaneously cast <i>summon nature's ally</i> spells, by sacrificing a pre-prepared spell of equal or higher level."),

		new ClassFeatureDefinition("animal_companion", "Animal Companion", SpecialAbilityType.EXTRAORDINARY)
		.addSummary("You may have an animal companion."),

		new ClassFeatureDefinition("nature_sense", "Nature Sense", SpecialAbilityType.EXTRAORDINARY)
		.addSummary("You gain a +2 bonus on Knowledge (Nature) and Survival checks.")
		.addBonus(Creature.STATISTIC_SKILLS + ".Knowledge (Nature)", 2)
		.addBonus(Creature.STATISTIC_SKILLS + ".Survival", 2),

		new ClassFeatureDefinition("wild_empathy", "Wild Empathy", SpecialAbilityType.EXTRAORDINARY)
		.addSummary("You can make a check (1d20 + druid level + Chr mod) to improve the attitude of an animal. You must be within 30 feet of it, and it generally takes one minute to perform the action."),	// TODO calculated value

		new ClassFeatureDefinition("woodland_stride", "Woodland Stride", SpecialAbilityType.EXTRAORDINARY)
		.addSummary("You can move through natural thorns, briars, etc. at full speed and without suffering damage or impairment. Magically altered areas still hamper you."),

		new ClassFeatureDefinition("trackless_step", "Trackless Step", SpecialAbilityType.EXTRAORDINARY)
		.addSummary("You leave no trail in natural surroundings, and cannot be tracked unless you choose to be."),

		new ClassFeatureDefinition("resist_natures_lure", "Resist Nature's Lure", SpecialAbilityType.EXTRAORDINARY)
		.addSummary("+4 to saving throws against the spell-like abilities of fey creatures.")
		.addBonus(Creature.STATISTIC_SAVING_THROWS, 4, "vs spell-like abilities of fey"),

		new ClassFeatureDefinition("wild_shape", "Wild Shape", SpecialAbilityType.SUPERNATURAL)
		.addSummary("You can turn yourself into a &(size) &(type) (and back) &(frequency) per day for 1 hour per druid level. The new form's Hit Dice cannot exceed your druid level.")
		.addParameter("size", "small or medium")
		.addParameter("type", "animal")
		.addParameter("frequency", "once"),

		new ClassFeatureDefinition("elemental_shape", "Elemental Shape", SpecialAbilityType.SUPERNATURAL)
		.addSummary("You can polymorph into a &(size) elemental &(frequency) per day; this functions identically to the Wild Shape ability, but is counted separately.")
		.addParameter("size", "small, medium, or large")
		.addParameter("frequency", "once"),

		new ClassFeatureDefinition("venom_immunity", "Venom Immunity", SpecialAbilityType.EXTRAORDINARY)
		.addSummary("You are immune to all poisons."),

		new ClassFeatureDefinition("thousand_faces", "A Thousand Faces", SpecialAbilityType.SUPERNATURAL)
		.addSummary("You can change your appearance at will, as if using the spell alter self."),

		new ClassFeatureDefinition("ac_bonus", "AC Bonus", SpecialAbilityType.EXTRAORDINARY)
		.addSummary("Add (Wis bonus + &(bonus)) AC; this bonus is only lost if you are immobilized, wearing armor, carrying a shield, or carrying a medium/heavy load.")		// TODO calculate value
		.addParameter("bonus", 0),

		new ClassFeatureDefinition("flurry_of_blows", "Flurry of Blows", SpecialAbilityType.EXTRAORDINARY)
		.addSummary("Take &(attacks) extra attacks when taking a full attack action. All attacks in the round suffer a &(penalty) penalty.")
		.addSummary("Take &(attacks) extra attacks when taking a full attack action.")
		.addParameter("penalty", -2)
		.addParameter("attacks", 1),

		new ClassFeatureDefinition("unarmed_strike", "Unarmed Strike")
		.addSummary("Your unarmed attacks deal &(damage) lethal damage and apply full strength bonus.")		// TODO calculate value
		.addParameter("damage", "1d6"),

		new ClassFeatureDefinition("evasion", "Evasion", SpecialAbilityType.EXTRAORDINARY)
		.addSummary("If an attack allows a Reflex save for half damage, then take no damage on a successful save."),

		new ClassFeatureDefinition("improved_evasion", "Improved Evasion", SpecialAbilityType.EXTRAORDINARY)
		.addSummary("You take half damage from attacks that allow a Reflex save. Take no damage on a successful save."),

		new ClassFeatureDefinition("monk_fast_movement", "Fast Movement", SpecialAbilityType.EXTRAORDINARY)
		.addSummary("Your speed increases by &(bonus) (limited by armor and encumbrance).")
		.addParameter("bonus", 10),

		new ClassFeatureDefinition("still_mind", "Still Mind", SpecialAbilityType.EXTRAORDINARY)
		.addSummary("+2 to saves vs enchantment spells and effects.")
		.addBonus(Creature.STATISTIC_SAVING_THROWS, 2, "vs enchantments"),

		new ClassFeatureDefinition("ki_strike", "Ki Strike", SpecialAbilityType.SUPERNATURAL)
		.addSummary("Your unarmed attacks are treated as &(type) weapons.")
		.addParameter("type", "magic"),

		new ClassFeatureDefinition("slow_fall", "Slow Fall", SpecialAbilityType.EXTRAORDINARY)
		.addSummary("As long as a wall is within arm's reach, take damage from a fall as if it were &(height) feet shorter.")
		.addSummary("As long as a wall is within arm's reach, take no damage from a fall.")
		.addParameter("height", 20),

		new ClassFeatureDefinition("purity_of_body", "Purity of Body", SpecialAbilityType.EXTRAORDINARY)
		.addSummary("Immune to all diseases except supernatural and magical diseases."),

		new ClassFeatureDefinition("wholeness_of_body", "Wholeness of Body", SpecialAbilityType.SUPERNATURAL)
		.addSummary("You can heal your own wounds, up to double your monk level points per day."),	// TODO calculated value

		new ClassFeatureDefinition("diamond_body", "Diamond Body", SpecialAbilityType.SUPERNATURAL)
		.addSummary("You are immune to all poisons."),

		new ClassFeatureDefinition("abundant_step", "Abundant Step", SpecialAbilityType.SUPERNATURAL)
		.addSummary("You can slip between spaces as if using the spell dimension door once per day, cast at half your monk level."),		// TODO calculate value

		new ClassFeatureDefinition("diamond_soul", "Diamond Soul", SpecialAbilityType.EXTRAORDINARY)
		.addSummary("You have spell resistance of 10 + your monk level."),		// TODO calculate value

		new ClassFeatureDefinition("quivering_palm", "Quivering Palm", SpecialAbilityType.SUPERNATURAL)
		.addSummary("(1/week) If you damage the victim with an unarmed attack, you can slay them with an act of will any time within monk level days."),		// TODO calculate value

		new ClassFeatureDefinition("timeless_body", "Timeless Body", SpecialAbilityType.EXTRAORDINARY)
		.addSummary("You no longer suffer additional penalties for aging, and cannot be magically aged. Your lifespan is not increased."),

		new ClassFeatureDefinition("tongue_of_the_sun_and_moon", "Tongue of the Sun and Moon", SpecialAbilityType.EXTRAORDINARY)
		.addSummary("You can speak with any living creature."),

		new ClassFeatureDefinition("empty_body", "Empty Body", SpecialAbilityType.SUPERNATURAL)
		.addSummary("You can become ethereal for monk level rounds per day, as the spell etherealness."),		// TODO calculate value

		new ClassFeatureDefinition("perfect_self", "Perfect Self")
		.addSummary("You are now considered an outsider. In addition, you gain damage resistance 10/magic."),

		new ClassFeatureDefinition("code_of_conduct", "Code of Conduct")
		.addSummary("You must remain Good. You must respect legitimate authority, act with honor, help those in need, and punish those that harm or threaten innocents. You must never knowingly associate with evil characters, or those that consistently offend your moral code."),

		new ClassFeatureDefinition("detect_evil", "Detect Evil", SpecialAbilityType.SPELL_LIKE)
		.addSummary("At will, as the spell."),

		new ClassFeatureDefinition("smite_evil", "Smite Evil", SpecialAbilityType.SUPERNATURAL)
		.addSummary("@(times) per day, you can add your Chr bonus to your attack roll; if the creature you strike is evil, you inflict an extra damage equal to your paladin level.")	// calculated
		.addParameter("times", "Once"),

		new ClassFeatureDefinition("divine_grace", "Divine Grace", SpecialAbilityType.SUPERNATURAL)
		.addSummary("Add a bonus equal to your Chr bonus to all saves."),	// calculated

		new ClassFeatureDefinition("lay_on_hands", "Lay on Hands", SpecialAbilityType.SUPERNATURAL)
		.addSummary("As a standard action, you can heal yourself or someone else. You can cure a total points of damage equal to your paladin level x your Chr bonus per day. These points can also be used to harm undead."),	// calculated

		new ClassFeatureDefinition("aura_of_courage", "Aura of Courage", SpecialAbilityType.SUPERNATURAL)
		.addSummary("You are immune to fear. All allies within 10 feet of you gain a +4 morale bonus to save against fear effects."),

		new ClassFeatureDefinition("divine_health", "Divine Health", SpecialAbilityType.EXTRAORDINARY)
		.addSummary("You are immune to all diseases, including magical diseases such as mummy rot and lycanthropy."),

		new ClassFeatureDefinition("paladin_spells", "Spells")
		.addSummary("You cast divine spells drawn from the paladin spell list. Your caster level is 0."),	// calculated

		new ClassFeatureDefinition("special_mount", "Special Mount", SpecialAbilityType.SPELL_LIKE)
		.addSummary("Once per day you can call your special steed to serve you for up to two hours per paladin level."),	// calculated

		new ClassFeatureDefinition("remove_disease", "Remove Disease", SpecialAbilityType.SPELL_LIKE)
		.addSummary("You can cast remove disease @(times) per week, as the spell.")
		.addParameter("times", "once"),

		new ClassFeatureDefinition("sorcerer_spells", "Spells")
		.addSummary("You can cast arcane spells drawn from the sorcerer/wizard spell list."),

		new ClassFeatureDefinition("familiar", "Familiar")
		.addSummary("You may call a familiar as a magical companion."),

		new ClassFeatureDefinition("wizard_spells", "Spells")
		.addSummary("You can cast arcane spells drawn from the sorcerer/wizard spell list.")
	};
}
