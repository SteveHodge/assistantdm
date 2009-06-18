package ui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import party.Creature;
import party.Character;

@SuppressWarnings("serial")
public class CharacterInitiativePanel extends JPanel implements PropertyChangeListener {
	protected Character character;
	protected JLabel dexLabel;
	protected JFormattedTextField baseInit;
	protected JLabel totLabel;

	public CharacterInitiativePanel(Character c) {
		character = c;

		setBorder(new TitledBorder("Initiative"));

		dexLabel = new JLabel("Dex Mod: "+character.getAbilityModifier(Creature.ABILITY_DEXTERITY));
		add(dexLabel);

		add(new JLabel("Base:"));

		baseInit = new JFormattedTextField();
		baseInit.setValue(new Integer(character.getBaseInitiative()));
		baseInit.setColumns(3);
		baseInit.addPropertyChangeListener("value", new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				if (evt.getPropertyName().equals("value")) {
					int total = (Integer)baseInit.getValue();
					character.setInitiativeModifier(total);
				}
			}
		});
		add(baseInit);

		totLabel = new JLabel("Total: "+character.getInitiativeModifier());
		add(totLabel);

		// update fields when character changes
		character.addPropertyChangeListener(this);
	}

	public void propertyChange(PropertyChangeEvent arg0) {
		if (arg0.getPropertyName().equals(Creature.PROPERTY_ABILITY_PREFIX+"Dexterity")) {
			dexLabel.setText("Dex Mod: "+character.getAbilityModifier(Creature.ABILITY_DEXTERITY));
		} else if (arg0.getPropertyName().equals(Creature.PROPERTY_INITIATIVE)) {
			totLabel.setText("Total: "+character.getInitiativeModifier());
			baseInit.setValue(character.getBaseInitiative());
		}
	}
}
