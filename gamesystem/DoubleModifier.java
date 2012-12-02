package gamesystem;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class DoubleModifier extends AbstractModifier {
	final Modifier modifier;

	public DoubleModifier(Modifier m) {
		modifier = m;
		modifier.addPropertyChangeListener(new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent e) {
				Object oldValue = e.getOldValue();
				Object doubleOld = oldValue;
				if (oldValue != null && oldValue instanceof Integer) doubleOld = 2*(Integer)oldValue;

				Object newValue = e.getNewValue();
				Object doubleNew = newValue;
				if (newValue != null && newValue instanceof Integer) doubleNew = 2*(Integer)newValue;

				pcs.firePropertyChange(e.getPropertyName(), doubleOld, doubleNew);
			}
		});
	}

	public int getModifier() {
		return 2*modifier.getModifier();
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