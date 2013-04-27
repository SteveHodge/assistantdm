package party;

import monsters.StatisticsBlock;

public class Monster extends Creature {
	int fullAC;
	int flatFootedAC;
	int touchAC;
	int initMod;
	int maxHPs;
	int wounds = 0;
	int nonLethal = 0;
	String name;
	String html;
	
	public static Monster createMonster(StatisticsBlock stats) {
		Monster m = new Monster();
		m.setName(stats.getName());
		int[] ac = stats.getACs();
		m.setAC(ac[0]);
		m.setTouchAC(ac[1]);
		m.setFlatFootedAC(ac[2]);
		m.setMaximumHitPoints(stats.getDefaultHPs());
		m.setInitiativeModifier(stats.getInitiativeModifier());
		m.html = stats.getHTML();
		return m;
	}

	public int getAC() {return fullAC;}
	public int getFlatFootedAC() {return flatFootedAC;}
	public int getInitiativeModifier() {return initMod;}
	public int getMaximumHitPoints() {return maxHPs;}
	public int getHitPoints() {return maxHPs - wounds - nonLethal;}
	public String getName() {return name;}
	public int getTouchAC() {return touchAC;}

	public boolean isEditable() {return true;}

	public final static String PROPERTY_AC_TOUCH = "AC: Touch";
	public final static String PROPERTY_AC_FLATFOOTED = "AC: Flat Footed";
	
	public String toString() {
		return name;
	}

	public String getHTML() {
		return html;
	}
	
	public void setAC(int ac) {
		int old = fullAC;
		fullAC = ac;
        pcs.firePropertyChange(PROPERTY_AC, old, ac);
	}

	public void setFlatFootedAC(int ac) {
		int old = flatFootedAC;
		flatFootedAC = ac;
        pcs.firePropertyChange(PROPERTY_AC_FLATFOOTED, old, ac);
	}

	public void setMaximumHitPoints(int hp) {
		int old = maxHPs;
		maxHPs = hp;
        pcs.firePropertyChange(PROPERTY_MAXHPS, old, hp);
	}

	public void setName(String name) {
		String old = name;
		this.name = name;
        pcs.firePropertyChange(PROPERTY_NAME, old, name);
	}

	public void setTouchAC(int ac) {
		int old = fullAC;
		touchAC = ac;
        pcs.firePropertyChange(PROPERTY_AC_TOUCH, old, ac);
	}

	public void setInitiativeModifier(int initMod) {
		int old = this.initMod;
		this.initMod = initMod;
        pcs.firePropertyChange(PROPERTY_INITIATIVE, old, initMod);
	}

	public int getNonLethal() {
		return nonLethal;
	}

	public int getWounds() {
		return wounds;
	}

	public void setNonLethal(int nonLethal) {
		int old = this.nonLethal;
		this.nonLethal = nonLethal;
        pcs.firePropertyChange(PROPERTY_NONLETHAL, old, nonLethal);
	}

	public void setWounds(int wounds) {
		int old = this.wounds;
		this.wounds = wounds;
        pcs.firePropertyChange(PROPERTY_WOUNDS, old, wounds);
	}

	public Object getProperty(String prop) {
		if (prop.equals(PROPERTY_NAME)) return name;
		if (prop.equals(PROPERTY_MAXHPS)) return maxHPs;
		if (prop.equals(PROPERTY_WOUNDS)) return wounds;
		if (prop.equals(PROPERTY_NONLETHAL)) return nonLethal;
		if (prop.equals(PROPERTY_INITIATIVE)) return initMod;
		if (prop.equals(PROPERTY_AC)) return fullAC;
		if (prop.equals(PROPERTY_AC_FLATFOOTED)) return flatFootedAC;
		if (prop.equals(PROPERTY_AC_TOUCH)) return touchAC;
		return null;
	}

	public void setProperty(String prop, Object value) {
		if (prop.equals(PROPERTY_NAME)) setName((String)value);
		if (prop.equals(PROPERTY_MAXHPS)) setMaximumHitPoints((Integer)value);
		if (prop.equals(PROPERTY_WOUNDS)) setWounds((Integer)value);
		if (prop.equals(PROPERTY_NONLETHAL)) setNonLethal((Integer)value);
		if (prop.equals(PROPERTY_INITIATIVE)) setInitiativeModifier((Integer)value);
		if (prop.equals(PROPERTY_AC)) setAC((Integer)value);
		if (prop.equals(PROPERTY_AC_FLATFOOTED)) setFlatFootedAC((Integer)value);
		if (prop.equals(PROPERTY_AC_TOUCH)) setTouchAC((Integer)value);
	}
}
