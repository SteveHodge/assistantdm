import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.beans.PropertyChangeEvent;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import party.Creature;
import party.Monster;
import ui.BoundIntegerField;

@SuppressWarnings("serial")
public class MonsterCombatEntry extends CombatEntry {
	public MonsterCombatEntry(Creature creature) {
		this.creature = creature;

		acComp = new BoundIntegerField(creature, Monster.PROPERTY_AC, 4);
		touchACComp = new BoundIntegerField(creature, Monster.PROPERTY_AC_TOUCH, 4);
		flatFootedACComp = new BoundIntegerField(creature, Monster.PROPERTY_AC_FLATFOOTED, 4);

		modifierComp = new BoundIntegerField(creature, Creature.PROPERTY_INITIATIVE, 3);

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
		if (evt.getPropertyName().equals("value")) {
			if (initBlank()) fireChange();
		}
	}
}
