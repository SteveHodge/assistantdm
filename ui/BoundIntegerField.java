package ui;

import javax.swing.JFormattedTextField;

import gamesystem.Creature;


// TODO this can be done with beans binding (JGoodies/JFace) - it's a more general solution

@SuppressWarnings("serial")
public class BoundIntegerField extends JFormattedTextField {
	protected Creature creature;
	protected String property;

	public BoundIntegerField(Creature c, String prop, int columns) {
		creature = c;
		property = prop;
		addPropertyChangeListener("value", evt -> {
			if (evt.getPropertyName().equals("value")) {
				//TODO some type checking should be done
				creature.setProperty(property, BoundIntegerField.this.getValue());
			}
		});
		creature.addPropertyChangeListener(property,
				//TODO some type checking should be done
				//it's ok to do this even if this change event is due to an update from this control
				//because setValue will not fire a change event if the property isn't actually changing
				evt -> setValue(creature.getPropertyValue(property)));
		setColumns(columns);
		//TODO some type checking should be done
		setValue(creature.getPropertyValue(property));
	}
}
