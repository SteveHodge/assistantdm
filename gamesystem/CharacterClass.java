package gamesystem;

import gamesystem.ClassFeature.ClassFeatureDefinition;
import gamesystem.Feat.FeatDefinition;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import party.Character;


public enum CharacterClass {
	// basic classes
	BARBARIAN("Barbarian", 12, BABProgression.FAST, SaveProgression.FAST, SaveProgression.SLOW, SaveProgression.SLOW),
	BARD("Bard", 6, BABProgression.AVERAGE, SaveProgression.SLOW, SaveProgression.FAST, SaveProgression.FAST),
	CLERIC("Cleric", 8, BABProgression.AVERAGE, SaveProgression.FAST, SaveProgression.SLOW, SaveProgression.FAST),
	DRUID("Druid", 8, BABProgression.AVERAGE, SaveProgression.FAST, SaveProgression.SLOW, SaveProgression.FAST),
	FIGHTER("Fighter", 10, BABProgression.FAST, SaveProgression.FAST, SaveProgression.SLOW, SaveProgression.SLOW),
	MONK("Monk", 8, BABProgression.AVERAGE, SaveProgression.FAST, SaveProgression.FAST, SaveProgression.FAST),
	PALADIN("Paladin", 10, BABProgression.FAST, SaveProgression.FAST, SaveProgression.SLOW, SaveProgression.SLOW),
	RANGER("Ranger", 8, BABProgression.FAST, SaveProgression.FAST, SaveProgression.FAST, SaveProgression.SLOW),
	ROGUE("Rogue", 6, BABProgression.AVERAGE, SaveProgression.SLOW, SaveProgression.FAST, SaveProgression.SLOW),
	SORCERER("Sorcerer", 4, BABProgression.SLOW, SaveProgression.SLOW, SaveProgression.SLOW, SaveProgression.FAST),
	WIZARD("Wizard", 4, BABProgression.SLOW, SaveProgression.SLOW, SaveProgression.SLOW, SaveProgression.FAST),

	// prestige classes
//	Arcane Archer	d8	good bab, good fort, good ref, bad will
//	Arcane Trickster	d4	bad bab, bad fort, good ref, good will
//	Archmage	d4	bad bab, bad fort, bad ref, good will
//	Assassin	d6	avg bab, bad fort, good ref, bad will
	BLACKGUARD("Blackguard", 10, BABProgression.FAST, SaveProgression.FAST, SaveProgression.SLOW, SaveProgression.SLOW),
//	Dragon Disciple	d12	avg bab, good fort, bad ref, good will
//	Duelist		d10	good bab, bad fort, good ref, bad will
//	Dwarven Defender	d12	good bab, good fort, bad ref, good will
//	Eldritch Knight	d6	good bab, good fort, bad ref, bad will
//	Hierophant	d8	bad bab, good fort, bad ref, good will
//	Horizon Walker	d8	good bab, good fort, bad ref, bad will
//	Loremaster	d4	bad bab, bad fort, bad ref, good will
//	Mystic Theurge	d4	bad bab, bad fort, bad ref, good will
//	Red Wizard	d4	bad bab, bad fort, bad ref, good will
//	Shadowdancer	d8	avg bab, bad fort, good ref, bad will
//	Thaumaturgist	d4	bad bab, bad fort, bad ref, good will

	// NPC classes
	ADEPT("Adept", 6, BABProgression.SLOW, SaveProgression.SLOW, SaveProgression.SLOW, SaveProgression.FAST),
	ARISTOCRAT("Aristocrat", 8, BABProgression.AVERAGE, SaveProgression.SLOW, SaveProgression.SLOW, SaveProgression.FAST),
	COMMONER("Commoner", 4, BABProgression.SLOW, SaveProgression.SLOW, SaveProgression.SLOW, SaveProgression.SLOW),
	EXPERT("Expert", 6, BABProgression.AVERAGE, SaveProgression.SLOW, SaveProgression.SLOW, SaveProgression.FAST),
	WARRIOR("Warrior", 8, BABProgression.FAST, SaveProgression.FAST, SaveProgression.SLOW, SaveProgression.SLOW);

	public static CharacterClass[] getCoreClasses() {
		return new CharacterClass[] { BARBARIAN, BARD, CLERIC, DRUID, FIGHTER, MONK, PALADIN, RANGER, ROGUE, SORCERER, WIZARD };
	}

	@Override
	public String toString() {
		return name;
	}

	public int getHitDiceType() {
		return hitdice;
	}

	public int getBAB(int level) {
		return bab.getBAB(level);
	}

	public int getBaseSave(SavingThrow.Type type, int level) {
		switch (type) {
		case FORTITUDE:
			return fortitude.getBaseSave(level);
		case REFLEX:
			return reflex.getBaseSave(level);
		case WILL:
			return will.getBaseSave(level);
		}
		return 0;
	}

	// TODO could use map for efficiency
	public static CharacterClass getCharacterClass(String name) {
		for (CharacterClass c : CharacterClass.values()) {
			if (c.name.equals(name)) return c;
		}
		return null;
	}

	public Set<LevelUpAction> getActions(int level) {
		Set<LevelUpAction> actions = new HashSet<>();
		for (LevelUpAction a : levelUpActions) {
			if (a.level == level) actions.add(a);
		}
		return actions;
	}

	private CharacterClass(String name, int hitdice, BABProgression bab, SaveProgression fort, SaveProgression ref, SaveProgression will) {
		this.name = name;
		this.hitdice = hitdice;
		this.bab = bab;
		this.fortitude = fort;
		this.reflex = ref;
		this.will = will;
	}

	private void addAction(int level, LevelUpAction action) {
		action.level = level;
		levelUpActions.add(action);
	}

	private String name;
	private int hitdice;
	private BABProgression bab;
	private SaveProgression fortitude, reflex, will;

	private List<LevelUpAction> levelUpActions = new ArrayList<LevelUpAction>();

	// addAction adds a level up action to a class
	// actions are executed to perform the required modification to the character. actions can have parameters

	public abstract static class LevelUpAction {
		int level;

		public abstract void apply(party.Character c);
	}

	public static class ClassOption {
		public String id;
		String[] options;
		public String selection;

		public ClassOption(String id) {
			this.id = id;
		}
	}

	static class AddBonusFeatAction extends LevelUpAction {
		String id;
		boolean requirePrereqs = true;
		String[] options;

		AddBonusFeatAction(boolean preqs, String[] options) {
			this(null, preqs, options);
		}

		AddBonusFeatAction(String id, boolean preqs, String[] options) {
			this.id = id;
			requirePrereqs = preqs;
			this.options = options;
		}

		@Override
		public String toString() {
			return String.format("%d: AddBonusFeatAction(reqPreqs = %b, options = [%s])", level, requirePrereqs, String.join(", ", options));
		}

		@Override
		public void apply(Character c) {
			String selection = null;
			ClassOption opt = c.classOptions.get(id);

			if (options.length == 1) {
				selection = options[0];
			} else {
				if (opt != null) {
					if (Arrays.asList(options).contains(opt.selection)) {
						selection = opt.selection;
						System.out.println("Found selection for " + opt.id + " = " + selection);
					} else {
						// opt.selection is invalid, reset it. note selection should still be null
						opt.selection = null;
					}
				} else {
					opt = new ClassOption(id);
					opt.options = options;	// TODO perhaps should filter options are aren't selectable (e.g. preqs are required and no fullfilled), but then we'll need to reset this each time
					c.classOptions.put(id, opt);
				}
				// TODO if there is only one reasonable option (because other feats are taken) and we don't already have a selection then automatically select it
				// TODO think I'll need to reset options each time (as they may be missing on load)?
			}

			if (selection != null) {
				System.out.println("Selected feat = " + selection);
				// find the Feat
				FeatDefinition feat = null;
				for (FeatDefinition f : Feat.FEATS) {
					if (f.name.equals(selection)) {
						feat = f;
						break;
					}
				}
				if (feat != null) {
					Feat f = feat.getFeat();
					f.bonus = true;
					c.feats.addElement(f);
				} else {
					System.err.println("Could not find feat " + options[0]);
				}
			}
		}
	}

	static class AddFeatureAction extends LevelUpAction {
		ClassFeatureDefinition factory;

		AddFeatureAction(String feature) {
			this.factory = ClassFeatureDefinition.findFeatureFactory(feature);
			if (this.factory == null) throw new IllegalArgumentException("Unknown feature " + feature);
		}

		@Override
		public String toString() {
			return String.format("%d: AddFeatureAction(%s)", level, factory.name);
		}

		@Override
		public void apply(Character c) {
			c.addClassFeature(factory.getFeature());
		}
	}

	static class RemoveFeatureAction extends LevelUpAction {
		String feature;

		RemoveFeatureAction(String feature) {
			if (ClassFeatureDefinition.findFeatureFactory(feature) == null) throw new IllegalArgumentException("Unknown feature " + feature);
			this.feature = feature;
		}

		@Override
		public String toString() {
			return String.format("%d: RemoveFeatureAction(%s)", level, feature);
		}

		@Override
		public void apply(Character c) {
			c.removeClassFeature(feature);
		}
	}

	static class SetParameterAction extends LevelUpAction {
		String feature;
		String parameter;
		Object value;

		SetParameterAction(String feature, String param, Object value) {
			if (ClassFeatureDefinition.findFeatureFactory(feature) == null) throw new IllegalArgumentException("Unknown feature " + feature);
			this.feature = feature;
			parameter = param;
			this.value = value;
		}

		@Override
		public String toString() {
			return String.format("%d: SetParameterAction(feature = %s, parameter = %s, value = %s)", level, feature, parameter, value.toString());
		}

		@Override
		public void apply(Character c) {
			c.setClassFeatureParameter(feature, parameter, value);
		}
	}

	//TODO monk Armor Restriction: If wearing ANY armor or carrying a shield, you lose your Wisdom bonus to AC, fast movement and flurry of blows abilities.
	// TODO ChooseFeatureAction
	static {
		try {
			BARBARIAN.addAction(1, new AddFeatureAction("barbarian_fast_movement"));
			BARBARIAN.addAction(1, new AddFeatureAction("illiteracy"));
			BARBARIAN.addAction(2, new AddFeatureAction("uncanny_dodge"));	// TODO if already have uncanny_dodge then change this to improved_uncanny_dodge
			BARBARIAN.addAction(5, new AddFeatureAction("improved_uncanny_dodge"));
			BARBARIAN.addAction(14, new AddFeatureAction("indomitable_will"));
			BARBARIAN.addAction(17, new AddFeatureAction("tireless_rage"));

			BARBARIAN.addAction(1, new AddFeatureAction("rage"));
			BARBARIAN.addAction(4, new SetParameterAction("rage", "times", "2 times"));
			BARBARIAN.addAction(8, new SetParameterAction("rage", "times", "3 times"));
			BARBARIAN.addAction(12, new SetParameterAction("rage", "times", "4 times"));
			BARBARIAN.addAction(16, new SetParameterAction("rage", "times", "5 times"));
			BARBARIAN.addAction(20, new SetParameterAction("rage", "times", "6 times"));
			BARBARIAN.addAction(11, new SetParameterAction("rage", "ability_bonus", 6));
			BARBARIAN.addAction(11, new SetParameterAction("rage", "save_bonus", 3));
			BARBARIAN.addAction(20, new SetParameterAction("rage", "ability_bonus", 8));
			BARBARIAN.addAction(20, new SetParameterAction("rage", "save_bonus", 4));

			BARBARIAN.addAction(3, new AddFeatureAction("trap_sense"));
			BARBARIAN.addAction(6, new SetParameterAction("trap_sense", "bonus", 2));
			BARBARIAN.addAction(9, new SetParameterAction("trap_sense", "bonus", 3));
			BARBARIAN.addAction(12, new SetParameterAction("trap_sense", "bonus", 4));
			BARBARIAN.addAction(15, new SetParameterAction("trap_sense", "bonus", 5));
			BARBARIAN.addAction(18, new SetParameterAction("trap_sense", "bonus", 6));

			BARBARIAN.addAction(7, new AddFeatureAction("damage_reduction"));
			BARBARIAN.addAction(10, new SetParameterAction("damage_reduction", "dr", 2));
			BARBARIAN.addAction(13, new SetParameterAction("damage_reduction", "dr", 3));
			BARBARIAN.addAction(16, new SetParameterAction("damage_reduction", "dr", 4));
			BARBARIAN.addAction(19, new SetParameterAction("damage_reduction", "dr", 5));

			BARD.addAction(1, new AddFeatureAction("bardic_knowledge"));
			BARD.addAction(1, new AddFeatureAction("bardic_music"));
			BARD.addAction(1, new AddFeatureAction("countersong"));
			BARD.addAction(1, new AddFeatureAction("fascinate"));
			BARD.addAction(3, new AddFeatureAction("inspire_competence"));
			BARD.addAction(6, new AddFeatureAction("suggestion"));
			BARD.addAction(9, new AddFeatureAction("inspire_greatness"));
			BARD.addAction(12, new AddFeatureAction("song_of_freedom"));
			BARD.addAction(15, new AddFeatureAction("inspire_heroics"));
			BARD.addAction(18, new AddFeatureAction("mass_suggestion"));

			BARD.addAction(1, new AddFeatureAction("inspire_courage"));
			BARD.addAction(8, new SetParameterAction("inspire_courage", "bonus", 2));
			BARD.addAction(14, new SetParameterAction("inspire_courage", "bonus", 3));
			BARD.addAction(20, new SetParameterAction("inspire_courage", "bonus", 4));

			CLERIC.addAction(1, new AddFeatureAction("aura"));
			CLERIC.addAction(2, new SetParameterAction("aura", "strength", "a moderate"));
			CLERIC.addAction(5, new SetParameterAction("aura", "strength", "a strong"));
			CLERIC.addAction(11, new SetParameterAction("aura", "strength", "an overwhelming"));

			CLERIC.addAction(1, new AddFeatureAction("cleric_spells"));
			CLERIC.addAction(1, new AddFeatureAction("cleric_spontaneous"));
			CLERIC.addAction(1, new AddFeatureAction("turning"));

			DRUID.addAction(1, new AddFeatureAction("druid_spells"));
			DRUID.addAction(1, new AddFeatureAction("druid_spontaneous"));
			DRUID.addAction(1, new AddFeatureAction("animal_companion"));
			DRUID.addAction(1, new AddFeatureAction("nature_sense"));
			DRUID.addAction(1, new AddFeatureAction("wild_empathy"));
			DRUID.addAction(2, new AddFeatureAction("woodland_stride"));
			DRUID.addAction(3, new AddFeatureAction("trackless_step"));
			DRUID.addAction(4, new AddFeatureAction("resist_natures_lure"));
			DRUID.addAction(9, new AddFeatureAction("venom_immunity"));
			DRUID.addAction(13, new AddFeatureAction("thousand_faces"));
			DRUID.addAction(15, new AddFeatureAction("timeless_body"));

			DRUID.addAction(5, new AddFeatureAction("wild_shape"));
			DRUID.addAction(8, new SetParameterAction("wild_shape", "size", "small, medium, or large"));
			DRUID.addAction(11, new SetParameterAction("wild_shape", "size", "tiny, small, medium, or large"));
			DRUID.addAction(15, new SetParameterAction("wild_shape", "size", "tiny, small, medium, large, or huge"));
			DRUID.addAction(12, new SetParameterAction("wild_shape", "type", "animal or plant"));
			DRUID.addAction(6, new SetParameterAction("wild_shape", "frequency", "2 times"));
			DRUID.addAction(7, new SetParameterAction("wild_shape", "frequency", "3 times"));
			DRUID.addAction(10, new SetParameterAction("wild_shape", "frequency", "4 times"));
			DRUID.addAction(14, new SetParameterAction("wild_shape", "frequency", "5 times"));
			DRUID.addAction(18, new SetParameterAction("wild_shape", "frequency", "6 times"));

			DRUID.addAction(16, new AddFeatureAction("elemental_shape"));
			DRUID.addAction(18, new SetParameterAction("elemental_shape", "size", "small, medium, large, or huge"));
			DRUID.addAction(18, new SetParameterAction("elemental_shape", "frequency", "2 times"));
			DRUID.addAction(20, new SetParameterAction("elemental_shape", "frequency", "3 times"));

			final String[] fighterBonusFeats = {
					"Improved Initiative", "Exotic Weapon Proficiency", Feat.FEAT_COMBAT_EXPERTISE, "Rapid Shot",
					"Manyshot", Feat.FEAT_POWER_ATTACK, Feat.FEAT_TWO_WEAPON_FIGHTING,
					Feat.FEAT_IMPROVED_TWO_WEAPON_FIGHTING, Feat.FEAT_GREATER_TWO_WEAPON_FIGHTING,
					Feat.FEAT_WEAPON_FINESSE, "Weapon Focus", "Greater Weapon Focus", "Dodge",
					"Improved Shield Bash", "Two-Weapon Defense", "Improved Critical", "Weapon Specialization",
					"Greater Weapon Specialization", "Improved Unarmed Strike", "Improved Grapple", "Blind-Fight",
					"Improved Disarm", "Improved Feint", "Improved Trip", "Whirlwind Attack", "Combat Reflexes",
					"Mobility", "Spring Attack", "Deflect Arrows", "Snatch Arrows", "Stunning Fist",
					"Mounted Combat", "Mounted Archery", "Ride-By Attack", "Trample", "Spirited Charge",
					"Point Blank Shot", "Far Shot", "Precise Shot", "Improved Precise Shot", "Shot on the Run",
					"Cleave", "Great Cleave", "Improved Bull Rush", "Improved Overrun", "Improved Sunder",
					"Rapid Reload", "Quick Draw"
			};

			FIGHTER.addAction(1, new AddBonusFeatAction("fighter_bonus_1", true, fighterBonusFeats));
			FIGHTER.addAction(2, new AddBonusFeatAction("fighter_bonus_2", true, fighterBonusFeats));
			FIGHTER.addAction(4, new AddBonusFeatAction("fighter_bonus_4", true, fighterBonusFeats));
			FIGHTER.addAction(6, new AddBonusFeatAction("fighter_bonus_6", true, fighterBonusFeats));
			FIGHTER.addAction(8, new AddBonusFeatAction("fighter_bonus_8", true, fighterBonusFeats));
			FIGHTER.addAction(10, new AddBonusFeatAction("fighter_bonus_10", true, fighterBonusFeats));
			FIGHTER.addAction(12, new AddBonusFeatAction("fighter_bonus_12", true, fighterBonusFeats));
			FIGHTER.addAction(14, new AddBonusFeatAction("fighter_bonus_14", true, fighterBonusFeats));
			FIGHTER.addAction(16, new AddBonusFeatAction("fighter_bonus_16", true, fighterBonusFeats));
			FIGHTER.addAction(18, new AddBonusFeatAction("fighter_bonus_18", true, fighterBonusFeats));
			FIGHTER.addAction(20, new AddBonusFeatAction("fighter_bonus_20", true, fighterBonusFeats));

			MONK.addAction(1, new AddBonusFeatAction("monk_bonus_1", false, new String[] { "Improved Grapple", "Stunning Fist" }));
			MONK.addAction(1, new AddBonusFeatAction(false, new String[] { "Improved Unarmed Strike" }));
			MONK.addAction(2, new AddBonusFeatAction("monk_bonus_2", false, new String[] { "Combat Reflexes", "Deflect Arrows" }));
			MONK.addAction(2, new AddFeatureAction("evasion"));
			MONK.addAction(3, new AddFeatureAction("still_mind"));
			MONK.addAction(5, new AddFeatureAction("purity_of_body"));
			MONK.addAction(6, new AddBonusFeatAction("monk_bonus_3", false, new String[] { "Improved Disarm", "Improved Trip" }));
			MONK.addAction(7, new AddFeatureAction("wholeness_of_body"));
			MONK.addAction(9, new RemoveFeatureAction("evasion"));
			MONK.addAction(9, new AddFeatureAction("improved_evasion"));
			MONK.addAction(11, new AddFeatureAction("diamond_body"));
			MONK.addAction(12, new AddFeatureAction("abundant_step"));
			MONK.addAction(13, new AddFeatureAction("diamond_soul"));
			MONK.addAction(15, new AddFeatureAction("quivering_palm"));
			MONK.addAction(17, new AddFeatureAction("timeless_body"));
			MONK.addAction(17, new AddFeatureAction("tongue_of_the_sun_and_moon"));
			MONK.addAction(19, new AddFeatureAction("empty_body"));
			MONK.addAction(20, new AddFeatureAction("perfect_self"));

			MONK.addAction(1, new AddFeatureAction("ac_bonus"));
			MONK.addAction(5, new SetParameterAction("ac_bonus", "bonus", 1));
			MONK.addAction(10, new SetParameterAction("ac_bonus", "bonus", 2));
			MONK.addAction(15, new SetParameterAction("ac_bonus", "bonus", 3));
			MONK.addAction(20, new SetParameterAction("ac_bonus", "bonus", 4));

			MONK.addAction(1, new AddFeatureAction("flurry_of_blows"));
			MONK.addAction(5, new SetParameterAction("flurry_of_blows", "penalty", 1));
			MONK.addAction(9, new SetParameterAction("flurry_of_blows", "penalty", 0));
			MONK.addAction(9, new SetParameterAction("flurry_of_blows", "template", 1));
			MONK.addAction(11, new SetParameterAction("flurry_of_blows", "attacks", 2));

			MONK.addAction(1, new AddFeatureAction("unarmed_strike"));
			MONK.addAction(4, new SetParameterAction("unarmed_strike", "damage", "1d8"));
			MONK.addAction(8, new SetParameterAction("unarmed_strike", "damage", "1d10"));
			MONK.addAction(12, new SetParameterAction("unarmed_strike", "damage", "2d6"));
			MONK.addAction(16, new SetParameterAction("unarmed_strike", "damage", "2d8"));
			MONK.addAction(20, new SetParameterAction("unarmed_strike", "damage", "2d10"));

			MONK.addAction(3, new AddFeatureAction("monk_fast_movement"));
			MONK.addAction(6, new SetParameterAction("monk_fast_movement", "bonus", 20));
			MONK.addAction(9, new SetParameterAction("monk_fast_movement", "bonus", 30));
			MONK.addAction(12, new SetParameterAction("monk_fast_movement", "bonus", 40));
			MONK.addAction(15, new SetParameterAction("monk_fast_movement", "bonus", 50));
			MONK.addAction(18, new SetParameterAction("monk_fast_movement", "bonus", 60));

			MONK.addAction(4, new AddFeatureAction("ki_strike"));
			MONK.addAction(10, new SetParameterAction("ki_strike", "type", "magic and lawful"));
			MONK.addAction(16, new SetParameterAction("ki_strike", "type", "magic, lawful, and adamantine"));

			MONK.addAction(4, new AddFeatureAction("slow_fall"));
			MONK.addAction(6, new SetParameterAction("slow_fall", "height", 30));
			MONK.addAction(8, new SetParameterAction("slow_fall", "height", 40));
			MONK.addAction(10, new SetParameterAction("slow_fall", "height", 50));
			MONK.addAction(12, new SetParameterAction("slow_fall", "height", 60));
			MONK.addAction(14, new SetParameterAction("slow_fall", "height", 70));
			MONK.addAction(16, new SetParameterAction("slow_fall", "height", 80));
			MONK.addAction(18, new SetParameterAction("slow_fall", "height", 90));
			MONK.addAction(18, new SetParameterAction("slow_fall", "template", 1));

			PALADIN.addAction(1, new AddFeatureAction("code_of_conduct"));

			PALADIN.addAction(1, new AddFeatureAction("aura"));
			PALADIN.addAction(2, new SetParameterAction("aura", "strength", "a moderate"));
			PALADIN.addAction(5, new SetParameterAction("aura", "strength", "a strong"));
			PALADIN.addAction(11, new SetParameterAction("aura", "strength", "an overwhelming"));

			PALADIN.addAction(1, new AddFeatureAction("detect_evil"));

			PALADIN.addAction(1, new AddFeatureAction("smite_evil"));
			PALADIN.addAction(5, new SetParameterAction("smite_evil", "times", "2 times"));
			PALADIN.addAction(10, new SetParameterAction("smite_evil", "times", "3 times"));
			PALADIN.addAction(15, new SetParameterAction("smite_evil", "times", "4 times"));
			PALADIN.addAction(20, new SetParameterAction("smite_evil", "times", "5 times"));

			PALADIN.addAction(2, new AddFeatureAction("divine_grace"));
			PALADIN.addAction(2, new AddFeatureAction("lay_on_hands"));
			PALADIN.addAction(3, new AddFeatureAction("aura_of_courage"));
			PALADIN.addAction(3, new AddFeatureAction("divine_health"));
			PALADIN.addAction(4, new AddFeatureAction("turning"));
			PALADIN.addAction(4, new AddFeatureAction("paladin_spells"));
			PALADIN.addAction(5, new AddFeatureAction("special_mount"));

			PALADIN.addAction(6, new AddFeatureAction("remove_disease"));
			PALADIN.addAction(9, new SetParameterAction("remove_disease", "times", "2 times"));
			PALADIN.addAction(12, new SetParameterAction("remove_disease", "times", "3 times"));
			PALADIN.addAction(15, new SetParameterAction("remove_disease", "times", "4 times"));
			PALADIN.addAction(18, new SetParameterAction("remove_disease", "times", "5 times"));

			//RANGER
			//ROGUE

			SORCERER.addAction(1, new AddFeatureAction("sorcerer_spells"));
			SORCERER.addAction(1, new AddFeatureAction("familiar"));

			WIZARD.addAction(1, new AddFeatureAction("wizard_spells"));
			WIZARD.addAction(1, new AddFeatureAction("familiar"));
			WIZARD.addAction(1, new AddBonusFeatAction(false, new String[] { "Scribe Scroll" }));

			final String[] wizardBonusFeats = {
					"Spell Mastery",
					"Empower Spell",
					"Enlarge Spell",
					"Extend Spell",
					"Heighten Spell",
					"Maximize Spell",
					"Quicken Spell",
					"Silent Spell",
					"Still Spell",
					"Widen Spell",
					"Brew Potion",
					"Craft Magic Arms and Armor",
					"Craft Rod",
					"Craft Staff",
					"Craft Wand",
					"Craft Wondrous Item",
					"Forge Ring"
			};

			WIZARD.addAction(5, new AddBonusFeatAction("wizard_bonus_5", true, wizardBonusFeats));
			WIZARD.addAction(10, new AddBonusFeatAction("wizard_bonus_10", true, wizardBonusFeats));
			WIZARD.addAction(15, new AddBonusFeatAction("wizard_bonus_15", true, wizardBonusFeats));
			WIZARD.addAction(20, new AddBonusFeatAction("wizard_bonus_20", true, wizardBonusFeats));

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
