package gamesystem;


public class SavingThrow extends Statistic {
	// TODO if these constants are really only applicable in relation to the array in Character then they should be defined there (and protected)
	public static final int SAVE_FORTITUDE = 0;
	public static final int SAVE_REFLEX = 1;
	public static final int SAVE_WILL = 2;
	protected static final String[] save_names = {"Fortitude", "Reflex", "Will"};

	public static String getSavingThrowName(int save) {
		return save_names[save];
	}

	public static int getSaveAbility(int save) {
		if (save == SAVE_FORTITUDE) return AbilityScore.ABILITY_CONSTITUTION;
		if (save == SAVE_REFLEX) return AbilityScore.ABILITY_DEXTERITY;
		if (save == SAVE_WILL) return AbilityScore.ABILITY_WISDOM;
		return -1;
	}

	public SavingThrow(int type, AbilityScore ability) {
		super(save_names[type]);
		addModifier(ability.getModifier());
	}
}
