package party;

import gamesystem.SizeCategory;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

// TODO should probably convert these constants to enums
public abstract class Creature {
	// properties
	public final static String PROPERTY_NAME = "Name";	// not currently sent to listeners
	public final static String PROPERTY_MAXHPS = "Hit Points";
	public final static String PROPERTY_WOUNDS = "Wounds";
	public final static String PROPERTY_NONLETHAL = "Non Lethal Damage";
	public final static String PROPERTY_HPS = "Current Hit Points";
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
	public final static String PROPERTY_BAB = "BAB";

	public final static String PROPERTY_SPACE = "Space";	// currently only a property on Monster
	public final static String PROPERTY_REACH = "Reach";	// currently only a property on Monster

	// informational string properties (these are simple values that can be set/retrieved)
	public static final String PROPERTY_PLAYER = "Player";
	public final static String PROPERTY_CLASS = "Class";
	public final static String PROPERTY_REGION = "Region";
	public final static String PROPERTY_RACE = "Race";
	public final static String PROPERTY_GENDER = "Gender";
	public final static String PROPERTY_ALIGNMENT = "Alignment";
	public final static String PROPERTY_DEITY = "Deity";
	public final static String PROPERTY_SIZE = "Size";
	public final static String PROPERTY_TYPE = "Type";
	public final static String PROPERTY_AGE = "Age";
	public final static String PROPERTY_HEIGHT = "Height";
	public final static String PROPERTY_WEIGHT = "Weight";
	public final static String PROPERTY_EYE_COLOUR = "Eye Colour";
	public final static String PROPERTY_HAIR_COLOUR = "Hair Colour";
	public final static String PROPERTY_SPEED = "Speed";
	public final static String PROPERTY_DAMAGE_REDUCTION = "Damage Reduction";
	public final static String PROPERTY_SPELL_RESISTANCE = "Spell Resistance";
	public final static String PROPERTY_ARCANE_SPELL_FAILURE = "Arcane Spell Failure";
	public final static String PROPERTY_ACTION_POINTS = "Action Points";

	// statistics
	// TODO should be combined with properties
	public final static String STATISTIC_STRENGTH = "abilities.strength";
	public final static String STATISTIC_INTELLIGENCE = "abilities.intelligence";
	public final static String STATISTIC_WISDOM = "abilities.wisdom";
	public final static String STATISTIC_DEXTERITY = "abilities.dexterity";
	public final static String STATISTIC_CONSTITUTION = "abilities.constitution";
	public final static String STATISTIC_CHARISMA = "abilities.charisma";
	//public final static String STATISTIC_ABILITY_CHECKS = "ability_check";
	public final static String STATISTIC_SAVING_THROWS = "saves";
	public final static String STATISTIC_FORTITUDE_SAVE = "saves.fortitude";
	public final static String STATISTIC_WILL_SAVE = "saves.will";
	public final static String STATISTIC_REFLEX_SAVE = "saves.reflex";
	public final static String STATISTIC_SKILLS = "skills";
	public final static String STATISTIC_AC = "ac";
	public final static String STATISTIC_ARMOR = "ac.armor";
	public final static String STATISTIC_SHIELD = "ac.shield";
	public final static String STATISTIC_NATURAL_ARMOR = "ac.natural_armor";
	public final static String STATISTIC_INITIATIVE = "initiative";
	public final static String STATISTIC_HPS = "hps";
	public final static String STATISTIC_LEVEL = "level";
	public final static String STATISTIC_ATTACKS = "attacks";
	public final static String STATISTIC_DAMAGE = "damage";
	public final static String STATISTIC_SIZE = "size";

	// The order of these needs to be the same as the ability enum in AbilityScore
	public final static String[] STATISTIC_ABILITY = {STATISTIC_STRENGTH,STATISTIC_DEXTERITY,STATISTIC_CONSTITUTION,STATISTIC_INTELLIGENCE,STATISTIC_WISDOM,STATISTIC_CHARISMA};
	// The order of these needs to be the same as the save enum in SavingThrow
	public final static String[] STATISTIC_SAVING_THROW = {STATISTIC_FORTITUDE_SAVE,STATISTIC_REFLEX_SAVE,STATISTIC_WILL_SAVE};

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
	abstract public void setInitiativeModifier(int init);

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

	abstract public SizeCategory getSize();
	abstract public void setSize(SizeCategory s);

	abstract public int getSpace();
	abstract public void setSpace(int s);

	abstract public int getReach();
	abstract public void setReach(int r);

	abstract public Object getProperty(String prop);
	abstract public void setProperty(String prop, Object value);
}
