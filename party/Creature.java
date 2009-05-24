package party;
import java.beans.PropertyChangeListener;

public interface Creature {
	public String getName();
	public void setName(String name);

	public int getInitiativeModifier();

	public int getMaximumHitPoints();
	public void setMaximumHitPoints(int ac);

	public int getWounds();
	public void setWounds(int wounds);

	public int getNonLethal();
	public void setNonLethal(int nonLethal);

	public int getAC();
	public void setAC(int ac);
	
	public int getTouchAC();
	public void setTouchAC(int ac);
	
	public int getFlatFootedAC();
	public void setFlatFootedAC(int ac);

	public void addPropertyChangeListener(PropertyChangeListener listener);
	public void removePropertyChangeListener(PropertyChangeListener listener);
}
