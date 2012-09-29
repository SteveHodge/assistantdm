package party;
import gamesystem.AbilityScore;
import gamesystem.SavingThrow;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public abstract class Creature {
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
	public final static String PROPERTY_BAB = "BAB";

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
	public final static String STATISTIC_INITIATIVE = "initiative";
	public final static String STATISTIC_HPS = "hps";
	public final static String STATISTIC_LEVEL = "level";
	public final static String STATISTIC_ATTACKS = "attacks";

	// The order of these needs to be the same as the ability constants in AbilityScore
	public final static String[] STATISTIC_ABILITY = {STATISTIC_STRENGTH,STATISTIC_DEXTERITY,STATISTIC_CONSTITUTION,STATISTIC_INTELLIGENCE,STATISTIC_WISDOM,STATISTIC_CHARISMA};
	// The order of these needs to be the same as the save constants in SavingThrow
	public final static String[] STATISTIC_SAVING_THROW = {STATISTIC_FORTITUDE_SAVE,STATISTIC_REFLEX_SAVE,STATISTIC_WILL_SAVE}; 

	public final static Map<String,String> STATISTIC_DESC;
	static {
		HashMap<String,String> map = new HashMap<String,String>();
		map.put(STATISTIC_STRENGTH, AbilityScore.getAbilityName(AbilityScore.ABILITY_STRENGTH));
		map.put(STATISTIC_INTELLIGENCE, AbilityScore.getAbilityName(AbilityScore.ABILITY_INTELLIGENCE));
		map.put(STATISTIC_WISDOM, AbilityScore.getAbilityName(AbilityScore.ABILITY_WISDOM));
		map.put(STATISTIC_DEXTERITY, AbilityScore.getAbilityName(AbilityScore.ABILITY_DEXTERITY));
		map.put(STATISTIC_CONSTITUTION, AbilityScore.getAbilityName(AbilityScore.ABILITY_CONSTITUTION));
		map.put(STATISTIC_CHARISMA, AbilityScore.getAbilityName(AbilityScore.ABILITY_CHARISMA));
		map.put(STATISTIC_SAVING_THROWS, "saves");
		map.put(STATISTIC_FORTITUDE_SAVE, SavingThrow.getSavingThrowName(SavingThrow.SAVE_FORTITUDE)+" save");
		map.put(STATISTIC_WILL_SAVE, SavingThrow.getSavingThrowName(SavingThrow.SAVE_WILL)+" save");
		map.put(STATISTIC_REFLEX_SAVE, SavingThrow.getSavingThrowName(SavingThrow.SAVE_REFLEX)+" save");
		map.put(STATISTIC_SKILLS, "skills");
		map.put(STATISTIC_AC, "AC");
		map.put(STATISTIC_INITIATIVE, "Initiative");
		map.put(STATISTIC_ATTACKS, "attack rolls");
		STATISTIC_DESC = Collections.unmodifiableMap(map);
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

	abstract public Object getProperty(String prop);
	abstract public void setProperty(String prop, Object value);
}
