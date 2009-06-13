package party;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;


public class Monster extends Creature {
	int fullAC;
	int flatFootedAC;
	int touchAC;
	int initMod;
	int maxHPs;
	int wounds = 0;
	int nonLethal = 0;
	String name;

	private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

	public int getAC() {return fullAC;}
	public int getFlatFootedAC() {return flatFootedAC;}
	public int getInitiativeModifier() {return initMod;}
	public int getMaximumHitPoints() {return maxHPs;}
	public int getHitPoints() {return maxHPs - wounds - nonLethal;}
	public String getName() {return name;}
	public int getTouchAC() {return touchAC;}

	public boolean isEditable() {return true;}

	public void setAC(int ac) {
		int old = fullAC;
		fullAC = ac;
        pcs.firePropertyChange("totalAC", old, ac);
	}

	public void setFlatFootedAC(int ac) {
		int old = flatFootedAC;
		flatFootedAC = ac;
        pcs.firePropertyChange("flatFootedAC", old, ac);
	}

	public void setMaximumHitPoints(int hp) {
		int old = maxHPs;
		maxHPs = hp;
        pcs.firePropertyChange("maximumHitPoints", old, hp);
	}

	public void setName(String name) {
		String old = name;
		this.name = name;
        pcs.firePropertyChange("name", old, name);
	}

	public void setTouchAC(int ac) {
		int old = fullAC;
		touchAC = ac;
        pcs.firePropertyChange("touchAC", old, ac);
	}

	public void setInitiativeModifier(int initMod) {
		int old = this.initMod;
		this.initMod = initMod;
        pcs.firePropertyChange("initiative", old, initMod);
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
        pcs.firePropertyChange("nonLethal", old, nonLethal);
	}

	public void setWounds(int wounds) {
		int old = this.wounds;
		this.wounds = wounds;
        pcs.firePropertyChange("wounds", old, wounds);
	}

	public void addPropertyChangeListener(PropertyChangeListener listener) {
		pcs.addPropertyChangeListener(listener);
	}

	public void removePropertyChangeListener(PropertyChangeListener listener) {
		this.pcs.removePropertyChangeListener(listener);
	}
}
