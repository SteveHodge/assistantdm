package ui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import party.Character;
import party.Creature;

@SuppressWarnings("serial")
public class CharacterHitPointPanel extends JPanel implements PropertyChangeListener {
	protected Character character;
	JFormattedTextField currHP;
	JFormattedTextField maxHP;
	JFormattedTextField wounds;
	JFormattedTextField nonLethal;

	public CharacterHitPointPanel(Character c) {
		character = c;

		setBorder(new TitledBorder("Hit Points"));
		add(new JLabel("Current Hitpoints: "));

		currHP = new JFormattedTextField();
		currHP.setValue(new Integer(character.getHPs()));
		currHP.setColumns(3);
		currHP.addPropertyChangeListener("value", new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				if (evt.getPropertyName().equals("value")) {
					int total = (Integer)currHP.getValue();
					character.setWounds(character.getMaximumHitPoints()-character.getNonLethal()-total);
				}
			}
		});
		add(currHP);

		add(new JLabel("Maximum Hitpoints: "));

		maxHP = new JFormattedTextField();
		maxHP.setValue(new Integer(character.getMaximumHitPoints()));
		maxHP.setColumns(3);
		maxHP.addPropertyChangeListener("value", new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				if (evt.getPropertyName().equals("value")) {
					int total = (Integer)maxHP.getValue();
					character.setMaximumHitPoints(total);
				}
			}
		});
		add(maxHP);
		
		add(new JLabel("Wounds: "));

		wounds = new JFormattedTextField();
		wounds.setValue(new Integer(character.getWounds()));
		wounds.setColumns(3);
		wounds.addPropertyChangeListener("value", new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				if (evt.getPropertyName().equals("value")) {
					int total = (Integer)wounds.getValue();
					character.setWounds(total);
				}
			}
		});
		add(wounds);

		add(new JLabel("Non-lethal: "));

		nonLethal = new JFormattedTextField();
		nonLethal.setValue(new Integer(character.getNonLethal()));
		nonLethal.setColumns(3);
		nonLethal.addPropertyChangeListener("value", new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				if (evt.getPropertyName().equals("value")) {
					int total = (Integer)nonLethal.getValue();
					character.setNonLethal(total);
				}
			}
		});
		add(nonLethal);

		// update fields when character changes
		character.addPropertyChangeListener(this);
	}

	public void propertyChange(PropertyChangeEvent arg0) {
		currHP.setValue(new Integer(character.getHPs()));
		if (arg0.getPropertyName().equals(Creature.PROPERTY_MAXHPS)) {
			maxHP.setValue(new Integer(character.getMaximumHitPoints()));
		} else if (arg0.getPropertyName().equals(Creature.PROPERTY_WOUNDS)) {
			wounds.setValue(new Integer(character.getWounds()));
		} else if (arg0.getPropertyName().equals(Creature.PROPERTY_NONLETHAL)) {
			nonLethal.setValue(new Integer(character.getNonLethal()));
		}
	}
}
