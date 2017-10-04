package ui;

import javax.swing.JFormattedTextField;

import gamesystem.core.Property;

// Binds a formatted text field to an Property<Integer>. Setting the value will set an override on the property (removing any previous override).

@SuppressWarnings("serial")
public class BoundIntegerField extends JFormattedTextField {
	Property.PropertyValue<Integer> overrideKey = null;

	public BoundIntegerField(Property<Integer> property, int columns) {
		addPropertyChangeListener("value", evt -> {
			if (evt.getPropertyName().equals("value")) {
				Integer val = (Integer) BoundIntegerField.this.getValue();
				if (val != null && !val.equals(property.getValue())) {
					if (overrideKey != null) property.removeOverride(overrideKey);
					overrideKey = property.addOverride(val);
//					System.out.println("Setting override on " + property.getName() + " to " + val);
				}
			}
		});
		property.addPropertyListener((source, old) -> {
			//it's ok to do this even if this change event is due to an update from this control
			//because setValue will not fire a change event if the property isn't actually changing
			setValue(property.getValue());
//			System.out.println("Setting field from " + property.getName() + " to " + property.getValue() + " due to update");
		});
		setColumns(columns);
		setValue(property.getValue());
//		System.out.println("Setting field from " + property.getName() + " to " + property.getValue() + " - initialization");
	}
}
