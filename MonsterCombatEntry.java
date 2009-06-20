import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.beans.PropertyChangeEvent;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import party.Creature;

@SuppressWarnings("serial")
public class MonsterCombatEntry extends CombatEntry {
	public MonsterCombatEntry(Creature creature) {
		this.creature = creature;

		JFormattedTextField field = new JFormattedTextField();
		field.setValue(new Integer(0));
		field.setColumns(4);
		field.addPropertyChangeListener("value", this);
		acComp = field;
		field = new JFormattedTextField();
		field.setValue(new Integer(0));
		field.setColumns(4);
		field.addPropertyChangeListener("value", this);
		touchACComp = field;
		field = new JFormattedTextField();
		field.setValue(new Integer(0));
		field.setColumns(4);
		field.addPropertyChangeListener("value", this);
		flatFootedACComp = field;

		field = new JFormattedTextField();
		field.setValue(new Integer(0));
		field.setColumns(3);
		field.addPropertyChangeListener("value", this);
		modifierComp = field;

		creature.addPropertyChangeListener(this);
		createPanel();
	}

	protected JComponent createNameSection() {
		JPanel section = new JPanel();
		section.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();

		c.gridx = 0; c.gridy = 0;
		c.insets = new Insets(2, 3, 2, 3);
		delete = new JButton("X");
		delete.setMargin(new Insets(0, 4, 0, 3));
		delete.setFocusPainted(false);
		delete.setEnabled(false);
		delete.addActionListener(this);
		section.add(delete, c);
		nameField = new JTextField(20);
		nameField.getDocument().addDocumentListener(new DocumentListener() {
			public void changedUpdate(DocumentEvent e) {
				initBlank();
				fireChange();
			}

			public void insertUpdate(DocumentEvent e) {
				initBlank();
				fireChange();
			}

			public void removeUpdate(DocumentEvent e) {
				initBlank();
				fireChange();
			}
			
		});

		c.gridx = GridBagConstraints.RELATIVE;
		c.weightx = 1.0;
		c.anchor = GridBagConstraints.LINE_START;
		section.add(nameField, c);

		return section;
	}

	public void propertyChange(PropertyChangeEvent evt) {
		super.propertyChange(evt);
		if (evt.getSource() == creature) {
			// update the relevant fields
			if (evt.getPropertyName().equals(Creature.PROPERTY_INITIATIVE)) {
				((JFormattedTextField)modifierComp).setValue(creature.getInitiativeModifier());
			} else if (evt.getPropertyName().equals(Creature.PROPERTY_AC)) {
				((JFormattedTextField)acComp).setValue(creature.getAC());
				((JFormattedTextField)touchACComp).setValue(creature.getTouchAC());
				((JFormattedTextField)flatFootedACComp).setValue(creature.getFlatFootedAC());
			}

		} else if (evt.getPropertyName().equals("value")) {
			if (initBlank()) fireChange();
		}
	}
}
