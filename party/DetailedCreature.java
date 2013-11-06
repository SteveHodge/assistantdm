package party;

import gamesystem.AC;
import gamesystem.AbilityScore;
import gamesystem.Attacks;
import gamesystem.HPs;
import gamesystem.InitiativeModifier;
import gamesystem.Modifier;
import gamesystem.SavingThrow;
import gamesystem.Size;
import gamesystem.SizeCategory;
import gamesystem.Statistic;

import java.util.EnumMap;

/*
 * partial implementation of Statistic based Creature (Character or DetailedMonster)
 * eventually this will be folded back into Creature
 */

public abstract class DetailedCreature extends Creature {
	String name;

	HPs hps;
	Size size;
	InitiativeModifier initiative;
	EnumMap<SavingThrow.Type, SavingThrow> saves = new EnumMap<SavingThrow.Type, SavingThrow>(SavingThrow.Type.class);
	EnumMap<AbilityScore.Type, AbilityScore> abilities = new EnumMap<AbilityScore.Type, AbilityScore>(AbilityScore.Type.class);

	AC ac;
	int tempAC, tempTouch, tempFF;	// ac overrides
	boolean hasTempAC, hasTempTouch, hasTempFF;	// flags for overrides

	Attacks attacks;

	void firePropertyChange(String property, Object oldValue, Object newValue) {
		pcs.firePropertyChange(property, oldValue, newValue);
	}

	@Override
	public String toString() {
		return name;
	}

	@Override
	public String getName() {
		return name;
	}

	public Modifier getAbilityModifier(AbilityScore.Type ability) {
		if (abilities.get(ability) == null) return null;
		return abilities.get(ability).getModifier();
	}

	@Override
	public int getInitiativeModifier() {
		return initiative.getValue();
	}

	//------------------- Hit Points -------------------
	// Hit points have a maximum value, wounds taken, non-lethal taken and a calculated
	// current value

	@Override
	public int getMaximumHitPoints() {
		return hps.getMaximumHitPoints();
	}

	@Override
	public void setMaximumHitPoints(int hp) {
		hps.setMaximumHitPoints(hp);
	}

	@Override
	public int getWounds() {
		return hps.getWounds();
	}

	@Override
	public void setWounds(int i) {
		hps.setWounds(i);
	}

	@Override
	public int getNonLethal() {
		return hps.getNonLethal();
	}

	@Override
	public void setNonLethal(int i) {
		hps.setNonLethal(i);
	}

	public int getHPs() {
		return hps.getHPs();
	}

	//------------- Size --------------
	@Override
	public SizeCategory getSize() {
		return size.getSize();
	}

	@Override
	public void setSize(SizeCategory s) {
		size.setBaseSize(s);
	}

	@Override
	public int getSpace() {
		return size.getSpace();
	}

	@Override
	public void setSpace(int s) {
		size.setBaseSpace(s);
	}

	@Override
	public int getReach() {
		return size.getReach();
	}

	@Override
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
	@Override
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
	@Override
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
	@Override
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
	@Override
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
	@Override
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
	@Override
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
	@Override
	abstract public void setName(String name);

	@Override
	abstract public Statistic getStatistic(String target);

	@Override
	abstract public void setInitiativeModifier(int init);

	@Override
	abstract public Object getProperty(String prop);

	@Override
	abstract public void setProperty(String prop, Object value);

	abstract public boolean hasFeat(String feat);
}
