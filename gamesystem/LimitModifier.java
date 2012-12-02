package gamesystem;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class LimitModifier extends AbstractModifier {
	int limit = Integer.MAX_VALUE;
	Modifier modifier;

	public LimitModifier(Modifier m) {
		modifier = m;
		modifier.addPropertyChangeListener(new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent e) {
				pcs.firePropertyChange("value", null, getModifier());
			}
		});
	}

	public int getLimit() {
		return limit;
	}

	public void setLimit(int l) {
		if (l == limit) return;
		limit = l;
		pcs.firePropertyChange("value", null, getModifier());	// we always fire even if the actual modifier didn't change incase someone is interested in the limit
	}

	public int getModifier() {
		if (modifier.getModifier() < limit) return modifier.getModifier();
		return limit;
	}

	public String getType() {
		return modifier.getType();
	}

	public String getSource() {
		return modifier.getSource();
	}

	public String getCondition() {
		return modifier.getCondition();
	}

	// TODO is it right to forward this?
	public int getID() {
		return modifier.getID();
	}
}