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
	ABERRATION("Aberration", 8, BABProgression.AVERAGE, SaveProgression.SLOW, SaveProgression.SLOW, SaveProgression.FAST),
	ANIMAL("Animal", 8, BABProgression.AVERAGE, SaveProgression.FAST, SaveProgression.FAST, SaveProgression.SLOW),
	CONSTRUCT("Construct", 10, BABProgression.AVERAGE, SaveProgression.SLOW, SaveProgression.SLOW, SaveProgression.SLOW),
	DRAGON("Dragon", 12, BABProgression.FAST, SaveProgression.FAST, SaveProgression.FAST, SaveProgression.FAST),
	ELEMENTAL("Elemental", 8, BABProgression.AVERAGE, SaveProgression.SLOW, SaveProgression.SLOW, SaveProgression.SLOW),
	FEY("Fey", 6, BABProgression.SLOW, SaveProgression.SLOW, SaveProgression.FAST, SaveProgression.FAST),
	GIANT("Giant", 8, BABProgression.AVERAGE, SaveProgression.FAST, SaveProgression.SLOW, SaveProgression.SLOW),
	HUMANOID("Humanoid", 8, BABProgression.AVERAGE, SaveProgression.SLOW, SaveProgression.SLOW, SaveProgression.SLOW),
	MAGICAL_BEAST("Magical Beast", 10, BABProgression.FAST, SaveProgression.FAST, SaveProgression.FAST, SaveProgression.SLOW),
	MONSTROUS_HUMANOID("Monstrous Humanoid", 8, BABProgression.FAST, SaveProgression.SLOW, SaveProgression.FAST, SaveProgression.FAST),
	OOZE("Ooze", 10, BABProgression.AVERAGE, SaveProgression.SLOW, SaveProgression.SLOW, SaveProgression.SLOW),
	OUTSIDER("Outsider", 8, BABProgression.FAST, SaveProgression.FAST, SaveProgression.FAST, SaveProgression.FAST),
	PLANT("Plant", 8, BABProgression.AVERAGE, SaveProgression.FAST, SaveProgression.SLOW, SaveProgression.SLOW),
	UNDEAD("Undead", 12, BABProgression.SLOW, SaveProgression.SLOW, SaveProgression.SLOW, SaveProgression.FAST),
	VERMIN("Vermin", 8, BABProgression.AVERAGE, SaveProgression.FAST, SaveProgression.SLOW, SaveProgression.SLOW);

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

	private MonsterType(String name, int hdType, BABProgression bab, SaveProgression fort, SaveProgression ref, SaveProgression will) {
		this.name = name;
		hitDiceType = hdType;
		babProgression = bab;
		fortitude = fort;
		reflex = ref;
		this.will = will;
	}

	private String name;
	private int hitDiceType;
	private BABProgression babProgression;
	private SaveProgression fortitude;
	private SaveProgression reflex;
	private SaveProgression will;
}
