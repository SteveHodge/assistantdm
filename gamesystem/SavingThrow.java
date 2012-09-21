package gamesystem;


public class SavingThrow extends Statistic {
	public static final int SAVE_FORTITUDE = 0;
	public static final int SAVE_REFLEX = 1;
	public static final int SAVE_WILL = 2;
	protected static final String[] save_names = {"Fortitude", "Reflex", "Will"};

	public SavingThrow(int type, AbilityScore ability) {
		super(save_names[type]);
		addModifier(ability.getModifier());
	}
}
