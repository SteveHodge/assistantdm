package gamesystem;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/*
 * Abstract implementation of Modifier that provides simple PropertyChangeListener support and default
 * type, source and condition of null, and ID of 0.
 * This is a useful starting point for any mutable Modifier implementation 
 */
public abstract class AbstractModifier implements Modifier {
	final protected PropertyChangeSupport pcs = new PropertyChangeSupport(this);

	public String getType() {
		return null;
	}

	public String getSource() {
		return null;
	}

	public String getCondition() {
		return null;
	}

	public int getID() {
		return 0;
	}

	public void addPropertyChangeListener(PropertyChangeListener listener) {
		pcs.addPropertyChangeListener(listener);
	}

	public void removePropertyChangeListener(PropertyChangeListener listener) {
		pcs.removePropertyChangeListener(listener);
	}

	public String toString() {
		String s = "";
		if (getType() != null) s += " "+getType();
		if (getModifier() >= 0) {
			s = "+" + getModifier() + s + " bonus";
		} else {
			s = getModifier() + s + " penalty";
		}

		if (getCondition() != null) s += " " + getCondition();
		if (getSource() != null) s += " (from "+getSource()+")";
		return s;
	}
}
