package gamesystem;

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

	private CharacterClass(String name, int hitdice, BABProgression bab, SaveProgression fort, SaveProgression ref, SaveProgression will) {
		this.name = name;
		this.hitdice = hitdice;
		this.bab = bab;
		this.fortitude = fort;
		this.reflex = ref;
		this.will = will;
	}

	private String name;
	private int hitdice;
	private BABProgression bab;
	private SaveProgression fortitude, reflex, will;
}
