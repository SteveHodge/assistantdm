package gamesystem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import gamesystem.AbilityScore.Type;
import gamesystem.CalculatedValue.AbilityMod;
import gamesystem.CalculatedValue.Calculation;
import gamesystem.CalculatedValue.ClassLevel;
import gamesystem.CalculatedValue.Constant;
import gamesystem.CalculatedValue.Format;
import gamesystem.CalculatedValue.Product;
import gamesystem.CalculatedValue.Sum;
import gamesystem.ClassFeature.ClassFeatureDefinition;
import gamesystem.ClassFeature.ClassFeatureDefinition.ParameterModifier;

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

			// update any modifiers relying on this parameter
			for (int i = 0; i < getModifierCount(); i++) {
				Modifier m = getModifier(i);
				if (m instanceof ParameterModifier) {
					ParameterModifier p = (ParameterModifier)m;
					if (p.parameter.equals(param)) p.updateValue();
				}
			}
		}
	}

	public Object getParameter(String parameter) {
		if (parameters != null) {
			return parameters.get(parameter);
		}
		return null;
	}

	public String getNameAndType() {
		if (definition.type == SpecialAbilityType.NATURAL) return definition.name;
		return String.format("%s (%s)", definition.name, definition.type.getAbbreviation());
	}

	public String getNameAndTypeHTML() {
		if (definition.type == SpecialAbilityType.SPELL_LIKE) return String.format("<i>%s</i> (%s)", definition.name, definition.type.getAbbreviation());
		return getNameAndType();
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
	public void apply(Creature c) {
		super.apply(c);

		// bind any calculated values in the parameters
		if (parameters != null) {
			for (String p : parameters.keySet()) {
				Object v = parameters.get(p);
				if (v instanceof Calculation) {
					CalculatedValue val = new CalculatedValue((Calculation) v, c);
					parameters.put(p, val);
				}
			}
		}
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

		ClassFeatureDefinition addAbilityBonus(String target, AbilityScore.Type ability, boolean untyped) {
			return addAbilityBonus(target, ability, untyped, null);
		}

		ClassFeatureDefinition addAbilityBonus(String target, AbilityScore.Type ability, boolean untyped, String condition) {
			AbilityBonusEffect e = new AbilityBonusEffect();
			if (!untyped) e.type = ability.toString();
			e.target = target;
			e.ability = ability;
			e.condition = condition;
			effects.add(e);
			return this;
		}

		ClassFeatureDefinition addParameterModifier(String target, String parameter, String type, String condition) {
			ParameterModifierEffect e = new ParameterModifierEffect();
			e.type = type;
			e.target = target;
			e.parameter = parameter;
			e.condition = condition;
			effects.add(e);
			return this;
		}

		ClassFeature getFeature(Creature c) {
			ClassFeature f = new ClassFeature(this);

			// setup any parameters
			for (String param : parameters.keySet()) {
				if (f.parameters == null) f.parameters = new HashMap<>();
				f.parameters.put(param, parameters.get(param));
			}

			// add any effects:
			for (Effect e : effects) {
				Modifier m = null;
				if (e instanceof AbilityBonusEffect) {
					m = ((AbilityBonusEffect) e).getModifier(c, name);
				} else if (e instanceof ParameterModifierEffect) {
					m = ((ParameterModifierEffect) e).getModifier(f);
				} else if (e instanceof FixedEffect) {
					m = ((FixedEffect) e).getModifier(name);
				}
				if (m != null) f.addModifier(m, e.target);
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

		static class AbilityBonusEffect extends FixedEffect {
			Type ability;

			Modifier getModifier(Creature c, String feature) {
				Modifier abilityMod = c.getAbilityModifier(ability);
				return new AbstractModifier() {
					{
						abilityMod.addPropertyChangeListener(evt -> pcs.firePropertyChange(evt.getPropertyName(), null, getModifier()));
					}

					@Override
					public String getSource() {
						return feature;
					}

					@Override
					public String getCondition() {
						return condition;
					}

					@Override
					public String getType() {
						return type;
					}

					@Override
					public int getModifier() {
						int mod = abilityMod.getModifier();
						return mod < 0 ? 0 : mod;
					}
				};
			}
		}

		static class ParameterModifierEffect extends FixedEffect {
			String parameter;

			Modifier getModifier(ClassFeature feature) {
				return new ParameterModifier(feature, parameter, type, condition);
			}
		}

		static class ParameterModifier extends AbstractModifier {
			ClassFeature feature;
			String parameter;
			String type;
			String condition;

			ParameterModifier(ClassFeature f, String param, String type, String cond) {
				feature = f;
				parameter = param;
				if (type != null && type.length() > 0) this.type = type;
				condition = cond;
			}

			void updateValue() {
				pcs.firePropertyChange("value", null, getModifier());
			}

			@Override
			public int getModifier() {
				Object value = feature.getParameter(parameter);
				if (value != null && value instanceof Integer) {
					return (Integer) value;
				}
				return 0;
			}

			@Override
			public String getType() {
				return type;
			}

			@Override
			public String getSource() {
				return feature.getName();
			}

			@Override
			public String getCondition() {
				return condition;
			}
		}
	}

/*	static class ParameterValue extends Calculation {
		String feature;
		String parameter;

		ParameterValue(String feature, String parameter) {
			this.feature = feature;
			this.parameter = parameter;
		}

		@Override
		Term bind(CalculatedValue calc, Creature c) {
			return new Term() {
				@Override
				int value() {
					if (c instanceof party.Character) {
						Object val = ((party.Character) c).getClassFeatureParameter(feature, parameter);
						if (val != null && val instanceof Integer) {
							return (Integer) val;
						}
					}
					return 0;
				}
			};
		}
	}
 */
	static ClassFeatureDefinition[] featureDefinitions = {
			new ClassFeatureDefinition("barbarian_fast_movement", "Fast Movement", SpecialAbilityType.EXTRAORDINARY)
			.addSummary("+10 to land speed when in medium armor or lighter and not carrying a heavy load."),

			new ClassFeatureDefinition("illiteracy", "Illiteracy")
			.addSummary("Cannot read or write. Must spend 2 skill points to gain literacy or gain levels in another class."),

			new ClassFeatureDefinition("rage", "Rage", SpecialAbilityType.EXTRAORDINARY)
			.addSummary(
					"You can fly into a screaming frenzy once per encounter, up to &(times) per day. This gives +&(ability_bonus) to Strength and Constitution, and a +&(save_bonus) morale bonus to Will saves, but gives a -2 penalty to AC. Cannot use any skills that require patience or concentration while enraged. Your rage lasts up to &(duration) rounds.")
			.addParameter("times", "once")
			.addParameter("ability_bonus", 4)
			.addParameter("save_bonus", 2)
			.addParameter("duration", new Sum(new Constant(3), new AbilityMod(AbilityScore.Type.CONSTITUTION))),	// XXX perhaps should include the ability bonus/2 (as it's con mod after the bonus)

			new ClassFeatureDefinition("uncanny_dodge", "Uncanny Dodge", SpecialAbilityType.EXTRAORDINARY)
			.addSummary("You retain your Dexterity bonus to AC even if flatfooted or struck by an invisible attacker."),

			new ClassFeatureDefinition("improved_uncanny_dodge", "Improved Uncanny Dodge", SpecialAbilityType.EXTRAORDINARY)
			.addSummary("Can no longer be flanked."),

			new ClassFeatureDefinition("trap_sense", "Trap Sense", SpecialAbilityType.EXTRAORDINARY)
			.addSummary("+&(bonus) to Reflex saves and AC against traps.")
			.addParameter("bonus", 1)
			.addParameterModifier(Creature.STATISTIC_REFLEX_SAVE, "bonus", null, "vs Traps")
			.addParameterModifier(Creature.STATISTIC_AC, "bonus", null, "vs Traps"),

			new ClassFeatureDefinition("damage_reduction", "Damage Reduction", SpecialAbilityType.EXTRAORDINARY)
			.addSummary("&(dr)/-")
			.addParameter("dr", 1),

			new ClassFeatureDefinition("indomitable_will", "Indomitable Will", SpecialAbilityType.EXTRAORDINARY)
			.addSummary("While in a rage you gain a +4 bonus on Will saves to resist Enchantment spells."),

			new ClassFeatureDefinition("tireless_rage", "Tireless Rage", SpecialAbilityType.EXTRAORDINARY)
			.addSummary("You no longer become fatigued at the end of your rage"),

			new ClassFeatureDefinition("bardic_knowledge", "Bardic Knowledge", SpecialAbilityType.EXTRAORDINARY)
			.addSummary("You can make a special knowledge check for stray bits of trivia. This check is 1d20&(bonus).")
			.addParameter("bonus", new Format(true, new Sum(new ClassLevel(CharacterClass.BARD), new AbilityMod(AbilityScore.Type.INTELLIGENCE)))),

			// TODO maybe have some way of making bardic music the parent of the sub-powers
			new ClassFeatureDefinition("bardic_music", "Bardic Music")
			.addSummary("Performances can create varied magical effects &(level) times per day.")
			.addParameter("level", new ClassLevel(CharacterClass.BARD)),

			new ClassFeatureDefinition("countersong", "Countersong", SpecialAbilityType.SUPERNATURAL)
			.addSummary("You can counter any sonic or language-dependent magical effect. Anyone within 30 feet can use your Perform check in place of their saving throw. You can maintain a countersong for 10 rounds."),

			new ClassFeatureDefinition("fascinate", "Fascinate", SpecialAbilityType.SPELL_LIKE)
			.addSummary("You can fascinate &(targets) creature(s) within 90 feet. If you beat their Will save with a Perform check, they will listen quietly for up to &(level) round(s).")
			.addParameter("targets", 1)
			.addParameter("level", new ClassLevel(CharacterClass.BARD)),

			new ClassFeatureDefinition("inspire_courage", "Inspire Courage", SpecialAbilityType.SUPERNATURAL)
			.addSummary(
					"While singing, all allies who can hear you gain a +&(bonus) morale bonus to saving throws against charm and fear effects, and a +&(bonus) morale bonus to attack and weapon damage rolls. The effect lasts as long as you sing plus 5 rounds.")
			.addParameter("bonus", 1),

			new ClassFeatureDefinition("inspire_competence", "Inspire Competence", SpecialAbilityType.SUPERNATURAL)
			.addSummary("You can help an ally succeed at a task. They get a +2 competence bonus to skill checks as long as they are able to see and hear you and are within 30 feet. This can be maintained for 2 minutes."),

			new ClassFeatureDefinition("suggestion", "Suggestion", SpecialAbilityType.SPELL_LIKE)
			.addSummary("You can make a <i>suggestion</i> (as the spell) to a creature you have already fascinated. Will save DC &(dc) negates.")
			.addParameter("dc", new Sum(new Constant(10), new ClassLevel(CharacterClass.BARD, 0.5f), new AbilityMod(AbilityScore.Type.CHARISMA))),

			new ClassFeatureDefinition("inspire_greatness", "Inspire Greatness", SpecialAbilityType.SUPERNATURAL)
			.addSummary(
					"You can inspire up to &(targets) creature(s). This gives them +2 bonus Hit Dice (d10s), +2 competence bonus on attacks, and +1 competence bonus on Fortitude saves. This lasts as long as you play, and for 5 rounds after you stop.")
			.addParameter("targets", 1),

			new ClassFeatureDefinition("song_of_freedom", "Song of Freedom", SpecialAbilityType.SPELL_LIKE)
			.addSummary("With one minute of uninterrupted music and concentration you can affect a single target within 30 feet as though with a break enchantment spell (CL &(level)).")
			.addParameter("level", new ClassLevel(CharacterClass.BARD)),

			new ClassFeatureDefinition("inspire_heroics", "Inspire Heroics", SpecialAbilityType.SUPERNATURAL)
			.addSummary(
					"You can inspire tremendous heroism in &(targets) willing allies (including yourself) within 30 feet. A creature so inspired gains a +4 morale bonus on saving throws and a +4 dodge bonus to AC.")
			.addParameter("targets", 1),

			new ClassFeatureDefinition("mass_suggestion", "Mass Suggestion", SpecialAbilityType.SPELL_LIKE)
			.addSummary("You can make a <i>suggestion</i> to any creatures you have already fascinated. Will save DC &(dc) negates).")
			.addParameter("dc", new Sum(new Constant(10), new ClassLevel(CharacterClass.BARD, 0.5f), new AbilityMod(AbilityScore.Type.CHARISMA))),

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
			// TODO handle paladin case. Paladin should just set the dmgmod parameter correctly, but we'll need to rebind
			new ClassFeatureDefinition("turning", "Turn/Rebuke Undead", SpecialAbilityType.SUPERNATURAL)
			.addSummary(
					"Can turn or rebuke undead &(times) times per day. A turning check is made on (1d20&(chrmod)); turning damage is equal to (2d6&(dmgmod)) on a successful check.")
			.addParameter("times", new Sum(new Constant(3), new AbilityMod(AbilityScore.Type.CHARISMA)))
			.addParameter("chrmod", new Format(true, new AbilityMod(AbilityScore.Type.CHARISMA)))
			.addParameter("dmgmod", new Format(true, new Sum(new ClassLevel(CharacterClass.CLERIC), new AbilityMod(AbilityScore.Type.CHARISMA)))),

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

			// TODO for ranger, need to reset the param correctly, but we'll need to rebind.
			new ClassFeatureDefinition("wild_empathy", "Wild Empathy", SpecialAbilityType.EXTRAORDINARY)
			.addSummary(
					"You can make a check (1d20&(bonus)) to improve the attitude of an animal. You must be within 30 feet of it, and it generally takes one minute to perform the action.")
			.addParameter("bonus", new Format(true, new Sum(new ClassLevel(CharacterClass.DRUID), new AbilityMod(AbilityScore.Type.CHARISMA)))),

			new ClassFeatureDefinition("woodland_stride", "Woodland Stride", SpecialAbilityType.EXTRAORDINARY)
			.addSummary("You can move through natural thorns, briars, etc. at full speed and without suffering damage or impairment. Magically altered areas still hamper you."),

			new ClassFeatureDefinition("trackless_step", "Trackless Step", SpecialAbilityType.EXTRAORDINARY)
			.addSummary("You leave no trail in natural surroundings, and cannot be tracked unless you choose to be."),

			new ClassFeatureDefinition("resist_natures_lure", "Resist Nature's Lure", SpecialAbilityType.EXTRAORDINARY)
			.addSummary("+4 to saving throws against the spell-like abilities of fey creatures.")
			.addBonus(Creature.STATISTIC_SAVING_THROWS, 4, "vs spell-like abilities of fey"),

			new ClassFeatureDefinition("wild_shape", "Wild Shape", SpecialAbilityType.SUPERNATURAL)
			.addSummary("You can turn yourself into a &(size) &(type) &(frequency) per day for up to &(level) hours. The new form's Hit Dice cannot exceed &(level).")
			.addParameter("size", "small or medium")
			.addParameter("type", "animal")
			.addParameter("frequency", "once")
			.addParameter("level", new ClassLevel(CharacterClass.DRUID)),

			new ClassFeatureDefinition("elemental_shape", "Elemental Shape", SpecialAbilityType.SUPERNATURAL)
			.addSummary("You can polymorph into a &(size) elemental &(frequency) per day; this functions identically to the Wild Shape ability, but is counted separately.")
			.addParameter("size", "small, medium, or large")
			.addParameter("frequency", "once"),

			new ClassFeatureDefinition("venom_immunity", "Venom Immunity", SpecialAbilityType.EXTRAORDINARY)
			.addSummary("You are immune to all poisons."),

			new ClassFeatureDefinition("thousand_faces", "A Thousand Faces", SpecialAbilityType.SUPERNATURAL)
			.addSummary("You can change your appearance at will, as if using the spell <i>alter self</i>."),

			new ClassFeatureDefinition("ac_bonus", "AC Bonus", SpecialAbilityType.EXTRAORDINARY)
			.addSummary(
					"Add your Wisdom bonus (&(wisbonus)) to your AC; this bonus is only lost if you are immobilized, wearing armor, carrying a shield, or carrying a medium/heavy load.")
			.addSummary(
					"Add your Wisdom bonus (&(wisbonus)) and a +&(bonus) bonus to your AC; these bonuses are only lost if you are immobilized, wearing armor, carrying a shield, or carrying a medium/heavy load.")
			.addParameter("bonus", 0)
			.addParameter("wisbonus", new Format(new AbilityMod(AbilityScore.Type.WISDOM, true)))
			.addAbilityBonus(Creature.STATISTIC_AC, AbilityScore.Type.WISDOM, false)
			.addParameterModifier(Creature.STATISTIC_AC, "bonus", null, null),

			new ClassFeatureDefinition("flurry_of_blows", "Flurry of Blows", SpecialAbilityType.EXTRAORDINARY)
			.addSummary("Take &(attacks) extra attacks when taking a full attack action. All attacks in the round suffer a &(penalty) penalty.")
			.addSummary("Take &(attacks) extra attacks when taking a full attack action.")
			.addParameter("penalty", -2)
			.addParameter("attacks", 1),

			new ClassFeatureDefinition("unarmed_strike", "Unarmed Strike")
			.addSummary("Your unarmed attacks deal &(damage)&(strmod) lethal damage.")
			.addParameter("damage", "1d6")
			.addParameter("strmod", new Format(true, new AbilityMod(AbilityScore.Type.STRENGTH))),

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
			.addSummary("You can heal your own wounds, up to &(healing) points per day.")
			.addParameter("healing", new Product(new Constant(2), new ClassLevel(CharacterClass.MONK))),

			new ClassFeatureDefinition("diamond_body", "Diamond Body", SpecialAbilityType.SUPERNATURAL)
			.addSummary("You are immune to all poisons."),

			new ClassFeatureDefinition("abundant_step", "Abundant Step", SpecialAbilityType.SUPERNATURAL)
			.addSummary("You can slip between spaces as if using the spell <i>dimension door</i> once per day, CL &(level).")
			.addParameter("level", new ClassLevel(CharacterClass.MONK, 0.5f)),

			new ClassFeatureDefinition("diamond_soul", "Diamond Soul", SpecialAbilityType.EXTRAORDINARY)
			.addSummary("You have spell resistance of &(SR).")
			.addParameter("SR", new Sum(new Constant(10), new ClassLevel(CharacterClass.MONK))),

			new ClassFeatureDefinition("quivering_palm", "Quivering Palm", SpecialAbilityType.SUPERNATURAL)
			.addSummary("(1/week) If you damage the victim with an unarmed attack, you can slay them with an act of will any time within &(level) days.")
			.addParameter("level", new ClassLevel(CharacterClass.MONK)),

			new ClassFeatureDefinition("timeless_body", "Timeless Body", SpecialAbilityType.EXTRAORDINARY)
			.addSummary("You no longer suffer additional penalties for aging, and cannot be magically aged. Your lifespan is not increased."),

			new ClassFeatureDefinition("tongue_of_the_sun_and_moon", "Tongue of the Sun and Moon", SpecialAbilityType.EXTRAORDINARY)
			.addSummary("You can speak with any living creature."),

			new ClassFeatureDefinition("empty_body", "Empty Body", SpecialAbilityType.SUPERNATURAL)
			.addSummary("You can become ethereal for &(level) rounds per day, as the <i>spell etherealness</i>.")
			.addParameter("level", new ClassLevel(CharacterClass.MONK)),

			new ClassFeatureDefinition("perfect_self", "Perfect Self")
			.addSummary("You are now considered an outsider. In addition, you gain damage reduction 10/magic."),

			new ClassFeatureDefinition("code_of_conduct", "Code of Conduct")
			.addSummary("You must remain Good. You must respect legitimate authority, act with honor, help those in need, and punish those that harm or threaten innocents. You must never knowingly associate with evil characters, or those that consistently offend your moral code."),

			new ClassFeatureDefinition("detect_evil", "Detect Evil", SpecialAbilityType.SPELL_LIKE)
			.addSummary("At will, as the spell."),

			new ClassFeatureDefinition("smite_evil", "Smite Evil", SpecialAbilityType.SUPERNATURAL)
			.addSummary("&(times) per day, add &(chrbonus) to your attack roll; if the creature you strike is evil, you inflict +&(level) extra damage.")
			.addParameter("times", "Once")
			.addParameter("chrbonus", new Format(new AbilityMod(AbilityScore.Type.CHARISMA, true)))
			.addParameter("level", new ClassLevel(CharacterClass.PALADIN)),

			new ClassFeatureDefinition("divine_grace", "Divine Grace", SpecialAbilityType.SUPERNATURAL)
			.addSummary("Add a &(chrbonus) bonus to all saves.")
			.addParameter("chrbonus", new Format(new AbilityMod(AbilityScore.Type.CHARISMA, true)))
			.addAbilityBonus(Creature.STATISTIC_SAVING_THROWS, AbilityScore.Type.CHARISMA, true),

			new ClassFeatureDefinition("lay_on_hands", "Lay on Hands", SpecialAbilityType.SUPERNATURAL)
			.addSummary("As a standard action, you can heal yourself or someone else. You can cure &(total) points of damage per day. These points can also be used to harm undead.")
			.addParameter("total", new Product(new ClassLevel(CharacterClass.PALADIN), new AbilityMod(AbilityScore.Type.CHARISMA, true))),

			new ClassFeatureDefinition("aura_of_courage", "Aura of Courage", SpecialAbilityType.SUPERNATURAL)
			.addSummary("You are immune to fear. All allies within 10 feet of you gain a +4 morale bonus to save against fear effects."),

			new ClassFeatureDefinition("divine_health", "Divine Health", SpecialAbilityType.EXTRAORDINARY)
			.addSummary("You are immune to all diseases, including magical diseases such as mummy rot and lycanthropy."),

			new ClassFeatureDefinition("paladin_spells", "Spells")
			.addSummary("You cast divine spells drawn from the paladin spell list. Your caster level is &(cl).")
			.addParameter("cl", new ClassLevel(CharacterClass.PALADIN, 0.5f)),

			new ClassFeatureDefinition("special_mount", "Special Mount", SpecialAbilityType.SPELL_LIKE)
			.addSummary("Once per day you can call your special steed to serve you for up to &(duration) hours.")
			.addParameter("duration", new ClassLevel(CharacterClass.PALADIN, 2)),

			new ClassFeatureDefinition("remove_disease", "Remove Disease", SpecialAbilityType.SPELL_LIKE)
			.addSummary("You can cast remove disease &(times) per week, as the spell.")
			.addParameter("times", "once"),

			// TODO choose enemy, calculate bonuses
			new ClassFeatureDefinition("favored_enemy", "Favored Enemy", SpecialAbilityType.EXTRAORDINARY)
			.addSummary("You have certain types of enemies that you have extensive knowledge about. Against these creatures, you gain the listed bonus to Bluff, Listen, Sense Motive, Spot, and Survival checks, as well as weapon damage rolls."),

			new ClassFeatureDefinition("combat_style_archery", "Combat Style (Archery)", SpecialAbilityType.EXTRAORDINARY)
			.addSummary("You are treated as having the Rapid Shot feat, provided you are wearing light or no armour"),

			new ClassFeatureDefinition("combat_style_2weapon", "Combat Style (Two-Weapon Combat)", SpecialAbilityType.EXTRAORDINARY)
			.addSummary("You are treated as having the Two-Weapon Fighting feat, provided you are wearing light or no armour"),

			new ClassFeatureDefinition("improved_combat_archery", "Improved Combat Style (Archery)", SpecialAbilityType.EXTRAORDINARY)
			.addSummary("You are treated as having the Manyshot feat, provided you are wearing light or no armour"),

			new ClassFeatureDefinition("improved_combat_2weapon", "Improved Combat Style (Two-Weapon Combat)", SpecialAbilityType.EXTRAORDINARY)
			.addSummary("You are treated as having the Improved Two-Weapon Fighting feat, provided you are wearing light or no armour"),

			new ClassFeatureDefinition("combat_mastery_archery", "Combat Style Mastery (Archery)", SpecialAbilityType.EXTRAORDINARY)
			.addSummary("You are treated as having the Improved Precise Shot feat, provided you are wearing light or no armour"),

			new ClassFeatureDefinition("combat_mastery_2weapon", "Combat Style Mastery (Two-Weapon Combat)", SpecialAbilityType.EXTRAORDINARY)
			.addSummary("You are treated as having the Greater Two-Weapon Fighting feat, provided you are wearing light or no armour"),

			new ClassFeatureDefinition("ranger_spells", "Spells")
			.addSummary("You can cast divine spells drawn from the ranger spell list. Your caster level is &(cl).")
			.addParameter("cl", new ClassLevel(CharacterClass.RANGER, 0.5f)),

			new ClassFeatureDefinition("swift_tracker", "Swift Tracker", SpecialAbilityType.EXTRAORDINARY)
			.addSummary("You can move your normal speed while following tracks without taking the normal -5 penalty. You take only a -10 penalty when moving at up to twice normal speed."),

			new ClassFeatureDefinition("camouflage", "Camouflage", SpecialAbilityType.EXTRAORDINARY)
			.addSummary("You can use the Hide skill in any sort of natural terrain, even if the terrain doesn't grant cover or concealment."),

			new ClassFeatureDefinition("hide_in_plain_sight", "Hide in Plain Sight", SpecialAbilityType.EXTRAORDINARY)
			.addSummary("While in any sort of natural terrain, you can use the Hide skill, even while being observed."),

			new ClassFeatureDefinition("sneak_attack", "Sneak Attack", SpecialAbilityType.EXTRAORDINARY)
			.addSummary(
					"Any time someone you attack is denied their Dexterity bonus to AC, or you are flanking them, you inflict an extra &(dice) damage. Ranged attacks must be within 30 feet to gain this, and this extra damage is not increased on a critical hit. Creatures that are immune to critical hits ignore this damage, as do creatures with concealment.")
			.addParameter("dice", "1d6"),

			new ClassFeatureDefinition("trapfinding", "Trapfinding", SpecialAbilityType.EXTRAORDINARY)
			.addSummary("You can use the Search skill to locate traps when the task has a DC higher than 20. You can use the Disable Device skill to disarm magic traps."),

			new ClassFeatureDefinition("crippling_strike", "Crippling Strike", SpecialAbilityType.EXTRAORDINARY)
			.addSummary("Sneak attacks addtionally deal 2 points of Strength damage."),

			new ClassFeatureDefinition("defensive_roll", "Defensive Roll", SpecialAbilityType.EXTRAORDINARY)
			.addSummary("Once per day, when you would bew reduced to 0 or fewer hps by damage in combat, and provided you are not denied your Dex bonus to AC, you may make a Reflex save (DC = damage dealt) to take half damage."),

			new ClassFeatureDefinition("opportunist", "Opportunist", SpecialAbilityType.EXTRAORDINARY)
			.addSummary("Once per round you may take an attack of opportunity against an opponent who has just been struck for melee damage."),

			// TODO select skills
			new ClassFeatureDefinition("skill_mastery", "Skill Mastery")
			.addSummary("You may always take a 10 when using selected skills."),

			new ClassFeatureDefinition("slippery_mind", "Slippery Mind", SpecialAbilityType.EXTRAORDINARY)
			.addSummary("If you are effected by an enchantment spell or effect, you may retake your saving throw 1 round later at the same DC."),

			new ClassFeatureDefinition("sorcerer_spells", "Spells")
			.addSummary("You can cast arcane spells drawn from the sorcerer/wizard spell list."),

			new ClassFeatureDefinition("familiar", "Familiar")
			.addSummary("You may call a familiar as a magical companion."),

			new ClassFeatureDefinition("wizard_spells", "Spells")
			.addSummary("You can cast arcane spells drawn from the sorcerer/wizard spell list.")
	};
}
