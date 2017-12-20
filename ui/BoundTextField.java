package ui;

import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;

import gamesystem.core.SimpleProperty;
import gamesystem.core.SimpleValueProperty;
import party.Character;

@SuppressWarnings("serial")
public class BoundTextField extends JTextField {
	protected SimpleValueProperty<String> property;
	protected boolean modifying = false;

	@SuppressWarnings("unchecked")
	public BoundTextField(Character character, String propName, int columns) {
		SimpleProperty<?> property = character.getProperty(propName);
		if (property == null && !propName.startsWith("extra.")) {
			property = character.getProperty("extra." + propName);
		}
		if (property == null) {
//			System.out.println("Creating property " + propName);
			initialize(new SimpleValueProperty<String>(propName, character, ""), columns);
		} else if (property instanceof SimpleValueProperty) {
//			System.out.println("Current value " + property.getValue().toString());
			initialize((SimpleValueProperty<String>) property, columns);
		} else {
			throw new IllegalArgumentException("Property " + propName + " is already defined but not an editable string property: " + property.getClass());
		}
	}

	public BoundTextField(SimpleValueProperty<String> prop, int columns) {
		initialize(prop, columns);
	}

	private void initialize(SimpleValueProperty<String> prop, int columns) {
		property = prop;
		getDocument().addDocumentListener(new DocumentListener() {
			protected void update(DocumentEvent e,String op) {
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
				update(e,"change");
			}

			@Override
			public void insertUpdate(DocumentEvent e) {
				update(e,"insert");
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				update(e,"remove");
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
}
