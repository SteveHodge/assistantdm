package gamesystem;

public enum MonsterType {
	ABERRATION("Aberration", 8, BABProgression.AVERAGE),
	ANIMAL("Animal", 8, BABProgression.AVERAGE),
	CONSTRUCT("Construct", 10, BABProgression.AVERAGE),
	DRAGON("Dragon", 12, BABProgression.FAST),
	ELEMENTAL("Elemental", 8, BABProgression.AVERAGE),
	FEY("Fey", 6, BABProgression.SLOW),
	GIANT("Giant", 8, BABProgression.AVERAGE),
	HUMANOID("Humanoid", 8, BABProgression.AVERAGE),
	MAGICAL_BEAST("Magical Beast", 10, BABProgression.FAST),
	MONSTROUS_HUMANOID("Monstrous Humanoid", 8, BABProgression.FAST),
	OOZE("Ooze", 10, BABProgression.AVERAGE),
	OUTSIDER("Outsider", 8, BABProgression.FAST),
	PLANT("Plant", 8, BABProgression.AVERAGE),
	UNDEAD("Undead", 12, BABProgression.SLOW),
	VERMIN("Vermin", 8, BABProgression.AVERAGE);

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

	// TODO could use map for efficiency
	public static MonsterType getMonsterType(String name) {
		for (MonsterType c : MonsterType.values()) {
			if (c.name.equals(name)) return c;
		}
		return null;
	}

	private MonsterType(String name, int hdType, BABProgression bab) {
		this.name = name;
		hitDiceType = hdType;
		babProgression = bab;
	}

	private String name;
	private int hitDiceType;
	private BABProgression babProgression;
}
