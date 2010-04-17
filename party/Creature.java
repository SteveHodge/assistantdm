package party;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public abstract class Creature {
	public static final int SAVE_FORTITUDE = 0;
	public static final int SAVE_REFLEX = 1;
	public static final int SAVE_WILL = 2;
	protected static final String[] save_names = {"Fortitude", "Reflex", "Will"};

	// note that Character.getTouchAC() requires the order of constants here (more specifically it assumes that
	// all components less than AC_DEX are to be excluded from touch ac).
	public static final int AC_ARMOR = 0;
	public static final int AC_SHIELD = 1;
	public static final int AC_NATURAL = 2;
	public static final int AC_DEX = 3;
	public static final int AC_SIZE = 4;
	public static final int AC_DEFLECTION = 5;
	public static final int AC_DODGE = 6;
	public static final int AC_OTHER = 7;
	public static final int AC_MAX_INDEX = 8;
	protected static final String[] ac_names = {"Armor","Shield","Natural","Dex","Size","Deflect","Dodge","Misc"};

	public static final int ABILITY_STRENGTH = 0;
	public static final int ABILITY_DEXTERITY = 1;
	public static final int ABILITY_CONSTITUTION = 2;
	public static final int ABILITY_INTELLIGENCE = 3;
	public static final int ABILITY_WISDOM = 4;
	public static final int ABILITY_CHARISMA = 5;
	protected static final String[] ability_names = {"Strength", "Dexterity", "Constitution", "Intelligence", "Wisdom", "Charisma"};

	// properties
	public final static String PROPERTY_NAME = "Name";	// not currently sent to listeners
	public final static String PROPERTY_MAXHPS = "Hit Points";
	public final static String PROPERTY_WOUNDS = "Wounds";
	public final static String PROPERTY_NONLETHAL = "Non Lethal Damage";
	public final static String PROPERTY_INITIATIVE = "Initiative";
	public final static String PROPERTY_ABILITY_PREFIX = "Ability: ";
	public final static String PROPERTY_ABILITY_OVERRIDE_PREFIX = "Temporary Ability: ";	// not currently sent to listeners
	public final static String PROPERTY_SAVE_PREFIX = "Save: ";
	public final static String PROPERTY_SAVE_MISC_PREFIX = "Save (misc mod): ";	// not currently sent to listeners
	public final static String PROPERTY_AC = "AC";
	public final static String PROPERTY_AC_COMPONENT_PREFIX = "AC: ";	// not currently sent to listeners
	public final static String PROPERTY_SKILL_PREFIX = "Skill: ";
	public final static String PROPERTY_SKILL_MISC_PREFIX = "Skill (misc mod): ";	// not currently sent to listeners
	public final static String PROPERTY_LEVEL = "Level";
	public final static String PROPERTY_XP = "XP";

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

	// ************************* Non static members and methods **************************

	protected final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

	public void addPropertyChangeListener(PropertyChangeListener listener) {
		pcs.addPropertyChangeListener(listener);
	}

	public void addPropertyChangeListener(String property, PropertyChangeListener listener) {
		pcs.addPropertyChangeListener(property, listener);
	}

	public void removePropertyChangeListener(PropertyChangeListener listener) {
		pcs.removePropertyChangeListener(listener);
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

	abstract public Object getProperty(String prop);
	abstract public void setProperty(String prop, Object value);
}
