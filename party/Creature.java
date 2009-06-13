package party;
import java.beans.PropertyChangeListener;

public abstract class Creature {
	public static final int SAVE_FORTITUDE = 0;
	public static final int SAVE_REFLEX = 1;
	public static final int SAVE_WILL = 2;

	public static final int AC_ARMOR = 0;
	public static final int AC_SHIELD = 1;
	public static final int AC_DEX = 2;
	public static final int AC_SIZE = 3;
	public static final int AC_NATURAL = 4;
	public static final int AC_DEFLECTION = 5;
	public static final int AC_DODGE = 6;
	public static final int AC_OTHER = 7;
	public static final int AC_MAX_INDEX = 8;

	public static final int ABILITY_STRENGTH = 0;
	public static final int ABILITY_DEXTERITY = 1;
	public static final int ABILITY_CONSTITUTION = 2;
	public static final int ABILITY_INTELLIGENCE = 3;
	public static final int ABILITY_WISDOM = 4;
	public static final int ABILITY_CHARISMA = 5;

	protected static final String[] save_names = {"Fortitude", "Reflex", "Will"};
	protected static final String[] ability_names = {"Strength", "Dexterity", "Constitution", "Intelligence", "Wisdom", "Charisma"};
	protected static final String[] ac_names = {"Armor","Shield","Dex","Size","Natural","Deflect","Dodge","Other"};

	public static int getModifier(int score) {
		return score/2-5;
	}

	public static String getSavingThrowName(int save) {
		return save_names[save];
	}

	public static int getSaveAbility(int save) {
		if (save == SAVE_FORTITUDE) return ABILITY_CONSTITUTION;
		if (save == SAVE_REFLEX) return ABILITY_DEXTERITY;
		if (save == SAVE_WILL) return ABILITY_WISDOM;
		return -1;
	}

	public static String getACComponentName(int type) {
		return ac_names[type];
	}

	public static String getAbilityName(int type) {
		return ability_names[type];
	}

	abstract public String getName();
	abstract public void setName(String name);

//	abstract public int getAbilityScore(int score);
//	abstract public void setAbilityScore(int score, int value);
//	abstract public int getAbilityScoreModifier(int score);

	abstract public int getInitiativeModifier();

	abstract public int getMaximumHitPoints();
	abstract public void setMaximumHitPoints(int ac);

	abstract public int getWounds();
	abstract public void setWounds(int wounds);

	abstract public int getNonLethal();
	abstract public void setNonLethal(int nonLethal);

	abstract public int getAC();
	abstract public void setAC(int ac);
	
	abstract public int getTouchAC();
	abstract public void setTouchAC(int ac);
	
	abstract public int getFlatFootedAC();
	abstract public void setFlatFootedAC(int ac);

	abstract public void addPropertyChangeListener(PropertyChangeListener listener);
	abstract public void removePropertyChangeListener(PropertyChangeListener listener);
}
