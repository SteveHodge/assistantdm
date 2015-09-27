package gamesystem;

import gamesystem.ClassFeature.ClassFeatureDefinition;
import gamesystem.Feat.FeatDefinition;

import java.util.ArrayList;
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

	static class AddBonusFeatAction extends LevelUpAction {
		boolean requirePrereqs = true;
		String[] options;

		AddBonusFeatAction(boolean preqs, String[] options) {
			requirePrereqs = preqs;
			this.options = options;
		}

		@Override
		public String toString() {
			return String.format("%d: AddBonusFeatAction(reqPreqs = %b, options = [%s])", level, requirePrereqs, String.join(", ", options));
		}

		@Override
		public void apply(Character c) {
			// TODO should remove any feats the character already has first
			if (options.length == 1) {
				// find the Feat
				FeatDefinition feat = null;
				for (FeatDefinition f : Feat.FEATS) {
					if (f.name.equals(options[0])) {
						feat = f;
						break;
					}
				}
				if (feat != null) {
					c.feats.addElement(feat.getFeat());
				} else {
					System.err.println("Could not find feat " + options[0]);
				}
			} else {
				// TODO handle case of multiple options
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

	//Armor Restriction: If wearing ANY armor or carrying a shield, you lose your Wisdom bonus to AC, fast movement and flurry of blows abilities.
	static {
		try {
			MONK.addAction(1, new AddBonusFeatAction(false, new String[] { "Improved Grapple", "Stunning Fist" }));
			MONK.addAction(1, new AddBonusFeatAction(false, new String[] { "Improved Unarmed Strike" }));
			MONK.addAction(2, new AddBonusFeatAction(false, new String[] { "Combat Reflexes", "Deflect Arrows" }));
			MONK.addAction(2, new AddFeatureAction("evasion"));
			MONK.addAction(3, new AddFeatureAction("still_mind"));
			MONK.addAction(5, new AddFeatureAction("purity_of_body"));
			MONK.addAction(6, new AddBonusFeatAction(false, new String[] { "Improved Disarm", "Improved Trip" }));
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
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
