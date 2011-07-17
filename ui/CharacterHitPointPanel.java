package ui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.BoxLayout;
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

	public CharacterHitPointPanel(Character c) {
		character = c;

		setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));

		setBorder(new TitledBorder("Hit Points"));
		add(new JLabel(" Current Hitpoints: "));

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

		add(new JLabel(" Maximum Hitpoints: "));
		add(new BoundIntegerField(character, Creature.PROPERTY_MAXHPS, 3));
		add(new JLabel(" Wounds: "));
		add(new BoundIntegerField(character, Creature.PROPERTY_WOUNDS, 3));
		add(new JLabel(" Non-lethal: "));
		add(new BoundIntegerField(character, Creature.PROPERTY_NONLETHAL, 3));

		// update fields when character changes
		character.addPropertyChangeListener(this);
	}

	public void propertyChange(PropertyChangeEvent arg0) {
		if (arg0.getPropertyName().equals(Creature.PROPERTY_MAXHPS)
				|| arg0.getPropertyName().equals(Creature.PROPERTY_WOUNDS)
				|| arg0.getPropertyName().equals(Creature.PROPERTY_NONLETHAL)) {
			currHP.setValue(new Integer(character.getHPs()));
		}
	}
}
