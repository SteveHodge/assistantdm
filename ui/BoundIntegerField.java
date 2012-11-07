package ui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JFormattedTextField;

import party.Creature;

// TODO this can be done with beans binding (JGoodies/JFace) - it's a more general solution

@SuppressWarnings("serial")
public class BoundIntegerField extends JFormattedTextField {
	protected Creature creature;
	protected String property;

	public BoundIntegerField(Creature c, String prop, int columns) {
		creature = c;
		property = prop;
		addPropertyChangeListener("value", new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				if (evt.getPropertyName().equals("value")) {
					//TODO some type checking should be done
					creature.setProperty(property, BoundIntegerField.this.getValue());
				}
			}
		});
		creature.addPropertyChangeListener(property, new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				//TODO some type checking should be done
				//it's ok to do this even if this change event is due to an update from this control
				//because setValue will not fire a change event if the property isn't actually changing
				setValue(creature.getProperty(property));
			}
		});
		setColumns(columns);
		//TODO some type checking should be done
		setValue(creature.getProperty(property));
	}
}
