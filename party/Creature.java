package party;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

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
