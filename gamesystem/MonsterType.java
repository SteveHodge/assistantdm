package gamesystem;

/*
 * Monsters' Types define the creature's features and traits. Generally when a template changes a creature's type the creature retains the features of the original type but uses the traits of the new type.
 * Also note that creatures with 1 HD or fewer advance by class and use the features of their class(es) instead of their type's features.
 * Features:
 *   Hit Dice type
 *   BAB progression
 *   Saving throw progression
 *   Skill points
 * Traits:
 *   Special qualities and immunities
 *   Weapon proficiencies
 *   Armor proficiencies
 *   Whether or not they breathe, eat, and sleep
 */

public enum MonsterType {
	// @formatter:off
	ABERRATION("Aberration", 8, BABProgression.AVERAGE, SaveProgression.SLOW, SaveProgression.SLOW, SaveProgression.FAST, 2),
	ANIMAL("Animal", 8, BABProgression.AVERAGE, SaveProgression.FAST, SaveProgression.FAST, SaveProgression.SLOW, 2),
	CONSTRUCT("Construct", 10, BABProgression.AVERAGE, SaveProgression.SLOW, SaveProgression.SLOW, SaveProgression.SLOW, 2),
	DRAGON("Dragon", 12, BABProgression.FAST, SaveProgression.FAST, SaveProgression.FAST, SaveProgression.FAST, 6),
	ELEMENTAL("Elemental", 8, BABProgression.AVERAGE, SaveProgression.SLOW, SaveProgression.SLOW, SaveProgression.SLOW, 2),	// save progression depends on element, this is handled in Monster
	FEY("Fey", 6, BABProgression.SLOW, SaveProgression.SLOW, SaveProgression.FAST, SaveProgression.FAST, 6),
	GIANT("Giant", 8, BABProgression.AVERAGE, SaveProgression.FAST, SaveProgression.SLOW, SaveProgression.SLOW, 2),
	HUMANOID("Humanoid", 8, BABProgression.AVERAGE, SaveProgression.SLOW, SaveProgression.SLOW, SaveProgression.SLOW, 2),
	MAGICAL_BEAST("Magical Beast", 10, BABProgression.FAST, SaveProgression.FAST, SaveProgression.FAST, SaveProgression.SLOW, 2),
	MONSTROUS_HUMANOID("Monstrous Humanoid", 8, BABProgression.FAST, SaveProgression.SLOW, SaveProgression.FAST, SaveProgression.FAST, 2),
	OOZE("Ooze", 10, BABProgression.AVERAGE, SaveProgression.SLOW, SaveProgression.SLOW, SaveProgression.SLOW, 2),
	OUTSIDER("Outsider", 8, BABProgression.FAST, SaveProgression.FAST, SaveProgression.FAST, SaveProgression.FAST, 8),
	PLANT("Plant", 8, BABProgression.AVERAGE, SaveProgression.FAST, SaveProgression.SLOW, SaveProgression.SLOW, 2),
	UNDEAD("Undead", 12, BABProgression.SLOW, SaveProgression.SLOW, SaveProgression.SLOW, SaveProgression.FAST, 4),
	VERMIN("Vermin", 8, BABProgression.AVERAGE, SaveProgression.FAST, SaveProgression.SLOW, SaveProgression.SLOW, 2);
	// @formatter:on

	@Override
	public String toString() {
		return name;
	}

	public int getHitDiceType() {
		return hitDiceType;
	}

	public int getBAB(int hd) {
		return babProgression.getBAB(hd);
	}

	public int getBaseSave(SavingThrow.Type type, int hitdice) {
		SaveProgression p = getProgression(type);
		if (p != null) {
			return p.getBaseSave(hitdice);
		}
		return 0;
	}

	public int getSkillPoints() {
		return skillPoints;
	}

	public SaveProgression getProgression(SavingThrow.Type type) {
		switch (type) {
		case FORTITUDE:
			return fortitude;
		case REFLEX:
			return reflex;
		case WILL:
			return will;
		}
		return null;
	}

	// TODO could use map for efficiency
	public static MonsterType getMonsterType(String name) {
		for (MonsterType c : MonsterType.values()) {
			if (c.name.equals(name)) return c;
		}
		return null;
	}

	private MonsterType(String name, int hdType, BABProgression bab, SaveProgression fort, SaveProgression ref, SaveProgression will, int skills) {
		this.name = name;
		hitDiceType = hdType;
		babProgression = bab;
		fortitude = fort;
		reflex = ref;
		this.will = will;
		skillPoints = skills;
	}

	private String name;
	private int hitDiceType;
	private BABProgression babProgression;
	private SaveProgression fortitude;
	private SaveProgression reflex;
	private SaveProgression will;
	private int skillPoints;
}
