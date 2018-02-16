package ui;

import java.awt.Color;
import java.awt.Dimension;

import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;

import gamesystem.core.OverridableProperty;
import gamesystem.core.Property;
import gamesystem.core.SettableProperty;
import gamesystem.core.SimpleValueProperty;
import party.Character;

// Property -> read-only label
// SettableProperty -> editable field, sets value
// SettableBaseValueProperty -> editable field, sets base value

// Note that components returned by methods in this class have their minimum size set to be the same as their preferred size.

// TODO better naming of methods
public class PropertyFields {
	public static JLabel createIntegerPropertyLabel(Property<Integer> property, String prefix) {
		JLabel label = new JLabel(prefix + property.getValue());
		property.addPropertyListener((source, oldValue) -> {
			label.setText(prefix + source.getValue());
		});
		return label;
	}

	// FIXME check if users of this should be using createSettableIntegerField instead
	public static JTextField createOverrideIntegerField(OverridableProperty<Integer> property, int columns) {
		return new BoundIntegerField(property, columns);
	}

	@SuppressWarnings("unchecked")
	public static JTextField createSettableTextField(Character character, String propName, int columns) {
		Property<?> property = character.getProperty(propName);
		if (property == null && !propName.startsWith("extra.")) {
			property = character.getProperty("extra." + propName);
		}
		if (property == null) {
//				System.out.println("Creating property " + propName);
			return new BoundTextField(new SimpleValueProperty<String>(propName, character, ""), columns);
		} else if (property instanceof SettableProperty) {
//				System.out.println("Current value " + property.getValue().toString());
			return new BoundTextField((SettableProperty<String>) property, columns);
		} else {
			throw new IllegalArgumentException("Property " + propName + " is already defined but not an editable string property: " + property.getClass());
		}
	}

	public static JTextField createSettableIntegerField(SettableProperty<Integer> property, int columns) {
		@SuppressWarnings("serial")
		JFormattedTextField field = new JFormattedTextField() {
			@Override
			public Dimension getMinimumSize() {
				return getPreferredSize();
			}
		};
		field.addPropertyChangeListener("value", evt -> {
			if (evt.getPropertyName().equals("value")) {
				Integer val = (Integer) field.getValue();
				if (val != null && !val.equals(property.getValue())) {
					property.setValue(val);
//							System.out.println("Setting value of " + property.getName() + " to " + val);
				}
			}
		});
		property.addPropertyListener((source, old) -> {
			//it's ok to do this even if this change event is due to an update from this control
			//because setValue will not fire a change event if the property isn't actually changing
			field.setValue(property.getValue());
//					System.out.println("Setting field from " + property.getName() + " to " + property.getValue() + " due to update");
		});
		field.setColumns(columns);
		field.setValue(property.getValue());
//				System.out.println("Setting field from " + property.getName() + " to " + property.getValue() + " - initialization");
		return field;
	}

// Binds a formatted text field to an Property<Integer>. Setting the value will set an override on the property (removing any previous override). When an override is set on the property, the background of the field will be blue.
// TODO typically we'd want all fields attached to a particular property to share the same override but that's not how it's presently implemented - perhaps a static override collection (then we could inline the class too)
	@SuppressWarnings("serial")
	private static class BoundIntegerField extends JFormattedTextField {
		OverridableProperty.PropertyValue<Integer> overrideKey = null;
		static public final Color overrideColor = new Color(104, 179, 255);

		public BoundIntegerField(OverridableProperty<Integer> property, int columns) {
			addPropertyChangeListener("value", evt -> {
				if (evt.getPropertyName().equals("value")) {
					Integer val = (Integer) BoundIntegerField.this.getValue();
					if (val != null && !val.equals(property.getValue())) {
						if (overrideKey != null) property.removeOverride(overrideKey);
						overrideKey = property.addOverride(val);
//						System.out.println("Setting override on " + property.getName() + " to " + val);
					}
				}
			});
			property.addPropertyListener((source, old) -> {
				//it's ok to do this even if this change event is due to an update from this control
				//because setValue will not fire a change event if the property isn't actually changing
				setValue(property.getValue());
				setBackground(property.hasOverride() ? overrideColor : Color.WHITE);
//				System.out.println("Setting field from " + property.getName() + " to " + property.getValue() + " due to update");
			});
			setColumns(columns);
			setValue(property.getValue());
			setBackground(property.hasOverride() ? overrideColor : Color.WHITE);
//			System.out.println("Setting field from " + property.getName() + " to " + property.getValue() + " - initialization");
		}

		@Override
		public Dimension getMinimumSize() {
			return getPreferredSize();
		}
	}

	@SuppressWarnings("serial")
	private static class BoundTextField extends JTextField {
		protected SettableProperty<String> property;
		protected boolean modifying = false;

		public BoundTextField(SettableProperty<String> prop, int columns) {
			property = prop;
			getDocument().addDocumentListener(new DocumentListener() {
				protected void update(DocumentEvent e, String op) {
					try {
						//System.out.println("Document "+op+" for "+BoundTextField.this.hashCode()+", setting character to "+e.getDocument().getText(0,e.getDocument().getLength()));
						modifying = true;
						String newVal = e.getDocument().getText(0, e.getDocument().getLength());
						if (!newVal.equals(property.getValue()))
							property.setValue(newVal);
						modifying = false;
					} catch (BadLocationException e1) {
						e1.printStackTrace();
					}
				}

				@Override
				public void changedUpdate(DocumentEvent e) {
					update(e, "change");
				}

				@Override
				public void insertUpdate(DocumentEvent e) {
					update(e, "insert");
				}

				@Override
				public void removeUpdate(DocumentEvent e) {
					update(e, "remove");
				}

			});

			property.addPropertyListener((source, old) -> {
				//System.out.println("Notified "+BoundTextField.this.hashCode()+", setting value to "+creature.getProperty(property));
				// we might get notified from the result of our own update (e.g. if there is another BoundTextField bound to the same property)
				// if we try to set the text in this case it will cause an exception, so we set a flag
				// TODO this is not threadsafe
				if (!modifying) {
					setText(property.getValue());
				}
			});
			setColumns(columns);
			if (property.getValue() != null) {
				setText(property.getValue());
			}
		}

		@Override
		public Dimension getMinimumSize() {
			return getPreferredSize();
		}

	}
}
