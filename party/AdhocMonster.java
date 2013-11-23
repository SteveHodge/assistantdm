package party;

import gamesystem.SizeCategory;
import gamesystem.Statistic;
import monsters.StatisticsBlock;

/*
 * Minimal implementation of Creature used for adhoc monsters created in the CombatPanel
 * Probably will be removed once DetailMonster is fully functional
 */

public class AdhocMonster extends Creature implements Monster {
	private int fullAC;
	private int flatFootedAC;
	private int touchAC;
	private int initMod;
	private int maxHPs;
	private int wounds = 0;
	private int nonLethal = 0;
	private SizeCategory size = SizeCategory.MEDIUM;
	private int space;
	private int reach;
	private String name;

	@Override
	public int getAC() {return fullAC;}
	@Override
	public int getFlatFootedAC() {return flatFootedAC;}
	@Override
	public int getInitiativeModifier() {return initMod;}
	@Override
	public int getMaximumHitPoints() {return maxHPs;}
	public int getHitPoints() {return maxHPs - wounds - nonLethal;}
	@Override
	public String getName() {return name;}
	@Override
	public int getTouchAC() {return touchAC;}

	public boolean isEditable() {return true;}

	public final static String PROPERTY_AC_TOUCH = "AC: Touch";
	public final static String PROPERTY_AC_FLATFOOTED = "AC: Flat Footed";

	@Override
	public String toString() {
		return name;
	}

	@Override
	public void setAC(int ac) {
		int old = fullAC;
		fullAC = ac;
		pcs.firePropertyChange(PROPERTY_AC, old, ac);
	}

	@Override
	public void setFlatFootedAC(int ac) {
		int old = flatFootedAC;
		flatFootedAC = ac;
		pcs.firePropertyChange(PROPERTY_AC_FLATFOOTED, old, ac);
	}

	@Override
	public void setMaximumHitPoints(int hp) {
		int old = maxHPs;
		maxHPs = hp;
		pcs.firePropertyChange(PROPERTY_MAXHPS, old, hp);
	}

	@Override
	public void setName(String name) {
		String old = name;
		this.name = name;
		pcs.firePropertyChange(PROPERTY_NAME, old, name);
	}

	@Override
	public void setTouchAC(int ac) {
		int old = fullAC;
		touchAC = ac;
		pcs.firePropertyChange(PROPERTY_AC_TOUCH, old, ac);
	}

	@Override
	public void setInitiativeModifier(int initMod) {
		int old = this.initMod;
		this.initMod = initMod;
		pcs.firePropertyChange(PROPERTY_INITIATIVE, old, initMod);
	}

	@Override
	public int getNonLethal() {
		return nonLethal;
	}

	@Override
	public int getWounds() {
		return wounds;
	}

	@Override
	public void setNonLethal(int nonLethal) {
		int old = this.nonLethal;
		this.nonLethal = nonLethal;
		pcs.firePropertyChange(PROPERTY_NONLETHAL, old, nonLethal);
	}

	@Override
	public void setWounds(int wounds) {
		int old = this.wounds;
		this.wounds = wounds;
		pcs.firePropertyChange(PROPERTY_WOUNDS, old, wounds);
	}

	@Override
	public void setSpace(int s) {
		int old = space;
		space = s;
		pcs.firePropertyChange(PROPERTY_SPACE, old, space);
	}

	@Override
	public int getSpace() {
		return space;
	}

	@Override
	public void setReach(int r) {
		int old = reach;
		reach = r;
		pcs.firePropertyChange(PROPERTY_REACH, old, reach);
	}

	@Override
	public int getReach() {
		return reach;
	}

	@Override
	public void setSize(SizeCategory s) {
		SizeCategory old = size;
		size = s;
		pcs.firePropertyChange(PROPERTY_SIZE, old, size);
	}

	@Override
	public SizeCategory getSize() {
		return size;
	}

	@Override
	public Object getProperty(String prop) {
		if (prop.equals(PROPERTY_NAME)) return name;
		if (prop.equals(PROPERTY_MAXHPS)) return maxHPs;
		if (prop.equals(PROPERTY_WOUNDS)) return wounds;
		if (prop.equals(PROPERTY_NONLETHAL)) return nonLethal;
		if (prop.equals(PROPERTY_INITIATIVE)) return initMod;
		if (prop.equals(PROPERTY_AC)) return fullAC;
		if (prop.equals(PROPERTY_AC_FLATFOOTED)) return flatFootedAC;
		if (prop.equals(PROPERTY_AC_TOUCH)) return touchAC;
		if (prop.equals(PROPERTY_SPACE)) return space;
		if (prop.equals(PROPERTY_REACH)) return reach;
		return null;
	}

	@Override
	public void setProperty(String prop, Object value) {
		if (prop.equals(PROPERTY_NAME)) setName((String)value);
		if (prop.equals(PROPERTY_MAXHPS)) setMaximumHitPoints((Integer)value);
		if (prop.equals(PROPERTY_WOUNDS)) setWounds((Integer)value);
		if (prop.equals(PROPERTY_NONLETHAL)) setNonLethal((Integer)value);
		if (prop.equals(PROPERTY_INITIATIVE)) setInitiativeModifier((Integer)value);
		if (prop.equals(PROPERTY_AC)) setAC((Integer)value);
		if (prop.equals(PROPERTY_AC_FLATFOOTED)) setFlatFootedAC((Integer)value);
		if (prop.equals(PROPERTY_AC_TOUCH)) setTouchAC((Integer)value);
		if (prop.equals(PROPERTY_SPACE)) setSpace((Integer) value);
		if (prop.equals(PROPERTY_REACH)) setReach((Integer) value);
	}

	@Override
	public String getStatsBlockHTML() {

		StringBuilder s = new StringBuilder();
		s.append("<html><table><tr><td></td><td>").append(getName()).append("</td></tr>");

		// SIZE_TYPE
		// HITDICE
		s.append("<tr><td>").append(StatisticsBlock.Field.INITIATIVE).append("</td><td>").append(getInitiativeModifier()).append("</td></tr>");
		// SPEED
		s.append("<tr><td>").append(StatisticsBlock.Field.AC).append("</td><td>");
		s.append(getAC()).append(", touch ").append(getTouchAC()).append(", flat-footed ").append(getFlatFootedAC());
		s.append("</td></tr>");
		// BASE_ATTACK_GRAPPLE
		//		ATTACK
		//		FULL_ATTACK
		//		SPACE_REACH
		//		SPECIAL_ATTACKS
		//		SPECIAL_QUALITIES
		//		SAVES
		//		ABILITIES
		//		SKILLS
		//		FEATS
		//		ENVIRONMENT
		//		ORGANIZATION
		//		CR
		//		TREASURE
		//		ALIGNMENT
		//		ADVANCEMENT
		//		LEVEL_ADJUSTMENT

		s.append("</table></html>");
		return s.toString();
	}

	@Override
	public Statistic getStatistic(String target) {
		return null;
	}
}
