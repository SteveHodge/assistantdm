package gamesystem;


import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import swing.ListModelWithToolTips;
import ui.BuffUI;

// TODO the initiative property should either be the base value or the total - pick one
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
	public final static String PROPERTY_XP = "XP";
	public final static String PROPERTY_BAB = "BAB";
	public final static String PROPERTY_SIZE = "Size";

	public final static String PROPERTY_SPACE = "Space";	// currently only a property on Monster
	public final static String PROPERTY_REACH = "Reach";	// currently only a property on Monster

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

	protected String name;

	protected HPs hps;
	protected Size size;
	protected InitiativeModifier initiative;
	protected EnumMap<SavingThrow.Type, SavingThrow> saves = new EnumMap<SavingThrow.Type, SavingThrow>(SavingThrow.Type.class);
	protected EnumMap<AbilityScore.Type, AbilityScore> abilities = new EnumMap<AbilityScore.Type, AbilityScore>(AbilityScore.Type.class);

	protected AC ac;
	protected int tempAC, tempTouch, tempFF;	// ac overrides
	protected boolean hasTempAC, hasTempTouch, hasTempFF;	// flags for overrides

	protected Attacks attacks;

	public BuffUI.BuffListModel buffs = new BuffUI.BuffListModel();	// TODO should be protected
	protected Map<String, Object> extraProperties = new HashMap<String, Object>();

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

	// TODO this id stuff might be better off in CombatPanel for now
	private static int nextID = 1;
	private static Map<Integer, Creature> idMap = new HashMap<Integer, Creature>();
	int id = -1;

	public int getID() {
		if (id == -1) {
			do {
				id = nextID++;
			} while (idMap.containsKey(id));
			idMap.put(id, this);
		}
		return id;
	}

	// TODO shouldn't be here. should check for existing use of new id
	public void setID(int newID) {
		if (id != -1) {
			idMap.remove(id);
		}
		id = newID;
		idMap.put(id, this);
	}

	public static Creature getCreature(int id) {
		return idMap.get(id);
	}

	abstract public void setName(String name);	// TODO shouldn't be abstract

	public HPs getHPStatistic() {
		return hps;
	}

	public Size getSizeStatistic() {
		return size;
	}

	public InitiativeModifier getInitiativeStatistic() {
		return initiative;
	}

	public AC getACStatistic() {
		return ac;
	}

	public AbilityScore getAbilityStatistic(AbilityScore.Type t) {
		return abilities.get(t);
	}

	public SavingThrow getSavingThrowStatistic(SavingThrow.Type t) {
		return saves.get(t);
	}

	public Attacks getAttacksStatistic() {
		return attacks;
	}

	// buff related methods
	public Statistic getStatistic(String name) {
		if (name.equals(STATISTIC_STRENGTH)) {
			return abilities.get(AbilityScore.Type.STRENGTH);
		} else if (name.equals(STATISTIC_INTELLIGENCE)) {
			return abilities.get(AbilityScore.Type.INTELLIGENCE);
		} else if (name.equals(STATISTIC_WISDOM)) {
			return abilities.get(AbilityScore.Type.WISDOM);
		} else if (name.equals(STATISTIC_DEXTERITY)) {
			return abilities.get(AbilityScore.Type.DEXTERITY);
		} else if (name.equals(STATISTIC_CONSTITUTION)) {
			return abilities.get(AbilityScore.Type.CONSTITUTION);
		} else if (name.equals(STATISTIC_CHARISMA)) {
			return abilities.get(AbilityScore.Type.CHARISMA);
		} else if (name.equals(STATISTIC_FORTITUDE_SAVE)) {
			return saves.get(SavingThrow.Type.FORTITUDE);
		} else if (name.equals(STATISTIC_WILL_SAVE)) {
			return saves.get(SavingThrow.Type.WILL);
		} else if (name.equals(STATISTIC_REFLEX_SAVE)) {
			return saves.get(SavingThrow.Type.REFLEX);
		} else if (name.equals(STATISTIC_AC)) {
			return ac;
		} else if (name.equals(STATISTIC_ARMOR)) {
			return ac.getArmor();
		} else if (name.equals(STATISTIC_SHIELD)) {
			return ac.getShield();
		} else if (name.equals(STATISTIC_NATURAL_ARMOR)) {
			return ac.getNaturalArmor();
		} else if (name.equals(STATISTIC_INITIATIVE)) {
			return initiative;
//		} else if (name.equals(STATISTIC_SKILLS)) {
//			return skills;
//		} else if (name.startsWith(STATISTIC_SKILLS+".")) {
//			SkillType type = SkillType.getSkill(name.substring(STATISTIC_SKILLS.length()+1));
//			return skills.getSkill(type);
////		} else if (name.equals(STATISTIC_SAVING_THROWS)) {
////			// TODO implement?
////			return null;
//		} else if (name.equals(STATISTIC_LEVEL)) {
//			return level;
		} else if (name.equals(STATISTIC_HPS)) {
			return hps;
		} else if (name.equals(STATISTIC_ATTACKS)) {
			return attacks;
		} else if (name.equals(STATISTIC_DAMAGE)) {
			return attacks.getDamageStatistic();
		} else if (name.equals(STATISTIC_SIZE)) {
			return size;
		} else {
			System.out.println("Unknown statistic " + name);
			return null;
		}
	}

	// TODO refactor the BuffListModel class and this accessor
	public ListModelWithToolTips getBuffListModel() {
		return buffs;
	}

	public void addBuff(Buff b) {
		b.applyBuff(this);
		buffs.addElement(b);
	}

	public void removeBuff(Buff b) {
		b.removeBuff(this);
		buffs.removeElement(b);
	}

// remove a buff by id
	public void removeBuff(int id) {
		for (int i = 0; i < buffs.getSize(); i++) {
			Buff b = (Buff) buffs.get(i);
			if (b.id == id) {
				b.removeBuff(this);
				buffs.removeElement(b);
			}
		}
	}

	public Object getProperty(String prop) {
		if (prop.equals(PROPERTY_NAME))
			return name;
		else if (prop.equals(PROPERTY_MAXHPS))
			return getMaximumHitPoints();
		else if (prop.equals(PROPERTY_WOUNDS))
			return getWounds();
		else if (prop.equals(PROPERTY_NONLETHAL))
			return getNonLethal();
		else if (prop.equals(PROPERTY_INITIATIVE))
			return getInitiativeModifier();
		else if (prop.equals(PROPERTY_AC))
			return getAC();
		else if (prop.equals(PROPERTY_SPACE))
			return getSpace();
		else if (prop.equals(PROPERTY_REACH))
			return getReach();
		else if (prop.equals(PROPERTY_BAB))
			return attacks.getBAB();
		else if (extraProperties.containsKey(prop)) {
//			System.out.println("extra property '"+prop+"': "+extraProperties.get(prop));
			return extraProperties.get(prop);
		} else {
//			System.out.println("Attempt to get unknown property: " + prop);
			return null;
		}
	}

	public void setProperty(String prop, Object value) {
		if (prop.equals(PROPERTY_NAME))
			setName((String) value);
		else if (prop.equals(PROPERTY_MAXHPS))
			setMaximumHitPoints((Integer) value);
		else if (prop.equals(PROPERTY_WOUNDS))
			setWounds((Integer) value);
		else if (prop.equals(PROPERTY_NONLETHAL))
			setNonLethal((Integer) value);
		else if (prop.equals(PROPERTY_INITIATIVE))
			setInitiativeModifier((Integer) value);
		else if (prop.equals(PROPERTY_AC))
			setAC((Integer) value);
		else if (prop.equals(PROPERTY_SIZE))
			size.setBaseSize((SizeCategory) value);
		else if (prop.equals(PROPERTY_SPACE))
			setSpace((Integer) value);
		else if (prop.equals(PROPERTY_REACH))
			setReach((Integer) value);
		else if (prop.equals(PROPERTY_BAB))
			attacks.setBAB((Integer) value);
		else {
			//System.out.println("Attempt to set unknown property: " + prop + " to " + value);
			Object old = extraProperties.get(prop);
			extraProperties.put(prop, value);
			pcs.firePropertyChange(prop, old, value);
		}
	}

	protected void firePropertyChange(String property, Object oldValue, Object newValue) {
		pcs.firePropertyChange(property, oldValue, newValue);
	}

	@Override
	public String toString() {
		return name;
	}

	public String getName() {
		return name;
	}

	//------------------- property level get/set methods -------------------
	// TODO most of these should be removed - they should be manipulated via the statistic

	public Modifier getAbilityModifier(AbilityScore.Type ability) {
		if (abilities.get(ability) == null) return null;
		return abilities.get(ability).getModifier();
	}

	public int getInitiativeModifier() {
		return initiative.getValue();
	}

	public void setInitiativeModifier(int i) {
		initiative.setBaseValue(i - initiative.getValue());
	}

	//------------------- Hit Points -------------------
	// Hit points have a maximum value, wounds taken, non-lethal taken and a calculated
	// current value

	public int getMaximumHitPoints() {
		return hps.getMaximumHitPoints();
	}

	public void setMaximumHitPoints(int hp) {
		hps.setMaximumHitPoints(hp);
	}

	public int getWounds() {
		return hps.getWounds();
	}

	public void setWounds(int i) {
		hps.setWounds(i);
	}

	public int getNonLethal() {
		return hps.getNonLethal();
	}

	public void setNonLethal(int i) {
		hps.setNonLethal(i);
	}

	public int getHPs() {
		return hps.getHPs();
	}

	//------------- Size --------------
	public SizeCategory getSize() {
		return size.getSize();
	}

	public void setSize(SizeCategory s) {
		size.setBaseSize(s);
	}

	public int getSpace() {
		return size.getSpace();
	}

	public void setSpace(int s) {
		size.setBaseSpace(s);
	}

	public int getReach() {
		return size.getReach();
	}

	public void setReach(int r) {
		size.setBaseReach(r);
	}

	//------------------- Armor Class -------------------
	/**
	 * Returns the temporary ac if there is one, otherwise calculates the total ac
	 * from the ac components
	 * 
	 * @return current total ac
	 */
	public int getAC() {
		return getAC(true);
	}

	private int getAC(boolean allowTemp) {
		if (allowTemp && hasTempAC) return tempAC;
		return ac.getValue();
	}

	/**
	 * Returns the temporary flat-footed ac if there is one, otherwise calculates the
	 * flat-footed ac from the ac components with any positive dexterity modifier
	 * ignored.
	 * 
	 * @return current flat-footed ac
	 */
	public int getFlatFootedAC() {
		return getFlatFootedAC(true);
	}

	private int getFlatFootedAC(boolean allowTemp) {
		if (allowTemp && hasTempFF) return tempFF;
		return ac.getFlatFootedAC().getValue();
	}

	/**
	 * Returns the temporary touch ac if there is one, otherwise calculates the touch
	 * ac from the ac components with all armor, shield and natural armor bonuses
	 * ignored.
	 * 
	 * @return current touch ac
	 */
	public int getTouchAC() {
		return getTouchAC(true);
	}

	private int getTouchAC(boolean allowTemp) {
		if (allowTemp && hasTempTouch) return tempTouch;
		return ac.getTouchAC().getValue();
	}

	/**
	 * Sets a temporary full ac score. Setting this to the normal value will remove
	 * the temporary score (as will <code>clearTemporaryAC()</code>)
	 * 
	 * @param ac
	 *            the score to set the full ac to
	 */
	public void setAC(int tempac) {
		if (hasTempTouch) {
			int totAC = getAC(false);
			if (totAC == tempac) {
				hasTempAC = false;
				firePropertyChange(PROPERTY_AC, tempAC, totAC);
				return;
			}
		}
		int old = getAC();
		tempAC = tempac;
		hasTempAC = true;
		firePropertyChange(PROPERTY_AC, old, ac);
	}

	/**
	 * Sets a temporary touch ac score. Setting this to the normal value will remove
	 * the temporary score (as will <code>clearTemporaryTouchAC()</code>
	 * 
	 * @param ac
	 *            the score to set the touch ac to
	 */
	public void setTouchAC(int tempac) {
		if (hasTempTouch) {
			int totAC = getTouchAC(false);
			if (totAC == tempac) {
				hasTempTouch = false;
				firePropertyChange(PROPERTY_AC, tempTouch, totAC);
				return;
			}
		}
		int old = getTouchAC();
		tempTouch = tempac;
		hasTempTouch = true;
		firePropertyChange(PROPERTY_AC, old, ac);
	}

	/**
	 * Sets a temporary flat-footed ac score. Setting this to the normal value will
	 * remove the temporary score (as will <code>clearTemporaryFlatFootedAC()</code>
	 * 
	 * @param ac
	 *            the score to set the flat-footed ac to
	 */
	public void setFlatFootedAC(int tempac) {
		if (hasTempFF) {
			int totAC = getFlatFootedAC(false);
			if (totAC == tempac) {
				hasTempFF = false;
				firePropertyChange(PROPERTY_AC, tempFF, totAC);
				return;
			}
		}
		int old = getFlatFootedAC();
		tempFF = tempac;
		hasTempFF = true;
		firePropertyChange(PROPERTY_AC, old, ac);
	}

	//------------- Others --------------
	abstract public boolean hasFeat(String feat);

	public boolean hasProperty(String name) {
		return extraProperties.containsKey(name);
	}

	// ----- visitor pattern for processing -----
	public void executeProcess(CreatureProcessor processor) {
		processor.processCreature(this);

		for (AbilityScore s : abilities.values()) {
			processor.processAbilityScore(s);
		}

		processor.processHPs(hps);
		processor.processInitiative(initiative);
		processor.processSize(size);

		for (SavingThrow.Type t : saves.keySet()) {
			SavingThrow s = saves.get(t);
			processor.processSavingThrow(s);
		}

		processor.processAC(ac);
		processor.processAttacks(attacks);

		for (int i = 0; i < buffs.getSize(); i++) {
			Buff b = (Buff) buffs.get(i);
			processor.processBuff(b);
		}

		for (String prop : extraProperties.keySet()) {
			processor.processProperty(prop, extraProperties.get(prop));
		}
	}
}
